package com.haven.app.service

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.os.VibrationEffect
import android.os.Vibrator
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.google.android.gms.location.LocationServices
import com.haven.app.HavenApp
import com.haven.app.R
import com.haven.app.data.api.HavenApiManager
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SosService @Inject constructor(
    @ApplicationContext private val context: Context,
    private val apiManager: HavenApiManager
) {
    suspend fun activateSos() {
        val location = getCurrentLocation()
        vibrateDevice()

        val myMember = apiManager.getMyMember()
        val senderName = myMember?.name ?: "Family Member"

        apiManager.activateSos(
            senderName = senderName,
            lat = location?.latitude ?: 0.0,
            lng = location?.longitude ?: 0.0
        )

        showLocalNotification()
    }

    private suspend fun getCurrentLocation(): Location? {
        return try {
            if (ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED
            ) {
                LocationServices.getFusedLocationProviderClient(context).lastLocation.await()
            } else null
        } catch (_: Exception) { null }
    }

    private fun vibrateDevice() {
        val vibrator = context.getSystemService(Vibrator::class.java)
        vibrator?.vibrate(
            VibrationEffect.createWaveform(longArrayOf(0, 500, 200, 500, 200, 500), -1)
        )
    }

    private fun showLocalNotification() {
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS)
            != PackageManager.PERMISSION_GRANTED
        ) return

        val notification = NotificationCompat.Builder(context, HavenApp.SOS_CHANNEL_ID)
            .setContentTitle("SOS Alert Active")
            .setContentText("All Haven members have been notified")
            .setSmallIcon(R.drawable.ic_sos)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .build()

        NotificationManagerCompat.from(context).notify(9999, notification)
    }
}
