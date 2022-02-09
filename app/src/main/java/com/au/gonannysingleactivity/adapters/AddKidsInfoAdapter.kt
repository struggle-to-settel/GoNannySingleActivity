package com.au.gonannysingleactivity.adapters

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.au.gonannysingleactivity.R
import com.au.gonannysingleactivity.webservices.Kid

class AddKidsInfoAdapter(private val context: Context, private var list: MutableList<Kid>,private var listener:OnItemClickListener) :
    RecyclerView.Adapter<AddKidsInfoAdapter.ViewHolder>() {

    interface OnItemClickListener {
        fun onDeleteClick(position:Int)
        fun onEditClick(position:Int)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(
            LayoutInflater.from(context)
                .inflate(R.layout.item_added_kids, parent, false)
        )
    }

    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.apply {
            list[position].let {
                kidName.text = "${it.kid_first_name} ${it.kid_last_name}"
                kidBehavior.text = it.hourly_behaviour
                kidAllergies.text = if (it.allergies == 1)
                    context.getString(R.string.yes)
                else
                    context.getString(R.string.no)
                kidIndoorCamera.text = if(it.indoor_camera==1)
                    context.getString(R.string.yes)
                else
                    context.getString(R.string.no)
            }
        }
    }

    override fun getItemCount(): Int {
        return list.size
    }

    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {

        val kidName: TextView = view.findViewById(R.id.tvValueName)
        val kidBehavior: TextView = view.findViewById(R.id.tvValueKids)
        val kidAllergies: TextView = view.findViewById(R.id.tvValueAllergies)
        val kidIndoorCamera: TextView = view.findViewById(R.id.tvValueHouseNumber)

        private val btnEditInfo: Button = view.findViewById(R.id.btnEditKidInfo)
        private val btnDeleteKidInfo: Button = view.findViewById(R.id.btnDeleteKidInfo)

        init {
            btnDeleteKidInfo.setOnClickListener {
                listener.onDeleteClick(absoluteAdapterPosition)
            }
            btnEditInfo.setOnClickListener {
                listener.onEditClick(absoluteAdapterPosition)
            }
        }
    }

}