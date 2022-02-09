package com.au.gonannysingleactivity.fragments

import android.os.Bundle
import android.view.View
import androidx.recyclerview.widget.LinearLayoutManager
import com.au.gonannysingleactivity.R
import com.au.gonannysingleactivity.adapters.CardsAdapter
import com.au.gonannysingleactivity.utils.CommonFunctions.Companion.getErrorMessage
import com.au.gonannysingleactivity.utils.CommonFunctions.Companion.hideProgressBar
import com.au.gonannysingleactivity.utils.CommonFunctions.Companion.showProgressBar
import com.au.gonannysingleactivity.utils.Extensions.Companion.showToast
import com.au.gonannysingleactivity.webservices.Card
import com.au.gonannysingleactivity.webservices.GetCardsResponse
import com.au.gonannysingleactivity.webservices.MessageResponse
import com.au.gonannysingleactivity.webservices.api.ApiInterface
import com.au.gonannysingleactivity.webservices.api.RetrofitInstance
import kotlinx.android.synthetic.main.fragment_setting_cards.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class CardsFragment : BaseFragment(), CardsAdapter.OnClickListener {

    override fun getLayoutToInflate(): Int {
        return R.layout.fragment_setting_cards
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setUpToolbar("Cards", true, "ADD") {
            replaceFragment(R.id.flCommonReplacement, AddCardFragment().apply {
                arguments = Bundle().apply {
                    putBoolean("to_add_card", true)
                }
            }, true)
        }

        getCards()

    }

    private fun getCards() {
        showProgressBar(requireContext())
        val call = RetrofitInstance.get().create(ApiInterface::class.java).getCards()
        call.enqueue(object : Callback<GetCardsResponse> {

            override fun onResponse(
                call: Call<GetCardsResponse>,
                response: Response<GetCardsResponse>
            ) {

                response.apply {
                    if (isSuccessful) {
                        hideProgressBar()
                        rvCards.layoutManager = LinearLayoutManager(requireContext())
                        rvCards.adapter = CardsAdapter(
                            requireContext(),
                            body()!!.cards as MutableList<Card>,
                            this@CardsFragment
                        )
                    } else {
                        hideProgressBar()
                        getErrorMessage(errorBody()!!, requireContext(), code())
                    }
                }
            }

            override fun onFailure(call: Call<GetCardsResponse>, t: Throwable) {
                hideProgressBar()
                t.message!!.showToast(requireContext())
            }
        })
    }

    override fun onDelete(paymentId: String) {

        val hashMap = HashMap<String,Any>()
        hashMap["payment_method"] = paymentId

        showProgressBar(requireContext())
        RetrofitInstance.create().deleteCard(hashMap)
            .enqueue(object : Callback<MessageResponse> {
                override fun onResponse(
                    call: Call<MessageResponse>,
                    response: Response<MessageResponse>
                ) {
                    response.apply {
                        if(isSuccessful){
                            hideProgressBar()
                            body()!!.message.showToast(requireContext())
                            getCards()
                        }else{
                            hideProgressBar()
                            getErrorMessage(errorBody()!!,requireContext(),code())
                        }
                    }
                }

                override fun onFailure(call: Call<MessageResponse>, t: Throwable) {
                    hideProgressBar()
                    t.message!!.showToast(requireContext())
                }
            })
    }

}