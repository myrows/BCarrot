package com.example.bcarrot.connection

import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothServerSocket
import android.bluetooth.BluetoothSocket
import android.os.Handler
import android.os.Message
import android.util.Log
import com.example.bcarrot.ui.devices.UserOperationsFragment
import java.io.IOException

class ServerClass(var myBluetoothAdapter : BluetoothAdapter, var handler : Handler) : Thread( ) {

    val serverSocket: BluetoothServerSocket? by lazy(LazyThreadSafetyMode.NONE) {
        myBluetoothAdapter?.listenUsingRfcommWithServiceRecord(
            UserOperationsFragment.APP_NAME,
            UserOperationsFragment.APP_UUID
        )
    }

    override fun run() {
        var socket : BluetoothSocket? = null
        while (socket == null) {
            try {
                var message : Message = Message.obtain()
                message.what =
                    UserOperationsFragment.STATE_CONNECTING
                handler.sendMessage( message )
                socket = serverSocket!!.accept()
            } catch ( e : IOException) {
                e.printStackTrace()
                var message : Message = Message.obtain()
                message.what =
                    UserOperationsFragment.STATE_CONNECTION_FAILED
                handler.sendMessage( message )
            }
            if ( socket != null ) {
                var message : Message = Message.obtain()
                message.what =
                    UserOperationsFragment.STATE_CONNECTED
                handler.sendMessage( message )

                //write some code for send / receive
                SendReceive(socket, handler)!!.start()
            }
        }
    }
    // Closes the connect socket and causes the thread to finish.
    fun cancel() {
        try {
            serverSocket?.close()
        } catch (e: IOException) {
            Log.e("SocketClose", "Could not close the connect socket", e)
        }
    }
}