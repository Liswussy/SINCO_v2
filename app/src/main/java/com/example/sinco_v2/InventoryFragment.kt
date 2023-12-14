package com.example.sinco_v2

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import com.google.android.material.tabs.TabLayout
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

class InventoryFragment : Fragment() {
private lateinit var linearLayout1: LinearLayout
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_inventory, container, false)

        // Add your fragments for each tab
        val allinventory = AllInventoryFragment()// AllInventoryFragment()
        val stocktake = StockTakeFragment()//StockTakeFragment()

        parentFragmentManager.beginTransaction()
            .replace(R.id.fragmentContainer, allinventory)
            .commit()

        val tabLayout = view.findViewById<TabLayout>(R.id.tabLayout)
        tabLayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab?) {
                // Handle tab selection
                when (tab?.position) {
                    0 -> {
                        parentFragmentManager.beginTransaction()
                            .replace(R.id.fragmentContainer, allinventory)
                            .commit()
                    }
                    1 -> {
                        parentFragmentManager.beginTransaction()
                            .replace(R.id.fragmentContainer, stocktake)
                            .commit()
                    }
                }
            }

            override fun onTabUnselected(tab: TabLayout.Tab?) {
                // Handle tab unselection
            }

            override fun onTabReselected(tab: TabLayout.Tab?) {
                // Handle tab reselection
            }
        })



        return view
    }

    private fun showProductInformation(){
        val db = Firebase.firestore
        val docRef = db.collection("products")
        docRef.get()
            .addOnSuccessListener { documents ->
                for (document in documents){
                    val consignorName = document.get("name") as? String ?: ""

                    val inflater = LayoutInflater.from(requireContext())
                    val customTextView = inflater.inflate(R.layout.consignor_items, linearLayout1, false)

                    val consignorTextView = customTextView.findViewById<TextView>(R.id.consignorTextView)
                    val consignorText = consignorName
                    consignorTextView.text = consignorText

                    linearLayout1.addView(customTextView)

                }

            }

    }


    }

