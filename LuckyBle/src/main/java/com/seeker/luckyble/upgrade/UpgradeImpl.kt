package com.seeker.luckyble.upgrade

import android.bluetooth.BluetoothGatt
import android.bluetooth.BluetoothGattCharacteristic
import android.bluetooth.BluetoothGattDescriptor
import android.os.Handler
import android.os.HandlerThread
import android.os.Message
import com.seeker.luckyble.BleLogger
import com.seeker.luckyble.connect.BleGattProfile
import com.seeker.luckyble.connect.IBleProcessor
import com.seeker.luckyble.connect.task.CharacterNotifyTask
import com.seeker.luckyble.utils.isLollipop
import java.util.*

/**
 *@author  Seeker
 *@date    2019/1/16/016  17:47
 *@describe 升级模式下,ota 升级（Ti oad模式）
 */
class UpgradeImpl(
    private val oadServer: String,
    private val oadBlock: String,
    private val oadIdentity: String
) : UpgradeProcess, Handler.Callback {

    companion object {
        private const val TAG = "UpgradeImpl"
        private const val INTERVAL = 5//写入索引值与notify索引值相差不超过5个
        private const val BLOCK_NOTIFY_TIMEOUT = 5_000L
        private const val FAIL = -1
        private const val WRITERED = 0//写入完成
    }

    private lateinit var listener: UpgradeListener

    private lateinit var bleProcess: IBleProcessor

    var isUpgradeMode: Boolean = false

    private lateinit var bleGatt: BluetoothGatt

    private lateinit var bleGattProfile: BleGattProfile

    private var blockCharacter: BluetoothGattCharacteristic? = null
    private var identityCharacter: BluetoothGattCharacteristic? = null

    private val dataProvider = DataProvider()

    private var mUpgrading: Boolean = false//是否正在升级

    /**
     * 保存上一个写入的block索引,用来和当前的索引判断，上一个索引值的block是否写入成功
     */
    private var mPreBlockIndex: Short = -1

    /**
     * 最后写入成功的索引值
     */
    private var mWriterIndex: Short = 0

    /**
     * block判断写入失败后，是否重置过block的索引值，重置过则不在重置
     */
    private var hasResetBlockIndex: Boolean = false

    /**
     * 是否完成，判断条件为，当block数据获取为null时，置为true，
     * 然后心电仪会断开连接，这个时候认为升级已经完毕
     */
    private var writerComplete: Boolean = false

    private var mDeviceBusy: Boolean = false

    private val mHandler: Handler

    init {
        val ht = HandlerThread("oadUpgrade_ht")
        ht.start()
        this.mHandler = Handler(ht.looper, this)
    }

    /**
     * 判断是否处于升级模式
     */
    fun isUpgradeMode(bleGattProfile: BleGattProfile, gatt: BluetoothGatt?): Boolean = bleGattProfile.let {
        isUpgradeMode = it.containsCharacterInService(UUID.fromString(oadServer), UUID.fromString(oadBlock)) &&
                it.containsCharacterInService(UUID.fromString(oadServer), UUID.fromString(oadIdentity))
        BleLogger.v(TAG, "isUpgradeMode()called[$isUpgradeMode]")
        if (isUpgradeMode) {
            this.bleGatt = gatt!!
            this.bleGattProfile = it
            this.blockCharacter = it.getGattCharacter(gatt, oadServer, oadBlock)
            this.identityCharacter = it.getGattCharacter(gatt, oadServer, oadIdentity)
        }
        isUpgradeMode
    }

    /**
     * 开始升级
     */
    fun startBleUpgrade(loader: Loader, listener: UpgradeListener, bleProcess: IBleProcessor) {
        BleLogger.v(TAG, "startBleUpgrade() called,${loader.javaClass.simpleName}, isUpgradeMode = $isUpgradeMode")
        if (!isUpgradeMode) {
            return
        }
        this.dataProvider.setSourceBinLoader(loader)
        this.listener = listener
        this.bleProcess = bleProcess
        this.bleProcess.enableNotify(oadServer, oadIdentity, true, BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE)
    }

    private fun isNotificationEnabled(character: String, gatt: BluetoothGatt): Boolean =
        gatt.getService(UUID.fromString(oadServer))?.getCharacteristic(UUID.fromString(character))?.getDescriptor(
            CharacterNotifyTask.CLIENT_CHARACTERISTIC_CONFIG
        )?.value?.contentEquals(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE) ?: false

    private fun reset() {
        this.dataProvider.reset()
        this.mUpgrading = true
        this.mPreBlockIndex = -1
        this.mWriterIndex = 0
        this.writerComplete = false
    }

    private fun writerCharIdentify() = identityCharacter?.let {
        writeCharacteristicWithNoRsp(it, dataProvider.identifyValues)
    } ?: listener.onUpgradeFail("Identity字段异常")

    @Synchronized
    private fun writerCharBlock() = blockCharacter?.let { c ->
        synchronized(mDeviceBusy) {
            if (!mDeviceBusy) {
                dataProvider.blockValues?.let {
                    mDeviceBusy = true
                    writeCharacteristicWithNoRsp(c, it)
                }
            }
        }
    } ?: listener.onUpgradeFail("Block")

    private fun writeCharacteristicWithNoRsp(character: BluetoothGattCharacteristic, values: ByteArray) {
        character.value = values
        character.writeType = BluetoothGattCharacteristic.WRITE_TYPE_NO_RESPONSE
        bleGatt.writeCharacteristic(character)
    }

    override fun onCharacteristicWrite(gatt: BluetoothGatt, characteristic: BluetoothGattCharacteristic, status: Int) {
        synchronized(mDeviceBusy) {
            mDeviceBusy = false
        }
        if (!mUpgrading) {
            return
        }
        if (checkBlockCharacteristic(characteristic)) {
            val values = characteristic.value
            mWriterIndex = ByteHelper.buildUint16(values[1], values[0])
            writerComplete = dataProvider.isWriterFinished
            mUpgrading = !writerComplete

            if (!writerComplete){
                synchronized(hasResetBlockIndex) {
                    if (mWriterIndex - mPreBlockIndex < INTERVAL) {
                        if (!hasResetBlockIndex) {
                            writerCharBlock()
                        }
                    }
                }
            }else{
                /**
                 * 设备端bug处理:
                 * 1.由于固件端在写入完成之后，主动断开连接,最后的几个notify不一定能收到回调，所以无法用notify的index来判断写入是否成功。
                 * 2.升级成功之后，会重启，但是没有主动调用断开连接的代码，所以app端无法收到断开通知，只能通过超时来判断，所以现在暂时延时
                 * 1秒来处理这部分逻辑
                 */
                mHandler.sendEmptyMessageDelayed(WRITERED,1000)
            }
        }
    }

    override fun onCharacteristicChanged(gatt: BluetoothGatt, characteristic: BluetoothGattCharacteristic) {

        if (!mUpgrading && !writerComplete) {
            return
        }
        if (checkBlockCharacteristic(characteristic)) {
            mHandler.removeMessages(FAIL)
            val values = characteristic.value
            val nextIndex = ByteHelper.buildUint16(values[1], values[0])
            if (mPreBlockIndex == nextIndex) {
                synchronized(hasResetBlockIndex) {
                    if (!hasResetBlockIndex) {
                        /**
                         * 如果当前的index一直写入不成功，则不会再进入当前分支里面
                         */
                        hasResetBlockIndex = true
                        synchronized(writerComplete) {
                            writerComplete = false
                        }
                        dataProvider.resetBlockIndex(nextIndex)
                        writerCharBlock()
                    } else if (mWriterIndex == mPreBlockIndex) {
                        /**
                         * 当已经判断过写入失败，block索引值校正过之后，写入还是失败，则急速尝试写入当前索引值
                         * 失败之后，不在写入block，所以最后一次写入的索引值一定是失败的索引值，当索引值一样的话，说明写入失败
                         */
                        dataProvider.resetBlockIndex(nextIndex)
                        writerCharBlock()
                    }
                }

            } else {
                synchronized(hasResetBlockIndex) {
                    /**
                     * 如果block写入失败过，则onCharacteristicWrite()不在写入block，直到失败的索引值 的block写入成功，在唤醒写入进程
                     */
                    if (hasResetBlockIndex) {
                        hasResetBlockIndex = false
                        writerCharBlock()
                    }
                }

                if (mWriterIndex - mPreBlockIndex < INTERVAL) {
                    writerCharBlock()
                }

            }
            this.mPreBlockIndex = nextIndex
            if (writerComplete) {
                mHandler.removeMessages(WRITERED)
                mHandler.sendEmptyMessageDelayed(WRITERED,1000)
            }
            if (writerComplete && dataProvider.isUpgradeFinished(mPreBlockIndex)) {
                listener.onUpgradeProgress(1f)
                if (dataProvider.isNotifyChangedComplete(mPreBlockIndex)){
                    mHandler.removeMessages(WRITERED)
                    listener.onUpgradeSuccess()
                }
            } else {
                mHandler.sendEmptyMessageDelayed(FAIL,BLOCK_NOTIFY_TIMEOUT)
                listener.onUpgradeProgress(dataProvider.getBlockProgress(mPreBlockIndex))
            }
        }else{
            writerCharBlock()
        }
    }

    override fun onDescriptorWrite(gatt: BluetoothGatt, descriptor: BluetoothGattDescriptor, status: Int) {
        if (!isNotificationEnabled(oadIdentity, gatt)) {
            this.bleProcess.enableNotify(
                oadServer,
                oadIdentity,
                true,
                BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE)
            return
        }
        if (!isNotificationEnabled(oadBlock, gatt)) {
            this.bleProcess.enableNotify(
                oadServer,
                oadBlock,
                true,
                BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE)
            return
        }
        deadWorkOk()
    }

    override fun handleMessage(msg: Message?): Boolean {
        msg?.let {
            when(it.what){
                FAIL-> listener.onUpgradeFail("长时间未收到字段change响应")
                WRITERED->listener.onUpgradeSuccess()
            }
        }
        return true
    }

    private fun deadWorkOk() {
        this.reset()
        if (isLollipop()) {
            bleGatt.requestConnectionPriority(BluetoothGatt.CONNECTION_PRIORITY_HIGH)
        }
        writerCharIdentify()
    }

    private fun checkBlockCharacteristic(characteristic: BluetoothGattCharacteristic): Boolean =
        characteristic.uuid == UUID.fromString(oadBlock)

}