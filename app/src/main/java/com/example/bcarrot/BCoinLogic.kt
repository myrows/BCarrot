package com.example.bcarrot

import android.content.Intent
import android.graphics.Bitmap
import android.media.MediaPlayer
import android.util.Log
import androidx.core.content.contentValuesOf
import com.example.bcarrot.common.MyApp
import com.example.bcarrot.common.SharedPreferencesManager
import com.google.firebase.firestore.FirebaseFirestore
import com.paypal.android.sdk.payments.PayPalConfiguration
import com.paypal.android.sdk.payments.PayPalPayment
import com.paypal.android.sdk.payments.PayPalService
import com.paypal.android.sdk.payments.PaymentActivity
import kotlinx.coroutines.withContext
import java.math.BigDecimal

class BCoinLogic {
    var db: FirebaseFirestore = FirebaseFirestore.getInstance()
    var mediaPlayer : MediaPlayer = MediaPlayer.create(MyApp.context, R.raw.coins_reward_sound)
    var mediaPlayerPay : MediaPlayer = MediaPlayer.create(MyApp.context, R.raw.get_bcoins)
    var coins : Long = 0

    fun addBCoins ( value : Long ) {
        db.collection("users")
            .whereEqualTo("email", SharedPreferencesManager.getSomeStringValue("user").toString())
            .get()
            .addOnSuccessListener { documents ->
                for (document in documents) {
                    var data : MutableMap<String, Any> = document.data
                    coins = data.getValue("bcoins") as Long
                    var valueCoins : Number =  coins + value
                    var map = mutableMapOf<String, Any>()
                    map.put(
                        key = "bcoins",
                        value = valueCoins
                    )
                    db.collection("users")
                        .document(document.id).update(map)
                        .addOnSuccessListener {
                            mediaPlayer.start()
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

    fun payBCoins ( value: Long ) {
        db.collection("users")
            .whereEqualTo("email", SharedPreferencesManager.getSomeStringValue("user").toString())
            .get()
            .addOnSuccessListener { documents ->
                for (document in documents) {
                    var data : MutableMap<String, Any> = document.data
                    coins = data.getValue("bcoins") as Long
                    var valueCoins : Number =  coins as Long - value
                    Log.d("Pay", "$valueCoins")
                    var map = mutableMapOf<String, Any>()
                    map.put(
                        key = "bcoins",
                        value = valueCoins
                    )
                    db.collection("users")
                        .document(document.id).update(map)
                        .addOnSuccessListener {
                            Log.d("Pay", "${documents.size()}")
                            mediaPlayerPay.start()
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

    fun getUserBcoins () : Long {
        var bCoins : Long = 0
        var result : Long = 0

        db.collection("users")
            .whereEqualTo("email", SharedPreferencesManager.getSomeStringValue("user").toString())
            .get()
            .addOnSuccessListener { documents ->
                for (document in documents) {
                    var data : MutableMap<String, Any> = document.data
                    bCoins = data.getValue("bcoins") as Long
                    result = bCoins
                    Log.d(
                        "Query",
                        "$result"
                    )
                }
            }
            .addOnFailureListener { exception ->
                Log.w(
                    "Query",
                    "Error getting documents: ",
                    exception
                )
            }
        return result
    }

    fun getPremium() {
        db.collection("users")
            .whereEqualTo("email", SharedPreferencesManager.getSomeStringValue("user").toString())
            .get()
            .addOnSuccessListener { documents ->
                for (document in documents) {
                    var data : MutableMap<String, Any> = document.data
                    var isPremium = data.getValue("premium") as Boolean
                    if ( !isPremium ) {
                        var map = mutableMapOf<String, Any>()
                        map.put(
                            key = "premium",
                            value = true
                        )
                        db.collection("users")
                            .document(document.id).update(map)
                            .addOnSuccessListener {
                            }.addOnFailureListener {
                                Log.d(
                                    "Update",
                                    "Ha ocurrido un error al cambiar el estado a premium"
                                )
                            }
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
}
