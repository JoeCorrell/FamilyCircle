package com.haven.app.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
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

    private val myMemberName: StateFlow<String> = membersFlow
        .map { members ->
            val myUid = apiManager.userId
            members.firstOrNull { it.userId == myUid }?.name ?: "You"
        }
        .stateIn(viewModelScope, SharingStarted.Eagerly, "You")

    val memberColors: StateFlow<Map<String, Long>> = membersFlow
        .map { members -> members.associate { it.name to it.color } }
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

    val familyName: StateFlow<String> = flow {
        val haven = apiManager.getHaven()
        emit(haven?.name ?: "My Family")
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), "My Family")

    private val _inputText = MutableStateFlow("")
    val inputText: StateFlow<String> = _inputText.asStateFlow()

    fun updateInput(text: String) { _inputText.value = text }

    fun sendMessage() {
        val text = _inputText.value.trim()
        if (text.isEmpty()) return
        viewModelScope.launch {
            apiManager.sendMessage(myMemberName.value, text)
            _inputText.value = ""
        }
    }
}
