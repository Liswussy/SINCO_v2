package com.example.sinco_v2

import android.content.Context
import android.content.pm.PackageManager
import android.icu.text.DecimalFormat
import android.icu.text.SimpleDateFormat
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Environment
import android.util.AttributeSet
import android.util.Log
import android.view.View
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.itextpdf.text.Document
import org.w3c.dom.Text
import java.io.File
import java.util.Date
import java.util.Locale
import java.util.TimeZone
import com.itextpdf.text.Element
import com.itextpdf.text.Font
import com.itextpdf.text.Paragraph
import com.itextpdf.text.Rectangle
import com.itextpdf.text.pdf.PdfWriter
import java.io.FileOutputStream
import android.Manifest
import android.content.Intent
import android.widget.Button
import java.util.*


class ReceiptActivity : AppCompatActivity() {

    private val WRITE_EXTERNAL_STORAGE_REQUEST_CODE = 1001
    private val VAT_RATE = 12.0
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_receipt)

        val df = DecimalFormat("#.##")
        val productList = intent.getSerializableExtra("productList") as? ArrayList<Product>
        val grandTotal = intent.getDoubleExtra("grandTotal", 0.0)
        val subTotal = intent.getDoubleExtra("subTotal", 0.0)
        val discountAmount = intent.getDoubleExtra("discountAmount", 0.0)
        val change = intent.getDoubleExtra("change", 0.0)
        val selectedPaymentMethod = intent.getStringExtra("selectedPaymentMethod")
        val cash = intent.getDoubleExtra("cashInputValue", 0.0)

        val grandTotalTextView = findViewById<TextView>(R.id.tv_total)
        val subTotalTextView = findViewById<TextView>(R.id.tv_subtotal)
        val discountAmountTextView = findViewById<TextView>(R.id.tv_discount)
        val changeTotalTextView = findViewById<TextView>(R.id.tv_change)
        val selectedPaymentMethodTextVIew = findViewById<TextView>(R.id.paymentTypeTextView)
        val cashTextView = findViewById<TextView>(R.id.tv_cash)
        val timestampTextView = findViewById<TextView>(R.id.tv_date)
        val vatTextView = findViewById<TextView>(R.id.tv_vat_amount)

        val cashierTextView = findViewById<TextView>(R.id.employeeName)

        val userId = FirebaseAuth.getInstance().currentUser?.uid
        userId?.let {
            val firestore = FirebaseFirestore.getInstance()
            val userDocument = firestore.collection("users").document(it)

            userDocument.get().addOnSuccessListener { document ->
                if (document.exists()) {
                    val firstname = document.getString("firstname") ?: ""
                    val lastname = document.getString("lastname") ?: ""
                    val fullName = "$firstname $lastname"
                    cashierTextView.text = fullName
                } else {
                    // Handle the case where the document doesn't exist
                    cashierTextView.text = "Cashier: N/A"
                }
            }.addOnFailureListener { exception ->
                // Handle failures in fetching data
                cashierTextView.text = "Cashier: N/A"
                exception.printStackTrace()
            }
        }
        val dateFormat = SimpleDateFormat("MMM dd, yyyy", Locale.ENGLISH)
        val timeFormat = SimpleDateFormat("hh:mm a", Locale.ENGLISH)

// Set the timezone for the time formats
        val currentDate = dateFormat.format(Date())
        val currentTime = timeFormat.format(Date())

        val dateNTime = "$currentDate, $currentTime"
        timestampTextView.text = dateNTime


        val linearLayoutItems = findViewById<LinearLayout>(R.id.linearlayoutItems)
        // Set the values to the TextViews
        grandTotalTextView.text = "P ${roundToTwoDecimalPlaces(grandTotal)}"
        subTotalTextView.text = "P ${roundToTwoDecimalPlaces(subTotal)}"
        discountAmountTextView.text = "P ${roundToTwoDecimalPlaces(discountAmount)}"
        changeTotalTextView.text = "P ${roundToTwoDecimalPlaces(change)}"
        selectedPaymentMethodTextVIew.text = selectedPaymentMethod.toString()
        cashTextView.text = "P ${roundToTwoDecimalPlaces(cash)}"

// Compute and display VAT
        val vatAmount = computeVatAmount(subTotal)
        vatTextView.text = "P ${roundToTwoDecimalPlaces(vatAmount)}"

        if (productList != null) {
            for (product in productList) {
                val id =product.productID
                val name = product.productName
                val price = product.price
                val quantity = product.quantity
                val entryView = layoutInflater.inflate(R.layout.receipt_items, null)

                val pricetext = "₱"+df.format(price)
                val amounttext = "₱"+df.format(price!! * quantity!!)

                val tvProductName = entryView.findViewById<TextView>(R.id.tv_product_name)
                val tvProductQty = entryView.findViewById<TextView>(R.id.tv_product_qty)
                val tvProductPrice = entryView.findViewById<TextView>(R.id.tv_product_price)
                val tvAmount = entryView.findViewById<TextView>(R.id.tv_amount)

                tvProductName.text = name
                tvProductQty.text = product.quantity?.toString() ?: "N/A"
                tvProductPrice.text = pricetext
                tvAmount.text = amounttext

                val amount = product.quantity?.let { it * product.price} ?: 0.0
                tvAmount.text = "P ${roundToTwoDecimalPlaces(amount)}"

                linearLayoutItems.addView(entryView)
            }

        }

//        val backButton = findViewById<Button>(R.id.backToRegister)
//        backButton.setOnClickListener {
//            // Handle back button click
//            navigateBackToOrderSummary()
//        }

        val downloadButton = findViewById<ImageButton>(R.id.downloadButton)
        downloadButton.setOnClickListener {
            // Check for storage permission
            if (ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE
                ) == PackageManager.PERMISSION_GRANTED
            ) {
                // Permission already granted, proceed with PDF creation and download
                generateAndDownloadPDF()
            } else {
                // Request the permission
                ActivityCompat.requestPermissions(
                    this,
                    arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE),
                    WRITE_EXTERNAL_STORAGE_REQUEST_CODE
                )
            }
        }

    }

    private fun roundToTwoDecimalPlaces(value: Double): String {
        return String.format("%.2f", value)
    }

    // Function to generate and download PDF
    private fun generateAndDownloadPDF() {
        val pdfFileName = "Receipt_${System.currentTimeMillis()}.pdf"
        val storageDir =
            Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
        val pdfFile = File(storageDir, pdfFileName)
        val pageSize = Rectangle(600f, 800f)
        val document = Document(pageSize)

        // Set a bold font for headers
        val headerFont = Font(Font.FontFamily.TIMES_ROMAN, 24f, Font.BOLD)

        // Set a bold font for other text
        val normalFont = Font(Font.FontFamily.TIMES_ROMAN, 12f, Font.NORMAL)

        try {
            PdfWriter.getInstance(document, FileOutputStream(pdfFile))
            document.open()

            // Header
            val receiptContent = getReceiptContent()
            val headerParagraph = Paragraph(receiptContent, headerFont)
            headerParagraph.alignment = Element.ALIGN_CENTER // Center alignment
            document.add(headerParagraph)

            // Content
            val contentParagraph = Paragraph("", normalFont)
            contentParagraph.alignment = Element.ALIGN_CENTER // Center alignment
            document.add(contentParagraph)

            Toast.makeText(this, "PDF Downloaded Successfully", Toast.LENGTH_SHORT).show()
        } catch (e: Exception) {
            Toast.makeText(this, "Error generating PDF: ${e.message}", Toast.LENGTH_SHORT).show()
            Log.e("PDF", "Error generating PDF: ${e.message}")
            e.printStackTrace()
        } finally {
            document.close()
        }
    }

    // Function to compute VAT amount
    private fun computeVatAmount(subtotal: Double): Double {
        return subtotal * (VAT_RATE / 100)
    }

    // Function to retrieve the content of the receipt
    private fun getReceiptContent(): String {
        val receiptContentBuilder = StringBuilder()

        // Add a line to separate header
        receiptContentBuilder.append("------------------------------------------------\n")

        // Fetch additional header information
        val headerTextView = findViewById<TextView>(R.id.header_title)
        val headerText = headerTextView.text.toString()


        // Append the modified text with the new font size and style to the receipt content
        receiptContentBuilder.append("$headerText\n")

        // Add a line to separate address
        receiptContentBuilder.append("------------------------------------------------\n")

        // Fetch address information
        val addressTextView = findViewById<TextView>(R.id.addressTextView)
        val addressText = addressTextView.text.toString()
        receiptContentBuilder.append("$addressText\n")

        // Add a line to separate customer information
        receiptContentBuilder.append("------------------------------------------------\n")

        // Fetch customer information

        // Fetch date information
        val dateTextView = findViewById<TextView>(R.id.tv_date)
        val dateText = dateTextView.text.toString()
        receiptContentBuilder.append("Date: $dateText\n")

        // Fetch cashier name
        val cashierNameTextView = findViewById<TextView>(R.id.employeeName)
        val cashierName = cashierNameTextView.text.toString()
        receiptContentBuilder.append("Cashier Name: $cashierName\n")

        // Add a line to separate items
        receiptContentBuilder.append("------------------------------------------------\n")

        // Fetch items header
        receiptContentBuilder.append("Items\n")

        // Fetch product details
        val linearLayout = findViewById<LinearLayout>(R.id.linearlayoutItems)
        for (i in 0 until linearLayout.childCount) {
            val entryView = linearLayout.getChildAt(i)
            val productNameTextView = entryView.findViewById<TextView>(R.id.tv_product_name)
            val productQuantityTextView = entryView.findViewById<TextView>(R.id.tv_product_qty)
            val productPriceTextView = entryView.findViewById<TextView>(R.id.tv_product_price)
            val productAmountTextView = entryView.findViewById<TextView>(R.id.tv_amount)

            val productName = productNameTextView.text.toString()
            val quantity = productQuantityTextView.text.toString()
            val price = productPriceTextView.text.toString()
            val amount = productAmountTextView.text.toString()

            receiptContentBuilder.append("$productName - $quantity x $price = $amount\n")
        }

        // Add a line to separate totals
        receiptContentBuilder.append("------------------------------------------------\n")

        // Fetch subtotal, discount, vatable, VAT, and total
        val subtotalTextView = findViewById<TextView>(R.id.tv_subtotal)
        val discountTextView = findViewById<TextView>(R.id.tv_discount) // Corrected from R.id.dicount
        val vatableTextView = findViewById<TextView>(R.id.tv_vatable)
        val vatTextView = findViewById<TextView>(R.id.tv_vat_amount) // Corrected from R.id.vat
        val totalTextView = findViewById<TextView>(R.id.tv_total)

        val subtotal = subtotalTextView.text.toString()
        val discount = discountTextView.text.toString()
        val vatable = vatableTextView.text.toString()
        val vat = vatTextView.text.toString()
        val total = totalTextView.text.toString()

        receiptContentBuilder.append("Subtotal: $subtotal\n")
        receiptContentBuilder.append("Discount: $discount\n")
        receiptContentBuilder.append("Vatable:  $vatable\n")
        receiptContentBuilder.append("VAT 12%:  $vat\n")
        receiptContentBuilder.append("Total:    $total\n")

        // Add a line to separate footer
        receiptContentBuilder.append("------------------------------------------------\n")

        return receiptContentBuilder.toString()
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == WRITE_EXTERNAL_STORAGE_REQUEST_CODE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permission granted, proceed with PDF creation and download
                generateAndDownloadPDF()
            } else {
                // Permission denied, handle accordingly
                Toast.makeText(this, "Storage permission denied", Toast.LENGTH_SHORT).show()
            }
        }
    }



}