package com.haven.app.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.haven.app.data.api.HavenApiManager
import com.haven.app.data.preferences.UserPreferences
import com.haven.app.ui.theme.HavenColors
import com.haven.app.ui.theme.HavenThemes
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val apiManager: HavenApiManager,
    userPreferences: UserPreferences
) : ViewModel() {

    private val myMemberFlow = apiManager.observeMembers()
        .map { members ->
            val myUid = apiManager.userId
            members.firstOrNull { it.userId == myUid }
        }

    val userName: StateFlow<String> = myMemberFlow
        .map { it?.name ?: "" }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), "")

    val userEmail: String = "" // Phone auth, no email

    val userPhotoUrl: StateFlow<String> = myMemberFlow
        .map { it?.photoUrl ?: "" }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), "")

    val userAvatarIcon: StateFlow<String> = myMemberFlow
        .map { it?.avatarIcon ?: "" }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), "")

    val userColor: StateFlow<Long> = myMemberFlow
        .map { it?.colorAsLong() ?: 0xFFE879A0 }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0xFFE879A0)

    val familyName: StateFlow<String> = flow {
        val haven = apiManager.getHaven()
        emit(haven?.name ?: "My Family")
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), "My Family")

    val inviteCode: StateFlow<String> = flow {
        val haven = apiManager.getHaven()
        emit(haven?.inviteCode ?: "")
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), "")

    val memberCount: StateFlow<Int> = apiManager.observeMembers()
        .map { it.size }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    val currentTheme: StateFlow<HavenColors> = userPreferences.theme
        .map { HavenThemes.fromKey(it) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), HavenThemes.Sand)

    fun signOut() {
        viewModelScope.launch { apiManager.signOut() }
    }
}
