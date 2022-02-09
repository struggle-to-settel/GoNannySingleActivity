package com.au.gonannysingleactivity.fragments

import android.os.Bundle
import android.view.View
import com.au.gonannysingleactivity.R

class RateToNannyFragment:BaseFragment() {

    override fun getLayoutToInflate(): Int {
        return R.layout.fragment_rate_reviews_to_nanny
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setUpToolbar(getString(R.string.rate_nanny),true,null){}

    }
}