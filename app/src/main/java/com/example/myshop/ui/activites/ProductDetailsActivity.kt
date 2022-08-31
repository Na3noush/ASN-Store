package com.example.myshop.ui.activites

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.WindowManager
import android.widget.Toast
import androidx.core.content.ContextCompat
import com.example.myshop.R
import com.example.myshop.firebase.FirestoreClass
import com.example.myshop.models.CartItem
import com.example.myshop.models.Product
import com.example.myshop.utils.Constants
import com.example.myshop.utils.GlideLoader
import kotlinx.android.synthetic.main.activity_add_product.*
import kotlinx.android.synthetic.main.activity_product_details.*

@Suppress("DEPRECATION")
class ProductDetailsActivity : BaseActivity(), View.OnClickListener {

    private var mProductId:String = ""
    private var mProductOwnerId:String = ""
    private lateinit var mProductDetails:Product

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_product_details)
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


        if (intent.hasExtra(Constants.EXTRA_PRODUCT_ID))
        {
            mProductId = intent.getStringExtra(Constants.EXTRA_PRODUCT_ID)!!
            Log.i("Product ID: ", mProductId)
            FirestoreClass().getProductDetails(this, mProductId)
        }
        if (intent.hasExtra(Constants.EXTRA_PRODUCT_OWNER_ID))
        {
            mProductOwnerId = intent.getStringExtra(Constants.EXTRA_PRODUCT_OWNER_ID)!!
        }

        if (FirestoreClass().getCurrentUserID() == mProductOwnerId)
        {
            btn_add_to_cart.visibility = View.GONE
        }
        else
        {
            btn_add_to_cart.visibility = View.VISIBLE
        }


        setActionBar()
        getProductDetails()

        btn_add_to_cart.setOnClickListener(this)
        btn_go_to_cart.setOnClickListener(this)
    }

    private fun setActionBar() //To make the action bar invisible and put back button
    {
        setSupportActionBar(toolbar_product_details_activity)

        val actionBar = supportActionBar
        if (actionBar != null)
        {
            actionBar.setDisplayHomeAsUpEnabled(true)
            actionBar.setHomeAsUpIndicator(R.drawable.ic_white_color_back_24dp)
        }

        toolbar_product_details_activity.setNavigationOnClickListener{(onBackPressed())}
    }

    private fun getProductDetails()
    {
        showProgressDialog(resources.getString(R.string.please_wait))
        FirestoreClass().getProductDetails(this, mProductId)
    }

    fun productDetailsSuccess(product:Product)
    {
        mProductDetails = product
        GlideLoader(this@ProductDetailsActivity).loadProductPicture(product.image, iv_product_detail_image)
        tv_product_details_title.text = product.title
        tv_product_details_description.text = product.description
        tv_product_details_price.text = "$${product.price}"
        tv_product_details_stock_quantity.text = product.stock_quantity

        if (product.stock_quantity.toInt() == 0)
        {
            hideProgressDialog()

            btn_add_to_cart.visibility = View.GONE

            tv_product_details_stock_quantity.text = resources.getString(R.string.lbl_out_of_stock)

            tv_product_details_stock_quantity.setTextColor(ContextCompat.getColor(
                this, R.color.colorSnackBarError))
        }

        if (FirestoreClass().getCurrentUserID() == product.user_id)
        {
            hideProgressDialog()
        }
        else
        {
            FirestoreClass().checkIfItemExistInCart(this, mProductId)
        }
    }

    private fun addToCart()
    {
        val addToCart = CartItem(
            FirestoreClass().getCurrentUserID(),
            mProductOwnerId,
            mProductId,
            mProductDetails.title,
            mProductDetails.price,
            mProductDetails.image,
            Constants.DEFAULT_CART_QUANTITY,
            )
        
        FirestoreClass().addCartItems(this@ProductDetailsActivity, addToCart)
    }
    
    fun addToCartSuccess()
    {
        hideProgressDialog()
        Toast.makeText(this, "The Item Added To The Cart Successfully", Toast.LENGTH_LONG).show()

        btn_add_to_cart.visibility = View.GONE
        btn_go_to_cart.visibility = View.VISIBLE
    }

    fun productExistInCart()
    {
        hideProgressDialog()
        btn_add_to_cart.visibility = View.GONE
        btn_go_to_cart.visibility = View.VISIBLE
    }


    override fun onClick(v: View?) {
        if (v != null)
        {
            when(v.id)
            {
                R.id.btn_add_to_cart ->
                {
                    addToCart()
                }

                R.id.btn_go_to_cart ->
                {
                    val intent = Intent(this@ProductDetailsActivity, CartListActivity::class.java)
                    startActivity(intent)
                }
            }
        }
    }
}