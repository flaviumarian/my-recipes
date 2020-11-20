package com.flavium.myrecipes

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdSize
import com.google.android.gms.ads.AdView
import com.google.android.gms.ads.MobileAds
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.ktx.Firebase

class MainActivity : AppCompatActivity() {

    var emailEditText: EditText? = null
    var passwordEditText: EditText? = null
    var logInButton: Button? = null
    private var mAuth : FirebaseAuth = Firebase.auth
    lateinit var mAdView : AdView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Ads
        MobileAds.initialize(this) {}
        mAdView = findViewById(R.id.adView)
        val adRequest = AdRequest.Builder().build()
        mAdView.loadAd(adRequest)


        emailEditText = findViewById(R.id.emailEditText)
        passwordEditText = findViewById(R.id.passwordEditText)
        logInButton = findViewById(R.id.logInButton)

        if(mAuth.currentUser != null){
            logIn()
        }

        logInButton?.setOnClickListener {

            if(emailEditText?.text.isNullOrBlank() || passwordEditText?.text!!.isNullOrBlank()){
                Toast.makeText(this, "Insert an email and a password", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // Check if we can log in the user
            mAuth.signInWithEmailAndPassword(emailEditText?.text.toString(), passwordEditText?.text.toString())
                .addOnCompleteListener(this) { task ->
                    if (task.isSuccessful) {
                        // Sign in success, update UI with the signed-in user's information
                        logIn()
                        emailEditText?.text?.clear()
                        passwordEditText?.text?.clear()
                    } else {
                        // Sign up the user
                        mAuth.createUserWithEmailAndPassword(emailEditText?.text.toString(), passwordEditText?.text.toString()).addOnCompleteListener(this){ task ->
                            if(task.isSuccessful){
                                FirebaseDatabase.getInstance().getReference().child("users").child(task.result!!.user!!.uid).child("email").setValue(emailEditText?.text.toString())
                                logIn()
                                emailEditText?.text?.clear()
                                passwordEditText?.text?.clear()
                            }else{
                                Toast.makeText(this, "Login Failed. Try again with correct data!", Toast.LENGTH_SHORT).show()
                            }
                        }
                    }
                }
        }

    }


    private fun logIn(){
        var intent = Intent(this, CategoriesActivity::class.java)
        startActivity(intent)
    }


}
