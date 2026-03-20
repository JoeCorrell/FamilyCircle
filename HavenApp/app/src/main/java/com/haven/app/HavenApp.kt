package com.haven.app

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.media.AudioAttributes
import android.net.Uri
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class HavenApp : Application() {

    companion object {
        const val LOCATION_CHANNEL_ID = "haven_location"
        const val SOS_CHANNEL_ID = "haven_sos"
        const val ALERTS_CHANNEL_ID = "haven_alerts"
        const val GEOFENCE_CHANNEL_ID = "haven_geofence"
        const val MESSAGES_CHANNEL_ID = "haven_messages"
    }

    override fun onCreate() {
        super.onCreate()
        createNotificationChannels()
    }

    private fun createNotificationChannels() {
        val manager = getSystemService(NotificationManager::class.java)
        val soundUri = Uri.parse("android.resource://$packageName/${R.raw.notification}")
        val audioAttributes = AudioAttributes.Builder()
            .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
            .setUsage(AudioAttributes.USAGE_NOTIFICATION)
            .build()

        val locationChannel = NotificationChannel(
            LOCATION_CHANNEL_ID,
            "Location Tracking",
            NotificationManager.IMPORTANCE_LOW
        ).apply {
            description = "Shows when Haven is tracking your location"
        }

        val sosChannel = NotificationChannel(
            SOS_CHANNEL_ID,
            "Emergency SOS",
            NotificationManager.IMPORTANCE_HIGH
        ).apply {
            description = "Emergency SOS alerts"
            enableVibration(true)
            vibrationPattern = longArrayOf(0, 500, 200, 500, 200, 500)
            setSound(soundUri, audioAttributes)
        }

        val alertsChannel = NotificationChannel(
            ALERTS_CHANNEL_ID,
            "Family Alerts",
            NotificationManager.IMPORTANCE_DEFAULT
        ).apply {
            description = "Arrival, departure, battery, and speed alerts"
            setSound(soundUri, audioAttributes)
        }

        val geofenceChannel = NotificationChannel(
            GEOFENCE_CHANNEL_ID,
            "Place Alerts",
            NotificationManager.IMPORTANCE_DEFAULT
        ).apply {
            description = "Notifications when family members arrive or leave places"
            setSound(soundUri, audioAttributes)
        }

        val messagesChannel = NotificationChannel(
            MESSAGES_CHANNEL_ID,
            "Family Messages",
            NotificationManager.IMPORTANCE_DEFAULT
        ).apply {
            description = "Chat messages and errand requests from family"
            setSound(soundUri, audioAttributes)
        }

        manager.createNotificationChannels(
            listOf(locationChannel, sosChannel, alertsChannel, geofenceChannel, messagesChannel)
        )
    }
}
