package com.au.gonannysingleactivity.fragments

import android.os.Bundle
import android.view.View
import com.au.gonannysingleactivity.R
import kotlinx.android.synthetic.main.fragment_forgot_password.*

class ForgotPasswordFragment:BaseFragment() {

    override fun getLayoutToInflate(): Int {
        return R.layout.fragment_forgot_password
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        toolbar.setTitleTextAppearance(requireContext(),R.style.toolbarTextFonts)
        toolbar.setTitleTextColor(resources.getColor(R.color.btnGreen,requireContext().theme))

    }

}