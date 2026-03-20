package com.haven.app.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.haven.app.data.api.HavenApiManager
import com.haven.app.data.api.toFamilyMember
import com.haven.app.data.model.FamilyMember
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import javax.inject.Inject

data class PlaceMarker(
    val id: String, val name: String,
    val lat: Double, val lng: Double,
    val radiusMeters: Float, val color: Long
)

@HiltViewModel
class MapViewModel @Inject constructor(
    private val apiManager: HavenApiManager
) : ViewModel() {

    val members: StateFlow<List<FamilyMember>> = apiManager.observeMembers()
        .map { list -> list.map { it.toFamilyMember() } }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val places: StateFlow<List<PlaceMarker>> = apiManager.observePlaces()
        .map { list ->
            list.map { PlaceMarker(it.id, it.name, it.latitude, it.longitude, it.radiusMeters, it.color) }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _selectedMember = MutableStateFlow<FamilyMember?>(null)
    val selectedMember: StateFlow<FamilyMember?> = _selectedMember.asStateFlow()

    fun selectMember(member: FamilyMember?) {
        _selectedMember.value = if (member == null || _selectedMember.value?.id == member.id) null else member
    }

    data class HistoryEntry(val address: String, val speed: Float, val status: String, val timestamp: Long)

    val selectedMemberHistory: StateFlow<List<HistoryEntry>> = _selectedMember
        .flatMapLatest { member ->
            if (member == null) flowOf(emptyList())
            else flow {
                val entries = apiManager.getLocationHistory(member.id.toString())
                emit(entries.map { HistoryEntry(it.address, it.speed.toFloat(), it.status, it.timestamp) })
            }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
}
