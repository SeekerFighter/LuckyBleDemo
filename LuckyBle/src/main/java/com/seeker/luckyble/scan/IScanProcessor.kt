package com.seeker.luckyble.scan

/**
 *@author  Seeker
 *@date    2018/11/26/026  11:20
 *@describe 扫描接口
 */
interface IScanProcessor {

    fun scan(scanCallback: BleScanCallback, scanOptions: ScanOptions)

    fun stop()
}