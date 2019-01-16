package com.seeker.luckyble.connect.callbacks

/**
 *@author  Seeker
 *@date    2018/11/24/024  13:42
 *@describe [BluetoothClient] 断开的时候，调用此接口，用来通知[BluetoothManager]哪一个客户端断开了,清除掉当前客户端信息
 */
internal interface DisConnect {
    fun clientDisConnect(macAddress:String)
}