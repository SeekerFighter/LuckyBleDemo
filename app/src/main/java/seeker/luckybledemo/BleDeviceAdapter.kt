package seeker.luckybledemo

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import com.seeker.luckyble.scan.BleDevice
import kotlinx.android.synthetic.main.item_bledevice.view.*

/**
 *@author  Seeker
 *@date    2019/1/15/015  14:43
 *@describe TODO
 */
class BleDeviceAdapter(val context:Context,val datas:List<BleDevice>): BaseAdapter() {

    private val mInflater = LayoutInflater.from(context)

    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
        val holder:ViewHolder
        val view:View
        if (convertView == null){
            view = mInflater.inflate(R.layout.item_bledevice,parent,false)
            holder = ViewHolder(view)
            view.tag = holder
        }else{
            view = convertView
            holder = view.tag as ViewHolder
        }
        holder.bind(getItem(position))
        return view
    }

    override fun getItem(position: Int): BleDevice  = datas[position]

    override fun getItemId(position: Int): Long  = position.toLong()

    override fun getCount(): Int  = datas.size

    private class ViewHolder(val view:View){
        fun bind(device:BleDevice){
            view.name.text = "蓝牙设备名称:   ${device.deviceName}"
            view.macAddress.text = "蓝牙设备地址:   ${device.deviceMac}"
        }
    }

}