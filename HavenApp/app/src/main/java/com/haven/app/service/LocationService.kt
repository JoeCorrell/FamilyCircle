package com.haven.app.service

import android.Manifest
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
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
import kotlin.math.sqrt

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
    // Drive tracking state
    private var driveStartTime = 0L
    private var driveStartAddress = ""
    private var driveStartLat = 0.0
    private var driveStartLng = 0.0
    private var driveTopSpeed = 0f
    private var driveHarshBrakes = 0
    private var lastSpeed = 0f
    private var lastNotifTimestamp = 0L
    private var lastMessageCount = -1
    private var lastSosState = false
    private var pollingStarted = false
    private var cachedMemberName: String? = null
    // Track which members are at which places: placeId -> set of member names
    private var memberPlaceState = mutableMapOf<String, MutableSet<String>>()
    // Crash detection
    private var sensorManager: SensorManager? = null
    private var crashSensorActive = false
    private var crashDetectedTime = 0L
    private var crashCountdownJob: Job? = null
    private val CRASH_THRESHOLD_G = 4.0f  // 4G force = typical crash impact
    private val CRASH_COUNTDOWN_MS = 60_000L  // 60 seconds before auto-SOS

    private val crashListener = object : SensorEventListener {
        override fun onSensorChanged(event: SensorEvent) {
            if (event.sensor.type != Sensor.TYPE_ACCELEROMETER) return
            val x = event.values[0]
            val y = event.values[1]
            val z = event.values[2]
            // Total acceleration magnitude (gravity is ~9.8 m/s², so subtract it)
            val magnitude = sqrt(x * x + y * y + z * z) / SensorManager.GRAVITY_EARTH
            if (magnitude > CRASH_THRESHOLD_G && wasDriving && crashDetectedTime == 0L) {
                onCrashDetected()
            }
        }
        override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}
    }

    companion object {
        const val NOTIFICATION_ID = 1001
        const val SOS_NOTIFICATION_ID = 8888
        const val CRASH_NOTIFICATION_ID = 8889
        const val ACTION_START = "com.haven.app.START_LOCATION"
        const val ACTION_STOP = "com.haven.app.STOP_LOCATION"
        const val ACTION_CANCEL_CRASH = "com.haven.app.CANCEL_CRASH"
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
                stopCrashDetection()
                stopLocationUpdates()
                stopForeground(STOP_FOREGROUND_REMOVE)
                stopSelf()
            }
            ACTION_CANCEL_CRASH -> {
                cancelCrashAlert()
            }
            else -> {
                startForegroundNotification()
                startLocationUpdates()
                if (!pollingStarted) {
                    pollingStarted = true
                    startNotificationPolling()
                    startSosPolling()
                }
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

                            if (cachedMemberName == null) {
                                cachedMemberName = apiManager.getMyMember()?.name
                            }
                            val memberName = cachedMemberName ?: "You"
                            val now = System.currentTimeMillis()

                            if (battery <= 15 && now - lastBatteryAlertTime > 30 * 60 * 1000) {
                                lastBatteryAlertTime = now
                                notificationHelper.notifyBatteryLow(memberName, battery)
                            }

                            if (speedMph > 80 && now - lastSpeedAlertTime > 10 * 60 * 1000) {
                                lastSpeedAlertTime = now
                                notificationHelper.notifySpeedAlert(memberName, speedMph.toInt())
                            }

                            // Detect harsh braking (speed drop > 15 mph between updates)
                            if (lastSpeed - speedMph > 15 && wasDriving) {
                                driveHarshBrakes++
                            }
                            lastSpeed = speedMph

                            // Track top speed during drive
                            if (wasDriving && speedMph > driveTopSpeed) {
                                driveTopSpeed = speedMph
                            }

                            // Require 3 consecutive DRIVING readings before confirming drive
                            // and 5 consecutive non-DRIVING readings before confirming stop
                            if (status == MemberStatus.DRIVING) {
                                drivingConfirmCount++
                                stoppedConfirmCount = 0
                                if (!wasDriving && drivingConfirmCount >= 3 && now - lastDriveNotifTime > 5 * 60 * 1000) {
                                    wasDriving = true
                                    lastDriveNotifTime = now
                                    driveStartTime = now
                                    driveStartAddress = address
                                    driveStartLat = location.latitude
                                    driveStartLng = location.longitude
                                    driveTopSpeed = speedMph
                                    driveHarshBrakes = 0
                                    notificationHelper.notifyDriveStarted(memberName)
                                    startCrashDetection()
                                }
                            } else {
                                drivingConfirmCount = 0
                                stoppedConfirmCount++
                                if (wasDriving && stoppedConfirmCount >= 5) {
                                    wasDriving = false
                                    stopCrashDetection()
                                    // Drive ended — record the trip
                                    val endTime = now
                                    val durationMin = ((endTime - driveStartTime) / 60000).toInt()
                                    if (durationMin >= 1) {
                                        val distMiles = haversineMeters(
                                            driveStartLat, driveStartLng,
                                            location.latitude, location.longitude
                                        ).toFloat() / 1609.34f
                                        val score = calculateDriveScore(driveTopSpeed, driveHarshBrakes)
                                        val myMember = apiManager.getMyMember()
                                        apiManager.createDrive(
                                            com.haven.app.data.api.CreateDriveRequest(
                                                memberId = myMember?.id ?: "",
                                                memberName = memberName,
                                                startTime = driveStartTime,
                                                endTime = endTime,
                                                fromLocation = driveStartAddress,
                                                toLocation = address,
                                                distanceMiles = distMiles,
                                                durationMinutes = durationMin,
                                                topSpeedMph = driveTopSpeed,
                                                harshBrakes = driveHarshBrakes,
                                                score = score
                                            )
                                        )
                                    }
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

    // Once we alert for an SOS, don't alert again until it's cleared and a new one happens
    private var hasAlertedForCurrentSos = false

    private fun startSosPolling() {
        serviceScope.launch {
            while (true) {
                try {
                    if (apiManager.isSignedIn) {
                        val hid = apiManager.havenId
                        if (hid != null) {
                            val haven = try { apiManager.api.getHaven(hid).body() } catch (_: Exception) { null }
                            val sosActive = haven?.activeSos == true
                            val sosBy = haven?.lastSosBy
                            val myName = haven?.members?.firstOrNull { it.userId == apiManager.userId }?.name
                            if (sosActive && !hasAlertedForCurrentSos && sosBy != myName) {
                                // New SOS — alert once
                                hasAlertedForCurrentSos = true

                                val sender = haven?.members?.firstOrNull { it.name == sosBy }
                                val lat = sender?.latitude ?: 0.0
                                val lng = sender?.longitude ?: 0.0

                                // Build intent for SosAlertActivity
                                val sosIntent = android.content.Intent(this@LocationService, com.haven.app.SosAlertActivity::class.java).apply {
                                    putExtra("senderName", sosBy ?: "Family Member")
                                    putExtra("latitude", lat)
                                    putExtra("longitude", lng)
                                    addFlags(android.content.Intent.FLAG_ACTIVITY_NEW_TASK or android.content.Intent.FLAG_ACTIVITY_CLEAR_TOP)
                                }
                                val pendingIntent = android.app.PendingIntent.getActivity(
                                    this@LocationService, 0, sosIntent,
                                    android.app.PendingIntent.FLAG_UPDATE_CURRENT or android.app.PendingIntent.FLAG_IMMUTABLE
                                )

                                // Notification with full-screen intent
                                val sosNotif = NotificationCompat.Builder(this@LocationService, com.haven.app.HavenApp.SOS_CHANNEL_ID)
                                    .setSmallIcon(R.drawable.ic_sos)
                                    .setContentTitle("SOS ALERT")
                                    .setContentText("${sosBy ?: "A family member"} needs help!")
                                    .setPriority(NotificationCompat.PRIORITY_MAX)
                                    .setCategory(NotificationCompat.CATEGORY_ALARM)
                                    .setAutoCancel(false)
                                    .setOngoing(true)
                                    .setContentIntent(pendingIntent)
                                    .setFullScreenIntent(pendingIntent, true)
                                    .setSound(android.net.Uri.parse("android.resource://${packageName}/${R.raw.notification}"))
                                    .setVibrate(longArrayOf(0, 800, 400, 800, 400, 800))
                                    .build()

                                if (ContextCompat.checkSelfPermission(this@LocationService, Manifest.permission.POST_NOTIFICATIONS)
                                    == PackageManager.PERMISSION_GRANTED) {
                                    NotificationManagerCompat.from(this@LocationService).notify(SOS_NOTIFICATION_ID, sosNotif)
                                }

                                // Set in-app overlay state
                                apiManager.sosReceived.value = com.haven.app.data.api.HavenApiManager.SosAlert(
                                    senderName = sosBy ?: "Family Member",
                                    latitude = lat, longitude = lng
                                )

                            } else if (!sosActive && lastSosState) {
                                // SOS cleared — reset so next SOS triggers again
                                hasAlertedForCurrentSos = false
                                NotificationManagerCompat.from(this@LocationService).cancel(SOS_NOTIFICATION_ID)
                                apiManager.sosReceived.value = null
                            }
                            lastSosState = sosActive
                        }
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

                        // ── Place arrival/departure detection ──
                        try {
                            val placesResp = apiManager.api.getPlaces(haven.id)
                            val places = if (placesResp.isSuccessful) placesResp.body() ?: emptyList() else emptyList()
                            val membersResp = apiManager.api.getMembers(haven.id)
                            val allMembers = if (membersResp.isSuccessful) membersResp.body() ?: emptyList() else emptyList()

                            for (place in places) {
                                val placeId = place.id
                                val currentAtPlace = mutableSetOf<String>()

                                for (m in allMembers) {
                                    if (m.latitude == 0.0 && m.longitude == 0.0) continue
                                    val dist = haversineMeters(m.latitude, m.longitude, place.latitude, place.longitude)
                                    if (dist <= place.radiusMeters) {
                                        currentAtPlace.add(m.name)
                                    }
                                }

                                val previousAtPlace = memberPlaceState[placeId] ?: mutableSetOf()

                                // Arrivals (in current but not previous)
                                for (name in currentAtPlace) {
                                    if (name !in previousAtPlace) {
                                        val member = allMembers.firstOrNull { it.name == name }
                                        if (member != null && member.userId != apiManager.userId) {
                                            notificationHelper.notifyArrival(name, place.name)
                                        }
                                    }
                                }

                                // Departures (in previous but not current)
                                for (name in previousAtPlace) {
                                    if (name !in currentAtPlace) {
                                        val member = allMembers.firstOrNull { it.name == name }
                                        if (member != null && member.userId != apiManager.userId) {
                                            notificationHelper.notifyDeparture(name, place.name)
                                        }
                                    }
                                }

                                memberPlaceState[placeId] = currentAtPlace
                            }
                        } catch (_: Exception) {}
                    }

                } catch (_: Exception) {}
                delay(5000)
            }
        }
    }

    private fun stopLocationUpdates() {
        fusedLocationClient.removeLocationUpdates(locationCallback)
    }

    private fun startCrashDetection() {
        if (crashSensorActive) return
        sensorManager = getSystemService(Context.SENSOR_SERVICE) as SensorManager
        val accelerometer = sensorManager?.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
        if (accelerometer != null) {
            sensorManager?.registerListener(crashListener, accelerometer, SensorManager.SENSOR_DELAY_UI)
            crashSensorActive = true
        }
    }

    private fun stopCrashDetection() {
        if (!crashSensorActive) return
        sensorManager?.unregisterListener(crashListener)
        crashSensorActive = false
        crashCountdownJob?.cancel()
        crashCountdownJob = null
        crashDetectedTime = 0L
    }

    private fun onCrashDetected() {
        crashDetectedTime = System.currentTimeMillis()
        // Show a high-priority notification with 60s countdown
        // If user doesn't cancel, auto-trigger SOS
        val cancelIntent = Intent(this, LocationService::class.java).apply {
            action = ACTION_CANCEL_CRASH
        }
        val cancelPendingIntent = android.app.PendingIntent.getService(
            this, 99, cancelIntent,
            android.app.PendingIntent.FLAG_UPDATE_CURRENT or android.app.PendingIntent.FLAG_IMMUTABLE
        )

        val crashNotif = NotificationCompat.Builder(this, HavenApp.SOS_CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_sos)
            .setContentTitle("Crash Detected")
            .setContentText("Are you okay? SOS will be sent in 60 seconds")
            .setPriority(NotificationCompat.PRIORITY_MAX)
            .setCategory(NotificationCompat.CATEGORY_ALARM)
            .setAutoCancel(false)
            .setOngoing(true)
            .addAction(0, "I'M OK — CANCEL", cancelPendingIntent)
            .setSound(android.net.Uri.parse("android.resource://$packageName/${R.raw.notification}"))
            .setVibrate(longArrayOf(0, 500, 300, 500, 300, 500, 300, 500))
            .build()

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS)
            == PackageManager.PERMISSION_GRANTED) {
            NotificationManagerCompat.from(this).notify(CRASH_NOTIFICATION_ID, crashNotif)
        }

        // Vibrate the phone
        val vibrator = getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
        vibrator?.vibrate(VibrationEffect.createWaveform(longArrayOf(0, 500, 300, 500, 300, 500), -1))

        // Start 60s countdown — if not cancelled, trigger SOS
        crashCountdownJob = serviceScope.launch {
            delay(CRASH_COUNTDOWN_MS)
            // User didn't cancel — auto-trigger SOS
            val memberName = cachedMemberName ?: "Someone"
            apiManager.activateSos(memberName, lastGeocodeLat, lastGeocodeLng)
            NotificationManagerCompat.from(this@LocationService).cancel(CRASH_NOTIFICATION_ID)
            crashDetectedTime = 0L
        }
    }

    private fun cancelCrashAlert() {
        crashCountdownJob?.cancel()
        crashCountdownJob = null
        crashDetectedTime = 0L
        NotificationManagerCompat.from(this).cancel(CRASH_NOTIFICATION_ID)
    }

    private fun calculateDriveScore(topSpeed: Float, harshBrakes: Int): Int {
        var score = 100
        if (topSpeed > 80) score -= 20 else if (topSpeed > 65) score -= 10
        score -= (harshBrakes * 8).coerceAtMost(40)
        return score.coerceIn(0, 100)
    }

    private fun haversineMeters(lat1: Double, lng1: Double, lat2: Double, lng2: Double): Double {
        val r = 6371000.0 // Earth radius in meters
        val dLat = Math.toRadians(lat2 - lat1)
        val dLng = Math.toRadians(lng2 - lng1)
        val a = Math.sin(dLat / 2) * Math.sin(dLat / 2) +
                Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2)) *
                Math.sin(dLng / 2) * Math.sin(dLng / 2)
        return r * 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a))
    }

    override fun onDestroy() {
        super.onDestroy()
        stopCrashDetection()
        stopLocationUpdates()
        // Mark offline before cancelling scope
        serviceScope.launch {
            try { apiManager.updateMyMember(mapOf("isOnline" to false)) } catch (_: Exception) {}
        }.invokeOnCompletion { serviceScope.cancel() }
    }
}
