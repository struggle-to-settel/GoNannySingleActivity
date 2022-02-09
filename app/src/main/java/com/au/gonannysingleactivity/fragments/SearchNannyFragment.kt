package com.au.gonannysingleactivity.fragments

import android.app.Dialog
import android.os.Bundle
import android.os.CountDownTimer
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.widget.ImageView
import android.widget.LinearLayout
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.widget.ViewPager2
import com.au.gonannysingleactivity.R
import com.au.gonannysingleactivity.objects.Constants.BASE_IMAGE_URL
import com.au.gonannysingleactivity.utils.CommonFunctions
import com.au.gonannysingleactivity.utils.CommonFunctions.Companion.getErrorMessage
import com.au.gonannysingleactivity.utils.CommonFunctions.Companion.hideProgressBar
import com.au.gonannysingleactivity.utils.CommonFunctions.Companion.showProgressBar
import com.au.gonannysingleactivity.utils.DotProgressBar
import com.au.gonannysingleactivity.utils.Extensions.Companion.showToast
import com.au.gonannysingleactivity.webservices.BookingDetailResponse
import com.au.gonannysingleactivity.webservices.NannyAboutResponse
import com.au.gonannysingleactivity.webservices.NannyPhoto
import com.au.gonannysingleactivity.webservices.api.RetrofitInstance
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import kotlinx.android.synthetic.main.basic_alert_dialog_layout.view.*
import kotlinx.android.synthetic.main.fragment_search_nanny.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class SearchNannyFragment : BaseFragment() {

    private var listImages: MutableList<NannyPhoto> = mutableListOf()
    private lateinit var llIndicators: LinearLayout
    private var isApproved: Boolean = false

    override fun getLayoutToInflate(): Int {
        return R.layout.fragment_search_nanny
    }

    override fun modifyBackIcon(): Boolean = true

    override fun modifyBackIconFunctionality() {
        cancelRequest()
    }

    override fun registerFirebaseBroadcastReceiver(): Boolean = true

    override fun onFirebaseMessageReceived(data: Bundle) {
        if (data.get("notif_type") == "2") {
            llSearchRequest.visibility = View.GONE
            rlNannyRequestApproved.visibility = View.VISIBLE
            showProgressBar(requireContext())
            RetrofitInstance.create().getBookingDetails(data.get("booking_id").toString().toInt())
                .enqueue(object : Callback<BookingDetailResponse> {
                    override fun onFailure(call: Call<BookingDetailResponse>, t: Throwable) {
                        hideProgressBar()
                        t.message!!.showToast(requireContext())
                    }

                    override fun onResponse(
                        call: Call<BookingDetailResponse>,
                        response: Response<BookingDetailResponse>
                    ) {
                        response.apply {
                            if (isSuccessful) {
                                RetrofitInstance.create().getNannyAbout(body()!!.nanny_id)
                                    .enqueue(object : Callback<NannyAboutResponse> {
                                        override fun onResponse(
                                            call: Call<NannyAboutResponse>,
                                            response: Response<NannyAboutResponse>
                                        ) {
                                            response.apply {
                                                if (isSuccessful) {
                                                    isApproved = true
                                                    hideProgressBar()
                                                    body()!!.nanny_data.apply {
                                                        listImages =
                                                            nanny_photos as MutableList<NannyPhoto>

                                                        tvName.text =
                                                            "$first_name ${last_name ?: ""}"
                                                        ratingBar.rating = avg_rating.toFloat()
                                                        tvRatingValue.text =
                                                            avg_rating.toFloat().toString()
                                                        Glide.with(requireContext())
                                                            .load("$BASE_IMAGE_URL$profile_image")
                                                            .apply(
                                                                RequestOptions.centerCropTransform()
                                                            ).into(ivProfile)
                                                        imageViewPager.apply {
                                                            adapter = ImageAdapterNanny()
                                                            registerOnPageChangeCallback(
                                                                onPageChangeCallback
                                                            )
                                                        }
                                                        timer.start()
                                                    }
                                                } else {
                                                    hideProgressBar()
                                                    getErrorMessage(
                                                        errorBody()!!,
                                                        requireContext(),
                                                        code()
                                                    )
                                                }
                                            }
                                        }

                                        override fun onFailure(
                                            call: Call<NannyAboutResponse>,
                                            t: Throwable
                                        ) {
                                            hideProgressBar()
                                            t.message!!.showToast(requireContext())
                                        }
                                    })
                            } else {
                                hideProgressBar()
                                getErrorMessage(errorBody()!!, requireContext(), code())
                            }
                        }
                    }
                })
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setUpToolbar(getString(R.string.search_nanny), true, null) {}
        llIndicators = view.findViewById(R.id.llIndicator)
        timer.start()
        val progressBar = DotProgressBar.Builder().build(requireContext())
        progressBar.startAnimation()
        fl.addView(progressBar)
        btnCancelRequest.setOnClickListener {
            cancelRequest()
        }

        btnRequestAgain.setOnClickListener {
            fl.visibility = View.VISIBLE
            btnRequestAgain.visibility = View.GONE
            timer.start()
        }
    }

    private val timer = object : CountDownTimer(60000, 1000) {

        override fun onFinish() {
            if(isApproved){
                btnApprove.text = getString(R.string.approved)
            }else {
                fl.visibility = View.GONE
                btnRequestAgain.visibility = View.VISIBLE
            }
        }

        override fun onTick(millisUntilFinished: Long) {
            if (isApproved) {
                (millisUntilFinished / 1000).let {
                    if (it in 0..9)
                        btnApprove.text = "Approve (00:0$it)"
                    else
                        btnApprove.text = "Approve (00:$it)"
                }
            }
        }

    }

    private fun cancelRequest() {
        val dialog = Dialog(requireContext())
        val view =
            LayoutInflater.from(requireContext()).inflate(R.layout.basic_alert_dialog_layout, null)
        view.apply {
            tvTitle.text = getString(R.string.cancel_request)
            tvMessage.text = getString(R.string.cancel_request_message)
            btnNo.setOnClickListener {
                dialog.dismiss()
            }
            btnYes.setOnClickListener {
                clearStack()
                requireActivity().onBackPressed()
            }
        }
        CommonFunctions.setWidthCustomization(dialog)
        dialog.apply {
            requestWindowFeature(Window.FEATURE_NO_TITLE)
            setCancelable(false)
            setCanceledOnTouchOutside(true)
            setContentView(view)
            show()
        }
    }

    inner class ImageAdapterNanny :
        RecyclerView.Adapter<ImageAdapterNanny.ViewHolder>() {

        override fun getItemCount(): Int = listImages.count()

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            Glide.with(holder.image).load("$BASE_IMAGE_URL${listImages[position].image}").apply(
                RequestOptions.centerCropTransform()
            ).into(holder.image)
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder = ViewHolder(
            LayoutInflater.from(parent.context).inflate(R.layout.item_image_view, parent, false)
        )

        inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
            val image: ImageView = view.findViewById(R.id.imageView)
        }

    }

    private val onPageChangeCallback = object : ViewPager2.OnPageChangeCallback() {

        override fun onPageSelected(position: Int) {
            super.onPageSelected(position)
            showIndicators(listImages.count(), position)
        }
    }

    private fun showIndicators(size: Int, position: Int) {
        for (i in 0 until size) {
            val params = LinearLayout.LayoutParams(24, 24)
            val imageView = ImageView(requireContext())
            imageView.layoutParams = params
            if (i == position)
                imageView.setBackgroundResource(R.drawable.indicator_selected)
            else imageView.setBackgroundResource(R.drawable.indicator_unselected)
            llIndicators.addView(imageView)
        }

    }
}