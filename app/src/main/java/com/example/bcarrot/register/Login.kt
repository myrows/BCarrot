package com.example.bcarrot.register

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.annotation.Nullable
import androidx.appcompat.app.AppCompatActivity
import com.example.bcarrot.MainActivity
import com.example.bcarrot.NavigationActivity
import com.example.bcarrot.R
import com.example.bcarrot.common.SharedPreferencesManager
import com.example.bcarrot.common.SharedPreferencesManager.setSomeStringValue
import com.example.bcarrot.model.User
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.tasks.OnCompleteListener
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.auth.ktx.auth
import kotlinx.android.synthetic.main.activity_login.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*


class Login : AppCompatActivity() {
    lateinit var mAuth: FirebaseAuth
    lateinit var email: String
    lateinit var password: String
    private var mGoogleSignInClient: GoogleSignInClient? = null
    private val db = FirebaseFirestore.getInstance()
    private lateinit var auth: FirebaseAuth
    var pattern = "yyyy-MM-dd"
    var simpleDateFormat: SimpleDateFormat = SimpleDateFormat(pattern)
    var date: String = simpleDateFormat.format(Date())

    companion object {
        private const val RC_SIGN_IN = 1
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        intent = Intent(this@Login, NavigationActivity::class.java)
        mAuth = FirebaseAuth.getInstance()
        auth = Firebase.auth
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail()
            .build()

        mGoogleSignInClient = GoogleSignIn.getClient(this, gso)

        buttonGoogleLogin.setOnClickListener {
            signIn()
        }

        imageViewCarrotArrow.setOnClickListener {
            onBackPressed()
        }

        buttonLogin.setOnClickListener {
            GlobalScope.launch(Dispatchers.Main) {
                email = editTextEmail.text.toString()
                password = editTextPassword.text.toString()

                // Form validate
                formValidate()

                if (email?.isNotEmpty() && password?.isNotEmpty()) {
                    formValidateNoEmpty()
                    firebaseAutentication(email, password)
                }
            }
        }
    }

    fun formValidate() {
        when ( true ) {
            email.isEmpty() -> {
                textViewEmailError.visibility = View.VISIBLE
                textViewPasswordError.visibility = View.INVISIBLE
            }
            password.isEmpty() -> {
                textViewEmailError.visibility = View.INVISIBLE
                textViewPasswordError.visibility = View.VISIBLE
            }
        }
    }

    fun formValidateNoEmpty() {
        textViewEmailError.visibility = View.INVISIBLE
        textViewPasswordError.visibility = View.INVISIBLE
    }

    fun firebaseAutentication( email : String, password : String ) {
        mAuth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener(
                this@Login
            ) { task ->
                if (task.isSuccessful) {
                    startActivity(intent)
                    var user = mAuth.currentUser
                } else {
                    Toast.makeText(
                        this@Login,
                        "Autenticación errónea",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
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
                    var intent = Intent( this@Login, MainActivity::class.java )
                    startActivity(intent)
                    var userSignin : User = User(userEmailAuthenticated!!, date, false, 300, "")
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

    override fun onStart() {
        super.onStart()
        var currentUser: FirebaseUser? = mAuth.currentUser

        currentUser?.let {
            startActivity(intent)
            SharedPreferencesManager.setSomeStringValue("user", mAuth.currentUser!!.email)
        }
    }
}