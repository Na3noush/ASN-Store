package com.example.myshop.models

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
class Product(
    val user_id:String = "",
    val user_name: String? = "",
    val image:String = "",
    val title:String = "",
    val price:String ="",
    val description:String = "",
    val stock_quantity:String = "",
    var product_id: String = ""): Parcelable