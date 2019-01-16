package com.seeker.luckyble.connect.callbacks

/**
 *@author  Seeker
 *@date    2018/11/28/028  9:38
 *@describe 蓝牙设备字段变化监听
 */
interface CharacterChangedListener {
    fun onCharacterChanged(value:ByteArray)
}