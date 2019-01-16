package com.seeker.luckyble.scan

/**
 *@author  Seeker
 *@date    2018/11/22/022  13:28
 *@describe 蓝牙扫描回调
 */
interface BleScanCallback {

    fun onScanStart(success:Boolean)//扫描开始

    fun onDeviceScaned(device: BleDevice)//扫描到设备回调

    fun onScanStop()//扫描完成
}