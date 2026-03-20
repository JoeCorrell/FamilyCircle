package com.haven.app.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.haven.app.data.model.FamilyMember
import com.haven.app.data.model.MemberStatus
import com.haven.app.data.remote.FirestoreManager
import com.haven.app.data.remote.HavenSession
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val firestoreManager: FirestoreManager,
    private val havenSession: HavenSession
) : ViewModel() {

    val members: StateFlow<List<FamilyMember>> = havenSession.havenId
        .filterNotNull()
        .flatMapLatest { havenId -> firestoreManager.observeMembers(havenId) }
        .map { list -> list.map { it.toFamilyMember() } }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val places: StateFlow<List<PlaceData>> = havenSession.havenId
        .filterNotNull()
        .flatMapLatest { havenId -> firestoreManager.observePlaces(havenId) }
        .map { list -> list.map { PlaceData(
            id = it["id"] as? String ?: "",
            name = it["name"] as? String ?: "",
            address = it["address"] as? String ?: "",
            color = (it["color"] as? Number)?.toLong() ?: 0xFFE879A0,
            membersPresent = (it["membersPresent"] as? Number)?.toInt() ?: 0
        )}}
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val recentNotifications: StateFlow<List<NotifData>> = havenSession.havenId
        .filterNotNull()
        .flatMapLatest { havenId -> firestoreManager.observeNotifications(havenId) }
        .map { list -> list.take(3).map { NotifData(
            title = it["title"] as? String ?: "",
            color = (it["color"] as? Number)?.toLong() ?: 0xFF34D399,
            timestamp = (it["timestamp"] as? Number)?.toLong() ?: 0L
        )}}
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Single shared drives flow to avoid duplicate Firestore listeners
    private val drivesFlow: SharedFlow<List<Map<String, Any>>> = havenSession.havenId
        .filterNotNull()
        .flatMapLatest { havenId -> firestoreManager.observeDrives(havenId) }
        .shareIn(viewModelScope, SharingStarted.WhileSubscribed(5000), replay = 1)

    val familyScore: StateFlow<Int> = drivesFlow
        .map { drives ->
            if (drives.isEmpty()) 100
            else drives.mapNotNull { (it["score"] as? Number)?.toInt() }.average().toInt().coerceIn(0, 100)
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 100)

    val drivesCount: StateFlow<Int> = drivesFlow
        .map { it.size }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    val unreadCount: StateFlow<Int> = MutableStateFlow(0)

    val familyName: StateFlow<String> = havenSession.havenName
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), "My Family")

    val placesCount: StateFlow<Int> = places.map { it.size }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    val alertsCount: StateFlow<Int> = recentNotifications.map { it.size }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)
}

data class PlaceData(val id: String, val name: String, val address: String, val color: Long, val membersPresent: Int)
data class NotifData(val title: String, val color: Long, val timestamp: Long) {
    fun timeAgo(): String {
        val diffMs = System.currentTimeMillis() - timestamp
        val minutes = diffMs / 60_000
        return when {
            minutes < 1 -> "Now"
            minutes < 60 -> "${minutes}m"
            else -> "${minutes/60}h"
        }
    }
}

fun Map<String, Any>.toFamilyMember(): FamilyMember = FamilyMember(
    id = (this["uid"] as? String)?.hashCode()?.toLong() ?: 0L,
    name = this["name"] as? String ?: "",
    initials = this["initials"] as? String ?: (this["name"] as? String)?.take(1)?.uppercase() ?: "?",
    color = (this["color"] as? Number)?.toLong() ?: 0xFFE879A0,
    phoneNumber = this["phoneNumber"] as? String ?: "",
    batteryLevel = (this["batteryLevel"] as? Number)?.toInt() ?: 100,
    currentAddress = this["currentAddress"] as? String ?: "",
    latitude = (this["latitude"] as? Number)?.toDouble() ?: 0.0,
    longitude = (this["longitude"] as? Number)?.toDouble() ?: 0.0,
    speed = (this["speed"] as? Number)?.toFloat() ?: 0f,
    lastSeenTimestamp = (this["lastSeenTimestamp"] as? Number)?.toLong() ?: System.currentTimeMillis(),
    status = try { MemberStatus.valueOf(this["status"] as? String ?: "UNKNOWN") } catch (_: Exception) { MemberStatus.UNKNOWN },
    isOnline = this["isOnline"] as? Boolean ?: false,
    photoUrl = this["photoUrl"] as? String ?: "",
    avatarIcon = this["avatarIcon"] as? String ?: "",
    ringPosition = (this["ringPosition"] as? Number)?.toFloat() ?: 0.5f,
    angle = (this["angle"] as? Number)?.toFloat() ?: 0f
)
