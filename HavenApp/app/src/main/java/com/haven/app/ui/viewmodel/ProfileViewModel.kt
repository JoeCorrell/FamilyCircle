package com.haven.app.ui.viewmodel

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.haven.app.data.api.HavenApiManager
import com.haven.app.data.api.toFamilyMember
import com.haven.app.data.model.EmergencyContact
import com.haven.app.data.model.FamilyMember
import com.haven.app.data.local.dao.EmergencyContactDao
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val apiManager: HavenApiManager,
    private val emergencyContactDao: EmergencyContactDao
) : ViewModel() {

    private val myMemberFlow = apiManager.observeMembers()
        .map { members ->
            val myUid = apiManager.userId
            members.firstOrNull { it.userId == myUid }
        }

    val userName: StateFlow<String> = myMemberFlow
        .map { it?.name ?: "" }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), "")

    val photoUrl: StateFlow<String> = myMemberFlow
        .map { it?.photoUrl ?: "" }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), "")

    val phoneNumber: StateFlow<String> = myMemberFlow
        .map { it?.phoneNumber ?: "" }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), "")

    val familyName: StateFlow<String> = flow {
        val haven = apiManager.getHaven()
        emit(haven?.name ?: "My Family")
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), "My Family")

    val inviteCode: StateFlow<String> = flow {
        val haven = apiManager.getHaven()
        emit(haven?.inviteCode ?: "")
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), "")

    val members: StateFlow<List<FamilyMember>> = apiManager.observeMembers()
        .map { list -> list.map { it.toFamilyMember() } }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val emergencyContacts: StateFlow<List<EmergencyContact>> = emergencyContactDao.getAllContacts()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val isUploadingPhoto = MutableStateFlow(false)

    val userColor: StateFlow<Long> = myMemberFlow
        .map { it?.color ?: 0xFFE879A0 }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0xFFE879A0)

    val avatarIcon: StateFlow<String> = myMemberFlow
        .map { it?.avatarIcon ?: "" }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), "")

    fun updateUserName(name: String) {
        viewModelScope.launch {
            apiManager.updateMyMember(mapOf(
                "name" to name,
                "initials" to name.take(1).uppercase()
            ))
        }
    }

    fun updateFamilyName(name: String) {
        viewModelScope.launch {
            apiManager.updateHavenName(name)
        }
    }

    fun uploadAvatar(uri: Uri) {
        // Photo upload requires Firebase Storage which we've removed.
        // For now, profile photos are handled via avatar icons.
    }

    fun updateAvatarIcon(iconName: String) {
        viewModelScope.launch {
            apiManager.updateMyMember(mapOf("avatarIcon" to iconName))
        }
    }

    fun updatePhoneNumber(phone: String) {
        viewModelScope.launch {
            apiManager.updateMyMember(mapOf("phoneNumber" to phone))
        }
    }

    fun updateMemberColor(color: Long) {
        viewModelScope.launch {
            apiManager.updateMyMember(mapOf("color" to color))
        }
    }

    fun addEmergencyContact(name: String, phone: String, relationship: String) {
        viewModelScope.launch {
            emergencyContactDao.insertContact(
                EmergencyContact(name = name, phoneNumber = phone, relationship = relationship, notifyOnSos = true)
            )
        }
    }

    fun removeEmergencyContact(contact: EmergencyContact) {
        viewModelScope.launch { emergencyContactDao.deleteContact(contact) }
    }
}
