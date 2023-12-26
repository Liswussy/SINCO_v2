package com.example.sinco_v2

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FirebaseFirestore

class LoginActivity : AppCompatActivity() {
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        auth = FirebaseAuth.getInstance()

        val loginButton = findViewById<Button>(R.id.buttonLogin)
        loginButton.setOnClickListener {
            loginUser()
        }
    }

    private fun loginUser() {
        val emailEditText = findViewById<EditText>(R.id.textInputFieldEmail)
        val passwordEditText = findViewById<EditText>(R.id.textInputFieldPassword)

        val email = emailEditText.text.toString()
        val password = passwordEditText.text.toString()

        if (email.isEmpty() || password.isEmpty()) {
            // Display a Toast message if email or password is empty
            Toast.makeText(this, "Please provide login information", Toast.LENGTH_SHORT).show()
            return
        }

        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    val user = auth.currentUser
                    if (user != null) {
                        // Fetch user role from Firestore and redirect accordingly
                        fetchUserRoleAndRedirect(user.uid)
                    }
                } else {
                    Toast.makeText(this, "Authentication failed.", Toast.LENGTH_SHORT).show()
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
                    when (userRole) {
                        "manager" -> {
                            // Redirect to ManagerHomeActivity that hosts the ManagerHomeFragment
                            val intent = Intent(this, ManagerActivity::class.java)
                            startActivity(intent)
                            finish()
                        }
                        "store staff" -> {
                            // Redirect to HomeActivity that hosts the HomeFragment
                            val intent = Intent(this, MainActivity::class.java)
                            startActivity(intent)
                            finish()
                        }
                        else -> {
                            // Redirect to a default home fragment or activity
                            val intent = Intent(this, MainActivity::class.java)
                            startActivity(intent)
                            finish()
                        }
                    }
                } else {
                    // Document doesn't exist
                    // Redirect to a default home fragment or activity
                    val intent = Intent(this, MainActivity::class.java)
                    startActivity(intent)
                    finish()
                }
            }
            .addOnFailureListener { exception ->
                // Failed to retrieve document
                // Redirect to a default home fragment or activity
                val intent = Intent(this, MainActivity::class.java)
                startActivity(intent)
                finish()
            }
    }
}
