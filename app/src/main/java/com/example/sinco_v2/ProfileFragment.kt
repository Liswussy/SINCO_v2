package com.example.sinco_v2

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.compose.ui.text.capitalize
import androidx.fragment.app.Fragment
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class ProfileFragment : Fragment() {

    private lateinit var auth: FirebaseAuth
    private val firestore = FirebaseFirestore.getInstance()
    private lateinit var tv_username: TextView
    private lateinit var tv_contact: TextView
    private lateinit var tv_role: TextView

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_profile, container, false)

        tv_username = view.findViewById(R.id.tv_username)
        tv_contact = view.findViewById(R.id.tv_contact)
        tv_role = view.findViewById(R.id.tv_role)

        auth = FirebaseAuth.getInstance()
        val currentUserUid = auth.currentUser?.uid
        currentUserUid?.let {
            firestore.collection("users").document(it)
                .get()
                .addOnSuccessListener { document ->
                    if (document != null) {
                        val firstname = document.getString("firstname") ?: ""
                        val lastname = document.getString("lastname") ?: ""

                        tv_username.text = "${firstname} ${lastname}"
                        tv_contact.text = document.getString("email") ?: "No email"
                        tv_role.text = document.getString("role")?.capitalize() ?: "No role"
                    } else {
                        Log.d("Firestore", "No such document")
                    }
                }
                .addOnFailureListener { exception ->
                    Log.d("Firestore", "get failed with ", exception)
                }
        }

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        auth = FirebaseAuth.getInstance()
        val logoutButton = view.findViewById<View>(R.id.buttonLogOut)
        logoutButton.setOnClickListener {
            logoutUser()
        }
    }

    private fun logoutUser() {
        auth.signOut()
        val intent = Intent(requireContext(), LoginActivity::class.java)
        startActivity(intent)
        requireActivity().finish()
    }
}
