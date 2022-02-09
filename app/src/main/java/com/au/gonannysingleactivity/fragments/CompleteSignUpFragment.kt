package com.au.gonannysingleactivity.fragments

import android.os.Bundle
import android.text.method.LinkMovementMethod
import android.text.style.ClickableSpan
import android.view.View
import android.widget.Toast
import com.au.gonannysingleactivity.R
import com.au.gonannysingleactivity.objects.Constants
import com.au.gonannysingleactivity.utils.ApplicationGlobal
import com.au.gonannysingleactivity.utils.CommonFunctions.Companion.getErrorMessage
import com.au.gonannysingleactivity.utils.CommonFunctions.Companion.hideProgressBar
import com.au.gonannysingleactivity.utils.CommonFunctions.Companion.isConnectedToInternet
import com.au.gonannysingleactivity.utils.CommonFunctions.Companion.isValidEmail
import com.au.gonannysingleactivity.utils.CommonFunctions.Companion.isValidName
import com.au.gonannysingleactivity.utils.CommonFunctions.Companion.isValidPassword
import com.au.gonannysingleactivity.utils.CommonFunctions.Companion.showLogOutDialog
import com.au.gonannysingleactivity.utils.CommonFunctions.Companion.showProgressBar
import com.au.gonannysingleactivity.utils.CommonFunctions.Companion.showToast
import com.au.gonannysingleactivity.webservices.VerifyOtpResponse
import com.au.gonannysingleactivity.webservices.api.ApiInterface
import com.au.gonannysingleactivity.webservices.api.RetrofitInstance
import kotlinx.android.synthetic.main.fragment_complete_sign_up.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class CompleteSignUpFragment : BaseFragment() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        ApplicationGlobal.preferenceManager.setScreenState(Constants.KEY_COMPLETE_SIGN_UP)
    }

    override fun getLayoutToInflate(): Int {
        return R.layout.fragment_complete_sign_up
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {

        ivLogOut.setOnClickListener {
            showLogOutDialog(
                requireContext(),
                requireActivity())
        }

        btnSignUp.setOnClickListener {
            showProgressBar(requireContext())
            if (isConnectedToInternet(requireContext())) {
                if (!isValidName(etName.text.toString().trim())) {
                    hideProgressBar()
                    showToast(
                        requireContext(),
                        getString(R.string.enter_valid_name)
                    )
                } else if (!isValidEmail(etEmail.text.toString().trim())) {
                    hideProgressBar()
                    showToast(
                        requireContext(),
                        getString(R.string.enter_valid_email)
                    )
                } else if (!isValidPassword(etPassword.text.toString().trim())) {
                    hideProgressBar()
                    showToast(
                        requireContext(),
                        getString(R.string.password_info)
                    )
                } else {
                    val hashMap = HashMap<String, Any>()
                    val names = etName.text.trim().toString().split(" ")
                    if (names.size >= 2) {
                        hashMap["first_name"] = names[0].trim()
                        hashMap["last_name"] = names[1].trim()
                    } else {
                        hashMap["first_name"] = names[0].trim()
                    }
                    hashMap["email"] = etEmail.text.trim().toString()
                    hashMap["password"] = etPassword.text.trim().toString()
                    hashMap["user_type"] = ApplicationGlobal.userType

                    val service = RetrofitInstance.get().create(ApiInterface::class.java)
                    val call = service.signUp(hashMap)

                    call.enqueue(object : Callback<VerifyOtpResponse> {
                        override fun onResponse(
                            call: Call<VerifyOtpResponse>,
                            response: Response<VerifyOtpResponse>
                        ) {
                            if (response.isSuccessful) {
                                hideProgressBar()
                                clearStack()
                                ApplicationGlobal.apply {
                                    userObject = response.body()!!.user
                                    accessToken = response.body()!!.token
                                    preferenceManager.apply {
                                        saveUserObject(response.body()!!.user)
                                        setAccessToken(response.body()!!.token)
                                    }
                                }
                                if(ApplicationGlobal.userType==1) {
                                    addFragment(R.id.frameLayout, AddKidInfoFragment(), false)
                                }else{
                                    addFragment(R.id.frameLayout,AddAddressFragment(),false)
                                }
                            } else {
                                hideProgressBar()
                                getErrorMessage(
                                    response.errorBody()!!,
                                    requireContext(),
                                    response.code()
                                )
                            }
                        }

                        override fun onFailure(call: Call<VerifyOtpResponse>, t: Throwable) {
                            hideProgressBar()
                            showToast(requireContext(), t.message!!)
                        }
                    })
                }
            }
        }

        val spannableString = getSpannableString(
            R.string.terms_amp_privacy,
            getString(R.string.terms_of_service),
            getString(R.string.privacy_policy),
            R.color.defaultRed,
            R.color.defaultRed,
            termsOfService,
            privacyPolicy
        )

        tvTermsConditions.apply {
            text = spannableString
            isClickable = true
            movementMethod = LinkMovementMethod.getInstance()
        }

    }

    private val termsOfService = object : ClickableSpan() {
        override fun onClick(widget: View) {
            replaceFragment(R.id.frameLayout,TermsOfServiceFragment.get(2),true)
        }
    }

    private val privacyPolicy = object : ClickableSpan() {
        override fun onClick(widget: View) {
            replaceFragment(R.id.frameLayout,TermsOfServiceFragment.get(1),true)
        }

    }

}