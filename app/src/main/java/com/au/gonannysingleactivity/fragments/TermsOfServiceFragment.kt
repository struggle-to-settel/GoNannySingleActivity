package com.au.gonannysingleactivity.fragments

import android.os.Bundle
import android.view.View
import com.au.gonannysingleactivity.R
import com.au.gonannysingleactivity.utils.CommonFunctions.Companion.getErrorMessage
import com.au.gonannysingleactivity.utils.CommonFunctions.Companion.hideProgressBar
import com.au.gonannysingleactivity.utils.CommonFunctions.Companion.showProgressBar
import com.au.gonannysingleactivity.utils.Extensions.Companion.showToast
import com.au.gonannysingleactivity.webservices.TermsConditionsResponse
import com.au.gonannysingleactivity.webservices.api.RetrofitInstance
import kotlinx.android.synthetic.main.fragment_terms_of_service.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class TermsOfServiceFragment : BaseFragment() {

    private var type: Int = 1

    override fun getLayoutToInflate(): Int {
        return R.layout.fragment_terms_of_service
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            type = it.getInt("type", 1)
        }

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setUpToolbar(
            if (type == 1) getString(R.string.privacy_policy_) else getString(R.string.terms_of_service),
            true,
            null
        ) {}

        showProgressBar(requireContext())
        RetrofitInstance.create().getTermsConditions(type).enqueue(object :
            Callback<TermsConditionsResponse> {

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
                        webView.loadData(body()!!.content.content, "text/html", "utf-8")
                    } else {
                        hideProgressBar()
                        getErrorMessage(errorBody()!!, requireContext(), code())
                    }
                }
            }
        })
    }

    companion object {
        fun get(type: Int) = TermsOfServiceFragment().apply {
            arguments = Bundle().apply {
                putInt("type", type)
            }
        }
    }
}