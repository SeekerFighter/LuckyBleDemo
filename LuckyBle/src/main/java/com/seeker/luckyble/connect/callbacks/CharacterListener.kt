package com.seeker.luckyble.connect.callbacks

import com.seeker.luckyble.connect.BleErrorCode
import com.seeker.luckyble.request.Request

/**
 *@author  Seeker
 *@date    2018/11/26/026  14:43
 *@describe 打开notify回调
 */
interface CharacterListener:CharacterNotifyListener,CharacterChangedListener {

    override fun onCharacterNotify(errorCode: BleErrorCode) {

    }

    override fun onCharacterChanged(value: ByteArray) {

    }
}