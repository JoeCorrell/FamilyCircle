package com.haven.app.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.haven.app.data.api.HavenApiManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class SavedPlace(
    val id: String, val name: String, val address: String,
    val latitude: Double, val longitude: Double,
    val radiusMeters: Float, val color: Long, val membersPresent: Int
)

@HiltViewModel
class SavedPlacesViewModel @Inject constructor(
    private val apiManager: HavenApiManager
) : ViewModel() {

    val places: StateFlow<List<SavedPlace>> = apiManager.observePlaces()
        .map { list ->
            list.map { SavedPlace(it.id, it.name, it.address, it.latitude, it.longitude, it.radiusMeters, it.color, it.membersPresent) }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun deletePlace(placeId: String) {
        viewModelScope.launch {
            apiManager.deletePlace(placeId)
        }
    }
}
