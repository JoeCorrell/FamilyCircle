package com.haven.app.data.repository

import com.haven.app.data.local.dao.DriveDao
import com.haven.app.data.model.Drive
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DriveRepository @Inject constructor(
    private val dao: DriveDao
) {
    fun getAllDrives(): Flow<List<Drive>> = dao.getAllDrives()

    fun getRecentDrives(limit: Int = 10): Flow<List<Drive>> = dao.getRecentDrives(limit)

    fun getDrivesByMember(memberId: Long): Flow<List<Drive>> = dao.getDrivesByMember(memberId)

    fun getAverageScore(since: Long): Flow<Float?> = dao.getAverageScore(since)

    suspend fun getAverageScoreForPeriod(since: Long, until: Long): Float? =
        dao.getAverageScoreForPeriod(since, until)

    suspend fun getDriveById(id: Long): Drive? = dao.getDriveById(id)

    suspend fun addDrive(drive: Drive): Long = dao.insertDrive(drive)

    suspend fun updateDrive(drive: Drive) = dao.updateDrive(drive)

    suspend fun deleteDrive(drive: Drive) = dao.deleteDrive(drive)
}
