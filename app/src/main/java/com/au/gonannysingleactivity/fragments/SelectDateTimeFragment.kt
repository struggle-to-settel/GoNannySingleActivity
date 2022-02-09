package com.au.gonannysingleactivity.fragments

import android.annotation.SuppressLint
import android.app.TimePickerDialog
import android.content.Intent
import android.os.Bundle
import android.view.View
import com.au.gonannysingleactivity.R
import com.au.gonannysingleactivity.objects.Constants
import com.au.gonannysingleactivity.utils.CommonFunctions
import com.au.gonannysingleactivity.utils.CommonFunctions.Companion.getDateInMillis
import kotlinx.android.synthetic.main.fragment_select_date_time.*
import java.text.SimpleDateFormat
import java.util.*

class SelectDateTimeFragment : BaseFragment() {

    // to send and retrieve date
    private var date: String = ""

    override fun getLayoutToInflate(): Int {
        return R.layout.fragment_select_date_time
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.apply {
            date = getString("selectedDate","")
        }
    }

    @SuppressLint("SimpleDateFormat")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setUpToolbar(getString(R.string.select_date_time), true, null) {}


        calendarView.apply {
            if(this@SelectDateTimeFragment.date!="") {
                setDate(getDateInMillis(this@SelectDateTimeFragment.date), false, true)
            }else{
                setDate(System.currentTimeMillis(), false, true)
            }
            minDate = System.currentTimeMillis()
            maxDate = System.currentTimeMillis() + Constants.oneWeekInMilli
        }

        val mCurrentTime = Calendar.getInstance()
        date = CommonFunctions.getFormattedDate(mCurrentTime.timeInMillis)

        calendarView.setOnDateChangeListener { _, year, month, dayOfMonth ->
            val day = if(dayOfMonth in 1..9) "0$dayOfMonth" else dayOfMonth
            val m = if((month+1) in 1..9) "0${month+1}" else month+1
            date = "$year-$m-$day"
        }

        btnConfirmAndSelect.setOnClickListener {
            requireActivity().apply {
                sendBroadcast(Intent(Constants.KEY_BROADCAST).apply {
                    putExtra(Constants.KEY_FROM_WHERE, Constants.KEY_SELECT_DATE_TIME)
                    putExtra("dateTime", date)
                })
                finish()
            }
        }
    }

}