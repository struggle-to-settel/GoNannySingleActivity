package com.au.gonannysingleactivity.fragments

import android.os.Bundle
import android.view.View
import com.au.gonannysingleactivity.R
import kotlinx.android.synthetic.main.fragment_nanny_about.*

class NannyAboutFragment:BaseFragment() {

    override fun getLayoutToInflate(): Int {
        return R.layout.fragment_nanny_about
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        nannyData!!.apply {
            tvValueAbout.text = about
        }
    }

}