package com.haven.app.ui.viewmodel

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.util.Base64
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.haven.app.data.api.HavenApiManager
import com.haven.app.data.api.toFamilyMember
import com.haven.app.data.model.EmergencyContact
import com.haven.app.data.model.FamilyMember
import com.haven.app.data.local.dao.EmergencyContactDao
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream
import javax.inject.Inject

@HiltViewModel
class ProfileViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val apiManager: HavenApiManager,
    private val emergencyContactDao: EmergencyContactDao
) : ViewModel() {

    private val myMemberFlow = apiManager.observeMyMember()

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

    private val _isUploadingPhoto = MutableStateFlow(false)
    val isUploadingPhoto: StateFlow<Boolean> = _isUploadingPhoto.asStateFlow()

    val userColor: StateFlow<Long> = myMemberFlow
        .map { it?.colorAsLong() ?: 0xFFE879A0 }
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
        viewModelScope.launch {
            _isUploadingPhoto.value = true
            try {
                val base64 = withContext(Dispatchers.IO) {
                    val original = context.contentResolver.openInputStream(uri)?.use { BitmapFactory.decodeStream(it) }
                        ?: return@withContext null

                    // Resize to 200x200
                    val size = 200
                    val scaled = Bitmap.createScaledBitmap(original, size, size, true)
                    original.recycle()

                    val baos = ByteArrayOutputStream()
                    scaled.compress(Bitmap.CompressFormat.JPEG, 70, baos)
                    scaled.recycle()

                    "data:image/jpeg;base64," + Base64.encodeToString(baos.toByteArray(), Base64.NO_WRAP)
                }
                if (base64 != null) {
                    apiManager.updateMyMember(mapOf("photoUrl" to base64))
                }
            } catch (_: Exception) {}
            _isUploadingPhoto.value = false
        }
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

    suspend fun createNewHaven(name: String, userName: String) {
        val result = apiManager.createHaven(name, userName)
        result.onSuccess {
            // Switch to the new Haven
            apiManager.switchHaven(it.haven.id)
        }
    }

    suspend fun joinNewHaven(inviteCode: String, userName: String): Boolean {
        val result = apiManager.joinHaven(inviteCode, userName)
        result.onSuccess {
            apiManager.switchHaven(it.haven.id)
        }
        return result.isSuccess
    }

    val myRole: StateFlow<String> = myMemberFlow
        .map { it?.role ?: "MEMBER" }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), "MEMBER")

    fun kickMember(memberId: String) {
        viewModelScope.launch { apiManager.kickMember(memberId) }
    }

    fun promoteMember(memberId: String) {
        viewModelScope.launch { apiManager.promoteMember(memberId) }
    }

    fun demoteMember(memberId: String) {
        viewModelScope.launch { apiManager.demoteMember(memberId) }
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
