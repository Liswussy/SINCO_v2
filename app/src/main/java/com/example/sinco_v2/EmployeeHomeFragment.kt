package com.example.sinco_v2

import android.content.ContentValues.TAG
import android.media.Image
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.squareup.picasso.Picasso
import java.time.LocalDateTime
import java.util.Calendar
import java.util.TimeZone

class EmployeeHomeFragment : Fragment() {

    private lateinit var scrollContainer: LinearLayout
    val db = Firebase.firestore

    private lateinit var textview_customers: TextView
    private lateinit var textview_transaction: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_employee_home, container, false)

        textview_customers = view.findViewById(R.id.textview_customers)
        textview_transaction = view.findViewById(R.id.textview_transaction)
        showOrdersCount()

        scrollContainer= view.findViewById(R.id.scrollContainer)
        showBestSelling()

        return view
    }

    private fun showOrdersCount() {
        val phTimeZone = TimeZone.getTimeZone("Asia/Manila")
        val calendar = Calendar.getInstance(phTimeZone)

        // Get the current date in PH Time
        val currentDate = calendar.time

        // Calculate the start of the day (midnight) timestamp
        calendar.time = currentDate
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)
        val startOfDay = calendar.time

        // Calculate the end of the day (just before midnight) timestamp
        calendar.time = currentDate
        calendar.set(Calendar.HOUR_OF_DAY, 23)
        calendar.set(Calendar.MINUTE, 59)
        calendar.set(Calendar.SECOND, 59)
        calendar.set(Calendar.MILLISECOND, 999)
        val endOfDay = calendar.time

        val ordersCollection = db.collection("orders")

        ordersCollection
            .whereGreaterThanOrEqualTo("timestamp", startOfDay)
            .whereLessThan("timestamp", endOfDay)
            .get()
            .addOnSuccessListener { documents ->
                var totalSum = 0.0
                var totalTransactions = 0
                for (document in documents) {
                    val total:Double = document.getDouble("total")!!
                    totalSum += total
                    totalTransactions += 1
                }
                textview_customers.text = totalTransactions.toString()
                // Format the totalSum to a string with two decimal places
                val formattedTotal = String.format("%.2f", totalSum)
                textview_transaction.text = "Php " + formattedTotal

            }
            .addOnFailureListener { exception ->

            }
    }

    private fun showBestSelling() {
        val currentDate = Calendar.getInstance()
        currentDate.set(Calendar.HOUR_OF_DAY, 0)
        currentDate.set(Calendar.MINUTE, 0)
        currentDate.set(Calendar.SECOND, 0)
        currentDate.set(Calendar.MILLISECOND, 0)
        currentDate.set(Calendar.DAY_OF_WEEK, Calendar.SUNDAY)
        val firstDayOfWeek = currentDate.time
        currentDate.add(Calendar.DAY_OF_WEEK, 6)
        currentDate.set(Calendar.HOUR_OF_DAY, 23)
        currentDate.set(Calendar.MINUTE, 59)
        currentDate.set(Calendar.SECOND, 59)
        val lastDayOfWeek = currentDate.time

        val firestore = FirebaseFirestore.getInstance()
        val query = firestore.collection("orders")
            .whereGreaterThanOrEqualTo("timestamp", firstDayOfWeek)
            .whereLessThanOrEqualTo("timestamp", lastDayOfWeek)

        val productQuantities = mutableMapOf<String, Long>()
        val productNames = mutableMapOf<String, String>()



        query.get()
            .addOnSuccessListener { documents ->
                for (document in documents) {
                    val products = document["products"] as List<Map<String, Any>>?
                    if (products != null) {
                        for (product in products) {
                            val productID = product["productID"] as String
                            val productName = product["productName"] as String
                            val quantity = product["quantity"] as Long

                            // Aggregate product quantities
                            productQuantities[productID] =
                                productQuantities.getOrDefault(productID, 0L) + quantity

                            // Store product name
                            productNames[productID] = productName
                        }
                    }
                }

                // Sort products by quantity in descending order
                val sortedProducts = productQuantities.entries.sortedByDescending { it.value }

                // Get the top 5 products
                val topProducts = sortedProducts.take(5)

                // Now, topProducts contains the top 5 products with the most combined quantity in the current month
                // You can use this list as needed
                // For example, you can print the top products:
                for ((index, entry) in topProducts.withIndex()) {
                    val productID = entry.key
                    val quantity = entry.value
                    val productName = productNames[productID]
//                    println("Top Product ${index + 1}:")
//                    println("Product ID: $productID")
//                    println("Quantity: $quantity")
//                    println()

                    val inflater = LayoutInflater.from(requireContext())
                    val presetView = inflater.inflate(R.layout.best_selling_items, scrollContainer, false)

                    val productImg = presetView.findViewById<ImageView>(R.id.productImg)
                    val productNameTextView = presetView.findViewById<TextView>(R.id.productNameTextView)
                    val productSoldTextView = presetView.findViewById<TextView>(R.id.productSoldTextView)

                    val docRef = db.collection("products").document(productID)
                    docRef.get()
                        .addOnSuccessListener { document ->
                            if (document != null) {
                                val imgSrc = document.getString("imgSrc")
                                Picasso.get()
                                    .load(imgSrc)
                                    .placeholder(R.drawable.beer_icon) // Placeholder image resource
                                    .error(R.drawable.beer_icon) // Error image resource (optional)
                                    .into(productImg)
                            } else {
                                Log.d(TAG, "No such document")
                            }
                        }
                        .addOnFailureListener { exception ->
                            Log.d(TAG, "get failed with ", exception)
                        }


                    productNameTextView.text = productName
                    productSoldTextView.text = "Sold : $quantity"


                    scrollContainer.addView(presetView)
                }
            }
            .addOnFailureListener { exception ->
                println("Error getting documents: $exception")
            }
    }

}