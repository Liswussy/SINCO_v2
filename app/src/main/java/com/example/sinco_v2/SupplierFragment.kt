import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.content.ContentProviderCompat.requireContext
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import com.example.sinco_v2.AddNewSupplierActivity
import com.example.sinco_v2.ModifyDeleteSupplierActivity
import com.example.sinco_v2.R
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

class SupplierFragment : Fragment() {
    private lateinit var linearlayout2: LinearLayout
    private lateinit var searchSupplierInputEditText: TextInputEditText

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_supplier, container, false)
        linearlayout2 = view.findViewById(R.id.linearlayout2)
        searchSupplierInputEditText = view.findViewById(R.id.searchInputEditText)

        showSupplierInformation()

        return view
    }

    private fun showSupplierInformation() {
        val db = Firebase.firestore
        val supplierRef = db.collection("suppliers")
        supplierRef.get()
            .addOnSuccessListener { documents ->
                for (document in documents) {
                    val supplierID = document.id
                    val supplierName = document.get("name") as? String ?: ""
                    val address = document.get("address") as? String ?: ""
                    val email = document.get("email") as? String ?: ""
                    val products = document.get("products") as? ArrayList<Map<String, Any>> ?: ArrayList()
                    val contactNum = document.get("contactnum") as? String ?: ""

                    val inflater = LayoutInflater.from(requireContext())
                    val customItemView = inflater.inflate(R.layout.supplier_items, linearlayout2, false)

                    val supplierTextView =
                        customItemView.findViewById<TextView>(R.id.supplierTextView)
                    supplierTextView.text = supplierName

                    val emailTextView =
                        customItemView.findViewById<TextView>(R.id.emailTextView)
                    emailTextView.text = email

                    val contactTextView = customItemView.findViewById<TextView>(R.id.contactTextView)
                    contactTextView.text = contactNum

                    val addressTextView = customItemView.findViewById<TextView>(R.id.addressTextView)
                    addressTextView.text = address

                    customItemView.setOnClickListener{
                        redirectToActivityWithID(ModifyDeleteSupplierActivity::class.java, supplierID)
                    }

                    linearlayout2.addView(customItemView)
                }
            }
    }
    private fun redirectToActivityWithID(activityClass: Class<out FragmentActivity>, supplierID: String) {
        val intent = Intent(requireContext(), activityClass)

        intent.putExtra("supplierID", supplierID) // Pass the supplierID as an extra
        val fragmentTransaction = requireActivity().supportFragmentManager.beginTransaction()
        fragmentTransaction.replace(R.id.fragment_container, SupplierFragment())
        fragmentTransaction.addToBackStack(null)
        fragmentTransaction.commit()
        // Start the activity directly
        startActivity(intent)
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

        val searchInputLayout = view.findViewById<TextInputLayout>(R.id.searchInputLayout)
        searchInputLayout.setEndIconOnClickListener {
            // Clear the text and show all suppliers
            searchSupplierInputEditText.text = null
            showSupplierInformation()
            // Hide the keyboard
            hideKeyboard()
        }

        // Set an OnEditorActionListener to handle the EditorInfo.IME_ACTION_DONE event
        searchSupplierInputEditText.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                handleSearch()
                // Manually clear the text after handling the search
                return@setOnEditorActionListener true
            }
            false
        }
    }

    private fun filterSupplierInformation(query: String) {
        linearlayout2.removeAllViews()

        val db = Firebase.firestore
        val supplierRef = db.collection("suppliers")

        val lowercaseQuery = query.toLowerCase()

        supplierRef.get()
            .addOnSuccessListener { documents ->
                for (document in documents) {
                    val supplierID = document.id
                    val supplierName = document.get("name") as? String ?: ""
                    val address = document.get("address") as? String ?: ""
                    val email = document.get("email") as? String ?: ""
                    val products = document.get("products") as? ArrayList<Map<String, Any>> ?: ArrayList()
                    val contactNum = document.get("contactnum") as? String ?: ""

                    // Use lowercase for comparison
                    val lowercaseSupplierName = supplierName.toLowerCase()
                    val lowercaseEmail = email.toLowerCase()
                    val lowercaseContactNum = contactNum.toLowerCase()

                    if (
                        lowercaseSupplierName.contains(lowercaseQuery) ||
                        lowercaseEmail.contains(lowercaseQuery) ||
                        lowercaseContactNum.contains(lowercaseQuery)
                    ) {
                        val inflater = LayoutInflater.from(requireContext())
                        val customItemView = inflater.inflate(R.layout.supplier_items, linearlayout2, false)

                        val supplierTextView =
                            customItemView.findViewById<TextView>(R.id.supplierTextView)
                        supplierTextView.text = supplierName

                        val emailTextView =
                            customItemView.findViewById<TextView>(R.id.emailTextView)
                        emailTextView.text = email

                        val contactTextView = customItemView.findViewById<TextView>(R.id.contactTextView)
                        contactTextView.text = contactNum

                        val addressTextView = customItemView.findViewById<TextView>(R.id.addressTextView)
                        addressTextView.text = address


                        customItemView.setOnClickListener{
                            redirectToActivityWithID(ModifyDeleteSupplierActivity::class.java, supplierID)
                        }

                        linearlayout2.addView(customItemView)
                    }
                }
            }
    }

    private fun handleSearch() {
        val query = searchSupplierInputEditText.text.toString()
        filterSupplierInformation(query)
        hideKeyboard()
    }

    private fun hideKeyboard() {
        val imm = requireContext().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(requireView().windowToken, 0)
    }
}
