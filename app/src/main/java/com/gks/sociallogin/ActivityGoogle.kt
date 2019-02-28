package com.gks.sociallogin

import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.view.View
import android.widget.Toast
import com.google.android.gms.auth.api.Auth
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.auth.api.signin.GoogleSignInResult
import com.google.android.gms.common.SignInButton
import com.google.android.gms.common.api.GoogleApiClient

class ActivityGoogle : AppCompatActivity(), View.OnClickListener {

    lateinit var googleSignInButton: SignInButton

    // Google API Client object.
    var googleApiClient: GoogleApiClient? = null
    val GOOGLE_LOG_IN_RC = 1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_google)

        googleSignInButton = findViewById<View>(R.id.google_sign_in_button) as SignInButton

        googleSignInButton.setOnClickListener(this@ActivityGoogle)

        // Configure Google Sign In
        val googleSignInOptions = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestEmail()
            .build()

        // Creating and Configuring Google Api Client.
        googleApiClient = GoogleApiClient.Builder(this@ActivityGoogle)
            .enableAutoManage(this@ActivityGoogle  /* OnConnectionFailedListener */) { }
            .addApi(Auth.GOOGLE_SIGN_IN_API, googleSignInOptions)
            .build()


    }

    override fun onClick(view: View?) {
        when (view?.id) {
            R.id.google_sign_in_button -> {
                googleLogin()
            }
        }
    }

    private fun googleLogin() {
        val signInIntent = Auth.GoogleSignInApi.getSignInIntent(googleApiClient)
        startActivityForResult(signInIntent, GOOGLE_LOG_IN_RC)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        // Result returned from launching the Intent from GoogleSignInApi.getSignInIntent(...);
        if (requestCode == GOOGLE_LOG_IN_RC) {
            val result = Auth.GoogleSignInApi.getSignInResultFromIntent(data)

            if (result.isSuccess) {

                // Google Sign In was successful, authenticate with Firebase
                val result = Auth.GoogleSignInApi.getSignInResultFromIntent(data)
                handleSignInResult(result)

            } else {
                Toast.makeText(this@ActivityGoogle, "Some error occurred.", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun handleSignInResult(result: GoogleSignInResult) {
        if (result.isSuccess()) {

            val acct = result.signInAccount

            val email = acct?.email
            val name = acct?.displayName

            Log.e("Arun", "$email $name")

            Toast.makeText(this@ActivityGoogle, "Welcome $name", Toast.LENGTH_SHORT).show()

        }
    }

}
