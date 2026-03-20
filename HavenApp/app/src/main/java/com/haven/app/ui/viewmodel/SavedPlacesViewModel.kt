package com.haven.app.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.haven.app.data.remote.FirestoreManager
import com.haven.app.data.remote.HavenSession
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class SavedPlace(
    val id: String,
    val name: String,
    val address: String,
    val lat: Double,
    val lng: Double,
    val radiusMeters: Float,
    val color: Long
)

@HiltViewModel
class SavedPlacesViewModel @Inject constructor(
    private val firestoreManager: FirestoreManager,
    private val havenSession: HavenSession
) : ViewModel() {

    val places: StateFlow<List<SavedPlace>> = havenSession.havenId
        .filterNotNull()
        .flatMapLatest { havenId -> firestoreManager.observePlaces(havenId) }
        .map { list ->
            list.map { data ->
                SavedPlace(
                    id = data["id"] as? String ?: "",
                    name = data["name"] as? String ?: "",
                    address = data["address"] as? String ?: "",
                    lat = (data["latitude"] as? Number)?.toDouble() ?: 0.0,
                    lng = (data["longitude"] as? Number)?.toDouble() ?: 0.0,
                    radiusMeters = (data["radiusMeters"] as? Number)?.toFloat() ?: 150f,
                    color = (data["color"] as? Number)?.toLong() ?: 0xFF60A5FA
                )
            }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun deletePlace(placeId: String) {
        viewModelScope.launch {
            val havenId = havenSession.havenId.value ?: return@launch
            try {
                firestoreManager.removePlace(havenId, placeId)
            } catch (_: Exception) {}
        }
    }
}
