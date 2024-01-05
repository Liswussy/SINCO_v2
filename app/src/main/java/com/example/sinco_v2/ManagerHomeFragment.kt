package com.example.sinco_v2

import ProductFragment
import SupplierFragment
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
import androidx.lifecycle.findViewTreeViewModelStoreOwner
import androidx.navigation.fragment.NavHostFragment.Companion.findNavController
import androidx.navigation.fragment.findNavController
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.QuerySnapshot
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import java.util.Calendar
import java.util.TimeZone


class ManagerHomeFragment : Fragment() {

private lateinit var layoutLowestQuantityTextView : LinearLayout
private lateinit var textview_transaction : TextView
val db = Firebase.firestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view =  inflater.inflate(R.layout.fragment_manager_home, container, false)

        layoutLowestQuantityTextView = view.findViewById(R.id.layoutLowestQuantity)
        textview_transaction = view.findViewById(R.id.salesTextView)
        showSalesMonthlyCount()
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Assuming you have a button or some other UI element that triggers the redirection
        val redirectToProductButton = view.findViewById<ImageButton>(R.id.product_icon)
        val redirectToSupplierButton = view.findViewById<ImageButton>(R.id.supplier_icon)
        val redirectToEmployeeButton = view.findViewById<ImageButton>(R.id.employee_icon)
        val redirectToReturnButton = view.findViewById<ImageButton>(R.id.return_icon)

        redirectToProductButton.setOnClickListener {
            redirectToFragment(ProductFragment())
        }
        redirectToSupplierButton.setOnClickListener {
            redirectToFragment(SupplierFragment())
        }

        redirectToEmployeeButton.setOnClickListener {
            redirectToFragment(EmployeeFragment())
        }
        redirectToReturnButton.setOnClickListener {
            redirectToFragment(ReturnProductFragment())
        }

        fetchActiveEmployeesCount()
        fetchTop3Products()


    }

    private fun fetchTop3Products() {
        val db = FirebaseFirestore.getInstance()
        val collectionReference = db.collection("products")

        collectionReference
            .orderBy("qnty") // Order by the quantity field in ascending order
            .limit(5) // Limit the result to the top 3 products
            .get()
            .addOnSuccessListener { querySnapshot: QuerySnapshot ->
                // Handle the result
                for (document in querySnapshot.documents) {
                    val productName = document.getString("prdnme")
                    val productQuantity = document.getDouble("qnty")?.toInt() ?: 0

                    val inflater = LayoutInflater.from(requireContext())
                    val customItemView = inflater.inflate(R.layout.least_quantity_product_items, layoutLowestQuantityTextView, false)

                    // Check if quantity is less than 15
                    if (productQuantity < 30) {
                        // Use the retrieved data as needed
                        val topLeastProductTextView = customItemView.findViewById<TextView>(R.id.topLeastProductTextView)
                        topLeastProductTextView.text = productName
                        val topLeastQuantityProductTextView = customItemView.findViewById<TextView>(R.id.topLeastQuantityProductTextView)
                        topLeastQuantityProductTextView.text = productQuantity.toString()

                        // Add the customItemView to your layout
                        layoutLowestQuantityTextView.addView(customItemView)
                    }
                }
            }
            .addOnFailureListener { exception ->
                // Handle errors here
            }
    }
    private fun fetchActiveEmployeesCount() {
        val db = FirebaseFirestore.getInstance()
        val collectionReference = db.collection("users")

        collectionReference.get()
            .addOnSuccessListener { querySnapshot: QuerySnapshot ->
                val count = querySnapshot.size()
                val tv_active_employees = view?.findViewById<TextView>(R.id.tv_active)
                tv_active_employees?.text = count.toString()
            }
            .addOnFailureListener { exception ->
                // Handle errors here
            }
    }


    private fun showSalesMonthlyCount() {
        val phTimeZone = TimeZone.getTimeZone("Asia/Manila")
        val calendar = Calendar.getInstance(phTimeZone)

        // Get the current date in PH Time
        val currentDate = calendar.time

        calendar.time = currentDate
        calendar.set(Calendar.DAY_OF_MONTH, 1) // Set the day to the first day of the month
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)
        val startOfMonth = calendar.time

// Calculate the end of the month timestamp
        calendar.time = currentDate
        calendar.set(Calendar.DAY_OF_MONTH, calendar.getActualMaximum(Calendar.DAY_OF_MONTH)) // Set the day to the last day of the month
        calendar.set(Calendar.HOUR_OF_DAY, 23)
        calendar.set(Calendar.MINUTE, 59)
        calendar.set(Calendar.SECOND, 59)
        calendar.set(Calendar.MILLISECOND, 999)
        val endOfMonth = calendar.time

        val ordersCollection = db.collection("orders")

        ordersCollection
            .whereGreaterThanOrEqualTo("timestamp", startOfMonth)
            .whereLessThan("timestamp", endOfMonth)
            .get()
            .addOnSuccessListener { documents ->
                var totalSum = 0.0
                var totalTransactions = 0
                for (document in documents) {
                    val total:Double = document.getDouble("total")!!
                    totalSum += total
                    totalTransactions += 1
                }
                // Format the totalSum to a string with two decimal places
                val formattedTotal = String.format("%.2f", totalSum)
                textview_transaction.text = "Php " + formattedTotal

            }
            .addOnFailureListener { exception ->

            }
    }


    private fun redirectToFragment(fragment: Fragment) {

        val fragmentTransaction = requireActivity().supportFragmentManager.beginTransaction()
        fragmentTransaction.replace(R.id.fragment_container, fragment)
        fragmentTransaction.addToBackStack(null)
        fragmentTransaction.commit()

    }



}




