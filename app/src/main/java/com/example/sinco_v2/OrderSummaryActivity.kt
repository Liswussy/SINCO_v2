package com.example.sinco_v2

import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.View
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import com.google.firebase.Firebase
import com.google.firebase.Timestamp
import com.google.firebase.firestore.firestore

class OrderSummaryActivity : AppCompatActivity() {

    private lateinit var confirmButton: Button
    var subTotal = 0.0
    var discountAmount = 0.0
    var grandTotal = 0.0
    var change = 0.0
    var cash = 0.0
    var productList = ArrayList<Product>()
    private lateinit var selectedPaymentMethod: Spinner
    private var selectedPaymentMethodValue: String = ""
    private lateinit var referenceNumber: TextInputEditText



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_order_summary)

        referenceNumber = findViewById(R.id.gcashInputEditText)

        val imageButton: ImageButton = findViewById(R.id.ib_back_icon)

        // Set OnClickListener for the ImageButton
        imageButton.setOnClickListener {
            // Call the function to navigate back to the fragment
            onBackPressed()
        }

        confirmButton = findViewById(R.id.confirmButton)
        confirmButton.setOnClickListener {

            navigateToReceiptActivity()
        }

        productList = (intent.getSerializableExtra("productList") as? ArrayList<Product>)!!

        grandTotal = 0.0
        subTotal = 0.0

        fun calculateTotal() {
            grandTotal = 0.0
            subTotal = 0.0

            if (productList != null) {
                if (productList.isEmpty()) {
                    val textViewSubTotal = findViewById<TextView>(R.id.tv_subtotal)
                    textViewSubTotal.text = "P ${roundToTwoDecimalPlaces(subTotal.toDouble())}"
                }

                for (product in productList) {
                    val price = product.price
                    val quantity = product.quantity
                    subTotal += price!! * quantity!!
                    val textViewSubTotal = findViewById<TextView>(R.id.tv_subtotal)
                    textViewSubTotal.text = "P ${roundToTwoDecimalPlaces(subTotal.toDouble())}"
                    val textViewTotal = findViewById<TextView>(R.id.tv_grand_total)
                    textViewTotal.text = ("P ${roundToTwoDecimalPlaces(grandTotal.toDouble())}")
                }
                val spinner: Spinner = findViewById(R.id.sp_discount)
                val tvDiscountAmount = findViewById<TextView>(R.id.tv_total_discount)

                val adapter = ArrayAdapter.createFromResource(
                    this,
                    R.array.Discount,
                    android.R.layout.simple_spinner_item
                )
                spinner.adapter = adapter
                adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                spinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                    override fun onItemSelected(
                        parentView: AdapterView<*>,
                        selectedItemView: View?,
                        position: Int,
                        id: Long
                    ) {
                        val selectedDiscountString =
                            resources.getStringArray(R.array.Discount)[position]

                        try {
                            val discountMultiplier =
                                selectedDiscountString.replace("%", "").toDouble() / 100.0
                            discountAmount = grandTotal * discountMultiplier

                            tvDiscountAmount.text = "P ${roundToTwoDecimalPlaces(discountAmount)}"

                            val cashInputEditText =
                                findViewById<TextInputEditText>(R.id.cashInputEditText)
                            if (!cashInputEditText.text.isNullOrBlank()) {
                                val cash = cashInputEditText.text.toString().toDouble()
                                calculateAndDisplayChange()
                            }
                        } catch (e: NumberFormatException) {
                            e.printStackTrace()
                        }

                        grandTotal = subTotal - discountAmount

                        val textViewTotal = findViewById<TextView>(R.id.tv_grand_total)
                        textViewTotal.text = "P ${roundToTwoDecimalPlaces(grandTotal)}"

                    }

                    override fun onNothingSelected(parentView: AdapterView<*>) {
                        // Do nothing here
                    }
                }
            } else {
                println("Product list is null.")
            }
        }

        calculateTotal()

        val linearLayout = findViewById<LinearLayout>(R.id.cartList)
        if (productList != null) {
            for (product in productList) {
                val id = product.productID
                val name = product.productName
                val price = product.price
                val quantity = product.quantity
                val quantityText = "Quantity: " + quantity.toString()

                val entryView = layoutInflater.inflate(R.layout.order_summary_items, null)

                val tv_product_name = entryView.findViewById<TextView>(R.id.tv_product_name)
                val tv_product_price = entryView.findViewById<TextView>(R.id.tv_product_price)
                val productQuantityTextView =
                    entryView.findViewById<TextView>(R.id.tv_product_qty)
                val minusButton = entryView.findViewById<ImageButton>(R.id.minusButton)
                val addButton = entryView.findViewById<ImageButton>(R.id.addButton)
                val modifyButton = entryView.findViewById<ImageButton>(R.id.modifyButton)

                tv_product_name.text = name
                tv_product_price.text = price.toString()
                productQuantityTextView.text = quantityText

                minusButton.setOnClickListener() {
                    for (product in productList) {
                        if (product.productID == id) {
                            product.quantity = product.quantity?.plus(-1)
                            val quantityText = "Quantity: " + product.quantity.toString()
                            productQuantityTextView.text = quantityText

                            if (product.quantity!! <= 0) {
                                linearLayout.removeView(entryView)
                                productList.removeIf { product -> product.productID == id }
                            }

                            calculateTotal()
                            break // Exit the loop after finding and updating the product
                        }
                    }
                }

                addButton.setOnClickListener() {
                    for (product in productList) {
                        if (product.productID == id) {
                            product.quantity = product.quantity?.plus(1)
                            val quantityText = "Quantity: " + product.quantity.toString()
                            productQuantityTextView.text = quantityText

                            calculateTotal()
                            break // Exit the loop after finding and updating the product
                        }
                    }
                }

                modifyButton.setOnClickListener() {
                    val dialogView =
                        LayoutInflater.from(this).inflate(R.layout.custom_dialog_layout, null)
                    val leftTextView: TextView = dialogView.findViewById(R.id.productNameTextView)
                    val rightTextView: TextView = dialogView.findViewById(R.id.rightTextView)
                    val confirmButton: Button = dialogView.findViewById(R.id.confirmButton)
                    val editTextNumber: EditText = dialogView.findViewById(R.id.editTextNumber)

                    if (quantity != null) {
                        editTextNumber.setText(product.quantity.toString())
                    }

                    leftTextView.text = name

                    val dialog = AlertDialog.Builder(this)
                        .setView(dialogView)
                        .create()

                    confirmButton.setOnClickListener {
                        for (product in productList) {
                            if (product.productID == id) {
                                val inputText = editTextNumber.text.toString()
                                if (inputText.isNotEmpty()) {
                                    val numericValue =
                                        inputText.toInt() // Convert the string to a double
                                    product.quantity = numericValue
                                    val quantityText = "Quantity: " + product.quantity.toString()
                                    productQuantityTextView.text = quantityText

                                    if (product.quantity!! <= 0) {
                                        linearLayout.removeView(entryView)
                                        productList.removeIf { product -> product.productID == id }
                                    }

                                    calculateTotal()
                                    break // Exit the loop after finding and updating the product
                                } else {
                                    println("EditText is empty")
                                }
                            }
                        }
                        dialog.dismiss()
                    }

                    dialog.show()
                }

                linearLayout.addView(entryView)
            }
        }

        val myTextView = findViewById<TextView>(R.id.addMoreItemsTextView)
        myTextView.setOnClickListener {
            onBackPressed()
        }

        println(productList)

        selectedPaymentMethod = findViewById(R.id.spinnerSelectPaymentMethod)
        val cashPayment: TextInputLayout = findViewById(R.id.cashTextInputLayout)
        val cashPaymentEditText = findViewById<TextInputEditText>(R.id.cashInputEditText)
        val gcashPayment: TextInputLayout = findViewById(R.id.gcashTextInputLayout)
        val gcashPaymentEditText = findViewById<TextInputEditText>(R.id.gcashInputEditText)

        val paymentMethodAdapter = ArrayAdapter.createFromResource(
            this,
            R.array.paymentMethod,
            android.R.layout.simple_spinner_item
        )
        selectedPaymentMethod.adapter = paymentMethodAdapter
        paymentMethodAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)

        selectedPaymentMethod.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parentView: AdapterView<*>,
                selectedItemView: View?,
                position: Int,
                id: Long
            ) {
                selectedPaymentMethodValue = parentView.getItemAtPosition(position).toString()
                when (position) {
                    0 -> {
                        cashPayment.visibility = View.GONE
                        gcashPayment.visibility = View.GONE
                        cashPaymentEditText.text = null
                        gcashPaymentEditText.text = null
                    }
                    1 -> {
                        cashPayment.visibility = View.VISIBLE
                        gcashPayment.visibility = View.GONE
                        cashPaymentEditText.text = null

                    }
                    2 -> {
                        cashPayment.visibility = View.GONE
                        gcashPayment.visibility = View.VISIBLE
                        gcashPaymentEditText.text = null

                    }
                }
            }

            override fun onNothingSelected(parentView: AdapterView<*>) {
                // Do nothing here
            }
        }

        val gcashInputEditText = findViewById<TextInputEditText>(R.id.gcashInputEditText)
        gcashInputEditText.setOnEditorActionListener { _, actionId, keyEvent ->
            if (actionId == EditorInfo.IME_ACTION_DONE ||
                (keyEvent != null && keyEvent.action == KeyEvent.ACTION_DOWN &&
                        keyEvent.keyCode == KeyEvent.KEYCODE_ENTER)
            ) {
                val inputText = gcashInputEditText.text.toString().trim()

                if (inputText.isNotEmpty()) {
                    // If the input is not empty, proceed with calculations or other actions
                    hideKeyboard()
                    // (You can add your own logic here if needed)
                } else {
                    // If the input is empty, display a message
                    gcashInputEditText.error = "Please enter a valid GCash reference number"
                }

                return@setOnEditorActionListener true
            }
            return@setOnEditorActionListener false
        }

        val cashInputEditText = findViewById<TextInputEditText>(R.id.cashInputEditText)
        cashInputEditText.addTextChangedListener(cashInputWatcher)

        cashInputEditText.setOnEditorActionListener { _, actionId, keyEvent ->
            if (actionId == EditorInfo.IME_ACTION_DONE ||
                (keyEvent != null && keyEvent.action == KeyEvent.ACTION_DOWN &&
                        keyEvent.keyCode == KeyEvent.KEYCODE_ENTER)
            ) {
                val inputText = cashInputEditText.text.toString().trim()

                if (inputText.isNotEmpty()) {
                    // If the input is not empty, proceed with calculations
                    cash = inputText.toDouble()
                    calculateAndDisplayChange()
                    hideKeyboard()
                } else {
                    // If the input is empty, display a message
                    cashInputEditText.error = "Please enter a valid amount"
                }

                return@setOnEditorActionListener true
            }
            return@setOnEditorActionListener false
        }


    }


    private fun navigateToReceiptActivity() {
        if (selectedPaymentMethodValue == "Select Payment") {
            // Display a message indicating that the user must select a valid payment method
            Toast.makeText(this, "Please select a valid payment method", Toast.LENGTH_SHORT).show()
        } else {

            val cashInputEditText = findViewById<TextInputEditText>(R.id.cashInputEditText)
            val gcashInputEditText = findViewById<TextInputEditText>(R.id.gcashInputEditText)

            if (selectedPaymentMethodValue == "Cash" && cashInputEditText.text.isNullOrBlank()) {
                // Display a message indicating that the Cash input is empty
                cashInputEditText.error = "Please enter a valid amount"
                return
            }

            if (selectedPaymentMethodValue == "GCash" && gcashInputEditText.text.isNullOrBlank()) {
                // Display a message indicating that the GCash input is empty
                gcashInputEditText.error = "Please enter a valid GCash reference number"
                return
            }
            // Check if Cash input is greater than or equal to the grand total
            if (selectedPaymentMethodValue == "Cash" && cash < grandTotal) {
                // Display a message indicating that the cash is less than the grand total
                Toast.makeText(this, "Cash should be equal or greater than the grand total", Toast.LENGTH_SHORT).show()
                return
            }

            if (selectedPaymentMethodValue == "Cash"){
                confirmOrder(
                    productList,
                    roundToTwoDecimalPlaces(grandTotal).toDouble(),
                    roundToTwoDecimalPlaces(subTotal).toDouble(),
                    roundToTwoDecimalPlaces(discountAmount).toDouble(),
                    roundToTwoDecimalPlaces(change).toDouble(),
                    selectedPaymentMethodValue,
                    roundToTwoDecimalPlaces(cash).toDouble(),
                    ""
                )
            } else {
                confirmOrder(
                    productList,
                    roundToTwoDecimalPlaces(grandTotal).toDouble(),
                    roundToTwoDecimalPlaces(subTotal).toDouble(),
                    roundToTwoDecimalPlaces(discountAmount).toDouble(),
                    roundToTwoDecimalPlaces(change).toDouble(),
                    selectedPaymentMethodValue,
                    roundToTwoDecimalPlaces(cash).toDouble(),
                    referenceNumber.text.toString()
                )
            }


            val intent = Intent(this, ReceiptActivity::class.java).apply {
                putExtra("productList", productList)
                putExtra("grandTotal", grandTotal)
                putExtra("subTotal", subTotal)
                putExtra("discountAmount", discountAmount)
                putExtra("change", change)
                putExtra("selectedPaymentMethod", selectedPaymentMethodValue)
                putExtra("cashInputValue", cash)
                putExtra("gcashInputValue", gcashInputEditText.text.toString())
            }
            startActivity(intent)
            finish()
        }
    }


    private fun calculateAndDisplayChange() {
        // Calculate discount amount based on the selected discount
        val spinner: Spinner = findViewById(R.id.sp_discount)
        val position = spinner.selectedItemPosition
        val selectedDiscountString = resources.getStringArray(R.array.Discount)[position]

        try {
            val discountMultiplier = selectedDiscountString.replace("%", "").toDouble() / 100.0
            discountAmount = grandTotal * discountMultiplier

            val tvDiscountAmount = findViewById<TextView>(R.id.tv_total_discount)
            tvDiscountAmount.text = "P ${roundToTwoDecimalPlaces(discountAmount)}"
        } catch (e: NumberFormatException) {
            e.printStackTrace()
        }

        // Calculate grand total
        grandTotal = subTotal - discountAmount

        val cashInputEditText = findViewById<TextInputEditText>(R.id.cashInputEditText)
        if (!cashInputEditText.text.isNullOrBlank()) {
            cash = cashInputEditText.text.toString().toDouble()
            val calculatedChange = cash - grandTotal

            if (calculatedChange >= 0) {
                // If change is non-negative, display the calculated change
                val changeTextView = findViewById<TextView>(R.id.tv_change)
                changeTextView.text = "P ${roundToTwoDecimalPlaces(calculatedChange)}"
                change = calculatedChange
            } else {
                // If change is negative, set it to zero and display zero
                val changeTextView = findViewById<TextView>(R.id.tv_change)
                changeTextView.text = "P 0.00"
                change = 0.0
            }
        } else {
            // If cash is not provided, set change to zero and display zero
            val changeTextView = findViewById<TextView>(R.id.tv_change)
            changeTextView.text = "P 0.00"
            change = 0.0
        }

        // Update grand total on the UI
        val textViewTotal = findViewById<TextView>(R.id.tv_grand_total)
        textViewTotal.text = "P ${roundToTwoDecimalPlaces(grandTotal)}"
    }
    private fun hideKeyboard() {
        val imm =
            getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(currentFocus?.windowToken, 0)
    }

    fun roundToTwoDecimalPlaces(value: Double): String {
        val roundedValue = String.format("%.2f", value)
        return roundedValue
    }

    private fun confirmOrder(
        products: ArrayList<Product>,
        grandTotal: Double,
        subTotal: Double,
        discountAmount: Double,
        change: Double,
        paymentMethod: String,
        cashInputValue: Double,
        referenceNumber: String
    ){
        val data = mapOf(
            "paymentMethod" to paymentMethod,
            "timestamp" to Timestamp.now(),
            "total" to grandTotal,
            "subTotal" to subTotal,
            "discountAmount" to discountAmount,
            "change" to change,
            "cashInputValue" to cashInputValue,
            "products" to products,
            "referenceNumber" to referenceNumber
        )

        val db = Firebase.firestore

        val ordersRef = db.collection("orders")
        ordersRef
            .add(data)
            .addOnSuccessListener {
                Toast.makeText(this,"Thank you for your purchase!",Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener{
                Toast.makeText(this,"Failed to save to database.",Toast.LENGTH_SHORT).show()
            }
    }


    private val cashInputWatcher = object : TextWatcher {
        override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            // Do nothing
        }

        override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
            // Do nothing
        }

        override fun afterTextChanged(s: Editable?) {
            calculateAndDisplayChange()
        }
    }


}

