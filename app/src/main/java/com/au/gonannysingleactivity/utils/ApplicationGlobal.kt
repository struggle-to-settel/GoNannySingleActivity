package com.au.gonannysingleactivity.utils

import android.app.Application
import android.os.Bundle
import android.util.Log
import com.au.gonannysingleactivity.R
import com.au.gonannysingleactivity.services.ApplicationFirebaseMessagingService
import com.au.gonannysingleactivity.webservices.User
import com.google.android.gms.tasks.OnSuccessListener
import com.google.android.libraries.places.api.Places
import com.google.firebase.iid.FirebaseInstanceIdReceiver
import com.google.firebase.iid.internal.FirebaseInstanceIdInternal
import com.google.firebase.ktx.Firebase
import com.google.firebase.messaging.FirebaseMessaging
import com.google.firebase.messaging.FirebaseMessagingService
import com.stripe.android.PaymentConfiguration

class ApplicationGlobal : Application() {

    companion object {
        lateinit var preferenceManager: PreferenceManager
        var userType = 0
        var accessToken = ""
        var userObject: User? = null
        var isChanged: Boolean = false
        var paymentId: String = ""
        var fcmId:String = ""
        var fromNotification:Boolean = false
        var mapNotification:HashMap<String,Any> = hashMapOf()
    }

    override fun onCreate() {
        super.onCreate()
        Places.initialize(
            this,
            getString(R.string.google_api)
        )
        PaymentConfiguration.init(
            applicationContext,
            getString(R.string.public_key_stripe)
        )


        FirebaseMessaging.getInstance().token.addOnSuccessListener {
            preferenceManager.setFirebaseId(it)
            fcmId = it
        }

        preferenceManager = PreferenceManager(applicationContext)
        userObject = preferenceManager.getUserObject()
        accessToken = preferenceManager.getAccessToken().toString()
        userType = preferenceManager.getUserType()
        Log.d("ApplicationGlobal", "onCreate: $fcmId")
    }
}