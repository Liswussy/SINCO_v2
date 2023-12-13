package com.example.sinco_v2

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.ImageButton
import android.widget.Spinner
import android.widget.Toast
import com.google.android.material.datepicker.CalendarConstraints
import com.google.android.material.datepicker.MaterialDatePicker
import java.util.Calendar
import java.util.Date


class AllSalesFragment : Fragment() {
    private lateinit var calendarButton : ImageButton
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_all_sales, container, false)

        val spinner: Spinner = view.findViewById(R.id.spinner)
        val adapter = ArrayAdapter.createFromResource(
            requireContext(),
            R.array.weight_goal_array,
            android.R.layout.simple_spinner_item
        )
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinner.adapter = adapter
        spinner.setOnItemSelectedListener(object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {

                val selectedItem = parent?.getItemAtPosition(position).toString()
                Toast.makeText(requireContext(), selectedItem, Toast.LENGTH_SHORT).show()
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {
                // Handle the case where nothing is selected if needed
            }
        })
        spinner.setSelection(0)

        val minDate = Calendar.getInstance()
        minDate.set(2023, Calendar.JANUARY, 1)

        // Set the maximum date to today
        val maxDate = Calendar.getInstance()

        val builder = MaterialDatePicker.Builder.dateRangePicker()
            .setTitleText("Select a date range")

        // Create custom DateValidator to restrict the selection
        val dateValidator = object : CalendarConstraints.DateValidator {
            override fun isValid(date: Long): Boolean {
                return date >= minDate.timeInMillis && date <= maxDate.timeInMillis
            }

            override fun writeToParcel(dest: android.os.Parcel, flags: Int) {
                // Implement writeToParcel as required
            }

            override fun describeContents(): Int {
                // Implement describeContents as required
                return 0
            }
        }

        // Create CalendarConstraints
        val constraintsBuilder = CalendarConstraints.Builder()
        constraintsBuilder.setStart(minDate.timeInMillis)
        constraintsBuilder.setEnd(maxDate.timeInMillis)
        constraintsBuilder.setValidator(dateValidator)

        builder.setCalendarConstraints(constraintsBuilder.build())

        val datePicker = builder.build()

        calendarButton = view.findViewById(R.id.calendar_icon)
        var canTrigger = true
        calendarButton.setOnClickListener{
            if (canTrigger){
                datePicker.show(parentFragmentManager, datePicker.toString())
                canTrigger = false
            }
        }
        datePicker.addOnPositiveButtonClickListener { selection ->
            val startDate = selection.first
            val endDate = selection.second
            val start = Date(startDate)
            val end = Date(endDate)
            Toast.makeText(requireContext(), "${start}, ${end}", Toast.LENGTH_SHORT).show()
            canTrigger = true
        }
        datePicker.addOnDismissListener{
            canTrigger = true
        }

        return view
    }


}