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
import androidx.fragment.app.FragmentActivity
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

class EmployeeFragment : Fragment() {
    private lateinit var linearlayout1: LinearLayout
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_employee, container, false)

        linearlayout1 = view.findViewById(R.id.linearlayout1)

        showEmployeeInformation()

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Assuming you have a button or some other UI element that triggers the redirection
        val redirectToActivityButton = view.findViewById<ImageButton>(R.id.ib_add_icon)

        redirectToActivityButton.setOnClickListener {
            redirectToActivity(AddNewEmployeeAccountActivity::class.java)
        }

    }

    private fun redirectToActivity(activityClass: Class<out FragmentActivity>) {
        val intent = Intent(requireContext(), activityClass)

        val fragmentTransaction = requireActivity().supportFragmentManager.beginTransaction()
        fragmentTransaction.replace(R.id.fragment_container, EmployeeFragment())
        fragmentTransaction.addToBackStack(null)
        fragmentTransaction.commit()

        startActivity(intent)
    }

    private fun showEmployeeInformation() {
        val db = Firebase.firestore
        val docRef = db.collection("users")
        docRef.get()
            .addOnSuccessListener { documents ->
                for (document in documents) {
                    val firstName = document.get("firstname") as? String ?: ""
                    val lastName = document.get("lastname") as? String ?: ""
                    val emailAddress = document.get("email") as? String ?: ""
                    val role = document.get("role") as? String ?: ""

                    val inflater = LayoutInflater.from(requireContext())
                    val customItemView =
                        inflater.inflate(R.layout.employee_items, linearlayout1, false)

                    val employeeTextView =
                        customItemView.findViewById<TextView>(R.id.employeeTextView)
                    val employeeText = "$firstName $lastName"
                    employeeTextView.text = employeeText

                    val emailTextView = customItemView.findViewById<TextView>(R.id.emailTextView)
                    val emailtext = emailAddress
                    emailTextView.text = emailtext

                    val roleTextView = customItemView.findViewById<TextView>(R.id.roleTextView)
                    val roleText = role
                    roleTextView.text = roleText

                    linearlayout1.addView((customItemView))


                }

            }


    }

}






