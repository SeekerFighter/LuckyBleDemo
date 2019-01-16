package com.seeker.luckyble.scan

import android.os.ParcelUuid
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.util.*
import kotlin.experimental.and

/**
 *@author  Seeker
 *@date    2018/11/22/022  13:59
 *@describe TODO
 */
internal object BluetoothUuid {

    private val BASE_UUID = ParcelUuid.fromString("00000000-0000-1000-8000-00805F9B34FB")!!

    /** Length of bytes for 16 bit UUID  */
    const val UUID_BYTES_16_BIT = 2
    /** Length of bytes for 32 bit UUID  */
    const val UUID_BYTES_32_BIT = 4
    /** Length of bytes for 128 bit UUID  */
    const val UUID_BYTES_128_BIT = 16

    fun parseUuidFrom(uuidBytes:ByteArray?):ParcelUuid{
        if (uuidBytes == null) {
            throw IllegalArgumentException("uuidBytes cannot be null")
        }

        val length:Int = uuidBytes.size
        if (length != UUID_BYTES_16_BIT && length != UUID_BYTES_32_BIT && length != UUID_BYTES_128_BIT) {
            throw IllegalArgumentException("uuidBytes length invalid - $length")
        }
        if (length == UUID_BYTES_128_BIT){
            val buf:ByteBuffer = ByteBuffer.wrap(uuidBytes).order(ByteOrder.LITTLE_ENDIAN)
            return ParcelUuid(UUID(buf.getLong(8),buf.getLong(0)))
        }
        // For 16 bit and 32 bit UUID we need to convert them to 128 bit value.
        // 128_bit_value = uuid * 2^96 + BASE_UUID
        var shortUuid: Long
        if (length == UUID_BYTES_16_BIT){
            shortUuid = (uuidBytes[0]  and 0xFF.toByte()).toLong()
            shortUuid += ((uuidBytes[1]  and 0xFF.toByte()).toLong()) shl 8
        }else{
            shortUuid = (uuidBytes[0] and 0xFF.toByte()).toLong()
            shortUuid += (uuidBytes[1] and 0xFF.toByte()).toLong() shl 8
            shortUuid += (uuidBytes[2] and 0xFF.toByte() ).toLong() shl 16
            shortUuid += (uuidBytes[3] and 0xFF.toByte()).toLong()  shl 24
        }
        val msb = BASE_UUID.uuid.mostSignificantBits + (shortUuid shl 32)
        val lsb = BASE_UUID.uuid.leastSignificantBits
        return ParcelUuid(UUID(msb, lsb))
    }

}