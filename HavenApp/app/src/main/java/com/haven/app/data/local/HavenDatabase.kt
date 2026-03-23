package com.haven.app.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.haven.app.data.local.dao.EmergencyContactDao
import com.haven.app.data.model.EmergencyContact

@Database(
    entities = [
        EmergencyContact::class
    ],
    version = 6,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class HavenDatabase : RoomDatabase() {
    abstract fun emergencyContactDao(): EmergencyContactDao
}
