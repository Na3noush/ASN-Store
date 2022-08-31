package com.example.myshop.firebase

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.SharedPreferences
import android.net.Uri
import android.util.Log
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.example.myshop.models.*
import com.example.myshop.ui.activites.*
import com.example.myshop.ui.fragments.DashboardFragment
import com.example.myshop.ui.fragments.OrdersFragment
import com.example.myshop.ui.fragments.ProductsFragment
import com.example.myshop.ui.fragments.SoldProductsFragment
import com.example.myshop.utils.Constants
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import kotlin.math.log

class FirestoreClass {
    private val mFireStore = FirebaseFirestore.getInstance()

    fun registerUser(activity: RegisterActivity, userInfo: User) {// create collection "users" to save user details after registration

        //Very Important Code -->
        mFireStore.collection(Constants.Users) // Utils/Constants
            .document(userInfo.id)
            .set(userInfo, SetOptions.merge())
            .addOnSuccessListener { activity.userRegistrationSuccess() }
            .addOnFailureListener{ e ->
                activity.hideProgressDialog()
                Log.e(
                    activity.javaClass.simpleName,
                    "Error while registration the user.",e
                )
            }
    }

    fun getCurrentUserID() :String {//getting current user id to use it in getting his details after logged in

        val currentUser = FirebaseAuth.getInstance().currentUser

        var currentUserID = ""
        if (currentUser != null)
        {
            currentUserID = currentUser.uid
        }
        return currentUserID
    }

    @SuppressLint("CommitPrefEdits")
    fun getUserDetails(activity: Activity){ //getting user details from "users" collection after logged in

        mFireStore.collection(Constants.Users)
            .document(getCurrentUserID())
            .get()
            .addOnSuccessListener { document ->
                Log.i(activity.javaClass.simpleName, document.toString())

                val user = document.toObject(User::class.java)!!

                //بنعمل ذاكرة تحفظ ال username علشان نستخدمة في ال Main Activity
                val sharedPreferences = activity.getSharedPreferences(Constants.MYSHOPPAL_PREFERENCES, Context.MODE_PRIVATE)

                val editor :SharedPreferences.Editor = sharedPreferences.edit()
                //Key: LOGGED_IN_USERNAME
                //Value : Ahmed Saad
                editor.putString(Constants.LOGGED_IN_USERNAME, user.uname)
                editor.apply()

                when(activity)
                {
                    is LoginActivity -> {
                        activity.userLoginInSuccess(user)
                    }

                    is SettingActivity -> {
                        activity.userDetailsSuccess(user)
                    }
                }
            }
            .addOnFailureListener{ e ->
                when(activity)
                {
                    is LoginActivity -> {
                        activity.hideProgressDialog()
                        Log.e(
                            activity.javaClass.simpleName,
                            "Error while registration the user.",e
                        )
                    }
                }
            }
    }

    fun updateUserProfileData(activity: Activity, userHashMap: HashMap<String, Any>){

        mFireStore.collection(Constants.Users).
        document(getCurrentUserID()).
        update(userHashMap).
        addOnSuccessListener {
            when(activity){
                is UserProfileActivity -> {
                    activity.hideProgressDialog()
                }
            }
        }.
        addOnFailureListener{
            when(activity){
                is UserProfileActivity -> {
                    activity.hideProgressDialog()
                }
            }
            Log.e(activity.javaClass.simpleName, "ERROR WHILE UPDATING")
        }
    }

    fun uploadImageToCloudStorage(activity: Activity, imageFileUri:Uri?, imageType:String){
        val sRef:StorageReference = FirebaseStorage.getInstance().reference.child(
            imageType + System.currentTimeMillis() +
                    "." + Constants.getFileExtension(activity, imageFileUri)
        )

        sRef.putFile(imageFileUri!!).addOnSuccessListener { taskSnapshot ->
            Log.e("Firebase Image Url", taskSnapshot.metadata!!.reference!!.downloadUrl.toString())

            taskSnapshot.metadata!!.reference!!.downloadUrl.addOnSuccessListener { uri ->
                Log.e("Downloadable image URL", uri.toString())
                when(activity)
                {
                    is UserProfileActivity ->
                    {
                        activity.imageUploadSuccess(uri.toString())
                    }

                    is AddProductActivity ->
                    {
                        activity.imageUploadSuccess(uri.toString())
                    }
                }
            }
        }
            .addOnFailureListener{ exception ->
                when(activity){
                    is UserProfileActivity -> {
                        activity.hideProgressDialog()
                    }
                    is AddProductActivity -> {
                        activity.hideProgressDialog()
                    }
                }
                Log.e(activity.javaClass.simpleName, exception.message, exception)
            }
    }

    fun uploadProductDetails(activity: AddProductActivity, productInfo:Product)
    {
        mFireStore.collection(Constants.PRODUCTS)
            .document()
            .set(productInfo, SetOptions.merge())
            .addOnSuccessListener { activity.productUploadedSuccessfully() }
            .addOnFailureListener {
                activity.hideProgressDialog()
                Log.e(activity.javaClass.simpleName, "Error while uploading the product.")
            }
    }

    fun getProductsList(fragment:Fragment)
    {
        mFireStore.collection(Constants.PRODUCTS)
            .whereEqualTo(Constants.USER_ID, getCurrentUserID())
            .get()
            .addOnSuccessListener { document ->
                Log.e("Product List", document.documents.toString())
                val productList:ArrayList<Product> = ArrayList()
                for (i in document.documents)
                {
                    val product = i.toObject(Product::class.java)
                    product!!.product_id = i.id

                    productList.add(product)
                }

                when(fragment)
                {
                    is ProductsFragment ->
                    {
                        fragment.successProductsListFromFirestore(productList)
                    }
                }
            }
    }

    fun getAllProductList(activity: Activity)
    {
        mFireStore.collection(Constants.PRODUCTS)
            .get()
            .addOnSuccessListener { document ->
                Log.e(activity.javaClass.simpleName, document.documents.toString())

                val productList:ArrayList<Product> = ArrayList()
                for (i in document.documents)
                {
                    val product = i.toObject(Product::class.java)
                    product!!.product_id = i.id

                    productList.add(product)
                }
                when(activity)
                {
                    is CartListActivity ->
                    {
                        activity.successProductListFromFireStore(productList)
                    }
                    is CheckoutActivity ->
                    {
                        activity.successGetAllProducts(productList)
                    }
                }
            }
            .addOnFailureListener{ e->
                when(activity)
                {
                    is CartListActivity ->
                    {
                        activity.hideProgressDialog()
                    }
                }
                Log.e(activity.javaClass.simpleName, "Error while getting all products", e)
            }
    }

    fun getDashboardItemsList(fragment: DashboardFragment) {
        mFireStore.collection(Constants.PRODUCTS)
            .get()
            .addOnSuccessListener { document ->
                Log.e("Product List", document.documents.toString())
                val productList: ArrayList<Product> = ArrayList()

                for (i in document.documents)
                {
                    val product = i.toObject(Product::class.java)
                    product!!.product_id = i.id

                    productList.add(product)
                }

                fragment.successDashboardItemsList(productList)
            }
            .addOnFailureListener {
                fragment.hideProgressDialog()
                Log.e(fragment.javaClass.simpleName, "Error while getting dashboard fragment")
            }
    }

    fun deleteProduct(fragment: ProductsFragment, productId:String)
    {
        mFireStore.collection(Constants.PRODUCTS)
            .document(productId)
            .delete()
            .addOnSuccessListener {
                val context:Context
                fragment.productDeletedSuccess()
            }
            .addOnFailureListener {
                e->
                fragment.hideProgressDialog()
                Log.e(fragment.javaClass.simpleName, "Error while deleting the product",e)
            }
    }

    fun getProductDetails(activity:ProductDetailsActivity, productId: String)
    {
        mFireStore.collection(Constants.PRODUCTS)
            .document(productId)
            .get()
            .addOnSuccessListener { document ->
                Log.e(activity.javaClass.simpleName, document.toString())

                val product = document.toObject(Product::class.java)
                if (product != null)
                {
                    activity.productDetailsSuccess(product)
                }
            }
    }

    fun checkIfItemExistInCart(activity: ProductDetailsActivity, productId: String)
    {
        mFireStore.collection(Constants.CART_ITEM)
            .whereEqualTo(Constants.USER_ID, getCurrentUserID())
            .whereEqualTo(Constants.PRODUCT_ID, productId)
            .get()
            .addOnSuccessListener { document ->
                Log.e(activity.javaClass.simpleName, document.documents.toString())

                if (document.documents.size > 0)
                {
                    activity.productExistInCart()
                }
                else
                {
                    activity.hideProgressDialog()
                }
            }
            .addOnFailureListener {
                e->
                activity.hideProgressDialog()
                Log.e(activity.javaClass.simpleName, "error while checking", e)
            }
    }

    fun addCartItems(activity:ProductDetailsActivity, addToCart: CartItem)
    {
        mFireStore.collection(Constants.CART_ITEM)
            .document()
            .set(addToCart, SetOptions.merge())
            .addOnSuccessListener {
                activity.addToCartSuccess()
            }
            .addOnFailureListener {
                e ->
                activity.hideProgressDialog()

                Log.e(
                    activity.javaClass.simpleName,
                    "error while adding to the cart",
                    e
                )
            }
    }

    fun getCartList(activity: Activity)
    {
        mFireStore.collection(Constants.CART_ITEM)
            .whereEqualTo(Constants.USER_ID, getCurrentUserID())
            .get()
            .addOnSuccessListener { document ->
                Log.e(activity.javaClass.simpleName, document.documents.toString())

                val list:ArrayList<CartItem> = ArrayList()
                for (i in document.documents)
                {
                    val cartItem = i.toObject(CartItem::class.java)!!
                    cartItem.id = i.id

                    list.add(cartItem)
                }

                when(activity)
                {
                    is CartListActivity ->
                    {
                        activity.successCartItems(list)
                    }

                    is CheckoutActivity ->
                    {
                        activity.successGetCartItemsList(list)
                    }
                }
            }
    }


    fun updateMyCart(context: Context, cart_id: String, itemHashMap: HashMap<String, Any>)
    {
        mFireStore.collection(Constants.CART_ITEM)
            .document(cart_id)
            .update(itemHashMap)
            .addOnSuccessListener {
                when(context)
                {
                    is CartListActivity ->
                    {
                        context.itemUpdateSuccess()
                    }
                }
            }
            .addOnFailureListener {
                e->
                when(context)
                {
                    is CartListActivity ->
                    {
                        context.hideProgressDialog()
                    }
                }
                Log.e(context.javaClass.simpleName, "Error while updating the cart", e)
            }
    }

    fun deleteItemWhereEqualId(fragment: ProductsFragment, productId: String)
    {
        mFireStore.collection(Constants.CART_ITEM)
            .document()
            .delete()
    }

    fun removeItemFromCart(context: Context, cart_id:String)
    {
        mFireStore.collection(Constants.CART_ITEM)
            .document(cart_id)
            .delete()
            .addOnSuccessListener {

                when(context)
                {
                    is CartListActivity ->
                    {
                        context.itemRemovedSuccessfully()
                    }
                }
            }
            .addOnFailureListener { e ->
                when(context)
                {
                    is CartListActivity ->
                    {
                        context.hideProgressDialog()
                    }
                }
            }
    }



    fun addAddress(activity: AddEditAddressActivity, addressInfo:Address)
    {
        mFireStore.collection(Constants.ADDRESSES)
            .document()
            .set(addressInfo, SetOptions.merge())
            .addOnSuccessListener {
                activity.addUpdateAddressesSuccess()
            }
            .addOnFailureListener {
                e ->
                activity.hideProgressDialog()
                Log.e(activity.javaClass.simpleName, "error while adding address", e)
            }
    }

    fun getAddressesList(activity: AddressListActivity)
    {
        mFireStore.collection(Constants.ADDRESSES)
            .whereEqualTo(Constants.USER_ID, getCurrentUserID())
            .get()
            .addOnSuccessListener { document ->
                Log.e(activity.javaClass.simpleName, document.documents.toString())

                val list:ArrayList<Address> = ArrayList()
                for (i in document.documents)
                {
                    val addresses = i.toObject(Address::class.java)!!
                    addresses.id = i.id
                    list.add(addresses)
                }
                activity.successGetAddresses(list)
            }
    }

    //WE UPDATE THE ADDRESS FROM THE ADAPTER AND THEN WE UPDATE THE DATABASE -->
    fun updateAddress(activity: AddEditAddressActivity, addressInfo: Address, addressId:String)
    {
        mFireStore.collection(Constants.ADDRESSES)
            .document(addressId)
            .set(addressInfo, SetOptions.merge())
            .addOnSuccessListener {
                activity.addUpdateAddressesSuccess()
            }
            .addOnFailureListener {
                e ->
                activity.hideProgressDialog()
                Log.e(activity.javaClass.simpleName, "error while updating the address", e)
            }
    }

    fun deleteAddress(activity: AddressListActivity, addressId: String)
    {
        mFireStore.collection(Constants.ADDRESSES)
            .document(addressId)
            .delete()
            .addOnSuccessListener {
                activity.addressDeletedSuccess()
            }
            .addOnFailureListener {
                e ->
                activity.hideProgressDialog()
                Log.e(activity.javaClass.simpleName, "error while deleting the address", e)
            }
    }

    fun uploadOrdersDetails(activity: CheckoutActivity, order_details:Order)
    {
        mFireStore.collection(Constants.ORDERS)
            .document()
            .set(order_details, SetOptions.merge())
            .addOnSuccessListener {
                activity.successPlacedOrder()
            }
            .addOnFailureListener {
                e ->
                activity.hideProgressDialog()
                Log.e(activity.javaClass.simpleName, "error while placing order", e)
            }
    }

    fun updateAllDetails(activity: CheckoutActivity, cartList:ArrayList<CartItem>, order: Order)
    {
        val writeBatch = mFireStore.batch()

        for (cartItem in cartList)
        {
            val productHashMap = HashMap<String, Any>()
            productHashMap[Constants.STOCK_QUANTITY] =
                (cartItem.stock_quantity.toInt() - cartItem.cart_quantity.toInt()).toString()

            val soldProduct = SoldProduct(
                cartItem.product_owner_id,
                cartItem.title,
                cartItem.price,
                cartItem.cart_quantity,
                cartItem.image,
                order.title,
                order.order_datetime,
                order.sub_total_amount,
                order.shipping_charge,
                order.total_amount,
                order.address
            )

            val documentReference = mFireStore.collection(Constants.SOLD_PRODUCTS)
                .document(cartItem.product_id)

            val documentReferenceUpdateQuantity = mFireStore.collection(Constants.PRODUCTS)
                .document(cartItem.product_id)

            writeBatch.set(documentReference, soldProduct)
            writeBatch.update(documentReferenceUpdateQuantity, productHashMap) // update quantity
        }

        for (cartItem in cartList)
        {
         val documentReference = mFireStore.collection(Constants.CART_ITEM)
             .document(cartItem.id)

         writeBatch.delete(documentReference)
        }


        writeBatch.commit()
            .addOnSuccessListener {
                activity.allDetailsUpdatedSuccess()
        }
            .addOnFailureListener {
                e->
                activity.hideProgressDialog()
                Log.e(activity.javaClass.simpleName, "error while update the details",e)
            }
    }

    fun getMyOrderList(fragment: OrdersFragment)
    {
        mFireStore.collection(Constants.ORDERS)
            .whereEqualTo(Constants.USER_ID, getCurrentUserID())
            .get()
            .addOnSuccessListener {
                document ->
                val list:ArrayList<Order> = ArrayList()

                for (i in document.documents)
                {
                    val orderItem = i.toObject(Order::class.java)!!
                    orderItem.id = i.id

                    list.add(orderItem)
                }

                fragment.populateOrderListInUi(list)
            }
            .addOnFailureListener {
                e ->
                fragment.hideProgressDialog()
                Log.e(fragment.javaClass.simpleName, "error while getting orders", e)
            }
    }

    fun getSoldProductList(fragment:SoldProductsFragment)
    {
        mFireStore.collection(Constants.SOLD_PRODUCTS)
            .whereEqualTo(Constants.USER_ID, getCurrentUserID())
            .get()
            .addOnSuccessListener {
                document ->
                val list:ArrayList<SoldProduct> = ArrayList()
                
                for (i in document.documents)
                {
                    val soldProductItem = i.toObject(SoldProduct::class.java)!!
                    soldProductItem.id = i.id
                    
                    list.add(soldProductItem)
                }
                
                fragment.successGetSoldProductsDetails(list)
            }
            .addOnFailureListener {
                e->
                fragment.hideProgressDialog()
                Log.e(fragment.javaClass.simpleName, "error while getting sold products list.", e)
            }
    }
}