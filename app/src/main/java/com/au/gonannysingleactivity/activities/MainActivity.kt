package com.au.gonannysingleactivity.activities

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.au.gonannysingleactivity.R
import com.au.gonannysingleactivity.fragments.*
import com.au.gonannysingleactivity.objects.Constants
import com.au.gonannysingleactivity.utils.ApplicationGlobal
import com.au.gonannysingleactivity.utils.ApplicationGlobal.Companion.mapNotification
import com.au.gonannysingleactivity.webservices.Document
import com.au.gonannysingleactivity.webservices.PostDocumentsList

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        setTheme(R.style.themeOverLapStatusBar)
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        var fragment: Fragment = SplashFragment()


        ApplicationGlobal.let {
            intent.apply {
                if (hasExtra("fromNotification") || hasExtra("notif_type")) {
                    if (hasExtra("fromNotification")) {
                        getBundleExtra("fromNotification").let { bundle ->
                            mapNotification.apply {
                                this["notif_type"] = bundle!!.getString("notif_type", "")
                                this["booking_id"] = bundle.getString("booking_id", "")
                                this["from_user_image"] = bundle.getString("from_user_image", "")
                                this["from_user_id"] = bundle.getString("from_user_id", "")
                                this["chat_id"] = bundle.getString("chat_id", "")
                                this["from_user_first_name"] =
                                    bundle.getString("from_user_first_name", "")
                            }
                        }
                    } else
                        mapNotification.apply {
                            this["notif_type"] = getStringExtra("notif_type")!!
                            this["booking_id"] = getStringExtra("booking_id")!!
                            this["from_user_image"] = getStringExtra("from_user_image")!!
                            this["from_user_id"] = getStringExtra("from_user_id")!!
                            this["chat_id"] = getStringExtra("chat_id")!!
                            this["from_user_first_name"] = getStringExtra("from_user_first_name")!!
                        }

                    it.fromNotification = true
                    startActivity(Intent(this@MainActivity, HomeActivity::class.java))
                    finish()
                }
            }

            it.preferenceManager.getScreenState().let { status ->
                if (status != null) {
                    when (status) {
                        Constants.KEY_COMPLETE_SIGN_UP -> fragment = CompleteSignUpFragment()
                        Constants.KEY_ADD_KID_INFO -> fragment = AddKidInfoFragment()
                        Constants.KEY_ADD_ADDRESS -> fragment = AddAddressFragment()
                        Constants.KEY_ADD_CARD -> fragment = AddCardFragment()
                        Constants.KEY_SIGN_AGREEMENT -> fragment = SignAgreementFragment()
                        Constants.KEY_ADD_PHOTO_FRAGMENT -> fragment = AddPhotosFragment()
                        Constants.KEY_ADD_DOCUMENTS -> fragment = AddDocumentsFragment()
                        Constants.KEY_WORKING_HOURS -> fragment = WorkingHourFragment()
                        Constants.KEY_LINK_BANK_ACCOUNT -> fragment = LinkBankAccountFragment()
                    }
                }
            }
        }
        supportFragmentManager.beginTransaction().add(R.id.frameLayout, fragment)
            .commit()
    }
    companion object {
        var documents = mutableListOf<Document>()
    }

}