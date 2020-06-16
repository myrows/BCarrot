package com.example.bcarrot

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.FragmentActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import kotlinx.android.synthetic.main.activity_login.*


class Login : AppCompatActivity() {
    lateinit var mAuth : FirebaseAuth
    lateinit var email : String
    lateinit var password : String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        intent = Intent( this@Login, UserOperationsActivity::class.java )
        mAuth = FirebaseAuth.getInstance()

        // Login forms

        imageViewCarrotArrow.setOnClickListener {
            onBackPressed()
        }

        buttonLogin.setOnClickListener {
            email = editTextEmail.text.toString()
            password = editTextPassword.text.toString()

            mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(
                    this
                ) { task ->
                    if (task.isSuccessful) {
                        // Sign in success, update UI with the signed-in user's information
                        Log.d("Login", "signInWithEmail:success")
                        startActivity( intent )

                        val user = mAuth.currentUser
                    } else {
                        // If sign in fails, display a message to the user.
                        Log.w("Login","signInWithEmail:failure", task.exception)
                        Toast.makeText(this@Login, "Autenticación errónea", Toast.LENGTH_SHORT).show()
                    }
                }
        }
    }

    override fun onStart() {
        super.onStart()
        var currentUser : FirebaseUser? = mAuth.currentUser

        currentUser?.let {
            startActivity( intent )
        }
    }
}