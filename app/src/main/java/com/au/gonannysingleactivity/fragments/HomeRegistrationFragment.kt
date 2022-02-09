package com.au.gonannysingleactivity.fragments

import android.os.Bundle
import android.text.TextPaint
import android.text.method.LinkMovementMethod
import android.text.style.ClickableSpan
import android.view.View
import com.au.gonannysingleactivity.R
import com.au.gonannysingleactivity.utils.ApplicationGlobal
import kotlinx.android.synthetic.main.fragment_home_registration.*

class HomeRegistrationFragment : BaseFragment(), View.OnClickListener {

    override fun getLayoutToInflate(): Int {
        return R.layout.fragment_home_registration
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        if(ApplicationGlobal.userType == 0){
            ivRegistrationHome.setBackgroundColor(R.drawable.image_nanny_four)
        }
        val spannableString = getSpannableString(
            R.string.already_have_an_account_sign_in,
            getString(R.string.sign_in), R.color.defaultRed, clickableSpan
        )

        tvAlreadyHaveSignIn.apply {
            text = spannableString
            isClickable = true
            movementMethod = LinkMovementMethod.getInstance()
        }

        btnContinueNumber.setOnClickListener(this)
    }

    private val clickableSpan = object : ClickableSpan() {
        override fun onClick(widget: View) {
            replaceFragment(R.id.frameLayout, SignInFragment(), true)
        }
        override fun updateDrawState(ds: TextPaint) {
            super.updateDrawState(ds)
            ds.isUnderlineText = false
        }
    }

    override fun onClick(v: View?) {
        if (v?.id == R.id.btnContinueNumber) {
            replaceFragment(R.id.frameLayout, ContinueWithNumberFragment(), true)
        }
    }
}