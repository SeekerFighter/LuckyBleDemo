package com.seeker.luckyble.upgrade

import java.io.IOException

/**
 * @author Seeker
 * @date 2018/4/25/025  17:13
 * @describe 镜像文件加载
 */

abstract class Loader(var sourcePath: String) {

    @Throws(IOException::class)
    abstract fun loadFile(): ByteArray

    companion object {
        const val FILE_BUFFER_SIZE = 0x40000
    }

}
