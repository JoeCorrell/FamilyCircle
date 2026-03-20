package com.haven.app.service

import android.Manifest
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Geocoder
import android.os.BatteryManager
import android.os.IBinder
import android.os.Looper
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import com.google.android.gms.location.*
import com.haven.app.HavenApp
import com.haven.app.R
import com.haven.app.data.model.MemberStatus
import com.haven.app.data.remote.FirebaseAuthManager
import com.haven.app.data.remote.FirestoreManager
import com.haven.app.data.remote.HavenSession
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.tasks.await
import java.util.Locale
import javax.inject.Inject

@AndroidEntryPoint
class LocationService : Service() {

    @Inject lateinit var firestoreManager: FirestoreManager
    @Inject lateinit var authManager: FirebaseAuthManager
    @Inject lateinit var havenSession: HavenSession
    @Inject lateinit var notificationHelper: NotificationHelper

    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var locationCallback: LocationCallback
    private val serviceScope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    private var lastGeocodeLat = 0.0
    private var lastGeocodeLng = 0.0
    private var lastAddress = "Unknown"
    private var lastBatteryAlertTime = 0L
    private var lastSpeedAlertTime = 0L
    private var wasDriving = false

    companion object {
        const val NOTIFICATION_ID = 1001
        const val ACTION_START = "com.haven.app.START_LOCATION"
        const val ACTION_STOP = "com.haven.app.STOP_LOCATION"
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onCreate() {
        super.onCreate()
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        setupLocationCallback()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_STOP -> {
                stopLocationUpdates()
                stopForeground(STOP_FOREGROUND_REMOVE)
                stopSelf()
            }
            else -> {
                startForegroundNotification()
                startLocationUpdates()
            }
        }
        return START_STICKY
    }

    private fun startForegroundNotification() {
        val notification = NotificationCompat.Builder(this, HavenApp.LOCATION_CHANNEL_ID)
            .setContentTitle("Haven is active")
            .setContentText("Sharing your location with family")
            .setSmallIcon(R.drawable.ic_location)
            .setOngoing(true)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .build()
        startForeground(NOTIFICATION_ID, notification)
    }

    private fun setupLocationCallback() {
        locationCallback = object : LocationCallback() {
            override fun onLocationResult(result: LocationResult) {
                result.lastLocation?.let { location ->
                    serviceScope.launch {
                        val userId = authManager.userId ?: return@launch
                        val havenId = havenSession.havenId.value ?: return@launch

                        // Only re-geocode if moved >100m from last geocode
                        val movedFar = Math.abs(location.latitude - lastGeocodeLat) > 0.001 ||
                            Math.abs(location.longitude - lastGeocodeLng) > 0.001
                        val address = if (movedFar) {
                            try {
                                val geocoder = Geocoder(this@LocationService, Locale.getDefault())
                                @Suppress("DEPRECATION")
                                val results = geocoder.getFromLocation(location.latitude, location.longitude, 1)
                                val addr = results?.firstOrNull()?.let { a ->
                                    a.thoroughfare ?: a.subLocality ?: a.locality ?: "Unknown"
                                } ?: "Unknown"
                                lastGeocodeLat = location.latitude
                                lastGeocodeLng = location.longitude
                                lastAddress = addr
                                addr
                            } catch (_: Exception) { lastAddress }
                        } else lastAddress

                        val speedMph = location.speed * 2.23694f
                        val status = when {
                            speedMph > 15 -> MemberStatus.DRIVING
                            speedMph > 2 -> MemberStatus.WALKING
                            else -> MemberStatus.HOME
                        }

                        val bm = getSystemService(Context.BATTERY_SERVICE) as BatteryManager
                        val battery = bm.getIntProperty(BatteryManager.BATTERY_PROPERTY_CAPACITY).coerceIn(0, 100)

                        try {
                            // Log location history when address changes
                            if (movedFar && address != "Unknown") {
                                firestoreManager.addLocationHistory(havenId, userId, mapOf(
                                    "address" to address,
                                    "latitude" to location.latitude,
                                    "longitude" to location.longitude,
                                    "speed" to speedMph,
                                    "status" to status.name,
                                    "timestamp" to System.currentTimeMillis()
                                ))
                            }

                            firestoreManager.updateMemberFields(havenId, userId, mapOf(
                                "latitude" to location.latitude,
                                "longitude" to location.longitude,
                                "currentAddress" to address,
                                "lastSeenTimestamp" to System.currentTimeMillis(),
                                "speed" to speedMph,
                                "status" to status.name,
                                "isOnline" to true,
                                "batteryLevel" to battery
                            ))

                            // Get member name for notifications
                            val memberName = try {
                                val members = firestoreManager.observeMembers(havenId).first()
                                (members.firstOrNull { it["uid"] == userId }?.get("name") as? String) ?: "You"
                            } catch (_: Exception) { "You" }

                            val now = System.currentTimeMillis()

                            // Battery low alert (once every 30 min)
                            if (battery <= 15 && now - lastBatteryAlertTime > 30 * 60 * 1000) {
                                lastBatteryAlertTime = now
                                notificationHelper.notifyBatteryLow(memberName, battery)
                            }

                            // Speed alert (once every 10 min, over 80mph)
                            if (speedMph > 80 && now - lastSpeedAlertTime > 10 * 60 * 1000) {
                                lastSpeedAlertTime = now
                                notificationHelper.notifySpeedAlert(memberName, speedMph.toInt())
                            }

                            // Drive started notification
                            if (status == MemberStatus.DRIVING && !wasDriving) {
                                notificationHelper.notifyDriveStarted(memberName)
                            }
                            wasDriving = status == MemberStatus.DRIVING

                        } catch (_: Exception) { /* offline — Firestore will sync later */ }
                    }
                }
            }
        }
    }

    private fun startLocationUpdates() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
            != PackageManager.PERMISSION_GRANTED
        ) return

        val request = LocationRequest.Builder(Priority.PRIORITY_BALANCED_POWER_ACCURACY, 30_000L)
            .setMinUpdateIntervalMillis(10_000L)
            .setMinUpdateDistanceMeters(5f)
            .setMaxUpdateDelayMillis(45_000L)
            .build()

        fusedLocationClient.requestLocationUpdates(request, locationCallback, Looper.getMainLooper())

        // Force an immediate update so data shows right away
        serviceScope.launch {
            try {
                val location = fusedLocationClient.lastLocation.await() ?: return@launch
                val userId = authManager.userId ?: return@launch
                val havenId = havenSession.havenId.value ?: return@launch
                val bm = getSystemService(Context.BATTERY_SERVICE) as BatteryManager
                val battery = bm.getIntProperty(BatteryManager.BATTERY_PROPERTY_CAPACITY).coerceIn(0, 100)
                val speedMph = location.speed * 2.23694f
                firestoreManager.updateMemberFields(havenId, userId, mapOf(
                    "latitude" to location.latitude,
                    "longitude" to location.longitude,
                    "lastSeenTimestamp" to System.currentTimeMillis(),
                    "speed" to speedMph,
                    "isOnline" to true,
                    "batteryLevel" to battery
                ))
            } catch (_: Exception) {}
        }
    }

    private fun stopLocationUpdates() {
        fusedLocationClient.removeLocationUpdates(locationCallback)
    }

    override fun onDestroy() {
        super.onDestroy()
        stopLocationUpdates()
        // Mark member as offline before cancelling scope
        kotlinx.coroutines.runBlocking {
            try {
                val userId = authManager.userId ?: return@runBlocking
                val havenId = havenSession.havenId.value ?: return@runBlocking
                firestoreManager.updateMemberFields(havenId, userId, mapOf("isOnline" to false))
            } catch (_: Exception) {}
        }
        serviceScope.cancel()
    }
}
