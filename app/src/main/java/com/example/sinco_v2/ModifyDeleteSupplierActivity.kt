package com.example.sinco_v2

import android.content.ContentValues
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
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
                        val contact= document.get("contactnum").toString()
                        val address= document.getString("address")

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

    }

}