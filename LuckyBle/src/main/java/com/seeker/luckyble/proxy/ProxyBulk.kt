package com.seeker.luckyble.proxy

import com.seeker.luckyble.BleLogger
import java.lang.Exception
import java.lang.reflect.Method

/**
 *@author  Seeker
 *@date    2018/11/22/022  17:19
 *@describe TODO
 */
class ProxyBulk(private val subject: Any, private val method: Method?,private val args:Array<out Any?>) {

    fun safeInvoke(): Any? = try {
        method?.invoke(subject,*args)
    } catch (e: Exception) {
        BleLogger.e(msg = "", throwable = e)
    }
}