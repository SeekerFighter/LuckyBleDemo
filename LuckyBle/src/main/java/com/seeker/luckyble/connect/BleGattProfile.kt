package com.seeker.luckyble.connect

import android.bluetooth.BluetoothGatt
import android.bluetooth.BluetoothGattCharacteristic
import android.os.ParcelUuid
import android.os.Parcelable
import com.seeker.luckyble.utils.generatePrivateKey
import kotlinx.android.parcel.Parcelize
import java.util.*

/**
 *@author  Seeker
 *@date    2018/11/26/026  13:10
 *@describe 蓝牙客户端服务字段缓存
 */
@Parcelize
class BleGattProfile : Parcelable {

    /**
     * key: service UUID
     * value: character UUID
     */
    private val gattServiceUUIDs:MutableMap<ParcelUuid,MutableList<ParcelUuid>> = mutableMapOf()

    private val gattCharacters:MutableMap<String,BluetoothGattCharacteristic> = mutableMapOf()

    /**
     * 某个服务内是否包含某个字段
     * @param serviceUUID 服务uuid
     * @param characterUUID 字段uuid
     */
    fun containsCharacterInService(serviceUUID:UUID,characterUUID: UUID):Boolean{
        var contains = false
        for ((key,value) in gattServiceUUIDs){
            var result = serviceUUID.toString() == key.toString()
            if (result){
                result = characterUUID.let {
                    var characterContains = false
                    for (character in value){
                        if (character.toString() == it.toString()){
                            characterContains = true
                            break
                        }
                    }
                    (result && characterContains)
                }
                if (result){
                    contains = true
                    break
                }
            }
        }
        return contains
    }

    /**
     * 是否包含某个服务
     */
    fun containsService(serviceUUID:UUID):Boolean{
        var result = false
        for ((key,value) in gattServiceUUIDs){
            if (serviceUUID.toString() == key.toString()){
                result = true
                break
            }
        }
        return result
    }

    fun confirmCache(gatt:BluetoothGatt?){
        releaseCache()
        gatt?.let {
            val services = it.services
            for (service in services){
                val uuid = ParcelUuid(service.uuid)
                val characters:MutableList<ParcelUuid> = mutableListOf()
                val characterUUIDs = service.characteristics
                for (id in characterUUIDs){
                    gattCharacters[generatePrivateKey(service.uuid,id.uuid)] = id
                    characters.add(ParcelUuid(id.uuid))
                }
                gattServiceUUIDs[uuid] = characters
            }
        }
    }

    fun getGattCharacter(gatt:BluetoothGatt?,serviceUUID: String,characterUUID: String):BluetoothGattCharacteristic?{
        var character = gattCharacters[generatePrivateKey(serviceUUID,characterUUID)]
        if (character == null){
            character = gatt?.let {
                it.getService(UUID.fromString(serviceUUID))?.getCharacteristic(UUID.fromString(characterUUID))
            }
        }
        return character
    }


    /**
     * 清除字段缓存
     */
    fun releaseCache(){
        gattServiceUUIDs.clear()
        gattCharacters.clear()
    }

    override fun toString(): String {
        val sb = StringBuffer()
        for ((key,value) in gattServiceUUIDs){
            sb.append("service [$key]-->characters[")
            for (v in value){
                sb.append("$v,")
            }
            sb.append("]\n")
        }
        return sb.toString()
    }
}