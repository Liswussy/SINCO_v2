package com.example.sinco_v2

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.CheckBox
import android.widget.LinearLayout
import android.widget.Spinner
import android.widget.Toast
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore


class NewReturnedProductsFragment : Fragment() {

    private lateinit var supplierProductsLayout: LinearLayout
    var consignorIdList = mutableListOf<String>()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_new_returned_products, container, false)

        getAllSupplierDataFromFirebaseToSpinner()
        supplierProductsLayout = view.findViewById(R.id.supplierProductsLayout)
        return view

    }



    private fun getAllSupplierDataFromFirebaseToSpinner() {

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

                    val productsList = document.get("products") as? List<Map<String, Any>>?
                    productsList?.let {
                        for (map in it) {
                            val productId = map["id"]
                            val productName = map["name"]
                            println("Product ID: $productId, Product Name: $productName")
                        }
                    }

                    consignorIdList.add(document.id)
                }

                val spinner: Spinner? = view?.findViewById(R.id.supplierSpinner)

                spinner?.let {
                    val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, suppliersNames)
                    adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                    it.adapter = adapter
                }

                spinner?.setOnItemSelectedListener(object : AdapterView.OnItemSelectedListener {
                    override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                        val consignorDocId = consignorIdList[position]

                        Toast.makeText(requireContext(), consignorDocId, Toast.LENGTH_SHORT).show()

                        val consignorDocRef = db.collection("suppliers").document(consignorDocId)
                        consignorDocRef.get()
                            .addOnSuccessListener { document ->
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


    private fun setupSuppliersProducts(document: DocumentSnapshot){

        val products = document.get("products") as? ArrayList<Map<String, Any>> ?: ArrayList()
        supplierProductsLayout.removeAllViews()

        for (product in products){
            val productName = product["name"] as? String ?: ""

            val inflater = LayoutInflater.from(requireContext())
            val customItemView = inflater.inflate(R.layout.delivery_product_items, supplierProductsLayout, false)

            val productNameCheckBox = customItemView.findViewById<CheckBox>(R.id.productNameCheckBox)
            productNameCheckBox.text = productName

            supplierProductsLayout.addView(customItemView)

        }


    }
}