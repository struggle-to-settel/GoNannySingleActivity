package com.au.gonannysingleactivity.fragments

import android.os.Bundle
import android.view.View
import com.au.gonannysingleactivity.R
import com.au.gonannysingleactivity.objects.Constants.KEY_LINK_BANK_ACCOUNT
import com.au.gonannysingleactivity.utils.ApplicationGlobal
import com.au.gonannysingleactivity.utils.CommonFunctions.Companion.showLogOutDialog
import kotlinx.android.synthetic.main.fragment_complete_sign_up.*
import kotlinx.android.synthetic.main.fragment_complete_sign_up.etEmail
import kotlinx.android.synthetic.main.fragment_complete_sign_up.etName
import kotlinx.android.synthetic.main.fragment_link_bank_account.*

class LinkBankAccountFragment : BaseFragment() {

    override fun getLayoutToInflate(): Int {
        return R.layout.fragment_link_bank_account
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        ApplicationGlobal.preferenceManager.setScreenState(KEY_LINK_BANK_ACCOUNT)
    }

    override fun modifyBackIconFunctionality() {
        requireActivity().apply {
            if(supportFragmentManager.backStackEntryCount>0) this.onBackPressed()
            else showLogOutDialog(requireContext(),this)
        }
    }

    override fun modifyBackIcon() = true

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setUpToolbar(getString(R.string.link_bank_account),true,getString(R.string.skip)){
            replaceFragment(R.id.frameLayout,SignAgreementFragment(),true)
        }

        ApplicationGlobal.userObject?.apply {
            if(last_name!=null) etName.setText("$first_name$last_name") else etName.setText(first_name.toString())
            etEmail.setText(email.toString())
            etPhone.setText(phone_number.toString())
            ccp.setCountryForPhoneCode(country_code.substring(1,country_code.length).toInt())
        }
    }
}