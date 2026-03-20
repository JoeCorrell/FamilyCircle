package com.haven.app.data.local.dao

import androidx.room.*
import com.haven.app.data.model.Drive
import kotlinx.coroutines.flow.Flow

@Dao
interface DriveDao {
    @Query("SELECT * FROM drives ORDER BY startTime DESC")
    fun getAllDrives(): Flow<List<Drive>>

    @Query("SELECT * FROM drives ORDER BY startTime DESC LIMIT :limit")
    fun getRecentDrives(limit: Int = 10): Flow<List<Drive>>

    @Query("SELECT * FROM drives WHERE memberId = :memberId ORDER BY startTime DESC")
    fun getDrivesByMember(memberId: Long): Flow<List<Drive>>

    @Query("SELECT * FROM drives WHERE id = :id")
    suspend fun getDriveById(id: Long): Drive?

    @Query("SELECT AVG(score) FROM drives WHERE startTime > :since")
    fun getAverageScore(since: Long): Flow<Float?>

    @Query("SELECT AVG(score) FROM drives WHERE startTime > :since AND startTime <= :until")
    suspend fun getAverageScoreForPeriod(since: Long, until: Long): Float?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDrive(drive: Drive): Long

    @Update
    suspend fun updateDrive(drive: Drive)

    @Delete
    suspend fun deleteDrive(drive: Drive)
}
