package com.haven.app.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.haven.app.data.api.HavenApiManager
import com.haven.app.data.model.Drive
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import javax.inject.Inject

@HiltViewModel
class SafetyViewModel @Inject constructor(
    private val apiManager: HavenApiManager
) : ViewModel() {

    val drives: StateFlow<List<Drive>> = MutableStateFlow(emptyList())
    val familyScore: StateFlow<Int> = MutableStateFlow(100)
    val weeklyScores: StateFlow<List<Int>> = MutableStateFlow(listOf(85, 90, 88, 92, 87, 94, 91))

    val memberCount: StateFlow<Int> = apiManager.observeMembers()
        .map { it.size }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)
}
