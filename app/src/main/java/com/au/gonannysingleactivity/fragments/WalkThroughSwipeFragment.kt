package com.au.gonannysingleactivity.fragments

import android.os.Bundle
import android.view.View
import com.au.gonannysingleactivity.R
import kotlinx.android.synthetic.main.fragment_swipe_on_boarding.*

private const val KEY_ONE = "KEY_ONE"
private const val KEY_TWO = "KEY_TWO"
private const val KEY_THREE = "KEY_THREE"

class WalkThroughSwipeFragment : BaseFragment() {

    private var imageId: Int? = null
    private var stringOne: Int? = null
    private var stringTwo: Int? = null

    override fun getLayoutToInflate(): Int {
        return R.layout.fragment_swipe_on_boarding
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        arguments?.apply {
            imageId = this.getInt(KEY_ONE, R.drawable.image_one)
            stringOne = this.getInt(KEY_TWO, R.string.obNannyOne)
            stringTwo = this.getInt(KEY_THREE, R.string.info_on_boarding)
        }
        imageId?.let { ivOnBoarding.setBackgroundResource(it) }
        tvHeadlineOnBoarding.text = stringOne?.let { getString(it) }
        tvInfoOnBoarding.text = stringTwo?.let { getString(it) }
    }

    companion object {

        fun newInstance(imageId: Int, stringOne: Int, stringTwo: Int): WalkThroughSwipeFragment {
            val bundle = Bundle().apply {
                this.putInt(KEY_ONE, imageId)
                this.putInt(KEY_TWO, stringOne)
                this.putInt(KEY_THREE, stringTwo)
            }
            val fragment = WalkThroughSwipeFragment()
            fragment.arguments = bundle
            return fragment
        }


    }
}