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
import android.widget.TextView
import android.widget.Toast
import androidx.constraintlayout.widget.ConstraintLayout
import com.google.android.material.button.MaterialButton
import com.google.android.material.checkbox.MaterialCheckBox
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.QueryDocumentSnapshot
import com.google.firebase.firestore.QuerySnapshot

class NewDeliveryFragment : Fragment() {

    private lateinit var supplierProductsLayout: LinearLayout
    private lateinit var productDefects : LinearLayout
    private lateinit var defectCheckBox: CheckBox
    private lateinit var damageCheckBox: CheckBox
    private lateinit var expiredCheckBox: CheckBox
    private lateinit var linearlayoutDamage: LinearLayout
    private lateinit var linearlayoutExpired: LinearLayout
    private var selectedProducts: MutableList<String> = mutableListOf()

    var consignorIdList = mutableListOf<String>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_new_delivery, container, false)

        supplierProductsLayout = view.findViewById(R.id.supplierProductsLayout)
        productDefects = view.findViewById(R.id.productDefects)
        defectCheckBox = view.findViewById(R.id.showDefectTextView)
        damageCheckBox = view.findViewById(R.id.damageDefect)
        expiredCheckBox = view.findViewById(R.id.defectExpired)
        linearlayoutDamage = view.findViewById(R.id.linearlayoutDamage)
        linearlayoutExpired = view.findViewById(R.id.linearlayoutExpired)

        // Set initial visibility of productDefects to GONE
        productDefects.visibility = View.GONE

        // Set initial visibility of damage and expired templates to GONE
        linearlayoutDamage.visibility = View.GONE
        linearlayoutExpired.visibility = View.GONE

        // Set OnCheckedChangeListener for the defectCheckBox
        defectCheckBox.setOnCheckedChangeListener { _, isChecked ->
            // If checkbox is checked, set productDefects layout to VISIBLE; otherwise, set it to GONE
            productDefects.visibility = if (isChecked) View.VISIBLE else View.GONE
        }

        // Set OnCheckedChangeListener for the damageCheckBox
        damageCheckBox.setOnCheckedChangeListener { _, isChecked ->
            // If checkbox is checked, set linearlayoutDamage to VISIBLE; otherwise, set it to GONE
            linearlayoutDamage.visibility = if (isChecked) View.VISIBLE else View.GONE
            // If linearlayoutDamage is visible, populate it with selected products
            if (isChecked) populateDefectTemplate(linearlayoutDamage)
        }

        // Set OnCheckedChangeListener for the expiredCheckBox
        expiredCheckBox.setOnCheckedChangeListener { _, isChecked ->
            // If checkbox is checked, set linearlayoutExpired to VISIBLE; otherwise, set it to GONE
            linearlayoutExpired.visibility = if (isChecked) View.VISIBLE else View.GONE
        }

        getAllSupplierDataFromFirebaseToSpinner()

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
                        // Handle when nothing is selected
                    }
                })

            }
    }

    private fun setupSuppliersProducts(document: DocumentSnapshot) {
        val products = document.get("products") as? ArrayList<Map<String, Any>> ?: ArrayList()
        supplierProductsLayout.removeAllViews()

        for (product in products) {
            val productName = product["name"] as? String ?: ""

            val inflater = LayoutInflater.from(requireContext())
            val customItemView = inflater.inflate(R.layout.delivery_product_items, supplierProductsLayout, false)

            val productNameCheckBox = customItemView.findViewById<CheckBox>(R.id.productNameCheckBox)
            productNameCheckBox.text = productName

            // Find TextInputLayout and EditText inside the customItemView
            val quantityInputLayout = customItemView.findViewById<TextInputLayout>(R.id.quantityTextInputLayout)
            val quantityEditText = customItemView.findViewById<TextInputEditText>(R.id.quantityTextInput)

            // Set up the listener to show/hide TextInputLayout based on CheckBox state
            productNameCheckBox.setOnCheckedChangeListener { _, isChecked ->
                if (isChecked) {
                    quantityInputLayout.visibility = View.VISIBLE
                    // Set the initial value to "0" if EditText is empty
                    if (quantityEditText.text.isNullOrBlank()) {
                        quantityEditText.setText("0")
                    }
                    // Add the product name to selectedProducts when the checkbox is checked
                    selectedProducts.add(productName)
                } else {
                    quantityInputLayout.visibility = View.GONE
                    // Reset the text when hiding
                    quantityEditText.setText("")
                    // Remove the product name from selectedProducts when the checkbox is unchecked
                    selectedProducts.remove(productName)
                }
            }

            // Ensure that the TextInputLayout is initially hidden
            quantityInputLayout.visibility = View.GONE

            supplierProductsLayout.addView(customItemView)
        }
    }

    private fun populateDefectTemplate(linearlayoutDamage: LinearLayout?) {
        linearlayoutDamage?.removeAllViews()

        // Check if linearlayoutDamage is not null and selectedProducts is not empty
        if (linearlayoutDamage != null && selectedProducts.isNotEmpty()) {
            // Iterate through selected products and create views
            for (productName in selectedProducts) {
                val inflater = LayoutInflater.from(requireContext())
                val customItemView = inflater.inflate(R.layout.damage_items, linearlayoutDamage, false)

                val productNameTextView = customItemView.findViewById<TextView>(R.id.product1CheckBoxTextView)
                productNameTextView.text = productName

                linearlayoutDamage.addView(customItemView)
            }
        }
    }
}
