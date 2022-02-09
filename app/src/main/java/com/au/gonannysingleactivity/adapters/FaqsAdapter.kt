package com.au.gonannysingleactivity.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.au.gonannysingleactivity.R
import com.au.gonannysingleactivity.webservices.Faq

class FaqsAdapter(private var list: MutableList<Faq>):RecyclerView.Adapter<FaqsAdapter.ViewHolder>() {



    inner class ViewHolder(view:View):RecyclerView.ViewHolder(view){
        val question:TextView = view.findViewById(R.id.tvValueQuestion)
        val answer:TextView = view.findViewById(R.id.tvValueAnswer)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.item_faqs,parent,false))
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.apply {
            list[position].let {
                question.text = it.question
                answer.text = it.answer
            }
        }
    }

    override fun getItemCount(): Int {
       return list.count()
    }

    fun setList(newList: MutableList<Faq>) {
        val startSize = list.size
        list = newList
        notifyItemRangeInserted(startSize,list.count())
    }
}