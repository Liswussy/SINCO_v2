package com.example.sinco_v2

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.ImageButton

class AddNewEmployeeAccountActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_new_employee_account)

        val backButton = findViewById<ImageButton>(R.id.ib_back_icon)
        backButton.setOnClickListener {
            finish()
        }

    }
}