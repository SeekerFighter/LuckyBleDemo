package com.seeker.luckyble

import android.bluetooth.BluetoothGattCharacteristic
import android.bluetooth.BluetoothGattDescriptor
import android.content.Context
import com.seeker.luckyble.connect.callbacks.*
import com.seeker.luckyble.request.Request
import com.seeker.luckyble.scan.BleScanCallback
import com.seeker.luckyble.scan.BleScanProcessor
import com.seeker.luckyble.scan.ScanOptions
import com.seeker.luckyble.upgrade.Loader
import com.seeker.luckyble.upgrade.UpgradeImpl
import com.seeker.luckyble.upgrade.UpgradeListener
import com.seeker.luckyble.utils.getUIProxy

/**
 *@author  Seeker
 *@date    2018/11/24/024  13:17
 *@describe 蓝牙客户端管理
 */
class BluetoothManager : DisConnect {

    private var globalContext: Context? = null

    private val clientMap = hashMapOf<String, BluetoothClient>()

    companion object {
        private const val TAG = "BluetoothManager"
        val instance: BluetoothManager by lazy(LazyThreadSafetyMode.SYNCHRONIZED) { BluetoothManager() }
    }

    fun firstGlobalInit(context: Context) {
        this.globalContext = context.applicationContext
    }

    /**
     * 蓝牙扫描
     * @param scanCallback 扫描回调接口,运行在主线程里面
     * @param scanOptions 扫描规则设置
     */
    @JvmOverloads
    fun scan(scanOptions: ScanOptions = ScanOptions(), scanCallback: BleScanCallback) {
        BleScanProcessor.instance.scan(scanCallback.getUIProxy(true), scanOptions)
    }

    /**
     * 停止蓝牙扫描
     */
    fun stopScan() {
        BleScanProcessor.instance.stop()
    }

    /**
     * 连接
     * @param macAddress 蓝牙地址
     * @param autoDiscoverServices 连接成功之后是否主动去查找服务
     * @param connectCallback 回调，运行在主线程
     */
    @Synchronized
    @JvmOverloads
    fun connect(
        macAddress: String,
        autoDiscoverServices: Boolean = true,
        upgradeImpl: UpgradeImpl? = null,
        connectCallback: ConnectCallback
    ) {
        globalContext?.let { context ->
            var client: BluetoothClient? = clientMap[macAddress]
            if (client == null) {
                client = BluetoothClient.instance
                client.initClient(context, macAddress, this, upgradeImpl)
                clientMap[macAddress] = client
                client.connect(autoDiscoverServices, connectCallback.getUIProxy(true))
            } else {
                BleLogger.e(TAG, "macAddress[$macAddress] possible connected,need call disConnect(mac) function first")
            }
        } ?: throw NullPointerException("globalContext == null,have you invoke firstGlobalInit(Context)?")
    }

    /**
     * 断开连接
     */
    fun disConnect(macAddress: String) = clientMap[macAddress]?.disConnect()

    /**
     * 查找服务
     */
    fun discoverServices(macAddress: String) = clientMap[macAddress]?.discoverServices()

    /**
     * 开始蓝牙设备升级
     */
    @JvmOverloads
    fun startBleUpgrade(macAddress: String, loader: Loader, listener: UpgradeListener, postUI: Boolean = true) =
        clientMap[macAddress]?.startBleUpgrade(loader, listener.getUIProxy(postUI))

    /**
     * 打开notify以及写入descriptor
     */
    @JvmOverloads
    fun enableNotify(
        macAddress: String,
        serviceUUID: String,
        characterUUID: String,
        enable: Boolean = true,
        descriptorValue: ByteArray = if (enable) BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE else BluetoothGattDescriptor.DISABLE_NOTIFICATION_VALUE,
        characterListener: CharacterListener? = null,
        postUI: Boolean = false
    ) {
        characterListener?.let {
            registerCharacterNotifyListener(
                macAddress,
                serviceUUID,
                characterUUID,
                it as CharacterNotifyListener,
                postUI
            )
            registerCharacterChangedListener(
                macAddress,
                serviceUUID,
                characterUUID,
                it as CharacterChangedListener,
                postUI
            )
        }
        clientMap[macAddress]?.enableNotify(serviceUUID, characterUUID, enable, descriptorValue)
    }

    /**
     * 写入数据
     */
    @JvmOverloads
    fun write(
        macAddress: String,
        serviceUUID: String,
        characterUUID: String,
        request: Request,
        writerType: Int = BluetoothGattCharacteristic.WRITE_TYPE_NO_RESPONSE,
        callback: CharacterWriterCallback? = null,
        postUI: Boolean = false
    ) =
        clientMap[macAddress]?.write(serviceUUID, characterUUID, request, writerType, callback?.getUIProxy(postUI))

    /**
     * 读取数据
     */
    @JvmOverloads
    fun read(
        macAddress: String,
        serviceUUID: String,
        characterUUID: String,
        callback: CharacterReadCallback? = null,
        postUI: Boolean = false
    ) =
        clientMap[macAddress]?.read(serviceUUID, characterUUID, callback?.getUIProxy(postUI))

    /**
     * 读取信号强度
     */
    @JvmOverloads
    fun readRemoteRssi(macAddress: String, rssiResponse: RssiResponse, postUI: Boolean = false) =
        clientMap[macAddress]?.readRemoteRssi(rssiResponse.getUIProxy(postUI))

    /**
     * 注册字段notify监听
     */
    @JvmOverloads
    fun registerCharacterNotifyListener(
        macAddress: String,
        serviceUUID: String,
        characterUUID: String,
        listener: CharacterNotifyListener,
        postUI: Boolean = false
    ) = clientMap[macAddress]?.registerCharacterNotifyListener(
        serviceUUID,
        characterUUID,
        listener.getUIProxy(postUI)
    )

    /**
     * 注册字段变化监听
     */
    @JvmOverloads
    fun registerCharacterChangedListener(
        macAddress: String,
        serviceUUID: String,
        characterUUID: String,
        listener: CharacterChangedListener,
        postUI: Boolean = false
    ) = clientMap[macAddress]?.registerCharacterChangedListener(
        serviceUUID,
        characterUUID,
        listener.getUIProxy(postUI)
    )

    /**
     * 注册字段写入监听
     */
    @JvmOverloads
    fun registerCharacterWriterListener(
        macAddress: String,
        serviceUUID: String,
        characterUUID: String,
        listener: CharacterWriterCallback,
        postUI: Boolean = false
    ) = clientMap[macAddress]?.registerCharacterWriterListener(
        serviceUUID,
        characterUUID,
        listener.getUIProxy(postUI)
    )

    override fun clientDisConnect(macAddress: String) {
        clientMap.remove(macAddress)
    }

    /**
     * 是否打印
     */
    fun setDebug(debug: Boolean) = BleLogger.setDebug(debug)
}