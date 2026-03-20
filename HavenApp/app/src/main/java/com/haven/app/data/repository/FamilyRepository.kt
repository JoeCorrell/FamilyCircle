package com.haven.app.data.repository

import com.haven.app.data.local.dao.FamilyMemberDao
import com.haven.app.data.model.FamilyMember
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FamilyRepository @Inject constructor(
    private val dao: FamilyMemberDao
) {
    fun getAllMembers(): Flow<List<FamilyMember>> = dao.getAllMembers()

    fun observeMember(id: Long): Flow<FamilyMember?> = dao.observeMember(id)

    fun getOnlineCount(): Flow<Int> = dao.getOnlineCount()

    suspend fun getMemberById(id: Long): FamilyMember? = dao.getMemberById(id)

    suspend fun addMember(member: FamilyMember): Long = dao.insertMember(member)

    suspend fun updateMember(member: FamilyMember) = dao.updateMember(member)

    suspend fun updateLocation(
        id: Long, lat: Double, lng: Double,
        address: String, timestamp: Long, speed: Float, status: String
    ) = dao.updateLocation(id, lat, lng, address, timestamp, speed, status)

    suspend fun updateBattery(id: Long, level: Int) = dao.updateBattery(id, level)

    suspend fun deleteMember(member: FamilyMember) = dao.deleteMember(member)

    suspend fun deleteById(id: Long) = dao.deleteById(id)
}
