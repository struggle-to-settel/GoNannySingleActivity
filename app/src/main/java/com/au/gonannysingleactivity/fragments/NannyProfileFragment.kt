package com.au.gonannysingleactivity.fragments

import android.os.Bundle
import android.view.View
import com.au.gonannysingleactivity.R
import com.au.gonannysingleactivity.adapters.NannyProfileAdapter
import com.au.gonannysingleactivity.objects.Constants.BASE_IMAGE_URL
import com.au.gonannysingleactivity.utils.CommonFunctions.Companion.getErrorMessage
import com.au.gonannysingleactivity.utils.CommonFunctions.Companion.hideProgressBar
import com.au.gonannysingleactivity.utils.CommonFunctions.Companion.showProgressBar
import com.au.gonannysingleactivity.utils.Extensions.Companion.showToast
import com.au.gonannysingleactivity.webservices.NannyAboutResponse
import com.au.gonannysingleactivity.webservices.NannyData
import com.au.gonannysingleactivity.webservices.api.ApiInterface
import com.au.gonannysingleactivity.webservices.api.RetrofitInstance
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.google.android.material.tabs.TabLayoutMediator
import kotlinx.android.synthetic.main.fragment_nanny_profile.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response


class NannyProfileFragment : BaseFragment() {

    private var nannyId: Int? = null


    override fun getLayoutToInflate(): Int {
        return R.layout.fragment_nanny_profile
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requireArguments().apply {
            nannyId = getInt("nanny_id")
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        ivBack.setOnClickListener {
            requireActivity().onBackPressed()
        }
        showProgressBar(requireContext())
        RetrofitInstance.get().create(ApiInterface::class.java).getNannyAbout(nannyId!!)
            .enqueue(object :
                Callback<NannyAboutResponse> {
                override fun onResponse(
                    call: Call<NannyAboutResponse>,
                    response: Response<NannyAboutResponse>
                ) {
                    response.apply {
                        if(isSuccessful){
                            nannyData = body()!!.nanny_data
                            nannyData!!.apply {
                                Glide.with(requireContext()).load("$BASE_IMAGE_URL${profile_image}")
                                    .apply(
                                        RequestOptions.centerCropTransform()
                                    ).into(ivProfilePic)

                                tvNannyName.text =
                                    if (last_name != null) "$first_name $last_name" else "$first_name"
                                ratingBar.rating = avg_rating.toFloat()
                                tvValueRating.text = avg_rating.toFloat().toString()
                            }

                            viewPager.adapter = NannyProfileAdapter(requireActivity())
                            TabLayoutMediator(tabLayout, viewPager) { tab, position ->
                                tab.text = when (position) {
                                    0 -> getString(R.string.about)
                                    else -> getString(R.string.photos)
                                }
                            }.attach()

                            hideProgressBar()
                        }else{
                            hideProgressBar()
                            getErrorMessage(errorBody()!!,requireContext(),code())
                        }
                    }
                }

                override fun onFailure(call: Call<NannyAboutResponse>, t: Throwable) {
                    hideProgressBar()
                    t.message!!.showToast(requireContext())
                }
            })
    }
}