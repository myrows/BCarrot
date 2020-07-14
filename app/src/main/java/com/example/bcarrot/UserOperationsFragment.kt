package com.example.bcarrot

import android.app.Activity
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothServerSocket
import android.bluetooth.BluetoothSocket
import android.content.BroadcastReceiver
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Message
import android.speech.tts.TextToSpeech
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.bcarrot.common.MyApp
import com.example.bcarrot.common.SharedPreferencesManager
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdSize
import com.google.android.gms.ads.AdView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.android.synthetic.main.fragment_user_operations.*
import java.io.IOException
import java.util.*


class UserOperationsFragment : Fragment() {
    private var REQUEST_CODE_ENABLED : Int = 0
    lateinit var enablingIntent : Intent
    private var columnCount = 1
    lateinit var recyclerViewCustom : RecyclerView
    lateinit var  deviceAdapter : MyItemRecyclerViewAdapter
    lateinit var floatingActionButton : FloatingActionButton
    lateinit var mAdView: AdView
    lateinit var myBluetoothAdapter : BluetoothAdapter
    lateinit var textToSpeech : TextToSpeech
    var db: FirebaseFirestore = FirebaseFirestore.getInstance()

    companion object {
        var APP_NAME : String = "BCarrot"
        var APP_UUID : UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB")
        var STATE_LISTENING = 1
        var STATE_CONNECTING = 2
        var STATE_CONNECTED = 3
        var STATE_CONNECTION_FAILED = 4
        var STATE_MESSAGE_RECEIVED = 5
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        //setContentView(R.layout.fragment_user_operations)

        myBluetoothAdapter = BluetoothAdapter.getDefaultAdapter()
        REQUEST_CODE_ENABLED = 1
        enablingIntent = Intent( BluetoothAdapter.ACTION_REQUEST_ENABLE )
        bluetoothEnable()

        //  Activate ServerSocket
        var socketConnection = ServerClass( myBluetoothAdapter, handler )
        socketConnection.start()
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        var view = inflater.inflate(R.layout.fragment_user_operations, container, false)
        recyclerViewCustom = view.findViewById(R.id.recyclerViewDevicesOperations)
        floatingActionButton = view.findViewById(R.id.floatingActionButtonBluetooth)
        textToSpeech= TextToSpeech(MyApp.context, TextToSpeech.OnInitListener {
            if ( it == TextToSpeech.SUCCESS ) {
                textToSpeech.language = Locale.getDefault()
            }
        })
        mAdView = view.findViewById(R.id.adView);
        val adView = AdView(context)
        adView.adSize = AdSize.BANNER
        adView.adUnitId = "ca-app-pub-3940256099942544/6300978111"
        val adRequest: AdRequest = AdRequest.Builder().build()
        mAdView.loadAd(adRequest)
        floatingActionButton.setOnClickListener {
            val sendIntent: Intent = Intent()
            sendIntent.action = android.provider.Settings.ACTION_BLUETOOTH_SETTINGS
            startActivity(sendIntent)
        }
        recyclerViewCustom.apply {

            deviceAdapter = MyItemRecyclerViewAdapter()

            // Set the adapter
            with(recyclerViewCustom) {
                layoutManager = when {
                    columnCount <= 1 -> LinearLayoutManager(context)
                    else -> GridLayoutManager(context, columnCount)
                }
                adapter = deviceAdapter
            }

            deviceAdapter.setData(getBondedDevices().toList())

        }

        return view
    }

    //  Receive data of connected device
    var handler : Handler = Handler( object : Handler.Callback {
        override fun handleMessage(msg: Message): Boolean {
            when ( msg.what ) {
                STATE_LISTENING -> {
                    Toast.makeText(MyApp.context, "Listening", Toast.LENGTH_LONG).show()
                }
                STATE_CONNECTING -> {
                    Toast.makeText(MyApp.context, "Conectando", Toast.LENGTH_LONG).show()
                }
                STATE_CONNECTED -> {
                    Toast.makeText(MyApp.context, "Conectado", Toast.LENGTH_LONG).show()
                }
                STATE_CONNECTION_FAILED -> {
                    Toast.makeText(MyApp.context, "Failed", Toast.LENGTH_LONG).show()
                }
                STATE_MESSAGE_RECEIVED -> {
                    var readBuff : ByteArray = msg.obj as ByteArray
                    var tempMsg = String(readBuff, 0, msg.arg1)
                    textToSpeech.speak(tempMsg, TextToSpeech.QUEUE_FLUSH, null)
                }
            }
            return false
        }

    })

    override fun onStart() {
        super.onStart()
        deviceAdapter.notifyDataSetChanged()
        userPremium()
    }

    fun getBondedDevices() : MutableSet<BluetoothDevice> {
        var btDevice : Set<BluetoothDevice> = myBluetoothAdapter?.bondedDevices
        var listNameDevices : MutableSet<BluetoothDevice> = mutableSetOf()

        if (btDevice.isNotEmpty()) {

            btDevice?.forEach {
                listNameDevices.add(it)
            }
        }
        return listNameDevices
    }

    fun bluetoothEnable () {
        myBluetoothAdapter?.let {
            if (!myBluetoothAdapter.isEnabled) startActivityForResult(
                enablingIntent,
                REQUEST_CODE_ENABLED
            )
        }

        var discoverableIntent = Intent( BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE )
        discoverableIntent.putExtra( BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION,3600)
        startActivity( discoverableIntent )
    }
    fun userPremium() {
        db.collection("users")
            .whereEqualTo("email", SharedPreferencesManager.getSomeStringValue("user").toString())
            .get()
            .addOnSuccessListener { documents ->
                for (document in documents) {
                    var data : MutableMap<String, Any> = document.data
                    var isPremium = data.getValue("premium") as Boolean
                    if ( isPremium ) {
                        mAdView.visibility = View.GONE
                    }
                }
            }
            .addOnFailureListener { exception ->
                Log.w(
                    "Query",
                    "Error getting documents: ",
                    exception
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