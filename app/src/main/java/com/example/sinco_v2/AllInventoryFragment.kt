package com.example.sinco_v2

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
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
                    val productQuantity = document.get("qnty")

                    val inflater = LayoutInflater.from(requireContext())
                    val customTextView = inflater.inflate(R.layout.inventory_items, linearlayout1, false)

                    val productTextView = customTextView.findViewById<TextView>(R.id.productTextView)
                    val productText = productName
                    productTextView.text = productText

                    val quantityTextView = customTextView.findViewById<TextView>(R.id.quantityTextView)
                    val quantityText =  productQuantity.toString()
                    quantityTextView.text = quantityText

                    linearlayout1.addView(customTextView)

                }

            }

    }

}