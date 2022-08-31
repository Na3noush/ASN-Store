package com.example.myshop.ui.activites

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.WindowManager
import android.widget.Toast
import com.example.myshop.R
import com.example.myshop.firebase.FirestoreClass
import com.example.myshop.models.User
import com.example.myshop.utils.Constants
import com.google.firebase.auth.FirebaseAuth
import kotlinx.android.synthetic.main.activity_login.*
import android.os.Build
import android.view.View.OnSystemUiVisibilityChangeListener


@Suppress("DEPRECATION")
class LoginActivity : com.example.myshop.ui.activites.BaseActivity(), View.OnClickListener{
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        val email = getSharedPreferences(
            Constants.MYSHOPPAL_PREFERENCES, Context.MODE_PRIVATE)
            .getString(Constants.LOGGED_IN_EMAIL, "")
        
        txtEmail.setText(email)
        
        
        val pass = getSharedPreferences(
            Constants.MYSHOPPAL_PREFERENCES, Context.MODE_PRIVATE)
            .getString(Constants.LOGGED_IN_PASS, "")
        
        txtPassword.setText(pass)


        setUpNavigationAndStatusBar()
        btnSubmit.setOnClickListener(this)
        tv_forget_password.setOnClickListener(this)
        txtNewUserRegNow.setOnClickListener(this)
    }

    override fun onClick(view: View?) {
        if (view !=null)
        {
            when(view.id)
            {
                R.id.tv_forget_password -> {
                    val intent = Intent(this, ForgotPasswordActivity::class.java)
                    startActivity(intent)
                }

                R.id.btnSubmit -> {
                    //Start
                    logInRegisteredUser()
                    //End
                }

                R.id.txtNewUserRegNow -> {
                    //Launch the Register screen when user click on this text
                    val intent = Intent(this, RegisterActivity::class.java)
                    startActivity(intent)
                }
            }
        }
    }

    private fun setUpNavigationAndStatusBar()
    {
        window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_FULLSCREEN
    }

    private fun logInRegisteredUser() // Log in Method
    {
        //Get the text from editText and trim the space
        val email = txtEmail.text.toString().trim {it <= ' '}
        val  password = txtPassword.text.toString().trim {it <= ' '}

        //Check empty fields
        if (email.trim().isNotEmpty() && password.trim().isNotEmpty())
        {
            showProgressDialog(resources.getString(R.string.please_wait))

            FirebaseAuth.getInstance().signInWithEmailAndPassword(email, password)
                .addOnCompleteListener()
                {   task ->

                    // if everything is ok -->
                    if (task.isSuccessful)
                    {
                        val sharedPref = getSharedPreferences(Constants.MYSHOPPAL_PREFERENCES, Context.MODE_PRIVATE)
                        val editor:SharedPreferences.Editor = sharedPref.edit()

                        editor.putString(Constants.LOGGED_IN_EMAIL, email)
                        editor.putString(Constants.LOGGED_IN_PASS, password)
                        editor.apply()
                        
                        FirestoreClass().getUserDetails(this@LoginActivity)
                        Toast.makeText(this, "You are logged in successfully.", Toast.LENGTH_SHORT).show()
                    }
                    //if There is something wrong show me what is the problem
                    else
                    {
                        hideProgressDialog()
                        showErrorSnackBar(task.exception!!.message!!, true)
                    }
                }
        }
        // if there was any empty fields -->
        else
        {
            showErrorSnackBar(resources.getString(R.string.err_msg_enter_all_fields), true)
        }
    }

    fun userLoginInSuccess(user : User)
    {
        hideProgressDialog()

        Log.i("User Name ", user.uname)
        Log.i("User Email ", user.email)
        
        
        if(user.profileCompleted == 0)
        {
            val intent = Intent(this@LoginActivity, UserProfileActivity::class.java)
            intent.putExtra(Constants.EXTRA_USER_DETAILS, user)
            startActivity(intent)
        }
        else
        {
            startActivity(Intent(this@LoginActivity, DashboardActivity::class.java))
        }
        finish()
    }
}