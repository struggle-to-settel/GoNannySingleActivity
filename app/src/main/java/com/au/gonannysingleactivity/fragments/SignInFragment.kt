package com.au.gonannysingleactivity.fragments

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.core.view.isVisible
import com.au.gonannysingleactivity.R
import com.au.gonannysingleactivity.activities.HomeActivity
import com.au.gonannysingleactivity.utils.ApplicationGlobal
import com.au.gonannysingleactivity.utils.CommonFunctions.Companion.getErrorMessage
import com.au.gonannysingleactivity.utils.CommonFunctions.Companion.hideProgressBar
import com.au.gonannysingleactivity.utils.CommonFunctions.Companion.isConnectedToInternet
import com.au.gonannysingleactivity.utils.CommonFunctions.Companion.isValidEmail
import com.au.gonannysingleactivity.utils.CommonFunctions.Companion.isValidNumber
import com.au.gonannysingleactivity.utils.CommonFunctions.Companion.isValidPassword
import com.au.gonannysingleactivity.utils.CommonFunctions.Companion.showProgressBar
import com.au.gonannysingleactivity.utils.CommonFunctions.Companion.showToast
import com.au.gonannysingleactivity.webservices.VerifyOtpResponse
import com.au.gonannysingleactivity.webservices.api.ApiInterface
import com.au.gonannysingleactivity.webservices.api.RetrofitInstance
import kotlinx.android.synthetic.main.fragment_sign_in.*
import kotlinx.android.synthetic.main.fragment_sign_in.btnSignIn
import kotlinx.android.synthetic.main.fragment_sign_in.etEmail
import kotlinx.android.synthetic.main.fragment_sign_in.etMobileNumber
import kotlinx.android.synthetic.main.fragment_sign_in.etPassword
import kotlinx.android.synthetic.main.fragment_sign_in.radioGroup
import kotlinx.android.synthetic.main.fragment_sign_in.tvForgotPassword
import kotlinx.android.synthetic.main.fragment_sign_in.tvIconCode
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class SignInFragment : BaseFragment() {

    val hashMap = HashMap<String,Any>()

    override fun getLayoutToInflate(): Int {
        return R.layout.fragment_sign_in
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        hashMap["fcm_id"] = ApplicationGlobal.fcmId
        hashMap["device_id"] = "jhisuhnj554fta"
        hashMap["device_type"] = 1
        hashMap["user_type"] = ApplicationGlobal.userType

        radioGroup.setOnCheckedChangeListener { group, checkedId ->
            when (checkedId) {
                R.id.rbMobileNumber -> {
                    tvIconCode.visibility = View.VISIBLE
                    etMobileNumber.visibility = View.VISIBLE
                    viewBorderMobile.visibility = View.VISIBLE
                    etEmail.visibility = View.GONE
                }
                R.id.rbEmail -> {
                    tvIconCode.visibility = View.GONE
                    etMobileNumber.visibility = View.GONE
                    viewBorderMobile.visibility = View.GONE
                    etEmail.visibility = View.VISIBLE
                }
            }
        }

        tvForgotPassword.setOnClickListener {
            replaceFragment(
                R.id.frameLayout,
                ForgotPasswordFragment(),
                true
            )
        }

        btnSignIn.setOnClickListener {
            if (isConnectedToInternet(requireContext())) {
                if (etEmail.isVisible) {
                    if (!isValidEmail(etEmail.text.toString().trim()))
                        Toast.makeText(requireContext(), "Enter valid email", Toast.LENGTH_SHORT)
                            .show()
                    else if (!isValidPassword(etPassword.text.toString().trim()))
                        Toast.makeText(requireContext(), "Enter valid password", Toast.LENGTH_SHORT)
                            .show()
                    else {
                        showProgressBar(requireContext())
                        hashMap["email"] = etEmail.text.toString().trim()
                        hashMap["password"] = etPassword.text.toString().trim()
                        login(hashMap)
                    }
                } else {
                    if (!isValidNumber(etMobileNumber.text.toString().trim())) {
                        Toast.makeText(requireContext(), "Enter valid email", Toast.LENGTH_SHORT)
                            .show()
                    } else if (!isValidPassword(etPassword.text.toString().trim())) {
                        Toast.makeText(requireContext(), "Enter valid password", Toast.LENGTH_SHORT)
                            .show()
                    } else {
                        hashMap["country_code"] = tvIconCode.selectedCountryCodeWithPlus.toString()
                        hashMap["phone_number"] = etMobileNumber.text.toString().trim()
                        hashMap["password"] = etPassword.text.toString().trim()
                        login(hashMap)
                    }
                }
            } else
                showToast(requireContext(), getString(R.string.check_internt))
        }
    }

    private fun login(hashMap: HashMap<String,Any>){
        val call =
            RetrofitInstance.get().create(ApiInterface::class.java).login(hashMap)
        call.enqueue(object : Callback<VerifyOtpResponse> {
            override fun onResponse(
                call: Call<VerifyOtpResponse>,
                response: Response<VerifyOtpResponse>
            ) {
                if (response.isSuccessful) {
                    hideProgressBar()
                    ApplicationGlobal.apply {
                        response.body()!!.let {
                            userObject = it.user
                            accessToken = it.token
                            preferenceManager.apply {
                                setAccessToken(it.token)
                                saveUserObject(it.user)
                            }
                        }
                    }
                    startActivity(Intent(requireActivity(), HomeActivity::class.java))
                    requireActivity().finish()
                } else {
                    hideProgressBar()
                    getErrorMessage(response.errorBody()!!,requireContext(),response.code())
                }
            }

            override fun onFailure(call: Call<VerifyOtpResponse>, t: Throwable) {
                hideProgressBar()
                showToast(requireContext(),t.message.toString())
            }
        })
    }

}