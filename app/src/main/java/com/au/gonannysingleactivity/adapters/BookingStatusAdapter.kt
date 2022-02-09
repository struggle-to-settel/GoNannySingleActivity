package com.au.gonannysingleactivity.adapters

import android.content.Context
import android.graphics.drawable.GradientDrawable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.RatingBar
import android.widget.TextView
import androidx.constraintlayout.widget.Group
import androidx.recyclerview.widget.RecyclerView
import com.au.gonannysingleactivity.R
import com.au.gonannysingleactivity.objects.Constants
import com.au.gonannysingleactivity.utils.CommonFunctions.Companion.getMonthSpecifiedFormattedDate
import com.au.gonannysingleactivity.utils.CommonFunctions.Companion.representTimeFormat
import com.au.gonannysingleactivity.utils.Extensions.Companion.round
import com.au.gonannysingleactivity.webservices.Booking
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.CenterCrop
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.bumptech.glide.request.RequestOptions

class BookingStatusAdapter(
    val context: Context,
    private val list: MutableList<Booking>,
    val listener: OnClickListener) :
    RecyclerView.Adapter<BookingStatusAdapter.ViewHolder>() {

    interface OnClickListener {
        fun onViewDetailClick(bookingId:Int)
    }

    override fun getItemCount(): Int {
        return list.size
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(
            LayoutInflater.from(context).inflate(R.layout.item_booking_status, parent, false)
        )
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.apply {
            list[position].let {

                when (it.status) {
                    1 -> {
                        statusBooking.text = context.getString(R.string.pending)
                        (statusBooking.background as GradientDrawable).setColor(context.getColor(R.color.yellowIconColor))
                        groupCompleted.visibility = View.VISIBLE
                    }
                    2 -> {
                        statusBooking.text = context.getString(R.string.ongoing)
                        (statusBooking.background as GradientDrawable).setColor(context.getColor(R.color.btnGreen))
                        groupCompleted.visibility = View.VISIBLE
                    }
                    else -> {
                        statusBooking.text = context.getString(R.string.completed)
                        (statusBooking.background as GradientDrawable).setColor(context.getColor(R.color.darkBlue))
                        groupCompleted.visibility = View.GONE
                    }
                }

                if (it.nanny_last_name != null) {
                    name.text = "${it.nanny_first_name} ${it.nanny_last_name}"
                } else {
                    name.text = it.nanny_first_name
                }
                Glide.with(context).load("${Constants.BASE_IMAGE_URL}${it.nanny_profile_image}")
                    .apply(
                        RequestOptions().placeholder(R.drawable.ic_profile_small)
                            .transforms(CenterCrop(),RoundedCorners(12))
                    ).into(image)

                price.text = "$${it.nanny_price.toFloat().round(2)}"
                dateTime.text = getMonthSpecifiedFormattedDate(representTimeFormat(it.needed_date_time))
                address.text = it.address
                duration.text = it.duration_name
                houseNumber.text = it.apartment_no
                ratingBar.rating = it.avg_rating.toFloat()
                ratingValue.text = it.avg_rating.toFloat().toString()
            }
        }
    }

    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val name: TextView = view.findViewById(R.id.tvNannyName)
        val image: ImageView = view.findViewById(R.id.ivNannyProfilePic)
        val price: TextView = view.findViewById(R.id.tvNannyPrice)
        val dateTime: TextView = view.findViewById(R.id.tvValueNeededDateTime)
        val duration: TextView = view.findViewById(R.id.tvValueDuration)
        val houseNumber:TextView = view.findViewById(R.id.tvValueHouseNumber)
        val address: TextView = view.findViewById(R.id.tvValueAddress)
        val statusBooking: TextView = view.findViewById(R.id.tvStatusBookingItem)
        val ratingBar: RatingBar = view.findViewById(R.id.ratingBar)
        val ratingValue: TextView = view.findViewById(R.id.tvRatingCount)
        val groupCompleted:Group = view.findViewById(R.id.groupCompleted)
        private val btnViewDetails: Button = view.findViewById(R.id.btnViewDetails)

        init {
            btnViewDetails.setOnClickListener {
                list[absoluteAdapterPosition].apply {
                    listener.onViewDetailClick(id)
                }
            }
        }
    }

}