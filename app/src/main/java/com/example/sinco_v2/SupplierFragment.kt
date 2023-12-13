package com.example.sinco_v2

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton


class SupplierFragment : Fragment() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_supplier, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val imageButton_1: ImageButton = view.findViewById(R.id.ib_add_icon)
        imageButton_1.setOnClickListener {
            // Navigate to FragmentB
            val addNewSupplierFragment = AddNewSupplierFragment()
            val transaction = requireActivity().supportFragmentManager.beginTransaction()
            transaction.replace(R.id.fragment_container, addNewSupplierFragment)
            transaction.addToBackStack(null)  // Optional, adds the transaction to the back stack
            transaction.commit()

        }
    }


}