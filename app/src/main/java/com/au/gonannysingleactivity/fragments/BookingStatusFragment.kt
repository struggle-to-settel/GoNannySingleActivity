package com.au.gonannysingleactivity.fragments

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.recyclerview.widget.LinearLayoutManager
import com.au.gonannysingleactivity.R
import com.au.gonannysingleactivity.activities.CommonReplacementActivity
import com.au.gonannysingleactivity.adapters.BookingStatusAdapter
import com.au.gonannysingleactivity.objects.Constants.KEY_FOR_FRAGMENT_REPLACEMENT
import com.au.gonannysingleactivity.objects.Constants.KEY_SHOW_BOOKING_DETAIL
import com.au.gonannysingleactivity.utils.ApplicationGlobal.Companion.isChanged
import com.au.gonannysingleactivity.utils.CommonFunctions.Companion.getErrorMessage
import com.au.gonannysingleactivity.utils.CommonFunctions.Companion.hideProgressBar
import com.au.gonannysingleactivity.utils.CommonFunctions.Companion.showProgressBar
import com.au.gonannysingleactivity.utils.Extensions.Companion.showToast
import com.au.gonannysingleactivity.utils.PagingScrollListener
import com.au.gonannysingleactivity.webservices.Booking
import com.au.gonannysingleactivity.webservices.ShowBookingsResponse
import com.au.gonannysingleactivity.webservices.api.RetrofitInstance
import kotlinx.android.synthetic.main.fragment_booking_status.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

const val KEY_FRAGMENT_TYPE = "key_fragment_type"

class BookingStatusFragment : BaseFragment(), BookingStatusAdapter.OnClickListener {

    private var fragmentType: Int = 0
    private var page: Int = 1
    private var isLoading: Boolean = true
    private var notHaveData: Boolean = true

    private var list: MutableList<Booking> = mutableListOf()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        fragmentType = arguments?.getInt(KEY_FRAGMENT_TYPE, 0)!!
        fragmentType = fragmentType.plus(1)
    }

    override fun getLayoutToInflate(): Int {
        return R.layout.fragment_booking_status
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        hideProgressBar()
        rvBookingStatus.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = BookingStatusAdapter(
                requireContext(),
                list,
                this@BookingStatusFragment
            )
            addOnScrollListener(object :
                PagingScrollListener(layoutManager as LinearLayoutManager) {

                override fun isLoading(): Boolean {
                    return isLoading
                }

                override fun loadMore() {
                    page++
                    progressCard.visibility = View.VISIBLE
                    loadData(true)
                }

                override fun notHaveData(): Boolean {
                    return notHaveData
                }
            })
        }

        loadData(false)

        refreshSwipe.setColorSchemeResources(R.color.defaultRed)
        refreshSwipe.setOnRefreshListener {
            loadData(true)
        }
    }

    private fun loadData(fromRefreshLoader: Boolean) {
        isLoading = true
        if (!fromRefreshLoader) {
            showProgressBar(requireContext())
        }

        RetrofitInstance.create().showBookings(page, 10, fragmentType, 1)
            .enqueue(object : Callback<ShowBookingsResponse> {
                override fun onResponse(
                    call: Call<ShowBookingsResponse>,
                    response: Response<ShowBookingsResponse>
                ) {
                    response.apply {
                        if (isSuccessful) {
                            notHaveData = body()!!.count < 10
                            if (body()!!.count > 0) {
                                notHaveData = false
                                val startSize = list.count()
                                list.addAll(body()!!.bookings)
                                rvBookingStatus.adapter!!.notifyItemRangeChanged(
                                    startSize,
                                    list.count()
                                )
                            }

                        } else {
                            getErrorMessage(errorBody()!!, requireContext(), code())
                        }
                    }
                    hideProgressBar()
                    progressCard.visibility = View.GONE
                    refreshSwipe.isRefreshing = false
                    tvNoBookings.visibility = if (list.size > 0) View.GONE else View.VISIBLE
                    isLoading = false
                }

                override fun onFailure(call: Call<ShowBookingsResponse>, t: Throwable) {
                    progressCard.visibility = View.GONE
                    refreshSwipe.isRefreshing = false
                    notHaveData = false
                    isLoading = false
                    hideProgressBar()
                    t.message!!.toString().showToast(requireContext())
                }
            })
    }

    override fun onResume() {
        super.onResume()
        if (isChanged) {
            refreshSwipe.isRefreshing = true
            loadData(true)
        }
    }

    companion object {
        fun newInstance(fragmentType: Int): BookingStatusFragment {
            return BookingStatusFragment().apply {
                arguments = Bundle().apply {
                    putInt(KEY_FRAGMENT_TYPE, fragmentType)
                }
            }
        }
    }

    override fun onViewDetailClick(bookingId: Int) {
        startActivity(Intent(requireActivity(), CommonReplacementActivity::class.java).apply {
            putExtra(KEY_FOR_FRAGMENT_REPLACEMENT, KEY_SHOW_BOOKING_DETAIL)
            putExtra("booking_id", bookingId)
        })
    }

}

