package com.haven.app.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.haven.app.data.api.HavenApiManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import javax.inject.Inject

data class NotificationItem(
    val id: String, val title: String, val color: Long, val timestamp: Long
) {
    fun timeAgo(): String {
        val diffMs = System.currentTimeMillis() - timestamp
        val minutes = diffMs / 60_000
        return when {
            minutes < 1 -> "Just now"
            minutes < 60 -> "${minutes}m ago"
            minutes < 1440 -> "${minutes / 60}h ago"
            else -> "${minutes / 1440}d ago"
        }
    }
}

@HiltViewModel
class NotificationsViewModel @Inject constructor(
    private val apiManager: HavenApiManager
) : ViewModel() {

    val notifications: StateFlow<List<NotificationItem>> = apiManager.observeNotifications()
        .map { list ->
            list.map { NotificationItem(it.id, it.title, it.color, it.timestamp) }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
}
