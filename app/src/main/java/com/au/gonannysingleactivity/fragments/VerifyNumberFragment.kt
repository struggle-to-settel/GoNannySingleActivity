package com.au.gonannysingleactivity.fragments

import android.annotation.SuppressLint
import android.os.Bundle
import android.os.CountDownTimer
import android.provider.Settings
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import com.au.gonannysingleactivity.R
import com.au.gonannysingleactivity.utils.ApplicationGlobal
import com.au.gonannysingleactivity.utils.CommonFunctions.Companion.getDeviceId
import com.au.gonannysingleactivity.utils.CommonFunctions.Companion.getErrorMessage
import com.au.gonannysingleactivity.utils.CommonFunctions.Companion.hideProgressBar
import com.au.gonannysingleactivity.utils.CommonFunctions.Companion.isConnectedToInternet
import com.au.gonannysingleactivity.utils.CommonFunctions.Companion.showProgressBar
import com.au.gonannysingleactivity.utils.CommonFunctions.Companion.showToast
import com.au.gonannysingleactivity.webservices.SendOtpResponse
import com.au.gonannysingleactivity.webservices.VerifyOtpResponse
import com.au.gonannysingleactivity.webservices.api.ApiInterface
import com.au.gonannysingleactivity.webservices.api.RetrofitInstance
import kotlinx.android.synthetic.main.fragment_verify_number.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class VerifyNumberFragment : BaseFragment(), View.OnClickListener {

    var mobileNumber: Long = 0
    var countryCode: String = ""
    var fromProfile: Boolean = false

    private lateinit var call: Call<VerifyOtpResponse>
    private lateinit var timer: CountDownTimer

    override fun onDestroyView() {
        super.onDestroyView()
        timer.cancel()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.apply {
            mobileNumber = this.getLong("mobile_number")
            countryCode = this.getString("country_code", "")
            fromProfile = this.getBoolean("fromProfile", false)
        }
    }

    override fun getLayoutToInflate(): Int {
        return R.layout.fragment_verify_number
    }

    @SuppressLint("SetTextI18n")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        tvOtpSentTo.text = "${getString(R.string.otp_sent_to)} $countryCode $mobileNumber"

        ivBack.setOnClickListener(this)
        etOtp.apply {
            setOnClickListener(this@VerifyNumberFragment)
            addTextChangedListener(textWatcher)
        }
        tvSendAgain.setOnClickListener(this)

        timer = countDownTimer
        timer.start()
    }

    private val sendAgain: View.OnClickListener = View.OnClickListener {
        showProgressBar(requireContext())
        if (isConnectedToInternet(requireContext())) {

            val hashMap = HashMap<String, Any>()

            hashMap["phone_number"] = mobileNumber
            hashMap["country_code"] = countryCode
            hashMap["user_type"] = ApplicationGlobal.userType

            val call = RetrofitInstance.get().create(ApiInterface::class.java).sendOtp(hashMap)
            call.enqueue(object : Callback<SendOtpResponse> {
                override fun onResponse(
                    call: Call<SendOtpResponse>,
                    response: Response<SendOtpResponse>
                ) {
                    if (response.isSuccessful) {
                        hideProgressBar()
                        showToast(requireContext(), getString(R.string.message_sent_ok))
                        timer = countDownTimer
                        timer.start()
                    } else {
                        hideProgressBar()
                        getErrorMessage(
                            response.errorBody()!!,
                            requireContext(),
                            response.code()
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
            showToast(requireContext(), getString(R.string.check_internt))
        }
    }

    private val textWatcher = object : TextWatcher {
        @SuppressLint("HardwareIds")
        override fun afterTextChanged(s: Editable?) {
            if (s?.length == 4) {
                showProgressBar(requireContext())
                if (isConnectedToInternet(requireContext())) {

                    val hashMap = HashMap<String, Any>()
                    hashMap.let {
                        it["country_code"] = countryCode
                        it["phone_number"] = mobileNumber
                        it["otp"] = s.toString().toInt()
                        it["fcm_id"] = ApplicationGlobal.fcmId
                        it["device_id"] = getDeviceId(requireContext())
                        it["device_type"] = 1
                        it["user_type"] = ApplicationGlobal.userType
                    }

                    call = if (fromProfile) {
                        RetrofitInstance.get().create(ApiInterface::class.java)
                            .verifyPhone(hashMap)
                    } else {
                        RetrofitInstance.get().create(ApiInterface::class.java)
                            .verifyOtp(hashMap)
                    }
                    call.enqueue(object : Callback<VerifyOtpResponse> {
                        override fun onResponse(
                            call: Call<VerifyOtpResponse>,
                            response: Response<VerifyOtpResponse>
                        ) {
                            if (response.isSuccessful) {
                                hideProgressBar()

                                ApplicationGlobal.let {
                                    it.userObject = response.body()!!.user

                                    it.preferenceManager.apply {
                                        saveUserObject(it.userObject)
                                        if (!fromProfile) {
                                            it.accessToken = response.body()!!.token
                                            setAccessToken(it.accessToken)
                                            clearStack()
                                            replaceFragment(
                                                R.id.frameLayout,
                                                NumberVerifiedFragment(),
                                                false
                                            )
                                        }else{
                                            requireActivity().onBackPressed()
                                        }
                                    }
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

                } else {
                    hideProgressBar()
                    showToast(
                        requireContext(),
                        getString(R.string.check_internt)
                    )
                }
            }
        }

        override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {

        }

        override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {

            when (s?.length) {
                0 -> {
                    fillTextViews("", "", "", "")
                    highLightViews(
                        R.color.defaultRed,
                        R.color.oftenTextColors,
                        R.color.oftenTextColors,
                        R.color.oftenTextColors
                    )
                }
                1 -> {
                    fillTextViews(s[0].toString(), "", "", "")
                    highLightViews(
                        R.color.defaultRed,
                        R.color.defaultRed,
                        R.color.oftenTextColors,
                        R.color.oftenTextColors
                    )
                }
                2 -> {
                    fillTextViews(s[0].toString(), s[1].toString(), "", "")
                    highLightViews(
                        R.color.defaultRed,
                        R.color.defaultRed,
                        R.color.defaultRed,
                        R.color.oftenTextColors
                    )
                }
                3 -> {
                    fillTextViews(s[0].toString(), s[1].toString(), s[2].toString(), "")
                    highLightViews(
                        R.color.defaultRed,
                        R.color.defaultRed,
                        R.color.defaultRed,
                        R.color.defaultRed
                    )
                }
                4 -> {
                    fillTextViews(
                        s[0].toString(),
                        s[1].toString(),
                        s[2].toString(),
                        s[3].toString()
                    )
                }
            }

        }

    }

    private val countDownTimer = object : CountDownTimer(30000, 1000) {

        @SuppressLint("SetTextI18n")
        override fun onTick(millisUntilFinished: Long) {
            tvSendAgain.apply {
                setOnClickListener(null)
                (millisUntilFinished / 1000).let {
                    text = when (it.toString().length) {
                        1 -> "00:0$it"
                        else -> "00:$it"
                    }
                }

            }

        }

        override fun onFinish() {
            tvSendAgain.apply {
                setOnClickListener(sendAgain)
                text = getString(R.string.send_again)
            }
        }
    }

    fun highLightViews(id1: Int, id2: Int, id3: Int, id4: Int) {
        viewBorderOne.setBackgroundResource(id1)
        viewBorderTwo.setBackgroundResource(id2)
        viewBorderThree.setBackgroundResource(id3)
        viewBorderFour.setBackgroundResource(id4)
    }

    fun fillTextViews(sOne: String, sTwo: String, sThree: String, sFour: String) {
        tvOtpOne.text = sOne
        tvOtpTwo.text = sTwo
        tvOtpThree.text = sThree
        tvOtpFour.text = sFour
    }

    override fun onClick(v: View?) {
        when (v!!.id) {
            R.id.tvSendAgain -> sendAgain
            R.id.etOtp -> etOtp.setSelection(etOtp.text.length)
            R.id.ivBack -> requireActivity().onBackPressed()
        }
    }

}