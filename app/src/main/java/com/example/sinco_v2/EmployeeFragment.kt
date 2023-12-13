package com.example.sinco_v2

import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton

class EmployeeFragment : Fragment() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_employee, container, false)

        val intent = Intent(requireContext(), AddNewEmployeeAccountActivity::class.java)
        val img_add_icon: ImageButton = view.findViewById(R.id.ib_add_icon)
        img_add_icon.setOnClickListener {
            // Navigate to FragmentC
            val NewEmployeeAccountActivity = AddNewEmployeeAccountActivity()
            val transaction = requireActivity().supportFragmentManager.beginTransaction()
            transaction.replace(R.id.fragment_container, NewEmployeeAccountActivity)
            transaction.addToBackStack(null)  // Optional, adds the transaction to the back stack
            transaction.commit()
            startActivity()


        }
        return view
    }

}