package com.seeker.luckyble.scan

import android.annotation.TargetApi
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import com.seeker.luckyble.BleLogger

/**
 *@author  Seeker
 *@date    2018/11/22/022  18:21
 *@describe 版本18以上蓝牙扫描
 */
@Suppress("DEPRECATION")
@TargetApi(18)
internal class BleScannerV18Impl: BleScanner() {

    init {
        BleLogger.v(TAG,"BleScannerV18Impl init...")
    }

    override fun startScan() {
        if (scaning){
            notifyScanStart(false)
        }else {
            notifyScanStart(bleAdapter.startLeScan(scanCallback))
        }
    }

    override fun stopScan() {
        if (scaning) {
            bleAdapter.stopLeScan(scanCallback)
            super.stopScan()
        }
    }

    private val scanCallback: BluetoothAdapter.LeScanCallback = BluetoothAdapter.LeScanCallback { device, rssi, scanRecord ->
        scanFilter?.let {
            for (f in it){
                if (match(f,device)){
                    notifyDeviceFounded(BleDevice(device,ScanRecorder.parseFromBytes(scanRecord),rssi))
                }
            }
        }?:notifyDeviceFounded(BleDevice(device,ScanRecorder.parseFromBytes(scanRecord),rssi))
    }

}