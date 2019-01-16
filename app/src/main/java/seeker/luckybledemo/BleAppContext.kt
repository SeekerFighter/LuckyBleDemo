package seeker.luckybledemo

import android.app.Application
import com.seeker.luckyble.BluetoothManager

/**
 *@author  Seeker
 *@date    2019/1/15/015  14:23
 *@describe TODO
 */
class BleAppContext : Application() {

    override fun onCreate() {
        super.onCreate()
        BluetoothManager.instance.firstGlobalInit(this)
    }

}