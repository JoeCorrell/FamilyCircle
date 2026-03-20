package com.haven.app.service

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.haven.app.HavenApp
import com.haven.app.R
import com.haven.app.data.remote.FirestoreManager
import com.haven.app.data.remote.HavenSession
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class NotificationHelper @Inject constructor(
    @ApplicationContext private val context: Context,
    private val firestoreManager: FirestoreManager,
    private val havenSession: HavenSession
) {
    companion object {
        // Color constants for notification types
        const val COLOR_MESSAGE = 0xFF38BDF8L    // blue
        const val COLOR_ERRAND = 0xFFFBBF24L     // amber
        const val COLOR_BATTERY = 0xFFF87171L    // red
        const val COLOR_SPEED = 0xFFFBBF24L      // amber
        const val COLOR_ARRIVAL = 0xFF34D399L    // green
        const val COLOR_DEPARTURE = 0xFFA78BFAL  // purple
        const val COLOR_CHECKIN = 0xFF38BDF8L    // blue
        const val COLOR_GEOFENCE = 0xFF60A5FAL   // blue
        const val COLOR_DRIVING = 0xFFFB923CL    // orange
    }

    /** Show a local Android notification AND persist to Firestore */
    suspend fun sendNotification(
        title: String,
        body: String,
        channelId: String = HavenApp.ALERTS_CHANNEL_ID,
        color: Long = COLOR_MESSAGE,
        type: String = "INFO"
    ) {
        // Persist to Firestore so all family members see it
        val havenId = havenSession.havenId.value ?: return
        withContext(Dispatchers.IO) {
            try {
                firestoreManager.addNotification(havenId, mapOf(
                    "title" to title,
                    "subtitle" to body,
                    "color" to color,
                    "timestamp" to System.currentTimeMillis(),
                    "type" to type
                ))
            } catch (_: Exception) {}
        }

        // Show local Android notification
        showLocalNotification(title, body, channelId)
    }

    /** Show only a local Android notification (no Firestore) */
    fun showLocalNotification(
        title: String,
        body: String,
        channelId: String = HavenApp.ALERTS_CHANNEL_ID
    ) {
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS)
            != PackageManager.PERMISSION_GRANTED
        ) return

        val notifId = (System.currentTimeMillis() % Int.MAX_VALUE).toInt()
        val notification = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle(title)
            .setContentText(body)
            .setPriority(
                when (channelId) {
                    HavenApp.SOS_CHANNEL_ID -> NotificationCompat.PRIORITY_HIGH
                    HavenApp.MESSAGES_CHANNEL_ID -> NotificationCompat.PRIORITY_DEFAULT
                    else -> NotificationCompat.PRIORITY_DEFAULT
                }
            )
            .setAutoCancel(true)
            .build()

        NotificationManagerCompat.from(context).notify(notifId, notification)
    }

    // ── Convenience methods for specific notification types ──

    suspend fun notifyMessage(senderName: String, messageText: String) {
        sendNotification(
            title = "$senderName sent a message",
            body = messageText.take(100),
            channelId = HavenApp.MESSAGES_CHANNEL_ID,
            color = COLOR_MESSAGE,
            type = "MESSAGE"
        )
    }

    suspend fun notifyErrand(senderName: String, item: String) {
        sendNotification(
            title = "$senderName needs something",
            body = "Errand: $item",
            channelId = HavenApp.MESSAGES_CHANNEL_ID,
            color = COLOR_ERRAND,
            type = "ERRAND"
        )
    }

    suspend fun notifyBatteryLow(memberName: String, level: Int) {
        sendNotification(
            title = "$memberName - battery low",
            body = "Battery at $level%. They may go offline soon.",
            channelId = HavenApp.ALERTS_CHANNEL_ID,
            color = COLOR_BATTERY,
            type = "BATTERY_LOW"
        )
    }

    suspend fun notifySpeedAlert(memberName: String, speedMph: Int) {
        sendNotification(
            title = "$memberName speed alert",
            body = "Currently driving at ${speedMph}mph",
            channelId = HavenApp.ALERTS_CHANNEL_ID,
            color = COLOR_SPEED,
            type = "SPEED_ALERT"
        )
    }

    suspend fun notifyArrival(memberName: String, placeName: String) {
        sendNotification(
            title = "$memberName arrived",
            body = "Arrived at $placeName",
            channelId = HavenApp.GEOFENCE_CHANNEL_ID,
            color = COLOR_ARRIVAL,
            type = "ARRIVAL"
        )
    }

    suspend fun notifyDeparture(memberName: String, placeName: String) {
        sendNotification(
            title = "$memberName left",
            body = "Left $placeName",
            channelId = HavenApp.GEOFENCE_CHANNEL_ID,
            color = COLOR_DEPARTURE,
            type = "DEPARTURE"
        )
    }

    suspend fun notifyCheckIn(requesterName: String, targetName: String) {
        sendNotification(
            title = "Check-in requested",
            body = "$requesterName requested a check-in for $targetName",
            channelId = HavenApp.ALERTS_CHANNEL_ID,
            color = COLOR_CHECKIN,
            type = "CHECKIN"
        )
    }

    suspend fun notifyDriveStarted(memberName: String) {
        sendNotification(
            title = "$memberName started driving",
            body = "Drive in progress",
            channelId = HavenApp.ALERTS_CHANNEL_ID,
            color = COLOR_DRIVING,
            type = "DRIVE_START"
        )
    }

    suspend fun notifyMemberOnline(memberName: String) {
        sendNotification(
            title = "$memberName is online",
            body = "Location sharing is active",
            channelId = HavenApp.ALERTS_CHANNEL_ID,
            color = COLOR_ARRIVAL,
            type = "ONLINE"
        )
    }
}
