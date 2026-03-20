package com.haven.app.ui.viewmodel

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.location.Geocoder
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.android.gms.location.LocationServices
import com.haven.app.data.api.CreatePlaceRequest
import com.haven.app.data.api.HavenApiManager
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
    private val apiManager: HavenApiManager
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
            try {
                apiManager.createPlace(CreatePlaceRequest(
                    name = name, address = address,
                    latitude = lat, longitude = lng,
                    radiusMeters = radiusMeters,
                    color = placeColors[Random.nextInt(placeColors.size)]
                ))
            } catch (_: Exception) {}
            onComplete()
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
