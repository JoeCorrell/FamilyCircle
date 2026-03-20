package com.haven.app.data.local.dao

import androidx.room.*
import com.haven.app.data.model.FamilyMember
import kotlinx.coroutines.flow.Flow

@Dao
interface FamilyMemberDao {
    @Query("SELECT * FROM family_members ORDER BY name ASC")
    fun getAllMembers(): Flow<List<FamilyMember>>

    @Query("SELECT * FROM family_members WHERE id = :id")
    suspend fun getMemberById(id: Long): FamilyMember?

    @Query("SELECT * FROM family_members WHERE id = :id")
    fun observeMember(id: Long): Flow<FamilyMember?>

    @Query("SELECT COUNT(*) FROM family_members WHERE isOnline = 1")
    fun getOnlineCount(): Flow<Int>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMember(member: FamilyMember): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(members: List<FamilyMember>)

    @Update
    suspend fun updateMember(member: FamilyMember)

    @Query("UPDATE family_members SET latitude = :lat, longitude = :lng, currentAddress = :address, lastSeenTimestamp = :timestamp, speed = :speed, status = :status WHERE id = :id")
    suspend fun updateLocation(id: Long, lat: Double, lng: Double, address: String, timestamp: Long, speed: Float, status: String)

    @Query("UPDATE family_members SET batteryLevel = :level WHERE id = :id")
    suspend fun updateBattery(id: Long, level: Int)

    @Delete
    suspend fun deleteMember(member: FamilyMember)

    @Query("DELETE FROM family_members WHERE id = :id")
    suspend fun deleteById(id: Long)
}
