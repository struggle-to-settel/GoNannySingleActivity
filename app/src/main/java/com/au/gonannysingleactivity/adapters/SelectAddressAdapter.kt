package com.au.gonannysingleactivity.adapters

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.au.gonannysingleactivity.R
import com.au.gonannysingleactivity.webservices.Adddresse

class SelectAddressAdapter(
    private var list: MutableList<Adddresse>,
    private val listener: OnSelectClickListener,
    private val isFromSetting:Boolean
) : RecyclerView.Adapter<SelectAddressAdapter.ViewHolder>() {


    interface OnSelectClickListener {
        fun onSelectClick(address: Adddresse)
        fun onEditClick(address: Adddresse)
    }

    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {

        val houseNumber: TextView = view.findViewById(R.id.tvValueHouseNo)
        val address: TextView = view.findViewById(R.id.tvValueAddress)
        val landmark: TextView = view.findViewById(R.id.tvValueLandmark)
        private val btnSelectAddress: Button = view.findViewById(R.id.btnSelectAddress)
        private val btnEditAddress:Button = view.findViewById(R.id.btnEditAddress)

        init {
            btnSelectAddress.setOnClickListener {
                listener.onSelectClick(list[absoluteAdapterPosition])
            }
            btnEditAddress.setOnClickListener{
                listener.onEditClick(list[absoluteAdapterPosition])
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view:View = LayoutInflater.from(parent.context).inflate(R.layout.item_saved_address, parent, false)
        if(isFromSetting){
            view.findViewById<Button>(R.id.btnSelectAddress).visibility = View.GONE
            view.findViewById<Button>(R.id.btnEditAddress).visibility = View.VISIBLE
        }else{
            view.findViewById<Button>(R.id.btnEditAddress).visibility = View.GONE
            view.findViewById<Button>(R.id.btnSelectAddress).visibility = View.VISIBLE
        }
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.let {
            list[position].apply {
                it.houseNumber.text = this.apartment_no
                it.address.text = this.address as CharSequence?
                it.landmark.text = this.landmark as CharSequence?
            }
        }
    }

    override fun getItemCount(): Int {
        return list.size
    }

    fun setList(newList: MutableList<Adddresse>){
        val count = list.count()
        list = newList
       notifyItemRangeInserted(count,list.count())
    }
}