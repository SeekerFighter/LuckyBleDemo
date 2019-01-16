package com.seeker.luckyble.scan

import android.annotation.SuppressLint
import android.os.Parcelable
import com.seeker.luckyble.utils.Constants
import kotlinx.android.parcel.Parcelize

/**
 *@author  Seeker
 *@date    2018/11/22/022  13:18
 *@describe 扫描规则设定
 */
@SuppressLint("ParcelCreator")
@Parcelize
class ScanOptions(
    val duration: Long = Constants.SCAN_TIME_LONG,
    val scanFilter:Array<ScanFilter>? = null
) : Parcelable