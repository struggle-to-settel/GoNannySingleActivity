package com.au.gonannysingleactivity.adapters

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.au.gonannysingleactivity.fragments.NannyAboutFragment
import com.au.gonannysingleactivity.fragments.NannyPhotosFragment

class NannyProfileAdapter(activity: FragmentActivity) :
    FragmentStateAdapter(activity) {

    override fun getItemCount(): Int {
        return 2
    }

    override fun createFragment(position: Int): Fragment {
        return when (position) {
            0 -> NannyAboutFragment()
            else -> NannyPhotosFragment()
        }
    }
}