package com.seeker.luckyble.upgrade;

/**
 * @author Seeker
 * @date 2018/4/25/025  17:44
 * @describe TODO
 */

public class ByteHelper {

    public static byte loUint16(short v) {
        return (byte) (v & 0xFF);
    }

    public static byte hiUint16(short v) {
        return (byte) (v >> 8);
    }

    public static short buildUint16(byte hi, byte lo) {
        return (short) ((hi << 8) + (lo & 0xff));
    }
}
