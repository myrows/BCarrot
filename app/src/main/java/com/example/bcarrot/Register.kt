package com.example.bcarrot

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.android.synthetic.main.activity_register.*

class Register : AppCompatActivity() {
    lateinit var mAuth : FirebaseAuth
    lateinit var email : String
    lateinit var password : String
    lateinit var db: FirebaseFirestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)

        intent = Intent( this@Register, MainActivity::class.java )
        mAuth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()

        imageViewCarrotArrowRegister.setOnClickListener {
            onBackPressed()
        }

        buttonRegister.setOnClickListener {
            email = editTextEmailRegister.text.toString()
            password = editTextPasswordRegister.text.toString()

            //  Register users
            mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this) { task ->
                    if (task.isSuccessful) {
                        // Sign in success, update UI with the signed-in user's information
                        Log.d("Login", "createUserWithEmail:success")

                        var userSignin : User = User( email )
                        db.collection("users")
                            .add(userSignin)
                            .addOnSuccessListener {
                                startActivity(intent)
                            }
                    } else {
                        // If sign in fails, display a message to the user.
                        Log.w("Login","createUserWithEmail:failure", task.exception)
                        Toast.makeText(this@Register, "Ha ocurrido un error", Toast.LENGTH_SHORT).show()
                    }
                }
        }
    }
}