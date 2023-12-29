package com.example.sinco_v2

import android.app.Activity
import android.content.ContentValues
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.View
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.firestore.FirebaseFirestore

class ModifyDeleteProductActivity : AppCompatActivity() {

    private lateinit var inputProductId: TextInputEditText
    private lateinit var inputProductName: TextInputEditText
    private lateinit var inputSize: TextInputEditText
    private lateinit var inputQuantity: TextInputEditText
    private lateinit var inputPrice: TextInputEditText
    private lateinit var inputSku: TextInputEditText
    private val db = FirebaseFirestore.getInstance()

    private val supplierList = mutableListOf<String>()
    private val supplierListID = mutableListOf<String>()
    private val volumeList = mutableListOf<String>()
    private val categoryList = mutableListOf<String>()

    private lateinit var supplierAdapter: ArrayAdapter<String>
    private lateinit var volumeAdapter: ArrayAdapter<String>
    private lateinit var categoryAdapter: ArrayAdapter<String>

    private val MY_PERMISSIONS_REQUEST_CAMERA = 345
    private val MY_PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE = 456

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_modify_delete_product)

        val ib_back_icon = findViewById<ImageButton>(R.id.ib_back_icon)
        ib_back_icon.setOnClickListener {
            finish()
        }

        inputProductId = findViewById(R.id.input_product_id)
        inputProductName = findViewById(R.id.input_product_name)
        inputSize = findViewById(R.id.input_size)
        inputQuantity = findViewById(R.id.input_quantity)
        inputPrice = findViewById(R.id.input_price)
        inputSku = findViewById(R.id.input_sku)

        val docID = intent.getStringExtra("docID")

        if (docID != null) {
            val docRef = db.collection("products").document(docID)
            docRef.get()
                .addOnSuccessListener { document ->
                    if (document != null) {
                        val prdnme = document.getString("prdnme")
                        val size = document.get("size").toString()
                        val qnty = document.get("qnty").toString()
                        val price = document.get("price").toString()
                        val sku = document.getString("sku")

                        inputProductId.setText(docID)
                        inputProductName.setText(prdnme)
                        inputSize.setText(size)
                        inputQuantity.setText(qnty)
                        inputPrice.setText(price)
                        inputSku.setText(sku)
                    } else {
                        Log.d(ContentValues.TAG, "No such document")
                    }
                }
                .addOnFailureListener { exception ->
                    Log.d(ContentValues.TAG, "get failed with ", exception)
                }
        }

        // Fetch and populate supplier data
        fetchSupplierData()
        fetchCategoryData()
        fetchVolumeData()
    }

    val PICK_IMAGE_REQUEST = 1
    val CAMERA_REQUEST = 2

    fun selectImage(view: View) {
        val options = arrayOf("Select from Gallery", "Take a Photo")
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Select Image")
        builder.setItems(options) { _, which ->
            when (which) {
                0 -> {
                    checkAndRequestExternalStoragePermission()
                }

                1 -> {
                    checkAndRequestCameraPermission()
                }
            }
        }
        builder.show()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK) {
            when (requestCode) {
                PICK_IMAGE_REQUEST -> {
                    // Handle image selection from gallery
                    val selectedImage = data?.data

                    //uploadImageToFirebase(selectedImage)

                }

                CAMERA_REQUEST -> {
                    // Handle captured photo from the camera
                    val photo = data?.extras?.get("data") as Bitmap

                    //uploadBitmapToFirebase(photo)

                }
            }
        }
    }

    private fun checkAndRequestCameraPermission() {
        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.CAMERA)
            != PackageManager.PERMISSION_GRANTED
        ) {
            // Permission is not granted, request it
            requestPermissions(
                arrayOf(android.Manifest.permission.CAMERA),
                MY_PERMISSIONS_REQUEST_CAMERA
            )
        } else {
            // Permission is already granted
            val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
            startActivityForResult(intent, CAMERA_REQUEST)
        }
    }

    private fun checkAndRequestExternalStoragePermission() {
        if (ContextCompat.checkSelfPermission(
                this,
                android.Manifest.permission.READ_EXTERNAL_STORAGE
            )
            != PackageManager.PERMISSION_GRANTED
        ) {
            // Permission is not granted, request it
            if (Build.VERSION.SDK_INT >= 33) {
                requestPermissions(
                    arrayOf(android.Manifest.permission.READ_MEDIA_IMAGES),
                    MY_PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE
                )
            } else {
                requestPermissions(
                    arrayOf(android.Manifest.permission.READ_EXTERNAL_STORAGE),
                    MY_PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE
                )
            }
        } else {
            // Permission is already granted
            val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
            startActivityForResult(intent, PICK_IMAGE_REQUEST)
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        println(requestCode)
        when (requestCode) {

            MY_PERMISSIONS_REQUEST_CAMERA -> {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // Permission granted, proceed with capturing image
                    val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
                    startActivityForResult(intent, CAMERA_REQUEST)
                } else {
                    // Permission denied, handle it gracefully (e.g., show a message to the user)
                    Toast.makeText(this, "Permission Denied", Toast.LENGTH_SHORT).show()
                }
            }

            MY_PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE -> {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // Permission granted, proceed with capturing image
                    val intent = Intent(
                        Intent.ACTION_PICK,
                        MediaStore.Images.Media.EXTERNAL_CONTENT_URI
                    )
                    startActivityForResult(intent, PICK_IMAGE_REQUEST)
                } else {
                    // Permission denied, handle it gracefully (e.g., show a message to the user)
                    Toast.makeText(this, "Permission Denied", Toast.LENGTH_SHORT).show()
                }
            }
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
    }

    private fun fetchSupplierData() {
        // Inside fetchSupplierData() function
        db.collection("products")
            .get()
            .addOnSuccessListener { result ->
                for (document in result) {
                    val supplierName =
                        document.getString("supp") // Use "supp" instead of "supplier"
                    if (supplierName != null && !supplierList.contains(supplierName)) {
                        supplierList.add(supplierName)
                        supplierListID.add(document.id) // Add the corresponding document ID
                    }
                }

                // Set up the supplier spinner with the fetched data
                val supplierSpinner = findViewById<Spinner>(R.id.sp_supplier)
                supplierAdapter =
                    ArrayAdapter(this, android.R.layout.simple_spinner_item, supplierList)
                supplierAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                supplierSpinner.adapter = supplierAdapter

                // Retrieve the selected supplier from the Firestore and set it in the spinner
                val docID = intent.getStringExtra("docID")
                if (docID != null) {
                    val docRef = db.collection("products").document(docID)
                    docRef.get()
                        .addOnSuccessListener { document ->
                            if (document != null) {
                                val selectedSupplier =
                                    document.getString("supp") // Use "supp" instead of "supplier"
                                val selectedIndex = supplierList.indexOf(selectedSupplier)
                                if (selectedIndex != -1) {
                                    supplierSpinner.setSelection(selectedIndex)
                                }
                            }
                        }
                        .addOnFailureListener { exception ->
                            Toast.makeText(
                                baseContext,
                                "Error retrieving product data: $exception",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                }
            }
            .addOnFailureListener { exception ->
                Toast.makeText(
                    baseContext,
                    "Failed to fetch supplier data: $exception",
                    Toast.LENGTH_SHORT
                ).show()
            }
    }

    private fun fetchCategoryData() {
        // Inside fetchCategoryData() function
        db.collection("products")
            .get()
            .addOnSuccessListener { result ->
                for (document in result) {
                    val category =
                        document.getString("ctg") // Use "ctg" instead of "category"
                    if (category != null && !categoryList.contains(category)) {
                        categoryList.add(category)
                    }
                }

                // Set up the category spinner with the fetched data
                val categorySpinner = findViewById<Spinner>(R.id.sp_category)
                categoryAdapter =
                    ArrayAdapter(this, android.R.layout.simple_spinner_item, categoryList)
                categoryAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                categorySpinner.adapter = categoryAdapter

                // Retrieve the selected category from the Firestore and set it in the spinner
                val docID = intent.getStringExtra("docID")
                if (docID != null) {
                    val docRef = db.collection("products").document(docID)
                    docRef.get()
                        .addOnSuccessListener { document ->
                            if (document != null) {
                                val selectedCategory =
                                    document.getString("ctg") // Use "ctg" instead of "category"
                                val selectedIndex = categoryList.indexOf(selectedCategory)
                                if (selectedIndex != -1) {
                                    categorySpinner.setSelection(selectedIndex)
                                }
                            }
                        }
                        .addOnFailureListener { exception ->
                            Toast.makeText(
                                baseContext,
                                "Error retrieving product data: $exception",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                }
            }
            .addOnFailureListener { exception ->
                Toast.makeText(
                    baseContext,
                    "Failed to fetch category data: $exception",
                    Toast.LENGTH_SHORT
                ).show()
            }
    }

    private fun fetchVolumeData() {
        // Inside fetchVolumeData() function
        db.collection("products")
            .get()
            .addOnSuccessListener { result ->
                for (document in result) {
                    val unit = document.getString("units")
                    if (unit != null && !volumeList.contains(unit)) {
                        volumeList.add(unit)
                    }
                }

                // Set up the volume spinner with the fetched data
                val volumeSpinner = findViewById<Spinner>(R.id.sp_units)
                volumeAdapter =
                    ArrayAdapter(this, android.R.layout.simple_spinner_item, volumeList)
                volumeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                volumeSpinner.adapter = volumeAdapter

                // Retrieve the selected unit from the Firestore and set it in the spinner
                val docID = intent.getStringExtra("docID")
                if (docID != null) {
                    val docRef = db.collection("products").document(docID)
                    docRef.get()
                        .addOnSuccessListener { document ->
                            if (document != null) {
                                val selectedUnit = document.getString("units")
                                val selectedIndex = volumeList.indexOf(selectedUnit)
                                if (selectedIndex != -1) {
                                    volumeSpinner.setSelection(selectedIndex)
                                }
                            }
                        }
                        .addOnFailureListener { exception ->
                            Toast.makeText(
                                baseContext,
                                "Error retrieving product data: $exception",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                }
            }
            .addOnFailureListener { exception ->
                Toast.makeText(
                    baseContext,
                    "Failed to fetch unit data: $exception",
                    Toast.LENGTH_SHORT
                ).show()
            }
    }

}
