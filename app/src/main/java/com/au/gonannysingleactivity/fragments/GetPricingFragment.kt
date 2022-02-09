package com.au.gonannysingleactivity.fragments

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.View
import com.au.gonannysingleactivity.R
import com.au.gonannysingleactivity.objects.Constants.KEY_FOR_ID
import com.au.gonannysingleactivity.utils.CommonFunctions.Companion.get12HourFormattedTime
import com.au.gonannysingleactivity.utils.CommonFunctions.Companion.getErrorMessage
import com.au.gonannysingleactivity.utils.CommonFunctions.Companion.getMonthSpecifiedFormattedDate
import com.au.gonannysingleactivity.utils.Extensions.Companion.showToast
import com.au.gonannysingleactivity.webservices.GetPricingResponse
import com.au.gonannysingleactivity.webservices.api.RetrofitInstance
import kotlinx.android.synthetic.main.fragment_pricing_details.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class GetPricingFragment : BaseFragment() {
    private var bookingId: Int = 0
    private var totalPrice: Int = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            bookingId = it.getInt(KEY_FOR_ID, 0)
        }
    }

    override fun getLayoutToInflate(): Int {
        return R.layout.fragment_pricing_details
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setUpToolbar(getString(R.string.pricing_details), true, null) {}

        val call = RetrofitInstance.create().getPricing(bookingId)
        call.enqueue(object : Callback<GetPricingResponse> {
            @SuppressLint("SetTextI18n")
            override fun onResponse(
                call: Call<GetPricingResponse>,
                response: Response<GetPricingResponse>
            ) {
                if (response.isSuccessful) {
                    response.body()!!.apply {
                        tvValueNeededDateTime.text =
                            getMonthSpecifiedFormattedDate(needed_date_time.substring(0..9))

                        val start = start_time.split(":")
                        val end = end_time.split(":")

                        tvValueDuration.text = "${
                            get12HourFormattedTime(
                                start[0].toInt(),
                                start[1].toInt()
                            )
                        } To ${get12HourFormattedTime(end[0].toInt(), end[1].toInt())}"

                        tvValueKids.text =
                            if (no_of_kids == 1) "$no_of_kids kid" else "$no_of_kids kid\'s"
                        tvValueAddress.text = address
                        tvValueServiceCharge.text = "$$price"
                        tvValueTax.text = "$$tax"
                        tvValueTotalCharge.text = "$$total_charge"
                        totalPrice = total_charge
                        tvValueApartmentNo.text = apartment_no
                    }
                } else {
                    getErrorMessage(response.errorBody()!!, requireContext(), response.code())
                }
            }

            override fun onFailure(call: Call<GetPricingResponse>, t: Throwable) {
                t.message.toString().showToast(requireContext())
            }
        })

        tvEdit.setOnClickListener{
            requireActivity().onBackPressed()
        }

        btnStartSearchingForNanny.setOnClickListener{
            replaceFragment(
                R.id.flCommonReplacement,
                SavedCardFragment().apply {
                    arguments = Bundle().apply {
                        putInt("totalPrice", totalPrice)
                        putInt("booking_id", this@GetPricingFragment.bookingId)
                    }
                },
                true
            )
        }
    }

}