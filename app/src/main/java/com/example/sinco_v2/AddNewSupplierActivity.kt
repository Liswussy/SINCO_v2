package com.example.sinco_v2

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.ImageButton
import android.widget.Toast
import androidx.core.content.ContentProviderCompat.requireContext
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

class AddNewSupplierActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_new_supplier)
        //back to Frag
        val backButton = findViewById<ImageButton>(R.id.ib_back_icon)
        backButton.setOnClickListener {
            finish()
        }
        val addButton = findViewById<Button>(R.id.btn_add)
        addButton.setOnClickListener {
            addNewSupplier()
        }

    }

    private val db = Firebase.firestore
    private fun addNewSupplier() {
        val supplierName = findViewById<TextInputEditText>(R.id.supplierNameTextInputEditText).text.toString()
        val address = findViewById<TextInputEditText>(R.id.addressTextInputEditText).text.toString()
        val contactNumber = findViewById<TextInputEditText>(R.id.contactNumberTextInputEditText).text.toString()
        val emailAddress = findViewById<TextInputEditText>(R.id.emailAddressTextInputEditText).text.toString()

        if (supplierName.isNotEmpty() && address.isNotEmpty() && contactNumber.isNotEmpty() && emailAddress.isNotEmpty()) {
            val data = hashMapOf(
                "address" to address,
                "contactnum" to contactNumber,
                "name" to supplierName,
                "email" to emailAddress
            )

            // Add a new document to the "suppliers" collection with an automatically generated ID
            db.collection("suppliers")
                .add(data)
                .addOnSuccessListener { documentReference ->
                    onAddSupplierSuccess()
                }
                .addOnFailureListener { e ->
                    onAddSupplierFailure()
                }
        } else {
            Toast.makeText(this, "Please fill in all supplier information fields", Toast.LENGTH_SHORT).show()
        }
    }

    private fun onAddSupplierSuccess() {
        Toast.makeText(this, "Added Supplier!", Toast.LENGTH_SHORT).show()
        clearSupplierFields()
    }

    private fun onAddSupplierFailure() {
        Toast.makeText(this, "Failed to add Supplier!", Toast.LENGTH_SHORT).show()
    }

    private fun clearSupplierFields() {
        findViewById<TextInputEditText>(R.id.supplierNameTextInputEditText).text?.clear()
        findViewById<TextInputEditText>(R.id.addressTextInputEditText).text?.clear()
        findViewById<TextInputEditText>(R.id.contactNumberTextInputEditText).text?.clear()
        findViewById<TextInputEditText>(R.id.emailAddressTextInputEditText).text?.clear()
    }
}
