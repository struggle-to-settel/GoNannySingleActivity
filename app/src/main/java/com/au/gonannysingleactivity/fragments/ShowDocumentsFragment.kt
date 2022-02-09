package com.au.gonannysingleactivity.fragments

import android.app.Dialog
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.ImageDecoder
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.widget.ImageView
import android.widget.TextView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.au.gonannysingleactivity.R
import com.au.gonannysingleactivity.activities.MainActivity.Companion.documents
import com.au.gonannysingleactivity.objects.Constants
import com.au.gonannysingleactivity.utils.CommonFunctions
import com.au.gonannysingleactivity.utils.CommonFunctions.Companion.getErrorMessage
import com.au.gonannysingleactivity.utils.CommonFunctions.Companion.hideProgressBar
import com.au.gonannysingleactivity.utils.CommonFunctions.Companion.setWidthCustomization
import com.au.gonannysingleactivity.utils.CommonFunctions.Companion.showProgressBar
import com.au.gonannysingleactivity.utils.Extensions.Companion.showToast
import com.au.gonannysingleactivity.webservices.Document
import com.au.gonannysingleactivity.webservices.GetDocumentsResponse
import com.au.gonannysingleactivity.webservices.MessageResponse
import com.au.gonannysingleactivity.webservices.UploadImageResponse
import com.au.gonannysingleactivity.webservices.api.ApiInterface
import com.au.gonannysingleactivity.webservices.api.RetrofitInstance
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import kotlinx.android.synthetic.main.basic_alert_dialog_layout.view.*
import kotlinx.android.synthetic.main.basic_alert_dialog_layout.view.tvTitle
import kotlinx.android.synthetic.main.dialog_add_photo_layout.view.*
import kotlinx.android.synthetic.main.fragment_show_documents.*
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.File

class ShowDocumentsFragment : BaseFragment() {

    private var list: MutableList<Document> = mutableListOf()
    private var document: Document? = null
    private var imageUrl: String = ""
    private var imageAdapter: ImagesAdapter = ImagesAdapter()

    override fun getLayoutToInflate(): Int = R.layout.fragment_show_documents

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setUpToolbar(getString(R.string.documents), true, null) {}
        rvShowDocuments.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = imageAdapter
        }
        showProgressBar(requireContext())
        getDocuments()
    }

    private fun getDocuments() {

        documents.clear()
        list.clear()

        RetrofitInstance.create().getDocuments().enqueue(object : Callback<GetDocumentsResponse> {
            override fun onResponse(
                call: Call<GetDocumentsResponse>,
                response: Response<GetDocumentsResponse>
            ) {
                response.apply {
                    if (isSuccessful) {

                        val startSize = list.count()
                        for (document in body()!!.documents)
                            if (document.doc_type == 2) documents.add(document)
                            else list.add(document)
                        list.add(0, documents.last())
                        imageAdapter.notifyItemRangeInserted(startSize, list.count())
                        hideProgressBar()

                    } else {
                        hideProgressBar()
                        getErrorMessage(errorBody()!!, requireContext(), code())
                    }
                }
            }

            override fun onFailure(call: Call<GetDocumentsResponse>, t: Throwable) {
                hideProgressBar()
                t.message!!.showToast(requireContext())
            }

        })
    }

    inner class ImagesAdapter : RecyclerView.Adapter<ImagesAdapter.ViewHolder>() {

        override fun getItemCount(): Int {
            return list.count()
        }


        @RequiresApi(Build.VERSION_CODES.P)
        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            LayoutInflater.from(parent.context)
                .inflate(R.layout.item_nanny_image, parent, false).apply {
                    return ViewHolder(this)
                }
        }

        @RequiresApi(Build.VERSION_CODES.P)
        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            holder.apply {
                list[position].let {
                    if (it.doc_type == 2) {
                        delete.visibility = View.GONE
                    }
                    Glide.with(image).load("${Constants.BASE_IMAGE_URL}${it.doc_image}").apply(
                        RequestOptions.centerCropTransform()
                    ).into(image)
                    imageCaption.text = it.doc_name
                }
            }
        }

        @RequiresApi(Build.VERSION_CODES.P)
        inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
            val image: ImageView = view.findViewById(R.id.ivItemUploaded)
            val imageCaption: TextView = view.findViewById(R.id.tvItemCaption)
            private val edit: ImageView = view.findViewById(R.id.ivEdit)
            val delete: ImageView = view.findViewById(R.id.ivDelete)

            init {

                edit.setOnClickListener {
                    val position = absoluteAdapterPosition
                    if (list[position].doc_type == 2) {
                        replaceFragment(R.id.flCommonReplacement, AddWWCCFragment().apply {
                            arguments = Bundle().apply {
                                putBoolean("toEditWWCCFromSetting", true)
                            }
                        }, true)
                    } else {
                        document = list[position]
                        //upload image from dialog
                        Dialog(requireContext()).apply {
                            LayoutInflater.from(requireContext())
                                .inflate(R.layout.dialog_add_photo_layout, null).let {

                                    it.tvTakePhoto.setOnClickListener {
                                        val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
                                        dismiss()
                                        takePhotoContract.launch(intent)
                                    }

                                    it.tvChooseFromGallery.setOnClickListener {
                                        val intent = Intent()
                                        intent.type = "image/*"
                                        intent.action = Intent.ACTION_PICK
                                        dismiss()
                                        chooseFromGalleryContract.launch(
                                            Intent.createChooser(
                                                intent,
                                                "Select An option"
                                            )
                                        )
                                    }

                                    it.tvCancel.setOnClickListener {
                                        dismiss()
                                    }
                                    setCancelable(false)
                                    setCanceledOnTouchOutside(false)
                                    requestWindowFeature(Window.FEATURE_NO_TITLE)
                                    setContentView(it)
                                    show()
                                }

                        }
                    }
                }

                delete.setOnClickListener {
                    Dialog(requireContext()).apply {
                        LayoutInflater.from(requireContext())
                            .inflate(R.layout.basic_alert_dialog_layout, null).let {
                                it.tvTitle.text = getString(R.string.delete_document)
                                it.tvMessage.text = getString(R.string.message_delete_document)
                                it.btnNo.setOnClickListener {
                                    this.dismiss()
                                }
                                it.btnYes.setOnClickListener {
                                    val position = absoluteAdapterPosition
                                    showProgressBar(requireContext())
                                    RetrofitInstance.create().deleteDocuments(list[position].id)
                                        .enqueue(object : Callback<MessageResponse> {
                                            override fun onResponse(
                                                call: Call<MessageResponse>,
                                                response: Response<MessageResponse>
                                            ) {
                                                response.apply {
                                                    if (isSuccessful) {
                                                        hideProgressBar()
                                                        imageAdapter.notifyItemRemoved(position)
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

                                            override fun onFailure(
                                                call: Call<MessageResponse>,
                                                t: Throwable
                                            ) {
                                                hideProgressBar()
                                                t.message!!.showToast(requireContext())
                                            }
                                        })
                                }
                                setWidthCustomization(this)
                                requestWindowFeature(Window.FEATURE_NO_TITLE)
                                setCanceledOnTouchOutside(false)
                                setCancelable(false)
                                setContentView(it)
                                show()
                            }
                    }
                }
            }
        }
    }

    private fun uploadImage(file: File) {
        showProgressBar(requireContext())
        val requestBody = file.asRequestBody("image/jpeg".toMediaTypeOrNull())
        val call = RetrofitInstance.get().create(ApiInterface::class.java).uploadImage(
            MultipartBody.Part.createFormData(
                "file",
                file.name,
                requestBody
            )
        )

        call.enqueue(object : Callback<UploadImageResponse> {
            override fun onResponse(
                call: Call<UploadImageResponse>,
                response: Response<UploadImageResponse>
            ) {
                response.apply {
                    if (isSuccessful) {
                        imageUrl = body()!!.filename
                        document!!.doc_image = imageUrl
                        RetrofitInstance.create().editNannyImage(
                            hashMapOf(
                                "document_id" to document!!.id,
                                "doc_image" to imageUrl,
                                "doc_type" to document!!.doc_type,
                                "doc_name" to document!!.doc_name
                            )
                        ).enqueue(object : Callback<MessageResponse> {
                            override fun onFailure(call: Call<MessageResponse>, t: Throwable) {
                                hideProgressBar()
                                t.message!!.showToast(requireContext())
                            }

                            override fun onResponse(
                                call: Call<MessageResponse>,
                                response: Response<MessageResponse>
                            ) {
                                response.apply {
                                    if (isSuccessful) {
                                        getDocuments()
                                    } else {
                                        hideProgressBar()
                                        getErrorMessage(errorBody()!!, requireContext(), code())
                                    }
                                }
                            }
                        })
                    } else {
                        hideProgressBar()
                        getErrorMessage(errorBody()!!, requireContext(), code())
                    }
                }
            }

            override fun onFailure(call: Call<UploadImageResponse>, t: Throwable) {
                hideProgressBar()
                t.message.toString().showToast(requireContext())
            }
        })
    }


    private val takePhotoContract =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            if (it.data != null) {
                val bitmap = it.data?.extras?.get("data") as Bitmap
                val file = CommonFunctions.convertBitmapToFile(requireContext(), bitmap)
                uploadImage(file)
            }
        }

    @RequiresApi(Build.VERSION_CODES.P)
    private val chooseFromGalleryContract =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            if (it.data != null) {
                val uri = it.data?.data
                val source = ImageDecoder.createSource(requireContext().contentResolver, uri!!)
                val bitmap: Bitmap = ImageDecoder.decodeBitmap(source)
                val file = CommonFunctions.convertBitmapToFile(requireContext(), bitmap)
                uploadImage(file)
            }
        }

}