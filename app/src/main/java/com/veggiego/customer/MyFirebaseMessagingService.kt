package com.veggiego.customer

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class MyFirebaseMessagingService :
    FirebaseMessagingService() {
// ✅ SAVE TOKEN

    override fun onNewToken(
        token: String
    ) {

        super.onNewToken(token)
        android.util.Log.d(
            "FCM_TOKEN",
            token
        )

        val uid =

            FirebaseAuth
                .getInstance()
                .currentUser
                ?.uid ?: return

        FirebaseFirestore
            .getInstance()

            .collection("users")

            .document(uid)

            .set(

                mapOf(
                    "fcmToken" to token
                ),

                com.google.firebase.firestore.SetOptions.merge()
            )
    }
    override fun onMessageReceived(
        message: RemoteMessage
    ) {

        super.onMessageReceived(message)
        android.util.Log.d(
            "FCM_DEBUG",
            "MESSAGE RECEIVED"
        )
        val title =

            message.notification?.title
                ?: message.data["title"]
                ?: "VeggieGo"

        val body =

            message.notification?.body
                ?: message.data["body"]
                ?: "New Update"

        showNotification(
            title,
            body
        )
    }

    private fun showNotification(
        title: String,
        body: String
    ) {

        val channelId =
            "veggiego_channel"

        val manager =

            getSystemService(
                Context.NOTIFICATION_SERVICE
            ) as NotificationManager

        if (

            Build.VERSION.SDK_INT
            >= Build.VERSION_CODES.O

        ) {

            val channel =

                NotificationChannel(

                    channelId,

                    "VeggieGo Notifications",

                    NotificationManager.IMPORTANCE_HIGH
                )

            manager.createNotificationChannel(
                channel
            )
        }

        val intent =

            Intent(
                this,
                MainActivity::class.java
            )

        val pendingIntent =

            PendingIntent.getActivity(

                this,

                0,

                intent,

                PendingIntent.FLAG_IMMUTABLE
            )

        val notification =

            NotificationCompat.Builder(
                this,
                channelId
            )

                .setContentTitle(title)

                .setContentText(body)

                .setSmallIcon(
                    R.drawable.ic_notification
                )

                .setDefaults(
                    NotificationCompat.DEFAULT_ALL
                )

                .setCategory(
                    NotificationCompat.CATEGORY_MESSAGE
                )

                .setVisibility(
                    NotificationCompat.VISIBILITY_PUBLIC
                )

                .setAutoCancel(true)

                .setContentIntent(
                    pendingIntent
                )

                .setPriority(
                    NotificationCompat.PRIORITY_HIGH
                )

                .setDefaults(
                    NotificationCompat.DEFAULT_ALL
                )
                .build()

        manager.notify(

            System.currentTimeMillis().toInt(),

            notification
        )
    }
}