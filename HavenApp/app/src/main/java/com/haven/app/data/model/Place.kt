package com.haven.app.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "places")
data class Place(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val address: String,
    val latitude: Double,
    val longitude: Double,
    val radiusMeters: Float = 150f,
    val color: Long, // ARGB color
    val membersPresent: Int = 0,
    val geofenceActive: Boolean = true,
    val createdAt: Long = System.currentTimeMillis()
)
