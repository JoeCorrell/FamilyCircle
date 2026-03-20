package com.haven.app.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "drives")
data class Drive(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val memberId: Long,
    val memberName: String,
    val startTime: Long,
    val endTime: Long = 0,
    val fromLocation: String = "",
    val toLocation: String = "",
    val score: Int = 100, // 0-100 safety score
    val distanceMiles: Float = 0f,
    val durationMinutes: Int = 0,
    val topSpeedMph: Int = 0,
    val harshBrakes: Int = 0,
    val hardAccelerations: Int = 0,
    val startLatitude: Double = 0.0,
    val startLongitude: Double = 0.0,
    val endLatitude: Double = 0.0,
    val endLongitude: Double = 0.0
) {
    fun formattedDistance(): String = String.format("%.1fmi", distanceMiles)

    fun formattedDuration(): String {
        return if (durationMinutes >= 60) {
            "${durationMinutes / 60}h ${durationMinutes % 60}m"
        } else {
            "${durationMinutes}m"
        }
    }

    fun formattedDate(): String {
        val now = System.currentTimeMillis()
        val diffMs = now - startTime
        val diffDays = diffMs / (1000 * 60 * 60 * 24)
        return when {
            diffDays == 0L -> {
                val cal = java.util.Calendar.getInstance().apply { timeInMillis = startTime }
                val hour = cal.get(java.util.Calendar.HOUR).let { if (it == 0) 12 else it }
                val min = cal.get(java.util.Calendar.MINUTE)
                val amPm = if (cal.get(java.util.Calendar.AM_PM) == 0) "a" else "p"
                "Today ${hour}:${String.format("%02d", min)}$amPm"
            }
            diffDays == 1L -> "Yesterday"
            diffDays < 7 -> "${diffDays}d ago"
            else -> {
                val cal = java.util.Calendar.getInstance().apply { timeInMillis = startTime }
                "${cal.get(java.util.Calendar.MONTH) + 1}/${cal.get(java.util.Calendar.DAY_OF_MONTH)}"
            }
        }
    }
}
