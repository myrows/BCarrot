package com.example.bcarrot

import android.app.Activity
import android.bluetooth.BluetoothAdapter
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import com.example.bcarrot.register.Login
import com.example.bcarrot.register.Register
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdSize
import com.google.android.gms.ads.AdView
import com.google.android.gms.ads.MobileAds
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {
    lateinit var mAdView: AdView
    lateinit var myBluetoothAdapter : BluetoothAdapter
    lateinit var enablingIntent : Intent

    companion object {
        var REQUEST_CODE_ENABLED = 1
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        // Initialize mobile ads
        MobileAds.initialize(this, "ca-app-pub-4204713758500577~5237010154")
        myBluetoothAdapter = BluetoothAdapter.getDefaultAdapter()
        enablingIntent = Intent( BluetoothAdapter.ACTION_REQUEST_ENABLE )
        bluetoothEnable()
        buttonLoginMain.setOnClickListener {
            var intent = Intent(this@MainActivity, Login::class.java)
            startActivity(intent)
        }

        textViewRegisterNow.setOnClickListener {
            var intent = Intent(this@MainActivity, Register::class.java)
            startActivity(intent)
        }
    }

    fun bluetoothEnable () {
        myBluetoothAdapter?.let {
            if (!myBluetoothAdapter.isEnabled) startActivityForResult(
                enablingIntent,
                REQUEST_CODE_ENABLED
            )
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