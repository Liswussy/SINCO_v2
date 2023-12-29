package com.example.sinco_v2

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.LinearLayout
import android.widget.TextView
import androidx.fragment.app.FragmentActivity
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

class ConsignorSalesFragment : Fragment() {
    private lateinit var linearLayout1: LinearLayout
    private lateinit var searchInputEditText: TextInputEditText
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view =  inflater.inflate(R.layout.fragment_consignor_sales, container, false)
        linearLayout1 = view.findViewById(R.id.linearlayout1)

        showConsignorName()
        searchInputEditText = view.findViewById(R.id.searchEditText)

        val searchInputLayout = view.findViewById<TextInputLayout>(R.id.searchInputLayout)
        searchInputLayout.setEndIconOnClickListener {
            // Clear the text and show all products
            searchInputEditText.text = null
            linearLayout1.removeAllViews() // Clear existing views
            showConsignorName()
            // Hide the keyboard
            hideKeyboard()
        }


        showConsignorName()
        handleSearch()

        return view
    }

    private fun redirectToActivityWithID(activityClass: Class<out FragmentActivity>, consignorID: String) {
        val intent = Intent(requireContext(), activityClass)
        intent.putExtra("consignorID",consignorID )

        val fragmentTransaction = requireActivity().supportFragmentManager.beginTransaction()
        fragmentTransaction.replace(R.id.fragment_container, ConsignorSalesFragment())
        fragmentTransaction.addToBackStack(null)
        fragmentTransaction.commit()

        startActivity(intent)
    }

    private fun showConsignorName(){
        val db = Firebase.firestore
        val docRef = db.collection("suppliers")
        docRef.get()
            .addOnSuccessListener { documents ->
                for (document in documents){
                    val consignorID = document.id
                    val consignorName = document.get("name") as? String ?: ""

                    val inflater = LayoutInflater.from(requireContext())
                    val customTextView = inflater.inflate(R.layout.consignor_items, linearLayout1, false)

                    val consignorTextView = customTextView.findViewById<TextView>(R.id.consignorTextView)
                    val consignorText = consignorName
                    consignorTextView.text = consignorText

                    customTextView.setOnClickListener{
                        redirectToActivityWithID(ConsignorProductSalesActivity::class.java,consignorID )

                    }
                    linearLayout1.addView(customTextView)

                }

            }

    }
    private fun filterConsignorName(query: String) {
        linearLayout1.removeAllViews()

        val db = Firebase.firestore
        val docRef = db.collection("suppliers")
        val lowercaseQuery = query.toLowerCase()

        docRef.get()
            .addOnSuccessListener { documents ->
                for (document in documents) {
                    val consignorName = document.get("name") as? String ?: ""
                    val lowercaseConsignorName = consignorName.toLowerCase()

                    if (lowercaseConsignorName.contains(lowercaseQuery)) {
                        val inflater = LayoutInflater.from(requireContext())
                        val customTextView =
                            inflater.inflate(R.layout.consignor_items, linearLayout1, false)

                        val consignorTextView =
                            customTextView.findViewById<TextView>(R.id.consignorTextView)
                        consignorTextView.text = consignorName

                        linearLayout1.addView(customTextView)
                    }
                }
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