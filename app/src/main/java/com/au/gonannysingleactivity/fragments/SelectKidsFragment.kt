package com.au.gonannysingleactivity.fragments

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.recyclerview.widget.LinearLayoutManager
import com.au.gonannysingleactivity.R
import com.au.gonannysingleactivity.adapters.AddKidsInfoAdapter
import com.au.gonannysingleactivity.adapters.SelectKidsAdapter
import com.au.gonannysingleactivity.objects.Constants
import com.au.gonannysingleactivity.utils.CommonFunctions.Companion.getErrorMessage
import com.au.gonannysingleactivity.utils.CommonFunctions.Companion.hideProgressBar
import com.au.gonannysingleactivity.utils.CommonFunctions.Companion.isConnectedToInternet
import com.au.gonannysingleactivity.utils.CommonFunctions.Companion.showProgressBar
import com.au.gonannysingleactivity.utils.CommonFunctions.Companion.showToast
import com.au.gonannysingleactivity.utils.Extensions.Companion.showToast
import com.au.gonannysingleactivity.webservices.GetKidsResponse
import com.au.gonannysingleactivity.webservices.Kid
import com.au.gonannysingleactivity.webservices.MessageResponse
import com.au.gonannysingleactivity.webservices.api.ApiInterface
import com.au.gonannysingleactivity.webservices.api.RetrofitInstance
import kotlinx.android.synthetic.main.activity_common_replacement.*
import kotlinx.android.synthetic.main.activity_common_replacement.view.*
import kotlinx.android.synthetic.main.fragment_add_kid_info.*
import kotlinx.android.synthetic.main.fragment_select_kid.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class SelectKidsFragment : BaseFragment(), SelectKidsAdapter.OnSelectClickListener,
    AddKidsInfoAdapter.OnItemClickListener {

    // to show only added kids in settings fragment
    private var fromSettingsKids: Boolean = false


    companion object{
        fun fromSettingKids():SelectKidsFragment = SelectKidsFragment().apply {
            arguments = Bundle().apply {
                putBoolean("fromSettingsKids",true)
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.apply {
            fromSettingsKids = getBoolean("fromSettingsKids", false)
        }
    }

    override fun getLayoutToInflate(): Int {
        return R.layout.fragment_select_kid
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        if (fromSettingsKids) {

            //setting up toolbar and end text clickListener added kids ui
            setUpToolbar(getString(R.string.kids), true, getString(R.string.add_kid)) {
                requireActivity().supportFragmentManager.beginTransaction()
                    .replace(R.id.flCommonReplacement, AddKidInfoFragment().apply {
                        arguments = Bundle().apply {
                            putBoolean("fromSelectKids", true)
                        }
                    }).addToBackStack(null).commit()
            }

            //managing visibility of views
            tvAddedKids.visibility = View.VISIBLE
            ivInfo.visibility = View.GONE
            tvInfoSelectKids.visibility = View.GONE
            btnConfirmAndSelect.visibility = View.GONE

        } else {
            //setting up toolbar and clickListener
            setUpToolbar(getString(R.string.select_kid), true, getString(R.string.add_kid)) {
                requireActivity().supportFragmentManager.beginTransaction()
                    .replace(R.id.flCommonReplacement, AddKidInfoFragment().apply {
                        arguments = Bundle().apply {
                            putBoolean("fromSelectKids", true)
                        }
                    }).addToBackStack(null).commit()
            }

            //managing visibility of views
            btnConfirmAndSelect.visibility = View.VISIBLE
            tvAddedKids.visibility = View.GONE
            ivInfo.visibility = View.VISIBLE
            tvInfoSelectKids.visibility = View.VISIBLE
        }

        //layout manager for recycler view
        rvSelectKids.layoutManager = LinearLayoutManager(requireContext())

        showProgressBar(requireContext())
        //Api call get kids list
        val call = RetrofitInstance.get().create(ApiInterface::class.java).getKidsList()
        call.enqueue(object : Callback<GetKidsResponse> {
            override fun onResponse(
                call: Call<GetKidsResponse>,
                response: Response<GetKidsResponse>
            ) {
                response.apply {
                    if (isSuccessful) {

                        hideProgressBar()
                        kidsList = body()!!.kids as MutableList<Kid>

                        //managing adapters
                        if (fromSettingsKids) {
                            rvSelectKids.adapter = AddKidsInfoAdapter(
                                requireContext(),
                                body()!!.kids as MutableList<Kid>,
                                this@SelectKidsFragment
                            )
                        } else {
                            for (kid in kidsList){
                                if(kid.id in listIds){
                                    kid.is_selected = true
                                }
                            }
                            rvSelectKids.adapter = SelectKidsAdapter(
                                requireContext(),
                                kidsList,
                                this@SelectKidsFragment
                            )
                        }

                        // managing views based on number of kids
                        if(kidsList.size==0){
                            tvAddedKids.visibility = View.GONE
                            tvNoKids.visibility = View.VISIBLE
                        }else{
                            tvNoKids.visibility =  View.GONE
                            tvAddedKids.visibility = View.VISIBLE
                        }

                    } else {
                        hideProgressBar()
                        getErrorMessage(errorBody()!!, requireContext(), code())
                    }
                }
            }

            override fun onFailure(call: Call<GetKidsResponse>, t: Throwable) {
                hideProgressBar()
                t.message.toString().showToast(requireContext())
            }
        })

        btnConfirmAndSelect.setOnClickListener {
            if (listIds.size > 0) {
                requireActivity().let {
                    it.sendBroadcast(Intent(Constants.KEY_BROADCAST).apply {
                        putExtra(Constants.KEY_FROM_WHERE, Constants.KEY_SELECT_KIDS)
                        putExtra("ids", listIds)
                    })
                    it.finish()
                }
            } else {
                getString(R.string.select_one_kid_warning).showToast(requireContext())
            }
        }
    }

    override fun onSelect(id: Int) {
        listIds.add(id)
    }

    override fun onUnSelect(id: Int) {
        listIds.remove(id)
    }

    override fun onDeleteClick(position: Int) {

        showProgressBar(requireContext())
        if (isConnectedToInternet(requireContext())) {
            val service = RetrofitInstance.get().create(ApiInterface::class.java)
            val call = service.deleteKid(kidsList[position].id)

            call.enqueue(object : Callback<MessageResponse> {

                override fun onResponse(
                    call: Call<MessageResponse>,
                    response: Response<MessageResponse>
                ) {
                    hideProgressBar()
                    response.apply {
                        if (isSuccessful) {
                            kidsList.removeAt(position)
                            rvSelectKids.adapter?.notifyItemRemoved(position)
                            if(kidsList.size==0){
                                tvAddedKids.visibility = View.GONE
                                tvNoKids.visibility = View.VISIBLE
                            }else{
                                tvAddedKids.visibility = View.VISIBLE
                                tvNoKids.visibility = View.GONE
                            }
                        } else {
                            getErrorMessage(
                                errorBody()!!,
                                requireContext(),
                                code()
                            )
                        }
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

    override fun onEditClick(position: Int) {
        replaceFragment(R.id.flCommonReplacement, AddKidInfoFragment().apply {
            arguments = Bundle().apply {
                kidsList[position].let {
                    putBoolean("fromEditKid", true)
                    putInt("position", position)
                }
            }
        }, true)
    }

}