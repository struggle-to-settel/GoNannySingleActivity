package com.au.gonannysingleactivity.fragments

import android.os.Bundle
import android.view.View
import android.view.View.OnClickListener
import com.au.gonannysingleactivity.R
import com.au.gonannysingleactivity.utils.ApplicationGlobal
import com.au.gonannysingleactivity.utils.CommonFunctions.Companion.getErrorMessage
import com.au.gonannysingleactivity.utils.CommonFunctions.Companion.hideProgressBar
import com.au.gonannysingleactivity.utils.CommonFunctions.Companion.isConnectedToInternet
import com.au.gonannysingleactivity.utils.CommonFunctions.Companion.isValidNumber
import com.au.gonannysingleactivity.utils.CommonFunctions.Companion.showProgressBar
import com.au.gonannysingleactivity.utils.CommonFunctions.Companion.showToast
import com.au.gonannysingleactivity.webservices.SendOtpResponse
import com.au.gonannysingleactivity.webservices.api.ApiInterface
import com.au.gonannysingleactivity.webservices.api.RetrofitInstance
import kotlinx.android.synthetic.main.fragment_continue_with_number.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response


class ContinueWithNumberFragment : BaseFragment(), OnClickListener {

    override fun getLayoutToInflate(): Int {
        return R.layout.fragment_continue_with_number
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        ivBack.setOnClickListener(this)
        btnContinue.setOnClickListener(this)
    }

    private fun getDataFromFields(): HashMap<String, Any> {

        val hashMap = HashMap<String, Any>()
        hashMap["phone_number"] = etPhone.text.trim().toString().toLong()
        hashMap["country_code"] =
            ccpContinueWithNumber.selectedCountryCodeWithPlus.toString()
        hashMap["user_type"] = ApplicationGlobal.userType

        return hashMap
    }

    override fun onClick(v: View?) {

        when (v?.id) {
            R.id.ivBack -> {
                requireActivity().onBackPressed()
            }
            R.id.btnContinue -> {
                showProgressBar(requireContext())

                if (isConnectedToInternet(requireContext())) {

                    if (isValidNumber(etPhone.text.toString())) {

                        val hashMap = getDataFromFields()
                        val call = RetrofitInstance.get().create(ApiInterface::class.java).sendOtp(hashMap)
                        call.enqueue(object : Callback<SendOtpResponse> {

                            override fun onResponse(
                                call: Call<SendOtpResponse>,
                                response: Response<SendOtpResponse>
                            ) {
                                hideProgressBar()
                                if (response.code() == 200) {
                                    val bundle = Bundle().apply {
                                        putString("country_code", ccpContinueWithNumber.selectedCountryCodeWithPlus.toString())
                                        putLong("mobile_number", etPhone.text.trim().toString().toLong())
                                    }

                                    ApplicationGlobal.accessToken = response.body()!!.token
                                    replaceFragment(
                                        R.id.frameLayout,
                                        VerifyNumberFragment().apply { arguments = bundle },
                                        true
                                    )
                                } else {
                                    getErrorMessage(
                                        response.errorBody()!!,
                                        requireContext(), response.code()
                                    )
                                }
                            }

                            override fun onFailure(call: Call<SendOtpResponse>, t: Throwable) {
                                hideProgressBar()
                                showToast(requireContext(), t.message.toString())
                            }

                        })

                    } else {
                        hideProgressBar()
                        showToast(requireContext(), getString(R.string.enter_phone_number))
                    }

                } else {
                    hideProgressBar()
                    showToast(requireContext(), getString(R.string.check_internt))
                }

            }

        }

    }
}