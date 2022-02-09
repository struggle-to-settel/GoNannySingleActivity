package com.au.gonannysingleactivity.fragments

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.View
import androidx.recyclerview.widget.LinearLayoutManager
import com.au.gonannysingleactivity.R
import com.au.gonannysingleactivity.adapters.AddKidsInfoAdapter
import com.au.gonannysingleactivity.objects.Constants
import com.au.gonannysingleactivity.utils.ApplicationGlobal
import com.au.gonannysingleactivity.utils.CommonFunctions.Companion.getErrorMessage
import com.au.gonannysingleactivity.utils.CommonFunctions.Companion.hideProgressBar
import com.au.gonannysingleactivity.utils.CommonFunctions.Companion.isConnectedToInternet
import com.au.gonannysingleactivity.utils.CommonFunctions.Companion.isValidName
import com.au.gonannysingleactivity.utils.CommonFunctions.Companion.showProgressBar
import com.au.gonannysingleactivity.utils.CommonFunctions.Companion.showToast
import com.au.gonannysingleactivity.webservices.GetKidsResponse
import com.au.gonannysingleactivity.webservices.Kid
import com.au.gonannysingleactivity.webservices.MessageResponse
import com.au.gonannysingleactivity.webservices.api.ApiInterface
import com.au.gonannysingleactivity.webservices.api.RetrofitInstance
import kotlinx.android.synthetic.main.common_toolbar.*
import kotlinx.android.synthetic.main.fragment_add_kid_info.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

/*
    *This fragment is used for adding kId
 */
class AddKidInfoFragment : BaseFragment(), AddKidsInfoAdapter.OnItemClickListener {

    var isFromSelectKids: Boolean = false
    var isFromEditKid: Boolean = false
    var position: Int = 0
    lateinit var call: Call<MessageResponse>

    override fun getLayoutToInflate(): Int {
        return R.layout.fragment_add_kid_info
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        arguments?.apply {
            isFromSelectKids = getBoolean("fromSelectKids", false)
            isFromEditKid = getBoolean("fromEditKid", false)
            position = getInt("position", 0)
        }
    }

    @SuppressLint("SetTextI18n")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)



        if (isFromSelectKids) {
            setUpToolbar(getString(R.string.add_kids_info), true, null, null)
        } else if (isFromEditKid) {
            setUpToolbar(getString(R.string.edit_kid_info), true, null, null)
            kidsList[position].apply {
                etKidName.setText("$kid_first_name $kid_last_name")
                etKidBehaviour.setText(hourly_behaviour)
            }
        } else {
            setUpToolbar(getString(R.string.add_kids_info), false, getString(R.string.skip)) {
                replaceFragment(
                    R.id.frameLayout,
                    AddAddressFragment(),
                    true
                )
            }
            ApplicationGlobal.preferenceManager.setScreenState(Constants.KEY_ADD_KID_INFO)
            rvAddKidInfo.layoutManager = LinearLayoutManager(requireContext())
            getDataAndUpdateRecyclerAdapter()
        }
        rgAllergies.setOnCheckedChangeListener { _, id ->
            when (id) {
                R.id.rbAllergiesYes -> etAllergy.visibility = View.VISIBLE
                else -> etAllergy.visibility = View.GONE
            }
        }
        btnAdd.setOnClickListener {

            if (isConnectedToInternet(requireContext())) {
                if (!isValidName(etKidName.text.toString().trim())) {
                    showToast(requireContext(), getString(R.string.enter_valid_name))
                } else if (!isValidName(etKidBehaviour.text.toString().trim())) {
                    showToast(requireContext(), getString(R.string.enter_behaviour))
                } else if (rbAllergiesYes.isChecked && etAllergy.text.trim().toString().isEmpty()) {
                    showToast(requireContext(), "Please Specify allergy")
                } else {
                    showProgressBar(requireContext())
                    val hashMap = getDataFromFields()
                    val service = RetrofitInstance.get().create(ApiInterface::class.java)
                    // Clear fields
                    etKidName.setText("")
                    etKidBehaviour.setText("")
                    if (rbAllergiesYes.isChecked) {
                        etAllergy.setText("")
                    }

                    call = if (isFromEditKid) {
                        hashMap["kid_id"] = kidsList[position].id
                        service.updateKidInfo(hashMap)
                    } else {
                        service.addKidInfo(hashMap)
                    }

                    call.enqueue(object : Callback<MessageResponse> {
                        override fun onResponse(
                            call: Call<MessageResponse>,
                            response: Response<MessageResponse>
                        ) {
                            if (response.isSuccessful) {
                                hideProgressBar()
                                if (isFromSelectKids || isFromEditKid) {
                                    requireActivity().onBackPressed()
                                } else {
                                    getDataAndUpdateRecyclerAdapter()
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

                        override fun onFailure(call: Call<MessageResponse>, t: Throwable) {
                            hideProgressBar()
                            showToast(requireContext(), t.message.toString())
                        }
                    })
                }
            } else {
                hideProgressBar()
                showToast(requireContext(), getString(R.string.check_internt))
            }
        }
    }


    private fun getDataFromFields(): HashMap<String, Any> {

        val hashMap = HashMap<String, Any>()
        val names = etKidName.text.toString().trim().split(" ")

        hashMap["first_name"] = names[0]
        hashMap["last_name"] = if (names.size > 1) names[1] else ""
        hashMap["hourly_behaviour"] = etKidBehaviour.text.toString().trim()
        hashMap["indoor_camera"] = if (rbIndoorCameraYes.isChecked) 1 else 0

        if (rbAllergiesYes.isChecked) {
            hashMap["allergies"] = 1
            hashMap["allergy"] = etAllergy.text.toString().trim()
        } else {
            hashMap["allergies"] = 0
            hashMap["allergy"] = ""
        }

        return hashMap
    }

    private fun getDataAndUpdateRecyclerAdapter() {

        val service = RetrofitInstance.get().create(ApiInterface::class.java)
        val call = service.getKidsList()

        call.enqueue(object : Callback<GetKidsResponse> {
            override fun onResponse(
                call: Call<GetKidsResponse>,
                response: Response<GetKidsResponse>
            ) {
                if (response.isSuccessful) {
                    hideProgressBar()
                    if (response.body()!!.kids.isNotEmpty()) {
                        tvToolbarNavigation.text = getString(R.string.next)
                        kidsList = response.body()!!.kids as MutableList<Kid>
                        rvAddKidInfo.adapter =
                            AddKidsInfoAdapter(requireContext(), kidsList, this@AddKidInfoFragment)
                    } else {
                        tvToolbarNavigation.text = getString(R.string.skip)
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

            override fun onFailure(call: Call<GetKidsResponse>, t: Throwable) {
                hideProgressBar()
                showToast(requireContext(), t.message.toString())
            }
        })
    }

    override fun onDeleteClick(position: Int) {
        val id = kidsList[position].id
        deleteKid(id, position)
        getDataAndUpdateRecyclerAdapter()
    }

    override fun onEditClick(position: Int) {
        replaceFragment(R.id.frameLayout, AddKidInfoFragment().apply {
            arguments = Bundle().apply {
                kidsList[position].let {
                    putBoolean("fromEditKid", true)
                    putInt("kidPosition", position)
                }

            }
        }, true)
    }

    private fun deleteKid(id: Int, position: Int) {
        showProgressBar(requireContext())
        if (isConnectedToInternet(requireContext())) {
            val service = RetrofitInstance.get().create(ApiInterface::class.java)
            val call = service.deleteKid(id)

            call.enqueue(object : Callback<MessageResponse> {

                override fun onResponse(
                    call: Call<MessageResponse>,
                    response: Response<MessageResponse>
                ) {
                    if (response.isSuccessful) {
                        hideProgressBar()
                        kidsList.removeAt(position)
                        rvAddKidInfo.adapter?.notifyItemRemoved(position)
                        getDataAndUpdateRecyclerAdapter()
                    } else {
                        hideProgressBar()
                        getErrorMessage(
                            response.errorBody()!!,
                            requireContext(),
                            response.code()
                        )
                    }
                }

                override fun onFailure(call: Call<MessageResponse>, t: Throwable) {
                    hideProgressBar()
                    showToast(requireContext(), t.message.toString())
                }
            })
        } else {
            hideProgressBar()
            showToast(requireContext(), requireContext().getString(R.string.check_internt))
        }
    }
}