package com.example.kotlinchatapp.registerlogin

import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.widget.Toast
import com.example.kotlinchatapp.R
import com.example.kotlinchatapp.messages.LatestMessagesActivity
import com.google.firebase.auth.FirebaseAuth
import kotlinx.android.synthetic.main.activity_login.*

class LoginActivity: AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)
        login_button.setOnClickListener {
            val email = emailText.text.toString()
            val password = passwordText.text.toString()

            Log.d("login", "Attempted login with email/pw: ${email}/***")

            FirebaseAuth.getInstance().signInWithEmailAndPassword(email,password)
                .addOnCompleteListener {
                    val intent =  Intent(this, LatestMessagesActivity::class.java)
                    intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK.or(Intent.FLAG_ACTIVITY_NEW_TASK)
                    startActivity(intent)
                }
                .addOnFailureListener {
                    Log.d("LoginActivity","Failed to login in: ${it.message}")
                    Toast.makeText(this,"Failed to login in: ${it.message}", Toast.LENGTH_SHORT).show()
                }
        }
        back_to_register_text.setOnClickListener {
            finish()
        }
    }
}