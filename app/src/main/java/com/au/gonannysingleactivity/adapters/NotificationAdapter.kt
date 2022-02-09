package com.au.gonannysingleactivity.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.au.gonannysingleactivity.R
import com.au.gonannysingleactivity.objects.Constants.BASE_IMAGE_URL
import com.au.gonannysingleactivity.webservices.Notification
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions

class NotificationAdapter :
    RecyclerView.Adapter<NotificationAdapter.ViewHolder>() {

    var list: MutableList<Notification> = mutableListOf()

    fun updateList(itemsToAdd: List<Notification>) {
        val start = list.count()
        list.addAll(itemsToAdd)
        notifyItemRangeInserted(start, list.count())
    }

    override fun getItemCount(): Int {
        return list.size
    }

    override fun getItemViewType(position: Int): Int {
        return list[position].is_read
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(
            LayoutInflater.from(parent.context).inflate(R.layout.item_notification, parent, false)
        )
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.apply {
            list[position].let {

                viewUnseen.visibility = if (it.is_read == 0) View.VISIBLE else View.GONE

                Glide.with(image).load("$BASE_IMAGE_URL${it.sender_profile_image}").apply(
                    RequestOptions.circleCropTransform().placeholder(R.mipmap.ic_app_logo_round)
                ).into(image)

                notificationTitle.text = it.title
                notificationMessage.text = it.message
                arrivalTime.text = it.time_since

            }
        }
    }

    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val image: ImageView = view.findViewById(R.id.ivNotificationSender)
        val notificationTitle: TextView = view.findViewById(R.id.tvNotificationTitle)
        val notificationMessage: TextView = view.findViewById(R.id.tvNotificationMessage)
        val arrivalTime: TextView = view.findViewById(R.id.tvNotificationArrivalTime)
        val viewUnseen: ImageView = view.findViewById(R.id.ivUnseen)
    }

}