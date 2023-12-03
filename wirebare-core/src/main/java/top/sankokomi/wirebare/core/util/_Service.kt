package top.sankokomi.wirebare.core.util

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.os.Build
import androidx.core.content.getSystemService

internal fun Service.defaultNotification(chanelId: String): Notification {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        getSystemService<NotificationManager>()?.let {
            if (it.getNotificationChannel(chanelId) == null) {
                it.createNotificationChannel(
                    NotificationChannel(
                        chanelId,
                        chanelId,
                        NotificationManager.IMPORTANCE_DEFAULT
                    )
                )
            }
        }
    }
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        Notification.Builder(this, chanelId)
    } else {
        @Suppress("DEPRECATION")
        Notification.Builder(this)
    }.setContentTitle(chanelId).setContentText(chanelId).build()
}