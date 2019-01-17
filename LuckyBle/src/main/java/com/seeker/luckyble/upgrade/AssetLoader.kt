package com.seeker.luckyble.upgrade

import android.content.Context

import java.io.IOException
import java.io.InputStream

/**
 * @author Seeker
 * @date 2018/4/25/025  17:15
 * @describe TODO
 */

class AssetLoader(private val mContext: Context, path: String) : Loader(path) {

    private val mFileBuffer: ByteArray = ByteArray(Loader.FILE_BUFFER_SIZE)

    @Throws(IOException::class)
    override fun loadFile(): ByteArray {
        var inputStream: InputStream? = null
        try {
            inputStream = mContext.assets.open(sourcePath)
            inputStream!!.read(mFileBuffer, 0, mFileBuffer.size)
        } finally {
            inputStream?.close()
        }
        return mFileBuffer
    }

}
