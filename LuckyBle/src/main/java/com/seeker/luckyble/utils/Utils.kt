@file:JvmMultifileClass
@file:JvmName("UtilsKt")

package com.seeker.luckyble.utils

import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothGatt
import android.bluetooth.BluetoothGattCharacteristic
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.os.Looper
import com.seeker.luckyble.BleLogger
import com.seeker.luckyble.BluetoothManager
import com.seeker.luckyble.proxy.ProxyInvocationHandler
import java.lang.reflect.Proxy
import java.util.*

/**
 *@author  Seeker
 *@date    2018/11/22/022  11:25
 *@describe 蓝牙操作相关工具类
 */

//23
fun isMarshmallow():Boolean = Build.VERSION.SDK_INT >= Build.VERSION_CODES.M

//21
fun isLollipop():Boolean = Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP

/**
 * 是否支持ble蓝牙
 */
fun Context.isSupportBle():Boolean{
    var support = false
    if (isBigger18()){
        support = packageManager.hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)
    }
    return support
}

/**
 * [BluetoothAdapter]
 */
fun bluetoothAdapter():BluetoothAdapter = BluetoothAdapter.getDefaultAdapter()

/**
 * 是否打印信息
 */
fun BluetoothManager.enableDebug(debug:Boolean) = setDebug(debug)

/**
 * 刷新缓存
 */
fun BluetoothGatt.refreshGattCache():Boolean = try {
    val refresh = BluetoothGatt::class.java.getMethod("refresh")
    refresh?.let {
        refresh.isAccessible = true
        refresh.invoke(this, *(arrayOfNulls<Any>(0)))
        true
    }?:false
}catch (e:Exception){
    BleLogger.e(msg = "",throwable = e)
    false
}

/**
 * 关闭 [BluetoothGatt]
 */
fun BluetoothGatt.closeGatt(delay:Long = 0L):Long{
    close()
    refreshGattCache()
    return delay
}

/**
 * JELLY_BEAN_MR2
 */
fun isBigger18() = Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2

/**
 * M is for Marshmallow!
 */
fun isBigger23() = Build.VERSION.SDK_INT >= Build.VERSION_CODES.M

/**
 * 根据传递的参数生成可key
 */
fun generatePrivateKey(vararg args:Any):String = with(StringBuffer()){
    for (arg in args){
        append("$arg&")
    }
    deleteCharAt(length-1).toString()
}

fun ByteArray.string():String = Arrays.toString(this)

/**
 * byte数组转16进制字符串
 */
fun ByteArray.hexString():String = with(this) {
    val sb = StringBuffer()
    for (byteChar in this){
        sb.append(String.format("0x%02X ",byteChar))
    }
    sb.deleteCharAt(sb.length-1).toString()
}

fun <E>MutableMap<String,MutableList<E>>.putE(key:String,e:E){
    if (this[key]?.addE(e) == null){
        this[key] = mutableListOf(e)
    }
}

fun <E>MutableList<E>.addE(e:E):Boolean = if (contains(e)) false else add(e)

inline fun <reified T :Any> T.validateInterface(){
    if (!this::class.java.isInterface){
        throw IllegalArgumentException("function declarations must be interfaces.")
    }
}

inline fun <reified T : Any> T.getUIProxy(postUI:Boolean = true): T =
    getUIProxy(if (postUI) Looper.getMainLooper() else Looper.myLooper())

/**
 * T must is a interface
 */
inline fun <reified T : Any> T.getUIProxy(looper: Looper = Looper.myLooper()): T = Proxy.newProxyInstance(
    javaClass.classLoader,
    javaClass.interfaces,
    ProxyInvocationHandler(this, looper)
) as T

fun BluetoothGattCharacteristic.key() = generatePrivateKey(service.uuid.toString(),uuid.toString())