package com.example.sinco_v2

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.google.firebase.auth.FirebaseAuth

class SplashActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)

        val auth = FirebaseAuth.getInstance()

        val user = auth.currentUser
        val userId = user?.uid

        if (userId != null){ // user is logged in
            val intent = Intent(this, ManagerActivity::class.java)
            startActivity(intent)
            finish()
        } else {
            val intent = Intent(this, ManagerActivity::class.java) // LOGIN ACTIVITY
            startActivity(intent)
            finish()
        }


    }
}