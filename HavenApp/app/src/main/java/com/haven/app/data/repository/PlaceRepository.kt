package com.haven.app.data.repository

import com.haven.app.data.local.dao.PlaceDao
import com.haven.app.data.model.Place
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PlaceRepository @Inject constructor(
    private val dao: PlaceDao
) {
    fun getAllPlaces(): Flow<List<Place>> = dao.getAllPlaces()

    suspend fun getPlaceById(id: Long): Place? = dao.getPlaceById(id)

    suspend fun getActivePlaces(): List<Place> = dao.getActivePlaces()

    suspend fun addPlace(place: Place): Long = dao.insertPlace(place)

    suspend fun updatePlace(place: Place) = dao.updatePlace(place)

    suspend fun updateMemberCount(id: Long, count: Int) = dao.updateMemberCount(id, count)

    suspend fun deletePlace(place: Place) = dao.deletePlace(place)
}
