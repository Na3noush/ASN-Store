package com.example.myshop.ui.activites

import android.Manifest
import android.app.Activity
import android.content.Context
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
import com.example.myshop.models.Product
import com.example.myshop.utils.Constants
import com.example.myshop.utils.GlideLoader
import kotlinx.android.synthetic.main.activity_add_product.*
import java.io.IOException

class AddProductActivity : BaseActivity(), View.OnClickListener {

    private var mSelectedImgUri: Uri? = null
    private var mProductImageURL: String = ""
    
    @Suppress("DEPRECATION")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_product)
        
        window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_FULLSCREEN
        
        setActionBar()
        iv_add_update_product.setOnClickListener(this)
        btn_submit_add_product.setOnClickListener(this)
    }

    private fun setActionBar() //To make the action bar invisible and put back button
    {
        setSupportActionBar(toolbar_add_product_activity)

        val actionBar = supportActionBar
        if (actionBar != null)
        {
            actionBar.setDisplayHomeAsUpEnabled(true)
            actionBar.setHomeAsUpIndicator(R.drawable.ic_white_color_back_24dp)
        }

        toolbar_add_product_activity.setNavigationOnClickListener{(onBackPressed())}
    }

    override fun onClick(v: View?) {
        if (v != null)
        {
            when(v.id)
            {
                R.id.iv_add_update_product ->
                {
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

                R.id.btn_submit_add_product ->
                {
                    showProgressDialog(resources.getString(R.string.please_wait))
                    if (validateUserProfileDetails())
                    {
                        uploadProductImage()
                    }
                }
            }
        }
    }

    private fun uploadProductImage()
    {
        if (mSelectedImgUri != null)
        {
            FirestoreClass().uploadImageToCloudStorage(this, mSelectedImgUri, Constants.PRODUCT_IMAGE)
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
                   iv_add_update_product.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.ic_vector_edit))

                    mSelectedImgUri = data.data!!
                    try
                    {
                        GlideLoader(this).loadUserPicture(mSelectedImgUri!!, iv_product_image)
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

    private fun validateUserProfileDetails(): Boolean
    {
        return when
        {
            mSelectedImgUri == null ->
            {
                hideProgressDialog()
                showErrorSnackBar(resources.getString(R.string.err_msg_select_product_image), true)
                false
            }

            TextUtils.isEmpty(et_product_title.text.toString().trim {it <= ' '}) ->
            {
                hideProgressDialog()
                showErrorSnackBar(resources.getString(R.string.err_msg_enter_product_title), true)
                false
            }

            TextUtils.isEmpty(et_product_price.text.toString().trim {it <= ' '}) ->
            {
                hideProgressDialog()
                showErrorSnackBar(resources.getString(R.string.err_msg_enter_product_price), true)
                false
            }

            TextUtils.isEmpty(et_product_description.text.toString().trim {it <= ' '}) ->
            {
                hideProgressDialog()
                showErrorSnackBar(resources.getString(R.string.err_msg_enter_product_description), true)
                false
            }

            TextUtils.isEmpty(et_product_quantity.text.toString().trim {it <= ' '}) ->
            {
                hideProgressDialog()
                showErrorSnackBar(resources.getString(R.string.err_msg_enter_product_quantity), true)
                false
            }

            else ->
            {
                true
            }
        }

    }

    fun imageUploadSuccess(imageUrl: String)// This Method to put image url in product details
    {
        mProductImageURL = imageUrl
        uploadProductDetails()
    }

    private fun uploadProductDetails()
    {
        val username = this.getSharedPreferences(
            Constants.MYSHOPPAL_PREFERENCES, Context.MODE_PRIVATE)
            .getString(Constants.LOGGED_IN_USERNAME,"")

        val product = Product(
            FirestoreClass().getCurrentUserID(),
            username,
            mProductImageURL,
            et_product_title.text.toString().trim { it <= ' ' },
            et_product_price.text.toString().trim { it <= ' ' },
            et_product_description.text.toString().trim { it <= ' ' },
            et_product_quantity.text.toString().trim { it <= ' ' },)

        FirestoreClass().uploadProductDetails(this, product)
    }

    fun productUploadedSuccessfully()
    {
        hideProgressDialog()
        Toast.makeText(this,
            resources.getString(R.string.product_uploaded_success_message),
            Toast.LENGTH_LONG)
            .show()
        finish()
    }
}