package com.example.bcarrot

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.example.bcarrot.register.Login
import com.example.bcarrot.register.Register
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdSize
import com.google.android.gms.ads.AdView
import com.google.android.gms.ads.MobileAds
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {
    lateinit var mAdView: AdView
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        // Initialize mobile ads
        MobileAds.initialize(this, "ca-app-pub-4204713758500577~5237010154")

        buttonLoginMain.setOnClickListener {
            var intent = Intent(this@MainActivity, Login::class.java)
            startActivity(intent)
        }

        textViewRegisterNow.setOnClickListener {
            var intent = Intent(this@MainActivity, Register::class.java)
            startActivity(intent)
        }
    }
}