package com.au.gonannysingleactivity.fragments

import android.os.Bundle
import android.view.View
import com.au.gonannysingleactivity.R
import com.au.gonannysingleactivity.utils.CommonFunctions.Companion.getErrorMessage
import com.au.gonannysingleactivity.utils.CommonFunctions.Companion.hideProgressBar
import com.au.gonannysingleactivity.utils.CommonFunctions.Companion.showProgressBar
import com.au.gonannysingleactivity.utils.Extensions.Companion.showToast
import com.au.gonannysingleactivity.webservices.UsedHoursResponse
import com.au.gonannysingleactivity.webservices.api.RetrofitInstance
import kotlinx.android.synthetic.main.fragment_setting_used_hour.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.lang.StringBuilder

class UsedHourFragment:BaseFragment() {
    override fun getLayoutToInflate(): Int {
        return R.layout.fragment_setting_used_hour
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setUpToolbar("Used Hours",true,null){}

        showProgressBar(requireContext())

        RetrofitInstance.create().getUsedHours().enqueue(object : Callback<UsedHoursResponse>{
            override fun onResponse(
                call: Call<UsedHoursResponse>,
                response: Response<UsedHoursResponse>
            ) {
                response.apply {
                    if(isSuccessful){
                        body()!!.bookings_data.apply {
                            tvNumberOfHours.text = "$used_hours hrs"
                            tvNumberOfHiring.text = "$no_of_times times"
                            tvRating.text = "$total_rating"
                            tvNumberOfReviews.text ="( $reviews_count reviews)"
                            ratingBar.rating = total_rating.toFloat()
                            hideProgressBar()
                        }
                    }else{
                        hideProgressBar()
                        getErrorMessage(errorBody()!!,requireContext(),code())
                    }
                }
            }
            override fun onFailure(call: Call<UsedHoursResponse>, t: Throwable) {
                hideProgressBar()
                t.message!!.showToast(requireContext())
            }
        })
    }
}