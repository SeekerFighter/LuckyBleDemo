package com.seeker.luckyble.connect.task

import android.bluetooth.BluetoothGatt
import com.seeker.luckyble.connect.BleErrorCode
import com.seeker.luckyble.connect.BleGattProfile
import com.seeker.luckyble.connect.callbacks.CharacterReadCallback

/**
 *@author  Seeker
 *@date    2018/11/26/026  16:45
 *@describe 数据写入
 */
class CharacterReadTask(
    gattProfile:BleGattProfile,
    private val bluetoothGatt:BluetoothGatt?,
    serviceUUID:String,
    characterUUID:String,
    private val callbacks: MutableList<CharacterReadCallback>?
) : AbsTask(gattProfile, bluetoothGatt, serviceUUID, characterUUID){

    override fun process() {
        if (checkValid()){
            if (bluetoothGatt!!.readCharacteristic(gattCharacter!!)){
                callbacks?.let {
                    for (c in it){
                        c.onCharacterRead()
                    }
                }
            }else{
                handlerError("[$key]读取数据失败")
            }
        }
    }

    override fun handlerError(msg:String){
        super.handlerError(msg)
        BleErrorCode.READ_ERROR.errorMsg = msg
        callbacks?.let {
            for (c in it){
                c.onCharacterRead(BleErrorCode.READ_ERROR)
            }
        }
    }
}