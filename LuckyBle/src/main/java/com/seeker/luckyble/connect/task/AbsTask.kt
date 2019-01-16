package com.seeker.luckyble.connect.task

import android.bluetooth.BluetoothGatt
import android.bluetooth.BluetoothGattCharacteristic
import androidx.annotation.CallSuper
import com.seeker.luckyble.connect.BleDispatcher
import com.seeker.luckyble.connect.BleGattProfile
import com.seeker.luckyble.utils.generatePrivateKey

/**
 *@author  Seeker
 *@date    2018/11/27/027  13:24
 *@describe 任务抽象类
 */
abstract class AbsTask(
    gattProfile: BleGattProfile,
    private val bluetoothGatt: BluetoothGatt?,
    serviceUUID:String,
    characterUUID:String
) :ITask{

    val key = generatePrivateKey(serviceUUID,characterUUID)

    val gattCharacter:BluetoothGattCharacteristic? = gattProfile.getGattCharacter(bluetoothGatt,serviceUUID, characterUUID)

    private lateinit var bleDispatcher:BleDispatcher

    fun checkValid():Boolean{
        if (bluetoothGatt == null){
            handlerError("bluetoothGatt is NULL,possible has disConnect!")
            return false
        }
        if (gattCharacter == null){
            handlerError("没有查找到[$key]的字段信息")
            return false
        }
        return true
    }

    override fun setBleDispatcher(dispatcher: BleDispatcher):ITask{
        this.bleDispatcher = dispatcher
        return this
    }

    @CallSuper
    open fun handlerError(msg:String){
        bleDispatcher.dispatcherHandler.postDelayed({
            bleDispatcher.releaseDeviceBusy()
        },10)
    }
}