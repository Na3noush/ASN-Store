package com.example.myshop.models

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
class User (
    val id:String = "",
    val uname:String = "",
    val email:String = "",
    val mobile:Long = 0,
    val gender:String = "",
    val image:String = "",
    var profileCompleted:Int = 0) : Parcelable