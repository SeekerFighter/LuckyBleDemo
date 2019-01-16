package com.seeker.luckyble.connect

/**
 *@author  Seeker
 *@date    2018/11/26/026  9:29
 *@describe 蓝牙未打开
 */
enum class BleErrorCode(errorCode:Int,var errorMsg:String){
    SUCCESS(0,"操作成功"),
    GATT_OPEN_ERROR(1,"打开BluetoothGatt失败"),
    GATT_NULL_ERROR(2,"mBluetoothGatt为空"),
    NOTIFY_ERROR(3,"notify打开失败"),
    WRITER_ERROR(4,"数据写入失败"),
    READ_ERROR(5,"数据读取失败")
}