package com.haven.app.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.haven.app.data.model.FamilyMember
import com.haven.app.data.remote.FirestoreManager
import com.haven.app.data.remote.HavenSession
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
    private val firestoreManager: FirestoreManager,
    private val havenSession: HavenSession
) : ViewModel() {

    // Single shared members flow
    private val sharedMembers: SharedFlow<List<Map<String, Any>>> = havenSession.havenId
        .filterNotNull()
        .flatMapLatest { havenId -> firestoreManager.observeMembers(havenId) }
        .shareIn(viewModelScope, SharingStarted.WhileSubscribed(5000), replay = 1)

    val members: StateFlow<List<FamilyMember>> = sharedMembers
        .map { list -> list.map { it.toFamilyMember() } }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val places: StateFlow<List<PlaceMarker>> = havenSession.havenId
        .filterNotNull()
        .flatMapLatest { havenId -> firestoreManager.observePlaces(havenId) }
        .map { list ->
            list.map { data ->
                PlaceMarker(
                    id = data["id"] as? String ?: "",
                    name = data["name"] as? String ?: "",
                    lat = (data["latitude"] as? Number)?.toDouble() ?: 0.0,
                    lng = (data["longitude"] as? Number)?.toDouble() ?: 0.0,
                    radiusMeters = (data["radiusMeters"] as? Number)?.toFloat() ?: 150f,
                    color = (data["color"] as? Number)?.toLong() ?: 0xFF60A5FA
                )
            }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val familyName: StateFlow<String> = havenSession.havenName
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), "My Family")

    private val _selectedMember = MutableStateFlow<FamilyMember?>(null)
    val selectedMember: StateFlow<FamilyMember?> = _selectedMember.asStateFlow()

    fun selectMember(member: FamilyMember?) {
        _selectedMember.value = if (member == null || _selectedMember.value?.id == member.id) null else member
    }

    data class HistoryEntry(val address: String, val speed: Float, val status: String, val timestamp: Long)

    val selectedMemberHistory: StateFlow<List<HistoryEntry>> = _selectedMember
        .combine(havenSession.havenId) { member, havenId -> member to havenId }
        .flatMapLatest { (member, havenId) ->
            if (member == null || havenId == null) {
                kotlinx.coroutines.flow.flowOf(emptyList())
            } else {
                sharedMembers.flatMapLatest { members ->
                    val uid = members.firstOrNull {
                        (it["name"] as? String) == member.name
                    }?.get("uid") as? String
                    if (uid != null) {
                        firestoreManager.observeLocationHistory(havenId, uid).map { list ->
                            list.map { data ->
                                HistoryEntry(
                                    address = data["address"] as? String ?: "Unknown",
                                    speed = (data["speed"] as? Number)?.toFloat() ?: 0f,
                                    status = data["status"] as? String ?: "UNKNOWN",
                                    timestamp = (data["timestamp"] as? Number)?.toLong() ?: 0L
                                )
                            }
                        }
                    } else {
                        kotlinx.coroutines.flow.flowOf(emptyList())
                    }
                }
            }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
}
