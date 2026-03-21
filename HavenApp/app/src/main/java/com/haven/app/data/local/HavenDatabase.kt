package com.haven.app.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.haven.app.data.local.dao.EmergencyContactDao
import com.haven.app.data.model.*

@Database(
    entities = [
        FamilyMember::class,
        Place::class,
        Drive::class,
        Message::class,
        AppNotification::class,
        EmergencyContact::class
    ],
    version = 5,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class HavenDatabase : RoomDatabase() {
    abstract fun emergencyContactDao(): EmergencyContactDao
}
