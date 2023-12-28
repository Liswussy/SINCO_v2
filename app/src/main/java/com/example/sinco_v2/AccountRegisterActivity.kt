package com.example.sinco_v2

import android.content.ContentValues.TAG
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.ImageButton
import android.widget.Toast
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthException
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import com.google.firebase.auth.FirebaseAuthWeakPasswordException
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

class AccountRegisterActivity : AppCompatActivity() {

    private lateinit var backButton: ImageButton
    private lateinit var buttonLogin: Button
    private lateinit var auth: FirebaseAuth
    val db = Firebase.firestore
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_account_register)

        backButton = findViewById(R.id.backButton)
        backButton.setOnClickListener{
            finish()
        }

        buttonLogin = findViewById(R.id.buttonLogin)
        buttonLogin.setOnClickListener{

            val firstName = findViewById<TextInputEditText>(R.id.firstnameTextInputEditText).text.toString().trim()
            val lastName = findViewById<TextInputEditText>(R.id.lastnameTextInputEditText).text.toString().trim()
            val email = findViewById<TextInputEditText>(R.id.emailTextInputEditText).text.toString().trim()
            val password = findViewById<TextInputEditText>(R.id.passwordTextInputEditText).text.toString().trim()
            val confirmPassword = findViewById<TextInputEditText>(R.id.confirmPasswordTextInputEditText).text.toString().trim()
            val contactNumber = findViewById<TextInputEditText>(R.id.contactTextInputEditText).text.toString().trim()

            if (firstName.isEmpty()){
                Toast.makeText(this, "First name cannot be empty", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            if (lastName.isEmpty()){
                Toast.makeText(this, "Last name cannot be empty", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            if (email.isEmpty()){
                Toast.makeText(this, "Email cannot be empty", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            if (password.isEmpty()){
                Toast.makeText(this, "Password cannot be empty", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (password.length < 8){
                Toast.makeText(this, "Password must be at least 8 characters", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (confirmPassword.isEmpty()){
                Toast.makeText(this, "Confirm Password cannot be empty", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (contactNumber.isEmpty()){
                Toast.makeText(this, "Contact Number cannot be empty", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (password != confirmPassword){
                Toast.makeText(this, "Password does not match", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            auth = Firebase.auth

            auth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this) { task ->
                    if (task.isSuccessful) {
                        val user = auth.currentUser
                        if (user != null) {
                            val docRef = db.collection("users").document(user.uid)
                            docRef.get()
                                .addOnSuccessListener { document ->
                                    println("HEREEEEEEEEEEEEEEEEEEEEEEEEEEEE")
                                    println(document.data)
                                    if (document.data != null) { // user already has entry in database
                                        Toast.makeText(
                                            baseContext,
                                            "An existing user is already registered with these credentials",
                                            Toast.LENGTH_SHORT,
                                        ).show()

                                        logoutUser()
                                    } else { // user has NO entry and new data will be made using input fields
                                        createUserData(user.uid, firstName, lastName, email, contactNumber)
                                    }
                                }
                                .addOnFailureListener { exception ->
                                    Toast.makeText(
                                        baseContext,
                                        "Authentication failed.",
                                        Toast.LENGTH_SHORT,
                                    ).show()
                                    logoutUser()
                                }
                        } else {
                            logoutUser()
                        }
                    } else {
                        try {
                            throw task.exception!!
                        } catch (e: FirebaseAuthException) {
                            // Firebase Authentication related exceptions
                            val errorCode = e.errorCode
                            val errorMessage = e.message
                            println("FirebaseAuthException: $errorCode - $errorMessage")
                            Toast.makeText(
                                baseContext,
                                errorMessage,
                                Toast.LENGTH_SHORT,
                            ).show()
                        } catch (e: FirebaseAuthUserCollisionException) {
                            // Handle user already exists exception
                            val errorCode = e.errorCode
                            val errorMessage = e.message
                            println("FirebaseAuthUserCollisionException: $errorCode - $errorMessage")
                            Toast.makeText(
                                baseContext,
                                errorMessage,
                                Toast.LENGTH_SHORT,
                            ).show()
                        } catch (e: FirebaseAuthWeakPasswordException) {
                            // Handle user already exists exception
                            val errorCode = e.errorCode
                            val errorMessage = e.message
                            println("FirebaseAuthUserCollisionException: $errorCode - $errorMessage")
                            Toast.makeText(
                                baseContext,
                                errorMessage,
                                Toast.LENGTH_SHORT,
                            ).show()
                        }
                        catch (e: Exception) {
                            // General exception handling
                            println("Exception: ${e.message}")
                            Toast.makeText(
                                baseContext,
                                e.message,
                                Toast.LENGTH_SHORT,
                            ).show()
                        }

                    }
                }

        }

    }

    private fun createUserData(
        uid: String,
        firstName: String,
        lastName: String,
        email: String,
        contactNumber: String
    ) {
        println("ASDJASNDKJANSDKNASKJDNKJASN")

        val userData = hashMapOf(
            "firstname" to firstName,
            "lastname" to lastName,
            "email" to email,
            "contactnum" to contactNumber,
            "role" to "inactive"
        )

        db.collection("users").document(uid)
            .set(userData)
            .addOnSuccessListener {
                Log.d(TAG, "DocumentSnapshot successfully written!")
                Toast.makeText(
                    baseContext,
                    "Your account has been created",
                    Toast.LENGTH_SHORT,
                ).show()
                logoutUser()
                finish()
            }
            .addOnFailureListener { e -> Log.w(TAG, "Error writing document", e)
                Toast.makeText(
                    baseContext,
                    "Network error. Please try again later.",
                    Toast.LENGTH_SHORT,
                ).show()
                logoutUser()
                finish()
            }
    }

    private fun logoutUser() {
        auth.signOut()
    }
}