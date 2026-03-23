package com.haven.app.service

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.haven.app.HavenApp
import com.haven.app.R
import com.haven.app.data.api.HavenApiManager
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class NotificationHelper @Inject constructor(
    @ApplicationContext private val context: Context,
    private val apiManager: HavenApiManager
) {
    companion object {
        const val COLOR_BATTERY = 0xFFF87171L
        const val COLOR_SPEED = 0xFFFBBF24L
        const val COLOR_ARRIVAL = 0xFF34D399L
        const val COLOR_DEPARTURE = 0xFFA78BFAL
        const val COLOR_DRIVING = 0xFFFB923CL
    }

    suspend fun sendNotification(
        title: String,
        body: String,
        channelId: String = HavenApp.ALERTS_CHANNEL_ID,
        color: Long = COLOR_ARRIVAL
    ) {
        withContext(Dispatchers.IO) {
            try { apiManager.createNotification(title, color) } catch (_: Exception) {}
        }
        showLocalNotification(title, body, channelId)
    }

    fun showLocalNotification(
        title: String,
        body: String,
        channelId: String = HavenApp.ALERTS_CHANNEL_ID
    ) {
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS)
            != PackageManager.PERMISSION_GRANTED
        ) return

        val soundUri = android.net.Uri.parse("android.resource://${context.packageName}/${R.raw.notification}")
        val notifId = (System.currentTimeMillis() % Int.MAX_VALUE).toInt()
        val notification = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle(title)
            .setContentText(body)
            .setSound(soundUri)
            .setPriority(
                when (channelId) {
                    HavenApp.SOS_CHANNEL_ID -> NotificationCompat.PRIORITY_HIGH
                    else -> NotificationCompat.PRIORITY_DEFAULT
                }
            )
            .setAutoCancel(true)
            .build()

        NotificationManagerCompat.from(context).notify(notifId, notification)
    }

    suspend fun notifyBatteryLow(memberName: String, level: Int) {
        sendNotification("$memberName - battery low", "Battery at $level%", HavenApp.ALERTS_CHANNEL_ID, COLOR_BATTERY)
    }

    suspend fun notifySpeedAlert(memberName: String, speedMph: Int) {
        sendNotification("$memberName speed alert", "Driving at ${speedMph}mph", HavenApp.ALERTS_CHANNEL_ID, COLOR_SPEED)
    }

    suspend fun notifyArrival(memberName: String, placeName: String) {
        // Local notification only — each device detects independently, so don't flood server
        showLocalNotification("$memberName arrived", "Arrived at $placeName", HavenApp.GEOFENCE_CHANNEL_ID)
    }

    suspend fun notifyDeparture(memberName: String, placeName: String) {
        showLocalNotification("$memberName left", "Left $placeName", HavenApp.GEOFENCE_CHANNEL_ID)
    }

    suspend fun notifyDriveStarted(memberName: String) {
        sendNotification("$memberName started driving", "Drive in progress", HavenApp.ALERTS_CHANNEL_ID, COLOR_DRIVING)
    }
}
