package com.au.gonannysingleactivity.fragments

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.recyclerview.widget.LinearLayoutManager
import com.au.gonannysingleactivity.R
import com.au.gonannysingleactivity.adapters.SelectAddressAdapter
import com.au.gonannysingleactivity.objects.Constants.KEY_BROADCAST
import com.au.gonannysingleactivity.objects.Constants.KEY_EDIT_ADDRESS
import com.au.gonannysingleactivity.objects.Constants.KEY_FROM_WHERE
import com.au.gonannysingleactivity.objects.Constants.KEY_SELECT_ADDRESS
import com.au.gonannysingleactivity.utils.CommonFunctions.Companion.getErrorMessage
import com.au.gonannysingleactivity.utils.CommonFunctions.Companion.hideProgressBar
import com.au.gonannysingleactivity.utils.CommonFunctions.Companion.showProgressBar
import com.au.gonannysingleactivity.utils.Extensions.Companion.showToast
import com.au.gonannysingleactivity.webservices.Adddresse
import com.au.gonannysingleactivity.webservices.GetAddressesResponse
import com.au.gonannysingleactivity.webservices.api.ApiInterface
import com.au.gonannysingleactivity.webservices.api.RetrofitInstance
import kotlinx.android.synthetic.main.fragment_setting_addresses.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class AddressesFragment : BaseFragment(), SelectAddressAdapter.OnSelectClickListener {

    private var fromSettingFragment: Boolean = false

    private val bookingAddresses = mutableListOf<Adddresse>()
    private val mailingAddresses = mutableListOf<Adddresse>()


    companion object{
        fun fromSettingFragment():AddressesFragment = AddressesFragment().apply {
            arguments = Bundle().apply {
                putBoolean("fromSettingFragment",true)
            }
        }
    }
    override fun getLayoutToInflate(): Int {
        return R.layout.fragment_setting_addresses
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.apply {
            fromSettingFragment = getBoolean("fromSettingFragment", false)
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        bookingAddresses.clear()
        mailingAddresses.clear()

        setUpToolbar("Addresses", true, null) {}

        rvBookingAddress.layoutManager = LinearLayoutManager(requireContext())
        rvMailingAddress.layoutManager = LinearLayoutManager(requireContext())

        if (fromSettingFragment) {
            tvAddedAddress.visibility = View.GONE
            tvBookingAddress.visibility = View.VISIBLE
            btnSelectAnotherAddress.visibility = View.GONE
            rvMailingAddress.visibility = View.VISIBLE
        }

        showProgressBar(requireContext())
        val call = RetrofitInstance.get().create(ApiInterface::class.java).getAddresses()
        call.enqueue(object : Callback<GetAddressesResponse> {
            override fun onResponse(
                call: Call<GetAddressesResponse>,
                response: Response<GetAddressesResponse>
            ) {
                response.apply {

                    if (isSuccessful) {
                        // populate list for each email type
                        for (item in body()!!.adddresses) if (item.address_type != 1) bookingAddresses.add(
                            item
                        ) else mailingAddresses.add(item)


                        if (fromSettingFragment) {

                            rvBookingAddress.adapter = SelectAddressAdapter(
                                bookingAddresses,
                                this@AddressesFragment,
                                fromSettingFragment
                            )

                            if (mailingAddresses.count() > 0) {

                                rvMailingAddress.adapter = SelectAddressAdapter(
                                    mailingAddresses,
                                    this@AddressesFragment,
                                    fromSettingFragment
                                )
                                tvMailingAddress.visibility = View.VISIBLE
                                hideProgressBar()
                            } else {
                                rvMailingAddress.visibility = View.GONE
                                tvMailingAddress.visibility = View.GONE
                                hideProgressBar()
                            }

                        } else {
                            rvMailingAddress.visibility = View.GONE
                            tvMailingAddress.visibility = View.GONE
                            rvBookingAddress.adapter = SelectAddressAdapter(
                                bookingAddresses,
                                this@AddressesFragment,
                                fromSettingFragment
                            )
                            hideProgressBar()
                        }

                    } else  //isSuccessful response
                    {
                        hideProgressBar()
                        getErrorMessage(errorBody()!!, requireContext(), code())
                    }

                }
            }

            override fun onFailure(call: Call<GetAddressesResponse>, t: Throwable) {
                hideProgressBar()
                t.message.toString().showToast(requireContext())
            }
        })

        btnSelectAnotherAddress.setOnClickListener {
            replaceFragment(R.id.flCommonReplacement, AddAddressFragment().apply {
                arguments = Bundle().apply {
                    putBoolean(KEY_SELECT_ADDRESS, true)
                }
            }, false)
        }
    }

    override fun onSelectClick(address: Adddresse) {

        address.let {
            requireActivity().sendBroadcast(Intent(KEY_BROADCAST).apply {
                putExtra(KEY_FROM_WHERE, KEY_SELECT_ADDRESS)
                putExtra("address", it.address.toString())
                putExtra("apartment_no", it.apartment_no)
                putExtra("landmark", it.landmark.toString())
                putExtra("city", it.city)
                putExtra("lat", it.lat.toString())
                putExtra("lng", it.lng.toString())
            })
        }
        requireActivity().onBackPressed()

    }

    override fun onEditClick(address: Adddresse) {
        replaceFragment(R.id.flCommonReplacement, AddAddressFragment().apply {
            arguments = Bundle().apply {
                putBoolean(KEY_EDIT_ADDRESS, true)
                putString("apartment_no", address.apartment_no)
                putString("landmark", address.landmark.toString())
                putDouble("lat", address.lat)
                putDouble("lng", address.lng)
                putInt("address_id", address.id)
            }
        }, true)
    }

}