package com.haven.app.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.haven.app.data.api.HavenApiManager
import com.haven.app.data.api.HavenInfo
import com.haven.app.data.api.toFamilyMember
import com.haven.app.data.model.FamilyMember
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val apiManager: HavenApiManager
) : ViewModel() {

    val members: StateFlow<List<FamilyMember>> = apiManager.observeMembers()
        .map { list -> list.map { it.toFamilyMember() } }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val places: StateFlow<List<PlaceData>> = apiManager.observePlaces()
        .map { list -> list.map { PlaceData(it.id, it.name, it.address, it.color, it.membersPresent) } }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val recentNotifications: StateFlow<List<NotifData>> = apiManager.observeNotifications()
        .map { list -> list.take(3).map { NotifData(it.title, it.color, it.timestamp) } }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val familyScore: StateFlow<Int> = MutableStateFlow(100)
    val drivesCount: StateFlow<Int> = MutableStateFlow(0)
    val unreadCount: StateFlow<Int> = MutableStateFlow(0)

    val familyName: StateFlow<String> = apiManager.observeMembers()
        .map {
            val haven = apiManager.getHaven()
            haven?.name ?: "My Family"
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), "My Family")

    val placesCount: StateFlow<Int> = places.map { it.size }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    val alertsCount: StateFlow<Int> = recentNotifications.map { it.size }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    private val _myHavens = MutableStateFlow<List<HavenInfo>>(emptyList())
    val myHavens: StateFlow<List<HavenInfo>> = _myHavens.asStateFlow()

    val activeHavenId: String? get() = apiManager.havenId

    init {
        viewModelScope.launch {
            _myHavens.value = apiManager.getMyHavens()
        }
    }

    fun switchHaven(havenId: String) {
        viewModelScope.launch {
            apiManager.switchHaven(havenId)
            _myHavens.value = apiManager.getMyHavens()
        }
    }
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
