package com.au.gonannysingleactivity.fragments

import android.app.Dialog
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.graphics.Bitmap
import android.graphics.ImageDecoder
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.view.LayoutInflater
import android.view.View
import android.view.Window
import android.widget.Button
import android.widget.TextView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import com.au.gonannysingleactivity.R
import com.au.gonannysingleactivity.activities.CommonReplacementActivity
import com.au.gonannysingleactivity.objects.Constants.BASE_IMAGE_URL
import com.au.gonannysingleactivity.objects.Constants.KEY_ADD_DOCUMENTS
import com.au.gonannysingleactivity.objects.Constants.KEY_ADD_WWCC_FRAGMENT
import com.au.gonannysingleactivity.objects.Constants.KEY_FOR_FRAGMENT_REPLACEMENT
import com.au.gonannysingleactivity.utils.ApplicationGlobal
import com.au.gonannysingleactivity.utils.CommonFunctions
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
import com.google.gson.reflect.TypeToken
import kotlinx.android.synthetic.main.fragment_add_document_example.*
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.File

class AddDocumentsFragment : BaseFragment(), View.OnClickListener {

    private var documentsList: MutableList<PostDocumentsList> = mutableListOf()
    private var key: String = ""
    private var imageUrl: String = ""

    override fun getLayoutToInflate(): Int {
        return R.layout.fragment_add_document_example
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requireActivity().registerReceiver(broadcastReceiver, IntentFilter("WWCC_DOCUMENT"))
        requireActivity().window.statusBarColor =
            requireContext().getColor(R.color.appBackgroundColor)
        ApplicationGlobal.preferenceManager.setScreenState(KEY_ADD_DOCUMENTS)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        requireActivity().unregisterReceiver(broadcastReceiver)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setUpToolbar(
            getString(R.string.add_document),
            true,
            getString(R.string.app_done)
        ) { done() }


        tvAddWWCC.setOnClickListener(this)
        tvAddFirstAid.setOnClickListener(this)
        tvAddCPR.setOnClickListener(this)
        tvAddPoliceCheck.setOnClickListener(this)
        tvAddInsurance.setOnClickListener(this)

        // image card delete icon listener
        ivCardDeleteCPR.setOnClickListener(this)
        ivCardDeleteFirstAid.setOnClickListener(this)
        ivCardDeletePoliceCheck.setOnClickListener(this)
        ivCardDeleteInsurance.setOnClickListener(this)

        // image card edit icon click listener
        ivCardEditCPR.setOnClickListener(this)
        ivCardEditFirstAid.setOnClickListener(this)
        ivCardEditInsurance.setOnClickListener(this)
        ivCardEditPoliceCheck.setOnClickListener(this)

        editWWCC.setOnClickListener(this)

    }

    private val broadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            documentsList.addAll(
                Gson().fromJson(
                    intent!!.getStringExtra("data"),
                    object : TypeToken<List<PostDocumentsList>>() {}.type
                )
            )
            if ((documentsList.find { it.doc_type == 2 }) != null) {
                tvAddWWCC.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_cheked, 0, 0, 0)
                editWWCC.visibility = View.VISIBLE
            } else {
                tvAddWWCC.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_link, 0, 0, 0)
                editWWCC.visibility = View.GONE
            }
        }
    }

    private fun done() {
        if (isConnectedToInternet(requireContext())) {
            documentsList.apply {
                val dataFilled: Boolean = (find { it.doc_type == 2 } != null)
                        && (find { it.doc_type == 3 } != null)
                        && (find { it.doc_type == 4 } != null)
                        && (find { it.doc_type == 5 } != null)
                        && (find { it.doc_type == 6 } != null)

                if (dataFilled) {
                    showProgressBar(requireContext())
                    val hashMap = hashMapOf<String, Any>()
                    hashMap["documents"] = this
                    hashMap["is_wwcc_edit"] = 0

                    RetrofitInstance.create().addDocuments(hashMap)
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
                                        hideProgressBar()
                                        replaceFragment(
                                            R.id.frameLayout,
                                            WorkingHourFragment(),
                                            true
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
                } else getString(R.string.wraning_upload_all_documents).showToast(requireContext())
            }

        } else getString(R.string.check_internt).showToast(requireContext())
    }

    override fun modifyBackIcon() = true

    override fun modifyBackIconFunctionality() {
        requireActivity().apply {
            if (supportFragmentManager.backStackEntryCount > 0) this.onBackPressed()
            else CommonFunctions.showLogOutDialog(requireContext(), this)
        }
    }


    @RequiresApi(Build.VERSION_CODES.P)
    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.tvAddWWCC -> startActivity(
                Intent(
                    requireContext(),
                    CommonReplacementActivity::class.java
                ).apply {
                    putExtra(KEY_FOR_FRAGMENT_REPLACEMENT, KEY_ADD_WWCC_FRAGMENT)
                })

            in listOf(R.id.tvAddFirstAid, R.id.ivCardEditFirstAid) -> {
                key = "FirstAid"
                openDialog()
            }
            in listOf(R.id.tvAddCPR, R.id.ivCardEditCPR) -> {
                key = "CPR"
                openDialog()
            }
            in listOf(R.id.tvAddPoliceCheck, R.id.ivCardEditPoliceCheck) -> {
                key = "PoliceCheck"
                openDialog()
            }
            in listOf(R.id.tvAddInsurance, R.id.ivCardEditInsurance) -> {
                key = "Insurance"
                openDialog()
            }
            R.id.ivCardDeleteCPR -> openDeleteDialog("cpr")
            R.id.ivCardDeleteFirstAid -> openDeleteDialog("firstAid")
            R.id.ivCardDeletePoliceCheck -> openDeleteDialog("policeCheck")
            R.id.ivCardDeleteInsurance -> openDeleteDialog("insurance")

            R.id.editWWCC -> replaceFragment(R.id.frameLayout, AddWWCCFragment().apply {
                arguments = Bundle().apply {
                    putBoolean("toEditWWCC", true)
                }
            }, true)
        }
    }


    @RequiresApi(Build.VERSION_CODES.P)
    private fun openDialog() {
        val dialog = Dialog(requireContext())
        val view =
            LayoutInflater.from(requireContext()).inflate(R.layout.dialog_add_photo_layout, null)

        view.findViewById<TextView>(R.id.tvTakePhoto).setOnClickListener {
            val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
            dialog.dismiss()
            contractTakeImage.launch(intent)
        }
        view.findViewById<TextView>(R.id.tvChooseFromGallery).setOnClickListener {
            val intent = Intent()
            intent.type = "image/*"
            intent.action = Intent.ACTION_PICK
            dialog.dismiss()
            contractChooseFromGallery.launch(Intent.createChooser(intent, "Select an option"))
        }
        view.findViewById<TextView>(R.id.tvCancel).setOnClickListener {
            dialog.dismiss()
        }

        setWidthCustomization(dialog)
        dialog.apply {
            requestWindowFeature(Window.FEATURE_NO_TITLE)
            setCancelable(false)
            setCanceledOnTouchOutside(true)
            setContentView(view)
            show()
        }
    }

    private val contractTakeImage =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            if (it.data != null) {
                val bitmap = it.data!!.extras!!.get("data") as Bitmap
                val file = CommonFunctions.convertBitmapToFile(requireContext(), bitmap)
                uploadImage(file)
            }
        }

    @RequiresApi(Build.VERSION_CODES.P)
    private val contractChooseFromGallery =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            if (it.data != null) {
                val uri = it.data?.data
                val source = ImageDecoder.createSource(requireContext().contentResolver, uri!!)
                val bitmap: Bitmap = ImageDecoder.decodeBitmap(source)
                val file = CommonFunctions.convertBitmapToFile(requireContext(), bitmap)
                uploadImage(file)
            }
        }

    // to get image url for the documents
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
                            View.GONE.let { gone ->
                                View.VISIBLE.let { visible ->
                                    imageUrl = body()!!.filename
                                    when (key) {
                                        "FirstAid" -> {
                                            tvAddFirstAid.visibility = gone
                                            imageCardFirstAid.visibility = visible
                                            Glide.with(requireContext())
                                                .load("$BASE_IMAGE_URL$imageUrl").apply(
                                                    RequestOptions.centerCropTransform()
                                                        .placeholder(R.drawable.logo)
                                                ).into(ivCardFirstAid)
                                            documentsList.add(
                                                PostDocumentsList(
                                                    imageUrl,
                                                    "First Aid",
                                                    3,
                                                    ""
                                                )
                                            )
                                        }
                                        "Insurance" -> {
                                            tvAddInsurance.visibility = gone
                                            imageCardInsurance.visibility = visible
                                            Glide.with(requireContext())
                                                .load("$BASE_IMAGE_URL$imageUrl").apply(
                                                    RequestOptions.centerCropTransform()
                                                        .placeholder(R.drawable.logo)
                                                ).into(ivCardInsurance)
                                            documentsList.add(
                                                PostDocumentsList(
                                                    imageUrl,
                                                    "Two references",
                                                    6,
                                                    ""
                                                )
                                            )
                                        }
                                        "CPR" -> {
                                            tvAddCPR.visibility = gone
                                            imageCardCPR.visibility = visible
                                            Glide.with(requireContext())
                                                .load("$BASE_IMAGE_URL$imageUrl").apply(
                                                    RequestOptions.centerCropTransform()
                                                        .placeholder(R.drawable.logo)
                                                ).into(ivCardCPR)
                                            documentsList.add(
                                                PostDocumentsList(
                                                    imageUrl,
                                                    "CPR",
                                                    4,
                                                    ""
                                                )
                                            )
                                        }
                                        "PoliceCheck" -> {
                                            tvAddPoliceCheck.visibility = gone
                                            imageCardPoliceCheck.visibility = visible
                                            Glide.with(requireContext())
                                                .load("$BASE_IMAGE_URL$imageUrl").apply(
                                                    RequestOptions.centerCropTransform()
                                                        .placeholder(R.drawable.logo)
                                                ).into(ivCardPoliceCheck)
                                            documentsList.add(
                                                PostDocumentsList(
                                                    imageUrl,
                                                    "police check",
                                                    5,
                                                    ""
                                                )
                                            )
                                        }
                                        else -> {
                                        }
                                    }
                                }
                            }
                            hideProgressBar()
                            body()!!.message.showToast(requireContext())
                        } else {
                            hideProgressBar()
                            getErrorMessage(errorBody()!!, requireContext(), code())
                        }
                    }
                }
            })
        } else getString(R.string.check_internt).showToast(requireContext())
    }

    private fun deleteImage(key: String) {
        View.VISIBLE.let { visible ->
            View.GONE.let { gone ->
                when (key) {
                    "firstAid" -> {
                        documentsList.remove(documentsList.find { it.doc_type == 3 })
                        tvAddFirstAid.visibility = visible
                        imageCardFirstAid.visibility = gone
                    }
                    "policeCheck" -> {
                        documentsList.remove(documentsList.find { it.doc_type == 5 })
                        tvAddPoliceCheck.visibility = visible
                        imageCardPoliceCheck.visibility = gone
                    }
                    "cpr" -> {
                        documentsList.remove(documentsList.find { it.doc_type == 4 })
                        tvAddCPR.visibility = visible
                        imageCardCPR.visibility = gone
                    }
                    else -> {
                        documentsList.remove(documentsList.find { it.doc_type == 6 })
                        tvAddInsurance.visibility = visible
                        imageCardInsurance.visibility = gone
                    }
                }
            }
        }
    }

    private fun openDeleteDialog(key: String) {
        val dialog = Dialog(requireContext())
        val view =
            LayoutInflater.from(requireContext()).inflate(R.layout.basic_alert_dialog_layout, null)
        view.findViewById<TextView>(R.id.tvTitle).text = getString(R.string.delete)
        view.findViewById<TextView>(R.id.tvMessage).text = getString(R.string.mesage_delete_photo)
        view.findViewById<Button>(R.id.btnNo).setOnClickListener {
            dialog.dismiss()
        }
        view.findViewById<Button>(R.id.btnYes).setOnClickListener {
            dialog.dismiss()
            deleteImage(key)
        }
        setWidthCustomization(dialog)
        dialog.apply {
            requestWindowFeature(Window.FEATURE_NO_TITLE)
            setCanceledOnTouchOutside(false)
            setCancelable(false)
            setContentView(view)
            show()
        }
    }

}