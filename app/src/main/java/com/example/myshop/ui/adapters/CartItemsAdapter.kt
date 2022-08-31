package com.example.myshop.ui.adapters

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.example.myshop.R
import com.example.myshop.firebase.FirestoreClass
import com.example.myshop.models.CartItem
import com.example.myshop.ui.activites.BaseActivity
import com.example.myshop.ui.activites.CartListActivity
import com.example.myshop.ui.fragments.ProductsFragment
import com.example.myshop.utils.Constants
import com.example.myshop.utils.GlideLoader
import kotlinx.android.synthetic.main.activity_cart_list.view.*
import kotlinx.android.synthetic.main.item_cart_layout.view.*

open class CartItemsAdapter(
    private val context: Context,
    private val list:ArrayList<CartItem>,
    private val updateCartItems:Boolean

): RecyclerView.Adapter<RecyclerView.ViewHolder>()
    {
        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
            return MyViewHolder(
                LayoutInflater.from(context).inflate(R.layout.item_cart_layout, parent, false)
            )
        }

        override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
            val model = list[position]

            if (holder is MyViewHolder)
            {
                GlideLoader(context).loadProductPicture(model.image, holder.itemView.iv_cart_item_image)
                holder.itemView.tv_cart_item_title.text = model.title
                holder.itemView.tv_cart_quantity.text = model.cart_quantity
                holder.itemView.tv_cart_item_price.text = "$${model.price}"

                if (updateCartItems)
                {
                    holder.itemView.ib_delete_cart_item.visibility = View.VISIBLE
                }
                else
                {
                    holder.itemView.ib_delete_cart_item.visibility = View.GONE
                }

                if (model.cart_quantity.toInt() <= 0)
                {
                    /*holder.itemView.ib_remove_cart_item.visibility = View.GONE
                    holder.itemView.ib_add_cart_item.visibility = View.GONE

                    holder.itemView.tv_cart_quantity.text =
                        context.resources.getString(R.string.lbl_out_of_stock)

                    holder.itemView.tv_cart_quantity.setTextColor(
                        ContextCompat.getColor(context, R.color.colorSnackBarError)
                    )

                    */
                    FirestoreClass().removeItemFromCart(context, model.id)
                }
                else
                {
                    if (updateCartItems)
                    {
                        holder.itemView.ib_remove_cart_item.visibility = View.VISIBLE
                        holder.itemView.ib_add_cart_item.visibility = View.VISIBLE
                        holder.itemView.ib_delete_cart_item.visibility = View.VISIBLE
                    }
                    else
                    {
                        holder.itemView.ib_remove_cart_item.visibility = View.GONE
                        holder.itemView.ib_add_cart_item.visibility = View.GONE
                        holder.itemView.ib_delete_cart_item.visibility = View.GONE
                    }

                    holder.itemView.tv_cart_quantity.setTextColor(
                        ContextCompat.getColor(
                            context,
                            R.color.colorSecondaryText
                        )
                    )
                }

                holder.itemView.ib_delete_cart_item.setOnClickListener{
                    when(context)
                    {
                        is CartListActivity ->
                        {
                            context.showProgressDialog(context.resources.getString(R.string.please_wait))
                        }
                    }
                    FirestoreClass().removeItemFromCart(context, model.id)
                }



                holder.itemView.ib_add_cart_item.setOnClickListener {


                    val cartQuantity: Int = model.cart_quantity.toInt()

                    if (cartQuantity < model.stock_quantity.toInt()) {

                        val itemHashMap = HashMap<String, Any>()

                        itemHashMap[Constants.CART_QUANTITY] = (cartQuantity + 1).toString()

                        // Show the progress dialog.
                        if (context is CartListActivity) {
                            context.showProgressDialog(context.resources.getString(R.string.please_wait))
                        }



                        FirestoreClass().updateMyCart(context, model.id, itemHashMap)


                    } else {
                        if (context is CartListActivity) {
                            context.showErrorSnackBar(
                                context.resources.getString(
                                    R.string.msg_for_available_stock,
                                    model.stock_quantity
                                ),
                                true
                            )
                        }

                    }
                }


                holder.itemView.ib_remove_cart_item.setOnClickListener{
                    if (model.cart_quantity == "1")
                    {
                        if (context is CartListActivity)
                        {
                            context.showProgressDialog("Please Wait")
                        }
                        FirestoreClass().removeItemFromCart(context, model.id)
                    }
                    else {

                        val cartQuantity: Int = model.cart_quantity.toInt()

                        val itemHashMap = HashMap<String, Any>()

                        itemHashMap[Constants.CART_QUANTITY] = (cartQuantity - 1).toString()

                        if (context is CartListActivity) {
                            context.showProgressDialog(context.resources.getString(R.string.please_wait))
                        }
                        FirestoreClass().updateMyCart(context, model.id, itemHashMap)
                    }
                }
            }
        }

        override fun getItemCount(): Int {
            return list.size
        }


        class MyViewHolder(view: View):RecyclerView.ViewHolder(view)
    }