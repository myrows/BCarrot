package com.example.bcarrot.ui.devices

import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.bcarrot.R
import com.example.bcarrot.ui.connect.ConnectDeviceActivity
import kotlinx.android.synthetic.main.fragment_item.view.*
import java.util.*
import kotlin.collections.ArrayList

class MyItemRecyclerViewAdapter() : RecyclerView.Adapter<MyItemRecyclerViewAdapter.ViewHolder>() {
    private var listNameDevices : List<BluetoothDevice> = ArrayList<BluetoothDevice>()
    lateinit var ctx : Context
    lateinit var myBluetoothAdapter : BluetoothAdapter

    companion object {
        var APP_NAME : String = "BCarrot"
        var APP_UUID : UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB")
        var STATE_LISTENING = 1
        var STATE_CONNECTING = 2
        var STATE_CONNECTED = 3
        var STATE_CONNECTION_FAILED = 4
        var STATE_MESSAGE_RECEIVED = 5
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.fragment_item, parent, false)

        ctx = parent.context
        myBluetoothAdapter = BluetoothAdapter.getDefaultAdapter()

        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = listNameDevices[position]

        holder.textDeviceName.text = item.name


        holder.itemView.setOnClickListener {
            var intentDeviceActivity : Intent = Intent ( ctx, ConnectDeviceActivity::class.java )
            intentDeviceActivity.putExtra("deviceBcarrot", item.address)
            ctx.startActivity( intentDeviceActivity )
        }

    }

    override fun getItemCount(): Int = listNameDevices.size

    fun setData( listDevices : List<BluetoothDevice> ) {
        listNameDevices = listDevices
        notifyDataSetChanged()
    }

    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val textDeviceName : TextView = view.textViewDeviceName
    }



}