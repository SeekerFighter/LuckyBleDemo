package com.seeker.luckyble.scan

import android.annotation.TargetApi
import android.bluetooth.le.ScanCallback
import android.bluetooth.le.ScanFilter
import android.bluetooth.le.ScanResult
import android.bluetooth.le.ScanSettings
import android.os.Build
import com.seeker.luckyble.BleLogger

/**
 *@author  Seeker
 *@date    2018/11/22/022  18:21
 *@describe 21版本以上蓝牙扫描
 */
@TargetApi(Build.VERSION_CODES.LOLLIPOP)
internal class BleScannerV21Impl: BleScanner() {

    init {
        BleLogger.v(TAG,"BleScannerV21Impl init...")
    }

    override fun startScan() {
        if (scaning){
            notifyScanStart(false)
        }else {
            val scanSettings: ScanSettings = ScanSettings.Builder()
                .setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY).build()
            val filters = ArrayList<ScanFilter>().apply {
                scanFilter?.let {
                    for (f in it) {
                        add(ScanFilter.Builder().setDeviceName(f.name).setDeviceAddress(f.macAddress).build())
                    }
                }
            }
            val success = bleAdapter.let { it ->
                it.bluetoothLeScanner?.let {
                    it.startScan(filters, scanSettings, scanCallback)
                    true
                }
            }
            notifyScanStart(success?:false)
        }
    }

    override fun stopScan() {
        if (scaning) {
            bleAdapter.bluetoothLeScanner?.stopScan(scanCallback)
            super.stopScan()
        }
    }

    private val scanCallback = object:ScanCallback() {
        override fun onScanResult(callbackType: Int, result: ScanResult?) {
            result?.scanRecord?.let {
                notifyDeviceFounded(BleDevice(result.device, ScanRecorder.parseFromBytes(it.bytes),result.rssi))
            }
        }

        override fun onBatchScanResults(results: MutableList<ScanResult>?) {
            results?.let { it ->
                for (result in it){
                    result.scanRecord?.let {
                        notifyDeviceFounded(BleDevice(result.device, ScanRecorder.copyFromScanRecord(it),result.rssi))
                    }
                }
            }
        }

        override fun onScanFailed(errorCode: Int) {
            BleLogger.e(TAG,"onScanFailed()called:errorCode = $errorCode")
            notifyScanStart(false)
        }
    }

}