package com.seeker.luckyble

import android.bluetooth.BluetoothManager
import android.content.Context
import com.seeker.luckyble.connect.BleConnectProcessor
import com.seeker.luckyble.connect.IBleProcessor
import com.seeker.luckyble.connect.callbacks.*
import com.seeker.luckyble.exception.NonSupportException
import com.seeker.luckyble.request.Request
import com.seeker.luckyble.utils.isSupportBle

/**
 *@author  Seeker
 *@date    2018/11/22/022  10:45
 *@describe 蓝牙操作客户端
 */
internal class BluetoothClient {

    private lateinit var bleContext: Context

    private lateinit var bluetoothManager: BluetoothManager

    private val connectProcessor: IBleProcessor = BleConnectProcessor.newInstance()

    private lateinit var macAddress: String

    companion object {
        val instance: BluetoothClient by lazy(LazyThreadSafetyMode.SYNCHRONIZED) { BluetoothClient() }
    }

    /**
     * step 1
     * 初始化蓝牙相关参数
     */
    fun initClient(context: Context, macAddress: String, disConnect: DisConnect) {
        this.macAddress = macAddress
        this.bleContext = context
        if (bleContext.isSupportBle()) {
            bluetoothManager = bleContext.getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
            connectProcessor.initProcessor(bleContext, macAddress, disConnect)
        } else {
            throw NonSupportException("当前设备不支持蓝牙操作")
        }
    }

    fun connect(autoDiscoverServices: Boolean, connectCallback: ConnectCallback) =
        connectProcessor.connect(autoDiscoverServices, connectCallback)

    fun disConnect() = connectProcessor.disConnect()

    fun discoverServices() = connectProcessor.discoverServices()

    fun enableNotify(serviceUUID: String, characterUUID: String, enable: Boolean, descriptorValue: ByteArray) =
        connectProcessor.enableNotify(serviceUUID, characterUUID, enable, descriptorValue)

    fun write(
        serviceUUID: String,
        characterUUID: String,
        request: Request,
        writerType: Int,
        callback: CharacterWriterCallback?
    ) = connectProcessor.write(serviceUUID, characterUUID, request, writerType, callback)

    fun read(
        serviceUUID: String,
        characterUUID: String,
        callback: CharacterReadCallback?
    ) = connectProcessor.read(serviceUUID, characterUUID, callback)

    fun readRemoteRssi(rssiResponse: RssiResponse) = connectProcessor.readRemoteRssi(rssiResponse)

    fun registerCharacterNotifyListener(
        serviceUUID: String,
        characterUUID: String,
        listener: CharacterNotifyListener
    ) = connectProcessor.registerCharacterNotifyListener(serviceUUID, characterUUID, listener)

    fun registerCharacterChangedListener(
        serviceUUID: String,
        characterUUID: String,
        listener: CharacterChangedListener
    ) = connectProcessor.registerCharacterChangedListener(serviceUUID, characterUUID, listener)

    fun registerCharacterWriterListener(
        serviceUUID: String,
        characterUUID: String,
        listener: CharacterWriterCallback
    ) = connectProcessor.registerCharacterWriterListener(serviceUUID, characterUUID, listener)

}