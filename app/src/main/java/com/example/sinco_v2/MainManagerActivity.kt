package com.example.sinco_v2

import DeliveryEmployeeFragment
import DeliveryManagerFragment
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class MainManagerActivity : AppCompatActivity() {

    private lateinit var bottomNavigation: BottomNavigationView
    private lateinit var userRole: String
    private val auth = FirebaseAuth.getInstance()
    private val firestore = FirebaseFirestore.getInstance()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_manager2)

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
                R.id.m_home -> replaceFragment(ManagerHomeFragment())
                R.id.sales -> replaceFragment(SalesFragment())
                R.id.inventory -> replaceFragment(InventoryFragment())
                R.id.delivery -> {
                    if (userRole.equals("manager", ignoreCase = true) || userRole.equals("Auditor", ignoreCase = true)){
                        replaceFragment(DeliveryManagerFragment())
                    } else {
                        replaceFragment(DeliveryEmployeeFragment())
                    }
                }
                R.id.profile -> replaceFragment(ProfileFragment())
            }
            true
        }

        bottomNavigation.selectedItemId = R.id.m_home
    }

    private fun replaceFragment(fragment: Fragment) {
        val fragmentManager = supportFragmentManager

        val transaction = fragmentManager.beginTransaction()
        transaction.replace(R.id.fragment_container, fragment)
        transaction.addToBackStack(null)
        transaction.commit()
    }
}



