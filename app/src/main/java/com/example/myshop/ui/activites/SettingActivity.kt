package com.example.myshop.ui.activites

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.WindowManager
import com.example.myshop.R
import com.example.myshop.firebase.FirestoreClass
import com.example.myshop.models.User
import com.example.myshop.utils.Constants
import com.example.myshop.utils.GlideLoader
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.ktx.Firebase
import kotlinx.android.synthetic.main.activity_setting.*

@Suppress("DEPRECATION")
class SettingActivity : BaseActivity(), View.OnClickListener {

    private lateinit var mUserDetails:User

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_setting)

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


        setActionBar()
        getUserDetails()
        btn_logout.setOnClickListener(this)
        tv_edit.setOnClickListener(this)
        ll_address.setOnClickListener(this)
    }

    private fun setActionBar() //To make the action bar invisible and put back button
    {
        setSupportActionBar(toolbar_settings_activity)

        val actionBar = supportActionBar
        if (actionBar != null)
        {
            actionBar.setDisplayHomeAsUpEnabled(true)
            actionBar.setHomeAsUpIndicator(R.drawable.ic_white_color_back_24dp)
        }

        toolbar_settings_activity.setNavigationOnClickListener{(onBackPressed())}
    }

    private fun getUserDetails()
    {
        showProgressDialog(resources.getString(R.string.please_wait))
        FirestoreClass().getUserDetails(this)
    }

    fun userDetailsSuccess(user : User)
    {
        mUserDetails = user

        hideProgressDialog()
        GlideLoader(this@SettingActivity).loadUserPicture(user.image, iv_user_photo)
        tv_name.text = user.uname
        tv_gender.text = user.gender
        tv_email.text = user.email
        tv_mobile_number.text = user.mobile.toString()
    }

    override fun onResume() {
        super.onResume()
        //getUserDetails()
    }

    override fun onClick(v: View?) {
        if (v != null)
        {
            when(v.id)
            {
                R.id.btn_logout ->
                {
                    FirebaseAuth.getInstance().signOut()

                    val sharedPref = getSharedPreferences(Constants.MYSHOPPAL_PREFERENCES, Context.MODE_PRIVATE)
                    val editor: SharedPreferences.Editor = sharedPref.edit()
                    editor.remove(Constants.LOGGED_IN_EMAIL)
                    editor.remove(Constants.LOGGED_IN_PASS)
                    editor.apply()

                    val intent = Intent(this, LoginActivity::class.java)
                    intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                    startActivity(intent)



                    finish()
                }

                R.id.tv_edit ->
                {
                    val intent = Intent(this, UserProfileActivity::class.java)
                    intent.putExtra(Constants.EXTRA_USER_DETAILS, mUserDetails)
                    startActivity(intent)
                }

                R.id.ll_address ->
                {
                    val intent = Intent(this, AddressListActivity::class.java)
                    startActivity(intent)
                }
            }
        }
    }
}