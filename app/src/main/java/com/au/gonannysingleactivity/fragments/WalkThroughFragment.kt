package com.au.gonannysingleactivity.fragments

import android.os.Bundle
import android.view.View
import androidx.viewpager2.widget.ViewPager2
import com.au.gonannysingleactivity.R
import com.au.gonannysingleactivity.adapters.OnBoardingAdapter
import kotlinx.android.synthetic.main.fragment_on_boarding.*

class WalkThroughFragment : BaseFragment(), View.OnClickListener {

    override fun getLayoutToInflate(): Int {
        return R.layout.fragment_on_boarding
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        vpOnBoarding.apply {
            adapter = OnBoardingAdapter(requireActivity())
            currentItem = 0
            registerOnPageChangeCallback(onPageChangeCallback)
        }
        btnNext.setOnClickListener(this)
        btnSkip.setOnClickListener(this)
    }

    override fun onClick(v: View?) {
        if (v?.id == R.id.btnNext) {
            when (vpOnBoarding.currentItem) {
                0 -> vpOnBoarding.currentItem += 1
                1 -> vpOnBoarding.currentItem += 1
                else -> {
                    clearStack()
                    replaceFragment(R.id.frameLayout, HomeRegistrationFragment(), false)
                }
            }
        } else {
            clearStack()
            replaceFragment(R.id.frameLayout, HomeRegistrationFragment(), false)
        }
    }

    private val onPageChangeCallback = object : ViewPager2.OnPageChangeCallback() {
        override fun onPageSelected(position: Int) {
            super.onPageSelected(position)
            when (position) {
                0 -> setUpIndicatorsButtons(
                    R.drawable.indicator_selected,
                    R.drawable.indicator_unselected,
                    R.drawable.indicator_unselected,
                    position
                )
                1 -> setUpIndicatorsButtons(
                    R.drawable.indicator_unselected,
                    R.drawable.indicator_selected,
                    R.drawable.indicator_unselected,
                    position
                )
                else -> setUpIndicatorsButtons(
                    R.drawable.indicator_unselected,
                    R.drawable.indicator_unselected,
                    R.drawable.indicator_selected,
                    position
                )
            }
        }
    }

    fun setUpIndicatorsButtons(idOne: Int, idTwo: Int, idThree: Int, position: Int) {
        ivIndicatorOne.setBackgroundResource(idOne)
        ivIndicatorTwo.setBackgroundResource(idTwo)
        ivIndicatorThree.setBackgroundResource(idThree)
        when (position) {
            0 -> {
                btnSkip.text = getString(R.string.skip)
                btnNext.text = getString(R.string.next)
            }
            1 -> {
                btnSkip.visibility = View.VISIBLE
                btnSkip.text = getString(R.string.skip)
                btnNext.text = getString(R.string.next)
            }
            else -> {
                btnSkip.visibility = View.GONE
                btnNext.text = getString(R.string.get_started)
            }
        }
    }

}