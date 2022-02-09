package com.au.gonannysingleactivity.fragments

import android.annotation.SuppressLint
import android.app.Dialog
import android.os.Bundle
import android.view.View
import android.view.Window
import android.widget.Button
import android.widget.EditText
import androidx.appcompat.content.res.AppCompatResources.getDrawable
import com.au.gonannysingleactivity.R
import com.au.gonannysingleactivity.objects.Constants.BASE_IMAGE_URL
import com.au.gonannysingleactivity.utils.ApplicationGlobal
import com.au.gonannysingleactivity.utils.ApplicationGlobal.Companion.fromNotification
import com.au.gonannysingleactivity.utils.ApplicationGlobal.Companion.isChanged
import com.au.gonannysingleactivity.utils.CommonFunctions.Companion.get12HourFormattedTime
import com.au.gonannysingleactivity.utils.CommonFunctions.Companion.getErrorMessage
import com.au.gonannysingleactivity.utils.CommonFunctions.Companion.getMonthSpecifiedFormattedDate
import com.au.gonannysingleactivity.utils.CommonFunctions.Companion.hideProgressBar
import com.au.gonannysingleactivity.utils.CommonFunctions.Companion.setWidthCustomization
import com.au.gonannysingleactivity.utils.CommonFunctions.Companion.showProgressBar
import com.au.gonannysingleactivity.utils.Extensions.Companion.round
import com.au.gonannysingleactivity.utils.Extensions.Companion.showToast
import com.au.gonannysingleactivity.webservices.BookingDetailResponse
import com.au.gonannysingleactivity.webservices.MessageResponse
import com.au.gonannysingleactivity.webservices.api.ApiInterface
import com.au.gonannysingleactivity.webservices.api.RetrofitInstance
import com.bumptech.glide.Glide
import kotlinx.android.synthetic.main.fragment_show_booking_details.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class ShowBookingDetailFragment : BaseFragment(), View.OnClickListener {

    private var bookingId: Int? = null
    private var nannyId: Int? = null
    private var nannyProfileImage: String = ""
    private var nannyName: String = ""

    override fun getLayoutToInflate(): Int {
        return R.layout.fragment_show_booking_details
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // getting booking id to show data of that particular booking
        requireArguments().apply {
            bookingId = getInt("booking_id")
        }

       /* nannyProfileImage = ApplicationGlobal.mapNotification["from_user_image"].toString()
        nannyId = ApplicationGlobal.mapNotification["from_user_id"].toString().toInt()
        nannyName = ApplicationGlobal.mapNotification["from_user_first_name"].toString()
*/
    }

    @SuppressLint("SetTextI18n")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        //setting OnClick Listeners
        btnNannyProfile.setOnClickListener(this)
        ivBack.setOnClickListener(this)
        btnCancelBooking.setOnClickListener(this)
        btnRateNanny.setOnClickListener(this)
        btnChatNanny.setOnClickListener(this)

        // getting data
        showProgressBar(requireContext())
        RetrofitInstance.get().create(ApiInterface::class.java).getBookingDetails(bookingId!!)
            .enqueue(object : Callback<BookingDetailResponse> {

                override fun onResponse(
                    call: Call<BookingDetailResponse>,
                    response: Response<BookingDetailResponse>
                ) {
                    hideProgressBar()
                    if (response.isSuccessful) {
                        response.body()!!.apply {

                            // to manage visibilities of buttons
                            View.VISIBLE.let { visible ->
                                View.GONE.let { gone ->
                                    when (status) {
                                        1 -> {
                                            btnRateNanny.visibility = gone
                                            btnCancelBooking.visibility = visible
                                            btnNannyProfile.visibility = visible
                                            groupViewTimeLeft.visibility = gone
                                            groupViewCode.visibility = visible
                                            tvCode.text = checkout_verification_code.toString()
                                            btnChatNanny.visibility =
                                                if (can_chat == 1) visible else gone
                                            tvPending.text = getString(R.string.pending)
                                            tvPending.background = getDrawable(
                                                requireContext(),
                                                R.drawable.yellow_button_shape
                                            )
                                        }
                                        2 -> {
                                            btnRateNanny.visibility = visible
                                            btnCancelBooking.visibility = gone
                                            btnNannyProfile.visibility = visible
                                            btnChatNanny.visibility =
                                                if (can_chat == 1) visible else gone
                                            tvPending.text = getString(R.string.ongoing)
                                            tvPending.background = getDrawable(
                                                requireContext(),
                                                R.drawable.green_button_shape_right
                                            )
                                        }
                                        else -> {
                                            btnRateNanny.visibility = visible
                                            btnCancelBooking.visibility = gone
                                            btnNannyProfile.visibility = visible
                                            groupViewTimeLeft.visibility = visible
                                            groupViewCode.visibility = gone
                                            btnChatNanny.visibility = gone
                                            tvTimeLeft.text = total_service_time
                                            tvPending.text = getString(R.string.completed)
                                            tvPending.background = getDrawable(
                                                requireContext(),
                                                R.drawable.blue_button_shape_right
                                            )

                                        }
                                    }
                                }
                            }

                            nannyId = nanny_id
                            nannyProfileImage = this.nanny_profile_image
                            nannyName = "$nanny_first_name ${nanny_last_name ?: ""}"


                            //filling data in ui
                            tvNannyName.text = nannyName
                            tvNannyPrice.text = nanny_price.toFloat().round(2).toString()
                            ratingBar.rating = avg_rating.toFloat()
                            tvRatingCount.text = avg_rating.toFloat().toString()
                            tvValueAbout.text = nanny_about
                            tvValueNeededTimeDate.text =
                                getMonthSpecifiedFormattedDate(needed_date_time.substring(0..9))

                            val start = start_time.split(":")
                            val end = end_time.split(":")

                            tvValueDuration.text = "${
                                get12HourFormattedTime(
                                    start[0].toInt(),
                                    start[1].toInt()
                                )
                            } to ${get12HourFormattedTime(end[0].toInt(), end[1].toInt())}"

                            tvValueKids.text = no_of_kids.toString()
                            tvValueHouseNumber.text = apartment_no
                            tvValueAddress.text = address

                            Glide.with(requireContext()).load("$BASE_IMAGE_URL$nanny_profile_image")
                                .into(ivNanny)
                        }
                    } else {
                        getErrorMessage(response.errorBody()!!, requireContext(), response.code())
                    }
                }

                override fun onFailure(call: Call<BookingDetailResponse>, t: Throwable) {
                    hideProgressBar()
                    t.message!!.showToast(requireContext())
                }
            })

    }

    override fun onClick(v: View?) {
        when (v?.id) {

            R.id.ivBack -> requireActivity().onBackPressed()

            R.id.btnNannyProfile -> {
                replaceFragment(
                    R.id.flCommonReplacement,
                    NannyProfileFragment().apply {
                        arguments = Bundle().apply {
                            putInt("nanny_id", nannyId!!)
                        }
                    },
                    true
                )
            }

            R.id.btnCancelBooking -> cancelBooking()

            R.id.btnRateNanny -> replaceFragment(
                R.id.flCommonReplacement,
                RateToNannyFragment(),
                true
            )

            R.id.btnChatNanny -> replaceFragment(
                R.id.flCommonReplacement,
                ChatsFragment.newInstance(bookingId!!, nannyId!!, nannyProfileImage, nannyName),
                true
            )
        }
    }

    private fun cancelBooking() {
        val dialog = Dialog(requireContext())
        dialog.setCanceledOnTouchOutside(false)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        val view = layoutInflater.inflate(R.layout.dialog_cancel_booking, null)
        dialog.setContentView(view)
        val btnDiscard: Button = view.findViewById(R.id.btnDiscard)
        val btnYes: Button = view.findViewById(R.id.btnProceed)
        val etReason: EditText = view.findViewById(R.id.etReason)
        setWidthCustomization(dialog)
        btnDiscard.setOnClickListener {
            dialog.dismiss()
        }
        btnYes.setOnClickListener {
            showProgressBar(requireContext())
            if (etReason.text.toString().trim().isNotEmpty()) {

                RetrofitInstance.get().create(ApiInterface::class.java).cancelBooking(
                    hashMapOf(
                        "booking_id" to bookingId!!,
                        "cancellation_reason" to etReason.text.toString()
                    )
                ).enqueue(object : Callback<MessageResponse> {
                    override fun onResponse(
                        call: Call<MessageResponse>,
                        response: Response<MessageResponse>
                    ) {
                        response.apply {
                            if (isSuccessful) {
                                hideProgressBar()
                                isChanged = true
                                body()!!.message.showToast(requireContext())
                                dialog.dismiss()
                                requireActivity().onBackPressed()
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
                hideProgressBar()
                getString(R.string.warning_enter_resaon).showToast(requireContext())
            }
        }
        dialog.show()
    }

    companion object {
        fun newInstance(bookingId: Int) = ShowBookingDetailFragment().apply {
            arguments = Bundle().apply {
                putInt("booking_id", bookingId)
            }
        }
    }

}