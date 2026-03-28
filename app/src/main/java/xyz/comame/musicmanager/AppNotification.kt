package xyz.comame.musicmanager

import android.app.NotificationChannel
import android.app.NotificationManager

class AppNotification {
    companion object {
        const val CHANNEL_ID = "download-track"

        fun createNotificationChannel(notificationManager: NotificationManager) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Track Download",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Music track download notifications"
            }
            notificationManager.createNotificationChannel(channel)
        }
    }
}