package com.example.bcarrot.register

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Toast
import com.example.bcarrot.MainActivity
import com.example.bcarrot.R
import com.example.bcarrot.model.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.android.synthetic.main.activity_register.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

class Register : AppCompatActivity() {
    lateinit var mAuth : FirebaseAuth
    lateinit var email : String
    lateinit var password : String
    lateinit var db: FirebaseFirestore
    var pattern = "yyyy-MM-dd"
    var simpleDateFormat: SimpleDateFormat = SimpleDateFormat(pattern)
    var date: String = simpleDateFormat.format(Date())

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
            GlobalScope.launch(Dispatchers.Main) {
                email = editTextEmailRegister.text.toString()
                password = editTextPasswordRegister.text.toString()

                // Form validate
                formValidate()

                if (email?.isNotEmpty() && password?.isNotEmpty()) {
                    formValidateNoEmpty()
                    registrationWithFirebase(email, password)
                }
            }
        }
    }

    fun formValidate() {
        when ( true ) {
            email.isEmpty() -> {
                textViewEmailErrorRegister.visibility = View.VISIBLE
                textViewPasswordErrorRegister.visibility = View.INVISIBLE
            }
            password.isEmpty() -> {
                textViewEmailErrorRegister.visibility = View.INVISIBLE
                textViewPasswordErrorRegister.visibility = View.VISIBLE
            }
        }
    }

    fun formValidateNoEmpty() {
        textViewEmailErrorRegister.visibility = View.INVISIBLE
        textViewPasswordErrorRegister.visibility = View.INVISIBLE
    }

    fun registrationWithFirebase ( email : String, password : String ) {
        mAuth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener(this@Register) { task ->
                if (task.isSuccessful) {
                    var userSignin : User = User(email, date, false, 0)
                    db.collection("users")
                        .add(userSignin)
                        .addOnSuccessListener {
                            startActivity(intent)
                        }
                } else {
                    Toast.makeText(this@Register, "Ha ocurrido un error", Toast.LENGTH_SHORT).show()
                }
            }
    }
}