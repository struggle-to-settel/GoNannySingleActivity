package com.au.gonannysingleactivity.fragments

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.Button
import android.widget.CheckBox
import android.widget.EditText
import android.widget.ImageView
import androidx.recyclerview.widget.LinearLayoutManager
import com.au.gonannysingleactivity.R
import com.au.gonannysingleactivity.adapters.SavedCardAdapter
import com.au.gonannysingleactivity.utils.CommonFunctions.Companion.getErrorMessage
import com.au.gonannysingleactivity.utils.CommonFunctions.Companion.hideProgressBar
import com.au.gonannysingleactivity.utils.CommonFunctions.Companion.isCardCvvValid
import com.au.gonannysingleactivity.utils.CommonFunctions.Companion.isCardExpiryValid
import com.au.gonannysingleactivity.utils.CommonFunctions.Companion.isCardNumberValid
import com.au.gonannysingleactivity.utils.CommonFunctions.Companion.showProgressBar
import com.au.gonannysingleactivity.utils.Extensions.Companion.showToast
import com.au.gonannysingleactivity.webservices.Card
import com.au.gonannysingleactivity.webservices.GetCardsResponse
import com.au.gonannysingleactivity.webservices.MessageResponse
import com.au.gonannysingleactivity.webservices.SetUpIntentResponse
import com.au.gonannysingleactivity.webservices.api.ApiInterface
import com.au.gonannysingleactivity.webservices.api.RetrofitInstance
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.stripe.android.ApiResultCallback
import com.stripe.android.PaymentConfiguration
import com.stripe.android.SetupIntentResult
import com.stripe.android.Stripe
import com.stripe.android.model.CardParams
import com.stripe.android.model.ConfirmSetupIntentParams
import com.stripe.android.model.PaymentMethodCreateParams
import kotlinx.android.synthetic.main.fragment_saved_card.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class SavedCardFragment : BaseFragment(),SavedCardAdapter.OnClickListener {
    private var price: Int = 0
    private var bookingId: Int = 0
    private var shouldSave: Boolean = false
    private var shouldAdd: Boolean = true
    private var paymentId: String = ""
    lateinit var stripe: Stripe
    override fun getLayoutToInflate(): Int {
        return R.layout.fragment_saved_card
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            price = it.getInt("totalPrice")
            bookingId = it.getInt("booking_id")
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setUpToolbar("Bill Total $$price", true, null) {}

        getCards()

        tvAddNewCardBottomSheet.setOnClickListener {
            val v = layoutInflater.inflate(R.layout.layout_add_card_bottom_sheet, null)
            val bottomSheet = BottomSheetDialog(requireContext())

            val close: ImageView = v.findViewById(R.id.ivClose)
            val cardNumber: EditText = v.findViewById(R.id.etCardNumber)
            val cardExpiry: EditText = v.findViewById(R.id.etCardExpiry)
            val cardCvv: EditText = v.findViewById(R.id.etCVV)
            addTextWatcher(cardExpiry)
            val checkBox: CheckBox = v.findViewById(R.id.cbSaveCard)
            val pay: Button = v.findViewById(R.id.btnSavePay)

            bottomSheet.apply {
                setContentView(v)
                setCanceledOnTouchOutside(false)
                dismissWithAnimation = true
            }
            close.setOnClickListener {
                bottomSheet.dismiss()
            }
            checkBox.setOnCheckedChangeListener { _, isChecked ->
                shouldSave = isChecked
                pay.text = if (isChecked) "Save And Pay" else "Pay"
            }

            bottomSheet.show()

            requireContext().let { cn ->
                pay.setOnClickListener {
                    if (!isCardNumberValid(cardNumber.text.toString())) {
                        getString(R.string.warning_enter_card_number).showToast(cn)
                    } else if (!isCardExpiryValid(cardExpiry.text.toString())) {
                        getString(R.string.wraning_card_expiry).showToast(cn)
                    } else if (!isCardCvvValid(cardCvv.text.toString())) {
                        getString(R.string.warning_card_cvv).showToast(cn)
                    } else {
                        showProgressBar(cn)
                        stripe = Stripe(
                            cn,
                            PaymentConfiguration.getInstance(cn).publishableKey
                        )
                        val arr = cardExpiry.text.split("/")

                        val card = CardParams(
                            cardNumber.text.toString(),
                            arr[0].toInt(),
                            arr[1].toInt(),
                            cardCvv.text.toString()
                        )

                        val params = PaymentMethodCreateParams.createCard(card)

                        RetrofitInstance.create().setUpIntent()
                            .enqueue(object :
                                Callback<SetUpIntentResponse> {
                                override fun onResponse(
                                    call: Call<SetUpIntentResponse>,
                                    response: Response<SetUpIntentResponse>
                                ) {
                                    if (response.isSuccessful) {
                                        val clientSecret: String = response.body()!!.client_secret
                                        val confirmSetUpIntentParams =
                                            ConfirmSetupIntentParams.create(params, clientSecret)
                                        stripe.confirmSetupIntent(
                                            this@SavedCardFragment,
                                            confirmSetUpIntentParams
                                        )
                                        hideProgressBar()
                                    } else {
                                        hideProgressBar()
                                        getErrorMessage(
                                            response.errorBody()!!,
                                            cn,
                                            response.code()
                                        )
                                    }
                                }

                                override fun onFailure(
                                    call: Call<SetUpIntentResponse>,
                                    t: Throwable
                                ) {
                                    hideProgressBar()
                                }
                            })
                    }
                }
            }

        }
    }

    private fun addTextWatcher(editText: EditText) {

        editText.addTextChangedListener(object : TextWatcher {

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {

            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                when (s?.length) {
                    1 -> shouldAdd = true

                    2 -> if (shouldAdd) {
                        editText.setText("$s/")
                    }
                    3 -> {
                        if (!shouldAdd) {
                            editText.setText(s.subSequence(0, 2))
                        }
                        shouldAdd = false
                    }
                }
                editText.setSelection(editText.text.length)
            }

            override fun afterTextChanged(s: Editable?) {

            }
        })

    }

    // for stripe setUpIntent confirmation
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        showProgressBar(requireContext())
        super.onActivityResult(requestCode, resultCode, data)
        stripe.onSetupResult(requestCode, data, object : ApiResultCallback<SetupIntentResult> {

            override fun onSuccess(result: SetupIntentResult) {
                paymentId = result.intent.paymentMethodId!!
                searchNanny()
            }

            override fun onError(e: Exception) {
                hideProgressBar()
                e.message!!.showToast(requireContext())
            }

        })
    }

    private fun searchNanny() {
        if (shouldSave) {
            RetrofitInstance.create().addCard(hashMapOf("payment_method" to paymentId))
                .enqueue(object : Callback<MessageResponse> {

                    override fun onFailure(call: Call<MessageResponse>, t: Throwable) {
                        hideProgressBar()
                        t.message!!.showToast(requireContext())
                        return
                    }

                    override fun onResponse(
                        call: Call<MessageResponse>,
                        response: Response<MessageResponse>
                    ) {
                        if (!response.isSuccessful) {
                            hideProgressBar()
                            getErrorMessage(
                                response.errorBody()!!,
                                requireContext(),
                                response.code()
                            )
                            return
                        }
                    }
                })
        }
        // after saving

        RetrofitInstance.create().searchNanny(bookingId,paymentId)
            .enqueue(object : Callback<MessageResponse> {

                override fun onFailure(call: Call<MessageResponse>, t: Throwable) {
                    hideProgressBar()
                    t.message!!.showToast(requireContext())
                }

                override fun onResponse(
                    call: Call<MessageResponse>,
                    response: Response<MessageResponse>
                ) {
                    response.apply {
                        if(isSuccessful){
                            hideProgressBar()
                            replaceFragment(R.id.flCommonReplacement,SearchNannyFragment(),true)
                        }else{
                            hideProgressBar()
                            getErrorMessage(errorBody()!!,requireContext(),code())
                        }
                    }
                }
            })
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
                        rvSavedCards.layoutManager = LinearLayoutManager(requireContext())
                        rvSavedCards.adapter = SavedCardAdapter(body()!!.cards as MutableList<Card>,this@SavedCardFragment)
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

    override fun onCardClicked(id: String) {
        paymentId = id
        searchNanny()
    }
}