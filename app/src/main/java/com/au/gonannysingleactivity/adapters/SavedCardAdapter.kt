package com.au.gonannysingleactivity.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.au.gonannysingleactivity.R
import com.au.gonannysingleactivity.webservices.Card

class SavedCardAdapter(private val list: MutableList<Card>,private val listener:OnClickListener):RecyclerView.Adapter<SavedCardAdapter.ViewHolder>() {

    interface OnClickListener{
        fun onCardClicked(id:String)
    }

    inner class ViewHolder(view:View):RecyclerView.ViewHolder(view){
        val cardNumber:TextView = view.findViewById(R.id.tvCardNumber)
        init {
            cardNumber.setOnClickListener {
                listener.onCardClicked(list[absoluteAdapterPosition].id)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.item_saved_card,parent,false))
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.cardNumber.text = "XXXX-XXXX-XXXX-${list[position].card.last4}"
    }

    override fun getItemCount(): Int {
        return list.count()
    }
}