package com.gks.sociallogin

import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.util.Base64
import android.util.Log
import android.view.View
import android.widget.Toast
import com.facebook.CallbackManager
import com.facebook.FacebookCallback
import com.facebook.FacebookException
import com.facebook.GraphRequest
import com.facebook.login.LoginResult
import com.facebook.login.widget.LoginButton
import org.json.JSONObject
import java.security.MessageDigest
import java.security.NoSuchAlgorithmException

class ActivityFacebook : AppCompatActivity() {

    lateinit var facebookSignInButton: LoginButton
    var callbackManager: CallbackManager? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_facebook)

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

    }

    public override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        callbackManager!!.onActivityResult(requestCode, resultCode, data)
    }

    private fun handleSignInResultFacebook(objects: JSONObject) {

        val name = objects.optString("name")
        val email = objects.optString("email")

        Toast.makeText(this, "Welcome $name ", Toast.LENGTH_LONG).show()

        Log.e("Arun", "$name $email")

    }
}
