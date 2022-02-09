package com.au.gonannysingleactivity.fragments

import android.os.Bundle
import android.view.View
import androidx.recyclerview.widget.LinearLayoutManager
import com.au.gonannysingleactivity.R
import com.au.gonannysingleactivity.adapters.FaqsAdapter
import com.au.gonannysingleactivity.utils.CommonFunctions.Companion.getErrorMessage
import com.au.gonannysingleactivity.utils.CommonFunctions.Companion.hideProgressBar
import com.au.gonannysingleactivity.utils.CommonFunctions.Companion.showProgressBar
import com.au.gonannysingleactivity.utils.Extensions.Companion.showToast
import com.au.gonannysingleactivity.webservices.Faq
import com.au.gonannysingleactivity.webservices.GetFaqResponse
import com.au.gonannysingleactivity.webservices.api.RetrofitInstance
import kotlinx.android.synthetic.main.fragment_setting_faqs.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class FaqFragment:BaseFragment() {

    private var list:MutableList<Faq> = mutableListOf()

    override fun getLayoutToInflate(): Int {
        return  R.layout.fragment_setting_faqs
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setUpToolbar("FAQs",true,null){}
        rvFaqs.layoutManager = LinearLayoutManager(requireContext())
        rvFaqs.adapter = FaqsAdapter(list)

        showProgressBar(requireContext())
        loadFaqs()

        refreshSwipe.setColorSchemeResources(R.color.defaultRed)
        refreshSwipe.setOnRefreshListener {
            loadFaqs()
        }

    }
    private fun loadFaqs(){
        RetrofitInstance.create().getFaqs().enqueue(object : Callback<GetFaqResponse> {
            override fun onResponse(
                call: Call<GetFaqResponse>,
                response: Response<GetFaqResponse>
            ) {
                response.apply {
                    if(isSuccessful){
                        if(body()!!.faqs.isNotEmpty()) {
                            tvNoFaqs.visibility = View.GONE
                            (rvFaqs.adapter as FaqsAdapter).setList(body()!!.faqs as MutableList<Faq>)
                            refreshSwipe.isRefreshing = false
                            hideProgressBar()
                        }else{
                            tvNoFaqs.visibility = View.VISIBLE
                            refreshSwipe.isRefreshing = false
                            hideProgressBar()
                        }
                    }else{
                        refreshSwipe.isRefreshing = false
                        hideProgressBar()
                        getErrorMessage(errorBody()!!,requireContext(),code())
                    }
                }
            }

            override fun onFailure(call: Call<GetFaqResponse>, t: Throwable) {
                refreshSwipe.isRefreshing = false
                hideProgressBar()
                t.message!!.showToast(requireContext())
            }
        })
    }
}