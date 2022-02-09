package com.au.gonannysingleactivity.fragments

import android.os.Bundle
import android.view.View
import android.widget.CompoundButton
import com.au.gonannysingleactivity.R
import com.au.gonannysingleactivity.objects.Constants.KEY_WORKING_HOURS
import com.au.gonannysingleactivity.utils.ApplicationGlobal
import com.au.gonannysingleactivity.utils.CommonFunctions.Companion.getErrorMessage
import com.au.gonannysingleactivity.utils.CommonFunctions.Companion.hideProgressBar
import com.au.gonannysingleactivity.utils.CommonFunctions.Companion.isConnectedToInternet
import com.au.gonannysingleactivity.utils.CommonFunctions.Companion.showLogOutDialog
import com.au.gonannysingleactivity.utils.CommonFunctions.Companion.showProgressBar
import com.au.gonannysingleactivity.utils.Extensions.Companion.showToast
import com.au.gonannysingleactivity.webservices.MessageResponse
import com.au.gonannysingleactivity.webservices.WorkingHourList
import com.au.gonannysingleactivity.webservices.api.RetrofitInstance
import kotlinx.android.synthetic.main.fragment_working_hour.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class WorkingHourFragment : BaseFragment(), CompoundButton.OnCheckedChangeListener {


    private var list: MutableList<WorkingHourList> = mutableListOf()
    private var toEdit: Boolean = false
    private var listType: ArrayList<Int> = arrayListOf()
    private var listHourIds: ArrayList<Int> = arrayListOf()

    override fun getLayoutToInflate(): Int {
        return R.layout.fragment_working_hour
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            toEdit = it.getBoolean("toEdit", false)
            listType = it.getIntegerArrayList("listType")!!
            listHourIds = it.getIntegerArrayList("listHourIds")!!
        }
        if (!toEdit)
            ApplicationGlobal.preferenceManager.setScreenState(KEY_WORKING_HOURS)
    }

    override fun modifyBackIconFunctionality() {
        requireActivity().apply {
            if (supportFragmentManager.backStackEntryCount > 0) this.onBackPressed()
            else showLogOutDialog(requireContext(), this)
        }
    }

    override fun modifyBackIcon() = true

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setUpToolbar(getString(R.string.working_hour), true, null) {}

        cbWorkingHourOne.setOnCheckedChangeListener(this)
        cbWorkingHourTwo.setOnCheckedChangeListener(this)
        cbWorkingHourThree.setOnCheckedChangeListener(this)
        cbWorkingHourFour.setOnCheckedChangeListener(this)

        btnSave.setOnClickListener {
            if (isConnectedToInternet(requireContext())) {
                if (list.isNotEmpty()) {
                    showProgressBar(requireContext())
                    RetrofitInstance.create()
                        .addWorkingHours(hashMapOf("add_working_hours" to list)).enqueue(object :
                            Callback<MessageResponse> {
                            override fun onResponse(
                                call: Call<MessageResponse>,
                                response: Response<MessageResponse>
                            ) {
                                response.apply {
                                    if (isSuccessful) {
                                        hideProgressBar()

                                        if (toEdit)
                                            requireActivity().onBackPressed()
                                        else
                                            replaceFragment(
                                                R.id.frameLayout,
                                                LinkBankAccountFragment(),
                                                true
                                            )
                                    } else {
                                        hideProgressBar()
                                        getErrorMessage(errorBody()!!, requireContext(), code())
                                    }
                                }
                            }

                            override fun onFailure(call: Call<MessageResponse>, t: Throwable) {
                                hideProgressBar()
                                t.message!!.showToast(requireContext())
                            }
                        })

                } else {
                    getString(R.string.select_an_working_hour).showToast(requireContext())
                }
            } else getString(R.string.check_internt).showToast(requireContext())
        }

        if (toEdit) {
            showProgressBar(requireContext())
            btnSave.text = getString(R.string.save_changes)
            RetrofitInstance.create()
                .deleteWorkingHours(hashMapOf("delete_hour_ids" to listHourIds))
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
                            if (isSuccessful) {
                                hideProgressBar()
                            } else {
                                hideProgressBar()
                                getErrorMessage(errorBody()!!, requireContext(), code())
                            }
                        }
                    }
                })
        } else
            list.add(WorkingHourList(1, getString(R.string.working_hour_one)))
    }

    override fun onCheckedChanged(buttonView: CompoundButton?, isChecked: Boolean) {
        when (buttonView?.id) {
            R.id.cbWorkingHourOne -> {
                (list.find { it.working_hours_type == 1 }).apply {
                    if (isChecked && this == null)
                        list.add(WorkingHourList(1, getString(R.string.working_hour_one)))
                    else
                        if (this != null) list.remove(this)

                }
            }
            R.id.cbWorkingHourTwo -> {
                list.find { it.working_hours_type == 2 }.apply {
                    if (isChecked && this == null)
                        list.add(WorkingHourList(2, getString(R.string.working_hour_two)))
                    else
                        if (this != null) list.remove(this)

                }
            }
            R.id.cbWorkingHourThree -> {
                list.find { it.working_hours_type == 3 }.apply {
                    if (isChecked && this == null)
                        list.add(WorkingHourList(3, getString(R.string.working_hour_three)))
                    else
                        if (this != null) list.remove(this)
                }
            }
            R.id.cbWorkingHourFour -> {
                list.find { it.working_hours_type == 4 }.apply {
                    if (isChecked && this==null)
                        list.add(WorkingHourList(4, getString(R.string.working_hour_four)))
                    else
                        if (this != null) list.remove(this)
                }
            }
        }
    }


}