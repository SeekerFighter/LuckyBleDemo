package seeker.luckybledemo

import android.os.Bundle
import android.view.View
import androidx.annotation.IntegerRes
import androidx.appcompat.app.AppCompatActivity
import com.seeker.luckyble.BluetoothManager
import com.seeker.luckyble.connect.BleGattProfile
import com.seeker.luckyble.connect.callbacks.ConnectCallback
import com.seeker.luckyble.upgrade.AssetLoader
import com.seeker.luckyble.upgrade.UpgradeImpl
import com.seeker.luckyble.upgrade.UpgradeListener
import kotlinx.android.synthetic.main.activity_bleconnect.*
import java.text.DecimalFormat

/**
 *@author  Seeker
 *@date    2019/1/15/015  15:09
 */
class BleConnectActivity : AppCompatActivity() {

    private lateinit var mActivity: AppCompatActivity

    val OAD_SERVICE = ""
    val OAD_IMGIDENTIFY = ""
    val OAD_IMGIBLOCK = ""

    private lateinit var mac: String

    private var connectStatusb: Boolean = false

    private val format = DecimalFormat("#.0")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_bleconnect)
        this.mActivity = this
        mac = intent.getStringExtra("deviceMac")
        connect.setOnClickListener { v: View? ->
            if (connectStatusb) {
                BluetoothManager.instance.disConnect(mac)
            } else {
                connect()
            }
        }
    }

    override fun onStop() {
        super.onStop()
        BluetoothManager.instance.disConnect(mac)
    }

    private fun connect() {
        BluetoothManager.instance.connect(mac, true,
            UpgradeImpl(OAD_SERVICE, OAD_IMGIBLOCK, OAD_IMGIDENTIFY),
            object : ConnectCallback {

                override fun onConnected() {
                    connectStatus.text = "连接成功：$mac"
                    connectStatusb = true
                    connect.text = "断开连接"
                }

                override fun onServicesFounded(success: Boolean, bleGattProfile: BleGattProfile?,isUpgradeMode:Boolean) {
                    if (success) {
                        connectStatus.text = "发现服务：$mac"
                        if (isUpgradeMode){
                            info.text = "升级模式"
                            upgrade()
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

    private fun upgrade(){
        BluetoothManager.instance.startBleUpgrade(mac, AssetLoader(this, "bins/20190110_1.3.8.6.bin"),
            object : UpgradeListener {
                override fun onUpgradeSuccess() {
                    info.text = "升级成功"
                }

                override fun onUpgradeProgress(progress: Float) {
                    info.text = "${format.format(progress*100)}%"
                }

                override fun onUpgradeFail(msg: String) {
                    info.text = "升级失败:$msg"
                }

            }
        )
    }

}