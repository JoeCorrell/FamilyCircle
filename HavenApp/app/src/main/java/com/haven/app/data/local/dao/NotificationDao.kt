package com.haven.app.data.local.dao

import androidx.room.*
import com.haven.app.data.model.AppNotification
import kotlinx.coroutines.flow.Flow

@Dao
interface NotificationDao {
    @Query("SELECT * FROM notifications ORDER BY timestamp DESC")
    fun getAllNotifications(): Flow<List<AppNotification>>

    @Query("SELECT * FROM notifications ORDER BY timestamp DESC LIMIT :limit")
    fun getRecentNotifications(limit: Int = 20): Flow<List<AppNotification>>

    @Query("SELECT COUNT(*) FROM notifications WHERE read = 0")
    fun getUnreadCount(): Flow<Int>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertNotification(notification: AppNotification): Long

    @Query("UPDATE notifications SET read = 1 WHERE id = :id")
    suspend fun markRead(id: Long)

    @Query("UPDATE notifications SET read = 1")
    suspend fun markAllRead()

    @Delete
    suspend fun deleteNotification(notification: AppNotification)

    @Query("DELETE FROM notifications")
    suspend fun deleteAll()
}
