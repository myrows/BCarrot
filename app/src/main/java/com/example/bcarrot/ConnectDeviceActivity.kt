package com.example.bcarrot

import android.app.Activity
import android.app.AlertDialog
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.content.DialogInterface
import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.os.Message
import android.provider.MediaStore
import android.speech.RecognizerIntent
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.Toast
import androidx.core.content.ContextCompat
import com.example.bcarrot.common.MyApp
import com.example.bcarrot.common.SharedPreferencesManager
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.rewarded.RewardItem
import com.google.android.gms.ads.rewarded.RewardedAd
import com.google.android.gms.ads.rewarded.RewardedAdCallback
import com.google.android.gms.ads.rewarded.RewardedAdLoadCallback
import com.google.firebase.firestore.FirebaseFirestore
import com.paypal.android.sdk.payments.*
import kotlinx.android.synthetic.main.activity_connect_device.*
import org.json.JSONException
import java.lang.Exception
import java.math.BigDecimal
import java.util.*
import kotlin.collections.ArrayList

class ConnectDeviceActivity : AppCompatActivity() {
    lateinit var bCoinLogic : BCoinLogic
    lateinit var buttonReward : Button
    lateinit var rewardedAd : RewardedAd
    lateinit var paypalConfiguration : PayPalConfiguration
    lateinit var clientClass : ClientClass
    lateinit var myBluetoothAdapter : BluetoothAdapter
    var db: FirebaseFirestore = FirebaseFirestore.getInstance()
    lateinit var buttonVideo : Button

    companion object {
        var stop = R.string.stop
        var accelerate = R.string.accelerate
        var left = R.string.left
        var right = R.string.right
        var STATE_LISTENING = 1
        var STATE_CONNECTING = 2
        var STATE_CONNECTED = 3
        var STATE_CONNECTION_FAILED = 4
        var STATE_MESSAGE_RECEIVED = 5
        var REQUEST_CODE_SPEECH_INPUT = 1000
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_connect_device)
        bCoinLogic = BCoinLogic()
        paypalConfiguration = PayPalConfiguration().environment(
            PayPalConfiguration.ENVIRONMENT_PRODUCTION).clientId(getString(R.string.clientId))
        /*textToSpeech= TextToSpeech(this, TextToSpeech.OnInitListener {
            if ( it == TextToSpeech.SUCCESS ) {
                textToSpeech.language = Locale.getDefault()
            }
        })*/
        myBluetoothAdapter = BluetoothAdapter.getDefaultAdapter()
        var addressDevice : String = intent.extras!!.get("deviceBcarrot").toString()
        var device = myBluetoothAdapter.getRemoteDevice(addressDevice)
        // Client connection
        clientClass = ClientClass( device, handler )
        clientClass.start()

        // Car actions
        imageViewIzquierda.setOnClickListener {
            clientClass.sendData("El vehículo gira a la izquierda")
        }
        imageViewFrenar.setOnClickListener {
            clientClass.sendData("El vehículo frena")
        }
        imageViewAcelerar.setOnClickListener {
            clientClass.sendData("El vehículo acelera")
        }
        imageViewDerecha.setOnClickListener {
            clientClass.sendData("El vehículo gira a la derecha")
        }
            imageViewVoice.setOnClickListener {
                userNoPremium()
            }
        vehiclePlate()
    }

    private fun vehiclePlate() {
        db.collection("users")
            .whereEqualTo("email", SharedPreferencesManager.getSomeStringValue("user").toString())
            .get()
            .addOnSuccessListener { documents ->
                for (document in documents) {
                    Log.d("Query", "${document.id} => ${document.data}")
                    db.collection("users").document(document.id).collection("vehicle")
                        .get()
                        .addOnSuccessListener { documents ->
                            for ( document in documents ) {
                                var data : MutableMap<String, Any> = document.data
                                var plate : String = data.getValue("plate") as String
                                if ( plate.isNotEmpty() || plate != null ) {
                                    textViewVehicleName.text = plate
                                } else {
                                    textViewVehicleName.text = "Sin matrícula"
                                }
                            }

                        }
                        .addOnFailureListener { exception ->
                            Log.w("Query", "Error getting documents: ", exception)
                        }
                }
            }
            .addOnFailureListener { exception ->
                Log.w("Query", "Error getting documents: ", exception)
                Toast.makeText(this@ConnectDeviceActivity, "Error al crear el vehículo", Toast.LENGTH_LONG).show()
            }
    }

    //  Receive data of connected device
    var handler : Handler = Handler( object : Handler.Callback {
        override fun handleMessage(msg: Message): Boolean {
            when ( msg.what ) {
                STATE_LISTENING -> {
                    Toast.makeText(this@ConnectDeviceActivity, "Listening", Toast.LENGTH_LONG).show()
                }
                STATE_CONNECTING -> {
                    Toast.makeText(this@ConnectDeviceActivity, "Conectando", Toast.LENGTH_LONG).show()
                }
                STATE_CONNECTED -> {
                    Toast.makeText(this@ConnectDeviceActivity, "Conectado", Toast.LENGTH_LONG).show()
                }
                STATE_CONNECTION_FAILED -> {
                    onBackPressed()
                }
                STATE_MESSAGE_RECEIVED -> {
                    //textToSpeech.speak(tempMsg, TextToSpeech.QUEUE_FLUSH, null)
                    var readBuff : ByteArray = msg.obj as ByteArray
                    var tempMsg = String(readBuff, 0, msg.arg1)
                    Toast.makeText(this@ConnectDeviceActivity, "Mensaje recibido, $tempMsg", Toast.LENGTH_LONG).show()
                }
            }
            return false
        }

    })

    private fun alertSuccess () {
        val dialog  = AlertDialog.Builder(this)
        val dialogView = layoutInflater.inflate(R.layout.custom_alert_premium, null)
        buttonReward = dialogView.findViewById(R.id.buttonRewardAd)
        dialog.setView(dialogView)
        dialog.setCancelable(false)
        buttonReward.setOnClickListener {
            showAd()
        }
        dialog.setPositiveButton("Cerrar", { dialogInterface: DialogInterface, i: Int -> })
        val customDialog = dialog.create()
        customDialog.show()
        customDialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener {
            customDialog.dismiss()
        }
    }

    fun loadAd() {
        rewardedAd = RewardedAd(this, "ca-app-pub-3940256099942544/5224354917" )
        var callback : RewardedAdLoadCallback = object : RewardedAdLoadCallback() {
            override fun onRewardedAdFailedToLoad(p0: Int) {
                super.onRewardedAdFailedToLoad(p0)

            }
            override fun onRewardedAdLoaded() {
                super.onRewardedAdLoaded()
                disableRewardButton()
            }
        }
        rewardedAd.loadAd( AdRequest.Builder().build(), callback )
    }

    fun showAd() {
        if ( rewardedAd.isLoaded ) {
            rewardedAd.show(this,
                object : RewardedAdCallback() {
                    override fun onRewardedAdFailedToShow(p0: Int) {
                        super.onRewardedAdFailedToShow(p0)
                        Toast.makeText(this@ConnectDeviceActivity, "Failed", Toast.LENGTH_LONG).show()
                    }
                    override fun onUserEarnedReward(p0: RewardItem) {
                        super.onUserEarnedReward(p0)
                        alertRewarded()
                        bCoinLogic.addBCoins( 100 )
                        // Load next video
                        loadAd()
                    }

                    override fun onRewardedAdClosed() {
                        super.onRewardedAdClosed()
                    }
                })
        }
    }

    private fun alertRewarded () {
        val dialog  = AlertDialog.Builder(this)
        val dialogView = layoutInflater.inflate(R.layout.custom_alert_rewarded, null)
        dialog.setView(dialogView)
        dialog.setCancelable(false)
        dialog.setPositiveButton("Cerrar", { dialogInterface: DialogInterface, i: Int -> })
        val customDialog = dialog.create()
        customDialog.show()
        customDialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener {
            customDialog.dismiss()
        }
    }

    fun alertPayBcoins () {
        val dialog  = AlertDialog.Builder(this)
        val dialogView = layoutInflater.inflate(R.layout.custom_pay_bcoins, null)
        buttonVideo = dialogView.findViewById(R.id.buttonGetAnnounce)
        dialog.setView(dialogView)
        dialog.setCancelable(true)
        buttonVideo.setOnClickListener {
            showAd()
        }
        dialog.setPositiveButton("Cerrar", { dialogInterface: DialogInterface, i: Int -> })
        val customDialog = dialog.create()
        customDialog.show()
        customDialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener {
            customDialog.dismiss()
        }
    }

    fun checkBcoinsVoice () {
        var coins : Long = 0
        db.collection("users")
            .whereEqualTo("email", SharedPreferencesManager.getSomeStringValue("user").toString())
            .get()
            .addOnSuccessListener { documents ->
                for (document in documents) {
                    var data : MutableMap<String, Any> = document.data
                    coins = data.getValue("bcoins") as Long
                    if ( coins >= 300 ) {
                        // Pay Bcoins
                        bCoinLogic.payBCoins( 300 )
                        activateVoiceIndication()
                    } else {
                        loadAd()
                        alertPayBcoins()
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

    fun activateVoiceIndication() {
        var intent : Intent = Intent( RecognizerIntent.ACTION_RECOGNIZE_SPEECH )
        intent.putExtra( RecognizerIntent.EXTRA_LANGUAGE_MODEL , RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
        intent.putExtra( RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault() )
        intent.putExtra( RecognizerIntent.EXTRA_PROMPT, "Te estoy escuchando, háblame" )

        // Start intent
        try {
            startActivityForResult( intent, REQUEST_CODE_SPEECH_INPUT )
        }catch (e : Exception) {
            Log.d("Voice", "Ha ocurrido un error - ${e.message}")
        }
    }

    fun userNoPremium() {
        db.collection("users")
            .whereEqualTo("email", SharedPreferencesManager.getSomeStringValue("user").toString())
            .get()
            .addOnSuccessListener { documents ->
                for (document in documents) {
                    var data : MutableMap<String, Any> = document.data
                    var isPremium = data.getValue("premium") as Boolean
                    if ( !isPremium ) {
                        checkBcoinsVoice()
                    } else {
                        activateVoiceIndication()
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

    fun disableRewardButton() {
        buttonVideo.isEnabled = true
        buttonVideo.background = ContextCompat.getDrawable(this, R.drawable.button )
    }

    private fun closeSession() {
        floatingActionButtonBluetoothDisconnect.setOnClickListener {
            onBackPressed()
            clientClass.cancel()
        }
    }

    override fun onStart() {
        super.onStart()
    }

    override fun onDestroy() {
        super.onDestroy()
    }

    override fun onStop() {
        super.onStop()
        //textToSpeech.stop()
    }

    fun paypalPayment() {
        var paypalPayment : PayPalPayment = PayPalPayment( BigDecimal("0.99"), "EUR", "Pago PREMIUM BCarrot", PayPalPayment.PAYMENT_INTENT_SALE )
        var intent : Intent = Intent(MyApp.context, PaymentActivity::class.java)
        intent.putExtra(PayPalService.EXTRA_PAYPAL_CONFIGURATION, paypalConfiguration)
        intent.putExtra(PaymentActivity.EXTRA_PAYMENT, paypalPayment)
        startActivityForResult( intent, PremiumFragment.REQUEST_CODE_PAYMENT)
    }

    private fun alertPremium () {
        val dialog  = AlertDialog.Builder(this)
        val dialogView = layoutInflater.inflate(R.layout.custom_premium, null)
        dialog.setView(dialogView)
        dialog.setCancelable(true)
        dialog.setPositiveButton("Cerrar", { dialogInterface: DialogInterface, i: Int -> })
        val customDialog = dialog.create()
        customDialog.show()
        customDialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener {
            customDialog.dismiss()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        when (requestCode) {
            REQUEST_CODE_SPEECH_INPUT -> {
                if ( resultCode == Activity.RESULT_OK ) {
                    var result : ArrayList<String>? = data?.getStringArrayListExtra( RecognizerIntent.EXTRA_RESULTS )
                    if (result != null) {
                        clientClass.sendData(result[0])
                    }
                }
            }
            PremiumFragment.REQUEST_CODE_PAYMENT -> {
                if ( resultCode == Activity.RESULT_OK ) {
                    var confirm : PaymentConfirmation? = data?.getParcelableExtra( PaymentActivity.EXTRA_RESULT_CONFIRMATION )
                    if ( confirm != null ) {
                        try {
                            alertPremium()
                            bCoinLogic.getPremium()

                        }catch (e : JSONException) {
                            Log.d("PayPalError", "${e.message}")
                        }
                    }
                } else if ( resultCode == Activity.RESULT_CANCELED ) {
                    Toast.makeText(this, "Pago cancelado", Toast.LENGTH_LONG).show()
                } else if ( resultCode == PaymentActivity.RESULT_EXTRAS_INVALID ) {
                    Toast.makeText(this, "Ha ocurrido un error", Toast.LENGTH_LONG).show()
                }
            }
        }
    }
}