package com.example.sinco_v2

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

class ConsignorSalesFragment : Fragment() {
    private lateinit var linearLayout1: LinearLayout
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view =  inflater.inflate(R.layout.fragment_consignor_sales, container, false)
        linearLayout1 = view.findViewById(R.id.linearlayout1)

        showConsignorName()
        return view
    }

    private fun showConsignorName(){
        val db = Firebase.firestore
        val docRef = db.collection("suppliers")
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