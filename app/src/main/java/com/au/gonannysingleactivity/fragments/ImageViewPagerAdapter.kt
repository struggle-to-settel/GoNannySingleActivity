package com.au.gonannysingleactivity.fragments

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import com.au.gonannysingleactivity.R
import com.au.gonannysingleactivity.objects.Constants.BASE_IMAGE_URL
import com.au.gonannysingleactivity.webservices.NannyImage
import com.bumptech.glide.Glide

class ImageViewPagerAdapter(private val list: List<NannyImage>) :
    RecyclerView.Adapter<ImageViewPagerAdapter.ViewHolder>() {

    override fun getItemCount(): Int = list.count()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder = ViewHolder(
        LayoutInflater.from(parent.context).inflate(R.layout.fragment_image_sliding, parent, false)
    )

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        Glide.with(holder.imageView).load("$BASE_IMAGE_URL${list[position].image}").into(holder.imageView)
    }

    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val imageView = view.findViewById<ImageView>(R.id.imageViewPager)
    }

}
