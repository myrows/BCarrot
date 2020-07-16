package com.example.bcarrot

import android.annotation.SuppressLint
import android.app.Activity
import android.app.AlertDialog
import android.bluetooth.BluetoothAdapter
import android.content.DialogInterface
import android.content.Intent
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
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
import coil.api.load
import coil.transform.CircleCropTransformation
import com.example.bcarrot.common.MyApp
import com.example.bcarrot.common.SharedPreferencesManager
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.rewarded.RewardItem
import com.google.android.gms.ads.rewarded.RewardedAd
import com.google.android.gms.ads.rewarded.RewardedAdCallback
import com.google.android.gms.ads.rewarded.RewardedAdLoadCallback
import com.google.android.gms.tasks.OnFailureListener
import com.google.android.gms.tasks.OnSuccessListener
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FileDownloadTask
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.google.firebase.storage.UploadTask
import com.paypal.android.sdk.payments.*
import org.json.JSONException
import java.io.File
import java.math.BigDecimal
import java.util.*


class PremiumFragment : Fragment() {
    lateinit var rewardedAd : RewardedAd
    lateinit var bCoinLogic: BCoinLogic
    lateinit var buttonReward : Button
    lateinit var buttonPay : Button
    lateinit var buttonDonate : Button
    lateinit var paypalConfiguration : PayPalConfiguration
    lateinit var avatar : ImageView
    lateinit var userBCoins : TextView
    lateinit var mAuth: FirebaseAuth
    lateinit var myBluetoothAdapter : BluetoothAdapter
    lateinit var fButtonChangeName : FloatingActionButton
    lateinit var myFragment : Fragment
    lateinit var userEmail : TextView
    lateinit var signOut : ImageView
    lateinit var infoBcoins : ImageView
    var db: FirebaseFirestore = FirebaseFirestore.getInstance()
    lateinit var filePath : Uri
    lateinit var storage : FirebaseStorage
    lateinit var storageReference : StorageReference
    lateinit var reference : StorageReference

    companion object {
        var REQUEST_CODE_PAYMENT = 7
        var REQUEST_CODE_IMAGE = 111
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
        findIndexesById( view )
        bCoinLogic = BCoinLogic()
        mAuth = FirebaseAuth.getInstance()
        myBluetoothAdapter = BluetoothAdapter.getDefaultAdapter()
        storage = FirebaseStorage.getInstance()
        storageReference = storage.reference
        paypalConfiguration = PayPalConfiguration().environment(
            PayPalConfiguration.ENVIRONMENT_PRODUCTION).clientId("AR6pKwEkquoMvRRVDDWGV8ImBi4upPpjNduxShBvDeuJ3p59CFFfYPdFKmd2_vBPTf_XkA3NTttpAiZP")

        checkAvatar()

        getUserBcoins()

        fButtonChangeName.setOnClickListener {
            userPremiumPlateAttach()
        }

        infoBcoins.setOnClickListener {
            loadAd()
            alertSupport()
        }

        avatar.setOnClickListener {
            startFileChooser()
        }

        signOut.setOnClickListener {
            var intentSignOut = Intent( context, MainActivity::class.java )
            context?.startActivity( intentSignOut )
            Firebase.auth.signOut()
        }
        userEmail.text = SharedPreferencesManager.getSomeStringValue("user").toString()

        // Inflate the layout for this fragment
        return view
    }

    private fun findIndexesById( view : View ) {
        myFragment = activity?.supportFragmentManager?.findFragmentById(R.id.fragment)!!
        avatar = view.findViewById(R.id.imageViewAvatar)
        userBCoins = view.findViewById(R.id.textViewUserBcoins)
        fButtonChangeName = view.findViewById(R.id.floatingActionButtonLinkBluetoothName)
        userEmail = view.findViewById(R.id.textViewUserEmail)
        signOut = view.findViewById(R.id.imageViewSignOut)
        infoBcoins = view.findViewById(R.id.imageViewBcoins)
    }

    override fun onStart() {
        super.onStart()
        loadAd()
        userNoPremium()
    }

    private fun startFileChooser() {
        var i = Intent()
        i.setType( "image/*" )
        i.setAction( Intent.ACTION_GET_CONTENT )
        startActivityForResult( Intent.createChooser( i, "Elige una imagen para BCarrot" ), REQUEST_CODE_IMAGE )
    }

    fun checkAvatar() {
        db.collection("users")
            .whereEqualTo("email", SharedPreferencesManager.getSomeStringValue("user").toString())
            .get()
            .addOnSuccessListener { documents ->
                for ( document in documents ) {
                    var data : MutableMap<String, Any> = document.data
                    var userAvatar = data.getValue("avatar") as String
                    if (userAvatar.isEmpty() || userAvatar == null) {
                        avatar.setImageDrawable(requireContext().getDrawable(R.drawable.ic_zanahoria))
                    } else {
                        getUserAvatar()
                    }
                }

            }
            .addOnFailureListener { exception ->
                Log.w("Query", "Error getting documents: ", exception)
            }
    }

    fun manageAvatar ( imageName : String ) {
        db.collection("users")
            .whereEqualTo("email", SharedPreferencesManager.getSomeStringValue("user").toString())
            .get()
            .addOnSuccessListener { documents ->
                for ( document in documents ) {
                    var data : MutableMap<String, Any> = document.data
                    var avatar = data.getValue("avatar") as String
                    if (avatar.isEmpty() || avatar == null) {
                        //setUserAvatar( imageName )
                    } else {
                        //updateUserAvatar( imageName )
                    }
                }

            }
            .addOnFailureListener { exception ->
                Log.w("Query", "Error getting documents: ", exception)
            }
    }

    fun setUserAvatar( imageName : String ) {
        val avatar = hashMapOf(
            "avatar" to "$imageName"
        )
        db.collection("users")
            .whereEqualTo("email", SharedPreferencesManager.getSomeStringValue("user").toString())
            .get()
            .addOnSuccessListener { documents ->
                    for ( document in documents ) {
                        db.collection("users").document(document.id).set(avatar)
                            .addOnSuccessListener {

                            }
                            .addOnFailureListener{

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

    fun updateUserAvatar ( imageName: String ) {
        db.collection("users")
            .whereEqualTo("email", SharedPreferencesManager.getSomeStringValue("user").toString())
            .get()
            .addOnSuccessListener { documents ->
                for (document in documents) {
                    var map = mutableMapOf<String, Any>()
                    map.put(
                        key = "avatar",
                        value = imageName
                    )
                    db.collection("users")
                        .document(document.id).update(map)
                        .addOnSuccessListener {
                        }.addOnFailureListener {
                            Log.d(
                                "Update",
                                "Ha ocurrido un error al actualizar el documento"
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

    fun getUserAvatar() {
        db.collection("users")
            .whereEqualTo("email", SharedPreferencesManager.getSomeStringValue("user").toString())
            .get()
            .addOnSuccessListener { documents ->
                for (document in documents) {
                    var data : MutableMap<String, Any> = document.data
                    var avatar : String = data.getValue("avatar") as String
                    getFile( avatar )
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

    private fun uploadFile() {
        var uuidRandom : String = UUID.randomUUID().toString()
        if ( filePath != null ) {
            reference = storageReference.child( uuidRandom )
            reference.putFile(filePath)
                .addOnSuccessListener(object : OnSuccessListener<UploadTask.TaskSnapshot?> {
                    override fun onSuccess(p0: UploadTask.TaskSnapshot?) {
                        updateUserAvatar( uuidRandom )
                    }
                })
                .addOnFailureListener(object : OnFailureListener {
                    override fun onFailure(p0: java.lang.Exception) {
                    }
                })
        }
    }

    fun getFile( image : String ) {
        var storageReference : StorageReference = storage.getReferenceFromUrl("gs://bcarrot-21b85.appspot.com/").child(image)
        var file : File = File.createTempFile("image", "jpg")
        storageReference.getFile(file)
            .addOnSuccessListener(object : OnSuccessListener<FileDownloadTask.TaskSnapshot> {
                override fun onSuccess(p0: FileDownloadTask.TaskSnapshot) {
                    var bitmap = BitmapFactory.decodeFile( file.absolutePath )
                    avatar.load( bitmap ) {
                        transformations(CircleCropTransformation())
                    }
                }
            })
            .addOnFailureListener(object : OnFailureListener {
                override fun onFailure(p0: java.lang.Exception) {
                }
            })
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

    private fun alertSupport () {
        val dialog  = AlertDialog.Builder(context)
        val dialogView = layoutInflater.inflate(R.layout.custom_donate, null)
        buttonReward = dialogView.findViewById(R.id.buttonRewardAd)
        buttonDonate = dialogView.findViewById(R.id.buttonDonate)
        dialog.setView(dialogView)
        dialog.setCancelable(false)
        buttonReward.setOnClickListener {
            showAd()
        }
        buttonDonate.setOnClickListener {
            paypalPaymentDonate()
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

    fun paypalPaymentDonate() {
        var paypalPayment : PayPalPayment = PayPalPayment( BigDecimal("4.99"), "EUR", "Donación BCarrot", PayPalPayment.PAYMENT_INTENT_SALE )
        var intent : Intent = Intent(MyApp.context, PaymentActivity::class.java)
        intent.putExtra(PayPalService.EXTRA_PAYPAL_CONFIGURATION, paypalConfiguration)
        intent.putExtra(PaymentActivity.EXTRA_PAYMENT, paypalPayment)
        startActivityForResult( intent, REQUEST_CODE_PAYMENT )
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        when ( requestCode ) {
            REQUEST_CODE_PAYMENT -> {
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
            REQUEST_CODE_IMAGE -> {
                if ( resultCode == Activity.RESULT_OK ) {
                    filePath = data?.data!!
                    var bitmap = MediaStore.Images.Media.getBitmap( requireActivity().contentResolver, filePath )
                    avatar.setImageBitmap(bitmap)
                    avatar.load(bitmap) {
                        transformations(CircleCropTransformation())
                    }
                    uploadFile()
                }
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

    fun userNoPremium() {
        db.collection("users")
            .whereEqualTo("email", SharedPreferencesManager.getSomeStringValue("user").toString())
            .get()
            .addOnSuccessListener { documents ->
                for (document in documents) {
                    var data : MutableMap<String, Any> = document.data
                    var isPremium = data.getValue("premium") as Boolean
                    if ( !isPremium ) {
                        alertSuccess()
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

    fun userPremiumPlateAttach() {
        db.collection("users")
            .whereEqualTo("email", SharedPreferencesManager.getSomeStringValue("user").toString())
            .get()
            .addOnSuccessListener { documents ->
                for (document in documents) {
                    var data : MutableMap<String, Any> = document.data
                    var isPremium = data.getValue("premium") as Boolean
                    if ( isPremium ) {
                        getUserBluetoothName()
                        Toast.makeText(context, "Ahora tu matrícula está vinculada al nombre bluetooth", Toast.LENGTH_LONG).show()
                    } else {
                        loadAd()
                        alertSuccess()
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
        buttonReward.isEnabled = true
        buttonReward.background = ContextCompat.getDrawable(requireContext(), R.drawable.button )
    }
}