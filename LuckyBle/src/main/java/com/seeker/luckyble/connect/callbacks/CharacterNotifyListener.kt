package com.seeker.luckyble.connect.callbacks

import com.seeker.luckyble.connect.BleErrorCode

/**
 *@author  Seeker
 *@date    2018/11/28/028  9:46
 *@describe 蓝牙字段打开notify监听
 */
interface CharacterNotifyListener {

    //当descriptor写入回调成功的时候才去返回
    fun onCharacterNotify(errorCode: BleErrorCode = BleErrorCode.SUCCESS)

}