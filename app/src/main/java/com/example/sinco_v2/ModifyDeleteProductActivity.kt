package com.example.sinco_v2

import android.Manifest
import android.app.Activity
import android.content.ContentValues
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.net.Uri
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.View
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import com.google.android.material.card.MaterialCardView
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.firestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream
import java.util.UUID

class ModifyDeleteProductActivity : AppCompatActivity() {

    private lateinit var inputProductId: TextInputEditText
    private lateinit var inputProductName: TextInputEditText
    private lateinit var inputSize: TextInputEditText
    private lateinit var sp_category: Spinner
    private lateinit var inputQuantity: TextInputEditText
    private lateinit var inputPrice: TextInputEditText
    private lateinit var inputSku: TextInputEditText

    private lateinit var costTextInputEditText: TextInputEditText
    private lateinit var button_upload: MaterialCardView
    private lateinit var imageView: ImageView
    private lateinit var imageSrcURI: Uri
    private lateinit var imageSrcBitmap: Bitmap
    private val CUSTOM_CATEGORY = "Custom Category"


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
        sp_category = findViewById(R.id.sp_category)
        inputQuantity = findViewById(R.id.input_quantity)
        inputPrice = findViewById(R.id.input_price)
        costTextInputEditText = findViewById(R.id.costTextInputEditText)
        inputSku = findViewById(R.id.input_sku)
        imageView = findViewById(R.id.imageView)
        button_upload = findViewById(R.id.button_upload)
        button_upload.setOnClickListener {
            selectImage()
        }
        val db = Firebase.firestore
        val docID = intent.getStringExtra("docID")
        if (docID != null) {
            val docRef = db.collection("products").document(docID)
            docRef.get()
                .addOnSuccessListener { document ->
                    if (document != null) {
                        val prdnme = document.getString("prdnme")
                        val size = document.getDouble("size").toString()
                        val qnty = document.getDouble("qnty")?.toInt() ?: 0
                        val price = document.getDouble("price").toString()
                        val cost = document.getDouble("cost").toString()
                        val sku = document.getString("sku")

                        inputProductId.setText(docID)
                        inputProductName.setText(prdnme)
                        inputSize.setText(size)
                        inputQuantity.setText(qnty.toString())
                        costTextInputEditText.setText(cost)
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
        updateProductInformation()

        val btn_delete = findViewById<Button>(R.id.btn_delete)
        btn_delete.setOnClickListener {
            val docID = intent.getStringExtra("docID")

            if (docID != null) {
                val docRef = db.collection("products").document(docID)
                docRef.get()
                    .addOnSuccessListener { documentSnapshot ->
                        if (documentSnapshot.exists()) {
                            val supplierID = documentSnapshot.getString("supplierID")
                            val productName = documentSnapshot.getString("prdnme")

                            if (supplierID != null && productName != null) {
                                deleteProductAndReferences(docID, supplierID, productName)
                            } else {
                                // Handle the case where supplierID or productName is null
                                Toast.makeText(
                                    this@ModifyDeleteProductActivity,
                                    "Error retrieving supplierID or productName",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                        } else {
                            // Handle the case where the document does not exist
                            Toast.makeText(
                                this@ModifyDeleteProductActivity,
                                "Product document does not exist",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    }
                    .addOnFailureListener { exception ->
                        // Handle the case where there's an error retrieving the document
                        Toast.makeText(
                            this@ModifyDeleteProductActivity,
                            "Error retrieving product document: ${exception.message}",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
            }
        }



    }

    val PICK_IMAGE_REQUEST = 1
    val CAMERA_REQUEST = 2

    fun selectImage() {
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
        if (resultCode == RESULT_OK) {
            when (requestCode) {
                PICK_IMAGE_REQUEST -> {
                    // Handle image selection from gallery
                    val selectedImage = data?.data
                    imageView.setImageURI(selectedImage)
                    if (selectedImage != null) {
                        imageSrcURI = selectedImage
                    }
                }

                CAMERA_REQUEST -> {
                    // Handle captured photo from the camera
                    val photo = data?.extras?.get("data") as Bitmap
                    imageView.setImageBitmap(photo)
                    imageSrcBitmap = photo

                }
            }
        }

    }

    private fun checkAndRequestCameraPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
            != PackageManager.PERMISSION_GRANTED
        ) {
            // Permission is not granted, request it
            requestPermissions(
                arrayOf(Manifest.permission.CAMERA),
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
                Manifest.permission.READ_EXTERNAL_STORAGE
            )
            != PackageManager.PERMISSION_GRANTED
        ) {
            // Permission is not granted, request it
            if (Build.VERSION.SDK_INT >= 33) {
                requestPermissions(
                    arrayOf(Manifest.permission.READ_MEDIA_IMAGES),
                    MY_PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE
                )
            } else {
                requestPermissions(
                    arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE),
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


    private fun updateProductInformation() {
        val updateButton = findViewById<Button>(R.id.btn_update)
        updateButton.setOnClickListener {
            // Get the data
            val prdname = findViewById<EditText>(R.id.input_product_name).text.toString()
            val category = findViewById<Spinner>(R.id.sp_category).selectedItem.toString()
            val supplier = findViewById<Spinner>(R.id.sp_supplier).selectedItem.toString()
            val sku = findViewById<EditText>(R.id.input_sku).text.toString()
            val cost = findViewById<EditText>(R.id.costTextInputEditText).text.toString().toDouble()
            val quantity = findViewById<EditText>(R.id.input_quantity).text.toString().toInt()
            val size = findViewById<EditText>(R.id.input_size).text.toString().toDouble()
            val units = findViewById<Spinner>(R.id.sp_units).selectedItem.toString()
            val price = findViewById<EditText>(R.id.input_price).text.toString().toDouble()
            val supplierID = supplierListID[supplierList.indexOf(supplier)]

            CoroutineScope(IO).launch {
                var firebaseImageSrc = ""
                if (::imageSrcURI.isInitialized) {
                    firebaseImageSrc = async { uploadImageToFirebase(imageSrcURI) }.await()
                } else if (::imageSrcBitmap.isInitialized) {
                    firebaseImageSrc = async { uploadBitmapToFirebase(imageSrcBitmap) }.await()
                }

                // Update product data
                val docID = intent.getStringExtra("docID")
                if (docID != null) {
                    val prodRef = db.collection("products").document(docID)

                    val updateData = mutableMapOf<String, Any>()
                    updateData["prdnme"] = prdname
                    updateData["ctg"] = category
                    updateData["supp"] = supplier
                    updateData["sku"] = sku
                    updateData["qnty"] = quantity
                    updateData["cost"] = cost
                    updateData["size"] = size
                    updateData["units"] = units
                    updateData["price"] = price
                    updateData["supplierID"] = supplierID

                    if (::imageSrcURI.isInitialized) {
                        updateData["imgSrc"] = firebaseImageSrc
                    } else if (::imageSrcBitmap.isInitialized) {
                        updateData["imgSrc"] = firebaseImageSrc
                    }

                    prodRef.update(updateData)
                        .addOnSuccessListener {
                            // Update successful
                            Toast.makeText(
                                this@ModifyDeleteProductActivity,
                                "Successfully updated product",
                                Toast.LENGTH_SHORT
                            ).show()

                            // Update supplier data
                            val supplierID = supplierListID[findViewById<Spinner>(R.id.sp_supplier).selectedItemPosition]
                            val supplierRef = db.collection("suppliers").document(supplierID)

                            val oldProduct = mapOf(
                               "id" to docID,
                                "name" to prdname
                            )
                            println(oldProduct)
                            val updatedProduct = mapOf(
                                "id" to docID,
                                "name" to prdname
                            )
                            // Update the product field in the supplier collection
                            supplierRef.update(
                                "products", FieldValue.arrayRemove(oldProduct),
                                "products", FieldValue.arrayUnion(updatedProduct),
                                "lastUpdated", System.currentTimeMillis()
                            )
                                .addOnSuccessListener {
                                    Toast.makeText(
                                        this@ModifyDeleteProductActivity,
                                        "Successfully updated supplier",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                }
                                .addOnFailureListener { e ->
                                    Toast.makeText(
                                        this@ModifyDeleteProductActivity,
                                        "Error updating supplier: ${e.message}",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                    Log.e("FirestoreUpdate", "Error updating supplier", e)
                                }
                        }
                        .addOnFailureListener { e ->
                            // Update failed
                            Toast.makeText(
                                this@ModifyDeleteProductActivity,
                                "Error updating product: ${e.message}",
                                Toast.LENGTH_SHORT
                            ).show()
                            Log.e("FirestoreUpdate", "Error updating product", e)
                        }
                }
            }
        }
    }



    private suspend fun uploadImageToFirebase(imageUri: Uri?): String {
        val imageSrc = CompletableDeferred<String>()

        val storage = FirebaseStorage.getInstance()
        val storageRef = storage.reference
        val currentUser = FirebaseAuth.getInstance().currentUser

        val inputStream = imageUri?.let { this.contentResolver?.openInputStream(it) }
        val buffer = ByteArrayOutputStream()
        val bufferSize = 1024
        val bufferData = ByteArray(bufferSize)
        var bytesRead: Int = 0
        var totalBytes = 0

        while (inputStream?.read(bufferData, 0, bufferSize).also {
                if (it != null) {
                    bytesRead = it
                }
            } != -1) {
            buffer.write(bufferData, 0, bytesRead)
            totalBytes += bytesRead
        }

        // Check if the total size exceeds 3MB (3 * 1024 * 1024 bytes)
        if (totalBytes > 3 * 1024 * 1024) {
            withContext(Dispatchers.Main) {
                Toast.makeText(
                    this@ModifyDeleteProductActivity,
                    "Image size must not exceed 3MB",
                    Toast.LENGTH_SHORT
                ).show()
                imageView.setImageResource(R.drawable.image_icon)
            }
            imageSrc.complete("")
        }

        if (imageUri != null) {
            val imageRef =
                storageRef.child("product_images/${UUID.randomUUID()}")

            imageRef.putFile(imageUri)
                .addOnSuccessListener { taskSnapshot ->
                    // Image upload success
                    // You can get the download URL here
                    imageRef.downloadUrl.addOnSuccessListener { uri ->
                        val imageUrl = uri.toString()
                        // Handle the URL as needed, e.g., save it to a database
                        imageSrc.complete(imageUrl)
                    }
                }
                .addOnFailureListener { exception ->
                    // Handle unsuccessful upload
                    Toast.makeText(this, "Failed to Upload Image", Toast.LENGTH_SHORT).show()
                    imageSrc.complete("")
                }
        }

        return imageSrc.await()
    }

    private suspend fun uploadBitmapToFirebase(bitmap: Bitmap): String {
        val imageSrc = CompletableDeferred<String>()

        val storage = FirebaseStorage.getInstance()
        val storageRef = storage.reference
        val currentUser = FirebaseAuth.getInstance().currentUser

        val baos = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos)
        val data = baos.toByteArray()

        // Check if the total size exceeds 3MB (3 * 1024 * 1024 bytes)
        Log.e("IMAGE BITMAP SIZE", data.size.toString())
        if (data.size >= (3 * 1024 * 1024)) {
            withContext(Dispatchers.Main) {
                Toast.makeText(
                    this@ModifyDeleteProductActivity,
                    "Image size must not exceed 3MB",
                    Toast.LENGTH_SHORT
                ).show()
                imageView.setImageResource(R.drawable.image_icon)
            }
            imageSrc.complete("")
        }

        val imageRef = storageRef.child("product_images/${UUID.randomUUID()}")

        val uploadTask = imageRef.putBytes(data)

        uploadTask.addOnSuccessListener { taskSnapshot ->
            // Image upload success
            // You can get the download URL here
            imageRef.downloadUrl.addOnSuccessListener { uri ->
                val imageUrl = uri.toString()
                // Handle the URL as needed, e.g., save it to a database
                imageSrc.complete(imageUrl)
            }.addOnFailureListener { exception ->
                // Handle unsuccessful upload
                Toast.makeText(this, "Failed to Upload Image", Toast.LENGTH_SHORT).show()
                imageSrc.complete("")
            }
        }


        return imageSrc.await()
    }

    private fun fetchSupplierData() {
        // Inside fetchSupplierData() function
        db.collection("suppliers")
            .get()
            .addOnSuccessListener { result ->
                for (document in result) {
                    val supplierName = document.getString("name")
                    val supplierId = document.id
                    if (supplierName != null && !supplierList.contains(supplierName)) {
                        supplierList.add(supplierName)
                        supplierListID.add(supplierId)
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

    private fun deleteProductAndReferences(docID: String, supplierID: String, productName: String) {
        val productsRef = db.collection("products").document(docID)
        val suppliersRef = db.collection("suppliers").document(supplierID)

        // Delete the product document
        productsRef.delete()
            .addOnSuccessListener {
                Toast.makeText(
                    this@ModifyDeleteProductActivity,
                    "Product deleted successfully",
                    Toast.LENGTH_SHORT
                ).show()

                // Update the supplier's array map
                suppliersRef.update(
                    "products",
                    FieldValue.arrayRemove(mapOf("id" to docID, "name" to productName))
                )
                    .addOnSuccessListener {
                        Toast.makeText(
                            this@ModifyDeleteProductActivity,
                            "Supplier's product reference updated successfully",
                            Toast.LENGTH_SHORT
                        ).show()

                        // After deleting references, you can finish the activity or perform any other action.
                        finish()
                    }
                    .addOnFailureListener { e ->
                        Toast.makeText(
                            this@ModifyDeleteProductActivity,
                            "Error updating supplier: ${e.message}",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
            }
            .addOnFailureListener { e ->
                Toast.makeText(
                    this@ModifyDeleteProductActivity,
                    "Error deleting product: ${e.message}",
                    Toast.LENGTH_SHORT
                ).show()
            }
    }


}