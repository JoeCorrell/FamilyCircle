package com.haven.app.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "messages")
data class Message(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val senderName: String,
    val senderMemberId: Long = 0, // 0 = current user ("You")
    val text: String,
    val timestamp: Long = System.currentTimeMillis(),
    val isFromCurrentUser: Boolean = false
) {
    fun formattedTime(): String {
        val cal = java.util.Calendar.getInstance().apply { timeInMillis = timestamp }
        val now = System.currentTimeMillis()
        val diffMs = now - timestamp
        return if (diffMs < 60_000) {
            "Now"
        } else {
            val hour = cal.get(java.util.Calendar.HOUR).let { if (it == 0) 12 else it }
            val min = String.format("%02d", cal.get(java.util.Calendar.MINUTE))
            val amPm = if (cal.get(java.util.Calendar.AM_PM) == 0) "a" else "p"
            "$hour:$min$amPm"
        }
    }
}
