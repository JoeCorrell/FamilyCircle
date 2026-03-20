package com.haven.app.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "family_members")
data class FamilyMember(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val initials: String,
    val color: Long, // ARGB color stored as Long
    val phoneNumber: String = "",
    val batteryLevel: Int = 100,
    val currentAddress: String = "",
    val latitude: Double = 0.0,
    val longitude: Double = 0.0,
    val speed: Float = 0f, // mph
    val lastSeenTimestamp: Long = System.currentTimeMillis(),
    val status: MemberStatus = MemberStatus.UNKNOWN,
    val isOnline: Boolean = false,
    val photoUrl: String = "",
    val avatarIcon: String = "",
    // Orbit map positioning
    val ringPosition: Float = 0.5f, // 0-1 distance from center
    val angle: Float = 0f // degrees on orbit
)

enum class MemberStatus {
    HOME, DRIVING, SCHOOL, WALKING, WORK, UNKNOWN;

    fun displayName(): String = name.lowercase().replaceFirstChar { it.uppercase() }
}
