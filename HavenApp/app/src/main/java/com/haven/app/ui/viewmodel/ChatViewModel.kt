package com.haven.app.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.haven.app.data.api.ErrandData
import com.haven.app.data.api.HavenApiManager
import com.haven.app.data.model.Message
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ChatViewModel @Inject constructor(
    private val apiManager: HavenApiManager
) : ViewModel() {

    private val membersFlow = apiManager.observeMembers()
        .shareIn(viewModelScope, SharingStarted.Eagerly, replay = 1)

    private val memberNames: StateFlow<Map<String, String>> = membersFlow
        .map { members -> members.associate { it.userId to it.name } }
        .stateIn(viewModelScope, SharingStarted.Eagerly, emptyMap())

    val myMemberName: StateFlow<String> = membersFlow
        .map { members ->
            val myUid = apiManager.userId
            members.firstOrNull { it.userId == myUid }?.name ?: "You"
        }
        .stateIn(viewModelScope, SharingStarted.Eagerly, "You")

    val memberColors: StateFlow<Map<String, Long>> = membersFlow
        .map { members -> members.associate { it.name to it.colorAsLong() } }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyMap())

    val messages: StateFlow<List<Message>> = apiManager.observeMessages()
        .combine(memberNames) { list, names ->
            val myUid = apiManager.userId
            list.map { data ->
                val resolvedName = names[data.senderUid] ?: data.senderName
                Message(
                    id = data.id.hashCode().toLong(),
                    senderName = resolvedName,
                    senderMemberId = data.senderUid.hashCode().toLong(),
                    text = data.text,
                    timestamp = data.timestamp,
                    isFromCurrentUser = data.senderUid == myUid
                )
            }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val errands: StateFlow<List<ErrandData>> = apiManager.observeErrands()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val familyName: StateFlow<String> = flow {
        val haven = apiManager.getHaven()
        emit(haven?.name ?: "My Family")
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), "My Family")

    private val _inputText = MutableStateFlow("")
    val inputText: StateFlow<String> = _inputText.asStateFlow()

    val myUserId: String? get() = apiManager.userId

    fun updateInput(text: String) { _inputText.value = text }

    fun sendMessage() {
        val text = _inputText.value.trim()
        if (text.isEmpty()) return
        viewModelScope.launch {
            apiManager.sendMessage(myMemberName.value, text)
            _inputText.value = ""
        }
    }

    fun sendErrand(item: String, address: String, note: String) {
        if (item.isBlank()) return
        viewModelScope.launch {
            apiManager.createErrand(myMemberName.value, item, address, note)
        }
    }

    fun acceptErrand(errandId: String) {
        viewModelScope.launch {
            apiManager.acceptErrand(errandId, myMemberName.value)
        }
    }

    private val _dismissedErrandIds = MutableStateFlow(setOf<String>())
    val dismissedErrandIds: StateFlow<Set<String>> = _dismissedErrandIds.asStateFlow()

    fun dismissErrand(id: String) {
        _dismissedErrandIds.value = _dismissedErrandIds.value + id
        viewModelScope.launch {
            val errand = errands.value.firstOrNull { it.id == id }
            if (errand?.status == "ACCEPTED") {
                apiManager.completeErrand(id)
            } else {
                apiManager.declineErrand(id)
            }
        }
    }

    fun completeErrand(id: String) {
        _dismissedErrandIds.value = _dismissedErrandIds.value + id
        viewModelScope.launch { apiManager.completeErrand(id) }
    }
}
