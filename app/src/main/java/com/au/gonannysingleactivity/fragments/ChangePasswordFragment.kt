package com.au.gonannysingleactivity.fragments

import android.os.Bundle
import android.view.View
import com.au.gonannysingleactivity.R
import com.au.gonannysingleactivity.utils.CommonFunctions.Companion.getErrorMessage
import com.au.gonannysingleactivity.utils.CommonFunctions.Companion.hideProgressBar
import com.au.gonannysingleactivity.utils.CommonFunctions.Companion.isValidPassword
import com.au.gonannysingleactivity.utils.CommonFunctions.Companion.showProgressBar
import com.au.gonannysingleactivity.utils.Extensions.Companion.showToast
import com.au.gonannysingleactivity.webservices.MessageResponse
import com.au.gonannysingleactivity.webservices.api.ApiInterface
import com.au.gonannysingleactivity.webservices.api.RetrofitInstance
import kotlinx.android.synthetic.main.fragment_setting_change_password.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class ChangePasswordFragment:BaseFragment() {
    override fun getLayoutToInflate(): Int {
        return R.layout.fragment_setting_change_password
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setUpToolbar("Change Password",true,null){}

        btnUpdatePassword.setOnClickListener {
            if(!isValidPassword(etOldPassword.text.toString().trim())){
                getString(R.string.password_info).showToast(requireContext())
            }else if(!isValidPassword(etNewPassword.text.toString().trim())){
                getString(R.string.password_info).showToast(requireContext())
            }else{
                showProgressBar(requireContext())
                val hashMap = HashMap<String,Any>()
                hashMap["old_password"] = etOldPassword.text.toString().trim()
                hashMap["new_password"] = etNewPassword.text.toString().trim()
                val call = RetrofitInstance.get().create(ApiInterface::class.java).changePassword(hashMap)
                call.enqueue(object : Callback<MessageResponse>{
                    override fun onResponse(
                        call: Call<MessageResponse>,
                        response: Response<MessageResponse>
                    ) {
                        hideProgressBar()
                        response.apply {
                            if(isSuccessful){
                                response.body()!!.message.showToast(requireContext())
                                requireActivity().onBackPressed()
                            }else{
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