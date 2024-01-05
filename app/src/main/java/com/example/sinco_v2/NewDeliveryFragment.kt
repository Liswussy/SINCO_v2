package com.example.sinco_v2

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.CheckBox
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatDialog
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore

class NewDeliveryFragment : Fragment() {

    private lateinit var supplierProductsLayout: LinearLayout
    private lateinit var productDefects : LinearLayout
    private lateinit var linearlayoutDamage: LinearLayout
    private var selectedProducts: MutableList<String> = mutableListOf()
    private var consignorID: String = ""
    var reference =  ArrayList<Map<String, Any>>()

    private val db = FirebaseFirestore.getInstance()

    var consignorIdList = mutableListOf<String>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_new_delivery, container, false)

        supplierProductsLayout = view.findViewById(R.id.supplierProductsLayout)
        productDefects = view.findViewById(R.id.productDefects)

        val helpButtonIcon = view.findViewById<ImageButton>(R.id.helpButtonIcon)
        helpButtonIcon.setOnClickListener{
            showTutorialDialog()
        }

        getAllSupplierDataFromFirebaseToSpinner()
        val btn_confirm = view.findViewById<Button>(R.id.btn_confirm)
        btn_confirm.setOnClickListener {

            updateProductQuantity()
        }

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
                        consignorID = consignorIdList[position]

                        Toast.makeText(requireContext(), consignorID, Toast.LENGTH_SHORT).show()

                        val consignorDocRef = db.collection("suppliers").document(consignorID)
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
    private fun showTutorialDialog() {
        val dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.custom_dialog_message_box, null)
        val builder = MaterialAlertDialogBuilder(requireContext())

        val messageTextView: TextView = dialogView.findViewById(R.id.messageTextView)
        messageTextView.text = "If you press the checkbox, the input box will appear. \n" +
                "Click and hold on the product name to view the defect form for each product; it will have a defect form."

        // Set the width and height of the dialog

        builder.setView(dialogView)
        builder.setTitle("Tutorial Message")
        builder.setPositiveButton("OK") { dialog, _ ->
            dialog.dismiss()
        }

        val dialog = builder.create()
        dialog.show()

        // Set the width and height after the dialog is shown

        dialogView.setOnTouchListener { _, _ -> true } // Disable touch outside to dismiss
    }

    private fun setupSuppliersProducts(document: DocumentSnapshot) {
        val products = document.get("products") as? ArrayList<Map<String, Any>> ?: ArrayList()
        supplierProductsLayout.removeAllViews()

        for (product in products) {
            val productName = product["name"] as? String ?: ""
            val prodID = product["id"] as? String ?: ""

            val prod = mapOf(
                "id" to prodID,
                "name" to productName
            )
            reference.add(prod)

            val inflater = LayoutInflater.from(requireContext())
            val customItemView = inflater.inflate(R.layout.delivery_product_items, supplierProductsLayout, false)

            val productNameCheckBox = customItemView.findViewById<CheckBox>(R.id.productNameCheckBox)
            val productNameTextView = customItemView.findViewById<TextView>(R.id.productNameTextView)
            val quantityInputLayout = customItemView.findViewById<TextInputLayout>(R.id.quantityTextInputLayout)
            val quantityEditText = customItemView.findViewById<TextInputEditText>(R.id.quantityTextInput)
            quantityEditText.setTag(productName)


            //Defect Checkbox & Layout
            val defectLayout = customItemView.findViewById<LinearLayout>(R.id.defectLayout)
            val damagedDefectCheckBox = customItemView.findViewById<CheckBox>(R.id.damageDefectCheckBox)
            val expiredDefectCheckBox = customItemView.findViewById<CheckBox>(R.id.expiredDefectCheckBox)

            //Damaged Layout & Edit Text
            val damageTextInputLayout = customItemView.findViewById<TextInputLayout>(R.id.damageTextInputLayout)
            val damageQuantityTextInput = customItemView.findViewById<TextInputEditText>(R.id.damageQuantityTextInput)

            //Expired Layout & Edit Text
            val expiredTextInputLayout = customItemView.findViewById<TextInputLayout>(R.id.expiredTextInputLayout)
            val expiredQuantityTextInput = customItemView.findViewById<TextInputEditText>(R.id.expiredQuantityTextInput)

            //Damage Checkbox when check or Unchecked
            damagedDefectCheckBox.setOnCheckedChangeListener { _, isChecked ->
                // If checkbox is checked, set damageTextInputLayout to VISIBLE; otherwise, set it to GONE
                if (isChecked) {
                    damageTextInputLayout.visibility = View.VISIBLE
                } else {
                    damageTextInputLayout.visibility = View.GONE
                    damageQuantityTextInput.setText("")
                }
            }


            expiredDefectCheckBox.setOnCheckedChangeListener { _, isChecked ->
                // If checkbox is checked, set damageTextInputLayout to VISIBLE; otherwise, set it to GONE
                if (isChecked) {
                    expiredTextInputLayout.visibility = View.VISIBLE
                } else {
                    expiredTextInputLayout.visibility = View.GONE
                    damageQuantityTextInput.setText("")
                }
            }


            // Set up the listener to show/hide TextInputLayout based on CheckBox state
            productNameCheckBox.setOnCheckedChangeListener { _, isChecked ->
                if (isChecked) {
                    quantityInputLayout.visibility = View.VISIBLE
                    // Set the initial value to "0" if EditText is empty
//                    if (quantityEditText.text.isNullOrBlank()) { }
                    // Add the product name to selectedProducts when the checkbox is checked
                    selectedProducts.add(productName)

                } else {
                    quantityInputLayout.visibility = View.GONE
                    // Reset the text when hiding
                    quantityEditText.setText("")
                    // Remove the product name from selectedProducts when the checkbox is unchecked
                    selectedProducts.remove(productName)

                    defectLayout.visibility = View.GONE
                    // Clear or reset defects when closing the defect layout
                    damagedDefectCheckBox.isChecked = false
                    expiredDefectCheckBox.isChecked = false
                    damageTextInputLayout.visibility = View.GONE
                    expiredTextInputLayout.visibility = View.GONE
                    damageQuantityTextInput.setText("")
                    expiredQuantityTextInput.setText("")

                    productDefects.visibility = View.GONE

                }
            }

            // Ensure that the TextInputLayout is initially hidden
            quantityInputLayout.visibility = View.GONE

            // defect
            productNameTextView.text = productName
            productNameTextView.setOnLongClickListener{
                if (defectLayout.visibility == View.GONE && productNameCheckBox.isChecked) {
                    defectLayout.visibility = View.VISIBLE
                    productDefects.visibility = View.VISIBLE
                } else {
                    defectLayout.visibility = View.GONE
                    // Clear or reset defects when closing the defect layout
                    damagedDefectCheckBox.isChecked = false
                    expiredDefectCheckBox.isChecked = false
                    damageTextInputLayout.visibility = View.GONE
                    expiredTextInputLayout.visibility = View.GONE
                    damageQuantityTextInput.setText("")
                    expiredQuantityTextInput.setText("")

                    productDefects.visibility = View.GONE

                }

                true // Return true to consume the long click event
            }

            supplierProductsLayout.addView(customItemView)
        }
    }


    private fun updateProductQuantity() {
        // Iterate through selected products
        for (productName in selectedProducts) {
            // Find the TextInputEditText for the current product
            val quantityEditText = supplierProductsLayout.findViewWithTag<TextInputEditText>(productName)

            // Get the quantity text from the EditText
            val quantityText = quantityEditText?.text?.toString()

            if (!quantityText.isNullOrBlank()) {
                try {
                    val quantity = quantityText.toInt()

                    // Get the document ID for the current product
                    var docID = ""
                    for (product in reference) {
                        if (product["name"] == productName) {
                            docID = (product["id"] as? String).toString()
                            break
                        }
                    }

                    if (docID.isNotEmpty()) {
                        // Retrieve the current quantity from the Firestore document
                        val prodRef = db.collection("products").document(docID)
                        prodRef.get()
                            .addOnSuccessListener { documentSnapshot ->
                                val currentQuantity = documentSnapshot.getLong("qnty") ?: 0

                                // Calculate the updated quantity
                                val updatedQuantity = currentQuantity + quantity

                                // Update the "qnty" field in the Firestore document
                                prodRef.update("qnty", updatedQuantity)
                                    .addOnSuccessListener {
                                        // Update successful
                                        Toast.makeText(requireContext(), "$productName quantity updated successfully", Toast.LENGTH_SHORT).show()
                                    }
                                    .addOnFailureListener { e ->
                                        // Handle any errors that occurred during the update
                                        Toast.makeText(requireContext(), "Error updating $productName quantity", Toast.LENGTH_SHORT).show()
                                        // You can also log the error for debugging purposes
                                        Toast.makeText(requireContext(), e.toString(), Toast.LENGTH_SHORT).show()
                                    }
                            }
                            .addOnFailureListener { e ->
                                // Handle any errors that occurred while retrieving the current quantity
                                Toast.makeText(requireContext(), "Error retrieving current quantity for $productName", Toast.LENGTH_SHORT).show()
                                Toast.makeText(requireContext(), e.toString(), Toast.LENGTH_SHORT).show()
                            }
                    }
                } catch (e: NumberFormatException) {
                    // Handle the case where the input is not a valid number
                    Toast.makeText(requireContext(), "Invalid quantity input for $productName", Toast.LENGTH_SHORT).show()
                }
            } else {
                // Handle the case where the quantity text is blank or null
                Toast.makeText(requireContext(), "Quantity cannot be blank for $productName", Toast.LENGTH_SHORT).show()
            }
        }
    }


}