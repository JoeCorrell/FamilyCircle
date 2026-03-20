package com.haven.app.ui.viewmodel

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.haven.app.data.model.EmergencyContact
import com.haven.app.data.model.FamilyMember
import com.haven.app.data.local.dao.EmergencyContactDao
import com.haven.app.data.remote.FirebaseAuthManager
import com.haven.app.data.remote.FirestoreManager
import com.haven.app.data.remote.HavenSession
import com.haven.app.data.remote.StorageManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val firestoreManager: FirestoreManager,
    private val authManager: FirebaseAuthManager,
    private val havenSession: HavenSession,
    private val emergencyContactDao: EmergencyContactDao,
    private val storageManager: StorageManager
) : ViewModel() {

    private val myMemberFlow: Flow<Map<String, Any>?> = havenSession.havenId
        .filterNotNull()
        .flatMapLatest { havenId -> firestoreManager.observeMembers(havenId) }
        .map { members -> members.firstOrNull { it["uid"] == authManager.userId } }

    val userName: StateFlow<String> = myMemberFlow
        .map { it?.get("name") as? String ?: "" }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), "")

    val photoUrl: StateFlow<String> = myMemberFlow
        .map { it?.get("photoUrl") as? String ?: "" }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), "")

    val phoneNumber: StateFlow<String> = myMemberFlow
        .map { it?.get("phoneNumber") as? String ?: "" }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), "")

    val familyName: StateFlow<String> = havenSession.havenName
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), "My Family")

    val inviteCode: StateFlow<String> = havenSession.inviteCode
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), "")

    val members: StateFlow<List<FamilyMember>> = havenSession.havenId
        .filterNotNull()
        .flatMapLatest { havenId -> firestoreManager.observeMembers(havenId) }
        .map { list -> list.map { it.toFamilyMember() } }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val emergencyContacts: StateFlow<List<EmergencyContact>> = emergencyContactDao.getAllContacts()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val isUploadingPhoto = MutableStateFlow(false)

    fun updateUserName(name: String) {
        viewModelScope.launch {
            val havenId = havenSession.havenId.value ?: return@launch
            val userId = authManager.userId ?: return@launch
            try {
                firestoreManager.updateMemberFields(havenId, userId, mapOf(
                    "name" to name,
                    "initials" to name.take(1).uppercase()
                ))
            } catch (_: Exception) {}
        }
    }

    fun updateFamilyName(name: String) {
        viewModelScope.launch {
            val havenId = havenSession.havenId.value ?: return@launch
            try {
                firestoreManager.createHaven(havenId, mapOf("name" to name))
                havenSession.setHaven(havenId, name, havenSession.inviteCode.value)
            } catch (_: Exception) {}
        }
    }

    fun uploadAvatar(uri: Uri) {
        viewModelScope.launch {
            val userId = authManager.userId ?: return@launch
            val havenId = havenSession.havenId.value ?: return@launch
            isUploadingPhoto.value = true
            try {
                val url = storageManager.uploadAvatar(userId, uri)
                firestoreManager.updateMemberFields(havenId, userId, mapOf("photoUrl" to url))
            } catch (_: Exception) {}
            isUploadingPhoto.value = false
        }
    }

    val userColor: StateFlow<Long> = myMemberFlow
        .map { (it?.get("color") as? Number)?.toLong() ?: 0xFFE879A0 }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0xFFE879A0)

    val avatarIcon: StateFlow<String> = myMemberFlow
        .map { it?.get("avatarIcon") as? String ?: "" }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), "")

    fun updateAvatarIcon(iconName: String) {
        viewModelScope.launch {
            val havenId = havenSession.havenId.value ?: return@launch
            val userId = authManager.userId ?: return@launch
            try {
                firestoreManager.updateMemberFields(havenId, userId, mapOf("avatarIcon" to iconName))
            } catch (_: Exception) {}
        }
    }

    fun updatePhoneNumber(phone: String) {
        viewModelScope.launch {
            val havenId = havenSession.havenId.value ?: return@launch
            val userId = authManager.userId ?: return@launch
            try {
                firestoreManager.updateMemberFields(havenId, userId, mapOf("phoneNumber" to phone))
            } catch (_: Exception) {}
        }
    }

    fun updateMemberColor(color: Long) {
        viewModelScope.launch {
            val havenId = havenSession.havenId.value ?: return@launch
            val userId = authManager.userId ?: return@launch
            try {
                firestoreManager.updateMemberFields(havenId, userId, mapOf("color" to color))
            } catch (_: Exception) {}
        }
    }

    fun addEmergencyContact(name: String, phone: String, relationship: String) {
        viewModelScope.launch {
            emergencyContactDao.insertContact(
                EmergencyContact(
                    name = name,
                    phoneNumber = phone,
                    relationship = relationship,
                    notifyOnSos = true
                )
            )
        }
    }

    fun removeEmergencyContact(contact: EmergencyContact) {
        viewModelScope.launch {
            emergencyContactDao.deleteContact(contact)
        }
    }
}
