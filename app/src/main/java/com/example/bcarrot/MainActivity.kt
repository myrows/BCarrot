package com.example.bcarrot

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)



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