package com.seeker.luckyble.connect.task

import android.bluetooth.BluetoothGatt
import com.seeker.luckyble.connect.BleErrorCode
import com.seeker.luckyble.connect.BleGattProfile
import com.seeker.luckyble.connect.callbacks.CharacterWriterCallback
import com.seeker.luckyble.request.Request

/**
 *@author  Seeker
 *@date    2018/11/26/026  16:45
 *@describe 数据写入
 */
class CharacterWriterTask(
    gattProfile:BleGattProfile,
    private val bluetoothGatt:BluetoothGatt?,
    serviceUUID:String,
    characterUUID:String,
    private val request: Request,
    private val writerType: Int,
    private val writerCallbacksMap: MutableList<CharacterWriterCallback>?
) : AbsTask(gattProfile, bluetoothGatt, serviceUUID, characterUUID){

    override fun process() {
        if (checkValid()){
            val character = gattCharacter!!
            character.value = request.dataArray()
            character.writeType = writerType
            if (bluetoothGatt!!.writeCharacteristic(gattCharacter)){
                writerCallbacksMap?.let {
                    for (listener in it){
                        listener.onCharacterWriter(request)
                    }
                }
            }else{
                handlerError("[$key]写入数据失败")
            }
        }
    }

    override fun handlerError(msg: String) {
        super.handlerError(msg)
        writerCallbacksMap?.let {
            BleErrorCode.WRITER_ERROR.errorMsg = msg
            for (listener in it) {
                listener.onCharacterWriter(request, BleErrorCode.WRITER_ERROR)
            }
        }
    }

}