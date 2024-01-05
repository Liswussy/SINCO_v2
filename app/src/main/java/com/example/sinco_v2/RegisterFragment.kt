package com.example.sinco_v2

import android.Manifest
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.os.Parcel
import android.os.Parcelable
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.Button
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.zxing.integration.android.IntentIntegrator
import com.google.zxing.integration.android.IntentResult
import com.journeyapps.barcodescanner.ScanContract
import com.journeyapps.barcodescanner.ScanIntentResult
import com.journeyapps.barcodescanner.ScanOptions
import com.squareup.picasso.Picasso


class RegisterFragment : Fragment() {
    private lateinit var linearlayout2: LinearLayout
    private lateinit var searchInputEditText: TextInputEditText

    private lateinit var scanButton: ImageView
    private lateinit var productManager: ProductManager
    private val CAMERA_PERMISSION_REQUEST_CODE = 101

    private val requestPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
            if (isGranted) {
                startBarcodeScanner()
            } else {
                // Handle permission denied
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        productManager = ProductManager(requireContext())

        arguments?.let {
            val productList = it.getSerializable("productList") as? ArrayList<Product> ?: ArrayList()
            if (productList != null) {
                productManager.setProductList(productList)
            }
        }



    }

    private fun checkCameraPermission(): Boolean {
        return ContextCompat.checkSelfPermission(
            requireContext(),
            Manifest.permission.CAMERA
        ) == PackageManager.PERMISSION_GRANTED
    }

    private fun requestCameraPermission() {
        if (ActivityCompat.shouldShowRequestPermissionRationale(
                requireActivity(),
                Manifest.permission.CAMERA
            ))
        {
        } else {
            requestPermissionLauncher.launch(Manifest.permission.CAMERA)
        }
    }

    private fun startBarcodeScanner() {
        barcodeLauncher.launch(ScanOptions())
    }

    private val barcodeLauncher = registerForActivityResult<ScanOptions, ScanIntentResult>(
        ScanContract()
    ) { result: ScanIntentResult ->
        if (result.contents == null) {
            Toast.makeText(requireContext(), "Scan Failed", Toast.LENGTH_LONG).show()
        } else {
            val barcodeContents = result.contents
            getProduct(barcodeContents)
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == CAMERA_PERMISSION_REQUEST_CODE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                startBarcodeScanner()
            } else {
                // Handle permission denied
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_register, container, false)

        linearlayout2 = view.findViewById(R.id.linearlayout2)

        showProducts()

        searchInputEditText = view.findViewById(R.id.searchEditText)

        val searchInputLayout = view.findViewById<TextInputLayout>(R.id.searchInputLayout)
        searchInputLayout.setEndIconOnClickListener {
            // Clear the text and show all products
            searchInputEditText.text = null
            linearlayout2.removeAllViews()
            showProducts()
            // Hide the keyboard
            hideKeyboard()
        }

        // Set an OnEditorActionListener to handle the EditorInfo.IME_ACTION_DONE event
        searchInputEditText.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                handleSearch()
                // Manually clear the text after handling the search
                return@setOnEditorActionListener true
            }
            false
        }

        val buttonPayment = view.findViewById<Button>(R.id.buttonPayment)
        buttonPayment.setOnClickListener {
            val selectedProducts = productManager.getProductList()

            if (selectedProducts.isNotEmpty()) {
                val intent = Intent(requireContext(), OrderSummaryActivity::class.java)
                intent.putExtra("productList", ArrayList(selectedProducts))
                startActivity(intent)
            } else {
                // Show a message or handle the case where no products are selected
                Toast.makeText(requireContext(), "Please select at least one product.", Toast.LENGTH_SHORT).show()
            }
        }

        scanButton = view.findViewById(R.id.scanButton)
        scanButton.setOnClickListener {
            if (checkCameraPermission()) {
                startBarcodeScanner()
            } else {
                requestCameraPermission()
            }
        }

        return view
    }

    private fun handleSearch() {
        val query = searchInputEditText.text.toString()
        filterProducts(query)
        hideKeyboard()
    }
    private fun hideKeyboard() {
        val imm =
            requireContext().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(requireView().windowToken, 0)
    }

    private fun showProducts(){
        val db = Firebase.firestore
        val productsCollection = db.collection("products")

        productsCollection.get()
            .addOnSuccessListener { result ->
                for (document in result){
                    val productID = document.id
                    val productImgSrc = document.getString("imgSrc")
                    val productName = document.getString("prdnme")
                    val price = document.getDouble("price")


                    val inflater = LayoutInflater.from(requireContext())
                    val customItemView =
                        inflater.inflate(R.layout.register_items, linearlayout2, false)

                    val imageView = customItemView.findViewById<ImageView>(R.id.imageView)
                    val productTextView = customItemView.findViewById<TextView>(R.id.productTextView)
                    val priceTextView = customItemView.findViewById<TextView>(R.id.priceTextView)

                    Picasso.get()
                        .load(productImgSrc)
                        .placeholder(R.drawable.beer_icon) // Placeholder image resource
                        .error(R.drawable.beer_icon) // Error image resource (optional)
                        .into(imageView)

                    productTextView.text = productName
                    priceTextView.text = "Php " + price?.let { roundToTwoDecimalPlaces(it) }

                    customItemView.setOnClickListener {
                        val newProduct = Product(productID, productName, price, 1)
                        productManager.addProduct(newProduct)
                    }

                    linearlayout2.addView(customItemView)
                }
            }
    }

    private fun filterProducts(name: String){
        val db = Firebase.firestore
        val productsCollection = db.collection("products")

        val lowercaseQuery = name.toLowerCase()

        productsCollection.get()
            .addOnSuccessListener { documents ->
                linearlayout2.removeAllViews()
                for (doc in documents) {
                    val docID = doc.id
                    val productName = doc.get("prdnme") as? String ?: ""
                    val imgSrc = doc.getString("imgSrc")
                    val category = doc.get("ctg") as? String ?: ""
                    val price = doc.get("price") as? Double ?: 0.0
                    val supplier = doc.get("supp") as? String ?: ""
                    val size = doc.get("size") as? Double ?: 0.0
                    val cost = doc.get("cost") as? Double ?: 0.0
                    val quantity = (doc.get("qnty") as? Number)?.toInt() ?: 0

                    // Use lowercase for comparison, but keep the original data for display
                    val lowercaseProductName = productName.toLowerCase()
                    val lowercaseCategory = category.toLowerCase()
                    val lowercaseSupplier = supplier.toLowerCase()

                    // Check if any of the fields contains the query
                    if (lowercaseProductName.contains(lowercaseQuery) ||
                        lowercaseCategory.contains(lowercaseQuery) ||
                        lowercaseSupplier.contains(lowercaseQuery)
                    ) {

                        val inflater = LayoutInflater.from(requireContext())
                        val customItemView =
                            inflater.inflate(R.layout.register_items, linearlayout2, false)

                        val imageView = customItemView.findViewById<ImageView>(R.id.imageView)
                        val productTextView = customItemView.findViewById<TextView>(R.id.productTextView)
                        val priceTextView = customItemView.findViewById<TextView>(R.id.priceTextView)

                        Picasso.get()
                            .load(imgSrc)
                            .placeholder(R.drawable.beer_icon) // Placeholder image resource
                            .error(R.drawable.beer_icon) // Error image resource (optional)
                            .into(imageView)

                        productTextView.text = productName
                        priceTextView.text = "Php " + price?.let { roundToTwoDecimalPlaces(it) }

                        customItemView.setOnClickListener {
                            val newProduct = Product(docID, productName, price, 1)
                            productManager.addProduct(newProduct)
                        }

                        linearlayout2.addView(customItemView)

                    }
                }
            }
    }

    fun getProduct(productID: String) {
        val db = Firebase.firestore
        val docRef = db.collection("products").document(productID)
        docRef.get()
            .addOnSuccessListener { document ->
                if (document.data != null) {
                    Log.v(ContentValues.TAG, document.toString());
                    val productName = document.getString("prdnme")
                    val price = document.getDouble("price")

                    val newProduct = Product(document.id, productName, price, 1)
                    productManager.addProduct(newProduct)

                } else {
                    showToast("Product does not exist")

                }
            }
            .addOnFailureListener { exception ->
                showToast("Database error")

            }
    }

    private fun showToast(message: String) {
        Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
    }

    fun roundToTwoDecimalPlaces(value: Double): String {
        val roundedValue = String.format("%.2f", value)
        return roundedValue
    }
}

data class Product(
    val productID: String?,
    val productName: String?,
    val price: Double?,
    var quantity: Int?
) :
    Parcelable {
    constructor(parcel: Parcel) : this(
        parcel.readString(),
        parcel.readString(),
        parcel.readValue(Double::class.java.classLoader) as? Double,
        parcel.readValue(Double::class.java.classLoader) as? Int
    ) {
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(productID)
        parcel.writeString(productName)
        parcel.writeValue(price)
        parcel.writeValue(quantity)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<Product> {
        override fun createFromParcel(parcel: Parcel): Product {
            return Product(parcel)
        }

        override fun newArray(size: Int): Array<Product?> {
            return arrayOfNulls(size)
        }
    }

}

class ProductManager(private val context: Context) {
    private var productList = mutableListOf<Product>()

    fun addProduct(product: Product) {
        // Check if the product entry already exists
        val existingProduct = productList.find { it.productID == product.productID }

        if (existingProduct == null) {
            productList.add(product)
            showToast("Product added: ${product.productName}", 500)
        } else {
            showToast("Product with ID ${product.productID} already exists.", 500)
        }
    }

    fun getProductList(): MutableList<Product> {
        return productList
    }

    fun setProductList(array: ArrayList<Product>) {
        productList = array
        return
    }

    private fun showToast(message: String, durationMillis: Int) {
        val toast = Toast.makeText(context, message, Toast.LENGTH_SHORT)
        toast.duration = durationMillis
        toast.show()
    }


}