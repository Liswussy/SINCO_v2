package com.example.sinco_v2

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.KeyEvent
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.FragmentActivity
import com.google.firebase.auth.FirebaseAuth
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

        val registerButton = findViewById<TextView>(R.id.registerButton)
        registerButton.setOnClickListener {
            val intent = Intent(this, AccountRegisterActivity::class.java)
            startActivity(intent)
        }
    }

    private fun loginUser() {
        val emailEditText = findViewById<EditText>(R.id.textInputFieldEmail)
        val passwordEditText = findViewById<EditText>(R.id.textInputFieldPassword)
        passwordEditText.setOnEditorActionListener { _, actionId, event ->
            if (actionId == EditorInfo.IME_ACTION_DONE || (event != null && event.action == KeyEvent.ACTION_DOWN && event.keyCode == KeyEvent.KEYCODE_ENTER)) {
                // Handle the enter key action here
                hideKeyboard()
                // You can perform any action or validation when the enter key is pressed
                return@setOnEditorActionListener true
            }
            return@setOnEditorActionListener false
        }

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
                            val intent = Intent(this, MainManagerActivity::class.java)
                            startActivity(intent)
                            finish()
                        }
                        "employee" -> {
                            // Redirect to HomeActivity that hosts the HomeFragment
                            val intent = Intent(this, MainEmployeeActivity::class.java)
                            startActivity(intent)
                            finish()
                        }
                        else -> {
                            // not allow access when user has no role
                            Toast.makeText(this, "Access Denied. Please contact a Manager to activate your account", Toast.LENGTH_SHORT).show()
                            logoutUser()
                        }
                    }
                } else {
                    // not allow access when user has no data
                    Toast.makeText(this, "Access Denied. Please contact a Manager to activate your account", Toast.LENGTH_SHORT).show()
                    logoutUser()
                }
            }
            .addOnFailureListener { exception ->
                // not allow access when user data cannot be read/verified
                Toast.makeText(this, "Access Denied. Cannot verify account.", Toast.LENGTH_SHORT).show()
                logoutUser()
            }
    }

    private fun logoutUser() {
        auth.signOut()
    }

    private fun hideKeyboard() {
        val imm =
            getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(currentFocus?.windowToken, 0)
    }
}
