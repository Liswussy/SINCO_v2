package com.example.sinco_v2

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import androidx.lifecycle.findViewTreeViewModelStoreOwner
import androidx.navigation.fragment.NavHostFragment.Companion.findNavController
import androidx.navigation.fragment.findNavController


class ManagerHomeFragment : Fragment() {


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_manager_home, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val imageButton_1: ImageButton = view.findViewById(R.id.product_icon)
        imageButton_1.setOnClickListener {
            // Navigate to FragmentB
            val productFragment = ProductFragment()
            val transaction = requireActivity().supportFragmentManager.beginTransaction()
            transaction.replace(R.id.fragment_container, productFragment)
            transaction.addToBackStack(null)  // Optional, adds the transaction to the back stack
            transaction.commit()

        }

        val imageButton_2: ImageButton = view.findViewById(R.id.supplier_icon)
        imageButton_2.setOnClickListener {
            // Navigate to FragmentC
            val supplierFragment = SupplierFragment()
            val transaction = requireActivity().supportFragmentManager.beginTransaction()
            transaction.replace(R.id.fragment_container, supplierFragment)
            transaction.addToBackStack(null)  // Optional, adds the transaction to the back stack
            transaction.commit()

        }

        val imageButton_3: ImageButton = view.findViewById(R.id.employee_icon)
        imageButton_3.setOnClickListener {
            // Navigate to FragmentC
            val manageEmployeeFragment = EmployeeFragment()
            val transaction = requireActivity().supportFragmentManager.beginTransaction()
            transaction.replace(R.id.fragment_container, manageEmployeeFragment)
            transaction.addToBackStack(null)  // Optional, adds the transaction to the back stack
            transaction.commit()

        }
    }


}