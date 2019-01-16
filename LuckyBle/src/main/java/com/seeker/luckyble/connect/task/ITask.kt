package com.seeker.luckyble.connect.task

import com.seeker.luckyble.connect.BleDispatcher

/**
 *@author  Seeker
 *@date    2018/11/26/026  16:26
 *@describe 蓝牙操作任务
 */
interface ITask {
    fun process()
    fun setBleDispatcher(dispatcher: BleDispatcher):ITask
}