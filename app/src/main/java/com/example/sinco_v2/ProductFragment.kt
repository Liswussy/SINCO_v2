package com.example.sinco_v2

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.TextView
import androidx.fragment.app.FragmentActivity
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

class ProductFragment : Fragment() {

    private lateinit var linearlayout1: LinearLayout
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_product, container, false)

        linearlayout1 = view.findViewById(R.id.linearlayout1)

        showProductInformation()

        return view
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Assuming you have a button or some other UI element that triggers the redirection
        val redirectToActivityButton = view.findViewById<ImageButton>(R.id.ib_add_icon)

        redirectToActivityButton.setOnClickListener {
            redirectToActivity(AddNewProductActivity::class.java)
        }

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


        val db = Firebase.firestore
        val productsRef = db.collection("products")
        productsRef.get()
            .addOnSuccessListener { documents ->
                for (doc in documents) {
                    val productName = doc.get("prdnme") as? String ?: ""
                    val category = doc.get("ctg") as? String ?: ""
                    val price = doc.get("price") as? Double ?: 0.0
                    val supplier = doc.get("supp") as? String ?: ""
                    val size = doc.get("size") as? Double ?: 0.0
                    val cost = doc.get("cost") as? Double ?: 0.0
                    val quantity = doc.get("qnty")
                    val icon = doc.get("imgSrc") as? String ?: ""

                    val inflater = LayoutInflater.from(requireContext())
                    val customItemView =
                        inflater.inflate(R.layout.prdouct_items, linearlayout1, false)

                    val productTextView =
                        customItemView.findViewById<TextView>(R.id.productTextView)
                    val productText = productName
                    productTextView.text = productText

                    val categoryTextView =
                        customItemView.findViewById<TextView>(R.id.categoryTextView)
                    val categoryText = category
                    categoryTextView.text = categoryText

                    val priceTextView = customItemView.findViewById<TextView>(R.id.priceTextView)
                    val priceText = "Price: ${price.toString()}"
                    priceTextView.text = priceText

                    val quantityTextView = customItemView.findViewById<TextView>(R.id.quantityTextView)
                    val quantityText = "Quantity: $quantity"
                    quantityTextView.text = quantityText

                    val supplierTextView = customItemView.findViewById<TextView>(R.id.supplierTextView)
                    val supplierText = "Supplier: $supplier"
                    supplierTextView.text = supplierText

                    val sizeTextView = customItemView.findViewById<TextView>(R.id.sizeTextView)
                    val sizeText = "Size: ${size.toString()}"
                    sizeTextView.text = sizeText

                    val costTextView = customItemView.findViewById<TextView>(R.id.costTextView)
                    val costText = "Cost: ${cost.toString()}"
                    costTextView.text = costText


                    linearlayout1.addView(customItemView)
                }

                }
            .addOnFailureListener{




            }
    }

}
