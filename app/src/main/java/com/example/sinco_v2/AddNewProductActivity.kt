package com.example.sinco_v2

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.View
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.google.android.material.card.MaterialCardView
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream
import java.util.UUID

class AddNewProductActivity : AppCompatActivity() {

    private val supplierList = mutableListOf<String>() // Initialize this with actual data
    private val supplierListID = mutableListOf<String>()// Initialize this with actual data

    private lateinit var imageView: ImageView
    private lateinit var imageSrcURI: Uri
    private lateinit var imageSrcBitmap: Bitmap
    private lateinit var button_upload: MaterialCardView

    private val MY_PERMISSIONS_REQUEST_CAMERA = 345
    private val MY_PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE = 456
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_new_product)

        fetchSupplierData()
        setListeners()
        setupCategorySpinner()
        setupUnitsSpinner()

        val ib_back_icon = findViewById<ImageButton>(R.id.ib_back_icon)
        ib_back_icon.setOnClickListener {
            finish()
        }
        imageView = findViewById(R.id.imageView)
        button_upload = findViewById(R.id.button_upload)
        button_upload.setOnClickListener {
            selectImage()
        }
    }

    private fun setupCategorySpinner() {
        val predefinedCategories =
            mutableListOf("Local Beer", "Gin", "Liquor", "Spirits", "Custom Category")
        val categorySpinner = findViewById<Spinner>(R.id.spinnerCategory)

        val categoryAdapter =
            ArrayAdapter(this, android.R.layout.simple_spinner_item, predefinedCategories)
        categoryAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        categorySpinner.adapter = categoryAdapter

        categorySpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>?,
                view: View?,
                position: Int,
                id: Long
            ) {
                handleCategoryItemSelected(categorySpinner)
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {
                // Handle when nothing is selected
            }
        }
    }

    private fun handleCategoryItemSelected(categorySpinner: Spinner) {
        val selectedItem = categorySpinner.selectedItem.toString()
        if (selectedItem == "Custom Category") {
            categorySpinner.setSelection(0)
            showCustomCategoryDialog()
        }
    }

    private fun showCustomCategoryDialog() {
        val dialogView =
            layoutInflater.inflate(R.layout.custom_dialog_layout_category, null)
        val editTextCategory: EditText =
            dialogView.findViewById(R.id.customCategoryTextInputEditText)
        val buttonCategory: Button = dialogView.findViewById(R.id.buttonCategory)

        val dialog = AlertDialog.Builder(this@AddNewProductActivity)
            .setView(dialogView)
            .create()

        buttonCategory.setOnClickListener {
            if (editTextCategory.text.toString().isNotEmpty()) {
                val newCategory = editTextCategory.text.toString()
                handleCustomCategoryAdded(newCategory)
                dialog.dismiss()
            }
        }

        dialog.show()
    }

    private fun handleCustomCategoryAdded(newCategory: String) {
        val predefinedCategories =
            mutableListOf("Local Beer", "Gin", "Liquor", "Spirits", "Custom Category")
        predefinedCategories.add(predefinedCategories.size - 1, newCategory)

        val categorySpinner = findViewById<Spinner>(R.id.spinnerCategory)
        val categoryAdapter =
            ArrayAdapter(this, android.R.layout.simple_spinner_item, predefinedCategories)
        categoryAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        categorySpinner.adapter = categoryAdapter
        categorySpinner.setSelection(predefinedCategories.indexOf(newCategory))
    }


    private fun setupUnitsSpinner() {
        val spinner: Spinner = findViewById(R.id.spinnerUnits)
        val unitsAdapter = ArrayAdapter.createFromResource(
            this,
            R.array.units,
            android.R.layout.simple_spinner_item
        )
        unitsAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinner.adapter = unitsAdapter
        spinner.setSelection(0)
        spinner.setOnItemSelectedListener(object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>?,
                view: View?,
                position: Int,
                id: Long
            ) {
                // Handle item selection if needed
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {
                // Handle the case where nothing is selected if needed
            }
        })
    }


    private fun addNewProduct() {
        val prdID =
            findViewById<TextInputEditText>(R.id.productIDTextInputEditText).text.toString()

        if (!prdID.matches(Regex("\\d{12}"))) {
            // Product ID format is invalid
            Toast.makeText(
                baseContext,
                "Product ID format is invalid. It must be a 12-digit integer.",
                Toast.LENGTH_SHORT
            ).show()
            return
        }

        val prdname =
            findViewById<TextInputEditText>(R.id.productNameTextInputEditText)?.text.toString()
        val category = findViewById<Spinner>(R.id.spinnerCategory)?.selectedItem.toString()
        val supplier = findViewById<Spinner>(R.id.spinnerSupplier)?.selectedItem.toString()
        val sku = findViewById<TextInputEditText>(R.id.skuIDTextInputEditText)?.text.toString()
        val cost = findViewById<TextInputEditText>(R.id.costTextInputEditText)?.text.toString()
        val quantity =
            findViewById<TextInputEditText>(R.id.quantityTextInputEditText)?.text.toString()
        val size = findViewById<TextInputEditText>(R.id.sizeTextInputEditText)?.text.toString()
        val units = findViewById<Spinner>(R.id.spinnerUnits)?.selectedItem.toString()
        val price =
            findViewById<TextInputEditText>(R.id.priceTextInputEditText)?.text.toString()
        val supplierID = supplierListID[supplierList.indexOf(supplier)]

        CoroutineScope(IO).launch {
            var firebaseImageSrc = ""
            if (::imageSrcURI.isInitialized) {
                firebaseImageSrc = async { uploadImageToFirebase(imageSrcURI) }.await()
            } else if (::imageSrcBitmap.isInitialized) {
                firebaseImageSrc = async { uploadBitmapToFirebase(imageSrcBitmap) }.await()
            }

            if (prdname.isNotEmpty() && supplier.isNotEmpty() && sku.isNotEmpty() && cost.isNotEmpty() && size.isNotEmpty()) {
                val intQty: Int = quantity.toIntOrNull() ?: 0
                val intSize: Int = size.toIntOrNull() ?: 0
                val floatPrice: Float = price.toFloatOrNull() ?: 0.0f
                val floatCost: Float = cost.toFloatOrNull() ?: 0f

                val data = hashMapOf(
                    "prdnme" to prdname,
                    "ctg" to category,
                    "supp" to supplier,
                    "sku" to sku,
                    "cost" to floatCost,
                    "qnty" to intQty,
                    "units" to units,
                    "size" to intSize,
                    "price" to floatPrice,
                    "imgSrc" to firebaseImageSrc,
                    "supplierID" to supplierID,
                )

                val db = Firebase.firestore
                val productCollectionRef = db.collection("products")

                productCollectionRef.document(prdID)
                    .get()
                    .addOnSuccessListener { documentSnapshot ->
                        if (documentSnapshot.exists()) {
                            Toast.makeText(
                                baseContext,
                                "Product with ID $prdID already exists",
                                Toast.LENGTH_SHORT
                            ).show()
                        } else {
                            // Add the product to the database
                            db.collection("products")
                                .document(prdID)
                                .set(data)
                                .addOnSuccessListener {
                                    Toast.makeText(
                                        baseContext,
                                        "Added Product!",
                                        Toast.LENGTH_SHORT
                                    ).show()

                                        findViewById<TextInputEditText>(R.id.productNameTextInputEditText)?.text?.clear()
                                        findViewById<TextInputEditText>(R.id.skuIDTextInputEditText)?.text?.clear()
                                        findViewById<TextInputEditText>(R.id.quantityTextInputEditText)?.text?.clear()
                                        findViewById<TextInputEditText>(R.id.sizeTextInputEditText)?.text?.clear()
                                        findViewById<TextInputEditText>(R.id.priceTextInputEditText)?.text?.clear()
                                        findViewById<TextInputEditText>(R.id.productIDTextInputEditText)?.text?.clear()
                                        findViewById<TextInputEditText>(R.id.costTextInputEditText)?.text?.clear()

                                        // Clear ImageView after adding product

                                    // Update the products list for the supplier
                                    val products = mapOf(
                                        "id" to prdID,
                                        "name" to prdname
                                    )
                                    val supplierRef =
                                        db.collection("suppliers").document(supplierID)
                                    supplierRef.update("products", FieldValue.arrayUnion(products))

                                }
                                .addOnFailureListener { e ->
                                    Toast.makeText(
                                        baseContext,
                                        "Failed to add Product!",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                }
                        }
                    }
                    .addOnFailureListener { e ->
                        Toast.makeText(
                            baseContext,
                            "Failed to check product ID existence",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
            } else {
                Toast.makeText(
                    baseContext,
                    "Please fill in all product information fields",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }
        private fun fetchSupplierData() {
            val db = FirebaseFirestore.getInstance()

            db.collection("suppliers")
                .get()
                .addOnSuccessListener { result ->
                    for (document in result) {
                        val supplierName = document.data["name"] as String
                        val supplierID = document.id

                        supplierList.add(supplierName)
                        supplierListID.add(supplierID)
                    }

                    // Set up the supplier spinner with the fetched data
                    val supplierSpinner = findViewById<Spinner>(R.id.spinnerSupplier)
                    val supplierAdapter =
                        ArrayAdapter(this, android.R.layout.simple_spinner_item, supplierList)
                    supplierAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                    supplierSpinner.adapter = supplierAdapter
                }
                .addOnFailureListener { exception ->
                    Toast.makeText(
                        baseContext,
                        "Failed to fetch supplier data: $exception",
                        Toast.LENGTH_SHORT
                    ).show()
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
                    this@AddNewProductActivity,
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
                        this@AddNewProductActivity,
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

        private fun setListeners() {
            val addButton = findViewById<Button>(R.id.buttonAdd)

            addButton.setOnClickListener {
                addNewProduct()
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
                val intent =
                    Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
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
    }

