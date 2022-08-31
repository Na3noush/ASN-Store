package com.example.myshop.ui.activites

import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.myshop.R
import com.example.myshop.models.Order
import com.example.myshop.ui.adapters.CartItemsAdapter
import com.example.myshop.utils.Constants
import kotlinx.android.synthetic.main.activity_my_order_details.*
import kotlinx.android.synthetic.main.activity_product_details.*
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit

@Suppress("DEPRECATION")
class MyOrdersDetailsActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_my_order_details)

        showMyOrdersDetails()
        setActionBar()
    }

    private fun showMyOrdersDetails()
    {
        var myOrdersDetails:Order = Order()
        if (intent.hasExtra(Constants.EXTRA_MY_ORDER_DETAILS))
        {
            myOrdersDetails = intent.getParcelableExtra<Order>(Constants.EXTRA_MY_ORDER_DETAILS)!!
        }
        setupUi(myOrdersDetails)
    }

    private fun setupUi(orderDetails:Order)
    {
        tv_order_details_id.text = orderDetails.title

        val dateFormat = "dd MMM yyyy HH:mm"
        val formatter = SimpleDateFormat(dateFormat, Locale.getDefault())
        val calender:Calendar = Calendar.getInstance()
        calender.timeInMillis = orderDetails.order_datetime
        val orderDateTime = formatter.format(calender.time)
        tv_order_details_date.text = orderDateTime

        val diffInMilliSeconds: Long = System.currentTimeMillis() - orderDetails.order_datetime
        val diffInHours: Long = TimeUnit.MILLISECONDS.toHours(diffInMilliSeconds)
        Log.e("Difference in Hours", "$diffInHours")

        when {
            diffInHours < 1 -> { //TODO ---> LOGIC Delivery Speed
                tv_order_status.text = resources.getString(R.string.order_status_pending)
                tv_order_status.setTextColor(
                    ContextCompat.getColor(
                        this@MyOrdersDetailsActivity,
                        R.color.colorSnackBarError
                    )
                )
            }
            diffInHours < 2 -> { //TODO ---> LOGIC Delivery Speed
                tv_order_status.text = resources.getString(R.string.order_status_in_process)
                tv_order_status.setTextColor(
                    ContextCompat.getColor(
                        this@MyOrdersDetailsActivity,
                        R.color.colorOrderStatusInProcess
                    )
                )
            }
            else -> {
                tv_order_status.text = resources.getString(R.string.order_status_delivered)
                tv_order_status.setTextColor(
                    ContextCompat.getColor(
                        this@MyOrdersDetailsActivity,
                        R.color.colorOrderStatusDelivered
                    )
                )
            }
        }

        rv_my_order_items_list.layoutManager = LinearLayoutManager(this@MyOrdersDetailsActivity)
        rv_my_order_items_list.setHasFixedSize(true)

        val cartListAdapter =
            CartItemsAdapter(this@MyOrdersDetailsActivity, orderDetails.items, false)
        rv_my_order_items_list.adapter = cartListAdapter

        tv_my_order_details_address_type.text = orderDetails.address.type
        tv_my_order_details_full_name.text = orderDetails.address.name
        tv_my_order_details_address.text =
            "${orderDetails.address.address}, ${orderDetails.address.zipCode}"
        tv_my_order_details_additional_note.text = orderDetails.address.additionalNote

        if (orderDetails.address.otherDetails.isNotEmpty()) {
            tv_my_order_details_other_details.visibility = View.VISIBLE
            tv_my_order_details_other_details.text = orderDetails.address.otherDetails
        } else {
            tv_my_order_details_other_details.visibility = View.GONE
        }
        tv_my_order_details_mobile_number.text = orderDetails.address.mobileNumber

        tv_order_details_sub_total.text = orderDetails.sub_total_amount
        tv_order_details_shipping_charge.text = orderDetails.shipping_charge
        tv_order_details_total_amount.text = orderDetails.total_amount
    }

    private fun setActionBar() //To make the action bar invisible and put back button
    {
        setSupportActionBar(toolbar_my_order_details_activity)

        val actionBar = supportActionBar
        if (actionBar != null)
        {
            actionBar.setDisplayHomeAsUpEnabled(true)
            actionBar.setHomeAsUpIndicator(R.drawable.ic_white_color_back_24dp)
        }

        toolbar_my_order_details_activity.setNavigationOnClickListener{(onBackPressed())}


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
}