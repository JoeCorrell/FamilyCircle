package com.haven.app.data.remote

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Holds the current session state: which Haven the user belongs to.
 * This is the single source of truth for the current havenId across the app.
 */
@Singleton
class HavenSession @Inject constructor() {

    private val _havenId = MutableStateFlow<String?>(null)
    val havenId: StateFlow<String?> = _havenId.asStateFlow()

    private val _havenName = MutableStateFlow("")
    val havenName: StateFlow<String> = _havenName.asStateFlow()

    private val _inviteCode = MutableStateFlow("")
    val inviteCode: StateFlow<String> = _inviteCode.asStateFlow()

    fun setHaven(id: String, name: String, code: String) {
        _havenId.value = id
        _havenName.value = name
        _inviteCode.value = code
    }

    fun clear() {
        _havenId.value = null
        _havenName.value = ""
        _inviteCode.value = ""
    }
}
