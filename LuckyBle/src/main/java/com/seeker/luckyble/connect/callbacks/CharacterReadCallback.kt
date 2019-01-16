package com.seeker.luckyble.connect.callbacks

import com.seeker.luckyble.connect.BleErrorCode

/**
 *@author  Seeker
 *@date    2018/12/6/006  17:36
 *@describe 蓝牙设备读取请求回调
 */
interface CharacterReadCallback:CharacterResponse {
    fun onCharacterRead(errorCode: BleErrorCode = BleErrorCode.SUCCESS)
}