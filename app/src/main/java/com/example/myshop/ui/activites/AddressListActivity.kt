package com.example.myshop.ui.activites

import android.app.Activity
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.view.View
import android.view.WindowManager
import android.widget.Toast
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.myshop.R
import com.example.myshop.firebase.FirestoreClass
import com.example.myshop.models.Address
import com.example.myshop.ui.adapters.AddressesAdapter
import com.example.myshop.utils.Constants
import com.example.myshop.utils.SwipeToDeleteCallback
import com.example.myshop.utils.SwipeToEditCallback
import kotlinx.android.synthetic.main.activity_address_list.*
import kotlinx.android.synthetic.main.activity_cart_list.*

class AddressListActivity : BaseActivity(), View.OnClickListener {

    private var mSelectAddress:Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_address_list)

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

        getAddressesFromFireStore()

        if (intent.hasExtra(Constants.EXTRA_SELECT_ADDRESS))
        {
            mSelectAddress = intent.getBooleanExtra(Constants.EXTRA_SELECT_ADDRESS, false)
        }

        if (mSelectAddress)
        {
            tv_title_address_list.text = resources.getString(R.string.title_select_address)
        }


        setActionBar()
        tv_add_address.setOnClickListener(this)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK)
        {
            getAddressesFromFireStore()
        }
    }

    override fun onResume() {
        super.onResume()
    }

    private fun setActionBar() //To make the action bar invisible and put back button
    {
        setSupportActionBar(toolbar_address_list_activity)

        val actionBar = supportActionBar
        if (actionBar != null)
        {
            actionBar.setDisplayHomeAsUpEnabled(true)
            actionBar.setHomeAsUpIndicator(R.drawable.ic_white_color_back_24dp)
        }

        toolbar_address_list_activity.setNavigationOnClickListener{(onBackPressed())}
    }

    private fun getAddressesFromFireStore()
    {
        showProgressDialog(resources.getString(R.string.please_wait))
        FirestoreClass().getAddressesList(this)
    }

    fun successGetAddresses(addressesList:ArrayList<Address>)
    {
        hideProgressDialog()

        if (addressesList.size > 0)
        {
            tv_no_address_found.visibility = View.GONE
            rv_address_list.visibility = View.VISIBLE

            rv_address_list.layoutManager = LinearLayoutManager(this)
            rv_address_list.setHasFixedSize(true)
            val addressesAdapter = AddressesAdapter(this, addressesList, mSelectAddress)
            rv_address_list.adapter = addressesAdapter

            if (!mSelectAddress)
            {
                //Swipe To Edit -->
                val editSwipeHandler = object : SwipeToEditCallback(this)
                {
                    override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {

                        val adapter = rv_address_list.adapter as AddressesAdapter
                        adapter.notifyEditItem(this@AddressListActivity, viewHolder.adapterPosition)
                    }
                }

                val editItemTouchHelper = ItemTouchHelper(editSwipeHandler)
                editItemTouchHelper.attachToRecyclerView(rv_address_list)

                val deleteSwipeHandler = object : SwipeToDeleteCallback(this)
                {
                    override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                        showProgressDialog(resources.getString(R.string.please_wait))

                        FirestoreClass().deleteAddress(this@AddressListActivity, addressesList[viewHolder.adapterPosition].id)
                    }
                }

                val deleteItemTouchHelper = ItemTouchHelper(deleteSwipeHandler)
                deleteItemTouchHelper.attachToRecyclerView(rv_address_list)
            }
        }

        else
        {
            tv_no_address_found.visibility = View.VISIBLE
            rv_address_list.visibility = View.GONE
        }
    }

    fun addressDeletedSuccess()
    {
        hideProgressDialog()
        Toast.makeText(this, "Address Deleted Successfully", Toast.LENGTH_LONG).show()
        getAddressesFromFireStore()
    }

    override fun onClick(v: View?) {
        if (v != null)
        {
            when(v.id)
            {
                R.id.tv_add_address ->
                {
                    val intent = Intent(this, AddEditAddressActivity::class.java)
                    startActivityForResult(intent, Constants.ADD_ADDRESS_REQUEST_CODE)
                }
            }
        }
    }
}