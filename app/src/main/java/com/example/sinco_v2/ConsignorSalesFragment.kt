package com.example.sinco_v2

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.LinearLayout
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import com.google.firebase.Firebase
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.firestore

class ConsignorSalesFragment : Fragment() {
    private lateinit var linearLayout1: LinearLayout
    private lateinit var recyclerView: RecyclerView
    private lateinit var searchInputEditText: TextInputEditText
    private val batchSize = 10
    private var lastDocument: DocumentSnapshot? = null
    private var isLoading = false

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_consignor_sales, container, false)
        linearLayout1 = view.findViewById(R.id.linearlayout1)

        showSuppliers()
        searchInputEditText = view.findViewById(R.id.searchEditText)

        val searchInputLayout = view.findViewById<TextInputLayout>(R.id.searchInputLayout)
        searchInputLayout.setEndIconOnClickListener {
            // Clear the text and show all products
            searchInputEditText.text = null
            linearLayout1.removeAllViews() // Clear existing views
            //showConsignorName()
            // Hide the keyboard
            hideKeyboard()
        }

        handleSearch()

        return view
    }

    private fun redirectToActivityWithID(activityClass: Class<out FragmentActivity>, consignorID: String) {
        val intent = Intent(requireContext(), activityClass)
        intent.putExtra("consignorID", consignorID)

        startActivity(intent)
    }

    private fun showSuppliers() {

        val db = Firebase.firestore
        db.collection("suppliers")
            .get()
            .addOnSuccessListener { documents ->
                for (doc in documents) {
                    val supplierName = doc.getString("name") ?: ""
                    val supplierID = doc.id

                    val inflater = LayoutInflater.from(requireContext())
                    val customItemView = inflater.inflate(R.layout. consignor_items, linearLayout1, false)

                    val supplierNameTextView = customItemView.findViewById<TextView>(R.id.consignorTextView)
                    supplierNameTextView.text = supplierName

                    val linearlayout2 = customItemView.findViewById<LinearLayout>(R.id.linearlayout2)
                    linearlayout2.setOnClickListener{
                        redirectToActivityWithID(ConsignorProductSalesActivity::class.java, supplierID)
                    }

                    linearLayout1.addView(customItemView)
                }
            }
            .addOnFailureListener { exception ->

            }

    }





    private fun filterConsignorName(query: String) {
        linearLayout1.removeAllViews()

        val db = FirebaseFirestore.getInstance()
        val collectionReference = db.collection("products")

        val supplierMap = mutableMapOf<String, MutableList<String>>()

        collectionReference.get()
            .addOnSuccessListener { documents ->
                for (document in documents) {
                    val consignorName = document.getString("supp") ?: ""
                    val lowercaseConsignorName = consignorName.toLowerCase()

                    if (lowercaseConsignorName.contains(query.toLowerCase())) {
                        if (!supplierMap.containsKey(consignorName)) {
                            supplierMap[consignorName] = mutableListOf()
                        }

                        supplierMap[consignorName]?.add(document.id)
                    }
                }

                // Add views after filtering
                for ((consignorName, documentIds) in supplierMap) {
                    val inflater = LayoutInflater.from(requireContext())
                    val customTextView =
                        inflater.inflate(R.layout.consignor_items, linearLayout1, false)

                    val consignorTextView =
                        customTextView.findViewById<TextView>(R.id.consignorTextView)
                    consignorTextView.text = consignorName

                    customTextView.setOnClickListener {
                        redirectToActivityWithID(
                            ConsignorProductSalesActivity::class.java,
                            documentIds[0] // Assuming you want to pass the first document ID
                        )
                    }

                    linearLayout1.addView(customTextView)
                }
            }
            .addOnFailureListener { exception ->
                // Handle errors here
            }
    }



    private fun handleSearch() {
        searchInputEditText.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                val query = searchInputEditText.text.toString()
                filterConsignorName(query)
                return@setOnEditorActionListener true
            }
            false
        }
    }

    private fun hideKeyboard() {
        val imm =
            requireContext().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(requireView().windowToken, 0)
    }
}
