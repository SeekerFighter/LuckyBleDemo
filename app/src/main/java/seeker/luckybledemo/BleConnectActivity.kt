package seeker.luckybledemo

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.seeker.luckyble.BluetoothManager
import com.seeker.luckyble.connect.BleGattProfile
import com.seeker.luckyble.connect.callbacks.ConnectCallback
//import com.seeker.luckyble.upgrade.AssetLoader
//import com.seeker.luckyble.upgrade.OadUpgradeServer
//import com.seeker.luckyble.upgrade.UpgradeListener
import kotlinx.android.synthetic.main.activity_bleconnect.*
import java.util.*

/**
 *@author  Seeker
 *@date    2019/1/15/015  15:09
 */
class BleConnectActivity :AppCompatActivity(){

    private lateinit var mActivity:AppCompatActivity

    val OAD_SERVICE = UUID.fromString("f000ffc0-a37d-e411-bedb-d4f51378eeee")

    val OAD_IMGIDENTIFY = UUID.fromString("F000FFC1-A37D-E411-BEDB-D4F51378EEEE")
    val OAD_IMGIBLOCK = UUID.fromString("F000FFC2-A37D-E411-BEDB-D4F51378EEEE")

    private lateinit var mac:String

    private var connectStatusb:Boolean = false

//    private lateinit var oadServer:OadUpgradeServer

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_bleconnect)
        this.mActivity = this
        mac = intent.getStringExtra("deviceMac")
        initUpgrade();
        connect.setOnClickListener { v: View? ->
            if (connectStatusb){
                BluetoothManager.instance.disConnect(mac)
            }else{
                connect()
            }
        }
    }

    override fun onStop() {
        super.onStop()
        BluetoothManager.instance.disConnect(mac)
    }

    private fun connect(){
        BluetoothManager.instance.connect(mac,true,object :ConnectCallback{

            override fun onConnected() {
                connectStatus.text = "连接成功：$mac"
                connectStatusb = true
                connect.text = "断开连接"
            }

            override fun onServicesFounded(success: Boolean, bleGattProfile: BleGattProfile?) {
                if (success){
                    bleGattProfile?.let {
//                        if (oadServer.validOadServer(it)){
//                            oadServer.startUpgradeProcess()
//                        }
                    }
                }
            }

            override fun onDisConnect() {
                connectStatus.text = "断开连接：$mac"
                connectStatusb = false
                connect.text = "开始连接"
            }

        })
    }

    private fun initUpgrade(){
//        oadServer = OadUpgradeServer.newInstance(OAD_SERVICE,OAD_IMGIBLOCK,OAD_IMGIDENTIFY,mac)
//        oadServer.setSourceBin(AssetLoader(this,"bins/20190110_1.3.8.6.bin"))
//        oadServer.setUpgradeListener(listener)
    }


//    val listener = object:UpgradeListener{
//
//        override fun onProgress(progress: Float) {
//            info.text = "progress = ${progress*100}"
//        }
//
//        override fun onDisConnect() {
//        }
//
//        override fun onConnectComplete() {
//        }
//
//        override fun onConnectStart() {
//        }
//
//        override fun onUpgradeStatus(status: Int) {
//        }
//
//    }


}