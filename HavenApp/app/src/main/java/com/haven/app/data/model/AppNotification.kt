package com.haven.app.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "notifications")
data class AppNotification(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val title: String,
    val subtitle: String = "",
    val color: Long, // ARGB color
    val timestamp: Long = System.currentTimeMillis(),
    val read: Boolean = false,
    val type: NotificationType = NotificationType.INFO
) {
    fun timeAgo(): String {
        val diffMs = System.currentTimeMillis() - timestamp
        val minutes = diffMs / 60_000
        val hours = minutes / 60
        val days = hours / 24
        return when {
            minutes < 1 -> "Just now"
            minutes < 60 -> "$minutes min ago"
            hours < 24 -> "$hours hr ago"
            days == 1L -> "Yesterday"
            else -> "$days days ago"
        }
    }
}

enum class NotificationType {
    ARRIVAL, DEPARTURE, BATTERY_LOW, SPEED_ALERT, SOS, GEOFENCE, INFO
}
