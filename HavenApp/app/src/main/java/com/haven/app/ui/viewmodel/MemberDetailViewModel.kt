package com.haven.app.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.haven.app.data.api.HavenApiManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MemberDetailViewModel @Inject constructor(
    private val apiManager: HavenApiManager
) : ViewModel() {

    fun sendCheckIn(memberName: String) {
        viewModelScope.launch {
            val myName = apiManager.getMyMember()?.name ?: "Someone"
            apiManager.createNotification("$myName requested a check-in from $memberName", 0xFF38BDF8)
        }
    }
}
