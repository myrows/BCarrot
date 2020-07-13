package com.example.bcarrot

import android.annotation.SuppressLint
import android.app.Activity
import android.app.AlertDialog
import android.bluetooth.BluetoothAdapter
import android.content.DialogInterface
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentTransaction
import coil.api.load
import coil.transform.CircleCropTransformation
import com.example.bcarrot.common.MyApp
import com.example.bcarrot.common.SharedPreferencesManager
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.rewarded.RewardItem
import com.google.android.gms.ads.rewarded.RewardedAd
import com.google.android.gms.ads.rewarded.RewardedAdCallback
import com.google.android.gms.ads.rewarded.RewardedAdLoadCallback
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.paypal.android.sdk.payments.*
import org.json.JSONException
import java.math.BigDecimal


class PremiumFragment : Fragment() {
    lateinit var rewardedAd : RewardedAd
    lateinit var bCoinLogic: BCoinLogic
    lateinit var buttonReward : Button
    lateinit var buttonPay : Button
    lateinit var paypalConfiguration : PayPalConfiguration
    lateinit var avatar : ImageView
    lateinit var userBCoins : TextView
    lateinit var userN : TextView
    lateinit var mAuth: FirebaseAuth
    lateinit var myBluetoothAdapter : BluetoothAdapter
    lateinit var fButtonChangeName : FloatingActionButton
    lateinit var myFragment : Fragment
    var db: FirebaseFirestore = FirebaseFirestore.getInstance()

    companion object {
        var REQUEST_CODE_PAYMENT = 7
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    @SuppressLint("ResourceType")
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        var view = inflater.inflate(R.layout.fragment_premium, container, false)
        myFragment = activity?.supportFragmentManager?.findFragmentById(R.id.fragment)!!
        avatar = view.findViewById(R.id.imageViewAvatar)
        userBCoins = view.findViewById(R.id.textViewUserBcoins)
        userN = view.findViewById(R.id.textViewUserName)
        fButtonChangeName = view.findViewById(R.id.floatingActionButtonLinkBluetoothName)
        bCoinLogic = BCoinLogic()
        mAuth = FirebaseAuth.getInstance()
        myBluetoothAdapter = BluetoothAdapter.getDefaultAdapter()
        var currentUser = mAuth.currentUser
        paypalConfiguration = PayPalConfiguration().environment(
            PayPalConfiguration.ENVIRONMENT_PRODUCTION).clientId(getString(R.string.clientId))

            avatar?.load("https://randomuser.me/api/portraits/men/4.jpg") {
                transformations(CircleCropTransformation())
            }

        getUserBcoins()

        fButtonChangeName.setOnClickListener {
            getUserBluetoothName()
            Toast.makeText(context, "Ahora tu matrícula está vinculada al nombre bluetooth", Toast.LENGTH_LONG).show()
        }

        // Inflate the layout for this fragment
        return view
    }

    override fun onStart() {
        super.onStart()
        loadAd()
        alertSuccess()
    }

    private fun alertSuccess () {
        val dialog  = AlertDialog.Builder(context)
        val dialogView = layoutInflater.inflate(R.layout.custom_alert_premium, null)
        buttonReward = dialogView.findViewById(R.id.buttonRewardAd)
        buttonPay = dialogView.findViewById(R.id.buttonPaypal)
        dialog.setView(dialogView)
        dialog.setCancelable(false)
        buttonReward.setOnClickListener {
            showAd()
        }
        buttonPay.setOnClickListener {
            paypalPayment()
        }
        dialog.setPositiveButton("Cerrar", { dialogInterface: DialogInterface, i: Int -> })
        val customDialog = dialog.create()
        customDialog.show()
        customDialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener {
            customDialog.dismiss()
        }
    }

    private fun alertRewarded () {
        val dialog  = AlertDialog.Builder(context)
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

    private fun alertPremium () {
        val dialog  = AlertDialog.Builder(context)
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

    fun loadAd() {
        rewardedAd = RewardedAd(context, "ca-app-pub-3940256099942544/5224354917" )
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
            rewardedAd.show(activity,
                object : RewardedAdCallback() {
                    override fun onRewardedAdFailedToShow(p0: Int) {
                        super.onRewardedAdFailedToShow(p0)
                        Toast.makeText(context, "Failed", Toast.LENGTH_LONG).show()
                    }
                    override fun onUserEarnedReward(p0: RewardItem) {
                        super.onUserEarnedReward(p0)
                        alertRewarded()
                        bCoinLogic.addBCoins( 100 )
                        // Update Fragment
                        refresh( PremiumFragment() )
                        // Load next video
                        loadAd()
                    }

                    override fun onRewardedAdClosed() {
                        super.onRewardedAdClosed()
                    }
                })
        }
    }

    fun paypalPayment() {
        var paypalPayment : PayPalPayment = PayPalPayment( BigDecimal("0.99"), "EUR", "Pago PREMIUM BCarrot", PayPalPayment.PAYMENT_INTENT_SALE )
        var intent : Intent = Intent(MyApp.context, PaymentActivity::class.java)
        intent.putExtra(PayPalService.EXTRA_PAYPAL_CONFIGURATION, paypalConfiguration)
        intent.putExtra(PaymentActivity.EXTRA_PAYMENT, paypalPayment)
        startActivityForResult( intent, REQUEST_CODE_PAYMENT )
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if ( requestCode == REQUEST_CODE_PAYMENT ) {
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
                Toast.makeText(context, "Pago cancelado", Toast.LENGTH_LONG).show()
            } else if ( resultCode == PaymentActivity.RESULT_EXTRAS_INVALID ) {
                Toast.makeText(context, "Ha ocurrido un error", Toast.LENGTH_LONG).show()
            }
        }
    }

    fun getUserBcoins () {
        var bCoins : Long = 0

        db.collection("users")
            .whereEqualTo("email", SharedPreferencesManager.getSomeStringValue("user").toString())
            .get()
            .addOnSuccessListener { documents ->
                for (document in documents) {
                    var data : MutableMap<String, Any> = document.data
                    bCoins = data.getValue("bcoins") as Long
                    userBCoins.text = bCoins.toString()
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

    fun getUserBluetoothName() {
        db.collection("users")
            .whereEqualTo("email", SharedPreferencesManager.getSomeStringValue("user").toString())
            .get()
            .addOnSuccessListener { documents ->
                for (document in documents) {
                    db.collection("users").document(document.id)
                        .collection("vehicle")
                        .get()
                        .addOnSuccessListener { documents ->
                            for ( document in documents ) {
                                var data : MutableMap<String, Any> = document.data
                                myBluetoothAdapter.name = data.getValue("plate") as String
                            }
                        }.addOnFailureListener { exception ->
                        Log.w(
                            "Query",
                            "Error getting documents: ",
                            exception
                        )
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

    fun refresh( fragment: Fragment ) = requireFragmentManager().beginTransaction().apply {
        commit()
    }

    fun disableRewardButton() {
        buttonReward.isEnabled = true
        buttonReward.background = ContextCompat.getDrawable(requireContext(), R.drawable.button )
    }
}