package com.example.myshop.ui.fragments

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.*
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.myshop.R
import com.example.myshop.databinding.FragmentDashboardBinding
import com.example.myshop.firebase.FirestoreClass
import com.example.myshop.models.Product
import com.example.myshop.ui.activites.CartListActivity
import com.example.myshop.ui.activites.SettingActivity
import com.example.myshop.ui.adapters.DashboardItemsListAdapter
import com.example.myshop.ui.adapters.MyProductsListAdapter
import kotlinx.android.synthetic.main.fragment_dashboard.*
import kotlinx.android.synthetic.main.fragment_products.*

class DashboardFragment : BaseFragment() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        //homeViewModel = ViewModelProvider(this).get(DashboardViewModel::class.java)
        val root = inflater.inflate(R.layout.fragment_dashboard, container, false)

        return root
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {

        inflater.inflate(R.menu.dashboard_menu, menu)
        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        val id = item.itemId

        when(id)
        {
            R.id.action_settings ->
            {
                startActivity(Intent(activity, SettingActivity::class.java))
                return true
            }
            R.id.action_cart ->
            {
                startActivity(Intent(activity, CartListActivity::class.java))
            }
        }
        return super.onOptionsItemSelected(item)
    }

    fun successDashboardItemsList(dashboardItemsList:ArrayList<Product>)
    {
        hideProgressDialog()

        if (dashboardItemsList.size > 0)
        {
            tv_no_dashboard_items_found.visibility = View.GONE
            rv_my_dashboard_items.visibility = View.VISIBLE

            rv_my_dashboard_items.layoutManager = GridLayoutManager(activity, 2)
            rv_my_dashboard_items.setHasFixedSize(true)
            val adapterDashboard = DashboardItemsListAdapter(requireActivity(), dashboardItemsList)
            rv_my_dashboard_items.adapter = adapterDashboard
        }
        else
        {
            tv_no_dashboard_items_found.visibility = View.VISIBLE
            rv_my_dashboard_items.visibility = View.GONE
        }
    }

    private fun getDashboardItems()
    {
        showProgressDialog(resources.getString(R.string.please_wait))
        FirestoreClass().getDashboardItemsList(this)
    }

    override fun onResume() {
        super.onResume()
        getDashboardItems()
    }
}