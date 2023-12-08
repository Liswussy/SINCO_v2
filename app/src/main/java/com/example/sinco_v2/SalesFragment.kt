package com.example.sinco_v2

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.viewpager.widget.ViewPager
import com.google.android.material.tabs.TabLayout

class SalesFragment : Fragment() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_sales, container, false)

        // Add your fragments for each tab
        val allSalesFragment = AllSalesFragment()// AllSalesFragment()
        val consignorsFragment = ConsignorSalesFragment()//ConsignorsFragment()

        parentFragmentManager.beginTransaction()
            .replace(R.id.fragmentContainer, allSalesFragment)
            .commit()

        val tabLayout = view.findViewById<TabLayout>(R.id.tabLayout)
        tabLayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab?) {
                // Handle tab selection
                when (tab?.position) {
                    0 -> {
                        parentFragmentManager.beginTransaction()
                            .replace(R.id.fragmentContainer, allSalesFragment)
                            .commit()
                    }
                    1 -> {
                        parentFragmentManager.beginTransaction()
                            .replace(R.id.fragmentContainer, consignorsFragment)
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

}