package com.seeker.luckyble.scan

import android.bluetooth.BluetoothDevice

/**
 *@author  Seeker
 *@date    2018/11/22/022  14:48
 *@describe 扫描到的蓝牙设备
 */

data class BleDevice(val mDevice: BluetoothDevice,
                val scanRecorder: ScanRecorder?,
                val rssi: Int) {

    val deviceName:String
            get() = mDevice.name ?: scanRecorder?.deviceName ?: ""

    val deviceMac:String
        get() = mDevice.address

}