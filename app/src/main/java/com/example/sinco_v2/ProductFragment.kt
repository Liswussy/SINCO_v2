import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import com.example.sinco_v2.AddNewProductActivity
import com.example.sinco_v2.ModifyDeleteProductActivity
import com.example.sinco_v2.R
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.squareup.picasso.Picasso

class ProductFragment : Fragment() {
    private lateinit var linearlayout1: LinearLayout
    private lateinit var searchInputEditText: TextInputEditText

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_product, container, false)

        linearlayout1 = view.findViewById(R.id.linearlayout1)
        searchInputEditText = view.findViewById(R.id.searchInputEditText)

        showProductInformation()

        val redirectToActivityButton = view.findViewById<ImageButton>(R.id.ib_add_icon)
        redirectToActivityButton.setOnClickListener {
            redirectToActivity(AddNewProductActivity::class.java)
        }

        val searchInputLayout = view.findViewById<TextInputLayout>(R.id.searchInputLayout)
        searchInputLayout.setEndIconOnClickListener {
            // Clear the text and show all products
            searchInputEditText.text = null
            showProductInformation()
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

        return view
    }

    private fun redirectToActivityWithID(activityClass: Class<out FragmentActivity>, docID: String) {
        val intent = Intent(requireContext(), activityClass)
        intent.putExtra("docID", docID)

        val fragmentTransaction = requireActivity().supportFragmentManager.beginTransaction()
        fragmentTransaction.replace(R.id.fragment_container, ProductFragment())
        fragmentTransaction.addToBackStack(null)
        fragmentTransaction.commit()

        startActivity(intent)
    }

    private fun redirectToActivity(activityClass: Class<out FragmentActivity>) {
        val intent = Intent(requireContext(), activityClass)

        val fragmentTransaction = requireActivity().supportFragmentManager.beginTransaction()
        fragmentTransaction.replace(R.id.fragment_container, ProductFragment())
        fragmentTransaction.addToBackStack(null)
        fragmentTransaction.commit()

        startActivity(intent)
    }

    private fun showProductInformation() {
        linearlayout1.removeAllViews()

        val db = Firebase.firestore
        val productsRef = db.collection("products")
        productsRef.get()
            .addOnSuccessListener { documents ->
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

                    val inflater = LayoutInflater.from(requireContext())
                    val customItemView =
                        inflater.inflate(R.layout.prdouct_items, linearlayout1, false)

                    val productIcon: ImageView = customItemView.findViewById(R.id.productIcon)
                    Picasso.get()
                        .load(imgSrc)
                        .placeholder(R.drawable.blue_sales_icon) // Placeholder image resource
                        .error(R.drawable.blue_sales_icon) // Error image resource (optional)
                        .into(productIcon)


                    val productTextView =
                        customItemView.findViewById<TextView>(R.id.productTextView)
                    val productText = productName
                    productTextView.text = productText

                    val categoryTextView =
                        customItemView.findViewById<TextView>(R.id.categoryTextView)
                    val categoryText = category
                    categoryTextView.text = categoryText

                    val priceTextView =
                        customItemView.findViewById<TextView>(R.id.priceTextView)
                    val priceText = "Price: ${price.toString()}"
                    priceTextView.text = priceText

                    val quantityTextView =
                        customItemView.findViewById<TextView>(R.id.quantityTextView)
                    val quantityText = "Quantity: ${quantity.toString()}"
                    quantityTextView.text = quantityText

                    val supplierTextView =
                        customItemView.findViewById<TextView>(R.id.supplierTextView)
                    val supplierText = "Supplier: $supplier"
                    supplierTextView.text = supplierText

                    val sizeTextView =
                        customItemView.findViewById<TextView>(R.id.sizeTextView)
                    val sizeText = "Size: ${size.toString()}"
                    sizeTextView.text = sizeText

                    val costTextView =
                        customItemView.findViewById<TextView>(R.id.costTextView)
                    val costText = "Cost: ${cost.toString()}"
                    costTextView.text = costText

                    customItemView.setOnClickListener{
                        redirectToActivityWithID(ModifyDeleteProductActivity::class.java, docID)
                    }

                    linearlayout1.addView(customItemView)
                }
            }
            .addOnFailureListener {
                // Handle failure
            }
    }

    private fun filterProductInformation(query: String) {
        linearlayout1.removeAllViews()

        val db = Firebase.firestore
        val productsRef = db.collection("products")

        val lowercaseQuery = query.toLowerCase()

        productsRef.get()
            .addOnSuccessListener { documents ->
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
                            inflater.inflate(R.layout.prdouct_items, linearlayout1, false)

                        val productIcon: ImageView = customItemView.findViewById(R.id.productIcon)
                        Picasso.get()
                            .load(imgSrc)
                            .placeholder(R.drawable.blue_sales_icon) // Placeholder image resource
                            .error(R.drawable.blue_sales_icon) // Error image resource (optional)
                            .into(productIcon)

                        val productTextView =
                            customItemView.findViewById<TextView>(R.id.productTextView)
                        productTextView.text = productName // Display the actual data

                        val categoryTextView =
                            customItemView.findViewById<TextView>(R.id.categoryTextView)
                        categoryTextView.text = category

                        val priceTextView =
                            customItemView.findViewById<TextView>(R.id.priceTextView)
                        priceTextView.text = "Price: ${price.toString()}"

                        val quantityTextView =
                            customItemView.findViewById<TextView>(R.id.quantityTextView)
                        quantityTextView.text = "Quantity: ${quantity.toString()}"

                        val supplierTextView =
                            customItemView.findViewById<TextView>(R.id.supplierTextView)
                        supplierTextView.text = "Supplier: $supplier"

                        val sizeTextView =
                            customItemView.findViewById<TextView>(R.id.sizeTextView)
                        sizeTextView.text = "Size: ${size.toString()}"

                        val costTextView =
                            customItemView.findViewById<TextView>(R.id.costTextView)
                        costTextView.text = "Cost: ${cost.toString()}"

                        customItemView.setOnClickListener{
                            redirectToActivityWithID(ModifyDeleteProductActivity::class.java, docID)
                        }

                        linearlayout1.addView(customItemView)
                    }
                }
            }
    }

    private fun handleSearch() {
        val query = searchInputEditText.text.toString()
        filterProductInformation(query)
        hideKeyboard()
    }
    private fun hideKeyboard() {
        val imm =
            requireContext().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(requireView().windowToken, 0)
    }
}
