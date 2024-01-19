package com.example.sinco_v2

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import com.google.android.material.datepicker.CalendarConstraints
import com.google.android.material.datepicker.MaterialDatePicker
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import java.util.Calendar
import java.util.Date

class ConsignorProductSalesActivity : AppCompatActivity() {

    private lateinit var linearlayoutTemplate: LinearLayout
    private lateinit var placeholderTextView: TextView
    private lateinit var calendarButton: ImageButton
    private var totalItemSold = 0
    private var totalAmountSold = 0.0


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_consignor_product_sales)
        linearlayoutTemplate = findViewById(R.id.linearlayoutTemplate)
        placeholderTextView = findViewById(R.id.placeHolderTextView)

        val ib_back_icon = findViewById<ImageButton>(R.id.ib_back_icon)
        ib_back_icon.setOnClickListener {
            finish()
        }


        // Move this line here, after consignorID is retrieved

        val spinner = findViewById<Spinner>(R.id.spinner)
        val adapter = ArrayAdapter.createFromResource(
            this,
            R.array.date_range_array,
            android.R.layout.simple_spinner_item
        )
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinner.adapter = adapter

        val calendar1 = Calendar.getInstance()
        val currentDate1 = calendar1.time
        getConsignorTransactionData(currentDate1, currentDate1)

        spinner.setOnItemSelectedListener(object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>?,
                view: View?,
                position: Int,
                id: Long
            ) {

                val selectedItem = parent?.getItemAtPosition(position).toString()
                val calendar = Calendar.getInstance()
                val currentDate = calendar.time

                removeAllViewsExceptFirst(linearlayoutTemplate)

                when (selectedItem) {
                    "Today" -> {
                        getConsignorTransactionData(currentDate, currentDate)
                    }

                    "Yesterday" -> {
                        calendar.add(Calendar.DAY_OF_YEAR, -1)
                        val yesterday = calendar.time
                        getConsignorTransactionData(yesterday, yesterday)
                    }

                    "Last 7 Days" -> {
                        calendar.add(Calendar.DAY_OF_YEAR, -6)
                        val sevenDaysAgo = calendar.time
                        getConsignorTransactionData(sevenDaysAgo, currentDate)
                    }

                    "Last 30 Days" -> {
                        calendar.add(Calendar.DAY_OF_YEAR, -29)
                        val thirtyDaysAgo = calendar.time
                        getConsignorTransactionData(thirtyDaysAgo, currentDate)
                    }

                    "This Month" -> {
                        calendar.set(Calendar.DAY_OF_MONTH, 1)
                        val firstDayOfMonth = calendar.time
                        calendar.add(Calendar.MONTH, 1)
                        calendar.add(Calendar.DAY_OF_MONTH, -1)
                        val lastDayOfMonth = calendar.time
                        getConsignorTransactionData(firstDayOfMonth, lastDayOfMonth)
                    }

                    "Last Month" -> {
                        calendar.add(Calendar.MONTH, -1)
                        val firstDayOfLastMonth = calendar.time
                        calendar.set(
                            Calendar.DAY_OF_MONTH,
                            calendar.getActualMaximum(Calendar.DAY_OF_MONTH)
                        )
                        val lastDayOfLastMonth = calendar.time
                        getConsignorTransactionData(
                            firstDayOfLastMonth,
                            lastDayOfLastMonth
                        )

                    }

                    "This Year" -> {
                        calendar.set(Calendar.MONTH, Calendar.JANUARY)
                        calendar.set(Calendar.DAY_OF_MONTH, 1)
                        val firstDayOfYear = calendar.time
                        calendar.add(Calendar.YEAR, 1)
                        calendar.add(Calendar.DAY_OF_MONTH, -1)
                        val lastDayOfYear = calendar.time
                        getConsignorTransactionData(firstDayOfYear, lastDayOfYear)

                    }
                }
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {
                // Handle the case where nothing is selected if needed
            }
        })
        spinner.setSelection(0)

        val minDate = Calendar.getInstance()
        minDate.set(2023, Calendar.JANUARY, 1)

        // Set the maximum date to today
        val maxDate = Calendar.getInstance()

        val builder = MaterialDatePicker.Builder.dateRangePicker()
            .setTitleText("Select a date range")

        // Create custom DateValidator to restrict the selection
        val dateValidator = object : CalendarConstraints.DateValidator {
            override fun isValid(date: Long): Boolean {
                return date >= minDate.timeInMillis && date <= maxDate.timeInMillis
            }

            override fun writeToParcel(dest: android.os.Parcel, flags: Int) {
                // Implement writeToParcel as required
            }

            override fun describeContents(): Int {
                // Implement describeContents as required
                return 0
            }
        }

        // Create CalendarConstraints
        val constraintsBuilder = CalendarConstraints.Builder()
        constraintsBuilder.setStart(minDate.timeInMillis)
        constraintsBuilder.setEnd(maxDate.timeInMillis)
        constraintsBuilder.setValidator(dateValidator)

        builder.setCalendarConstraints(constraintsBuilder.build())

        val datePicker = builder.build()

        calendarButton = findViewById(R.id.calendar_icon)
        var canTrigger = true
        calendarButton.setOnClickListener {
            if (canTrigger) {
                // Remove views when the calendar icon is clicked
                removeAllViewsExceptFirst(linearlayoutTemplate)

                datePicker.show(supportFragmentManager, datePicker.toString())
                canTrigger = false
            }
        }

        datePicker.addOnPositiveButtonClickListener { selection ->
            val startDate = selection.first
            val endDate = selection.second

            val start = Date(startDate)
            val end = Date(endDate)

            getConsignorTransactionData(start, end)

            canTrigger = true
        }
        datePicker.addOnDismissListener {
            canTrigger = true
        }
    }

    private fun getConsignorTransactionData(startOfDay: Date, endOfDay: Date) {

        // val suppliersCollection = db.collection("suppliers")
        val consignorID = intent.getStringExtra("consignorID")
        val db = FirebaseFirestore.getInstance()

        // Reference to the "orders" collection
        val ordersCollection = db.collection("orders")

        // Query to filter data based on the consignorID and date range
        val query = consignorID?.let {
            ordersCollection
                .whereArrayContains("consignorID", it)
                .whereGreaterThanOrEqualTo("timestamp", startOfDay)
                .whereLessThanOrEqualTo("timestamp", endOfDay)
                .orderBy("timestamp", Query.Direction.DESCENDING)
        } // Order by timestamp in descending order

        // Execute the query
        if (query != null) {
            query.get()
                .addOnSuccessListener { querySnapshot ->
                    val linearLayout = findViewById<LinearLayout>(R.id.linearlayout1)

                    removeAllViewsExceptFirst(linearLayout)
                    if (querySnapshot.isEmpty) {
                        placeholderTextView.visibility = View.VISIBLE

                    } else {
                        placeholderTextView.visibility = View.GONE
                    }

                    for (document in querySnapshot) {
                        // Access the "products" array from the document
                        val products = document.get("products") as? List<Map<String, Any>>
                        // Check if products is not null and is a list

                        if (products != null) {
                            // Iterate through the products
                            for (product in products) {
                                // Access data for each product
                                val productName = product["productName"] as? String
                                val quantity = product["quantity"] as? Long
                                val price = product["price"] as? Double

                                val productAmount = (price ?: 0.0) * (quantity ?: 0)

                                totalItemSold += quantity?.toInt() ?: 0
                                totalAmountSold += productAmount

                                // Create a TextView using LayoutInflater
                                val inflater = LayoutInflater.from(this)
                                val customComponent = inflater.inflate(
                                    R.layout.consignor_product_items,
                                    linearlayoutTemplate,
                                    false
                                )

                                val prdname =
                                    customComponent.findViewById<TextView>(R.id.productNameTextView)
                                val sold = customComponent.findViewById<TextView>(R.id.soldTextView)
                                val amount =
                                    customComponent.findViewById<TextView>(R.id.totalAmountTextView)

                                // Set the text for the TextView using productName instead of productID
                                prdname.text = productName
                                sold.text = quantity.toString()
                                amount.text = productAmount.toString()

                                // Add the TextView to the LinearLayout
                                linearlayoutTemplate.addView(customComponent)

                                val totalSoldTextView =
                                    findViewById<TextView>(R.id.totalSoldTextView)
                                totalSoldTextView.text = totalItemSold.toString()

                                val totalAmountTextView =
                                    findViewById<TextView>(R.id.addedAmountTextView)
                                totalAmountTextView.text = totalAmountSold.toString()

                            }
                        }
                    }
                    // Show a Toast message indicating successful data retrieval
                    Toast.makeText(
                        this@ConsignorProductSalesActivity,
                        "Data retrieved successfully",
                        Toast.LENGTH_SHORT
                    ).show()
                }
                .addOnFailureListener { exception ->
                    // Handle failures
                    Log.e("ConsignorProductSales", "Error getting documents", exception)

                    // Show a Toast message indicating an error
                    Toast.makeText(
                        this@ConsignorProductSalesActivity,
                        "Error getting data: ${exception.message}",
                        Toast.LENGTH_SHORT
                    ).show()
                }
        }
    }

    fun removeAllViewsExceptFirst(layout: ViewGroup) {
        // Ensure that the layout has at least one child view
        if (layout.childCount > 1) {
            // Iterate through child views starting from index 1 (second view)
            for (i in layout.childCount - 1 downTo 1) {
                val childView = layout.getChildAt(i)
                // Remove the view from the layout
                layout.removeView(childView)
            }
        }
    }

    fun startOfDay(inputDate: Date): Date {
        val calendar = Calendar.getInstance()
        calendar.time = inputDate
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)
        return calendar.time
    }

    fun endOfDay(inputDate: Date): Date {
        val calendar = Calendar.getInstance()
        calendar.time = inputDate
        calendar.set(Calendar.HOUR_OF_DAY, 23)
        calendar.set(Calendar.MINUTE, 59)
        calendar.set(Calendar.SECOND, 59)
        calendar.set(Calendar.MILLISECOND, 999)
        return calendar.time
    }


}
