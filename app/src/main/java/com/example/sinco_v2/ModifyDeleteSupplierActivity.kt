package com.example.sinco_v2

import android.content.ContentValues
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.firestore.FirebaseFirestore

class ModifyDeleteSupplierActivity : AppCompatActivity() {
    private lateinit var supplierName: TextInputEditText
    private lateinit var supplierEmail: TextInputEditText
    private lateinit var supplierContact: TextInputEditText
    private lateinit var supplierAddress: TextInputEditText

    private val db = FirebaseFirestore.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_modify_delete_supplier)

        val ib_back_icon = findViewById<ImageButton>(R.id.ib_back_icon)
        ib_back_icon.setOnClickListener {
            finish()
        }
        supplierName = findViewById(R.id.supplierNameTextInputEditText)
        supplierEmail = findViewById(R.id.supplierEmailTextInputEditText)
        supplierContact = findViewById(R.id.supplierContactTextInputEditText)
        supplierAddress = findViewById(R.id.supplierAddressTextInputEditText)

        val supplierID = intent.getStringExtra("supplierID")

        if (supplierID != null) {
            val docRef = db.collection("suppliers").document(supplierID)
            docRef.get()
                .addOnSuccessListener { document ->
                    if (document != null) {
                        val name = document.getString("name")
                        val email = document.getString("email")
                        val contact = document.get("contactnum").toString()
                        val address = document.getString("address")

                        supplierName.setText(name)
                        supplierEmail.setText(email)
                        supplierContact.setText(contact)
                        supplierAddress.setText(address)


                    } else {
                        Log.d(ContentValues.TAG, "No such document")
                    }
                }
                .addOnFailureListener { exception ->
                    Log.d(ContentValues.TAG, "get failed with ", exception)
                }
        }

        val buttonUpdate = findViewById<Button>(R.id.btn_update)
        buttonUpdate.setOnClickListener {
            updateSupplierDatabase()
        }

    }

    private fun updateSupplierDatabase() {
        val updatedName = supplierName.text.toString()
        val updatedEmail = supplierEmail.text.toString()
        val updatedContact = supplierContact.text.toString()
        val updatedAddress = supplierAddress.text.toString()

        val supplierID = intent.getStringExtra("supplierID")

        if (supplierID != null) {
            val docRef = db.collection("suppliers").document(supplierID)

            // Create a map with the updated data
            val updatedData = mapOf(
                "name" to updatedName,
                "email" to updatedEmail,
                "contactnum" to updatedContact,
                "address" to updatedAddress
            )

            // Update the document in Firestore
//            docRef.update(updatedData)
//                .addOnSuccessListener {
//                    updateProductsSupplierInfo(supplierID, updatedDataSupplier)
//                    Log.d(ContentValues.TAG, "DocumentSnapshot successfully updated!")
//                    // You can add additional actions after a successful update if needed
//                }
//                .addOnFailureListener { e ->
//                    Log.w(ContentValues.TAG, "Error updating document", e)
//                    // Handle the error
//                }
        }
    }

    private fun updateProductsSupplierInfo(
        supplierID: String,
        updatedDataSupplier: Map<String, Any>
    ) {
        val productsRef = db.collection("products").whereEqualTo("supplier", supplierID)

        productsRef.get()
            .addOnSuccessListener { products ->
                for (product in products) {
                    val productID = product.id
                    val docRefProduct = db.collection("products").document(productID)

                    // Update the supplier information in the products collection
                    docRefProduct.update("supp", updatedDataSupplier)
                        .addOnSuccessListener {
                            Log.d(ContentValues.TAG, "Product Document successfully updated!")
                            // You can add additional actions after a successful product update if needed
                        }
                        .addOnFailureListener { e ->
                            Log.w(ContentValues.TAG, "Error updating product document", e)
                            // Handle the error
                        }
                }
            }
            .addOnFailureListener { e ->
                Log.w(ContentValues.TAG, "Error getting products associated with the supplier", e)
                // Handle the error
            }

    }
}