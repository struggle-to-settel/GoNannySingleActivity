package com.au.gonannysingleactivity.fragments

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.View
import com.au.gonannysingleactivity.R
import com.au.gonannysingleactivity.objects.Constants
import com.au.gonannysingleactivity.utils.ApplicationGlobal
import com.au.gonannysingleactivity.utils.CommonFunctions.Companion.getErrorMessage
import com.au.gonannysingleactivity.utils.CommonFunctions.Companion.hideProgressBar
import com.au.gonannysingleactivity.utils.CommonFunctions.Companion.isCardCvvValid
import com.au.gonannysingleactivity.utils.CommonFunctions.Companion.isCardExpiryValid
import com.au.gonannysingleactivity.utils.CommonFunctions.Companion.isCardNumberValid
import com.au.gonannysingleactivity.utils.CommonFunctions.Companion.isNameValid
import com.au.gonannysingleactivity.utils.CommonFunctions.Companion.showProgressBar
import com.au.gonannysingleactivity.utils.CommonFunctions.Companion.showToast
import com.au.gonannysingleactivity.utils.Extensions.Companion.showToast
import com.au.gonannysingleactivity.webservices.MessageResponse
import com.au.gonannysingleactivity.webservices.SetUpIntentResponse
import com.au.gonannysingleactivity.webservices.api.ApiInterface
import com.au.gonannysingleactivity.webservices.api.RetrofitInstance
import com.stripe.android.*
import com.stripe.android.model.CardParams
import com.stripe.android.model.ConfirmSetupIntentParams
import com.stripe.android.model.PaymentMethodCreateParams
import kotlinx.android.synthetic.main.fragment_add_card.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class AddCardFragment : BaseFragment() {
    private val TAG = "AddCardFragment"
    var shouldAdd = true
    var toAddCard:Boolean = false
    lateinit var stripe: Stripe
    lateinit var clientSecret: String
    lateinit var cardParams: PaymentMethodCreateParams
    override fun getLayoutToInflate(): Int {
        return R.layout.fragment_add_card
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        arguments?.let {
            toAddCard = it.getBoolean("to_add_card",false)
        }
        stripe = Stripe(
            requireContext(),
            PaymentConfiguration.getInstance(requireContext()).publishableKey
        )
        Log.d(TAG, "onCreate: stripe class got initialized")
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        if(toAddCard){
            setUpToolbar(getString(R.string.add_a_card), false, null) {}
        }else{
            setUpToolbar(getString(R.string.add_a_card), false, getString(R.string.skip)) {
                replaceFragment(R.id.frameLayout, SignAgreementFragment(), true)
            }
            ApplicationGlobal.preferenceManager.setScreenState(Constants.KEY_ADD_CARD)
        }


        btnAddCard.setOnClickListener {
            showProgressBar(requireContext())
            val cardNumber = etCardNumber.text.toString().trim()
            val cardExpiry = etExpiry.text.toString().trim()
            val cardCVV = etCVV.text.toString().trim()
            val cardHolderName = etCardHolderName.text.toString().trim()

            if (!isCardNumberValid(cardNumber)) {
                showToast(
                    requireContext(),
                    getString(R.string.warning_enter_card_number)
                )
                hideProgressBar()
            } else if (!isCardExpiryValid(cardExpiry)) {
                showToast(
                    requireContext(),
                    getString(R.string.wraning_card_expiry)
                )
                hideProgressBar()
            } else if (!isCardCvvValid(cardCVV)) {
                showToast(
                    requireContext(),
                    getString(R.string.warning_card_cvv)
                )
                hideProgressBar()
            } else if (!isNameValid(cardHolderName)) {
                showToast(
                    requireContext(),
                    getString(R.string.warning_card_holder_name)
                )
                hideProgressBar()
            } else {
                val call = RetrofitInstance.get().create(ApiInterface::class.java).setUpIntent()
                call.enqueue(object : Callback<SetUpIntentResponse> {
                    override fun onResponse(
                        call: Call<SetUpIntentResponse>,
                        response: Response<SetUpIntentResponse>
                    ) {
                        response.apply {
                            if (isSuccessful) {
                                Log.d(TAG, "onResponse: Got response from setUpIntent Api ${body()!!.client_secret}")
                                clientSecret = body()!!.client_secret
                                val card = CardParams(
                                    cardNumber,
                                    cardExpiry.substring(0..1).toInt(),
                                    cardExpiry.substring(3..4).toInt(),
                                    cardCVV)

                                // getting paymentMethodeCreateParams from CardParams
                                cardParams = PaymentMethodCreateParams.createCard(card)

                                val confirmSetupIntentParams = ConfirmSetupIntentParams.create(cardParams, clientSecret)
                                
                                stripe.confirmSetupIntent(
                                    this@AddCardFragment,
                                    confirmSetupIntentParams)

                            }
                            /* for stripe ui to add card */
//                          AddPaymentMethodActivityStarter(requireActivity()).startForResult()
                            else {
                                hideProgressBar()
                                getErrorMessage(errorBody()!!, requireContext(), code())
                            }
                        }
                    }

                    override fun onFailure(call: Call<SetUpIntentResponse>, t: Throwable) {
                        hideProgressBar()
                        t.message.toString().showToast(requireContext())
                    }
                })
            }
        }
        etExpiry.addTextChangedListener(expiryTextWatcher)

    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode,resultCode,data)
        stripe.onSetupResult(requestCode,data,object : ApiResultCallback<SetupIntentResult>{

            override fun onSuccess(result: SetupIntentResult) {
                sendPaymentId(result.intent.paymentMethodId!!)
                ApplicationGlobal.paymentId = result.intent.paymentMethodId!!
            }

            override fun onError(e: Exception) {
                hideProgressBar()
                e.message.toString().showToast(requireContext())
            }
        })
    }

    private val expiryTextWatcher = object : TextWatcher {

        override fun afterTextChanged(s: Editable?) {

        }


        override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {

        }

        override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
            when (s?.length) {
                1 -> shouldAdd = true

                2 -> {
                    if (shouldAdd) {
                        etExpiry.setText("$s/")
                    }
                    if(s.toString().toInt()>12){
                        etExpiry.setText("${s[0]}")
                    }
                }
                3 -> {
                    if (!shouldAdd) {
                        etExpiry.setText(s.subSequence(0, 2))
                    }
                    shouldAdd = false
                }
            }
            etExpiry.setSelection(etExpiry.text.length)
        }
    }

    private fun sendPaymentId(paymentId:String){
        val hashMap = HashMap<String,Any>()
        hashMap["payment_method"] = paymentId
        val call = RetrofitInstance.get().create(ApiInterface::class.java).addCard(hashMap)
        call.enqueue(object :Callback<MessageResponse>{
            override fun onResponse(
                call: Call<MessageResponse>,
                response: Response<MessageResponse>
            ) {
                response.apply {
                    if(isSuccessful){
                        if(toAddCard){
                            hideProgressBar()
                            requireActivity().onBackPressed()
                        }else{
                            hideProgressBar()
                            replaceFragment(R.id.frameLayout,SignAgreementFragment(),true)
                        }
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