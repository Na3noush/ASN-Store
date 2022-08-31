package com.example.myshop.ui.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.myshop.R
import com.example.myshop.firebase.FirestoreClass
import com.example.myshop.models.Order
import com.example.myshop.ui.activites.BaseActivity
import com.example.myshop.ui.adapters.OrdersAdapter
import kotlinx.android.synthetic.main.fragment_orders.*

class OrdersFragment : BaseFragment() {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
     //   notificationsViewModel = ViewModelProvider(this).get(NotificationsViewModel::class.java)

        val root = inflater.inflate(R.layout.fragment_orders, container, false)

        return root
    }

    private fun getMyOrdersList()
    {
        showProgressDialog(resources.getString(R.string.please_wait))
        FirestoreClass().getMyOrderList(this)
    }

    fun populateOrderListInUi(orderList:ArrayList<Order>)
    {
        hideProgressDialog()
        if (orderList.size > 0)
        {
            rv_my_order_items.visibility = View.VISIBLE
            tv_no_orders_found.visibility = View.GONE

            rv_my_order_items.layoutManager = LinearLayoutManager(activity)
            rv_my_order_items.setHasFixedSize(true)

            val adapterOrders = OrdersAdapter(requireActivity(), orderList)
            rv_my_order_items.adapter = adapterOrders
        }
        else
        {
            rv_my_order_items.visibility = View.GONE
            tv_no_orders_found.visibility = View.VISIBLE
        }
    }

    override fun onResume() {
        super.onResume()

        getMyOrdersList()
    }
}