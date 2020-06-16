package com.example.bcarrot

import android.app.Activity
import android.bluetooth.BluetoothAdapter
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log

class UserOperationsActivity : AppCompatActivity() {
    lateinit var myBluetoothAdapter : BluetoothAdapter
    private var REQUEST_CODE_ENABLED : Int = 0
    lateinit var enablingIntent : Intent

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_user_operations)

        myBluetoothAdapter = BluetoothAdapter.getDefaultAdapter()
        REQUEST_CODE_ENABLED = 1
        enablingIntent = Intent( BluetoothAdapter.ACTION_REQUEST_ENABLE )

        // Check if bluetooth enabled - if result it's disabled then allow enable bluetooth
        bluetoothEnable()
    }

    fun bluetoothEnable () {
        myBluetoothAdapter?.let {
            if ( !myBluetoothAdapter.isEnabled ) startActivityForResult( enablingIntent, REQUEST_CODE_ENABLED )
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if ( requestCode == REQUEST_CODE_ENABLED ) {
            if ( resultCode == Activity.RESULT_OK ) {
                Log.d("Bluetooth", "Enabled")
            } else if ( resultCode == Activity.RESULT_CANCELED ) {
                Log.d("Bluetooth", "Disabled")
            }
        }
    }
}