package com.haven.app.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.haven.app.data.model.Drive
import com.haven.app.data.remote.FirestoreManager
import com.haven.app.data.remote.HavenSession
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import javax.inject.Inject

@HiltViewModel
class SafetyViewModel @Inject constructor(
    firestoreManager: FirestoreManager,
    havenSession: HavenSession
) : ViewModel() {

    val drives: StateFlow<List<Drive>> = havenSession.havenId
        .filterNotNull()
        .flatMapLatest { havenId -> firestoreManager.observeDrives(havenId) }
        .map { list ->
            list.map { data ->
                Drive(
                    id = (data["id"] as? String)?.hashCode()?.toLong() ?: 0L,
                    memberId = 0,
                    memberName = data["memberName"] as? String ?: "",
                    startTime = (data["startTime"] as? Number)?.toLong() ?: 0L,
                    endTime = (data["endTime"] as? Number)?.toLong() ?: 0L,
                    fromLocation = data["fromLocation"] as? String ?: "",
                    toLocation = data["toLocation"] as? String ?: "",
                    score = (data["score"] as? Number)?.toInt() ?: 0,
                    distanceMiles = (data["distanceMiles"] as? Number)?.toFloat() ?: 0f,
                    durationMinutes = (data["durationMinutes"] as? Number)?.toInt() ?: 0,
                    topSpeedMph = (data["topSpeedMph"] as? Number)?.toInt() ?: 0,
                    harshBrakes = (data["harshBrakes"] as? Number)?.toInt() ?: 0,
                )
            }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val familyScore: StateFlow<Int> = drives
        .map { list -> if (list.isEmpty()) 0 else list.map { it.score }.average().toInt() }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    val memberCount: StateFlow<Int> = havenSession.havenId
        .filterNotNull()
        .flatMapLatest { havenId -> firestoreManager.observeMembers(havenId) }
        .map { it.size }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    private val _weeklyScores = MutableStateFlow(listOf(0f, 0f, 0f, 0f, 0f, 0f, 0f))
    val weeklyScores: StateFlow<List<Float>> = _weeklyScores.asStateFlow()
}
