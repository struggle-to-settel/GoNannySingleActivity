package com.au.gonannysingleactivity.fragments

import android.content.Intent
import android.os.Bundle
import android.view.View
import com.au.gonannysingleactivity.R
import com.au.gonannysingleactivity.activities.HomeActivity
import com.au.gonannysingleactivity.utils.ApplicationGlobal
import kotlinx.android.synthetic.main.fragment_first.*

class SplashFragment : BaseFragment() {

    override fun getLayoutToInflate(): Int {
        return R.layout.fragment_first
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        ApplicationGlobal.userObject.let {
            if (it != null) {
                if (it.is_agreement_signed == 1) {
                    startActivity(Intent(requireActivity(), HomeActivity::class.java))
                    requireActivity().finish()
                } else if (it.email == "") replaceFragment(R.id.frameLayout, SignAgreementFragment(), false)
            }
        }

        btnContinueAsParent.setOnClickListener {
            ApplicationGlobal.userType = 1
            ApplicationGlobal.preferenceManager.setUserType(1)
            replaceFragment(R.id.frameLayout, WalkThroughFragment(), true)
        }
        btnContinueAsNanny.setOnClickListener {
            ApplicationGlobal.userType = 3
            ApplicationGlobal.preferenceManager.setUserType(3)
            replaceFragment(R.id.frameLayout, WalkThroughFragment(), true)
        }
    }
}