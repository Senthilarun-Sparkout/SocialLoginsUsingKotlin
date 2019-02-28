package com.gks.sociallogin

import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.util.Base64
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import com.facebook.CallbackManager
import com.facebook.FacebookCallback
import com.facebook.FacebookException
import com.facebook.GraphRequest
import com.facebook.login.LoginManager
import com.facebook.login.LoginResult
import com.facebook.login.widget.LoginButton
import com.google.android.gms.auth.api.Auth
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.auth.api.signin.GoogleSignInResult
import com.google.android.gms.common.SignInButton
import com.google.android.gms.common.api.GoogleApiClient
import com.google.android.gms.common.api.ResultCallback
import com.google.android.gms.common.api.Status
import org.json.JSONObject
import java.security.MessageDigest
import java.security.NoSuchAlgorithmException

class MainActivity : AppCompatActivity(), View.OnClickListener {


    //    For Google Integration
    lateinit var googleSignInButton: SignInButton
    // Google API Client object.
    var googleApiClient: GoogleApiClient? = null
    val GOOGLE_LOG_IN_RC = 1


    //    For Facebook Integration
    lateinit var facebookSignInButton: LoginButton
    var callbackManager: CallbackManager? = null
    var mLayoutLogin: LinearLayout? = null
    var mLayoutDetails: LinearLayout? = null

    var mContent: TextView? = null

    lateinit var mBtnSignOut: Button

    var integer: Int? = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)


//        For Google Integration
        googleSignInButton = findViewById<View>(R.id.google_sign_in_button) as SignInButton

        googleSignInButton.setOnClickListener(this@MainActivity)

        // Configure Google Sign In
        val googleSignInOptions = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestEmail()
            .build()

        // Creating and Configuring Google Api Client.
        googleApiClient = GoogleApiClient.Builder(this@MainActivity)
            .enableAutoManage(this@MainActivity  /* OnConnectionFailedListener */) { }
            .addApi(Auth.GOOGLE_SIGN_IN_API, googleSignInOptions)
            .build()


//        For Facebook Integration

        //        To generate KeyHash
        try {
            val info = packageManager.getPackageInfo(
                "com.gks.sociallogin",
                PackageManager.GET_SIGNATURES
            )
            for (signature in info.signatures) {
                val md = MessageDigest.getInstance("SHA")
                md.update(signature.toByteArray())
                Log.e("KeyHash:", Base64.encodeToString(md.digest(), Base64.DEFAULT))
            }
        } catch (e: PackageManager.NameNotFoundException) {

        } catch (e: NoSuchAlgorithmException) {

        }

        facebookSignInButton = findViewById<View>(R.id.facebook_sign_in_button) as LoginButton


        callbackManager = CallbackManager.Factory.create();
        facebookSignInButton.setReadPermissions("email")

        // Callback registration
        facebookSignInButton.registerCallback(callbackManager, object : FacebookCallback<LoginResult> {
            override fun onSuccess(loginResult: LoginResult) {
                // App code
                val request = GraphRequest.newMeRequest(loginResult.accessToken) { `object`, response ->
                    try {
                        //here is the data that you want
                        if (`object`.has("id")) {

                            Log.e("Arun", `object`.toString());

                            handleSignInResultFacebook(`object`)
                        } else {
                            Log.e("Arun", `object`.toString());
                        }

                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }

                val parameters = Bundle()
                parameters.putString("fields", "name,email,id,picture.type(large)")
                request.parameters = parameters
                request.executeAsync()

            }

            override fun onCancel() {
                // App code
            }

            override fun onError(exception: FacebookException) {
                // App code
            }
        })

        mLayoutLogin = findViewById<View>(R.id.mLayoutLogin) as LinearLayout
        mLayoutDetails = findViewById<View>(R.id.mLayoutDetails) as LinearLayout
        mContent = findViewById<View>(R.id.mContent) as TextView

        mBtnSignOut = findViewById<View>(R.id.mBtnSignOut) as Button
        mBtnSignOut.setOnClickListener(this)

    }

    override fun onClick(view: View?) {
        when (view?.id) {
            R.id.google_sign_in_button -> {
                googleLogin()
            }

            R.id.mBtnSignOut -> {
                if (integer == 1) {
                    fbLogOut()
                } else if (integer == 2) {
                    googleLogout()
                }
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
                Toast.makeText(this@MainActivity, "Some error occurred.", Toast.LENGTH_SHORT).show()
            }
        } else {
            callbackManager!!.onActivityResult(requestCode, resultCode, data)
        }
    }

    private fun handleSignInResult(result: GoogleSignInResult) {
        if (result.isSuccess()) {

            val acct = result.signInAccount

            val email = acct?.email
            val name = acct?.displayName

            mLayoutLogin!!.visibility = View.GONE
            mLayoutDetails!!.visibility = View.VISIBLE
            mBtnSignOut!!.visibility = View.VISIBLE

            mContent!!.text = "Successfully Logined $name"

            integer = 2

        }
    }

    private fun handleSignInResultFacebook(objects: JSONObject) {

        val name = objects.optString("name")
        val email = objects.optString("email")

        mLayoutLogin!!.visibility = View.GONE
        mLayoutDetails!!.visibility = View.VISIBLE
        mBtnSignOut!!.visibility = View.VISIBLE

        mContent!!.text = "Successfully Logined $name"

        integer = 1
    }

    override fun onDestroy() {
        super.onDestroy()
    }

    private fun fbLogOut() {

        LoginManager.getInstance().logOut();

        mLayoutLogin!!.visibility = View.VISIBLE
        mLayoutDetails!!.visibility = View.GONE
        mBtnSignOut!!.visibility = View.GONE

    }

    private fun googleLogout() {
        Auth.GoogleSignInApi.signOut(googleApiClient).setResultCallback(
            object : ResultCallback<Status> {
                override fun onResult(status: Status) {
                    mLayoutLogin!!.visibility = View.VISIBLE
                    mLayoutDetails!!.visibility = View.GONE
                    mBtnSignOut!!.visibility = View.GONE
                }
            })
    }

}
