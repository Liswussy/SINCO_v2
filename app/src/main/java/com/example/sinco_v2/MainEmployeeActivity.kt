package com.example.sinco_v2

import DeliveryEmployeeFragment
import DeliveryManagerFragment
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class MainEmployeeActivity : AppCompatActivity() {

    private lateinit var bottomNavigation: BottomNavigationView
    private lateinit var userRole: String
    private val auth = FirebaseAuth.getInstance()
    private val firestore = FirebaseFirestore.getInstance()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // get user's role. Default is "Employee"
        val currentUserUid = auth.currentUser?.uid
        currentUserUid?.let {
            firestore.collection("users").document(it)
                .get()
                .addOnSuccessListener { document ->
                    if (document != null) {
                        userRole = document.getString("role") ?: "Employee"
                    } else {
                        Log.d("Firestore", "No such document")
                    }
                }
                .addOnFailureListener { exception ->
                    Log.d("Firestore", "get failed with ", exception)
                }
        }

        bottomNavigation = findViewById(R.id.bottom_navigation)
        bottomNavigation.setOnItemSelectedListener { menuItem ->
            when (menuItem.itemId) {
                R.id.home -> replaceFragment(EmployeeHomeFragment())
                R.id.register -> replaceFragment(RegisterFragment())
                R.id.delivery -> {
                    if (userRole.equals("manager", ignoreCase = true) || userRole.equals("Auditor", ignoreCase = true)){
                        replaceFragment(DeliveryManagerFragment())
                    } else {
                        replaceFragment(DeliveryEmployeeFragment())
                    }
                }
                R.id.orders -> replaceFragment(OrdersFragment())
                R.id.profile -> replaceFragment(ProfileFragment())
            }
            true
        }

        bottomNavigation.selectedItemId = R.id.home
    }

    private fun replaceFragment(fragment: Fragment){
        val fragmentManager = supportFragmentManager

        val transaction = fragmentManager.beginTransaction()
        transaction.replace(R.id.fragment_container, fragment)
        transaction.addToBackStack(null)
        transaction.commit()
    }
}
