package com.au.gonannysingleactivity.fragments

import android.content.Intent
import android.os.Bundle
import android.view.View
import com.au.gonannysingleactivity.R
import com.au.gonannysingleactivity.objects.Constants
import com.au.gonannysingleactivity.utils.CommonFunctions
import com.au.gonannysingleactivity.utils.CommonFunctions.Companion.get12HourFormattedTime
import com.au.gonannysingleactivity.utils.Extensions.Companion.showToast
import com.au.gonannysingleactivity.utils.TimePicker
import kotlinx.android.synthetic.main.fragment_select_duration.*
import java.util.*

class SelectDurationFragment : BaseFragment() {

    // check date is not today's date
    private var isTodayDate = false
    private var date: String = ""

    // for option two
    private var startHour: Int = 0
    private var startMinute: Int = 0

    // to fill data when duration has already selected and here to edit data
    lateinit var selectedStartTime: String
    var selectedDuration: Int? = null
    lateinit var selectedEndTime: String

    // just for check
    private var timeSelected = false

    // to send the data using broadcast
    private var startTime = ""
    private var endTime = ""
    private var intent: Intent = Intent(Constants.KEY_BROADCAST)
        .putExtra(Constants.KEY_FROM_WHERE, Constants.KEY_SELECT_DURATION)

    override fun getLayoutToInflate(): Int {
        return R.layout.fragment_select_duration
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.apply {
            date = getString("dateOne", "")
            selectedDuration = getInt("selectedDuration", 8)
            selectedStartTime = getString("selectedStartTime", "")
            selectedEndTime = getString("selectedEndTime", "")
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        //setting up toolbar
        setUpToolbar(getString(R.string.select_duration), true, null) {}

        // calendar class object to get current time and date
        val mCurrentTime = Calendar.getInstance()

        // checking if the selected date is today's or not
        if (date == CommonFunctions.getFormattedDate(mCurrentTime.timeInMillis)) {

            if (mCurrentTime.get(Calendar.HOUR_OF_DAY) + 2 >= 7) {
                rbDurationOne.isClickable = false
                rbDurationOne.isChecked = false
            }
            isTodayDate = true

        } else isTodayDate = false

        // managing time selector visibility of radio buttons
        radioGroup.setOnCheckedChangeListener { _, checkedId ->
            when (checkedId) {
                R.id.rbDurationOne -> {
                    constraintOne.visibility = View.GONE
                    constraintTwo.visibility = View.GONE
                }
                R.id.rbDurationTwo -> {
                    constraintOne.visibility = View.VISIBLE
                    constraintTwo.visibility = View.GONE
                }
                R.id.rbDurationThree -> {
                    constraintOne.visibility = View.GONE
                    constraintTwo.visibility = View.VISIBLE
                }
            }
        }

        // if here to edit the details
        if (selectedDuration != 8) {
            when (selectedDuration) {
                1 -> rbDurationOne.isChecked = true
                2 -> {
                    rbDurationTwo.isChecked = true
                    constraintOne.visibility = View.VISIBLE
                    selectedStartTime.split(",").apply {
                        etStartTimeDurationOptionOne.setText(
                            get12HourFormattedTime(
                                this[0].toInt(),
                                this[1].toInt()
                            )
                        )
                    }
                    selectedEndTime.split(",").apply {
                        etEndTimeDurationOptionOne.setText(
                            get12HourFormattedTime(
                                this[0].toInt(),
                                this[1].toInt()
                            )
                        )
                    }

                }
                3 -> {
                    rbDurationThree.isChecked = true
                    constraintTwo.visibility = View.VISIBLE
                    selectedStartTime.split(",").apply {
                        startHour = this[0].toInt()
                        startMinute = this[1].toInt()
                        etStartTimeDurationOptionTwo.setText(
                            get12HourFormattedTime(
                                this[0].toInt(),
                                this[1].toInt()
                            )
                        )
                    }
                    selectedEndTime.split(",").apply {
                        etEndTimeDurationOptionTwo.setText(
                            get12HourFormattedTime(
                                this[0].toInt(),
                                this[1].toInt()
                            )
                        )
                    }
                    timeSelected = true
                }
            }
        }

        btnConfirmAndSelect.setOnClickListener {
            when (radioGroup.checkedRadioButtonId) {
                R.id.rbDurationOne -> {
                    startTime = "7,0"
                    endTime = "19,0"
                    requireActivity().sendBroadcast(intent.apply {
                        putExtra("duration", 1)
                        putExtra("startTime", startTime)
                        putExtra("endTime", endTime)
                    })
                    requireActivity().finish()
                }
                R.id.rbDurationTwo -> {
                    if (etStartTimeDurationOptionOne.text.toString().isBlank()
                        && etEndTimeDurationOptionOne.text.toString().isBlank()
                    ) {

                        getString(R.string.select_time).showToast(requireContext())
                    } else {
                        requireActivity().sendBroadcast(intent.apply {
                            putExtra("duration", 2)
                            putExtra("startTime", startTime)
                            putExtra("endTime", endTime)
                        })
                        requireActivity().finish()
                    }
                }
                R.id.rbDurationThree -> {
                    if (etStartTimeDurationOptionTwo.text.toString().isBlank()) {
                        getString(R.string.select_start_time).showToast(requireContext())
                    } else if (etEndTimeDurationOptionTwo.text.toString().isBlank()) {
                        getString(R.string.select_end_time).showToast(requireContext())
                    } else {
                        requireActivity().sendBroadcast(intent.apply {
                            putExtra("duration", 3)
                            putExtra("startTime", startTime)
                            putExtra("endTime", endTime)
                        })
                        requireActivity().finish()
                    }
                }
            }

        }

        etStartTimeDurationOptionOne.setOnClickListener {
            val hourOfDay = if (isTodayDate) mCurrentTime.get(Calendar.HOUR_OF_DAY) + 2 else 5
            val cMinute = if (isTodayDate) mCurrentTime.get(Calendar.MINUTE) else 0

            if (hourOfDay !in 5..11 || (hourOfDay == 11 && cMinute > 0)) {
                "can\'t select time today, try tomorrow".showToast(requireContext())
            } else {
                TimePicker(
                    requireContext(),
                    R.style.timePickerDialog,
                    { _, hour, minute ->
                        if (hour in hourOfDay..11) {
                            startHour = hour
                            startMinute = minute
//                            if (hour == 11 && minute >= cMinute) {
//                                startHour = 11
//                                startMinute = 0
//                            } else {
//                                startHour = hour
//                                startMinute = minute
//                            }
                            etStartTimeDurationOptionOne.setText(
                                get12HourFormattedTime(
                                    startHour,
                                    startMinute
                                )
                            )
                            etEndTimeDurationOptionOne.setText(
                                get12HourFormattedTime(
                                    startHour + 6,
                                    startMinute
                                )
                            )
                            startTime = "$startHour,$startMinute"
                            endTime = "${startHour + 6},$startMinute"
                        }
                    }, hourOfDay, cMinute, true, 0
                ).show()
            }
        }

        etStartTimeDurationOptionTwo.setOnClickListener {

            val hourOfDay = if (isTodayDate) mCurrentTime.get(Calendar.HOUR_OF_DAY) + 2 else 5
            val cMinute = if (isTodayDate) mCurrentTime.get(Calendar.MINUTE) else 0

            if (hourOfDay !in 5..21 || (hourOfDay == 21 && cMinute > 0)) {
                "can\'t select time today, try tomorrow".showToast(requireContext())
            } else {
                TimePicker(
                    requireContext(),
                    R.style.timePickerDialog,
                    { _, hour, minute ->
                        // hour should be in selectable bounds
                        if (hour in hourOfDay..21) {
                            if (hour == 21 && minute >= cMinute) {
                                startHour = hour
                                startMinute = 0
                            } else {
                                startHour = hour
                                startMinute = minute
                            }
                            etStartTimeDurationOptionTwo.setText(
                                get12HourFormattedTime(
                                    startHour,
                                    startMinute
                                )
                            )
                            etEndTimeDurationOptionTwo.setText("")
                            timeSelected = true
                            startTime = "$hour,$minute"

                        }
                    }, hourOfDay, cMinute, true, 1
                ).show()
            }
        }

        etEndTimeDurationOptionTwo.setOnClickListener {

            val hourOfDay = startHour + 1
            val cMinute = startMinute
            if (timeSelected) {
                TimePicker(requireContext(), R.style.timePickerDialog, { _, hour, minute ->
                    if (hour in startHour..22) {
                        etEndTimeDurationOptionTwo.setText(
                            get12HourFormattedTime(
                                hour,
                                minute
                            )
                        )
                        endTime = "$hour,$minute"
                    }


                }, hourOfDay, cMinute, true, 2).show()
            } else {
                getString(R.string.fill_start_time_warning).showToast(requireContext())
            }
        }
    }

}