package com.gocation.gocation_android.login

import android.content.Intent
import android.content.SharedPreferences
import android.media.MediaPlayer.OnPreparedListener
import android.net.Uri
import android.os.Bundle
import android.preference.PreferenceManager
import android.support.v7.app.AppCompatActivity
import android.widget.VideoView
import com.facebook.*
import com.facebook.login.LoginResult
import com.gocation.gocation_android.*
import com.gocation.gocation_android.R
import com.gocation.gocation_android.data.User
import com.gocation.gocation_android.main.MainActivity
import com.google.firebase.auth.FacebookAuthProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import kotlinx.android.synthetic.main.activity_login.*
import org.json.JSONException


/**
 * Created by dylanlange on 11/05/17.
 */

class LoginActivity: AppCompatActivity() {

    lateinit var mCallbackManager: CallbackManager
    lateinit var mAuth: FirebaseAuth


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        //videobackground login screeen
        //TODO: change background video to something smalller
        val videoview = findViewById(R.id.loginVid) as VideoView
        val uri = Uri.parse("android.resource://" + packageName + "/" + R.raw.login_bgvid)
        videoview.setVideoURI(uri)
        videoview.start()

        videoview.setOnPreparedListener(OnPreparedListener { mp -> mp.isLooping = true })

        mAuth = FirebaseAuth.getInstance()

        var prefs: SharedPreferences = PreferenceManager.getDefaultSharedPreferences(this)
        if(prefs.getString(ID_PREFS_KEY, "").equals("")){
            //id hasnt been saved to preferences. safe to say that the user isn't logged in.
        } else {
            //logged in already so just go to main activity
            goToMainActivity()
        }

        mCallbackManager = CallbackManager.Factory.create()
        btn_login.setReadPermissions("public_profile", "email")
        btn_login.registerCallback(mCallbackManager, object: FacebookCallback<LoginResult> {

            override fun onSuccess(result: LoginResult?) {
                if(result == null) return
                handleFacebookAccessToken(result.accessToken)
                val request = GraphRequest.newMeRequest(result.accessToken)
                { returnedObject, hi ->
                    try {
                        val id = returnedObject.getString("id")
                        val name = returnedObject.getString("name")
                        val email = returnedObject.getString("email")
                        val gender = returnedObject.getString("gender")
                        val ageRange = returnedObject.getString("age_range")
                        val imageUrl = returnedObject.getJSONObject("picture").getJSONObject("data").getString("url")

                        signInFacebookUser(id, name, email, gender, ageRange, imageUrl)
                    } catch (e: JSONException) {
                        e.printStackTrace()
                    }
                }
                val parameters = Bundle()
                parameters.putString("fields", "id,name,email,gender,age_range,picture.type(large)")
                request.parameters = parameters
                request.executeAsync()
            }

            override fun onCancel() {

            }

            override fun onError(error: FacebookException?) {

            }

        })
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent) {
        super.onActivityResult(requestCode, resultCode, data)
        mCallbackManager.onActivityResult(requestCode, resultCode, data)
    }

    private fun signInFacebookUser(id: String, name: String, email: String, gender: String, ageRange: String, imageUrl: String) {
        signInToFirebase(id, name, email, gender, ageRange, imageUrl)
        var prefs: SharedPreferences = PreferenceManager.getDefaultSharedPreferences(this)
        var editor: SharedPreferences.Editor = prefs.edit()

        editor.putString(ID_PREFS_KEY, id)
        editor.putString(NAME_PREFS_KEY, name)
        editor.putString(EMAIL_PREFS_KEY, email)
        editor.putString(GENDER_PREFS_KEY, gender)
        editor.putString(AGE_RANGE_PREFS_KEY, ageRange)
        editor.putString(IMAGE_URL_PREFS_KEY, imageUrl)

        editor.apply()

        goToMainActivity()
    }

    private fun signInToFirebase(id: String, name: String, email: String, gender: String, ageRange: String, imageUrl: String){
        FirebaseDatabase.getInstance().getReference("users").child(id).setValue(
                User(id, name, email.toFirebaseKey(), gender, ageRange, imageUrl, "Unknown")
        )
    }

    private fun goToMainActivity(){
        var i: Intent = Intent(this, MainActivity::class.java)
        startActivity(i)
        finish()
    }

    private fun handleFacebookAccessToken(token: AccessToken) {
        val credential = FacebookAuthProvider.getCredential(token.token)
        mAuth.signInWithCredential(credential)
    }

}

