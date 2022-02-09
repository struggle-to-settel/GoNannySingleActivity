package com.au.gonannysingleactivity.fragments

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import com.au.gonannysingleactivity.R
import com.au.gonannysingleactivity.activities.HomeActivity
import com.au.gonannysingleactivity.utils.ApplicationGlobal

class NumberVerifiedFragment : BaseFragment() {

    override fun getLayoutToInflate(): Int {
        return R.layout.fragment_number_verified
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        Handler(Looper.getMainLooper()).postDelayed({
            clearStack()

            //TODO Manage for Nanny
            ApplicationGlobal.userObject?.apply {
                if (is_agreement_signed == 0) {
                    if (email != null && email!="")
                        replaceFragment(R.id.frameLayout, SignAgreementFragment(), false)
                    else
                        replaceFragment(R.id.frameLayout, CompleteSignUpFragment(), false)
                } else {
                    startActivity(Intent(requireActivity(), HomeActivity::class.java))
                    requireActivity().finish()
                }
            }
        }, 200)
    }
}
