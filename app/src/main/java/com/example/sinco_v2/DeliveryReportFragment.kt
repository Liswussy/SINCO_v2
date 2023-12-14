package com.example.sinco_v2

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.Spinner
import android.widget.TextView
import androidx.navigation.fragment.findNavController
import com.google.android.material.datepicker.CalendarConstraints
import com.google.android.material.datepicker.MaterialDatePicker
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.dialog.MaterialDialogs
import com.google.firebase.Timestamp
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.QueryDocumentSnapshot
import com.google.firebase.firestore.QuerySnapshot
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

class DeliveryReportFragment : Fragment() {
    private lateinit var linearlayout1: LinearLayout
    private lateinit var calendarButton: ImageButton
    private lateinit var placeholderTextView: TextView
    var canGetData = true
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_delivery_report, container, false)

        linearlayout1 = view.findViewById(R.id.linearlayout1)
        placeholderTextView = view.findViewById(R.id.placeholderTextView)

        val spinner: Spinner = view.findViewById(R.id.sp_date)
        val adapter = ArrayAdapter.createFromResource(
            requireContext(),
            R.array.date_range_array,
            android.R.layout.simple_spinner_item
        )

        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinner.adapter = adapter
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

                when (selectedItem) {
                    "Today" -> {
                        showDeliveryReports(currentDate, currentDate)
                    }

                    "Yesterday" -> {
                        calendar.add(Calendar.DAY_OF_YEAR, -1)
                        val yesterday = calendar.time
                        showDeliveryReports(yesterday, yesterday)
                    }

                    "Last 7 Days" -> {
                        calendar.add(Calendar.DAY_OF_YEAR, -6)
                        val sevenDaysAgo = calendar.time
                        showDeliveryReports(sevenDaysAgo, currentDate)
                    }

                    "Last 30 Days" -> {
                        calendar.add(Calendar.DAY_OF_YEAR, -29)
                        val thirtyDaysAgo = calendar.time
                        showDeliveryReports(thirtyDaysAgo, currentDate)
                    }

                    "This Month" -> {
                        calendar.set(Calendar.DAY_OF_MONTH, 1)
                        val firstDayOfMonth = calendar.time
                        calendar.add(Calendar.MONTH, 1)
                        calendar.add(Calendar.DAY_OF_MONTH, -1)
                        val lastDayOfMonth = calendar.time
                        showDeliveryReports(firstDayOfMonth, lastDayOfMonth)
                    }

                    "Last Month" -> {
                        calendar.add(Calendar.MONTH, -1)
                        val firstDayOfLastMonth = calendar.time
                        calendar.set(
                            Calendar.DAY_OF_MONTH,
                            calendar.getActualMaximum(Calendar.DAY_OF_MONTH)
                        )
                        val lastDayOfLastMonth = calendar.time
                        showDeliveryReports(firstDayOfLastMonth, lastDayOfLastMonth)
                    }

                    "This Year" -> {
                        calendar.set(Calendar.MONTH, Calendar.JANUARY)
                        calendar.set(Calendar.DAY_OF_MONTH, 1)
                        val firstDayOfYear = calendar.time
                        calendar.add(Calendar.YEAR, 1)
                        calendar.add(Calendar.DAY_OF_MONTH, -1)
                        val lastDayOfYear = calendar.time
                        showDeliveryReports(firstDayOfYear, lastDayOfYear)
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

        calendarButton = view.findViewById(R.id.calendar_icon)
        var canTrigger = true
        calendarButton.setOnClickListener {
            if (canTrigger) {
                datePicker.show(parentFragmentManager, datePicker.toString())
                canTrigger = false
            }
        }
        datePicker.addOnPositiveButtonClickListener { selection ->
            val startDate = selection.first
            val endDate = selection.second

            val start = Date(startDate)
            val end = Date(endDate)

            showDeliveryReports(start, end)

            canTrigger = true
        }
        datePicker.addOnDismissListener {
            canTrigger = true
        }

        return view
    }


    private fun showDeliveryReports(startDate: Date, endDate: Date) {
        if (!canGetData) {
            return
        }

        removeAllViewsExceptFirst(linearlayout1)

        val startDateTimestamp = Timestamp(startOfDay(startDate))
        val endDateTimestamp = Timestamp(endOfDay(endDate))

        val db = Firebase.firestore
        val deliveryRef = db.collection("delivery_reports")
        deliveryRef
            .whereGreaterThanOrEqualTo("date", startDateTimestamp)
            .whereLessThanOrEqualTo("date", endDateTimestamp)
            .orderBy("date", Query.Direction.DESCENDING)
            .get()
            .addOnSuccessListener { documents ->
                removeAllViewsExceptFirst(linearlayout1)
                if (documents.isEmpty) {
                    placeholderTextView.visibility = View.VISIBLE
                } else {
                    placeholderTextView.visibility = View.GONE
                }

                for (document in documents) {
                    val date = document.get("date") as? Timestamp ?: Timestamp.now()
                    val products =
                        document.get("products") as? ArrayList<Map<String, Any>> ?: ArrayList()
                    val supplier = document.get("supplier") as? String ?: ""

                    val inflater = LayoutInflater.from(requireContext())
                    val customItemView =
                        inflater.inflate(R.layout.delivery_report_items, linearlayout1, false)

                    val itemsDeliveredViewed =
                        customItemView.findViewById<TextView>(R.id.itemsDelivered)
                    val numOfDelivered = products.size

                    var itemsDelivered = ""
                    itemsDelivered = if (products.size == 1) {
                        "$numOfDelivered Item Delivered"
                    } else {
                        "$numOfDelivered Items Delivered"
                    }
                    itemsDeliveredViewed.text = itemsDelivered

                    val deliveryIDTextView =
                        customItemView.findViewById<TextView>(R.id.tv_deliveryID)
                    val deliveryIDText = "Delivery ID:\n${document.id}"
                    deliveryIDTextView.text = deliveryIDText

                    val tv_supplier = customItemView.findViewById<TextView>(R.id.tv_supplier)
                    tv_supplier.text = supplier

                    val dateTimeTextView = customItemView.findViewById<TextView>(R.id.tv_date)
                    val dateTimeText = formatDate(date.toDate())
                    dateTimeTextView.text = dateTimeText

                    customItemView.setOnClickListener() {
                        showCustomDialogForDeliveryReport(document)
                    }

                    linearlayout1.addView(customItemView)

                }
                canGetData = true
            }
            .addOnFailureListener { exception ->
                placeholderTextView.visibility = View.VISIBLE
                canGetData = true
            }


    }

    fun showCustomDialogForDeliveryReport(document: QueryDocumentSnapshot) {
        val products = document.get("products") as? ArrayList<Map<String, Any>> ?: ArrayList()
        val date = document.get("date") as? Timestamp ?: Timestamp.now()
        val supplier = document.get("supplier") as? String ?: ""

        val view = LayoutInflater.from(requireContext())
            .inflate(R.layout.custom_dialog_delivery_report, null)
        val builder = MaterialAlertDialogBuilder(requireContext())

        val dateTimeTextView = view.findViewById<TextView>(R.id.tv_date)
        val dateTimeText = formatDate(date.toDate())
        dateTimeTextView.text = dateTimeText

        val tv_supplier = view.findViewById<TextView>(R.id.tv_supplier)
        tv_supplier.text = supplier

        val productTextView = view.findViewById<TextView>(R.id.productTextView)
        var productText = ""

        val db = Firebase.firestore

        for (product in products) {
            val prodID = product["prodID"] as? String ?: ""
            val quantity = product["qty"]

            // Step 1: Retrieve the product document using prodID
            val productRef = db.collection("products").document(prodID)

            productRef.get()
                .addOnSuccessListener { productDocument ->
                    // Step 2: Get the product name from the retrieved product document
                    val productName = productDocument.getString("prdnme")
                    // Step 3: Update productText with the product name
                    productText += "$productName: $quantity\n"

                    // Update the TextView
                    productTextView.text = productText
                }
                .addOnFailureListener { exception ->
                    // Handle failure in fetching the product document
                    println("Failed to fetch product document: $exception")
                }
        }
        builder.setView(view)
            .setTitle(document.id)
            .setPositiveButton("OK") { dialog, which ->
                dialog.dismiss()
            }

        val dialog = builder.create()
        dialog.show()
    }

    fun formatDate(inputDate: Date): String {
        val pattern = "MMM d, yyyy"
        val dateFormat = SimpleDateFormat(pattern, Locale.getDefault())
        return dateFormat.format(inputDate)
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