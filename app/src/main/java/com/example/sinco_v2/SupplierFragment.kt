package com.example.sinco_v2

import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.content.ContentProviderCompat.requireContext
import androidx.fragment.app.FragmentActivity
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase


class SupplierFragment : Fragment() {
    private lateinit var linearlayout2: LinearLayout
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_supplier, container, false)
        linearlayout2 = view.findViewById(R.id.linearlayout2)

        showSupplierInformation()

        return view
    }







    private fun showSupplierInformation(){

        val db = Firebase.firestore
        val supplierRef = db.collection("suppliers")
        supplierRef.get()
            .addOnSuccessListener { documents ->
                for (document in documents){
                    val supplierName = document.get("name") as? String ?: ""
                    val address = document.get("address") as? String ?: ""
                    val email = document.get("email") as? String ?: ""
                    val products = document.get("products") as? ArrayList<Map<String, Any>> ?: ArrayList()
                    val contactNum = document.get("contactnum") as? String ?: ""

                    val inflater = LayoutInflater.from(requireContext())
                    val customItemView = inflater.inflate(R.layout.supplier_items, linearlayout2, false)

                    val supplierTextView =
                        customItemView.findViewById<TextView>(R.id.supplierTextView)
                    val supplierText = supplierName
                    supplierTextView.text = supplierText

                    val emailTextView =
                        customItemView.findViewById<TextView>(R.id.emailTextView)
                    val emailText = email
                    emailTextView.text = emailText

                    val contactTextView = customItemView.findViewById<TextView>(R.id.contactTextView)
                    val contactText = contactNum
                    contactTextView.text = contactText

                    val addressTextView = customItemView.findViewById<TextView>(R.id.addressTextView)
                    val addressText = address
                    addressTextView.text = addressText


                    linearlayout2.addView(customItemView)
                }

            }
    }
    private fun redirectToActivity(activityClass: Class<out FragmentActivity>) {
        val intent = Intent(requireContext(), activityClass)

        val fragmentTransaction = requireActivity().supportFragmentManager.beginTransaction()
        fragmentTransaction.replace(R.id.fragment_container, SupplierFragment())
        fragmentTransaction.addToBackStack(null)
        fragmentTransaction.commit()

        startActivity(intent)
    }
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val redirectToActivityButton = view.findViewById<ImageButton>(R.id.ib_add_icon)

        redirectToActivityButton.setOnClickListener {
            redirectToActivity(AddNewSupplierActivity::class.java)
        }


    }

}



