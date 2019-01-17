package com.seeker.luckyble.upgrade

import androidx.annotation.FloatRange

/**
 *@author  Seeker
 *@date    2019/1/17/017  9:21
 *@describe 升级监听
 */
interface UpgradeListener {

    fun onUpgradeSuccess()

    fun onUpgradeProgress(@FloatRange(from = 0.0,to = 1.0)progress:Float)

    fun onUpgradeFail(msg:String = "")
}