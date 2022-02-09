package com.au.gonannysingleactivity.fragments

import android.app.Dialog
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.ImageDecoder
import android.graphics.drawable.GradientDrawable
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.view.LayoutInflater
import android.view.View
import android.view.Window
import android.widget.ImageView
import android.widget.TextView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import com.au.gonannysingleactivity.R
import com.au.gonannysingleactivity.objects.Constants.BASE_IMAGE_URL
import com.au.gonannysingleactivity.utils.ApplicationGlobal
import com.au.gonannysingleactivity.utils.CommonFunctions.Companion.convertBitmapToFile
import com.au.gonannysingleactivity.utils.CommonFunctions.Companion.getColorForLatter
import com.au.gonannysingleactivity.utils.CommonFunctions.Companion.getErrorMessage
import com.au.gonannysingleactivity.utils.CommonFunctions.Companion.hideProgressBar
import com.au.gonannysingleactivity.utils.CommonFunctions.Companion.isValidEmail
import com.au.gonannysingleactivity.utils.CommonFunctions.Companion.isValidName
import com.au.gonannysingleactivity.utils.CommonFunctions.Companion.isValidNumber
import com.au.gonannysingleactivity.utils.CommonFunctions.Companion.setWidthCustomization
import com.au.gonannysingleactivity.utils.CommonFunctions.Companion.showProgressBar
import com.au.gonannysingleactivity.utils.Extensions.Companion.showToast
import com.au.gonannysingleactivity.webservices.UpdateUserProfileResponse
import com.au.gonannysingleactivity.webservices.UploadImageResponse
import com.au.gonannysingleactivity.webservices.api.ApiInterface
import com.au.gonannysingleactivity.webservices.api.RetrofitInstance
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DecodeFormat
import com.bumptech.glide.request.RequestOptions
import com.bumptech.glide.request.target.Target
import kotlinx.android.synthetic.main.fragment_edit_profile.*
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream

class EditProfileFragment : BaseFragment() {

    private var imageUrl: String = ""
    private var file: File? = null
    private var isNumberChanged: Boolean = false

    private lateinit var ivProfile: ImageView
    private lateinit var tvProfile: TextView

    override fun getLayoutToInflate(): Int {
        return R.layout.fragment_edit_profile
    }


    //upload image and get url that addresses that image
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
                        hideProgressBar()
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

    @RequiresApi(Build.VERSION_CODES.P)
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        ivProfile = view.findViewById(R.id.ivProfile)
        tvProfile = view.findViewById(R.id.tvProfile)
        setUpToolbar(
            getString(R.string.edit_profile),
            true,
            getString(R.string.save)
        ) {
            saveProfile()
        }
        ApplicationGlobal.userObject?.apply {
            etFirstName.setText(first_name.toString())
            if (last_name != null) {
                etLastName.setText(last_name.toString())
            }
            etEmail.setText(email.toString())
            etPhoneNumber.setText(phone_number.toString())

            if (profile_image == null) {
                tvProfile.visibility = View.VISIBLE
                tvProfile.text = first_name.toString()[0].toString()
                (ivProfile.background as GradientDrawable).setColor(
                    ContextCompat.getColor(
                        requireContext(),
                        getColorForLatter(
                            first_name.toString()[0].toString().lowercase()
                        )
                    )
                )
            } else {
                tvProfile.visibility = View.GONE
                Glide.with(requireContext()).load("$BASE_IMAGE_URL${profile_image}")
                    .apply(
                        RequestOptions().circleCrop().format(DecodeFormat.PREFER_ARGB_8888)
                            .override(
                                Target.SIZE_ORIGINAL
                            ).placeholder(R.drawable.ic_profile_large)
                    ).into(ivProfile)
            }
        }

        if (tvProfile.isVisible) {
            tvProfile.setOnClickListener(imageClickListener)
        } else {
            ivProfile.setOnClickListener(imageClickListener)
        }

    }

    // save changes
    private fun saveProfile() {

        val firstName = etFirstName.text.toString().trim()
        val lastName = etLastName.text.toString().trim()
        val phoneNumber = etPhoneNumber.text.toString().toLong()
        val countryCode = ccpPhone.selectedCountryCodeWithPlus.toString()
        val email = etEmail.text.toString()

        if (!isValidName(firstName)) {
            getString(R.string.enter_valid_name).showToast(requireContext())
        } else if (!isValidName(lastName)) {
            getString(R.string.enter_valid_name).showToast(requireContext())
        } else if (!isValidEmail(email)) {
            getString(R.string.enter_valid_email).showToast(requireContext())
        } else if (!isValidNumber(phoneNumber.toString())) {
            getString(R.string.enter_phone_number).showToast(requireContext())
        } else {

            showProgressBar(requireContext())
            val hashMap = HashMap<String, Any>()

            ApplicationGlobal.userObject!!.let {
                if (firstName != it.first_name) {
                    hashMap["first_name"] = firstName
                }
                if (lastName != it.last_name) {
                    hashMap["last_name"] = lastName
                }
                if (phoneNumber != it.phone_number || countryCode != it.country_code) {
                    isNumberChanged = true
                    hashMap["phone_number"] = phoneNumber
                    hashMap["country_code"] = countryCode
                }
                if (email != it.email) {
                    hashMap["email"] = email
                }
                if (imageUrl.isNotBlank()) {
                    hashMap["profile_image"] = imageUrl
                }
            }

            if (hashMap.isEmpty()) {
                hideProgressBar()
            } else {
                val call =
                    RetrofitInstance.get().create(ApiInterface::class.java)
                        .updateUserProfile(hashMap)
                call.enqueue(object : Callback<UpdateUserProfileResponse> {
                    override fun onResponse(
                        call: Call<UpdateUserProfileResponse>,
                        response: Response<UpdateUserProfileResponse>
                    ) {
                        response.apply {
                            if (isSuccessful) {
                                if (isNumberChanged) {
                                    replaceFragment(
                                        R.id.flCommonReplacement,
                                        VerifyNumberFragment().apply {
                                            arguments = Bundle().apply {
                                                putBoolean("fromProfile", true)
                                                putLong("mobile_number", phoneNumber)
                                                putString("country_code", countryCode)
                                            }
                                        },
                                        true
                                    )
                                    hideProgressBar()
                                } else {
                                    ApplicationGlobal.apply {
                                        userObject = body()!!.user
                                        preferenceManager.saveUserObject(body()!!.user)
                                        isChanged = true
                                    }
                                    hideProgressBar()
                                    requireActivity().onBackPressed()
                                }
                            } else {
                                hideProgressBar()
                                getErrorMessage(errorBody()!!, requireContext(), code())
                            }
                        }
                    }

                    override fun onFailure(call: Call<UpdateUserProfileResponse>, t: Throwable) {
                        hideProgressBar()
                        t.message.toString().showToast(requireContext())
                    }
                })
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.P)
    private val imageClickListener = View.OnClickListener {

        val dialog = Dialog(requireContext())
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        val view = LayoutInflater.from(requireContext())
            .inflate(R.layout.dialog_add_photo_layout, null)
        dialog.setContentView(view)
        val takePhoto = view.findViewById<TextView>(R.id.tvTakePhoto)
        val chooseFromGallery = view.findViewById<TextView>(R.id.tvChooseFromGallery)
        val cancel = view.findViewById<TextView>(R.id.tvCancel)

        setWidthCustomization(dialog)

        takePhoto.setOnClickListener {
            val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
            dialog.dismiss()
            contractTakeImage.launch(intent)
        }
        chooseFromGallery.setOnClickListener {
            val intent = Intent()
            intent.type = "image/*"
            intent.action = Intent.ACTION_PICK
            dialog.dismiss()
            contractChooseFromGallery.launch(Intent.createChooser(intent, "Select an Option"))

        }
        cancel.setOnClickListener {
            dialog.dismiss()
        }
        dialog.show()
    }

    private val contractTakeImage =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            if (it.data != null) {
                val bitmap = it.data?.extras?.get("data") as Bitmap
                file = convertBitmapToFile(requireContext(),bitmap)
                Glide.with(requireContext()).load(bitmap)
                    .apply(RequestOptions.circleCropTransform().placeholder(R.drawable.ic_profile_large)).into(ivProfile)
                uploadImage(file!!)
            }
        }

    @RequiresApi(Build.VERSION_CODES.P)
    private val contractChooseFromGallery =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            if (it.data != null) {
                val uri = it.data?.data
                val source = ImageDecoder.createSource(requireContext().contentResolver, uri!!)
                val bitmap: Bitmap = ImageDecoder.decodeBitmap(source)
                file = convertBitmapToFile(requireContext(),bitmap)
                Glide.with(requireContext()).load(uri)
                    .apply(RequestOptions.circleCropTransform().placeholder(R.drawable.ic_profile_large)).into(ivProfile)
                uploadImage(file!!)
            }
        }


}