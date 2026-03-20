package com.haven.app.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.haven.app.data.preferences.UserPreferences
import com.haven.app.data.remote.FirebaseAuthManager
import com.haven.app.data.remote.FirestoreManager
import com.haven.app.data.remote.HavenSession
import com.haven.app.ui.theme.HavenColors
import com.haven.app.ui.theme.HavenThemes
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val havenSession: HavenSession,
    private val firestoreManager: FirestoreManager,
    private val authManager: FirebaseAuthManager,
    userPreferences: UserPreferences
) : ViewModel() {

    private val myMemberFlow: Flow<Map<String, Any>?> = havenSession.havenId
        .filterNotNull()
        .flatMapLatest { havenId -> firestoreManager.observeMembers(havenId) }
        .map { members -> members.firstOrNull { it["uid"] == authManager.userId } }

    val userName: StateFlow<String> = myMemberFlow
        .map { it?.get("name") as? String ?: "" }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), "")

    val userEmail: String = authManager.currentUser?.email ?: ""

    val userPhotoUrl: StateFlow<String> = myMemberFlow
        .map { it?.get("photoUrl") as? String ?: "" }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), "")

    val userAvatarIcon: StateFlow<String> = myMemberFlow
        .map { it?.get("avatarIcon") as? String ?: "" }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), "")

    val userColor: StateFlow<Long> = myMemberFlow
        .map { (it?.get("color") as? Number)?.toLong() ?: 0xFFE879A0 }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0xFFE879A0)

    val familyName: StateFlow<String> = havenSession.havenName
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), "My Family")

    val inviteCode: StateFlow<String> = havenSession.inviteCode
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), "")

    val memberCount: StateFlow<Int> = havenSession.havenId
        .filterNotNull()
        .flatMapLatest { havenId -> firestoreManager.observeMembers(havenId) }
        .map { it.size }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    val currentTheme: StateFlow<HavenColors> = userPreferences.theme
        .map { HavenThemes.fromKey(it) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), HavenThemes.Sand)

    fun signOut() {
        authManager.signOut()
        havenSession.clear()
    }
}
