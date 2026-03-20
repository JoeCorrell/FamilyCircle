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
import android.os.VibrationEffect
import android.os.Vibrator
import androidx.core.app.NotificationManagerCompat
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
    private var lastDriveNotifTime = 0L
    private var drivingConfirmCount = 0
    private var stoppedConfirmCount = 0
    private var wasDriving = false
    private var lastNotifTimestamp = 0L
    private var lastMessageCount = -1
    private var lastSosState = false

    companion object {
        const val NOTIFICATION_ID = 1001
        const val SOS_NOTIFICATION_ID = 8888
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
                startNotificationPolling()
                startSosPolling()
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

                            // Require 3 consecutive DRIVING readings before confirming drive
                            // and 5 consecutive non-DRIVING readings before confirming stop
                            // Prevents notification spam from GPS speed fluctuations
                            if (status == MemberStatus.DRIVING) {
                                drivingConfirmCount++
                                stoppedConfirmCount = 0
                                if (!wasDriving && drivingConfirmCount >= 3 && now - lastDriveNotifTime > 5 * 60 * 1000) {
                                    wasDriving = true
                                    lastDriveNotifTime = now
                                    notificationHelper.notifyDriveStarted(memberName)
                                }
                            } else {
                                drivingConfirmCount = 0
                                stoppedConfirmCount++
                                if (wasDriving && stoppedConfirmCount >= 5) {
                                    wasDriving = false
                                }
                            }

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

    private fun startSosPolling() {
        serviceScope.launch {
            while (true) {
                try {
                    if (apiManager.isSignedIn) {
                        if (apiManager._sosCleared) {
                            apiManager._sosCleared = false
                            lastSosState = false
                            NotificationManagerCompat.from(this@LocationService).cancel(SOS_NOTIFICATION_ID)
                        }
                        val haven = try {
                            val hid = apiManager.havenId ?: return@launch
                            apiManager.api.getHaven(hid).body()
                        } catch (_: Exception) { null }

                        val sosActive = haven?.activeSos == true
                        val sosBy = haven?.lastSosBy

                        if (sosActive && !lastSosState) {
                            // Find the SOS sender's coordinates
                            val sender = haven?.members?.firstOrNull { it.name == sosBy }
                            val lat = sender?.latitude ?: 0.0
                            val lng = sender?.longitude ?: 0.0

                            // Set the in-app SOS alert state
                            apiManager.sosReceived.value = com.haven.app.data.api.HavenApiManager.SosAlert(
                                senderName = sosBy ?: "Family Member",
                                latitude = lat, longitude = lng
                            )

                            // Launch app via notification
                            val launchIntent = packageManager.getLaunchIntentForPackage(packageName)
                            val pendingIntent = android.app.PendingIntent.getActivity(
                                this@LocationService, 0, launchIntent,
                                android.app.PendingIntent.FLAG_UPDATE_CURRENT or android.app.PendingIntent.FLAG_IMMUTABLE
                            )

                            val sosNotif = NotificationCompat.Builder(this@LocationService, com.haven.app.HavenApp.SOS_CHANNEL_ID)
                                .setSmallIcon(R.drawable.ic_sos)
                                .setContentTitle("SOS ALERT")
                                .setContentText("${sosBy ?: "A family member"} activated SOS! They need help!")
                                .setPriority(NotificationCompat.PRIORITY_MAX)
                                .setCategory(NotificationCompat.CATEGORY_ALARM)
                                .setAutoCancel(false)
                                .setOngoing(true)
                                .setContentIntent(pendingIntent)
                                .setFullScreenIntent(pendingIntent, true)
                                .setSound(android.net.Uri.parse("android.resource://${packageName}/${R.raw.notification}"))
                                .setVibrate(longArrayOf(0, 500, 200, 500, 200, 500, 200, 500))
                                .build()

                            if (ContextCompat.checkSelfPermission(this@LocationService, Manifest.permission.POST_NOTIFICATIONS)
                                == PackageManager.PERMISSION_GRANTED) {
                                NotificationManagerCompat.from(this@LocationService).notify(SOS_NOTIFICATION_ID, sosNotif)
                            }

                            val vibrator = getSystemService(Vibrator::class.java)
                            vibrator?.vibrate(VibrationEffect.createWaveform(longArrayOf(0, 500, 200, 500, 200, 500, 200, 500), -1))
                        } else if (!sosActive && lastSosState) {
                            NotificationManagerCompat.from(this@LocationService).cancel(SOS_NOTIFICATION_ID)
                            apiManager.sosReceived.value = null
                        }
                        lastSosState = sosActive
                    }
                } catch (_: Exception) {}
                delay(3000)
            }
        }
    }

    private fun startNotificationPolling() {
        serviceScope.launch {
            // Initialize with current counts so we don't spam on first load
            var initialized = false
            while (true) {
                try {
                    if (!apiManager.isSignedIn) { delay(5000); continue }

                    // Check for new notifications
                    val notifs = apiManager.observeNotifications()
                    // We can't easily collect a flow here, so use direct API calls
                    val myName = apiManager.getMyMember()?.name ?: "You"
                    val haven = apiManager.getHaven()

                    // Poll messages for new ones
                    if (haven != null) {
                        val messages = try {
                            val resp = apiManager.api.getMessages(haven.id)
                            if (resp.isSuccessful) resp.body() ?: emptyList() else emptyList()
                        } catch (_: Exception) { emptyList() }

                        if (!initialized) {
                            lastMessageCount = messages.size
                            lastNotifTimestamp = System.currentTimeMillis()
                            initialized = true
                        } else if (messages.size > lastMessageCount) {
                            // New messages since last check
                            val newMsgs = messages.drop(lastMessageCount)
                            for (msg in newMsgs) {
                                if (msg.senderUid != apiManager.userId) {
                                    val senderName = msg.senderName
                                    if (msg.text.startsWith("[Errand]")) {
                                        notificationHelper.showLocalNotification(
                                            "Haven: $senderName created an errand",
                                            msg.text.removePrefix("[Errand] ").take(80),
                                            com.haven.app.HavenApp.MESSAGES_CHANNEL_ID
                                        )
                                    } else {
                                        notificationHelper.showLocalNotification(
                                            "Haven: $senderName sent a message",
                                            msg.text.take(80),
                                            com.haven.app.HavenApp.MESSAGES_CHANNEL_ID
                                        )
                                    }
                                }
                            }
                            lastMessageCount = messages.size
                        }

                        // Check for new errands
                        val errands = try {
                            val resp = apiManager.api.getErrands(haven.id)
                            if (resp.isSuccessful) resp.body() ?: emptyList() else emptyList()
                        } catch (_: Exception) { emptyList() }

                        for (errand in errands) {
                            val errandTime = errand.timestamp.toLong()
                            if (errandTime > lastNotifTimestamp && errand.senderUid != apiManager.userId) {
                                if (errand.status == "PENDING") {
                                    notificationHelper.showLocalNotification(
                                        "Haven: ${errand.senderName} needs something",
                                        errand.item + if (errand.address.isNotEmpty()) " at ${errand.address}" else "",
                                        com.haven.app.HavenApp.MESSAGES_CHANNEL_ID
                                    )
                                } else if (errand.status == "ACCEPTED" && errand.acceptedBy != apiManager.userId) {
                                    notificationHelper.showLocalNotification(
                                        "Haven: ${errand.acceptedName} accepted an errand",
                                        errand.item,
                                        com.haven.app.HavenApp.MESSAGES_CHANNEL_ID
                                    )
                                }
                            }
                        }
                        lastNotifTimestamp = System.currentTimeMillis()
                    }

                } catch (_: Exception) {}
                delay(5000) // Check every 5 seconds (faster for SOS)
            }
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
