package com.example.myshop.ui.fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.myshop.R
import com.example.myshop.firebase.FirestoreClass
import com.example.myshop.models.SoldProduct
import com.example.myshop.ui.adapters.SoldProductsAdapter
import kotlinx.android.synthetic.main.fragment_sold_products.*


class SoldProductsFragment : BaseFragment() {
    
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_sold_products, container, false)
    }
    
    override fun onResume() {
        super.onResume()
        getSoldProductListFromFireStore()
    }
    
    private fun getSoldProductListFromFireStore()
    {
        showProgressDialog(resources.getString(R.string.please_wait))
        FirestoreClass().getSoldProductList(this)
    }
    
    fun successGetSoldProductsDetails(soldProductsList:ArrayList<SoldProduct>)
    {
        hideProgressDialog()
        
        if (soldProductsList.size > 0)
        {
            rv_sold_product_items.visibility =View.VISIBLE
            tv_no_sold_products_found.visibility = View.GONE
            
            rv_sold_product_items.layoutManager = LinearLayoutManager(activity)
            rv_sold_product_items.setHasFixedSize(true)

            val adapterSoldProducts = SoldProductsAdapter(requireActivity(), soldProductsList)
            rv_sold_product_items.adapter = adapterSoldProducts
        }
        else
        {
            rv_sold_product_items.visibility = View.GONE
            tv_no_sold_products_found.visibility = View.VISIBLE
        }
    }
    
}