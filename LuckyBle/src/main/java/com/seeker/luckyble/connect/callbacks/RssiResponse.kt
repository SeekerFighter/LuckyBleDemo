package com.seeker.luckyble.connect.callbacks

/**
 *@author  Seeker
 *@date    2018/11/27/027  17:08
 *@describe 信号强度回调
 */
interface RssiResponse {
    fun onRssiResponse(rssi:Int)
}