package com.seeker.luckyble.connect.callbacks

import com.seeker.luckyble.connect.BleGattProfile

/**
 *@author  Seeker
 *@date    2018/11/24/024  13:04
 *@describe 蓝牙连接回调
 */
interface ConnectCallback {

    /**
     * 连接成功
     */
    fun onConnected()

    /**
     * 服务获取回调
     */
    fun onServicesFounded(success:Boolean,bleGattProfile: BleGattProfile?)

    /**
     * 断开连接
     */
    fun onDisConnect()

}