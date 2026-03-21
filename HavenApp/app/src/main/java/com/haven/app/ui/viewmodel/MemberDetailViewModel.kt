package com.haven.app.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.haven.app.data.api.HavenApiManager
import com.haven.app.data.api.LocationHistoryEntry
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MemberDetailViewModel @Inject constructor(
    private val apiManager: HavenApiManager
) : ViewModel() {

    private val _locationHistory = MutableStateFlow<List<LocationHistoryEntry>>(emptyList())
    val locationHistory: StateFlow<List<LocationHistoryEntry>> = _locationHistory.asStateFlow()

    private val _isLoadingHistory = MutableStateFlow(false)
    val isLoadingHistory: StateFlow<Boolean> = _isLoadingHistory.asStateFlow()

    fun loadLocationHistory(memberId: String) {
        viewModelScope.launch {
            _isLoadingHistory.value = true
            _locationHistory.value = apiManager.getLocationHistory(memberId)
            _isLoadingHistory.value = false
        }
    }

    fun sendCheckIn(memberName: String) {
        viewModelScope.launch {
            val myName = apiManager.getMyMember()?.name ?: "Someone"
            apiManager.createNotification("$myName requested a check-in from $memberName", 0xFF38BDF8)
        }
    }
}
