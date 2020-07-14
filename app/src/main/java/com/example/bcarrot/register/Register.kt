package com.example.bcarrot.register

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import com.example.bcarrot.MainActivity
import com.example.bcarrot.R
import com.example.bcarrot.model.User
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.ktx.Firebase
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
    private lateinit var auth: FirebaseAuth
    private var mGoogleSignInClient: GoogleSignInClient? = null
    var pattern = "yyyy-MM-dd"
    var simpleDateFormat: SimpleDateFormat = SimpleDateFormat(pattern)
    var date: String = simpleDateFormat.format(Date())

    companion object {
        private const val RC_SIGN_IN = 1
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)

        intent = Intent( this@Register, MainActivity::class.java )
        mAuth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()

        auth = Firebase.auth
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail()
            .build()

        mGoogleSignInClient = GoogleSignIn.getClient(this, gso)

        imageViewCarrotArrowRegister.setOnClickListener {
            onBackPressed()
        }

        buttonR.setOnClickListener {
            signIn()
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

    private fun signIn() {
        val signInIntent = mGoogleSignInClient!!.signInIntent
        startActivityForResult(signInIntent, RC_SIGN_IN)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        // Result returned from launching the Intent from GoogleSignInApi.getSignInIntent(...);
        if (requestCode == RC_SIGN_IN) {
            val task = GoogleSignIn.getSignedInAccountFromIntent(data)
            try {
                // Google Sign In was successful, authenticate with Firebase
                val account = task.getResult(ApiException::class.java)!!
                Log.d("FirebaseAuth", "firebaseAuthWithGoogle:" + account.id)
                firebaseAuthWithGoogle(account.idToken!!)
            } catch (e: ApiException) {
                // Google Sign In failed, update UI appropriately
                Log.w("FirebaseAuth", "Google sign in failed", e)
                // ...
                Toast.makeText(this, "Error de autentificación", Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun firebaseAuthWithGoogle(idToken: String) {
        val credential = GoogleAuthProvider.getCredential(idToken, null)
        auth.signInWithCredential(credential)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    var userEmailAuthenticated = auth.currentUser!!.email
                    // Sign in success, update UI with the signed-in user's information
                    Log.d("FirebaseAuth", "signInWithCredential:success")
                    var intent = Intent( this@Register, MainActivity::class.java )
                    startActivity(intent)
                    var userSignin : User = User(userEmailAuthenticated!!, date, false, 300)
                    db.collection("users")
                        .whereEqualTo("email", userEmailAuthenticated)
                        .get()
                        .addOnSuccessListener { documents ->
                            if ( documents.size() <= 0 ) {
                                db.collection("users")
                                    .add(userSignin)
                                    .addOnSuccessListener {
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
                } else {
                    // If sign in fails, display a message to the user.
                    Log.w("FirebaseAuth", "signInWithCredential:failure", task.exception)
                    // ...
                    Toast.makeText(this, "Error de autentificación", Toast.LENGTH_LONG).show()
                }

                // ...
            }
    }

    fun registrationWithFirebase ( email : String, password : String ) {
        mAuth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener(this@Register) { task ->
                if (task.isSuccessful) {
                    var userSignin : User = User(email, date, false, 300)
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