package com.example.sinco_v2

import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import androidx.fragment.app.FragmentActivity
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

    }

    private fun redirectToFragment(fragment: Fragment) {

        val fragmentTransaction = requireActivity().supportFragmentManager.beginTransaction()
        fragmentTransaction.replace(R.id.fragment_container, fragment)
        fragmentTransaction.addToBackStack(null)
        fragmentTransaction.commit()

    }



}




