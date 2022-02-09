package com.au.gonannysingleactivity.adapters

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.au.gonannysingleactivity.R
import com.au.gonannysingleactivity.fragments.WalkThroughSwipeFragment
import com.au.gonannysingleactivity.utils.ApplicationGlobal

class OnBoardingAdapter(fragmentActivity: FragmentActivity) :
    FragmentStateAdapter(fragmentActivity) {

    override fun getItemCount(): Int {
        return 3
    }

    override fun createFragment(position: Int): Fragment {

        lateinit var fragment:Fragment
        val imageId: Int?
        val stringOne: Int?
        val stringTwo: Int?

        when (position) {
            0 -> {
                if(ApplicationGlobal.userType==1){
                    imageId = R.drawable.image_one
                    stringOne = R.string.create_a_request_easily_for_kids
                    stringTwo = R.string.info_on_boarding
                }else{
                    imageId = R.drawable.image_nanny_one
                    stringOne = R.string.gonanny_is_made
                    stringTwo = R.string.you_get_control
                }
                
            }
            1 -> {
                if(ApplicationGlobal.userType==1){
                    imageId = R.drawable.image_two
                    stringOne = R.string.find_the_best_nanny
                    stringTwo = R.string.info_on_boarding
                }else{
                    imageId = R.drawable.image_nanny_two
                    stringOne = R.string.with_gonanny_generous
                    stringTwo = R.string.with_gonanny_you_get
                }
            }
            else -> {
                if(ApplicationGlobal.userType==1){
                    imageId = R.drawable.image_three
                    stringOne = R.string.manage_pay_from_app
                    stringTwo = R.string.info_on_boarding
                }else{
                    imageId = R.drawable.image_nanny_three
                    stringOne = R.string.with_gonanny_love
                    stringTwo = R.string.express_yourself_through
                }
            }
        }

        fragment = WalkThroughSwipeFragment.newInstance(imageId,stringOne,stringTwo)
        return fragment
    }
}