package com.example.myshop.utils

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.provider.MediaStore
import android.webkit.MimeTypeMap

object Constants {

    const val Users:String = "users"
    const val PRODUCTS:String = "products"
    const val CART_ITEM:String = "cart_item"
    const val ORDERS:String = "orders"
    const val SOLD_PRODUCTS:String = "sold_products"

    const val MYSHOPPAL_PREFERENCES:String = "MyShopPalPrefs"
    const val LOGGED_IN_EMAIL:String ="logged_in_email"
    const val LOGGED_IN_PASS:String="logged_in_pass"
    
    const val LOGGED_IN_USERNAME:String = "logged_in_username"
    const val EXTRA_USER_DETAILS:String = "extra_user_details"

    const val READ_STORAGE_PERMISSION_CODE = 2
    const val PICK_IMAGE_REQUEST_CODE = 1

    const val MALE : String= "male"
    const val FEMALE : String = "female"
    const val NAME : String = "uname"
    const val IMAGE : String = "image"
    const val MOBILE : String = "mobile"
    const val GENDER : String = "gender"    
    const val PROFILE_COMPLETED = "profileCompleted"

    const val PRODUCT_IMAGE:String = "Product_Image"
    const val USER_PROFILE_IMAGE:String = "User_Profile_Image"

    const val USER_ID:String = "user_id"

    const val EXTRA_PRODUCT_ID:String = "extra_product_id"
    const val EXTRA_PRODUCT_OWNER_ID:String = "extra_user_id"

    const val DEFAULT_CART_QUANTITY:String = "1"

    const val CART_QUANTITY:String = "cart_quantity"
    const val STOCK_QUANTITY:String = "stock_quantity"
    


    const val PRODUCT_ID:String = "product_id"
    const val PRODUCT_ID_TO_CART = "product_id_to_cart"
    const val ORDER_ID:String = "order_id"

    const val ADDRESSES: String = "addresses"

    const val HOME:String ="Home"
    const val OFFICE:String ="OFFICE"
    const val OTHER:String ="Other"

    const val EXTRA_ADDRESS_DETAILS: String = "AddressDetails"
    const val EXTRA_SELECT_ADDRESS: String = "extra_select_address"
    const val EXTRA_SELECTED_ADDRESS: String = "extra_selected_address"
    const val EXTRA_MY_ORDER_DETAILS:String = "extra_MY_ORDER_DETAILS"
    const val EXTRA_SOLD_PRODUCTS_DETAILS: String = "extra_sold_product_details"

    const val ADD_ADDRESS_REQUEST_CODE: Int = 121


    fun showImageChooser(activity:Activity)
    {
        val galleryIntent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        activity.startActivityForResult(galleryIntent, PICK_IMAGE_REQUEST_CODE)
    }

    /**
     * A function to get the image file extension of the selected image.
     *
     * @param activity Activity reference.
     * @param uri Image file uri.
     */
    fun getFileExtension(activity: Activity, uri: Uri?): String? {
        /*
         * MimeTypeMap: Two-way map that maps MIME-types to file extensions and vice versa.
         *
         * getSingleton(): Get the singleton instance of MimeTypeMap.
         *
         * getExtensionFromMimeType: Return the registered extension for the given MIME type.
         *
         * contentResolver.getType: Return the MIME type of the given content URL.
         */
        return MimeTypeMap.getSingleton()
            .getExtensionFromMimeType(activity.contentResolver.getType(uri!!))
    }
}