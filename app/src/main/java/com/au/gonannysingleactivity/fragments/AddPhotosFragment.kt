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
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.au.gonannysingleactivity.R
import com.au.gonannysingleactivity.objects.Constants.BASE_IMAGE_URL
import com.au.gonannysingleactivity.objects.Constants.KEY_ADD_PHOTO_FRAGMENT
import com.au.gonannysingleactivity.utils.ApplicationGlobal
import com.au.gonannysingleactivity.utils.CommonFunctions.Companion.convertBitmapToFile
import com.au.gonannysingleactivity.utils.CommonFunctions.Companion.getErrorMessage
import com.au.gonannysingleactivity.utils.CommonFunctions.Companion.hideProgressBar
import com.au.gonannysingleactivity.utils.CommonFunctions.Companion.isConnectedToInternet
import com.au.gonannysingleactivity.utils.CommonFunctions.Companion.setWidthCustomization
import com.au.gonannysingleactivity.utils.CommonFunctions.Companion.showLogOutDialog
import com.au.gonannysingleactivity.utils.CommonFunctions.Companion.showProgressBar
import com.au.gonannysingleactivity.utils.Extensions.Companion.showToast
import com.au.gonannysingleactivity.webservices.*
import com.au.gonannysingleactivity.webservices.api.RetrofitInstance
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import kotlinx.android.synthetic.main.common_toolbar.*
import kotlinx.android.synthetic.main.common_toolbar.view.*
import kotlinx.android.synthetic.main.fragment_nanny_add_photos.*
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.File

open class AddPhotosFragment : BaseFragment(), View.OnClickListener {

    private var list: MutableList<NannyImage> = mutableListOf()
    private var imageAdapter: ImageAdapter =  ImageAdapter()
    lateinit var lm: LinearLayoutManager

    private var toAddImage:Boolean = false
    private var toEditImage: Boolean = false
    private var imageId: Int? = null
    private var imageUrl: String = ""
    private var imageCaption: String = ""

    override fun getLayoutToInflate(): Int {
        return R.layout.fragment_nanny_add_photos
    }

    override fun modifyBackIcon(): Boolean {
        return true
    }

    override fun modifyBackIconFunctionality() {
        if (requireActivity().supportFragmentManager.backStackEntryCount > 0) {
            requireActivity().onBackPressed()
        } else {
            showLogOutDialog(requireContext(), requireActivity())
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        arguments?.apply {
            toAddImage = getBoolean("toAddImage",true)
            toEditImage = getBoolean("toEditImage", false)
            imageId = getInt("image_id")
            imageUrl = getString("image_url", "")
            imageCaption = getString("image_caption", "")
        }

        if (!(toEditImage||toAddImage)) {
            ApplicationGlobal.preferenceManager.setScreenState(KEY_ADD_PHOTO_FRAGMENT)
        }

    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        if (toEditImage||toAddImage) {

            if(toEditImage) {
                setUpToolbar(getString(R.string.edit_image), true, null) {}
                etPhotoCaption.setText(imageCaption)
                Glide.with(requireContext()).load("$BASE_IMAGE_URL$imageUrl")
                    .apply(RequestOptions.centerCropTransform()).into(ivUploadedPhoto)
                rvUploadPhotos.visibility = View.GONE
                btnAddPhoto.text = getString(R.string.edit_image)
            }else setUpToolbar(getString(R.string.add_photo), true, null) {}

        } else {
            setUpToolbar(getString(R.string.add_photos), true, getString(R.string.skip)) {
                replaceFragment(R.id.frameLayout, AddDocumentsFragment(), true)
            }
            rvUploadPhotos.visibility = View.VISIBLE
            rvUploadPhotos.apply {

                lm = object : LinearLayoutManager(requireContext()) {
                    override fun canScrollVertically(): Boolean {
                        return false
                    }
                }

                layoutManager = lm
                adapter = imageAdapter
            }
            btnAddPhoto.text = getString(R.string.add_photos)
        }

        ivUploadedPhoto.setOnClickListener(this)
        ivUploadPhotos.setOnClickListener(this)
        btnAddPhoto.setOnClickListener(this)

        if (isConnectedToInternet(requireContext()) && !toEditImage) {
            showProgressBar(requireContext())
            getImages()
        }

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


    private fun addPhoto() {
        if (isConnectedToInternet(requireContext())) {
            val caption = etPhotoCaption.text.trim().toString()
            if (imageUrl.isNotBlank()) {
                if (caption.isNotEmpty()) {
                    val call: Call<MessageResponse>

                    if (toEditImage) {
                        val hashMap = hashMapOf<String, Any>()
                        hashMap["image_id"] = imageId!!
                        hashMap["image"] = imageUrl
                        hashMap["caption"] = caption
                        call = RetrofitInstance.create().editNannyImage(hashMap)
                    } else {
                        val hashMap = hashMapOf<String, Any>()
                        hashMap["add_image"] = mutableListOf(NannyPhoto(imageUrl, caption))
                        call = RetrofitInstance.create().nannyUploadImages(hashMap)
                    }

                    etPhotoCaption.setText("")
                    ivUploadedPhoto.setBackgroundResource(R.color.white)

                    showProgressBar(requireContext())
                    call.enqueue(object : Callback<MessageResponse> {
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

                                    if (toEditImage||toAddImage) {
                                        hideProgressBar()
                                        requireActivity().onBackPressed()
                                    } else getImages()

                                } else {
                                    hideProgressBar()
                                    getErrorMessage(errorBody()!!, requireContext(), code())
                                }

                            }
                        }
                    })
                } else
                    getString(R.string.enter_image_caption).showToast(requireContext())
            } else
                getString(R.string.upload_an_image).showToast(requireContext())
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
            R.id.ivUploadedPhoto -> openDialog()
            R.id.ivUploadPhotos -> openDialog()
            R.id.btnAddPhoto -> addPhoto()
        }
    }

    private fun getImages() {
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
                            list = body()!!.nanny_images as MutableList<NannyImage>
                            hideProgressBar()
                            imageAdapter.notifyItemRangeChanged(0, list.count())
                            applicationToolbar.tvToolbarNavigation.text =
                                if (list.count() > 0) getString(R.string.next) else getString(R.string.skip)
                        } else {
                            hideProgressBar()
                            getErrorMessage(errorBody()!!, requireContext(), code())
                        }
                    }
                }
            })
    }

    inner class ImageAdapter : RecyclerView.Adapter<ImageAdapter.ViewHolder>() {

        override fun getItemCount(): Int {
            return list.count()
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            return ViewHolder(
                LayoutInflater.from(parent.context)
                    .inflate(R.layout.item_nanny_image, parent, false)
            )
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            holder.apply {
                list[position].let {
                    Glide.with(requireContext()).load("$BASE_IMAGE_URL${it.image}")
                        .apply(RequestOptions.centerCropTransform()).into(image)
                    caption.text = it.image_caption
                }
            }
        }


        inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
            val image: ImageView = view.findViewById(R.id.ivItemUploaded)
            val caption: TextView = view.findViewById(R.id.tvItemCaption)
            private val delete: ImageView = view.findViewById(R.id.ivDelete)
            private val edit: ImageView = view.findViewById(R.id.ivEdit)

            init {

                delete.setOnClickListener {
                    openDeleteDialog(list[absoluteAdapterPosition].id, absoluteAdapterPosition)
                }
                edit.setOnClickListener {

                    list[absoluteAdapterPosition].let {
                        replaceFragment(R.id.frameLayout, AddPhotosFragment().apply {
                            arguments = Bundle().apply {
                                putBoolean("toEditImage", true)
                                putInt("image_id", it.id)
                                putString("image_url", it.image)
                                putString("image_caption", it.image_caption)
                            }
                        }, true)
                    }
                }

            }
        }
    }

    private fun openDeleteDialog(id: Int, position: Int) {
        val dialog = Dialog(requireContext())
        val view =
            LayoutInflater.from(requireContext()).inflate(R.layout.basic_alert_dialog_layout, null)

        view.findViewById<TextView>(R.id.tvTitle).text = getString(R.string.delete_photo)
        view.findViewById<TextView>(R.id.tvMessage).text = getString(R.string.mesage_delete_photo)

        view.findViewById<Button>(R.id.btnNo).setOnClickListener {
            dialog.dismiss()
        }

        view.findViewById<Button>(R.id.btnYes).setOnClickListener {
            dialog.dismiss()
            initiateDeletion(id, position)
        }

        dialog.apply {
            requestWindowFeature(Window.FEATURE_NO_TITLE)
            setCancelable(false)
            setCanceledOnTouchOutside(false)
            setWidthCustomization(this)
            setContentView(view)
            show()
        }

    }

    private fun initiateDeletion(id: Int, position: Int) {
        if (isConnectedToInternet(requireContext())) {
            showProgressBar(requireContext())
            RetrofitInstance.create()
                .deleteNannyImage(id)
                .enqueue(object : Callback<MessageResponse> {

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
                                list.removeAt(position)
                                imageAdapter.notifyItemRemoved(position)
                                getImages()
                            } else {
                                hideProgressBar()
                                getErrorMessage(errorBody()!!, requireContext(), code())
                            }
                        }
                    }
                })
        } else {
            getString(R.string.check_internt).showToast(requireContext())
        }
    }
}
