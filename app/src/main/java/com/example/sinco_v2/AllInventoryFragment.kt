package com.example.sinco_v2

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.ArrayAdapter
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TextView
import com.google.android.material.internal.ViewUtils.hideKeyboard
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

class AllInventoryFragment : Fragment() {
    private lateinit var linearlayout1: LinearLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_all_inventory, container, false)
        linearlayout1 = view.findViewById(R.id.linearlayout1)

        val searchEditText = view.findViewById<TextInputEditText>(R.id.searchEditText)
        searchEditText.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                val query = searchEditText.text.toString()
                filterProductInformation(query)
                hideKeyboard() // Hide the keyboard after pressing Enter
                return@setOnEditorActionListener true
            }
            false
        }

        showProductInformation()
        return view
    }

    private fun showProductInformation(){
        val db = Firebase.firestore
        val docRef = db.collection("products")
        docRef.get()
            .addOnSuccessListener { documents ->
                for (document in documents){
                    val productName = document.get("prdnme") as? String ?: ""
                    val productQuantity = (document.get("qnty") as? Number)?.toInt() ?: 0

                    val inflater = LayoutInflater.from(requireContext())
                    val customTextView = inflater.inflate(R.layout.inventory_items, linearlayout1, false)

                    val productTextView = customTextView.findViewById<TextView>(R.id.productTextView)
                    val productText = productName
                    productTextView.text = productText

                    val quantityTextView = customTextView.findViewById<TextView>(R.id.quantityTextView)
                    val quantityText = productQuantity.toString()
                    quantityTextView.text = quantityText

                    linearlayout1.addView(customTextView)

                }

            }

    }

    private fun filterProductInformation(query: String) {
        linearlayout1.removeAllViews()

        val db = Firebase.firestore
        val docRef = db.collection("products")

        val lowercaseQuery = query.toLowerCase()

        docRef.get()
            .addOnSuccessListener { documents ->
                for (document in documents) {
                    val productName = (document.get("prdnme") as? String)?.toLowerCase() ?: ""
                    val productQuantity = (document.get("qnty") as? Number)?.toInt() ?: 0

                    if (productName.contains(lowercaseQuery)) {
                        val inflater = LayoutInflater.from(requireContext())
                        val customTextView =
                            inflater.inflate(R.layout.inventory_items, linearlayout1, false)

                        val productTextView =
                            customTextView.findViewById<TextView>(R.id.productTextView)
                        val productText = document.get("prdnme") as? String ?: ""
                        productTextView.text = productText

                        val quantityTextView =
                            customTextView.findViewById<TextView>(R.id.quantityTextView)
                        val quantityText = productQuantity.toString()
                        quantityTextView.text = quantityText

                        linearlayout1.addView(customTextView)
                    }
                }
            }
    }
    private fun hideKeyboard() {
        val imm = requireContext().getSystemService(InputMethodManager::class.java)
        imm.hideSoftInputFromWindow(requireView().windowToken, 0)
    }
}