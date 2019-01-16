package com.seeker.luckyble.request

/**
 *@author  Seeker
 *@date    2018/11/27/027  14:50
 *@describe 外部实现接口，主要用于数据的写入
 */
interface Request {

    /**
     * 待写入的数据字节数组
     */
    fun dataArray():ByteArray

    /**
     * 当前写入的请求识别编码
     */
    fun requestCode():Int = 0

}