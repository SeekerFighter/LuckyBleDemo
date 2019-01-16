package com.seeker.luckyble.scan

import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.os.Build
import android.os.Handler
import android.os.Looper
import androidx.annotation.CallSuper
import com.seeker.luckyble.BleLogger
import com.seeker.luckyble.utils.Constants
import com.seeker.luckyble.utils.bluetoothAdapter
import com.seeker.luckyble.utils.isLollipop

/**
 *@author  Seeker
 *@date    2018/11/22/022  18:21
 *@describe 蓝牙扫描实现抽象类
 */
abstract class BleScanner {

    private val handler:Handler = Handler(Looper.myLooper())

    private lateinit var bleScanCallback:BleScanCallback

    private var duration:Long = Constants.SCAN_TIME_LONG
    var scanFilter:Array<ScanFilter>? = null

    val bleAdapter:BluetoothAdapter = bluetoothAdapter()

    var scaning:Boolean = false//是否正在扫描

    companion object {

        const val TAG:String = "BleScanner"

        fun newInstance(): BleScanner = if (isLollipop()) {
            BleScannerV21Impl()
        } else {
            BleScannerV18Impl()
        }
    }

    fun prepare(callback: BleScanCallback,scanOptions: ScanOptions){
        BleLogger.v(TAG,"prepare()called ...")
        this.bleScanCallback = callback
        this.duration = scanOptions.duration
        this.scanFilter = scanOptions.scanFilter
    }

    abstract fun startScan()

    @CallSuper
    open fun stopScan(){
        notifyScanStop()
    }

    fun notifyScanStart(success:Boolean){
        BleLogger.v(TAG,"notifyScanStart() called with :success = [$success]")
        scaning = success
        bleScanCallback.onScanStart(success)
        handler.postDelayed({
            stopScan()
        },duration)
    }

    private fun notifyScanStop(){
        BleLogger.v(TAG,"notifyScanStop() called...")
        bleScanCallback.onScanStop()
        scaning = false
    }

    fun notifyDeviceFounded(bleDevice: BleDevice) = bleScanCallback.onDeviceScaned(bleDevice)

    fun match(filter: ScanFilter,device: BluetoothDevice):Boolean{
        return filter.name == device.name && filter.macAddress == device.address
    }

}