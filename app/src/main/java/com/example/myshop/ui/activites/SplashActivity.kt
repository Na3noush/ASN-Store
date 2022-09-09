package com.example.myshop.ui.activites

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.WindowManager
import android.os.Handler
import android.util.Log
import android.view.View
import android.widget.Toast
import com.example.myshop.R
import com.example.myshop.firebase.FirestoreClass
import com.example.myshop.models.User
import com.example.myshop.utils.Constants
import com.google.firebase.auth.FirebaseAuth
import kotlinx.android.synthetic.main.activity_login.*

@Suppress("DEPRECATION")
class SplashActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)

        cleaningScreen()
        loginCurrentUser()

    }

    private fun cleaningScreen()
    {
        // This is used to hide the status bar and make
        // the splash screen as a full screen activity.
        val currentApiVersion = Build.VERSION.SDK_INT

        val flags = (View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                or View.SYSTEM_UI_FLAG_FULLSCREEN
                or View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY)

        // This work only for android 4.4+

        // This work only for android 4.4+
        if (currentApiVersion >= Build.VERSION_CODES.KITKAT) {
            window.decorView.systemUiVisibility = flags

            // Code below is to handle presses of Volume up or Volume down.
            // Without this, after pressing volume buttons, the navigation bar will
            // show up and won't hide
            val decorView = window.decorView
            decorView
                .setOnSystemUiVisibilityChangeListener { visibility ->
                    if (visibility and View.SYSTEM_UI_FLAG_FULLSCREEN == 0) {
                        decorView.systemUiVisibility = flags
                    }
                }
        }
    }

    private fun loginCurrentUser()
    {

        val email: String = getSharedPreferences(
            Constants.MYSHOPPAL_PREFERENCES, Context.MODE_PRIVATE)
            .getString(Constants.LOGGED_IN_EMAIL, "")!!


        val pass = getSharedPreferences(
            Constants.MYSHOPPAL_PREFERENCES, Context.MODE_PRIVATE)
            .getString(Constants.LOGGED_IN_PASS, "")!!


        if (email.isNotEmpty() && pass.isNotEmpty())
        {
            FirebaseAuth.getInstance().signInWithEmailAndPassword(email, pass)
                .addOnCompleteListener()
                {   task ->

                    // if everything is ok -->
                    if (task.isSuccessful)
                    {
                        FirestoreClass().getUserDetails(this@SplashActivity)
                    }
                    //if There is something wrong show me what is the problem
                    else
                    {
                        val intent = Intent(this, LoginActivity::class.java)
                        startActivity(intent)
                    }
                }
        }
        else
        {
            Handler().postDelayed({
                val intent = Intent(this, LoginActivity::class.java)
                startActivity(intent)
                finish()
            }, 2500) // 3000 is the delayed time in milliseconds.
        }
    }


    fun loginFromSplashSuccess(user : User)
    {
        Log.i("User Name ", user.uname)
        Log.i("User Email ", user.email)


        if(user.profileCompleted == 0)
        {
            val intent = Intent(this@SplashActivity, UserProfileActivity::class.java)
            intent.putExtra(Constants.EXTRA_USER_DETAILS, user)
            startActivity(intent)
        }
        else
        {
            startActivity(Intent(this@SplashActivity, DashboardActivity::class.java))
        }
        finish()
    }
}