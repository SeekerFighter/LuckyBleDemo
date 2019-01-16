package com.seeker.luckyble.exception

/**
 *@author  Seeker
 *@date    2018/11/22/022  11:37
 *@describe 蓝牙不支持异常
 */
class NonSupportException(errMsg:String?=null): RuntimeException(errMsg)