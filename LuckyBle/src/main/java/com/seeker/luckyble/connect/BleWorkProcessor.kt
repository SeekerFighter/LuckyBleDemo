package com.seeker.luckyble.connect

import android.bluetooth.*
import android.content.Context
import android.os.Handler
import android.os.HandlerThread
import android.os.Looper
import com.seeker.luckyble.BleLogger
import com.seeker.luckyble.connect.callbacks.*
import com.seeker.luckyble.connect.task.CharacterNotifyTask
import com.seeker.luckyble.connect.task.CharacterReadTask
import com.seeker.luckyble.connect.task.CharacterWriterTask
import com.seeker.luckyble.request.Request
import com.seeker.luckyble.upgrade.Loader
import com.seeker.luckyble.upgrade.UpgradeImpl
import com.seeker.luckyble.upgrade.UpgradeListener
import com.seeker.luckyble.utils.*

/**
 *@author  Seeker
 *@date    2018/11/24/024  12:58
 *@describe 蓝牙连接操作管理类
 */
internal class BleWorkProcessor: BluetoothGattCallback(), IBleProcessor {

    private lateinit var bleContext:Context

    private lateinit var macAddress: String

    private var upgrade: UpgradeImpl? = null

    private val workLooper: Looper

    private lateinit var mBluetoothDevice:BluetoothDevice

    private var mBluetoothGatt:BluetoothGatt? = null

    private val workHandler:Handler

    private var autoDiscoverServices:Boolean = true

    private var connectCallback: ConnectCallback? = null

    private lateinit var disConnect: DisConnect

    private val mBleGattProfile:BleGattProfile

    private val characterNotifyListenersMap:MutableMap<String,MutableList<CharacterNotifyListener>>
    private val characterChangedListenersMap:MutableMap<String,MutableList<CharacterChangedListener>>
    private val characterWriterCallbacksMap:MutableMap<String,MutableList<CharacterWriterCallback>>
    private val characterReadCallbacksMap:MutableMap<String,MutableList<CharacterReadCallback>>

    private val bleDispatcher:BleDispatcher = BleDispatcher()

    private val rssiResponses: MutableList<RssiResponse>

    init {
        val mWorkerThread = HandlerThread(TAG)
        mWorkerThread.start()
        workLooper = mWorkerThread.looper
        workHandler = Handler(workLooper)
        mBleGattProfile = BleGattProfile()
        characterNotifyListenersMap = mutableMapOf()
        characterChangedListenersMap = mutableMapOf()
        characterWriterCallbacksMap = mutableMapOf()
        rssiResponses = mutableListOf()
        characterReadCallbacksMap = mutableMapOf()
    }

    companion object {
        private const val TAG = "BleConnectProcessor"
        fun newInstance():IBleProcessor{
            val connectProcessor = BleWorkProcessor()
            return (connectProcessor as IBleProcessor).getUIProxy(connectProcessor.workLooper)
        }
    }

    override fun initProcessor(context: Context,macAddress: String,disConnect: DisConnect,upgrade: UpgradeImpl?){
        this.bleContext = context
        this.macAddress = macAddress
        this.mBluetoothDevice = bluetoothAdapter().getRemoteDevice(macAddress)
        this.disConnect = disConnect
        this.upgrade = upgrade
    }

    override fun connect(autoDiscoverServices:Boolean,connectCallback: ConnectCallback) {
        BleLogger.v(TAG,"connect called() for macAddress[$macAddress], autoDiscoverServices = [$autoDiscoverServices]")
        this.autoDiscoverServices = autoDiscoverServices
        this.connectCallback = connectCallback
        val delay = mBluetoothGatt?.closeGatt() ?:0L
        mBluetoothGatt = null
        workHandler.postDelayed({
            mBluetoothGatt = if (isBigger23()){
                mBluetoothDevice.connectGatt(bleContext,false,this,BluetoothDevice.TRANSPORT_LE)
            }else{
                mBluetoothDevice.connectGatt(bleContext,false,this)
            }
            if (mBluetoothGatt == null){
                BleLogger.e(TAG,"open BluetoothGatt for [$macAddress] error,mBluetoothGatt == null")
                disConnect()
            }
        },delay)
    }

    override fun disConnect() {
        BleLogger.v(TAG,"disConnect() called for macAddress[$macAddress]")
        mBluetoothGatt?.disconnect()?:handlerDisConnectStatus()
    }

    override fun discoverServices() {
        BleLogger.v(TAG,"discoverServices() called for macAddress[$macAddress]")
        if (mBluetoothGatt?.discoverServices() != true){
            BleLogger.e(TAG,"discoverServices for macAddress[$macAddress] error,now disConnect!!!")
            disConnect()
        }
    }

    override fun startBleUpgrade(loader: Loader, listener: UpgradeListener) {
        upgrade?.startBleUpgrade(loader, listener,this)
    }

    override fun enableNotify(serviceUUID: String,characterUUID: String,enable:Boolean,descriptorValue:ByteArray) {
        val key = generatePrivateKey(serviceUUID,characterUUID)
        BleLogger.v(TAG,"enableNotify() called with [$key],enable = [$enable]")
        bleDispatcher.addNewTask(CharacterNotifyTask(mBleGattProfile,mBluetoothGatt,serviceUUID, characterUUID, enable,descriptorValue,characterNotifyListenersMap[key]))
    }

    override fun write(serviceUUID: String, characterUUID: String, request: Request,writerType: Int,callback: CharacterWriterCallback?) {
        val key = generatePrivateKey(serviceUUID,characterUUID)
        BleLogger.v(TAG,"write for [$key],response = [$writerType]")
        callback?.let {
            characterWriterCallbacksMap.putE(key,it)
        }
        bleDispatcher.addNewTask(CharacterWriterTask(mBleGattProfile,mBluetoothGatt,serviceUUID, characterUUID, request,writerType,characterWriterCallbacksMap[key]))
    }

    override fun read(serviceUUID: String, characterUUID: String, callback: CharacterReadCallback?) {
        val key = generatePrivateKey(serviceUUID,characterUUID)
        BleLogger.v(TAG,"read for [$key]")
        callback?.let {
            characterReadCallbacksMap.putE(key,it)
        }
        bleDispatcher.addNewTask(CharacterReadTask(mBleGattProfile,mBluetoothGatt,serviceUUID, characterUUID,characterReadCallbacksMap[key]))
    }

    override fun readRemoteRssi(rssiResponse: RssiResponse) {
        BleLogger.v(TAG,"readRemoteRssi() called...")
        rssiResponses.addE(rssiResponse)
        mBluetoothGatt?.readRemoteRssi()
    }

    override fun registerCharacterNotifyListener(
        serviceUUID: String,
        characterUUID: String,
        listener: CharacterNotifyListener
    ) = characterNotifyListenersMap.putE(generatePrivateKey(serviceUUID,characterUUID),listener)

    override fun registerCharacterChangedListener(
        serviceUUID: String,
        characterUUID: String,
        listener: CharacterChangedListener
    ) = characterChangedListenersMap.putE(generatePrivateKey(serviceUUID,characterUUID),listener)

    override fun registerCharacterWriterListener(
        serviceUUID: String,
        characterUUID: String,
        listener: CharacterWriterCallback
    )  = characterWriterCallbacksMap.putE(generatePrivateKey(serviceUUID,characterUUID),listener)


    override fun onConnectionStateChange(gatt: BluetoothGatt, status: Int, newState: Int) {
        BleLogger.v(TAG,"onConnectionStateChange() called with:status = [$status],newState = [$newState]")
        if (status == BluetoothGatt.GATT_SUCCESS && newState == BluetoothProfile.STATE_CONNECTED) {
            connectCallback?.onConnected()
            if (autoDiscoverServices){
                workHandler.postDelayed({discoverServices()},500)
            }
        }else{
            val delay = mBluetoothGatt?.closeGatt()?:0L
            mBluetoothGatt = null
            workHandler.postDelayed({handlerDisConnectStatus()},delay)
        }
    }

    override fun onServicesDiscovered(gatt: BluetoothGatt, status: Int) {
        BleLogger.v(TAG,"onServicesDiscovered() called with status = [$status]")
        if (status == BluetoothGatt.GATT_SUCCESS) {
            mBleGattProfile.confirmCache(mBluetoothGatt)
            connectCallback?.onServicesFounded(true,mBleGattProfile,
                upgrade?.isUpgradeMode(mBleGattProfile,mBluetoothGatt) ?:false)
        }else{
            connectCallback?.onServicesFounded(false,null,false)
        }
    }

    override fun onCharacteristicChanged(gatt: BluetoothGatt, characteristic: BluetoothGattCharacteristic) {
        if (upgrade != null && upgrade!!.isUpgradeMode){
            upgrade!!.onCharacteristicChanged(gatt, characteristic)
            return
        }
        val key = characteristic.key()
        BleLogger.v(TAG,"onCharacteristicChanged()called for [$key]")
        characterChangedListenersMap[key]?.let {
            for (listener in it){
                listener.onCharacterChanged(characteristic.value)
            }
        }
    }

    override fun onCharacteristicWrite(gatt: BluetoothGatt,characteristic: BluetoothGattCharacteristic,status: Int) {
        if (upgrade != null && upgrade!!.isUpgradeMode){
            upgrade!!.onCharacteristicWrite(gatt, characteristic,status)
            return
        }
        BleLogger.v(TAG,"onCharacteristicWrite()called for [${characteristic.key()}],status = [$status]")
        bleDispatcher.releaseDeviceBusy()
    }

    override fun onCharacteristicRead(gatt: BluetoothGatt, characteristic: BluetoothGattCharacteristic, status: Int) {
        val key = characteristic.key();
        BleLogger.v(TAG,"onCharacteristicRead()called for [$key],status = [$status]")
        with(characteristic.value){
            characterReadCallbacksMap[key]?.let {
                for (callback in it){
                    callback.onCharacterResponse(this)
                }
            }

        }
        bleDispatcher.releaseDeviceBusy()
    }

    override fun onDescriptorWrite(gatt: BluetoothGatt, descriptor: BluetoothGattDescriptor, status: Int) {
        descriptor.characteristic?.let {
            BleLogger.v(TAG,"onDescriptorWrite()called for [${it.key()}],status = [$status]")
        }
        bleDispatcher.releaseDeviceBusy()
        if (upgrade != null && upgrade!!.isUpgradeMode){
            upgrade!!.onDescriptorWrite(gatt, descriptor,status)
        }
    }

    override fun onReadRemoteRssi(gatt: BluetoothGatt, rssi: Int, status: Int) {
        BleLogger.v(TAG,"onReadRemoteRssi()called with status = [$status],rssi = [$rssi]")
        for (rr in rssiResponses){
            rr.onRssiResponse(rssi)
        }
    }

    /**
     * 传递连接断开结果
     */
    private fun handlerDisConnectStatus(){
        mBleGattProfile.releaseCache()
        characterNotifyListenersMap.clear()
        characterChangedListenersMap.clear()
        characterWriterCallbacksMap.clear()
        rssiResponses.clear()
        characterReadCallbacksMap.clear()
        bleDispatcher.releaseCache()
        disConnect.clientDisConnect(macAddress)
        connectCallback?.onDisConnect()
    }

}