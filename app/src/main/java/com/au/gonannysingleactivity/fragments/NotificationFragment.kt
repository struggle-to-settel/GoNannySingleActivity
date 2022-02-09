package com.au.gonannysingleactivity.fragments

import android.os.Bundle
import android.view.View
import androidx.recyclerview.widget.LinearLayoutManager
import com.au.gonannysingleactivity.R
import com.au.gonannysingleactivity.adapters.NotificationAdapter
import com.au.gonannysingleactivity.utils.CommonFunctions.Companion.getErrorMessage
import com.au.gonannysingleactivity.utils.CommonFunctions.Companion.hideProgressBar
import com.au.gonannysingleactivity.utils.CommonFunctions.Companion.showProgressBar
import com.au.gonannysingleactivity.utils.Extensions.Companion.showToast
import com.au.gonannysingleactivity.utils.PagingScrollListener
import com.au.gonannysingleactivity.webservices.NotificationResponse
import com.au.gonannysingleactivity.webservices.api.RetrofitInstance
import kotlinx.android.synthetic.main.fragment_notifications.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class NotificationFragment : BaseFragment() {

    lateinit var notificationAdapter: NotificationAdapter
    lateinit var lm: LinearLayoutManager
    private var isLoading: Boolean = true
    private var notHaveData: Boolean = false
    private var page = 1

    override fun getLayoutToInflate(): Int {
        return R.layout.fragment_notifications
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        rvNotification.apply {
            notificationAdapter = NotificationAdapter()
            adapter = notificationAdapter
            lm = LinearLayoutManager(requireContext())
            layoutManager = lm
            addOnScrollListener(object : PagingScrollListener(lm) {
                override fun isLoading(): Boolean {
                    return isLoading
                }

                override fun loadMore() {
                    page++
                    progressCard.visibility = View.VISIBLE
                    getNotifications()
                }

                override fun notHaveData(): Boolean {
                    return notHaveData
                }
            })
        }


        showProgressBar(requireContext())
        getNotifications()
    }

    private fun getNotifications() {
        isLoading = true
        RetrofitInstance.create().getNotifications(page, 10)
            .enqueue(object : Callback<NotificationResponse> {
                override fun onResponse(
                    call: Call<NotificationResponse>,
                    response: Response<NotificationResponse>
                ) {
                    response.apply {
                        if (isSuccessful) {

                            tvNoNotifications.visibility =
                                if (body()!!.count > 0) View.GONE else View.VISIBLE

                            if (body()!!.notifications.isNotEmpty()) {
                                notificationAdapter.updateList(body()!!.notifications)
                            }
                            notHaveData = body()!!.count < 10
                            hideProgressBar()

                            progressCard.visibility = View.GONE
                            isLoading = false
                        } else {
                            hideProgressBar()
                            notHaveData = true
                            isLoading = false
                            progressCard.visibility = View.GONE
                            getErrorMessage(errorBody()!!, requireContext(), code())
                        }
                    }
                }

                override fun onFailure(call: Call<NotificationResponse>, t: Throwable) {
                    hideProgressBar()
                    notHaveData = true
                    isLoading = false
                    progressCard.visibility = View.GONE
                    t.message!!.showToast(requireContext())
                }
            })
    }
}