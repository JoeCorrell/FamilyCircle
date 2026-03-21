package com.haven.app.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.haven.app.data.api.HavenApiManager
import com.haven.app.data.api.toDrive
import com.haven.app.data.model.Drive
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import javax.inject.Inject

@HiltViewModel
class SafetyViewModel @Inject constructor(
    private val apiManager: HavenApiManager
) : ViewModel() {

    val drives: StateFlow<List<Drive>> = apiManager.observeDrives()
        .map { list -> list.map { it.toDrive() } }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val memberCount: StateFlow<Int> = apiManager.observeMembers()
        .map { it.size }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)
}
