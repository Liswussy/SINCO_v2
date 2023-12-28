package com.example.sinco_v2

import android.content.ContentValues.TAG
import android.content.Context
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.AttributeSet
import android.util.Log
import android.view.View
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.Spinner
import android.widget.Toast
import androidx.compose.ui.text.toLowerCase
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.example.sinco_v2.databinding.ActivityLoginBinding
import com.example.sinco_v2.databinding.ActivityMainBinding
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

class EnrollEmployeeActivity : AppCompatActivity() {

    private lateinit var firstName: EditText
    private lateinit var lastName: EditText
    private lateinit var email: EditText
    private lateinit var contactNum: EditText
    private lateinit var role: Spinner
    private lateinit var enrollButton: Button
    private lateinit var deactivateButton: Button
    private lateinit var mAuth: FirebaseAuth
    val db = Firebase.firestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_new_employee_account)

        mAuth = FirebaseAuth.getInstance()

        // Initialize your views
        initializeViews()

        // Set click listener for the add button
        val backButton = findViewById<ImageButton>(R.id.ib_back_icon)
        backButton.setOnClickListener {
            finish()
        }

        val employeeUID = intent.getStringExtra("employeeUID")

        if (employeeUID != null){
            val docRef = db.collection("users").document(employeeUID)
            docRef.get()
                .addOnSuccessListener { document ->
                    if (document != null) {
                        val firstnameString = document.getString("firstname")
                        val lastnameString = document.getString("lastname")
                        val emailString = document.getString("email")
                        val contactNumString = document.getString("contactnum")
                        val roleString = document.getString("role")

                        firstName.setText(firstnameString)
                        lastName.setText(lastnameString)
                        email.setText(emailString)
                        contactNum.setText(contactNumString)

                        val roleArray = resources.getStringArray(R.array.role)
                        val positionToSelect = when (roleString) {
                            "employee" -> roleArray.indexOf("Employee")
                            "manager" -> roleArray.indexOf("Manager")
                            "auditor" -> roleArray.indexOf("Auditor")
                            else -> 0 // Default position, you can change it based on your needs
                        }

                        role.setSelection(positionToSelect)

                    } else {
                        Log.d(TAG, "No such document")
                    }
                }
                .addOnFailureListener { exception ->
                    Log.d(TAG, "get failed with ", exception)
                }

            enrollButton.setOnClickListener{
                val newRole = role.selectedItem.toString()
                enrollUser(employeeUID, newRole.toLowerCase())
            }

            deactivateButton.setOnClickListener{
                deactivateUser(employeeUID)
            }
        }

    }

    private fun enrollUser(employeeUID: String, newRole: String) {
        val docRef = db.collection("users").document(employeeUID)
        docRef.update("role", newRole)
            .addOnSuccessListener {
                Toast.makeText(this, "Successfully enrolled user account.", Toast.LENGTH_SHORT).show()
                finish()
            }
            .addOnFailureListener {
                Toast.makeText(this, "Failed to enroll user. Please try again", Toast.LENGTH_SHORT).show()
            }
    }

    private fun initializeViews() {
        firstName = findViewById(R.id.firstNameTextInputEditText)
        lastName = findViewById(R.id.NameTextInputEditText)
        email = findViewById(R.id.emailTextInputEditText)
        contactNum = findViewById(R.id.contactTextInputEditText)
        role = findViewById(R.id.spinnerRole) // Change to Spinner
        enrollButton = findViewById(R.id.enrollButton)
        deactivateButton = findViewById(R.id.deactivateButton)

        role = findViewById(R.id.spinnerRole)
        setupRoleSpinner()
    }

    private fun deactivateUser(employeeUID: String){
        MaterialAlertDialogBuilder(this)
            .setTitle("Deactivate User Account")
            .setMessage("Are you sure you want to deactivate this account?")
            .setNeutralButton("Cancel") { dialog, which ->
                // Respond to neutral button press
            }
            .setPositiveButton("Confirm") { dialog, which ->
                // Respond to positive button press
                val docRef = db.collection("users").document(employeeUID)
                docRef.update("role", "inactive")
                    .addOnSuccessListener {
                        Toast.makeText(this, "Successfully deactivated user account.", Toast.LENGTH_SHORT).show()
                        finish()
                    }
                    .addOnFailureListener {
                        Toast.makeText(this, "Failed to deactivate user. Please try again", Toast.LENGTH_SHORT).show()
                    }
            }
            .show()
    }

    private fun setupRoleSpinner() {
        // Retrieve the array from resources
        val roleArray = resources.getStringArray(R.array.role)

        // Create an adapter for the Spinner
        val adapter = ArrayAdapter(
            this,
            R.layout.custom_spinner_dropdown_items, R.id.textview1, roleArray)

        // Set the adapter for the Spinner
        role.adapter = adapter

    }
}
