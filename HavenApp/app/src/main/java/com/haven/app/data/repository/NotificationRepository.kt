package com.haven.app.data.repository

import com.haven.app.data.local.dao.NotificationDao
import com.haven.app.data.model.AppNotification
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class NotificationRepository @Inject constructor(
    private val dao: NotificationDao
) {
    fun getAllNotifications(): Flow<List<AppNotification>> = dao.getAllNotifications()

    fun getRecentNotifications(limit: Int = 20): Flow<List<AppNotification>> =
        dao.getRecentNotifications(limit)

    fun getUnreadCount(): Flow<Int> = dao.getUnreadCount()

    suspend fun addNotification(notification: AppNotification): Long =
        dao.insertNotification(notification)

    suspend fun markRead(id: Long) = dao.markRead(id)

    suspend fun markAllRead() = dao.markAllRead()

    suspend fun deleteNotification(notification: AppNotification) =
        dao.deleteNotification(notification)

    suspend fun deleteAll() = dao.deleteAll()
}
