package com.example.sinco_v2

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.Spinner
import android.widget.Toast
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.AuthResult
import com.google.firebase.firestore.FirebaseFirestore

class AddNewEmployeeAccountActivity : AppCompatActivity() {

    private lateinit var firstName: EditText
    private lateinit var lastName: EditText
    private lateinit var email: EditText
    private lateinit var password: EditText
    private lateinit var confirmPassword: EditText
    private lateinit var contactNum: EditText
    private lateinit var role: Spinner
    private lateinit var addButton: Button
    private lateinit var mAuth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_new_employee_account)

        mAuth = FirebaseAuth.getInstance()

        // Initialize your views
        initializeViews()

        // Set click listener for the add button
        addButton.setOnClickListener {
            validateAndCreateUserAccount()
        }

        val backButton = findViewById<ImageButton>(R.id.ib_back_icon)
        backButton.setOnClickListener {
            finish()
        }
    }

    private fun initializeViews() {
        firstName = findViewById(R.id.firstNameTextInputEditText)
        lastName = findViewById(R.id.NameTextInputEditText)
        email = findViewById(R.id.emailTextInputEditText)
        password = findViewById(R.id.passwordTextInputEditText)
        confirmPassword = findViewById(R.id.confirmPasswordTextInputEditText)
        contactNum = findViewById(R.id.contactTextInputEditText)
        role = findViewById(R.id.spinnerRole) // Change to Spinner
        addButton = findViewById(R.id.addButton)

        role = findViewById(R.id.spinnerRole)
        setupRoleSpinner()
    }

    private fun validateAndCreateUserAccount() {
        if (password.text.toString() == confirmPassword.text.toString()) {
            createUserAccount(email.text.toString(), password.text.toString())
        } else {
            Toast.makeText(this, "Passwords do not match", Toast.LENGTH_SHORT).show()
        }
    }

    private fun createUserAccount(email: String, password: String) {
        mAuth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener(this) { task: Task<AuthResult> ->
                if (task.isSuccessful) {
                    val user: FirebaseUser? = mAuth.currentUser
                    val db = FirebaseFirestore.getInstance()

                    val userData = hashMapOf(
                        "firstname" to firstName.text.toString(),
                        "lastname" to lastName.text.toString(),
                        "email" to email,
                        "contactnum" to contactNum.text.toString(),
                        "role" to role.selectedItem.toString() // Get selected item from Spinner
                    )

                    if (user != null) {
                        db.collection("users").document(user.uid)
                            .set(userData)
                            .addOnSuccessListener {
                                Toast.makeText(
                                    this,
                                    "New Account Created",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                            .addOnFailureListener {
                                Toast.makeText(this, "Task failed", Toast.LENGTH_SHORT).show()
                            }
                    }
                } else {
                    val exception = task.exception
                    // Handle the error, e.g., display an error message to the user
                }
            }
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
