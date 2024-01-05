package com.example.sinco_v2

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.Spinner
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.firebase.Timestamp
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.QueryDocumentSnapshot
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale


class OrdersFragment : Fragment() {

    private lateinit var linearlayout1: LinearLayout
    private lateinit var placeholderTextView: TextView
    var canGetData = true



    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_orders, container, false)

        linearlayout1 = view.findViewById(R.id.linearlayout1)
        placeholderTextView = view.findViewById(R.id.placeholderTextView)
        //showOrderInformation()


        val calendar1 = Calendar.getInstance()
        val currentDate1 = calendar1.time
        getTransactionHistoryData(currentDate1, currentDate1)

        val spinner: Spinner = view.findViewById(R.id.spinnerDate)
        val adapter = ArrayAdapter.createFromResource(
            requireContext(),
            R.array.date_range_array,
            android.R.layout.simple_spinner_item
        )

        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinner.adapter = adapter
        spinner.setOnItemSelectedListener(object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {

                val selectedItem = parent?.getItemAtPosition(position).toString()

                val calendar = Calendar.getInstance()
                val currentDate = calendar.time

                when (selectedItem) {
                    "Today" -> {
                        getTransactionHistoryData(currentDate, currentDate)
                    }

                    "Yesterday" -> {
                        calendar.add(Calendar.DAY_OF_YEAR, -1)
                        val yesterday = calendar.time
                        getTransactionHistoryData(yesterday, yesterday)
                    }

                    "Last 7 Days" -> {
                        calendar.add(Calendar.DAY_OF_YEAR, -6)
                        val sevenDaysAgo = calendar.time
                        getTransactionHistoryData(sevenDaysAgo, currentDate)
                    }

                    "Last 30 Days" -> {
                        calendar.add(Calendar.DAY_OF_YEAR, -29)
                        val thirtyDaysAgo = calendar.time
                        getTransactionHistoryData(thirtyDaysAgo, currentDate)
                    }

                    "This Month" -> {
                        calendar.set(Calendar.DAY_OF_MONTH, 1)
                        val firstDayOfMonth = calendar.time
                        calendar.add(Calendar.MONTH, 1)
                        calendar.add(Calendar.DAY_OF_MONTH, -1)
                        val lastDayOfMonth = calendar.time
                        getTransactionHistoryData(firstDayOfMonth, lastDayOfMonth)
                    }

                    "Last Month" -> {
                        calendar.add(Calendar.MONTH, -1)
                        val firstDayOfLastMonth = calendar.time
                        calendar.set(
                            Calendar.DAY_OF_MONTH,
                            calendar.getActualMaximum(Calendar.DAY_OF_MONTH)
                        )
                        val lastDayOfLastMonth = calendar.time
                        getTransactionHistoryData(firstDayOfLastMonth, lastDayOfLastMonth)
                    }

                    "This Year" -> {
                        calendar.set(Calendar.MONTH, Calendar.JANUARY)
                        calendar.set(Calendar.DAY_OF_MONTH, 1)
                        val firstDayOfYear = calendar.time
                        calendar.add(Calendar.YEAR, 1)
                        calendar.add(Calendar.DAY_OF_MONTH, -1)
                        val lastDayOfYear = calendar.time
                        getTransactionHistoryData(firstDayOfYear, lastDayOfYear)
                    }
                }
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {
                // Handle the case where nothing is selected if needed
            }
        })
        spinner.setSelection(0)



        // set the dropdown layout resource
//        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
//
//        spinner.adapter = adapter

        return view
    }

//    private fun showOrderInformation(){
//        val db = Firebase.firestore
//        val docRef = db.collection("orders")
//        docRef.get()
//            .addOnSuccessListener { documents ->
//                for (document in documents){
//                    val products = document.get("products") as? ArrayList<Map<String, Any>> ?: ArrayList()
//                    val totalAmount = document.get("total") as? Double ?: 0.0
//                    val date = document.get("timestamp") as? Timestamp ?: Timestamp.now()
//
//                    val inflater = LayoutInflater.from(requireContext())
//                    val customTextView = inflater.inflate(R.layout.orders_items, linearlayout1, false)
//
//                    val productTextView = customTextView.findViewById<TextView>(R.id.tv_product_name)
//                    var productText = ""
//                    for (product in products){
//                        val name = product["productName"] as String
//                        val quantity = product["quantity"]
//
//                        productText = "$name \nQuantity: $quantity"
//                    }
//                    productTextView.text = productText
//
//                    val totalAmountTextView = customTextView.findViewById<TextView>(R.id.tv_amount)
//                    val totalAmountText =  "Total: ${formatDoubleToTwoDecimalPlaces(totalAmount)}"
//                    totalAmountTextView.text = totalAmountText
//
//                    val dateTimeTextView = customTextView.findViewById<TextView>(R.id.tv_date)
//                    val dateTimeText = formatDate(date.toDate())
//                    dateTimeTextView.text = dateTimeText
//
//                    linearlayout1.addView(customTextView)
//
//                }
//
//            }
//
//    }

    private fun getTransactionHistoryData(startDate: Date, endDate: Date){
        if (!canGetData){
            return
        }

        removeAllViewsExceptFirst(linearlayout1)

        // convert dates to firebase timestamps
        val startDateTimestamp = Timestamp(startOfDay(startDate))
        val endDateTimestamp = Timestamp(endOfDay(endDate))

        val db = Firebase.firestore
        val ordersRef = db.collection("orders")
        ordersRef
            .whereGreaterThanOrEqualTo("timestamp", startDateTimestamp)
            .whereLessThanOrEqualTo("timestamp", endDateTimestamp)
            .orderBy("timestamp", Query.Direction.DESCENDING)
            .get()
            .addOnSuccessListener { documents ->
                removeAllViewsExceptFirst(linearlayout1)
                if (documents.isEmpty){
                    placeholderTextView.visibility = View.VISIBLE
                } else {
                    placeholderTextView.visibility = View.GONE
                }

                for (document in documents) {
                    val products = document.get("products") as? ArrayList<Map<String, Any>> ?: ArrayList()
                    val totalAmount = document.get("total") as? Double ?: 0.0
                    val date = document.get("timestamp") as? Timestamp ?: Timestamp.now()

                    val inflater = LayoutInflater.from(requireContext())
                    val customItemView = inflater.inflate(R.layout.orders_items, linearlayout1, false)

                    val productTextView = customItemView.findViewById<TextView>(R.id.tv_product_name)
                    var productText = ""
                    for (product in products){
                        val name = product["productName"] as String
                        val quantity = product["quantity"]

                        productText = "$name \nQuantity: $quantity"
                    }
                    productTextView.text = productText

                    val totalAmountTextView = customItemView.findViewById<TextView>(R.id.tv_amount)
                    val totalAmountText =  "Total: ${formatDoubleToTwoDecimalPlaces(totalAmount)}"
                    totalAmountTextView.text = totalAmountText

                    val dateTimeTextView = customItemView.findViewById<TextView>(R.id.tv_date)
                    val dateTimeText = formatDate(date.toDate())
                    dateTimeTextView.text = dateTimeText

                    customItemView.setOnClickListener{
                        showCustomDialog(document)
                    }

                    linearlayout1.addView(customItemView)
                }

                canGetData = true
            }

    }

    fun showCustomDialog(document: QueryDocumentSnapshot) {
        val paymentMethod = document.get("paymentMethod") as? String ?: ""
        val products = document.get("products") as? ArrayList<Map<String, Any>> ?: ArrayList()
        val timestamp = document.get("timestamp") as? Timestamp ?: Timestamp.now()
        val total = document.get("total") as? Double ?: 0.0

        val view = LayoutInflater.from(requireContext()).inflate(R.layout.custom_dialog_orders, null)
        val builder = MaterialAlertDialogBuilder(requireContext())

        val dateTimeTextView = view.findViewById<TextView>(R.id.dateTimeTextView)
        val dateTimeText = formatDate(timestamp.toDate())
        dateTimeTextView.text = dateTimeText

        val paymentTypeTextView = view.findViewById<TextView>(R.id.paymentTypeTextView)
        val paymentTypeText = "$paymentMethod Payment"
        paymentTypeTextView.text = paymentTypeText

        val totalAmountTextView = view.findViewById<TextView>(R.id.totalAmountTextView)
        val totalAmountText = "Php ${formatDoubleToTwoDecimalPlaces(total)}"
        totalAmountTextView.text = totalAmountText

        val productsTextView = view.findViewById<TextView>(R.id.productsTextView)
        var productsText = ""
        for (product in products){
            val price = product["price"] as Double
            val name = product["productName"] as String
            val quantity = product["quantity"]

            productsText += "\n$name - Php${formatDoubleToTwoDecimalPlaces(price)} x ${quantity}"
        }
        productsTextView.text = productsText

        builder.setView(view)
            .setTitle(document.id)
            .setPositiveButton("OK") { dialog, which ->
                dialog.dismiss()
            }

        val dialog = builder.create()
        dialog.show()
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

    fun formatDate(inputDate: Date): String {
        val pattern = "MMM d, yyyy h:mm a"
        val dateFormat = SimpleDateFormat(pattern, Locale.getDefault())
        return dateFormat.format(inputDate)
    }

    fun formatDoubleToTwoDecimalPlaces(value: Double): String {
        return String.format("%.2f", value)
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