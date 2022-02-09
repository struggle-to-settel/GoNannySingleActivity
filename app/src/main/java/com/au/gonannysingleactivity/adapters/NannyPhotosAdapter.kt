package com.au.gonannysingleactivity.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.au.gonannysingleactivity.R
import com.au.gonannysingleactivity.objects.Constants.BASE_IMAGE_URL
import com.au.gonannysingleactivity.webservices.NannyPhoto
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions

class NannyPhotosAdapter(private val context: Context, private val list: MutableList<NannyPhoto>) :
    RecyclerView.Adapter<NannyPhotosAdapter.ViewHolder>() {

    override fun getItemCount(): Int {
        return list.size
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NannyPhotosAdapter.ViewHolder {
        return ViewHolder(
            LayoutInflater.from(context).inflate(R.layout.item_nanny_photos_card, parent, false)
        )
    }

    override fun onBindViewHolder(holder: NannyPhotosAdapter.ViewHolder, position: Int) {
        holder.apply {
            list[position].let {
                Glide.with(context).load("$BASE_IMAGE_URL${it.image}")
                    .apply(RequestOptions.centerCropTransform()).into(image)
                caption.text = it.image_caption
            }
        }
    }

    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val image: ImageView = view.findViewById(R.id.ivNanny)
        val caption: TextView = view.findViewById(R.id.tvCaption)
    }
}
