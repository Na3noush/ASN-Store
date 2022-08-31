package com.example.myshop.ui.activites

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.text.TextUtils
import android.view.View
import android.view.WindowManager
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.myshop.R
import com.example.myshop.firebase.FirestoreClass
import com.example.myshop.utils.Constants
import com.example.myshop.utils.GlideLoader
import kotlinx.android.synthetic.main.activity_setting.*
import kotlinx.android.synthetic.main.activity_user_profile.*
import kotlinx.android.synthetic.main.activity_user_profile.iv_user_photo
import java.io.IOException

@Suppress("DEPRECATION")
class UserProfileActivity : BaseActivity(), View.OnClickListener {

    private lateinit var mUserDetails: com.example.myshop.models.User
    private var mSelectedImageFileUri : Uri? = null
    private var mUserProfileImageURL :String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_user_profile)

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


        if (intent.hasExtra(Constants.EXTRA_USER_DETAILS))
        {
            mUserDetails = intent.getParcelableExtra(Constants.EXTRA_USER_DETAILS)!!
        }

        if (mUserDetails.profileCompleted == 0)
        {
            tv_title1.text = resources.getString(R.string.title_complete_profile)
            et_first_name.setText(mUserDetails.uname)
            et_first_name.isEnabled = false
            et_email.setText(mUserDetails.email)
            et_email.isEnabled = false
        }
        else
        {
            tv_title1.text = resources.getString(R.string.title_update_profile)
            et_first_name.setText(mUserDetails.uname)
            et_first_name.isEnabled = true
            et_email.setText(mUserDetails.email)
            et_email.isEnabled = false
            if (mUserDetails.mobile != 0L)
            {
                et_mobile_number.setText(mUserDetails.mobile.toString())
            }
            if (mUserDetails.gender == Constants.MALE)
            {
                rb_male.isChecked = true
            }
            else
            {
                rb_female.isChecked = true
            }
            GlideLoader(this@UserProfileActivity).loadUserPicture(mUserDetails.image, iv_user_photo)
        }


        iv_user_photo.setOnClickListener(this@UserProfileActivity)
        btn_submit.setOnClickListener(this)
    }

    override fun onClick(v: View?) {
        if (v != null)
        {
            when(v.id)
            {
                R.id.iv_user_photo -> {
                    if (ContextCompat.checkSelfPermission(this,
                            Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED)
                    {
                        Constants.showImageChooser(this)
                    }
                    else
                    {
                        ActivityCompat.requestPermissions(this,
                            arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE),
                            Constants.READ_STORAGE_PERMISSION_CODE)
                    }
                }

                R.id.btn_submit -> {

                    showProgressDialog(resources.getString(R.string.please_wait))
                    if (validateUserProfileDetails())
                    {
                        if (mSelectedImageFileUri != null)
                        {
                            FirestoreClass().uploadImageToCloudStorage(this, mSelectedImageFileUri, Constants.USER_PROFILE_IMAGE)
                        }
                        else
                        {
                            updateUserProfileDetails()
                        }
                    }
                }
            }
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED)
        {
            Constants.showImageChooser(this)
        }
        else
        {
            Toast.makeText(this, R.string.read_storage_permission_denied, Toast.LENGTH_LONG).show()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (resultCode == Activity.RESULT_OK)
        {
            if (requestCode == Constants.PICK_IMAGE_REQUEST_CODE)
            {
                if (data != null)
                {
                    try
                    {
                        mSelectedImageFileUri = data.data!!
                        /*iv_user_photo.setImageURI(Uri.parse(selectImageFileUri.toString()))*/
                        GlideLoader(this).loadUserPicture(mSelectedImageFileUri!!, iv_user_photo)
                    }
                    catch (e : IOException)
                    {
                        e.printStackTrace()
                        Toast.makeText(this, R.string.image_selection_failed, Toast.LENGTH_LONG).show()
                    }
                }
            }
        }
    }

    private fun updateUserProfileDetails()
    {
        val userHashMap = HashMap<String, Any>()
        val profileCompleted = 1

        val firstName = et_first_name.text.toString().trim{it <= ' '}
        if (firstName != mUserDetails.uname)
        {
            userHashMap[Constants.NAME] = firstName
        }

        val mobileNumber = et_mobile_number.text.toString().trim{it <= ' '}
        if (mobileNumber.isNotEmpty())
        {
            userHashMap[Constants.MOBILE] = mobileNumber.toLong()
        }

        val gender = if (rb_male.isChecked)
        {
            Constants.MALE
        }
        else
        {
            Constants.FEMALE
        }
        showProgressDialog(resources.getString(R.string.please_wait))


        if (mUserProfileImageURL.isNotEmpty())
        {
            userHashMap[Constants.IMAGE] = mUserProfileImageURL
        }
        userHashMap[Constants.PROFILE_COMPLETED] = profileCompleted
        userHashMap[Constants.GENDER] = gender


        FirestoreClass().updateUserProfileData(this ,userHashMap)
        profileUpdatedSuccessfully()
        //LoginActivity().userProfileCompleted(mUserDetails)
    }

    private fun validateUserProfileDetails(): Boolean
    {
        return when
        {
            TextUtils.isEmpty(et_first_name.text.toString().trim {it <= ' '}) ->
            {
                hideProgressDialog()
                showErrorSnackBar(resources.getString(R.string.err_msg_enter_first_name), true)
                false
            }

            TextUtils.isEmpty(et_mobile_number.text.toString().trim {it <= ' '}) ->
            {
                hideProgressDialog()
                showErrorSnackBar(resources.getString(R.string.err_msg_enter_mobile_number), true)
                false
            }

            else ->
            {
                true
            }
        }

    }

    private fun profileUpdatedSuccessfully() //
    {
        Toast.makeText(this, resources.getString(R.string.msg_profile_update_success), Toast.LENGTH_LONG).show()
        val intent = Intent(this@UserProfileActivity, DashboardActivity::class.java)
        startActivity(intent)
        finish()
    }

    fun imageUploadSuccess(imageUrl: String) { // This Method to put image url in user details
        hideProgressDialog()
        mUserProfileImageURL = imageUrl
        updateUserProfileDetails()
    }
}