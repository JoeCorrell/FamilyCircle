package com.haven.app.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.haven.app.data.remote.FirestoreManager
import com.haven.app.data.remote.HavenSession
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import javax.inject.Inject

data class NotificationItem(
    val title: String,
    val color: Long,
    val timestamp: Long
) {
    fun timeAgo(): String {
        val diffMs = System.currentTimeMillis() - timestamp
        val minutes = diffMs / 60_000
        val hours = minutes / 60
        return when {
            minutes < 1 -> "Just now"
            minutes < 60 -> "$minutes min ago"
            hours < 24 -> "$hours hr ago"
            else -> "${hours / 24}d ago"
        }
    }
}

@HiltViewModel
class NotificationsViewModel @Inject constructor(
    firestoreManager: FirestoreManager,
    havenSession: HavenSession
) : ViewModel() {

    val notifications: StateFlow<List<NotificationItem>> = havenSession.havenId
        .filterNotNull()
        .flatMapLatest { havenId -> firestoreManager.observeNotifications(havenId) }
        .map { list ->
            list.map { data ->
                NotificationItem(
                    title = data["title"] as? String ?: "",
                    color = (data["color"] as? Number)?.toLong() ?: 0xFF34D399,
                    timestamp = (data["timestamp"] as? Number)?.toLong() ?: 0L
                )
            }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
}
