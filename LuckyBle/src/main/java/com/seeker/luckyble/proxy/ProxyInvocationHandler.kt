package com.seeker.luckyble.proxy

import android.os.Handler
import android.os.Looper
import android.os.Message
import com.seeker.luckyble.BleLogger
import java.lang.reflect.InvocationHandler
import java.lang.reflect.Method

/**
 *@author  Seeker
 *@date    2018/11/22/022  17:02
 *@describe 运行线程转换
 */
class ProxyInvocationHandler(
    private val subject: Any,
    runLooper: Looper
) : InvocationHandler, Handler.Callback {

    private val handler: Handler = Handler(runLooper, this)

    override fun invoke(proxy: Any?, method: Method, args: Array<out Any>?): Any?{
        val bulk = ProxyBulk(subject, method, args?: emptyArray())
        if (method.name == "equals" || method.name == "toString" || method.name == "hashCode"){
            return bulk.safeInvoke()
        }
        return try {
            poseUInvoke(bulk)
        }catch (e:Exception){
            BleLogger.e(msg = "",throwable = e)
        }
    }

    override fun handleMessage(msg: Message?): Boolean {
        msg?.let {
            (it.obj as ProxyBulk).safeInvoke()
        }
        return true
    }

    private fun poseUInvoke(bulk: ProxyBulk) {
        handler.obtainMessage(0, bulk).sendToTarget()
    }

}