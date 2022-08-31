package com.example.myshop.ui.activites

import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.view.View
import android.view.WindowManager
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.myshop.R
import com.example.myshop.firebase.FirestoreClass
import com.example.myshop.models.CartItem
import com.example.myshop.models.Product
import com.example.myshop.ui.adapters.CartItemsAdapter
import com.example.myshop.utils.Constants
import kotlinx.android.synthetic.main.activity_cart_list.*
import kotlinx.android.synthetic.main.activity_product_details.*
import kotlinx.android.synthetic.main.fragment_dashboard.*

@Suppress("DEPRECATION")
class CartListActivity : BaseActivity(), View.OnClickListener {

    private lateinit var mProductList:ArrayList<Product>
    private lateinit var mCartListItems: ArrayList<CartItem>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_cart_list)

        setActionBar()
        btn_checkout.setOnClickListener(this)
    }

    private fun setActionBar() //To make the action bar invisible and put back button
    {
        setSupportActionBar(toolbar_cart_list_activity)

        val actionBar = supportActionBar
        if (actionBar != null)
        {
            actionBar.setDisplayHomeAsUpEnabled(true)
            actionBar.setHomeAsUpIndicator(R.drawable.ic_white_color_back_24dp)
        }

        toolbar_cart_list_activity.setNavigationOnClickListener{(onBackPressed())}


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

    override fun onResume() {
        super.onResume()
        //getCartItems()
        getProductList()
    }

    private fun getCartItems()
    {
        //hideProgressDialog()
        //showProgressDialog(resources.getString(R.string.please_wait))
        FirestoreClass().getCartList(this)
    }

    fun successCartItems(cartList:ArrayList<CartItem>)
    {
        hideProgressDialog()
        for (product in mProductList) {
            for (cart in cartList) {
                if (cart.product_id == product.product_id) {

                    cart.stock_quantity = product.stock_quantity


                    if (cart.cart_quantity.toInt() > product.stock_quantity.toInt())
                    {
                        cart.cart_quantity = product.stock_quantity
                    }

                    if (product.stock_quantity.toInt() == 0) {
                        cart.cart_quantity = product.stock_quantity
                    }

                }
            }
        }

        for (cart in cartList)
        {
            if (cart.stock_quantity == "0")
            {
                FirestoreClass().removeItemFromCart(this, cart.id)
            }
        }


        mCartListItems = cartList


        if (mCartListItems.size > 0)
        {
            rv_cart_items_list.visibility = View.VISIBLE
            ll_checkout.visibility = View.GONE
            tv_no_cart_item_found.visibility = View.GONE

            rv_cart_items_list.layoutManager = LinearLayoutManager(this)
            rv_cart_items_list.setHasFixedSize(true)

            val cartListAdapter = CartItemsAdapter(this, mCartListItems, true)
            rv_cart_items_list.adapter = cartListAdapter

            var subTotal:Double =0.0

            for (item in mCartListItems)
            {
                val availableQuantity = item.stock_quantity.toInt()

                if (availableQuantity > 0) {
                    val price = item.price.toDouble()
                    val quantity = item.cart_quantity.toInt()
                    subTotal += (price * quantity)
                }
            }

            tv_sub_total.text = "$$subTotal"
            tv_shipping_charge.text = "$10.0" //Todo --> LOGIC
            if (subTotal > 0)
            {
                ll_checkout.visibility = View.VISIBLE

                var total = subTotal + 10
                tv_total_amount.text = "$$total"
            }
            else
            {
                ll_checkout.visibility = View.GONE
            }
        }
        else
        {
            rv_cart_items_list.visibility = View.GONE
            ll_checkout.visibility = View.GONE
            tv_no_cart_item_found.visibility = View.VISIBLE
        }
    }

    private fun getProductList(){
        showProgressDialog(resources.getString(R.string.please_wait))
        FirestoreClass().getAllProductList(this@CartListActivity)
    }

    private fun getProductList2(){
        FirestoreClass().getAllProductList(this@CartListActivity)
    }

    fun successProductListFromFireStore(productList:ArrayList<Product>)
    {
        mProductList = productList
        getCartItems()
    }

    fun itemRemovedSuccessfully()
    {
        //hideProgressDialog()
        getCartItems()
    }

    fun itemUpdateSuccess()
    {
        //hideProgressDialog()
        getProductList2()
    }

    override fun onClick(v: View?) {
        if (v != null)
        {
            when(v.id)
            {
                R.id.btn_checkout ->
                {
                    val intent = Intent(this@CartListActivity, AddressListActivity::class.java)
                    intent.putExtra(Constants.EXTRA_SELECT_ADDRESS, true)
                    startActivity(intent)
                }
            }
        }
    }
}