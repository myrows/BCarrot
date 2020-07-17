package com.example.bcarrot.connection

import android.bluetooth.BluetoothSocket
import android.os.Handler
import java.io.IOException
import java.io.InputStream
import java.io.OutputStream

class SendReceive(private val bluetoothSocket: BluetoothSocket, var handler: Handler) : Thread() {
    private val inputStream: InputStream?
    private val outputStream: OutputStream?

    companion object {
        var STATE_MESSAGE_RECEIVED = 5
    }

    init {
        var tempIn: InputStream? = null
        var tempOut: OutputStream? = null
        try {
            tempIn = bluetoothSocket.inputStream
            tempOut = bluetoothSocket.outputStream
        } catch (e: IOException) {
            e.printStackTrace()
        }
        inputStream = tempIn
        outputStream = tempOut
    }

    override fun run() {
        val buffer = ByteArray(1024)
        var bytes: Int
        while (true) {
            try {
                bytes = inputStream!!.read(buffer)
                handler.obtainMessage(STATE_MESSAGE_RECEIVED, bytes, -1, buffer).sendToTarget()
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }
    }

    fun write(bytes: ByteArray?) {
        try {
            outputStream!!.write(bytes)
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }
}