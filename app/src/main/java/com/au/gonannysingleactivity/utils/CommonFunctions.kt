package com.au.gonannysingleactivity.utils

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.location.Address
import android.location.Geocoder
import android.net.ConnectivityManager
import android.provider.Settings
import android.view.LayoutInflater
import android.view.Window
import android.view.WindowManager
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat.startActivity
import com.au.gonannysingleactivity.R
import com.au.gonannysingleactivity.activities.MainActivity
import com.au.gonannysingleactivity.utils.Extensions.Companion.showToast
import com.au.gonannysingleactivity.webservices.ErrorBody
import com.au.gonannysingleactivity.webservices.MessageResponse
import com.au.gonannysingleactivity.webservices.api.ApiInterface
import com.au.gonannysingleactivity.webservices.api.RetrofitInstance
import com.google.android.gms.maps.model.LatLng
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.*

open class CommonFunctions {


    companion object {

        var dialog: Dialog? = null

        private val pinkColorArray = arrayOf("a", "g", "m", "s", "y")
        private val orangeColorArray = arrayOf("b", "h", "n", "t", "z")
        private val yellowColorArray = arrayOf("c", "i", "o", "u")
        private val greenColorArray = arrayOf("d", "j", "p", "v")
        private val blueColorArray = arrayOf("e", "k", "q", "w")





        fun convertBitmapToFile(context: Context,bitmap: Bitmap): File {

            val f = File(context.cacheDir, "imageFileName")
            f.createNewFile()

            //Convert bitmap to byte array
            val bos = ByteArrayOutputStream()
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, bos)
            val bitmapData = bos.toByteArray()

            //write the bytes in file
            val fos: FileOutputStream?
            try {
                fos = FileOutputStream(f)
                fos.write(bitmapData)
                fos.flush()
                fos.close()
            } catch (e: Exception) {
                e.printStackTrace()
            }
            return f
        }





        fun addressFromLatLng(context: Context, latLng: LatLng): Address =
            Geocoder(context, Locale.getDefault()).getFromLocation(
                latLng.latitude,
                latLng.longitude,
                1
            ).first()


        fun representTimeFormat(string: String): String {
            return string.substring(0..9)
        }

        fun getColorForLatter(char: CharSequence): Int {

            return when (char) {
                in pinkColorArray -> R.color.pinkColor
                in orangeColorArray -> R.color.orangeColor
                in yellowColorArray -> R.color.yellowColorImg
                in greenColorArray -> R.color.greenColorImg
                in blueColorArray -> R.color.blueColor
                else -> R.color.purpleColor
            }
        }

        fun setWidthCustomization(dialog: Dialog) {
            val lp = WindowManager.LayoutParams()
            lp.copyFrom(dialog.window!!.attributes)
            lp.width = WindowManager.LayoutParams.MATCH_PARENT;
            lp.height = WindowManager.LayoutParams.WRAP_CONTENT
            dialog.window!!.attributes = lp
        }

        fun showLogOutDialog(
            context: Context,
            activity: Activity
        ) {

            val builder = Dialog(context)
            builder.requestWindowFeature(Window.FEATURE_NO_TITLE)
            val view =
                LayoutInflater.from(context).inflate(R.layout.basic_alert_dialog_layout, null)
            val yesButton = view.findViewById<Button>(R.id.btnYes)
            val noButton = view.findViewById<Button>(R.id.btnNo)
            view.findViewById<TextView>(R.id.tvTitle).text = context.getString(R.string.log_out)
            view.findViewById<TextView>(R.id.tvMessage).text = context.getString(R.string.message_log_out)
            builder.setContentView(view)

            setWidthCustomization(builder)

            yesButton.setOnClickListener {
                showProgressBar(context)
                val hashMap = HashMap<String, Any>()
                hashMap["device_id"] = getDeviceId(context)
                val call =
                    RetrofitInstance.get().create(ApiInterface::class.java).logOut(hashMap)
                call.enqueue(object : Callback<MessageResponse> {
                    override fun onResponse(
                        call: Call<MessageResponse>,
                        response: Response<MessageResponse>
                    ) {
                        hideProgressBar()
                        response.apply {
                            if (isSuccessful) {
                                ApplicationGlobal.apply {
                                    userObject = null
                                    accessToken = ""
                                    preferenceManager.apply {
                                        saveUserObject(null)
                                        setScreenState("")
                                        clearAccessToken()
                                    }
                                }
                                startActivity(
                                    context,
                                    Intent(activity, MainActivity::class.java),
                                    null
                                )
                                activity.finish()
                            } else {
                                getErrorMessage(errorBody()!!, context, code())
                            }
                        }
                    }

                    override fun onFailure(call: Call<MessageResponse>, t: Throwable) {
                        hideProgressBar()
                        t.message!!.showToast(context)
                    }
                })
            }
            noButton.setOnClickListener {
                builder.dismiss()
            }
            builder.setCanceledOnTouchOutside(false)
            builder.show()
        }

        @SuppressLint("HardwareIds")
        fun getDeviceId(context: Context): String {
            return Settings.Secure.getString(context.contentResolver, Settings.Secure.ANDROID_ID)
        }

        fun get12HourFormattedTime(hourOfDay: Int, minute: Int): String {
            val min = if (minute in 0..9) "0$minute" else "$minute"
            return when (hourOfDay) {
                in 0..9 -> "0$hourOfDay:$min AM"
                12 -> "$hourOfDay:$min PM"
                in 10..12 -> "$hourOfDay:$min AM"
                in 12..24 -> {
                    if (hourOfDay - 12 > 9) {
                        "${hourOfDay - 12}:$min PM"
                    } else {
                        "0${hourOfDay - 12}:$min PM"
                    }
                }
                else -> ""
            }
        }

        fun get24HourFormattedTime(hour: Int, minute: Int): String {
            val min = if (minute in 0..9) "0$minute" else "$minute"
            return when (hour) {
                in 0..9 -> "0$hour:$min:00"
                else -> "$hour:$min:00"
            }
        }

        // for date in form of yyyy-mm-dd from time in millis
        fun getFormattedDate(time: Long): String {
            return SimpleDateFormat("yyyy-MM-dd hh:mm:ss.SSS", Locale.getDefault()).format(time)
                .substring(0, 11)
        }

        fun getFormattedTime(time:Long):String{
            return SimpleDateFormat("yyyy-MM-dd hh:mm:ss.SSS", Locale.getDefault()).format(time).substring(11,19)
        }

        // to format date from yyyy-mm-dd to yyyy-jan-dd
        fun getMonthSpecifiedFormattedDate(string: String): String {
            val inputFormat = SimpleDateFormat("yyyy-MM-dd")
            val outputFormat = SimpleDateFormat("yyyy-MMM-dd")

            return outputFormat.format(inputFormat.parse(string))

        }

        fun getDateInMillis(date: String): Long {
            return SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).parse(date)!!.time
        }

        fun showProgressBar(context: Context) {
            dialog = Dialog(context)
            dialog!!.apply {
                setCancelable(false)
                requestWindowFeature(Window.FEATURE_NO_TITLE)
                window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
                setContentView(R.layout.layout_progress_bar)
            }
            dialog!!.show()
        }

        fun hideProgressBar() {
            if (dialog != null) {
                dialog!!.dismiss()
                dialog = null
            }
        }

        fun checkPermissionEnabled(context: Context): Boolean {
            return !(ActivityCompat.checkSelfPermission(
                context,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
                    && ActivityCompat.checkSelfPermission(
                context,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED)
        }


        fun isConnectedToInternet(context: Context): Boolean {
            val connectivityManager =
                context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
            val activeNetworkInfo = connectivityManager.activeNetworkInfo
            if (activeNetworkInfo != null && activeNetworkInfo.isConnected) {
                return true
            }
            return false
        }

        fun getErrorMessage(responseBody: ResponseBody, context: Context, errorCode: Int) {
            try {
                val errorConverter = RetrofitInstance.get()
                    .responseBodyConverter<ErrorBody>(
                        ErrorBody::class.java,
                        arrayOfNulls<Annotation>(0)
                    )
                val error = errorConverter.convert(responseBody)
                showToast(context, error!!.error_description)
//                if (errorCode == 401) {
//                    ApplicationGlobal.accessToken = ""
//                    Prefs.with(context).removeAll(context)
//                    context.startActivity(Intent(context, SplashActivity::class.java))
//                    .finishAffinity()
//                }
            } catch (e: java.lang.Exception) {
                showToast(context, e.message.toString())
            }
        }

        fun showToast(context: Context, message: String) {
            Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
        }

        fun isValidName(string: String): Boolean {
            return string.length >= 3
        }

        fun isValidEmail(string: String): Boolean {
            return string.contains(regex = Regex("^[a-zA-Z0-9+_.-]+@[a-zA-Z0-9.-]+\$"))
        }

        fun isValidNumber(string: String): Boolean {
            return string.length in 7..11
        }

        fun isValidPassword(string: String): Boolean {
            return string.contains(
                Regex(
                    "^" +
                            "(?=.*[@#$%^&+=])" +     // at least 1 special character
                            "(?=\\S+$)" +            // no white spaces
                            ".{4,}" +                // at least 4 characters
                            "$"
                )
            )
        }

        fun isCardNumberValid(cardNumber: String): Boolean {
            return cardNumber.length in 16..19
        }

        fun isCardCvvValid(cardCVV: String): Boolean {
            return cardCVV.length in 3..4
        }

        fun isCardExpiryValid(cardExpiry: String): Boolean {
            return cardExpiry.length in 2..6
        }

        fun isNameValid(cardHolderName: String): Boolean {
            return cardHolderName.length in 3..30
        }

        fun isParagraphValid(para: String): Boolean {
            return para.length in 40..200
        }

    }
}
