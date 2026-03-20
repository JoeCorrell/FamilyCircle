package com.haven.app.data.repository

import com.haven.app.data.local.dao.MessageDao
import com.haven.app.data.model.Message
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MessageRepository @Inject constructor(
    private val dao: MessageDao
) {
    fun getAllMessages(): Flow<List<Message>> = dao.getAllMessages()

    fun getRecentMessages(limit: Int = 50): Flow<List<Message>> = dao.getRecentMessages(limit)

    suspend fun sendMessage(message: Message): Long = dao.insertMessage(message)

    suspend fun deleteMessage(message: Message) = dao.deleteMessage(message)

    suspend fun clearAll() = dao.deleteAllMessages()
}
