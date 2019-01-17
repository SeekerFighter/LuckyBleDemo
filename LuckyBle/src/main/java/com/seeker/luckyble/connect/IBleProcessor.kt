package com.seeker.luckyble.connect

import android.content.Context
import com.seeker.luckyble.connect.callbacks.*
import com.seeker.luckyble.request.Request
import com.seeker.luckyble.upgrade.Loader
import com.seeker.luckyble.upgrade.UpgradeImpl
import com.seeker.luckyble.upgrade.UpgradeListener

/**
 *@author  Seeker
 *@date    2018/11/22/022  15:16
 *@describe 蓝牙操作相关函数接口
 */
interface IBleProcessor {

    fun initProcessor(context: Context, macAddress: String,disConnect: DisConnect,upgrade: UpgradeImpl?)

    /**
     * 连接外设
     * @param autoDiscoverServices 连接成功是否自动调用发现服务函数
     * @param connectCallback 蓝牙设备连接状态回调接口
     */
    fun connect(autoDiscoverServices:Boolean,connectCallback: ConnectCallback)

    /**
     * 断开连接
     */
    fun disConnect()

    /**
     * 获取服务字段
     */
    fun discoverServices()

    /**
     * 开始升级蓝牙设备
     */
    fun startBleUpgrade(loader: Loader, listener: UpgradeListener)

    /**
     * 打开notify
     */
    fun enableNotify(serviceUUID:String,characterUUID:String,enable:Boolean,descriptorValue:ByteArray)

    /**
     * 写入数据
     */
    fun write(serviceUUID:String, characterUUID:String, request: Request,writerType: Int,callback: CharacterWriterCallback?)

    /**
     * 读取数据
     */
    fun read(serviceUUID: String,characterUUID: String,callback: CharacterReadCallback?)

    /**
     * 读取信号强度
     */
    fun readRemoteRssi(rssiResponse: RssiResponse)

    fun registerCharacterNotifyListener(serviceUUID: String,characterUUID: String,listener: CharacterNotifyListener)

    fun registerCharacterChangedListener(serviceUUID: String,characterUUID: String,listener: CharacterChangedListener)

    fun registerCharacterWriterListener(serviceUUID: String,characterUUID: String,listener: CharacterWriterCallback)
}