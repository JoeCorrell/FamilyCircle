package com.haven.app.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.haven.app.data.model.Message
import com.haven.app.data.remote.FirebaseAuthManager
import com.haven.app.data.remote.FirestoreManager
import com.haven.app.data.remote.HavenSession
import com.haven.app.service.NotificationHelper
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ChatViewModel @Inject constructor(
    private val firestoreManager: FirestoreManager,
    private val havenSession: HavenSession,
    private val authManager: FirebaseAuthManager,
    private val notificationHelper: NotificationHelper
) : ViewModel() {

    // Single shared members flow — avoids triple Firestore listeners
    private val sharedMembers: SharedFlow<List<Map<String, Any>>> = havenSession.havenId
        .filterNotNull()
        .flatMapLatest { havenId -> firestoreManager.observeMembers(havenId) }
        .shareIn(viewModelScope, SharingStarted.Eagerly, replay = 1)

    // Map of uid -> name for resolving sender names
    private val memberNames: StateFlow<Map<String, String>> = sharedMembers
        .map { members ->
            members.associate {
                (it["uid"] as? String ?: "") to (it["name"] as? String ?: "Unknown")
            }
        }
        .stateIn(viewModelScope, SharingStarted.Eagerly, emptyMap())

    private val myMemberName: StateFlow<String> = sharedMembers
        .map { members ->
            val myUid = authManager.userId
            (members.firstOrNull { it["uid"] == myUid }?.get("name") as? String) ?: "You"
        }
        .stateIn(viewModelScope, SharingStarted.Eagerly, "You")

    val memberColors: StateFlow<Map<String, Long>> = sharedMembers
        .map { members ->
            members.associate {
                (it["name"] as? String ?: "Unknown") to ((it["color"] as? Number)?.toLong() ?: 0xFF999999)
            }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyMap())

    val messages: StateFlow<List<Message>> = havenSession.havenId
        .filterNotNull()
        .flatMapLatest { havenId -> firestoreManager.observeMessages(havenId) }
        .combine(memberNames) { list, names ->
            val myUid = authManager.userId
            list.map { data ->
                val senderUid = data["senderUid"] as? String ?: ""
                val storedName = data["senderName"] as? String ?: "Unknown"
                val resolvedName = names[senderUid] ?: storedName
                Message(
                    id = (data["id"] as? String)?.hashCode()?.toLong() ?: System.nanoTime(),
                    senderName = resolvedName,
                    senderMemberId = senderUid.hashCode().toLong(),
                    text = data["text"] as? String ?: "",
                    timestamp = (data["timestamp"] as? Number)?.toLong() ?: System.currentTimeMillis(),
                    isFromCurrentUser = senderUid == myUid
                )
            }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val familyName: StateFlow<String> = havenSession.havenName
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), "My Family")

    private val _inputText = MutableStateFlow("")
    val inputText: StateFlow<String> = _inputText.asStateFlow()

    fun updateInput(text: String) { _inputText.value = text }

    fun sendMessage() {
        val text = _inputText.value.trim()
        if (text.isEmpty()) return
        val havenId = havenSession.havenId.value ?: return

        viewModelScope.launch {
            // Use cached member name — sharedMembers is Eagerly started
            val senderName = myMemberName.value

            firestoreManager.sendMessage(havenId, mapOf(
                "senderUid" to (authManager.userId ?: ""),
                "senderName" to senderName,
                "text" to text,
                "timestamp" to System.currentTimeMillis()
            ))
            _inputText.value = ""
        }
    }
}
