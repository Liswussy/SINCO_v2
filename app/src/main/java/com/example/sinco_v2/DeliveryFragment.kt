import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.example.sinco_v2.DeliveryReportFragment
import com.example.sinco_v2.NewDeliveryFragment
import com.example.sinco_v2.R
import com.google.android.material.tabs.TabLayout
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class DeliveryFragment : Fragment() {
    private val auth = FirebaseAuth.getInstance()
    private val firestore = FirebaseFirestore.getInstance()

    private var userRole: String = "Employee"
    private var tabsAdded: Boolean = false
    private var fragmentAdded: Boolean = false

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_delivery, container, false)
        setupTabs(view)
        return view
    }

    private fun setupTabs(view: View) {
        val newDelivery = NewDeliveryFragment()
        val deliveryReport = DeliveryReportFragment()
        val tabLayout = view.findViewById<TabLayout>(R.id.tabLayout)

        if (!tabsAdded) {
            val currentUserUid = auth.currentUser?.uid

            currentUserUid?.let {
                firestore.collection("users").document(it)
                    .get()
                    .addOnSuccessListener { document ->
                        if (document != null) {
                            userRole = document.getString("role") ?: "Employee"
                            addTabs(tabLayout, newDelivery, deliveryReport)
                        } else {
                            Log.d("Firestore", "No such document")
                        }
                    }
                    .addOnFailureListener { exception ->
                        Log.d("Firestore", "get failed with ", exception)
                    }
            }
        }
    }

    private fun addTabs(tabLayout: TabLayout, newDelivery: NewDeliveryFragment, deliveryReport: DeliveryReportFragment) {
        // Clear existing tabs
        tabLayout.removeAllTabs()

        if (userRole.equals("manager", ignoreCase = true) || userRole.equals("Auditor", ignoreCase = true)) {
            tabLayout.addTab(tabLayout.newTab().setText("New Delivery"))
            tabLayout.addTab(tabLayout.newTab().setText("Delivery Report"))
            tabsAdded = true

            // Select the first tab by default (New Delivery)
            tabLayout.getTabAt(0)?.select()
        } else {
            tabLayout.visibility = View.GONE
        }

        tabLayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab?) {
                when (tab?.position) {
                    0 -> replaceFragment(newDelivery)
                    1 -> {
                        if (userRole.equals("Auditor", ignoreCase = true) || userRole.equals("Manager", ignoreCase = true)) {
                            replaceFragment(deliveryReport)
                        } else {
                            Log.d("UserRole", "Employee tried to access Delivery Report")
                            Toast.makeText(requireContext(), "You don't have access to Delivery Report", Toast.LENGTH_SHORT).show()
                        }
                    }
                }
            }

            override fun onTabUnselected(tab: TabLayout.Tab?) {
                // Handle tab unselection
            }

            override fun onTabReselected(tab: TabLayout.Tab?) {
                // Handle tab reselection
            }
        })
    }

    private fun replaceFragment(fragment: Fragment) {
        parentFragmentManager.beginTransaction()
            .replace(R.id.fragmentContainer, fragment)
            .commit()
        fragmentAdded = true
    }
}
