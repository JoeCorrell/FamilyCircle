package com.haven.app.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.haven.app.data.remote.FirebaseAuthManager
import com.haven.app.data.remote.FirestoreManager
import com.haven.app.data.remote.HavenSession
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

enum class AuthState { LOADING, SIGNED_OUT, NO_HAVEN, READY }

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val authManager: FirebaseAuthManager,
    private val firestoreManager: FirestoreManager,
    private val havenSession: HavenSession
) : ViewModel() {

    private val _authState = MutableStateFlow(AuthState.LOADING)
    val authState: StateFlow<AuthState> = _authState.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    init {
        checkAuthState()
    }

    private fun checkAuthState() {
        viewModelScope.launch {
            _authState.value = AuthState.LOADING
            try {
                if (!authManager.isSignedIn) {
                    _authState.value = AuthState.SIGNED_OUT
                    return@launch
                }
                val userId = authManager.userId ?: run {
                    _authState.value = AuthState.SIGNED_OUT
                    return@launch
                }
                val havenId = try {
                    kotlinx.coroutines.withTimeout(10_000L) {
                        firestoreManager.getUserHavenId(userId)
                    }
                } catch (_: Exception) { null }

                if (havenId == null) {
                    _authState.value = AuthState.NO_HAVEN
                } else {
                    try {
                        loadHavenSession(havenId)
                    } catch (_: Exception) { /* proceed anyway */ }
                    _authState.value = AuthState.READY
                }
            } catch (e: Exception) {
                _error.value = e.message
                _authState.value = AuthState.NO_HAVEN
            }
        }
    }

    fun signUp(email: String, password: String) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            val result = authManager.signUp(email, password)
            _isLoading.value = false
            result.onSuccess {
                _authState.value = AuthState.NO_HAVEN
            }.onFailure {
                _error.value = it.message ?: "Sign up failed"
            }
        }
    }

    fun signIn(email: String, password: String) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            val result = authManager.signIn(email, password)
            _isLoading.value = false
            result.onSuccess {
                checkAuthState()
            }.onFailure {
                _error.value = it.message ?: "Sign in failed"
            }
        }
    }

    fun signOut() {
        authManager.signOut()
        havenSession.clear()
        _authState.value = AuthState.SIGNED_OUT
    }

    fun createHaven(name: String, userName: String) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            try {
                val userId = authManager.userId
                if (userId == null) {
                    _error.value = "Not signed in. Please sign in again."
                    _isLoading.value = false
                    return@launch
                }
                val havenId = java.util.UUID.randomUUID().toString().take(20)
                val inviteCode = generateInviteCode()

                kotlinx.coroutines.withTimeout(15_000L) {
                    firestoreManager.createHaven(havenId, mapOf(
                        "name" to name,
                        "inviteCode" to inviteCode,
                        "createdBy" to userId,
                        "createdAt" to System.currentTimeMillis(),
                        "activeSos" to false
                    ))

                    firestoreManager.setMember(havenId, userId, mapOf(
                        "name" to userName,
                        "initials" to userName.take(1).uppercase(),
                        "color" to 0xFFE879A0,
                        "batteryLevel" to 100,
                        "latitude" to 0.0,
                        "longitude" to 0.0,
                        "speed" to 0f,
                        "status" to "UNKNOWN",
                        "currentAddress" to "",
                        "lastSeenTimestamp" to System.currentTimeMillis(),
                        "isOnline" to true,
                        "phoneNumber" to (authManager.currentUser?.email ?: "")
                    ))

                    firestoreManager.setUserHaven(userId, havenId)
                }
                loadHavenSession(havenId)
                _authState.value = AuthState.READY
            } catch (e: kotlinx.coroutines.TimeoutCancellationException) {
                _error.value = "Timed out. Check your internet connection and make sure Firestore is enabled in Firebase Console."
            } catch (e: Exception) {
                _error.value = "${e.javaClass.simpleName}: ${e.message ?: "Failed to create Haven"}"
            }
            _isLoading.value = false
        }
    }

    fun joinHaven(inviteCode: String, userName: String) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            try {
                val userId = authManager.userId ?: run {
                    _error.value = "Not signed in."
                    _isLoading.value = false
                    return@launch
                }

                val havenId = kotlinx.coroutines.withTimeout(10_000L) {
                    firestoreManager.getHavenByInviteCode(inviteCode.trim().uppercase())
                }
                if (havenId == null) {
                    _error.value = "Invalid invite code. Check and try again."
                    _isLoading.value = false
                    return@launch
                }

                val colors = listOf(0xFF60A5FA, 0xFFA78BFA, 0xFF34D399, 0xFFFBBF24, 0xFFFB923C, 0xFFF87171)
                val color = colors.random()

                kotlinx.coroutines.withTimeout(15_000L) {
                    firestoreManager.setMember(havenId, userId, mapOf(
                        "name" to userName,
                        "initials" to userName.take(1).uppercase(),
                        "color" to color,
                        "batteryLevel" to 100,
                        "latitude" to 0.0,
                        "longitude" to 0.0,
                        "speed" to 0f,
                        "status" to "UNKNOWN",
                        "currentAddress" to "",
                        "lastSeenTimestamp" to System.currentTimeMillis(),
                        "isOnline" to true,
                        "phoneNumber" to (authManager.currentUser?.email ?: "")
                    ))

                    firestoreManager.setUserHaven(userId, havenId)
                }
                loadHavenSession(havenId)
                _authState.value = AuthState.READY
            } catch (e: kotlinx.coroutines.TimeoutCancellationException) {
                _error.value = "Timed out. Check your internet and Firestore setup."
            } catch (e: Exception) {
                _error.value = "${e.javaClass.simpleName}: ${e.message ?: "Failed to join Haven"}"
            }
            _isLoading.value = false
        }
    }

    private suspend fun loadHavenSession(havenId: String) {
        val data = firestoreManager.getHavenData(havenId) ?: return
        havenSession.setHaven(
            id = havenId,
            name = data["name"] as? String ?: "My Family",
            code = data["inviteCode"] as? String ?: ""
        )
    }

    private fun generateInviteCode(): String {
        val chars = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789"
        return (1..6).map { chars.random() }.joinToString("")
    }

    fun clearError() { _error.value = null }
}
