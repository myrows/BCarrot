package com.example.bcarrot

import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothSocket
import android.content.Context
import android.os.Handler
import android.os.Message
import android.util.Log
import java.io.IOException
import java.util.*

class ClientClass(var device: BluetoothDevice, var handler : Handler) : Thread() {
    lateinit var sendReceive : SendReceive
    companion object {
        private var APP_UUID : UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB")
        var STATE_CONNECTED = 3
        var STATE_CONNECTION_FAILED = 4
    }

    val socket : BluetoothSocket by lazy(LazyThreadSafetyMode.NONE) {
        device.createRfcommSocketToServiceRecord(APP_UUID)
    }

    public override fun run() {
        try {
            socket.connect()
            var message : Message = Message.obtain()
            message.what = STATE_CONNECTED
            handler.sendMessage( message )
            sendReceive = SendReceive(socket, handler)
            sendReceive.start()
        } catch (e : IOException) {
            e.printStackTrace()
            var message : Message = Message.obtain()
            message.what = STATE_CONNECTION_FAILED
            handler.sendMessage( message )
        }
    }

    fun sendData( data : String ) {
        sendReceive.write( data.toByteArray() )
    }

    // Closes the client socket and causes the thread to finish.
    fun cancel() {
        try {
            socket.close()
        } catch (e: IOException) {
            Log.e("SockedClosed", "Could not close the client socket", e)
        }
    }
}