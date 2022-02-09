package com.au.gonannysingleactivity.services

import android.annotation.SuppressLint
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.widget.RemoteViews
import androidx.core.app.NotificationCompat
import com.au.gonannysingleactivity.R
import com.au.gonannysingleactivity.activities.MainActivity
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage

class ApplicationFirebaseMessagingService : FirebaseMessagingService() {

    override fun onNewToken(p0: String) {
        super.onNewToken(p0)
    }

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        super.onMessageReceived(remoteMessage)
        remoteMessage.apply {
            data.apply {
                if (!isEmpty()) {
                    showNotification(data)
                }
            }

            /*notification.apply {
                if (this != null) {
                }*/
        }
    }

    private fun getBundleFromMap(map: Map<String, Any>): Bundle = Bundle().apply {
        for (i in map)
            putString(i.key, i.value.toString())
    }


    private fun getCustomRemoteViews(title: String, content: String): RemoteViews {
        return RemoteViews(packageName, R.layout.layout_notification).apply {
            setTextViewText(R.id.tvTitle, title)
            setTextViewText(R.id.tvContent, content)
        }
    }

    @SuppressLint("UnspecifiedImmutableFlag")
    private fun showNotification(data: Map<String, Any>) {

        val title = data["subject"].toString()
        val content = data["message"].toString()

        val bundle: Bundle = getBundleFromMap(data)

        //broadcasting message
        sendBroadcast(Intent("firebase_message").apply {
            putExtra("data", bundle)
        })

        // called when app is open
        val intent = Intent(this, MainActivity::class.java).apply {
            putExtra("fromNotification", bundle)
        }

        val channelId = "notification_channel"
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)

        //called when app is close or or ni foreground
        val pendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_ONE_SHOT, bundle)

        val builder = NotificationCompat.Builder(applicationContext, channelId).apply {
            setAutoCancel(true)
            setSmallIcon(R.mipmap.ic_app_logo_round)
            setVibrate(longArrayOf(1000, 1000, 1000, 1000, 1000))
            setOnlyAlertOnce(true)
            setContentIntent(pendingIntent)
        }

        builder.setContent(
            getCustomRemoteViews(title, content)
        )

        val notificationManager = getSystemService(
            Context.NOTIFICATION_SERVICE
        ) as NotificationManager

        if (Build.VERSION.SDK_INT
            >= Build.VERSION_CODES.O
        ) {
            val notificationChannel = NotificationChannel(
                channelId, "web_app",
                NotificationManager.IMPORTANCE_HIGH
            )
            notificationManager.createNotificationChannel(
                notificationChannel
            )
        }
        notificationManager.notify(0, builder.build())
    }
}


