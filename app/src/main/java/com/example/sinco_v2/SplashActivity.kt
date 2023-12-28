// SplashActivity.kt

package com.example.sinco_v2

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class SplashActivity : AppCompatActivity() {
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)
        redirectToDestination()
    }

    private fun redirectToDestination() {
        val user = auth.currentUser
        if (user != null) {
            // User is already authenticated, fetch user role and redirect accordingly
            fetchUserRoleAndRedirect(user.uid)
        } else {
            // User is not authenticated, redirect to LoginActivity
            redirectToLogin()
        }
    }

    private fun redirectToLogin() {
        val intent = Intent(this, LoginActivity::class.java)
        startActivity(intent)
        finish()
    }

    private fun redirectToHomeActivity(userRole: String) {
        when (userRole) {
            "manager" -> {
                // Redirect to ManagerActivity that hosts the ManagerFragment
                val intent = Intent(this, MainManagerActivity::class.java)
                startActivity(intent)
                finish()
            }

            "employee" -> {
                // Redirect to MainActivity that hosts the HomeFragment
                val intent = Intent(this, MainEmployeeActivity::class.java)
                startActivity(intent)
                finish()
            }

            else -> {
                // Redirect to a default home fragment or activity
                logoutUser()
                val intent = Intent(this, LoginActivity::class.java)
                startActivity(intent)
                finish()
            }
        }
    }

    private fun fetchUserRoleAndRedirect(userId: String) {
        val db = FirebaseFirestore.getInstance()
        val docRef = db.collection("users").document(userId)

        docRef.get()
            .addOnSuccessListener { document ->
                if (document != null && document.exists()) {
                    val userRole = document.getString("role")
                    // Redirect based on user role
                    redirectToHomeActivity(userRole.orEmpty())
                } else {
                    // Document doesn't exist
                    // Redirect to a default home fragment or activity
                    redirectToHomeActivity("")
                }
            }
            .addOnFailureListener { exception ->
                // Failed to retrieve document
                // Redirect to a default home fragment or activity
                redirectToHomeActivity("")
            }
    }

    private fun logoutUser() {
        auth.signOut()
    }
}

