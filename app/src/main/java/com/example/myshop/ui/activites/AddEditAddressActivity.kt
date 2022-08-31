package com.example.myshop.ui.activites

import android.os.Build
import android.os.Bundle
import android.text.TextUtils
import android.view.View
import android.view.WindowManager
import android.widget.Toast
import com.example.myshop.R
import com.example.myshop.firebase.FirestoreClass
import com.example.myshop.models.Address
import com.example.myshop.utils.Constants
import kotlinx.android.synthetic.main.activity_add_edit_address.*

@Suppress("DEPRECATION")
class AddEditAddressActivity : BaseActivity(), View.OnClickListener {

    private var mAddressDetails:Address? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_edit_address)

        window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_FULLSCREEN

        if (intent.hasExtra(Constants.EXTRA_ADDRESS_DETAILS))
        {
            mAddressDetails = intent.getParcelableExtra(Constants.EXTRA_ADDRESS_DETAILS)
            //send data
        }

        if (mAddressDetails != null)
        {
            if (mAddressDetails!!.id.isNotEmpty())
            {
                tv_title.text = resources.getString(R.string.title_edit_address)
                btn_submit_address.text = resources.getString(R.string.btn_lbl_update)

                et_full_name.setText(mAddressDetails?.name)
                et_phone_number.setText(mAddressDetails?.mobileNumber)
                et_address.setText(mAddressDetails?.address)
                et_zip_code.setText(mAddressDetails?.zipCode)
                et_additional_note.setText(mAddressDetails?.additionalNote)


                when(mAddressDetails!!.type)
                {
                    Constants.HOME ->
                    {
                        rb_home.isChecked = true
                    }
                    Constants.OFFICE ->
                    {
                        rb_office.isChecked = true
                    }
                    else ->
                    {
                        rb_other.isChecked = true
                        til_other_details.visibility = View.VISIBLE
                        et_other_details.setText(mAddressDetails!!.otherDetails)
                    }
                }
            }
        }

        setActionBar()
        btn_submit_address.setOnClickListener(this)
        rg_type.setOnCheckedChangeListener() {_, checkedid ->
            if (checkedid == R.id.rb_other)
            {
                til_other_details.visibility = View.VISIBLE
            }
            else
            {
                til_other_details.visibility = View.GONE
            }
        }
    }

    private fun setActionBar() //To make the action bar invisible and put back button
    {
        setSupportActionBar(toolbar_add_edit_address_activity)

        val actionBar = supportActionBar
        if (actionBar != null)
        {
            actionBar.setDisplayHomeAsUpEnabled(true)
            actionBar.setHomeAsUpIndicator(R.drawable.ic_white_color_back_24dp)
        }

        toolbar_add_edit_address_activity.setNavigationOnClickListener{(onBackPressed())}
    }



    private fun validateData() : Boolean{
        return when{
            TextUtils.isEmpty(et_full_name.text.toString().trim{it <= ' '}) ->
            {
                showErrorSnackBar(resources.getString(R.string.err_msg_please_enter_full_name),
                    true)
                false
            }

            TextUtils.isEmpty(et_phone_number.text.toString().trim{it <= ' '}) ->
            {
                showErrorSnackBar(resources.getString(R.string.err_msg_please_enter_phone_number),
                    true)
                false
            }

            TextUtils.isEmpty(et_address.text.toString().trim{it <= ' '}) ->
            {
                showErrorSnackBar(resources.getString(R.string.err_msg_please_enter_address),
                    true)
                false
            }

            TextUtils.isEmpty(et_zip_code.text.toString().trim{it <= ' '}) ->
            {
                showErrorSnackBar(resources.getString(R.string.err_msg_please_enter_zip_code),
                    true)
                false
            }

            rb_other.isChecked && TextUtils.isEmpty(et_other_details.text.toString().trim{it <= ' '}) ->
            {
                showErrorSnackBar(resources.getString(R.string.err_msg_please_enter_other_details),
                    true)
                false
            }
            else ->
            {
                true
            }
        }
    }

    fun addUpdateAddressesSuccess()

    {
        hideProgressDialog()

        val notifySuccessMessage :String = if (mAddressDetails != null && mAddressDetails!!.id.isNotEmpty())
        {
            resources.getString(R.string.msg_your_address_updated_successfully)
        }
        else
        {
            resources.getString(R.string.err_your_address_added_successfully)
        }

        Toast.makeText(this, notifySuccessMessage, Toast.LENGTH_LONG).show()

        setResult(RESULT_OK)
        finish()
    }


    override fun onClick(v: View?) {
        if (v != null)
        {
            when(v.id)
            {
                R.id.btn_submit_address ->
                {
                    saveAddressToFireStore()
                }
            }
        }
    }
    private fun saveAddressToFireStore(){
        val fullName:String = et_full_name.text.toString().trim{it <= ' '}
        val phoneNumber:String = et_phone_number.text.toString().trim{it <= ' '}
        val address:String = et_address.text.toString().trim{it <= ' '}
        val zipCode:String = et_zip_code.text.toString().trim{it <= ' '}
        val additionalNote:String = et_additional_note.text.toString().trim{it <= ' '}
        val otherDetails:String = et_other_details.text.toString().trim{it <= ' '}

        if (validateData())
        {
            showProgressDialog(resources.getString(R.string.please_wait))

            val addressType:String = when
            {
                rb_home.isChecked ->
                {
                    Constants.HOME
                }
                rb_office.isChecked ->
                {
                    Constants.OFFICE
                }
                else ->
                {
                    Constants.OTHER
                }
            }

            val addressModel = Address(
                FirestoreClass().getCurrentUserID(),
                fullName,
                phoneNumber,
                address,
                zipCode,
                additionalNote,
                addressType,
                otherDetails
            )

            if (mAddressDetails != null && mAddressDetails!!.id.isNotEmpty())
            {
                FirestoreClass().updateAddress(this, addressModel, mAddressDetails!!.id)
            }
            else
            {
                FirestoreClass().addAddress(this@AddEditAddressActivity, addressModel)
            }
        }
    }

}