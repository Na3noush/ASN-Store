package com.example.myshop.ui.activites

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.view.View
import android.view.WindowManager
import android.widget.Toast
import com.example.myshop.R
import com.example.myshop.firebase.FirestoreClass
import com.example.myshop.models.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import kotlinx.android.synthetic.main.activity_register.*


@Suppress("DEPRECATION")
open class RegisterActivity : BaseActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)

        window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_FULLSCREEN
        
        cirRegisterButton.setOnClickListener()
        {
            registerUser()
        }

        txtAlreadyHaveAcc.setOnClickListener()
        {
            onBackPressed()
           // val intent = Intent(this, LoginActivity::class.java)
           // startActivity(intent)
        }
    }

    private fun registerUser() {
        val name = editTextName.text.toString()
        val email:String = editTextEmail.text.toString().trim {it <= ' '}
        val mob = editTextMobile.text.toString()
        val pass:String = editTextPassword.text.toString().trim {it <= ' '}

        if (name.trim().isNotEmpty() && email.trim().isNotEmpty() && mob.trim().isNotEmpty() && pass.trim().isNotEmpty())
        {
            showProgressDialog(resources.getString(R.string.please_wait))
            FirebaseAuth.getInstance().createUserWithEmailAndPassword(email, pass)
                .addOnCompleteListener { task ->
                    //if registration success
                    if (task.isSuccessful) {

                        val firebaseUser: FirebaseUser = task.result!!.user!!
                        val user = User(
                            firebaseUser.uid,
                            editTextName.text.toString().trim { it <= ' ' },
                            editTextEmail.text.toString().trim { it <= ' ' }
                        )

                        FirestoreClass().registerUser(this@RegisterActivity, user)

                        Toast.makeText(this,
                            "Registered Successfully, Your ID Is ${firebaseUser.uid}",
                            Toast.LENGTH_LONG)
                            .show()
                        FirebaseAuth.getInstance().signOut()
                        startActivity(Intent(this@RegisterActivity, LoginActivity::class.java))
                    }
                    else {
                        hideProgressDialog()
                        showErrorSnackBar(task.exception!!.message.toString(), true)
                        //Toast.makeText(this, task.exception!!.message.toString(), Toast.LENGTH_LONG).show()
                    }
                }
        }
        else {
            showErrorSnackBar(resources.getString(R.string.err_msg_enter_all_fields), true)

        }
    } // RegisterUser And Check Empty Fields

    fun userRegistrationSuccess(){
        //Hide progress dialog
        hideProgressDialog()
    }
}