package com.seeker.luckyble.scan

import android.os.HandlerThread
import android.os.Looper
import com.seeker.luckyble.BleLogger
import com.seeker.luckyble.utils.getUIProxy

/**
 *@author  Seeker
 *@date    2018/11/22/022  17:44
 *@describe 蓝牙扫描操作
 */
internal class BleScanProcessor:IScanProcessor {

    private val bleScanner:BleScanner = BleScanner.newInstance()

    private val workLooper: Looper

    init {
        val mWorkerThread = HandlerThread(TAG)
        mWorkerThread.start()
        workLooper = mWorkerThread.looper
    }

    companion object {
        private const val TAG = "BleScanProcessor"
        val instance: IScanProcessor by lazy(LazyThreadSafetyMode.SYNCHRONIZED) {
            val scanProcessor = BleScanProcessor()
            (scanProcessor as IScanProcessor).getUIProxy(scanProcessor.workLooper)
        }
    }

    override fun scan(scanCallback: BleScanCallback, scanOptions: ScanOptions){
        BleLogger.v(TAG,"scan() called")
        bleScanner.prepare(scanCallback,scanOptions)
        bleScanner.startScan()
    }

    override fun stop(){
        BleLogger.v(TAG,"stop() called")
        bleScanner.stopScan()
    }

}