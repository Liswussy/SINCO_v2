package com.example.sinco_v2

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import com.google.firebase.Timestamp
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale


class OrdersFragment : Fragment() {

    lateinit var linearlayout1: LinearLayout
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_orders, container, false)
        linearlayout1 = view.findViewById(R.id.linearlayout1)
        showOrderInformation()

        return view
    }

    private fun showOrderInformation(){
        val db = Firebase.firestore
        val docRef = db.collection("orders")
        docRef.get()
            .addOnSuccessListener { documents ->
                for (document in documents){
                    val products = document.get("products") as? ArrayList<Map<String, Any>> ?: ArrayList()
                    val totalAmount = document.get("total") as? Double ?: 0.0
                    val date = document.get("timestamp") as? Timestamp ?: Timestamp.now()

                    val inflater = LayoutInflater.from(requireContext())
                    val customTextView = inflater.inflate(R.layout.orders_items, linearlayout1, false)

                    val productTextView = customTextView.findViewById<TextView>(R.id.tv_product_name)
                    var productText = ""
                    for (product in products){
                        val name = product["productName"] as String
                        val quantity = product["quantity"]

                        productText = "$name \nQuantity: $quantity"
                    }
                    productTextView.text = productText

                    val totalAmountTextView = customTextView.findViewById<TextView>(R.id.tv_amount)
                    val totalAmountText =  "Total: ${formatDoubleToTwoDecimalPlaces(totalAmount)}"
                    totalAmountTextView.text = totalAmountText

                    val dateTimeTextView = customTextView.findViewById<TextView>(R.id.tv_date)
                    val dateTimeText = formatDate(date.toDate())
                    dateTimeTextView.text = dateTimeText

                    linearlayout1.addView(customTextView)

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
}