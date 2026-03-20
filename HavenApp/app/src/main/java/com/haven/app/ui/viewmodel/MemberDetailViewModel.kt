package com.haven.app.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.haven.app.data.remote.FirebaseAuthManager
import com.haven.app.data.remote.FirestoreManager
import com.haven.app.data.remote.HavenSession
import com.haven.app.service.NotificationHelper
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MemberDetailViewModel @Inject constructor(
    private val firestoreManager: FirestoreManager,
    private val havenSession: HavenSession,
    private val authManager: FirebaseAuthManager,
    private val notificationHelper: NotificationHelper
) : ViewModel() {
    fun sendCheckIn(memberName: String) {
        viewModelScope.launch {
            val havenId = havenSession.havenId.value ?: return@launch
            val requesterName = try {
                val members = firestoreManager.observeMembers(havenId).first()
                (members.firstOrNull { it["uid"] == authManager.userId }?.get("name") as? String) ?: "Someone"
            } catch (_: Exception) { "Someone" }
            notificationHelper.notifyCheckIn(requesterName, memberName)
        }
    }
}
