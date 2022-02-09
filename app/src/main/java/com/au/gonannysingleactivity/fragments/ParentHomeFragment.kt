package com.au.gonannysingleactivity.fragments

import android.annotation.SuppressLint
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import android.view.View
import androidx.appcompat.content.res.AppCompatResources
import com.au.gonannysingleactivity.R
import com.au.gonannysingleactivity.activities.CommonReplacementActivity
import com.au.gonannysingleactivity.objects.Constants
import com.au.gonannysingleactivity.objects.Constants.KEY_FOR_FRAGMENT_REPLACEMENT
import com.au.gonannysingleactivity.objects.Constants.KEY_FOR_ID
import com.au.gonannysingleactivity.objects.Constants.KEY_GET_PRICING
import com.au.gonannysingleactivity.objects.Constants.KEY_HOME
import com.au.gonannysingleactivity.objects.Constants.KEY_SELECT_ADDRESS
import com.au.gonannysingleactivity.objects.Constants.KEY_SELECT_DATE_TIME
import com.au.gonannysingleactivity.objects.Constants.KEY_SELECT_DURATION
import com.au.gonannysingleactivity.objects.Constants.KEY_SELECT_KIDS
import com.au.gonannysingleactivity.utils.ApplicationGlobal
import com.au.gonannysingleactivity.utils.CommonFunctions.Companion.get12HourFormattedTime
import com.au.gonannysingleactivity.utils.CommonFunctions.Companion.get24HourFormattedTime
import com.au.gonannysingleactivity.utils.CommonFunctions.Companion.getErrorMessage
import com.au.gonannysingleactivity.utils.CommonFunctions.Companion.getMonthSpecifiedFormattedDate
import com.au.gonannysingleactivity.utils.CommonFunctions.Companion.hideProgressBar
import com.au.gonannysingleactivity.utils.CommonFunctions.Companion.showProgressBar
import com.au.gonannysingleactivity.utils.Extensions.Companion.showToast
import com.au.gonannysingleactivity.webservices.CreateBookingResponse
import com.au.gonannysingleactivity.webservices.api.ApiInterface
import com.au.gonannysingleactivity.webservices.api.RetrofitInstance
import kotlinx.android.synthetic.main.fragment_home.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.util.*
import kotlin.collections.HashMap

class ParentHomeFragment : BaseFragment(), View.OnClickListener {

    val hashMap = HashMap<String, Any>()

    // to check the fields are filled or not
    var isTimeSelected = false
    var isDurationSelected = false
    var isAddressSelected = false
    var areKidsSelected = false

    var start_time: String = ""
    var end_time: String = ""

    var intent: Intent? = null

    override fun getLayoutToInflate(): Int {
        return R.layout.fragment_home
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        ApplicationGlobal.preferenceManager.setScreenState(KEY_HOME)
        requireActivity().registerReceiver(broadcastReceiver, IntentFilter(Constants.KEY_BROADCAST))
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        ApplicationGlobal.preferenceManager.getAccessToken()
        super.onViewCreated(view, savedInstanceState)
        intent = Intent(requireContext(), CommonReplacementActivity::class.java)
        btnSelectDateTime.setOnClickListener(this)
        btnSelectDuration.setOnClickListener(this)
        btnSelectAddress.setOnClickListener(this)
        btnSelectNoOfKids.setOnClickListener(this)
        btnGetPricing.setOnClickListener(this)
    }

    private val broadcastReceiver = object : BroadcastReceiver() {

        @SuppressLint("SetTextI18n")
        override fun onReceive(context: Context?, intent: Intent?) {

            intent?.extras?.let { extras ->

                when (extras.getString(Constants.KEY_FROM_WHERE)) {

                    KEY_SELECT_DATE_TIME -> {

                        extras.apply {

                            tvDateTime.text =
                                getMonthSpecifiedFormattedDate(getString("dateTime")!!)
                            val calendar = Calendar.getInstance()

                            hashMap["needed_date_time"] = "${getString("dateTime").toString()} ${
                                get24HourFormattedTime(
                                    calendar.get(Calendar.HOUR_OF_DAY),
                                    calendar.get(Calendar.MINUTE)
                                )
                            }"

                        }

                        btnSelectDateTime.apply {
                            background = AppCompatResources.getDrawable(
                                requireContext(),
                                R.drawable.green_button_shape_right
                            )
                            text = requireContext().getString(R.string.edit)
                        }
                        isTimeSelected = true

                    }

                    KEY_SELECT_ADDRESS -> {
                        extras.apply {
                            tvAddress.text = getString("address", "")
                            hashMap["voucher"] = "shfyukj743uhaijbv"
                            hashMap["apartment_no"] = getString("apartment_no", "")
                            hashMap["city"] = getString("city", "")
                            hashMap["landmark"] = getString("landmark", "")
                            hashMap["time_zone"] = "+05:30"
                            hashMap["address"] = getString("address", "")
                            hashMap["lat"] = getString("lat", "0").toDouble()
                            hashMap["lng"] = getString("lng", "0").toDouble()
                        }
                        btnSelectAddress.apply {
                            background = AppCompatResources.getDrawable(
                                requireContext(),
                                R.drawable.green_button_shape_right
                            )
                            text = requireContext().getString(R.string.edit)
                        }
                        isAddressSelected = true
                    }

                    KEY_SELECT_DURATION -> {
                        btnSelectDuration.apply {
                            background = AppCompatResources.getDrawable(
                                requireContext(),
                                R.drawable.green_button_shape_right
                            )
                            text = requireContext().getString(R.string.edit)
                        }
                        extras.apply {
                            getInt("duration").let { key ->
                                hashMap["duration"] = key
                                hashMap["duration_name"] = when (key) {
                                    1 -> getString(R.string.duration_one)
                                    2 -> getString(R.string.duration_two)
                                    else -> getString(R.string.duration_three)
                                }
                            }
                            start_time = getString("startTime").toString()
                            end_time = getString("endTime").toString()

                            getString("startTime")!!.split(",").apply {
                                getString("endTime")!!.split(",").let {

                                    hashMap["start_time"] =
                                        get24HourFormattedTime(this[0].toInt(), this[1].toInt())
                                    hashMap["end_time"] =
                                        get24HourFormattedTime(it[0].toInt(), it[1].toInt())
                                    tvDuration.text = "${
                                        get12HourFormattedTime(
                                            this[0].toInt(),
                                            this[1].toInt()
                                        )
                                    } To ${get12HourFormattedTime(it[0].toInt(), it[1].toInt())}"
                                }
                            }
                        }
                        isDurationSelected = true
                    }

                    else -> {
                        btnSelectNoOfKids.apply {
                            background = AppCompatResources.getDrawable(
                                requireContext(),
                                R.drawable.green_button_shape_right
                            )
                            text = requireContext().getString(R.string.edit)
                        }
                        extras.getIntegerArrayList("ids")!!.apply {
                            hashMap["no_of_kids"] = size
                            hashMap["kid_ids"] = this
                            tvNumberKids.text = if (size == 1) "$size kid" else "$size kid\'s"
                        }
                        areKidsSelected = true
                    }
                }
            }
        }
    }


    override fun onDestroy() {
        super.onDestroy()
        requireActivity().unregisterReceiver(broadcastReceiver)
    }

    override fun onClick(v: View?) {

        when (v?.id) {

            R.id.btnSelectDateTime -> {
                intent!!.putExtra(
                    KEY_FOR_FRAGMENT_REPLACEMENT,
                    KEY_SELECT_DATE_TIME
                )
                if (isTimeSelected) {
                    intent!!.putExtra("selectedDate", hashMap["needed_date_time"].toString())
                }
                startActivity(intent)
            }

            R.id.btnSelectDuration -> {
                if (isTimeSelected) {
                    intent!!.apply {
                        putExtra(KEY_FOR_FRAGMENT_REPLACEMENT, KEY_SELECT_DURATION)
                        putExtra(KEY_SELECT_DATE_TIME, tvDateTime.text.toString())
                    }
                    if (isDurationSelected) {
                        intent!!.putExtra(
                            "selectedDuration",
                            hashMap["duration"].toString().toInt()
                        )
                        intent!!.putExtra("selectedStartTime", start_time)
                        intent!!.putExtra("selectedEndTime", end_time)
                    }
                    startActivity(intent)
                } else {
                    getString(R.string.select_date_time_warning).showToast(requireContext())
                }

            }

            R.id.btnSelectAddress -> {
                intent!!.putExtra(KEY_FOR_FRAGMENT_REPLACEMENT, KEY_SELECT_ADDRESS)
                startActivity(intent)
            }

            R.id.btnSelectNoOfKids -> {
                intent!!.putExtra(KEY_FOR_FRAGMENT_REPLACEMENT, KEY_SELECT_KIDS)
                startActivity(intent)
            }

            R.id.btnGetPricing -> {
                requireContext().let {
                    if (!isAddressSelected)
                        getString(R.string.select_add).showToast(it)
                    else if (!isTimeSelected)
                        getString(R.string.select_date_time_warning).showToast(it)
                    else if (!isDurationSelected)
                        getString(R.string.select_duration_warning).showToast(it)
                    else if (!areKidsSelected)
                        getString(R.string.select_kid_warning).showToast(it)
                    else {
                        showProgressBar(it)
                        val call = RetrofitInstance.get().create(ApiInterface::class.java)
                            .createBooking(hashMap)
                        call.enqueue(object : Callback<CreateBookingResponse> {
                            override fun onResponse(
                                call: Call<CreateBookingResponse>,
                                response: Response<CreateBookingResponse>
                            ) {
                                response.apply {

                                    if (isSuccessful) {
                                        intent!!.apply {
                                            putExtra(KEY_FOR_FRAGMENT_REPLACEMENT, KEY_GET_PRICING)
                                            putExtra(KEY_FOR_ID, body()!!.id)
                                        }
                                        hideProgressBar()
                                        startActivity(intent)
                                    } else {
                                        hideProgressBar()
                                        getErrorMessage(errorBody()!!, it, code())
                                    }
                                }

                            }

                            override fun onFailure(
                                call: Call<CreateBookingResponse>,
                                t: Throwable
                            ) {
                                hideProgressBar()
                                t.message.toString().showToast(it)
                            }
                        })
                    }
                }
            }

        }
    }
}