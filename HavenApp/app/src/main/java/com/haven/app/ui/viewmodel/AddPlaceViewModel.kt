package com.haven.app.ui.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.haven.app.data.api.CreatePlaceRequest
import com.haven.app.data.api.HavenApiManager
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.launch
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

}
