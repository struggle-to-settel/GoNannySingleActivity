package com.au.gonannysingleactivity.fragments

import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.au.gonannysingleactivity.R
import com.au.gonannysingleactivity.adapters.NannyPhotosAdapter
import com.au.gonannysingleactivity.objects.Constants.BASE_IMAGE_URL
import com.au.gonannysingleactivity.utils.CommonFunctions
import com.au.gonannysingleactivity.utils.CommonFunctions.Companion.getErrorMessage
import com.au.gonannysingleactivity.utils.CommonFunctions.Companion.hideProgressBar
import com.au.gonannysingleactivity.utils.CommonFunctions.Companion.showProgressBar
import com.au.gonannysingleactivity.utils.Extensions.Companion.showToast
import com.au.gonannysingleactivity.webservices.GetNannyImagesResponse
import com.au.gonannysingleactivity.webservices.MessageResponse
import com.au.gonannysingleactivity.webservices.NannyImage
import com.au.gonannysingleactivity.webservices.NannyPhoto
import com.au.gonannysingleactivity.webservices.api.RetrofitInstance
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import kotlinx.android.synthetic.main.basic_alert_dialog_layout.view.*
import kotlinx.android.synthetic.main.fragment_nanny_photos.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class NannyPhotosFragment : BaseFragment() {

    private var fromSetting: Boolean = false
    override fun getLayoutToInflate(): Int {
        return R.layout.fragment_nanny_photos
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            fromSetting = it.getBoolean("fromSetting", false)
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        if (fromSetting) {
            includedToolbar.visibility = View.VISIBLE
            tvPhotos.visibility = View.GONE

            setUpToolbar(getString(R.string.photos), true, getString(R.string.add)) {
                val fragment = AddPhotosFragment().apply {
                    arguments = Bundle().apply {
                        putBoolean("toAddImage", true)
                    }
                }
                replaceFragment(R.id.flCommonReplacement, fragment, true)
            }

            showProgressBar(requireContext())
            RetrofitInstance.create().getNannyImages()
                .enqueue(object : Callback<GetNannyImagesResponse> {
                    override fun onFailure(call: Call<GetNannyImagesResponse>, t: Throwable) {
                        hideProgressBar()
                        t.message!!.showToast(requireContext())
                    }

                    override fun onResponse(
                        call: Call<GetNannyImagesResponse>,
                        response: Response<GetNannyImagesResponse>
                    ) {
                        response.apply {
                            if (isSuccessful) {
                                rvNannyPhotos.layoutManager = LinearLayoutManager(requireContext())
                                rvNannyPhotos.adapter =
                                    ImagesAdapter(body()!!.nanny_images as MutableList<NannyImage>)
                                hideProgressBar()
                            } else {
                                hideProgressBar()
                                getErrorMessage(errorBody()!!, requireContext(), code())
                            }

                        }
                    }
                })
        } else {
            includedToolbar.visibility = View.GONE
            tvPhotos.visibility = View.VISIBLE
            nannyData!!.apply {
                rvNannyPhotos.layoutManager = LinearLayoutManager(requireContext())
                rvNannyPhotos.adapter =
                    NannyPhotosAdapter(requireContext(), nanny_photos as MutableList<NannyPhoto>)
            }
        }
    }

    inner class ImagesAdapter(val list: MutableList<NannyImage>) :

        RecyclerView.Adapter<ImagesAdapter.ViewHolder>() {

        override fun getItemCount(): Int = list.count()
        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder = ViewHolder(
            LayoutInflater.from(parent.context).inflate(R.layout.item_nanny_image, parent, false)
        )

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            holder.apply {
                list[position].let {
                    Glide.with(image).load("$BASE_IMAGE_URL${it.image}")
                        .apply(RequestOptions.centerCropTransform()).into(image)
                    imageCaption.text = it.image_caption
                }
            }
        }

        inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
            val image: ImageView = view.findViewById(R.id.ivItemUploaded)
            val imageCaption: TextView = view.findViewById(R.id.tvItemCaption)
            private val edit: ImageView = view.findViewById(R.id.ivEdit)
            private val delete: ImageView = view.findViewById(R.id.ivDelete)

            init {

                edit.setOnClickListener {
                    replaceFragment(R.id.flCommonReplacement, AddPhotosFragment().apply {
                        arguments = Bundle().apply {
                            list[absoluteAdapterPosition].let { item ->
                                putBoolean("toEditImage", true)
                                putInt("image_id", item.id)
                                putString("image_url", item.image)
                                putString("image_caption", item.image_caption)
                            }
                        }
                    }, true)
                }
                delete.setOnClickListener {
                    val dialog = Dialog(requireContext())
                    LayoutInflater.from(requireContext())
                        .inflate(R.layout.basic_alert_dialog_layout, null).apply {
                            tvTitle.text = getString(R.string.delete_photo)
                            tvMessage.text = getString(R.string.mesage_delete_photo)
                            btnNo.setOnClickListener { dialog.dismiss() }
                            btnYes.setOnClickListener {
                                dialog.dismiss()
                                if (CommonFunctions.isConnectedToInternet(requireContext())) {
                                    showProgressBar(requireContext())
                                    RetrofitInstance.create()
                                        .deleteNannyImage(id)
                                        .enqueue(object : Callback<MessageResponse> {

                                            override fun onFailure(
                                                call: Call<MessageResponse>,
                                                t: Throwable
                                            ) {
                                                hideProgressBar()
                                                t.message!!.showToast(requireContext())
                                            }

                                            override fun onResponse(
                                                call: Call<MessageResponse>,
                                                response: Response<MessageResponse>
                                            ) {

                                                response.apply {
                                                    if (isSuccessful) {
                                                        list.removeAt(absoluteAdapterPosition)
                                                        rvNannyPhotos.adapter?.notifyItemRemoved(
                                                            absoluteAdapterPosition
                                                        )
                                                    } else {
                                                        hideProgressBar()
                                                        getErrorMessage(
                                                            errorBody()!!,
                                                            requireContext(),
                                                            code()
                                                        )
                                                    }
                                                }
                                            }
                                        })
                                } else {
                                    getString(R.string.check_internt).showToast(requireContext())
                                }
                            }

                            dialog.apply {
                                requestWindowFeature(Window.FEATURE_NO_TITLE)
                                setCancelable(false)
                                setCanceledOnTouchOutside(false)
                                CommonFunctions.setWidthCustomization(this)
                                setContentView(view)
                                show()
                            }
                        }

                }
            }
        }
    }

}