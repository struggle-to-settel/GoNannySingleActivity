package com.au.gonannysingleactivity.fragments

import android.os.Bundle
import android.view.View
import com.au.gonannysingleactivity.R
import com.au.gonannysingleactivity.objects.Constants.KEY_HOME
import com.au.gonannysingleactivity.utils.ApplicationGlobal
import com.au.gonannysingleactivity.utils.CommonFunctions.Companion.getErrorMessage
import com.au.gonannysingleactivity.utils.CommonFunctions.Companion.hideProgressBar
import com.au.gonannysingleactivity.utils.CommonFunctions.Companion.showProgressBar
import com.au.gonannysingleactivity.utils.Extensions.Companion.showToast
import com.au.gonannysingleactivity.webservices.UpdateUserProfileResponse
import com.au.gonannysingleactivity.webservices.User
import com.au.gonannysingleactivity.webservices.api.RetrofitInstance
import kotlinx.android.synthetic.main.activity_home.*
import kotlinx.android.synthetic.main.activity_home.view.*
import kotlinx.android.synthetic.main.fragment_nanny_home.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class NannyHomeFragment : BaseFragment() {

    override fun getLayoutToInflate(): Int {
        return R.layout.fragment_nanny_home
    }

    override fun registerFirebaseBroadcastReceiver(): Boolean = true

    // handle push notifications here
    override fun onFirebaseMessageReceived(data: Bundle) {

    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        ApplicationGlobal.preferenceManager.setScreenState(KEY_HOME)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        showProgressBar(requireContext())
        RetrofitInstance.create().getProfile().enqueue(object : Callback<User> {
            override fun onFailure(call: Call<User>, t: Throwable) {
                hideProgressBar()
                t.message!!.showToast(requireContext())
            }

            override fun onResponse(call: Call<User>, response: Response<User>) {
                response.apply {
                    if (isSuccessful) {
                        ApplicationGlobal.apply {

                            userObject = body()!!
                            preferenceManager.saveUserObject(body()!!)

                            if (userObject!!.is_active == 1) {
                                tvReadyForRequest.visibility = View.VISIBLE
                                tvNewRequest.visibility = View.GONE
                                rlOffline.visibility = View.GONE
                            } else {
                                tvReadyForRequest.visibility = View.GONE
                                tvNewRequest.visibility = View.GONE
                                rlOffline.visibility = View.VISIBLE
                            }
                            hideProgressBar()
                        }
                    } else {
                        hideProgressBar()
                        getErrorMessage(errorBody()!!, requireContext(), code())
                    }
                }
            }
        })

        requireActivity().toolbarHome.apply {
            toolbarHome.switchHome.setOnCheckedChangeListener { view, isChecked ->
                tvOnline.visibility = if (isChecked) View.VISIBLE else View.GONE
                tvOffline.visibility = if (isChecked) View.GONE else View.VISIBLE

                showProgressBar(requireContext())
                RetrofitInstance.create()
                    .updateUserProfile(hashMapOf("is_active" to if (isChecked) 1 else 0))
                    .enqueue(object : Callback<UpdateUserProfileResponse> {
                        override fun onFailure(
                            call: Call<UpdateUserProfileResponse>,
                            t: Throwable
                        ) {
                            hideProgressBar()
                            t.message!!.showToast(requireContext())
                        }

                        override fun onResponse(
                            call: Call<UpdateUserProfileResponse>,
                            response: Response<UpdateUserProfileResponse>
                        ) {
                            response.apply {
                                if (isSuccessful) {
                                    ApplicationGlobal.apply {
                                        userObject = body()!!.user
                                        preferenceManager.saveUserObject(body()!!.user)
                                        if (body()!!.user.is_active == 1.0) {
                                            rlOffline.visibility = View.GONE
                                            tvReadyForRequest.visibility = View.VISIBLE
                                            tvNewRequest.visibility = View.GONE
                                        } else {
                                            rlOffline.visibility = View.VISIBLE
                                            tvReadyForRequest.visibility = View.GONE
                                            tvNewRequest.visibility = View.GONE
                                        }
                                    }
                                    hideProgressBar()
                                } else {
                                    hideProgressBar()
                                    getErrorMessage(errorBody()!!, requireContext(), code())
                                }
                            }
                        }
                    })
            }
        }

    }


}
