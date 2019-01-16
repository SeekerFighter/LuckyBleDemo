package com.seeker.luckyble.connect.callbacks

import com.seeker.luckyble.connect.BleErrorCode
import com.seeker.luckyble.request.Request

/**
 *@author  Seeker
 *@date    2018/11/28/028  9:38
 *@describe 蓝牙设备数据写入监听
 */
interface CharacterWriterCallback {
    fun onCharacterWriter(request: Request, errorCode: BleErrorCode = BleErrorCode.SUCCESS)
}