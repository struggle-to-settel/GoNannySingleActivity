package com.au.gonannysingleactivity.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.au.gonannysingleactivity.R
import com.au.gonannysingleactivity.webservices.Card

class CardsAdapter(
    private val context: Context,
    private val list: MutableList<Card>,
    private val listener: OnClickListener
) : RecyclerView.Adapter<CardsAdapter.ViewHolder>() {

    interface OnClickListener {
        fun onDelete(paymentId:String)
    }

    override fun getItemCount(): Int {
        return list.size
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(LayoutInflater.from(context).inflate(R.layout.item_card,parent,false))
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.apply {
            list[position].let {
                name.text = it.billing_details.name
                expiry.text = "${it.card.exp_month}/${it.card.exp_year}"
                lastFour.text = it.card.last4
            }
        }
    }
    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val lastFour: TextView = view.findViewById(R.id.tvCardNumberFour)
        val name: TextView = view.findViewById(R.id.tvCardHolderName)
        val expiry: TextView = view.findViewById(R.id.tvCardExpiry)
        private val delete: ImageView = view.findViewById(R.id.ivDelete)
        init {
            delete.setOnClickListener {
                listener.onDelete(list[absoluteAdapterPosition].id)
            }
        }
    }
}