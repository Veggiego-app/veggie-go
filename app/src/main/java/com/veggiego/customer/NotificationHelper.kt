package com.veggiego.customer

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat

object NotificationHelper {

    fun showNotification(

        context: Context,

        title: String,

        body: String

    ) {

        val channelId =
            "veggiego_local"

        val manager =

            context.getSystemService(
                Context.NOTIFICATION_SERVICE
            ) as NotificationManager

        // ✅ CHANNEL

        if (
            Build.VERSION.SDK_INT >=
            Build.VERSION_CODES.O
        ) {

            val channel =

                NotificationChannel(

                    channelId,

                    "VeggieGo Local",

                    NotificationManager
                        .IMPORTANCE_HIGH
                )

            manager
                .createNotificationChannel(
                    channel
                )
        }

        // ✅ OPEN APP

        val intent =

            Intent(
                context,
                MainActivity::class.java
            )

        val pendingIntent =

            PendingIntent.getActivity(

                context,

                0,

                intent,

                PendingIntent.FLAG_IMMUTABLE
            )

        // ✅ NOTIFICATION

        val builder =

            NotificationCompat.Builder(

                context,

                channelId

            )

                .setSmallIcon(
                    android.R.drawable.ic_dialog_info
                )

                .setContentTitle(title)

                .setContentText(body)

                .setPriority(
                    NotificationCompat.PRIORITY_HIGH
                )

                .setContentIntent(
                    pendingIntent
                )

                .setAutoCancel(true)

        manager.notify(

            System.currentTimeMillis()
                .toInt(),

            builder.build()
        )
    }
}