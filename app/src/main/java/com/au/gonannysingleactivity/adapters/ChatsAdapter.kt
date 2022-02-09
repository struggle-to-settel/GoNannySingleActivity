package com.au.gonannysingleactivity.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.au.gonannysingleactivity.R
import com.au.gonannysingleactivity.utils.CommonFunctions.Companion.get12HourFormattedTime
import com.au.gonannysingleactivity.webservices.Messages

class ChatsAdapter(private val list: MutableList<Messages>) :
    RecyclerView.Adapter<RecyclerView.ViewHolder>() {


    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        holder.apply {
            list[position].let {

                if (it.from_me == 1)
                    (this as SendViewHolder).apply {
                        sentText.text = it.message
                        if (it.created_at.length > 8) {
                            it.created_at.split("T")[1].apply {
                                timeSent.text = get12HourFormattedTime(
                                    substring(0, 2).toInt(),
                                    substring(3, 5).toInt()
                                )
                            }
                        } else {
                            timeSent.text = get12HourFormattedTime(
                                it.created_at.substring(0, 2).toInt(),
                                it.created_at.substring(3, 5).toInt()
                            )
                        }
                    }
                else
                    (this as ReceiveViewHolder).apply {
                        receivedText.text = it.message
                        it.created_at.split("T")[1].apply {
                            timeReceived.text = get12HourFormattedTime(
                                substring(0, 2).toInt(),
                                substring(3, 5).toInt()
                            )
                        }
                    }
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when (viewType) {
            0 -> ReceiveViewHolder(
                LayoutInflater.from(parent.context)
                    .inflate(R.layout.item_received_message, parent, false)
            )
            else -> SendViewHolder(
                LayoutInflater.from(parent.context)
                    .inflate(R.layout.item_sent_message, parent, false)
            )
        }
    }

    override fun getItemCount(): Int = list.count()

    override fun getItemViewType(position: Int): Int = list[position].from_me

    inner class SendViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val sentText: TextView = view.findViewById(R.id.tvSend)
        val timeSent: TextView = view.findViewById(R.id.tvTimeSent)
    }

    inner class ReceiveViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val receivedText: TextView = view.findViewById(R.id.tvReceive)
        val timeReceived: TextView = view.findViewById(R.id.tvTimeReceived)
    }


}