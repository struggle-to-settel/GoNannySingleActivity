package com.au.gonannysingleactivity.fragments

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.appcompat.content.res.AppCompatResources
import com.au.gonannysingleactivity.R
import com.au.gonannysingleactivity.activities.CommonReplacementActivity
import com.au.gonannysingleactivity.adapters.BookingFragmentAdapter
import com.au.gonannysingleactivity.objects.Constants
import com.au.gonannysingleactivity.utils.ApplicationGlobal.Companion.fromNotification
import com.au.gonannysingleactivity.utils.ApplicationGlobal.Companion.mapNotification
import com.google.android.material.tabs.TabLayoutMediator
import kotlinx.android.synthetic.main.fragment_bookings.*

class BookingFragment : BaseFragment() {
    private val TAG = "BookingFragment"

    override fun getLayoutToInflate(): Int {
        return R.layout.fragment_bookings
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

//        viewPagerBookings.offscreenPageLimit = 2
        viewPagerBookings.isUserInputEnabled = false
        viewPagerBookings.adapter = BookingFragmentAdapter(requireActivity())


        TabLayoutMediator(tabLayout, viewPagerBookings) { tab, position ->
            tab.view.background = AppCompatResources.getDrawable(requireContext(), R.drawable.middle_tab_stroke)
            tab.text = when (position) {
                0 -> getString(R.string.pending)
                1 -> getString(R.string.ongoing)
                else -> getString(R.string.completed)
            }
        }.attach()

    }

    override fun onResume() {
        super.onResume()
        Log.d(TAG, "onResume: BookingFragment")
    }

}