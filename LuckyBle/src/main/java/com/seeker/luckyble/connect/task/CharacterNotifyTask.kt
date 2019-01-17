package com.seeker.luckyble.connect.task

import android.bluetooth.BluetoothGatt
import com.seeker.luckyble.connect.BleErrorCode
import com.seeker.luckyble.connect.BleGattProfile
import com.seeker.luckyble.connect.callbacks.CharacterNotifyListener
import java.util.*

/**
 *@author  Seeker
 *@date    2018/11/26/026  16:45
 *@describe 打开notify
 */
class CharacterNotifyTask(
    gattProfile:BleGattProfile,
    private val bluetoothGatt:BluetoothGatt?,
    serviceUUID:String,
    characterUUID:String,
    private val enable:Boolean,
    private val descriptorValue:ByteArray,
    private val notifyListeners: MutableList<CharacterNotifyListener>?
) : AbsTask(gattProfile, bluetoothGatt, serviceUUID, characterUUID){

    companion object {
        val CLIENT_CHARACTERISTIC_CONFIG = UUID.fromString("00002902-0000-1000-8000-00805f9b34fb")!!
    }

    override fun process() {
        if (checkValid()){
            val character = gattCharacter!!
            val gatt = bluetoothGatt!!
            if (gatt.setCharacteristicNotification(character,enable)){
                val descriptor = character.getDescriptor(CLIENT_CHARACTERISTIC_CONFIG)
                descriptor?.let {des->
                    if (des.setValue(descriptorValue) && gatt.writeDescriptor(des)){
                        notifyListeners?.let {listeners->
                            for (listener in listeners){
                                listener.onCharacterNotify()
                            }
                        }
                    }else{
                        handlerError("writeDescriptor for [$key] failed!")
                    }
                }?:handlerError("getDescriptor for [$key] is null!")
            }else{
                handlerError("setNotify for [$key] failed!")
            }
        }
    }

    override fun handlerError(msg:String){
        super.handlerError(msg)
        notifyListeners?.let {listeners->
            BleErrorCode.NOTIFY_ERROR.errorMsg = msg
            for (listener in listeners){
                listener.onCharacterNotify(BleErrorCode.NOTIFY_ERROR)
            }
        }
    }
}