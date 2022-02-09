package com.au.gonannysingleactivity.utils

import android.app.TimePickerDialog
import android.content.Context
import android.widget.TimePicker

class TimePicker(
    cn: Context,
    style: Int,
    listener: OnTimeSetListener,
    private val initialHour: Int,
    private val initialMinute: Int,
    is24Hour: Boolean,
    private val durationType: Int // for duration type
) : TimePickerDialog(cn, style, listener, initialHour, initialMinute, is24Hour) {

    override fun onTimeChanged(view: TimePicker?, hourOfDay: Int, minute: Int) {
        super.onTimeChanged(view, hourOfDay, minute)
        when (durationType) {
            0 -> {
                when (hourOfDay) {
                    !in initialHour..11 -> updateTime(initialHour, initialMinute)
                }
                if (hourOfDay == 11 && minute>0) {
                    updateTime(11,0)
                }
            }
            1 -> {
                when (hourOfDay) {
                    // restricting hour not to be greater than 21
                    in 21..24 -> updateTime(21, 0)
                    !in initialHour..21 -> updateTime(initialHour, initialMinute)
                }
                if(initialHour==hourOfDay){
                    if(initialMinute>minute){
                        updateTime(hourOfDay,initialMinute)
                    }
                }
            }
            2 -> {
                when (hourOfDay) {
                    //restricting hour not to be greater than 22
                    in 22..24 -> updateTime(22, 0)
                    !in initialHour..22 -> updateTime(initialHour, initialMinute)
                }
                if(initialHour==hourOfDay){
                    if(initialMinute>minute){
                        updateTime(hourOfDay,initialMinute)
                    }
                }
            }
        }
    }

}