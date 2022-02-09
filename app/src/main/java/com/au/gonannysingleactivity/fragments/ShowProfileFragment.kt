package com.au.gonannysingleactivity.fragments

import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat.getColor
import com.au.gonannysingleactivity.R
import com.au.gonannysingleactivity.objects.Constants.BASE_IMAGE_URL
import com.au.gonannysingleactivity.utils.ApplicationGlobal
import com.au.gonannysingleactivity.utils.ApplicationGlobal.Companion.isChanged
import com.au.gonannysingleactivity.utils.ApplicationGlobal.Companion.userObject
import com.au.gonannysingleactivity.utils.ApplicationGlobal.Companion.userType
import com.au.gonannysingleactivity.utils.CommonFunctions.Companion.getColorForLatter
import com.au.gonannysingleactivity.utils.CommonFunctions.Companion.getErrorMessage
import com.au.gonannysingleactivity.utils.CommonFunctions.Companion.hideProgressBar
import com.au.gonannysingleactivity.utils.CommonFunctions.Companion.showProgressBar
import com.au.gonannysingleactivity.utils.Extensions.Companion.showToast
import com.au.gonannysingleactivity.webservices.MessageResponse
import com.au.gonannysingleactivity.webservices.SendOtpResponse
import com.au.gonannysingleactivity.webservices.User
import com.au.gonannysingleactivity.webservices.api.ApiInterface
import com.au.gonannysingleactivity.webservices.api.RetrofitInstance
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DecodeFormat
import com.bumptech.glide.request.RequestOptions
import com.bumptech.glide.request.target.Target.SIZE_ORIGINAL
import kotlinx.android.synthetic.main.fragment_show_profile.*
import kotlinx.android.synthetic.main.fragment_show_profile.view.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class ShowProfileFragment : BaseFragment(),View.OnClickListener {

    private lateinit var ivProfilePic:ImageView
    private lateinit var tvProfilePic:TextView

    override fun getLayoutToInflate(): Int {
        return R.layout.fragment_show_profile
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setUpToolbar(getString(R.string.profile), true, null) {}

        ivProfilePic = view.findViewById(R.id.ivProfilePic)
        tvProfilePic = view.findViewById(R.id.tvProfilePic)

        btnEditProfile.setOnClickListener(this)
        tvEmailVerify.setOnClickListener(this)
        tvPhoneVerify.setOnClickListener(this)

        showProgressBar(requireContext())
        val call = RetrofitInstance.get().create(ApiInterface::class.java).getProfile()
        call.enqueue(object : Callback<User> {
            override fun onResponse(call: Call<User>, response: Response<User>) {
                response.apply {
                    if (isSuccessful) {
                        ApplicationGlobal.apply {
                            userObject = body()!!
                            preferenceManager.apply {
                                saveUserObject(body()!!)
                            }
                            updateUserInfo(userObject!!)
                        }
                        hideProgressBar()
                    } else {
                        hideProgressBar()
                        getErrorMessage(errorBody()!!, requireContext(), code())
                    }
                }
            }

            override fun onFailure(call: Call<User>, t: Throwable) {
                hideProgressBar()
                t.message.toString().showToast(requireContext())
            }
        })

    }

    override fun onResume() {
        super.onResume()
        if (isChanged){
            updateUserInfo(userObject!!)
        }
    }

    private fun updateUserInfo(userObject:User) {
        userObject.apply {
            tvName.text = "$first_name $last_name"
            tvValueEmail.text = email.toString()
            tvValuePhoneNumber.text = phone_number.toString()

            if (profile_image == null) {

                (ivProfilePic.background as GradientDrawable).setColor(
                    getColor(
                        requireContext(),
                        getColorForLatter(
                            first_name.toString()[0].toString().lowercase()
                        )
                    )
                )

                tvProfilePic.text = first_name.toString()[0].toString()
            } else {
                tvProfilePic.visibility = View.GONE
                Glide.with(requireContext())
                    .load("$BASE_IMAGE_URL${profile_image}").apply(
                        RequestOptions().circleCrop().override(SIZE_ORIGINAL).placeholder(R.drawable.ic_profile_large)
                    ).into(ivProfilePic)
            }
            if (is_email_verified == 0) {
                tvEmailVerify.visibility = View.VISIBLE
                ivEmailVerified.visibility = View.GONE
            } else {
                tvEmailVerify.visibility = View.GONE
                ivEmailVerified.visibility = View.VISIBLE
            }

            if (is_phone_verified == 0) {
                tvPhoneVerify.visibility = View.VISIBLE
                ivPhoneVerified.visibility = View.GONE
            } else {
                tvPhoneVerify.visibility = View.GONE
                ivPhoneVerified.visibility = View.VISIBLE
            }

        }
    }

    override fun onClick(v: View?) {
        when(v!!.id){
            R.id.btnEditProfile->replaceFragment(R.id.flCommonReplacement, EditProfileFragment(), true)
            R.id.tvEmailVerify-> {
                showProgressBar(requireContext())
                val hashMap = HashMap<String, Any>()

                hashMap["email"] = tvValueEmail.text.toString()

                val call = RetrofitInstance.get().create(ApiInterface::class.java).verifyEmailPhone(hashMap)

                call.enqueue(object : Callback<MessageResponse> {
                    override fun onResponse(
                        call: Call<MessageResponse>,
                        response: Response<MessageResponse>
                    ) {
                        hideProgressBar()
                        response.apply {
                            if (isSuccessful) {
                                body()!!.message.showToast(requireContext())
                            } else {
                                getErrorMessage(errorBody()!!, requireContext(), code())
                            }
                        }
                    }

                    override fun onFailure(call: Call<MessageResponse>, t: Throwable) {
                        hideProgressBar()
                        t.message!!.showToast(requireContext())
                    }
                })
            }
            R.id.tvPhoneVerify->{
                showProgressBar(requireContext())
                val hashMap = HashMap<String,Any>()
                hashMap["country_code"] = userObject!!.country_code
                hashMap["phone_number"] = userObject!!.phone_number
                val call = RetrofitInstance.get().create(ApiInterface::class.java).verifyEmailPhone(hashMap)
                call.enqueue(object : Callback<MessageResponse>{
                    override fun onResponse(
                        call: Call<MessageResponse>,
                        response: Response<MessageResponse>
                    ) {
                        response.apply {
                            if(isSuccessful){
                                hideProgressBar()
                                replaceFragment(R.id.flCommonReplacement,VerifyNumberFragment().apply {
                                    arguments = Bundle().apply {
                                        putBoolean("fromProfile",true)
                                    }
                                },true)
                            }else{
                                hideProgressBar()
                                getErrorMessage(errorBody()!!,requireContext(),code())
                            }
                        }
                    }

                    override fun onFailure(call: Call<MessageResponse>, t: Throwable) {
                        hideProgressBar()
                        t.message.toString().showToast(requireContext())
                    }
                })
            }
        }
    }
}