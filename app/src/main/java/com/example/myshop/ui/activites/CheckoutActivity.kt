package com.example.myshop.ui.activites

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.view.View
import android.view.WindowManager
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.myshop.R
import com.example.myshop.firebase.FirestoreClass
import com.example.myshop.models.Address
import com.example.myshop.models.CartItem
import com.example.myshop.models.Order
import com.example.myshop.models.Product
import com.example.myshop.ui.adapters.CartItemsAdapter
import com.example.myshop.utils.Constants
import kotlinx.android.synthetic.main.activity_checkout.*

@Suppress("DEPRECATION")
class CheckoutActivity : BaseActivity(),View.OnClickListener {

    private var mAddressDetails: Address? = null
    private lateinit var mProductList : ArrayList<Product>
    private lateinit var mCartItemsList : ArrayList<CartItem>
    private var mSubTotal : Double = 0.0
    private var mTotalAmount : Double = 0.0
    private lateinit var mOrderDetails: Order

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (Build.VERSION.SDK_INT < 16) {
            window.setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN)
        }

        setContentView(R.layout.activity_checkout)


        setActionBar()
        showSelectedAddress()
        getAllProducts()

        btn_place_order.setOnClickListener(this)
    }

    private fun showSelectedAddress()
    {
        if (intent.hasExtra(Constants.EXTRA_SELECTED_ADDRESS))
        {
            mAddressDetails = intent.getParcelableExtra<Address>(Constants.EXTRA_SELECTED_ADDRESS)
        }
        if (mAddressDetails != null)
        {
            tv_checkout_address_type.text = mAddressDetails!!.type
            tv_checkout_full_name.text = mAddressDetails!!.name
            tv_checkout_address.text = "${mAddressDetails!!.address}, ${mAddressDetails!!.zipCode}"
            tv_checkout_additional_note.text = mAddressDetails!!.additionalNote
            if (mAddressDetails!!.otherDetails.isNotEmpty())
            {
                tv_checkout_other_details.text = mAddressDetails!!.otherDetails

            }
            tv_checkout_mobile_number.text = mAddressDetails!!.mobileNumber
        }
    }

    private fun getAllProducts()
    {
        showProgressDialog(resources.getString(R.string.please_wait))
        FirestoreClass().getAllProductList(this)
    }

    fun successGetAllProducts(productList:ArrayList<Product>)
    {
        mProductList = productList
        getCartItemsList()
    }

    private fun getCartItemsList()
    {
        FirestoreClass().getCartList(this)
    }

    fun successGetCartItemsList(cartItemsList:ArrayList<CartItem>)
    {
        hideProgressDialog()
        for (product in mProductList)
        {
            for (cartItem in cartItemsList)
            {
                if (product.product_id == cartItem.product_id)
                {
                    cartItem.stock_quantity = product.stock_quantity
                }
            }
        }
        mCartItemsList = cartItemsList
        rv_cart_list_items.layoutManager = LinearLayoutManager(this@CheckoutActivity)
        rv_cart_list_items.setHasFixedSize(true)

        val cartListAdapter = CartItemsAdapter(this, mCartItemsList, false)
        rv_cart_list_items.adapter = cartListAdapter

        for (item in mCartItemsList)
        {
            val availableQuantity = item.stock_quantity.toInt()
            if (availableQuantity > 0)
            {
                val price = item.price.toInt()
                val quantity = item.cart_quantity.toInt()

                mSubTotal += (price * quantity)
            }
        }

        tv_checkout_sub_total.text = "$${mSubTotal}"
        tv_checkout_shipping_charge.text = "10$" //TODO --> You Can Change The Logic

        if (mSubTotal > 0)
        {
            ll_checkout_place_order.visibility = View.VISIBLE
            mTotalAmount = mSubTotal + 10

            tv_checkout_total_amount.text = "$${mTotalAmount}"
        }
        else
        {
            ll_checkout_place_order.visibility = View.GONE
        }
    }

    private fun placeOrder()
    {
        showProgressDialog(resources.getString(R.string.please_wait))
        if (mAddressDetails != null)
        {
             mOrderDetails = Order(
                FirestoreClass().getCurrentUserID(),
                mCartItemsList,
                mAddressDetails!!,
                "My Order ${System.currentTimeMillis()}",
                mCartItemsList[0].image, // [0] means -> The First Item That We Have
                mSubTotal.toString(),
                "10",
                mTotalAmount.toString(),
                System.currentTimeMillis()
            )

            FirestoreClass().uploadOrdersDetails(this, mOrderDetails)
        }
    }

    fun successPlacedOrder()
    {
        FirestoreClass().updateAllDetails(this, mCartItemsList, mOrderDetails)
    }

    fun allDetailsUpdatedSuccess()
    {
        hideProgressDialog()
        Toast.makeText(this, "Order placed successfully", Toast.LENGTH_LONG).show()
        val intent = Intent(this@CheckoutActivity, DashboardActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }

    private fun setActionBar() //To make the action bar invisible, put back button, hide the navigation bar
    {
        setSupportActionBar(toolbar_checkout_activity)

        val actionBar = supportActionBar
        if (actionBar != null)
        {
            actionBar.setDisplayHomeAsUpEnabled(true)
            actionBar.setHomeAsUpIndicator(R.drawable.ic_white_color_back_24dp)
        }

        toolbar_checkout_activity.setNavigationOnClickListener{(onBackPressed())}

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

    override fun onClick(v: View?) {
        super.onClick(v)
        if (v != null)
        {
            when(v.id)
            {
                R.id.btn_place_order ->
                {
                    placeOrder()
                }
            }
        }
    }
}