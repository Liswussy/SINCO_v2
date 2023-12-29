package com.example.sinco_v2

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.LinearLayout
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase


class StockTakeFragment : Fragment() {

    private lateinit var linearLayout : LinearLayout
    private val supplierIdList = mutableListOf<String>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view =
            inflater.inflate(R.layout.fragment_stock_take, container, false)

        linearLayout = view.findViewById(R.id.linearlayout2)
        getAllSupplierDataFromFirebaseToSpinner(view)

        return view
    }

    private fun getAllSupplierDataFromFirebaseToSpinner(view: View) {
        val db = FirebaseFirestore.getInstance()
        val suppliersRef = db.collection("suppliers")

        suppliersRef.get()
            .addOnSuccessListener { documents ->
                val suppliersNames = mutableListOf<String>()

                for (document in documents) {
                    val name = document.getString("name")
                    name?.let {
                        suppliersNames.add(it)
                    }

                    supplierIdList.add(document.id)
                }

                val spinner: Spinner? = view.findViewById(R.id.spinner)

                spinner?.let {
                    val adapter =
                        ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, suppliersNames)
                    adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                    it.adapter = adapter
                }

                spinner?.setOnItemSelectedListener(object : AdapterView.OnItemSelectedListener {
                    override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                        val supplierDocId = supplierIdList[position]

                        Toast.makeText(requireContext(), supplierDocId, Toast.LENGTH_SHORT).show()

                        val supplierDocRef = db.collection("suppliers").document(supplierDocId)
                        supplierDocRef.get()
                            .addOnSuccessListener { document ->
                                // Handle success, if needed
                                setupSuppliersProducts(document)
                            }
                            .addOnFailureListener {
                                Toast.makeText(requireContext(), "Database Error", Toast.LENGTH_SHORT).show()
                            }
                    }

                    override fun onNothingSelected(parent: AdapterView<*>?) {

                    }
                })

            }
    }

    private fun setupSuppliersProducts(document: DocumentSnapshot) {
        val products = document.get("products") as? ArrayList<Map<String, Any>> ?: ArrayList()
        linearLayout.removeAllViews()

        val db = Firebase.firestore

        for (product in products) {
            val productName = product["name"] as? String ?: ""
            val prodID = product["id"] as? String ?: ""

            val inflater = LayoutInflater.from(requireContext())
            val customItemView =
                inflater.inflate(R.layout.stocktake_product_items, linearLayout, false)

            // Now you can find the TextViews or other views in your stocktake_product_items layout
            val productTextView = customItemView.findViewById<TextView>(R.id.productNameTextView)
            val expectedTextView = customItemView.findViewById<TextView>(R.id.expectedTextView)

            // Set the values
            productTextView.text = productName

            // Reference the product document directly using prodID
            val productRef = db.collection("products").document(prodID)

            productRef.get()
                .addOnSuccessListener { productDocument ->
                    // Get the additional information from the product document
                    val productQuantity = (productDocument.getDouble("qnty")?.toInt()) ?: 0

                    // Update the UI with additional information if needed
                    expectedTextView.text = productQuantity.toString()

                    // Add customItemView to your layout (linearlayout1, for example)
                    linearLayout.addView(customItemView)
                }
                .addOnFailureListener { exception ->
                    // Handle failure in fetching the product document
                    println("Failed to fetch product document: $exception")
                }
        }
    }




}