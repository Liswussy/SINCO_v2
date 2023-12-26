package com.example.sinco_v2

import DeliveryFragment
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.fragment.app.Fragment
import com.google.android.material.bottomnavigation.BottomNavigationView

private lateinit var bottomNavigation: BottomNavigationView

class ManagerActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_manager2)

        bottomNavigation = findViewById(R.id.bottom_navigation)
        bottomNavigation.setOnItemSelectedListener { menuItem ->
            when (menuItem.itemId) {
                R.id.m_home -> replaceFragment(ManagerHomeFragment())
                R.id.sales -> replaceFragment(SalesFragment())
                R.id.inventory -> replaceFragment(InventoryFragment())
                R.id.delivery -> replaceFragment(DeliveryFragment())
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



