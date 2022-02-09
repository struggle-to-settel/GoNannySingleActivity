package com.au.gonannysingleactivity.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.au.gonannysingleactivity.R
import com.au.gonannysingleactivity.utils.CommonFunctions.Companion.getErrorMessage
import com.au.gonannysingleactivity.utils.CommonFunctions.Companion.hideProgressBar
import com.au.gonannysingleactivity.utils.CommonFunctions.Companion.showProgressBar
import com.au.gonannysingleactivity.utils.Extensions.Companion.showToast
import com.au.gonannysingleactivity.webservices.GetWorkingHoursResponse
import com.au.gonannysingleactivity.webservices.WorkingHour
import com.au.gonannysingleactivity.webservices.api.RetrofitInstance
import kotlinx.android.synthetic.main.fragment_get_working_hours.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class WorkingHoursFragment : BaseFragment() {

    private var listType: ArrayList<Int> = arrayListOf()
    private var listHourIds: ArrayList<Int> = arrayListOf()

    override fun getLayoutToInflate(): Int = R.layout.fragment_get_working_hours

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setUpToolbar(getString(R.string.working_hour), true, getString(R.string.edit)) {
            replaceFragment(R.id.flCommonReplacement, WorkingHourFragment().apply {
                arguments = Bundle().apply {
                    putBoolean("toEdit", true)
                    putIntegerArrayList("listType", listType)
                    putIntegerArrayList("listHourIds", listHourIds)
                }
            }, true)
        }

        showProgressBar(requireContext())
        RetrofitInstance.create().getWorkingHours()
            .enqueue(object : Callback<GetWorkingHoursResponse> {
                override fun onFailure(call: Call<GetWorkingHoursResponse>, t: Throwable) {
                    hideProgressBar()
                    t.message!!.showToast(requireContext())
                }

                override fun onResponse(
                    call: Call<GetWorkingHoursResponse>,
                    response: Response<GetWorkingHoursResponse>
                ) {
                    response.apply {
                        if (isSuccessful) {
                            rvWorkingHours.layoutManager = LinearLayoutManager(requireContext())
                            rvWorkingHours.adapter = WorkingHourAdapter(body()!!.working_hours)
                        } else {
                            hideProgressBar()
                            getErrorMessage(errorBody()!!, requireContext(), code())
                        }
                    }
                }
            })
    }

    inner class WorkingHourAdapter(private var list: List<WorkingHour>) :
        RecyclerView.Adapter<WorkingHourAdapter.ViewHolder>() {

        override fun getItemCount(): Int {
            return list.count()
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            return ViewHolder(
                LayoutInflater.from(requireContext())
                    .inflate(R.layout.item_working_hours, parent, false)
            )
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            holder.workingHour.text = list[position].working_hours
        }

        inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
            val workingHour: TextView = view.findViewById(R.id.tvWorkingHour)
        }

    }

}