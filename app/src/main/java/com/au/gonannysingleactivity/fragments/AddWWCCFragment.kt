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
import android.widget.ArrayAdapter
import android.widget.ImageView
import android.widget.TextView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.au.gonannysingleactivity.R
import com.au.gonannysingleactivity.activities.MainActivity.Companion.documents
import com.au.gonannysingleactivity.objects.Constants.BASE_IMAGE_URL
import com.au.gonannysingleactivity.utils.CommonFunctions.Companion.convertBitmapToFile
import com.au.gonannysingleactivity.utils.CommonFunctions.Companion.getErrorMessage
import com.au.gonannysingleactivity.utils.CommonFunctions.Companion.hideProgressBar
import com.au.gonannysingleactivity.utils.CommonFunctions.Companion.isConnectedToInternet
import com.au.gonannysingleactivity.utils.CommonFunctions.Companion.setWidthCustomization
import com.au.gonannysingleactivity.utils.CommonFunctions.Companion.showProgressBar
import com.au.gonannysingleactivity.utils.Extensions.Companion.showToast
import com.au.gonannysingleactivity.webservices.MessageResponse
import com.au.gonannysingleactivity.webservices.PostDocumentsList
import com.au.gonannysingleactivity.webservices.UploadImageResponse
import com.au.gonannysingleactivity.webservices.api.RetrofitInstance
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.google.gson.Gson
import kotlinx.android.synthetic.main.basic_alert_dialog_layout.view.*
import kotlinx.android.synthetic.main.fragment_add_wwcc_document.*
import kotlinx.android.synthetic.main.item_nanny_image.view.*
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.File

class AddWWCCFragment : BaseFragment(), View.OnClickListener {

    private val statesList: MutableList<String> = mutableListOf(
        "New South Wales",
        "Queensland",
        "South Australia",
        "Tasmania",
        "Victoria",
        "Western Australia",
        "Northern Territory",
        "Australian Capital Territory"
    )

    private var list: MutableList<PostDocumentsList> = mutableListOf()
    private var imageUrl: String = ""
    private var toEditWWCC: Boolean = false
    private var toEditWWCCSetting: Boolean = false

    override fun getLayoutToInflate(): Int {
        return R.layout.fragment_add_wwcc_document
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.apply {
            toEditWWCC = getBoolean("toEditWWCC", false)
            toEditWWCCSetting = getBoolean("toEditWWCCFromSetting", false)

            if (toEditWWCCSetting) {
                for (item in documents) list.add(
                    PostDocumentsList(
                        item.doc_image, item.doc_name,
                        item.doc_type, item.state.toString()
                    )
                )
            }
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setUpToolbar(getString(R.string.add_wwcc), true, getString(R.string.save)) { save() }

        autoCompleteSelectStates.apply {
            setAdapter(
                ArrayAdapter(
                    requireContext(),
                    android.R.layout.simple_list_item_1,
                    statesList
                )
            )
            threshold = 1
            setOnClickListener { this.showDropDown() }

            setOnItemClickListener { _, _, position, _ ->
                this.setText(statesList[position], false)
            }

        }

        btnSubmit.setOnClickListener(this)
        ivUploadedPhoto.setOnClickListener(this)
        ivUploadWWCC.setOnClickListener(this)

        rvWWCCDocuments.apply {
            adapter = WWCCAdapter()
            layoutManager = LinearLayoutManager(requireContext())
        }

        if (toEditWWCC || toEditWWCCSetting) {
            setUpToolbar(getString(R.string.edit_wwcc), true, getString(R.string.save)) { save() }
            if (toEditWWCC) {
                autoCompleteSelectStates.setText(list.last().state)
                Glide.with(requireContext()).load("$BASE_IMAGE_URL${list.last().doc_image}").apply(
                    RequestOptions.centerCropTransform()
                ).into(ivUploadedPhoto)
            }
            rvWWCCDocuments.adapter?.notifyItemRangeInserted(0, list.count())
        }
    }

    private fun save() {
        if (list.count() > 0) {
            if (toEditWWCCSetting) {
                showProgressBar(requireContext())
                val addDocsList = mutableListOf<PostDocumentsList>()
                for (doc in list)
                    if (documents.find { it.state == doc.state } == null)
                        addDocsList.add(doc)

                RetrofitInstance.create()
                    .addDocuments(hashMapOf("documents" to addDocsList, "is_wwcc_edit" to 1)).enqueue(object :Callback<MessageResponse>{
                        override fun onResponse(
                            call: Call<MessageResponse>,
                            response: Response<MessageResponse>
                        ) {
                            response.apply {
                                if(isSuccessful){
                                    hideProgressBar()
                                    requireActivity().onBackPressed()
                                }else{
                                    hideProgressBar()
                                    getErrorMessage(errorBody()!!,requireContext(),code())
                                }
                            }
                        }

                        override fun onFailure(call: Call<MessageResponse>, t: Throwable) {
                            hideProgressBar()
                            t.message!!.showToast(requireContext())
                        }
                    })

            } else {
                requireActivity().sendBroadcast(Intent("WWCC_DOCUMENT").apply {
                    putExtra("data", Gson().toJson(list))
                })
                requireActivity().onBackPressed()
            }
        }
    }

    // Adapter
    inner class WWCCAdapter : RecyclerView.Adapter<WWCCAdapter.ViewHolder>() {

        inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
            val image: ImageView = view.findViewById(R.id.ivItemUploaded)
            val state: TextView = view.findViewById(R.id.tvItemCaption)
            private val delete: ImageView = view.findViewById(R.id.ivDelete)

            init {
                delete.setOnClickListener {

                    if (list.count() > 1) {
                        val dialog = Dialog(requireContext())
                        LayoutInflater.from(requireContext())
                            .inflate(R.layout.basic_alert_dialog_layout, null).let { view ->

                                view.tvTitle.text = getString(R.string.delete_document)
                                view.tvMessage.text = getString(R.string.message_delete_document)
                                view.btnNo.setOnClickListener { dialog.dismiss() }
                                view.btnYes.setOnClickListener {
                                    list.removeAt(absoluteAdapterPosition)
                                    dialog.dismiss()
                                    notifyItemRemoved(absoluteAdapterPosition)
                                }

                                setWidthCustomization(dialog)

                                dialog.apply {
                                    requestWindowFeature(Window.FEATURE_NO_TITLE)
                                    setCancelable(false)
                                    setCanceledOnTouchOutside(false)
                                    setContentView(view)
                                    show()
                                }
                            }
                    } else {
                        getString(R.string.delete_wwcc_document).showToast(requireContext())
                    }
                }
            }
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.item_nanny_image, parent, false)
            view.ivEdit.visibility = View.GONE
            return ViewHolder(view)
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            holder.apply {
                list[position].let {
                    Glide.with(requireContext()).load("$BASE_IMAGE_URL${it.doc_image}").apply(
                        RequestOptions.centerCropTransform().placeholder(R.drawable.logo_text_large)
                    ).into(image)
                    state.text = it.state
                }
            }
        }

        override fun getItemCount(): Int = list.count()

    }

    @RequiresApi(Build.VERSION_CODES.P)
    private fun openDialog() {
        val dialog = Dialog(requireContext())
        val view = LayoutInflater.from(requireContext())
            .inflate(R.layout.dialog_add_photo_layout, null)
        val takePhoto: TextView = view.findViewById(R.id.tvTakePhoto)
        val chooseFromGallery: TextView = view.findViewById(R.id.tvChooseFromGallery)
        val cancel: TextView = view.findViewById(R.id.tvCancel)
        takePhoto.setOnClickListener {
            val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
            dialog.dismiss()
            contractTakePhoto.launch(intent)
        }
        chooseFromGallery.setOnClickListener {
            val intent = Intent()
            intent.type = "image/*"
            intent.action = Intent.ACTION_PICK
            dialog.dismiss()
            contractChooseFromGallery.launch(Intent.createChooser(intent, "Select an option."))
        }
        cancel.setOnClickListener {
            dialog.dismiss()
        }
        setWidthCustomization(dialog)
        dialog.apply {
            requestWindowFeature(Window.FEATURE_NO_TITLE)
            setCancelable(false)
            setCanceledOnTouchOutside(false)
            setContentView(view)
            show()
        }
    }

    private fun uploadImage(file: File) {
        if (isConnectedToInternet(requireContext())) {
            showProgressBar(requireContext())
            val requestBody = file.asRequestBody("image/jpeg".toMediaTypeOrNull())
            val call = RetrofitInstance.create().uploadImage(
                MultipartBody.Part.createFormData(
                    "file",
                    file.name,
                    requestBody
                )
            )
            call.enqueue(object : Callback<UploadImageResponse> {

                override fun onFailure(call: Call<UploadImageResponse>, t: Throwable) {
                    hideProgressBar()
                    t.message!!.showToast(requireContext())
                }

                override fun onResponse(
                    call: Call<UploadImageResponse>,
                    response: Response<UploadImageResponse>
                ) {
                    response.apply {
                        if (isSuccessful) {
                            imageUrl = body()!!.filename
                            hideProgressBar()
                            body()!!.message.showToast(requireContext())
                        } else {
                            hideProgressBar()
                            getErrorMessage(errorBody()!!, requireContext(), code())
                        }
                    }
                }
            })
        } else
            getString(R.string.check_internt).showToast(requireContext())

    }

    private val contractTakePhoto =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            if (it.data != null) {
                val bitmap = it.data!!.extras!!.get("data") as Bitmap
                Glide.with(requireContext()).load(bitmap)
                    .apply(RequestOptions.centerCropTransform()).into(ivUploadedPhoto)
                val file = convertBitmapToFile(requireContext(), bitmap)
                uploadImage(file)
            }
        }

    @RequiresApi(Build.VERSION_CODES.P)

    private val contractChooseFromGallery =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            if (it.data != null) {
                val uri = it.data?.data
                Glide.with(requireContext()).load(uri).apply(RequestOptions.centerCropTransform())
                    .into(ivUploadedPhoto)
                val source = ImageDecoder.createSource(requireContext().contentResolver, uri!!)
                val bitmap: Bitmap = ImageDecoder.decodeBitmap(source)
                val file = convertBitmapToFile(requireContext(), bitmap)
                uploadImage(file)
            }
        }

    @RequiresApi(Build.VERSION_CODES.P)
    override fun onClick(v: View?) {
        when (v?.id) {

            R.id.btnSubmit -> {

                val state = autoCompleteSelectStates.text.trim().toString()

                if (imageUrl.isNotBlank()) {
                    if (state.isNotBlank()) {

                        if ((list.find { it.state == state }) == null) {

                            list.add(PostDocumentsList(imageUrl, "wwcc", 2, state))
                            rvWWCCDocuments.adapter?.notifyItemInserted(list.count() - 1)

                            ivUploadedPhoto.setBackgroundResource(android.R.color.transparent)
                            autoCompleteSelectStates.setText("")


                        } else getString(R.string.cant_add_wwcc_to_same_state).showToast(
                            requireContext()
                        )

                    } else getString(R.string.select_an_state).showToast(requireContext())
                } else getString(R.string.upload_an_image).showToast(requireContext())

            }

            R.id.ivUploadedPhoto -> openDialog()
            R.id.ivUploadWWCC -> openDialog()
        }
    }


}
