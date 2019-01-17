package com.seeker.luckyble.upgrade

import android.bluetooth.BluetoothGatt
import android.bluetooth.BluetoothGattCharacteristic
import android.bluetooth.BluetoothGattDescriptor

/**
 *@author  Seeker
 *@date    2019/1/16/016  18:03
 *@describe 升级过程
 */
interface UpgradeProcess {

    fun onCharacteristicWrite(gatt: BluetoothGatt,characteristic: BluetoothGattCharacteristic, status: Int)

    fun onCharacteristicChanged(gatt: BluetoothGatt,characteristic: BluetoothGattCharacteristic)

    fun onDescriptorWrite(gatt:BluetoothGatt, descriptor: BluetoothGattDescriptor, status:Int)
}