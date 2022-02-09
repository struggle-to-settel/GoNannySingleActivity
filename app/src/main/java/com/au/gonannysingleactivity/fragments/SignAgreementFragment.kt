package com.au.gonannysingleactivity.fragments

import android.content.Intent
import android.os.Bundle
import android.view.View
import com.au.gonannysingleactivity.R
import com.au.gonannysingleactivity.activities.HomeActivity
import com.au.gonannysingleactivity.objects.Constants
import com.au.gonannysingleactivity.utils.ApplicationGlobal
import com.au.gonannysingleactivity.utils.CommonFunctions.Companion.getErrorMessage
import com.au.gonannysingleactivity.utils.CommonFunctions.Companion.hideProgressBar
import com.au.gonannysingleactivity.utils.CommonFunctions.Companion.isConnectedToInternet
import com.au.gonannysingleactivity.utils.CommonFunctions.Companion.showProgressBar
import com.au.gonannysingleactivity.utils.CommonFunctions.Companion.showToast
import com.au.gonannysingleactivity.utils.Extensions.Companion.showToast
import com.au.gonannysingleactivity.webservices.TermsConditionsResponse
import com.au.gonannysingleactivity.webservices.UpdateUserProfileResponse
import com.au.gonannysingleactivity.webservices.api.ApiInterface
import com.au.gonannysingleactivity.webservices.api.RetrofitInstance
import kotlinx.android.synthetic.main.fragment_sign_agreement.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class SignAgreementFragment : BaseFragment() {

    override fun onStart() {
        super.onStart()
        ApplicationGlobal.preferenceManager.setScreenState(Constants.KEY_SIGN_AGREEMENT)
    }

    override fun getLayoutToInflate(): Int {
        return R.layout.fragment_sign_agreement
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setUpToolbar(getString(R.string.sign_an_agreement), false, null) {}

        val id = if (ApplicationGlobal.userType == 3) 5 else 4

        if (isConnectedToInternet(requireContext())) {
            showProgressBar(requireContext())
            RetrofitInstance.create().getTermsConditions(id)
                .enqueue(object : Callback<TermsConditionsResponse> {
                    override fun onFailure(call: Call<TermsConditionsResponse>, t: Throwable) {
                        hideProgressBar()
                        t.message!!.showToast(requireContext())
                    }

                    override fun onResponse(
                        call: Call<TermsConditionsResponse>,
                        response: Response<TermsConditionsResponse>
                    ) {
                        response.apply {
                            if (isSuccessful) {
                                hideProgressBar()
                                webView.loadDataWithBaseURL(
                                    null,
                                    body()!!.content.content,
                                    "text/html",
                                    "utf-8",
                                    null
                                )
                            } else {
                                hideProgressBar()
                                getErrorMessage(errorBody()!!, requireContext(), code())
                            }
                        }
                    }
                })

        } else getString(R.string.check_internt).showToast(requireContext())

        btnAgree.setOnClickListener {
            showProgressBar(requireContext())
            if (isConnectedToInternet(requireContext())) {
                val hashMap = HashMap<String, Any>()
                hashMap["is_agreement_signed"] = 1

                val call = RetrofitInstance.get().create(ApiInterface::class.java)
                    .updateUserProfile(hashMap)
                call.enqueue(object : Callback<UpdateUserProfileResponse> {
                    override fun onResponse(
                        call: Call<UpdateUserProfileResponse>,
                        response: Response<UpdateUserProfileResponse>
                    ) {
                        if (response.isSuccessful) {
                            hideProgressBar()
                            ApplicationGlobal.preferenceManager.saveUserObject(response.body()!!.user)
                            ApplicationGlobal.userObject = response.body()!!.user
                            startActivity(Intent(requireActivity(), HomeActivity::class.java))
                            requireActivity().finish()
                        } else {
                            hideProgressBar()
                            getErrorMessage(
                                response.errorBody()!!,
                                requireContext(),
                                response.code()
                            )
                        }
                    }

                    override fun onFailure(call: Call<UpdateUserProfileResponse>, t: Throwable) {
                        hideProgressBar()
                        showToast(requireContext(), t.message.toString())
                    }
                })
            }

        }
    }

}