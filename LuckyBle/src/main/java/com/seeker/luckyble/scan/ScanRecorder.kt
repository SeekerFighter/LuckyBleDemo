package com.seeker.luckyble.scan

/**
 * @author Seeker
 * @date 2018/4/9/009  13:19
 * @describe 蓝牙扫描解析结果
 */

import android.annotation.TargetApi
import android.bluetooth.le.ScanRecord
import android.os.Build
import android.os.ParcelUuid
import android.util.SparseArray
import com.seeker.luckyble.BleLogger
import java.util.*

class ScanRecorder(
    /**
     * Returns a list of service UUIDs within the advertisement that are used to identify the
     * bluetooth GATT services.
     */
    val serviceUuids: List<ParcelUuid>?,
    /**
     * Returns a sparse array of manufacturer identifier and its corresponding manufacturer specific
     * data.
     */
    val manufacturerSpecificData: SparseArray<ByteArray>?,
    /**
     * Returns a map of service UUID and its corresponding service data.
     */
    val serviceData: Map<ParcelUuid, ByteArray>?,
    // Flags of the advertising data.
    /**
     * Returns the advertising flags indicating the discoverable mode and capability of the device.
     * Returns -1 if the flag field is not set.
     */
    val advertiseFlags: Int, // Transmission power level(in dB).
    /**
     * Returns the transmission power level of the packet in dBm. Returns [Integer.MIN_VALUE]
     * if the field is not set. This value can be used to calculate the path loss of a received
     * packet using the following equation:
     *
     *
     * `pathloss = txPowerLevel - rssi`
     */
    val txPowerLevel: Int,
    // Local name of the Bluetooth LE device.
    /**
     * Returns the local name of the BLE device. The is a UTF-8 encoded string.
     */
    val deviceName: String? // Raw bytes of scan record.
) {
    companion object {

        private const val TAG = "ScanRecorder"

        // The following data type values are assigned by Bluetooth SIG.
        // For more details refer to Bluetooth 4.1 specification, Volume 3, Part C, Section 18.
        private const val DATA_TYPE_FLAGS = 0x01
        private const val DATA_TYPE_SERVICE_UUIDS_16_BIT_PARTIAL = 0x02
        private const val DATA_TYPE_SERVICE_UUIDS_16_BIT_COMPLETE = 0x03
        private const val DATA_TYPE_SERVICE_UUIDS_32_BIT_PARTIAL = 0x04
        private const val DATA_TYPE_SERVICE_UUIDS_32_BIT_COMPLETE = 0x05
        private const val DATA_TYPE_SERVICE_UUIDS_128_BIT_PARTIAL = 0x06
        private const val DATA_TYPE_SERVICE_UUIDS_128_BIT_COMPLETE = 0x07
        private const val DATA_TYPE_LOCAL_NAME_SHORT = 0x08
        private const val DATA_TYPE_LOCAL_NAME_COMPLETE = 0x09
        private const val DATA_TYPE_TX_POWER_LEVEL = 0x0A
        private const val DATA_TYPE_SERVICE_DATA = 0x16
        private const val DATA_TYPE_MANUFACTURER_SPECIFIC_DATA = 0xFF

        /**
         * Parse scan record bytes to [android.bluetooth.le.ScanRecord].
         *
         *
         * The format is defined in Bluetooth 4.1 specification, Volume 3, Part C, Section 11 and 18.
         *
         *
         * All numerical multi-byte entities and values shall use little-endian **byte**
         * order.
         *
         * @param scanRecord The scan record of Bluetooth LE advertisement and/or scan response.
         */

        @TargetApi(Build.VERSION_CODES.LOLLIPOP)
        fun copyFromScanRecord(scanRecord: ScanRecord): ScanRecorder = ScanRecorder(
            scanRecord.serviceUuids,
            scanRecord.manufacturerSpecificData,
            scanRecord.serviceData,
            scanRecord.advertiseFlags,
            scanRecord.txPowerLevel,
            scanRecord.deviceName
        )

        fun parseFromBytes(scanRecord: ByteArray?): ScanRecorder? {
            if (scanRecord == null) {
                return null
            }

            var currentPos = 0
            var advertiseFlag = -1
            val serviceUuids: MutableList<ParcelUuid> = ArrayList()
            var localName: String? = null
            var txPowerLevel = Integer.MIN_VALUE

            val manufacturerData = SparseArray<ByteArray>()
            val serviceData = HashMap<ParcelUuid, ByteArray>()

            try {
                while (currentPos < scanRecord.size) {
                    // length is unsigned int.
                    val length = scanRecord[currentPos++].toInt() and 0xFF
                    if (length == 0) {
                        break
                    }
                    // Note the length includes the length of the field type itself.
                    val dataLength = length - 1
                    // fieldType is unsigned int.
                    val fieldType = scanRecord[currentPos++].toInt() and 0xFF
                    when (fieldType) {
                        DATA_TYPE_FLAGS -> advertiseFlag = scanRecord[currentPos].toInt() and 0xFF
                        DATA_TYPE_SERVICE_UUIDS_16_BIT_PARTIAL, DATA_TYPE_SERVICE_UUIDS_16_BIT_COMPLETE -> parseServiceUuid(
                            scanRecord, currentPos,
                            dataLength, BluetoothUuid.UUID_BYTES_16_BIT, serviceUuids
                        )
                        DATA_TYPE_SERVICE_UUIDS_32_BIT_PARTIAL, DATA_TYPE_SERVICE_UUIDS_32_BIT_COMPLETE -> parseServiceUuid(
                            scanRecord, currentPos, dataLength,
                            BluetoothUuid.UUID_BYTES_32_BIT, serviceUuids
                        )
                        DATA_TYPE_SERVICE_UUIDS_128_BIT_PARTIAL, DATA_TYPE_SERVICE_UUIDS_128_BIT_COMPLETE -> parseServiceUuid(
                            scanRecord, currentPos, dataLength,
                            BluetoothUuid.UUID_BYTES_128_BIT, serviceUuids
                        )
                        DATA_TYPE_LOCAL_NAME_SHORT, DATA_TYPE_LOCAL_NAME_COMPLETE -> localName = String(
                            extractBytes(scanRecord, currentPos, dataLength)
                        )
                        DATA_TYPE_TX_POWER_LEVEL -> txPowerLevel = scanRecord[currentPos].toInt()
                        DATA_TYPE_SERVICE_DATA -> {
                            // The first two bytes of the service data are service data UUID in little
                            // endian. The rest bytes are service data.
                            val serviceUuidLength = BluetoothUuid.UUID_BYTES_16_BIT
                            val serviceDataUuidBytes = extractBytes(
                                scanRecord, currentPos,
                                serviceUuidLength
                            )
                            val serviceDataUuid = BluetoothUuid.parseUuidFrom(
                                serviceDataUuidBytes
                            )
                            val serviceDataArray = extractBytes(
                                scanRecord,
                                currentPos + serviceUuidLength, dataLength - serviceUuidLength
                            )
                            serviceData[serviceDataUuid] = serviceDataArray
                        }
                        DATA_TYPE_MANUFACTURER_SPECIFIC_DATA -> {
                            // The first two bytes of the manufacturer specific data are
                            // manufacturer ids in little endian.
                            val manufacturerId =
                                (scanRecord[currentPos + 1].toInt() and 0xFF shl 8) + (scanRecord[currentPos].toInt() and 0xFF)
                            val manufacturerDataBytes = extractBytes(
                                scanRecord, currentPos + 2,
                                dataLength - 2
                            )
                            manufacturerData.put(manufacturerId, manufacturerDataBytes)
                        }
                        else -> {
                        }
                    }// Just ignore, we don't handle such data type.
                    currentPos += dataLength
                }

                return ScanRecorder(
                    serviceUuids, manufacturerData, serviceData, advertiseFlag, txPowerLevel, localName
                )
            } catch (e: Exception) {
                BleLogger.e(TAG, "unable to parse scan record: " + Arrays.toString(scanRecord))
                // As the record is invalid, ignore all the parsed results for this packet
                // and return an empty record with raw scanRecord bytes in results
                return ScanRecorder(null, null, null, -1, Integer.MIN_VALUE, null)
            }

        }

        // Parse service UUIDs.
        private fun parseServiceUuid(
            scanRecord: ByteArray, currentPos: Int, dataLength: Int,
            uuidLength: Int, serviceUuids: MutableList<ParcelUuid>
        ): Int {
            var currentPos = currentPos
            var dataLength = dataLength
            while (dataLength > 0) {
                val uuidBytes = extractBytes(
                    scanRecord, currentPos,
                    uuidLength
                )
                serviceUuids.add(BluetoothUuid.parseUuidFrom(uuidBytes))
                dataLength -= uuidLength
                currentPos += uuidLength
            }
            return currentPos
        }

        // Helper method to extract bytes from byte array.
        private fun extractBytes(scanRecord: ByteArray, start: Int, length: Int): ByteArray {
            val bytes = ByteArray(length)
            System.arraycopy(scanRecord, start, bytes, 0, length)
            return bytes
        }
    }
}
