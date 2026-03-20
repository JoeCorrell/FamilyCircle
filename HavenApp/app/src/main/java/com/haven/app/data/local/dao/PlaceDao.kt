package com.haven.app.data.local.dao

import androidx.room.*
import com.haven.app.data.model.Place
import kotlinx.coroutines.flow.Flow

@Dao
interface PlaceDao {
    @Query("SELECT * FROM places ORDER BY name ASC")
    fun getAllPlaces(): Flow<List<Place>>

    @Query("SELECT * FROM places WHERE id = :id")
    suspend fun getPlaceById(id: Long): Place?

    @Query("SELECT * FROM places WHERE geofenceActive = 1")
    suspend fun getActivePlaces(): List<Place>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPlace(place: Place): Long

    @Update
    suspend fun updatePlace(place: Place)

    @Query("UPDATE places SET membersPresent = :count WHERE id = :id")
    suspend fun updateMemberCount(id: Long, count: Int)

    @Delete
    suspend fun deletePlace(place: Place)
}
