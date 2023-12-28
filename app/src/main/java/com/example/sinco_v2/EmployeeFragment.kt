package com.example.sinco_v2

import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.compose.ui.text.capitalize
import androidx.fragment.app.FragmentActivity
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

class EmployeeFragment : Fragment() {
    private lateinit var linearlayout1: LinearLayout
    private lateinit var swipeRefreshLayout: SwipeRefreshLayout

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_employee, container, false)

        linearlayout1 = view.findViewById(R.id.linearlayout1)
        swipeRefreshLayout = view.findViewById(R.id.swipeRefreshLayout)

        showEmployeeInformation()
        swipeRefreshLayout.setOnRefreshListener {
            showEmployeeInformation()
            swipeRefreshLayout.isRefreshing = false
        }



        return view
    }

    private fun redirectToActivity(activityClass: Class<out FragmentActivity>, employeeUID: String) {
        val intent = Intent(requireContext(), activityClass)
        intent.putExtra("employeeUID", employeeUID)

        val fragmentTransaction = requireActivity().supportFragmentManager.beginTransaction()
        fragmentTransaction.replace(R.id.fragment_container, EmployeeFragment())
        fragmentTransaction.addToBackStack(null)
        fragmentTransaction.commit()

        startActivity(intent)
    }

    private fun showEmployeeInformation() {
        linearlayout1.removeAllViews()

        val db = Firebase.firestore
        val docRef = db.collection("users")
        docRef.get()
            .addOnSuccessListener { documents ->
                for (document in documents) {
                    val employeeUID = document.id
                    val firstName = document.get("firstname") as? String ?: ""
                    val lastName = document.get("lastname") as? String ?: ""
                    val emailAddress = document.get("email") as? String ?: ""
                    val contactNum = document.get("contactnum") as? String ?: ""
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

                    val contactTextView = customItemView.findViewById<TextView>(R.id.contactTextView)
                    val contactNumText = contactNum
                    contactTextView.text = contactNumText

                    val roleTextView = customItemView.findViewById<TextView>(R.id.roleTextView)
                    val roleText = role.capitalize()
                    roleTextView.text = roleText

                    customItemView.setOnClickListener{
                        redirectToActivity(EnrollEmployeeActivity::class.java, employeeUID)
                    }

                    linearlayout1.addView((customItemView))


                }

            }


    }

}






