package seeker.luckybledemo

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.os.Handler
import android.os.Message
import android.widget.AdapterView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.seeker.luckyble.BleLogger
import com.seeker.luckyble.BluetoothManager
import com.seeker.luckyble.scan.BleDevice
import com.seeker.luckyble.scan.BleScanCallback
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity(),Handler.Callback {


    private val TAG = "MainActivity"

    private val PERMISSIONS = arrayOf(
        Manifest.permission.ACCESS_COARSE_LOCATION,
        Manifest.permission.ACCESS_FINE_LOCATION)

    private fun isGranted(p:String):Boolean
            = ContextCompat.checkSelfPermission(this,PERMISSIONS[0]) == PackageManager.PERMISSION_GRANTED

    private val devices = ArrayList<BleDevice>()

    private lateinit var bleAdapter: BleDeviceAdapter

    private val handler = Handler(this)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        bleAdapter = BleDeviceAdapter(this,devices)
        listView.adapter = bleAdapter
        listView.onItemClickListener = AdapterView.OnItemClickListener {
                parent, view, position, id ->
            toConnect(bleAdapter.getItem(position))
        }
    }

    override fun onResume() {
        super.onResume()
        devices.clear()
        bleAdapter.notifyDataSetChanged()
        doWork()
    }

    private fun toConnect(device: BleDevice){
        BluetoothManager.instance.stopScan()
        val intent = Intent(this,BleConnectActivity::class.java)
        intent.putExtra("deviceMac",device.deviceMac)
        startActivity(intent)
    }

    private fun doWork(){
        if (isGranted(PERMISSIONS[0]) && isGranted(PERMISSIONS[1])){
            scan()
        }else{
            ActivityCompat.requestPermissions(this,PERMISSIONS,0)
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        doWork()
    }

    private fun scan(){

        BluetoothManager.instance.scan(
//            ScanOptions(scanFilter = arrayOf(ScanFilter(name = "Seeker"))),
            scanCallback = object :BleScanCallback{

            override fun onScanStart(success: Boolean) {
                BleLogger.d(TAG,msg = "onScanStart()called:success = $success")
            }

            override fun onDeviceScaned(device: BleDevice) {
                val msg = handler.obtainMessage()
                msg.obj = device
                handler.sendMessageDelayed(msg,10)
            }

            override fun onScanStop() {
                BleLogger.d(TAG,msg = "onScanStop()called:")
            }
        })
    }

    override fun handleMessage(msg: Message): Boolean {
        val device:BleDevice = msg.obj as BleDevice
        var contains = false
        for (d in devices){
            if (d.deviceMac == device.deviceMac){
                contains = true
                break
            }
        }
        if (!contains){
            devices.add(device)
            bleAdapter.notifyDataSetChanged()
        }
        return true
    }
}
