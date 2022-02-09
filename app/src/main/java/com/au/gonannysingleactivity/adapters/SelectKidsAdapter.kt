package com.au.gonannysingleactivity.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.au.gonannysingleactivity.R
import com.au.gonannysingleactivity.webservices.Kid

class SelectKidsAdapter(
    private val context: Context,
    private val list: MutableList<Kid>,
    private val listener: OnSelectClickListener
) :
    RecyclerView.Adapter<SelectKidsAdapter.ViewHolder>() {

    interface OnSelectClickListener {
        fun onSelect(id: Int)
        fun onUnSelect(id:Int)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(
            LayoutInflater.from(context)
                .inflate(R.layout.item_select_kids, parent, false)
        )
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.apply {
            list[position].let {
                kidName.text = "${it.kid_first_name} ${it.kid_last_name}"
                kidBehavior.text = it.hourly_behaviour
                kidAllergies.text =
                    if (it.allergies == 1) context.getString(R.string.yes) else context.getString(R.string.no)
                kidIndoorCamera.text =
                    if (it.indoor_camera == 1) context.getString(R.string.yes) else context.getString(
                        R.string.no
                    )
                btnSelectKid.text = if(it.is_selected) "Selected" else "Select"
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
        val btnSelectKid: Button = view.findViewById(R.id.btnSelectKids)

        init {
            btnSelectKid.apply {
                setOnClickListener {
                    if (this.text.contentEquals("select", true)) {
                        this.text = "Selected"
                        listener.onSelect(list[absoluteAdapterPosition].id)
                    }
                    else{
                        this.text = "Select"
                        listener.onUnSelect(list[absoluteAdapterPosition].id)
                    }
                }
            }
        }
    }
}