package com.seeker.luckyble.scan

import android.annotation.SuppressLint
import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

/**
 *@author  Seeker
 *@date    2018/11/23/023  9:42
 *@describe 扫描时的过滤条件
 */
@SuppressLint("ParcelCreator")
@Parcelize
class ScanFilter(val name:String ? = null,val macAddress:String ? = null) : Parcelable