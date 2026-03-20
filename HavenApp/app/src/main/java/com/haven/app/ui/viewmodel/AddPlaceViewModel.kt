package com.haven.app.ui.viewmodel

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.location.Geocoder
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.android.gms.location.LocationServices
import com.haven.app.data.remote.FirestoreManager
import com.haven.app.data.remote.HavenSession
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.util.Locale
import javax.inject.Inject
import kotlin.random.Random

@HiltViewModel
class AddPlaceViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val firestoreManager: FirestoreManager,
    private val havenSession: HavenSession
) : ViewModel() {

    private val placeColors = listOf(
        0xFFE879A0, 0xFF60A5FA, 0xFFA78BFA,
        0xFF34D399, 0xFFFBBF24, 0xFFFB923C
    )

    fun savePlaceWithCoords(
        name: String, address: String,
        lat: Double, lng: Double,
        radiusMeters: Float,
        onComplete: () -> Unit = {}
    ) {
        viewModelScope.launch {
            val havenId = havenSession.havenId.value ?: return@launch
            try {
                firestoreManager.addPlace(havenId, mapOf(
                    "name" to name,
                    "address" to address,
                    "latitude" to lat,
                    "longitude" to lng,
                    "radiusMeters" to radiusMeters,
                    "color" to placeColors[Random.nextInt(placeColors.size)],
                    "membersPresent" to 0,
                    "createdAt" to System.currentTimeMillis()
                ))
            } catch (_: Exception) {}
            onComplete()
        }
    }

    fun savePlace(name: String, address: String, radiusMeters: Float) {
        viewModelScope.launch {
            val havenId = havenSession.havenId.value ?: return@launch

            val coords = try {
                val geocoder = Geocoder(context, Locale.getDefault())
                @Suppress("DEPRECATION")
                val results = geocoder.getFromLocationName(address, 1)
                results?.firstOrNull()?.let { it.latitude to it.longitude }
            } catch (_: Exception) { null }

            val (lat, lng) = coords ?: getCurrentLocation() ?: (0.0 to 0.0)

            try {
            firestoreManager.addPlace(havenId, mapOf(
                "name" to name,
                "address" to address,
                "latitude" to lat,
                "longitude" to lng,
                "radiusMeters" to radiusMeters,
                "color" to placeColors[Random.nextInt(placeColors.size)],
                "membersPresent" to 0,
                "createdAt" to System.currentTimeMillis()
            ))
            } catch (_: Exception) {}
        }
    }

    private suspend fun getCurrentLocation(): Pair<Double, Double>? {
        return try {
            if (ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED
            ) {
                val location = LocationServices.getFusedLocationProviderClient(context).lastLocation.await()
                location?.let { it.latitude to it.longitude }
            } else null
        } catch (_: Exception) { null }
    }
}
