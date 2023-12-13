package com.example.sinco_v2

import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton

class ProductFragment : Fragment() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_product, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Assuming you have a button or some other UI element that triggers the redirection
        val redirectToActivityButton = view.findViewById<ImageButton>(R.id.ib_add_icon)

        redirectToActivityButton.setOnClickListener {
            // Create an Intent to start the activity
            val intent = Intent(activity, AddNewProductActivity::class.java)

            // You can also pass data to the new activity if needed
            intent.putExtra("key", "value")
            val fragmentTransaction = requireActivity().supportFragmentManager.beginTransaction()
            fragmentTransaction.replace(R.id.fragment_container, ProductFragment())
            fragmentTransaction.addToBackStack(null)
            fragmentTransaction.commit()
            // Start the activity
            startActivity(intent)
        }
    }


}