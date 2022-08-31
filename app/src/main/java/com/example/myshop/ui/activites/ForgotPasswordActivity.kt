package com.example.myshop.ui.activites

import android.os.Build
import android.os.Bundle
import android.view.View
import android.view.WindowManager
import android.widget.Toast
import com.example.myshop.R
import com.google.firebase.auth.FirebaseAuth
import kotlinx.android.synthetic.main.activity_forgot_password.*

class ForgotPasswordActivity : BaseActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_forgot_password)

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
        btnSubmit.setOnClickListener()
        {
            submitResetPassword()
        }
        txtBackToLogin.setOnClickListener()
        {
            onBackPressed()
        }
    }
    private fun submitResetPassword()
    {
        //Get the text from editText and trim the space
        val email = txtEmail.text.toString().trim {it <= ' '}

        //Check empty fields
        if (email.trim().isNotEmpty())
        {
            showProgressDialog(resources.getString(R.string.please_wait))

            FirebaseAuth.getInstance().sendPasswordResetEmail(email)
                .addOnCompleteListener()
                {task ->

                    hideProgressDialog()
                    // if everything is ok -->
                    if (task.isSuccessful)
                    {
                        //TODO - Send User To Main Activity
                        Toast.makeText(this, "Email sent successfully to reset your password.", Toast.LENGTH_LONG).show()
                        finish()
                    }
                    //if There is something wrong show me what is the problem
                    else
                    {
                        showErrorSnackBar(task.exception!!.message!!, true)
                    }
                }
        }
        // if there was any empty fields -->
        else
        {
            showErrorSnackBar(resources.getString(R.string.err_msg_enter_email), true)
        }
    }
}