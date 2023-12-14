package com.example.sinco_v2

import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

class AddNewProductActivity : AppCompatActivity() {

    private val supplierList = mutableListOf<String>() // Initialize this with actual data
    private val supplierListID = mutableListOf<String>() // Initialize this with actual data

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_new_product)

        fetchSupplierData()
        addNewProduct()
        setListeners()
        setupCategorySpinner()
        setupUnitsSpinner()


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

                                // Clear TextInputLayouts after adding product
                                findViewById<TextInputEditText>(R.id.productNameTextInputEditText)?.text?.clear()
                                findViewById<TextInputEditText>(R.id.skuIDTextInputEditText)?.text?.clear()
                                findViewById<TextInputEditText>(R.id.quantityTextInputEditText)?.text?.clear()
                                findViewById<TextInputEditText>(R.id.sizeTextInputEditText)?.text?.clear()
                                findViewById<TextInputEditText>(R.id.priceTextInputEditText)?.text?.clear()
                                findViewById<TextInputEditText>(R.id.productIDTextInputEditText)?.text?.clear()
                                findViewById<TextInputEditText>(R.id.costTextInputEditText)?.text?.clear()

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
                this,
                "Please fill in all product information fields",
                Toast.LENGTH_SHORT
            ).show()
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

    private fun setListeners() {
        val addButton = findViewById<Button>(R.id.buttonAdd)

        addButton.setOnClickListener {
            addNewProduct()
        }
    }
}
