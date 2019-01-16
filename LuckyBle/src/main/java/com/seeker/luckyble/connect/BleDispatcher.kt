package com.seeker.luckyble.connect

import android.os.Handler
import android.os.Looper
import com.seeker.luckyble.connect.task.ITask
import java.util.*

/**
 *@author  Seeker
 *@date    2018/11/26/026  16:16
 *@describe 蓝牙操作分发任务
 */
class BleDispatcher {

    private val tasks: MutableList<ITask> = Collections.synchronizedList(mutableListOf())

    private var mDeviceBusy: Boolean = false

    val dispatcherHandler:Handler = Handler(Looper.myLooper())

    @Synchronized
    fun addNewTask(task: ITask) {
        tasks.add(task)
        scheduleNextTask()
    }

    fun releaseDeviceBusy() {
        synchronized(mDeviceBusy) {
            mDeviceBusy = false
        }
        scheduleNextTask()
    }

    private fun scheduleNextTask() {
        synchronized(mDeviceBusy) {
            if (!mDeviceBusy && tasks.size > 0) {
                mDeviceBusy = true
                tasks.removeAt(0).setBleDispatcher(this).process()
            }
        }
    }

    fun releaseCache() {
        tasks.clear()
        synchronized(mDeviceBusy) {
            mDeviceBusy = false
        }
    }
}