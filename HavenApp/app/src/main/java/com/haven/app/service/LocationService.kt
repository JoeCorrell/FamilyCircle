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
import com.haven.app.data.api.HavenApiManager
import com.haven.app.data.api.LocationUpdate
import com.haven.app.data.model.MemberStatus
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.*
import kotlinx.coroutines.tasks.await
import java.util.Locale
import javax.inject.Inject

@AndroidEntryPoint
class LocationService : Service() {

    @Inject lateinit var apiManager: HavenApiManager
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
                        if (!apiManager.isSignedIn) return@launch

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
                            apiManager.updateLocation(LocationUpdate(
                                latitude = location.latitude,
                                longitude = location.longitude,
                                speed = speedMph.toDouble(),
                                currentAddress = address,
                                status = status.name,
                                batteryLevel = battery
                            ))

                            val memberName = apiManager.getMyMember()?.name ?: "You"
                            val now = System.currentTimeMillis()

                            if (battery <= 15 && now - lastBatteryAlertTime > 30 * 60 * 1000) {
                                lastBatteryAlertTime = now
                                notificationHelper.notifyBatteryLow(memberName, battery)
                            }

                            if (speedMph > 80 && now - lastSpeedAlertTime > 10 * 60 * 1000) {
                                lastSpeedAlertTime = now
                                notificationHelper.notifySpeedAlert(memberName, speedMph.toInt())
                            }

                            if (status == MemberStatus.DRIVING && !wasDriving) {
                                notificationHelper.notifyDriveStarted(memberName)
                            }
                            wasDriving = status == MemberStatus.DRIVING

                        } catch (_: Exception) {}
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

        serviceScope.launch {
            try {
                val location = fusedLocationClient.lastLocation.await() ?: return@launch
                if (!apiManager.isSignedIn) return@launch
                val bm = getSystemService(Context.BATTERY_SERVICE) as BatteryManager
                val battery = bm.getIntProperty(BatteryManager.BATTERY_PROPERTY_CAPACITY).coerceIn(0, 100)
                val speedMph = location.speed * 2.23694f
                apiManager.updateLocation(LocationUpdate(
                    latitude = location.latitude,
                    longitude = location.longitude,
                    speed = speedMph.toDouble(),
                    currentAddress = "",
                    status = "UNKNOWN",
                    batteryLevel = battery
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
        runBlocking {
            try {
                apiManager.updateMyMember(mapOf("isOnline" to false))
            } catch (_: Exception) {}
        }
        serviceScope.cancel()
    }
}
