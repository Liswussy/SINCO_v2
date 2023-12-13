package com.example.sinco_v2

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton


class AddNewSupplierFragment : Fragment() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_add_new_supplier, container, false)

        val backButton: ImageButton = view.findViewById(R.id.ib_back_icon)
        backButton.setOnClickListener {
            // Navigate to SupplierFragment
            requireActivity().supportFragmentManager.popBackStack()


        }
        return view

    }

}

