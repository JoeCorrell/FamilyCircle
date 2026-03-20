package com.haven.app.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.haven.app.data.api.AuthState
import com.haven.app.data.api.HavenApiManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val apiManager: HavenApiManager
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
                _authState.value = apiManager.checkAuth()
            } catch (e: Exception) {
                _error.value = e.message
                _authState.value = AuthState.SIGNED_OUT
            }
        }
    }

    fun signUp(phone: String, password: String) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            apiManager.signUp(phone, password)
                .onSuccess { _authState.value = AuthState.NO_HAVEN }
                .onFailure { _error.value = it.message ?: "Sign up failed" }
            _isLoading.value = false
        }
    }

    fun signIn(phone: String, password: String) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            apiManager.signIn(phone, password)
                .onSuccess { _authState.value = apiManager.checkAuth() }
                .onFailure { _error.value = it.message ?: "Sign in failed" }
            _isLoading.value = false
        }
    }

    fun signOut() {
        viewModelScope.launch {
            apiManager.signOut()
            _authState.value = AuthState.SIGNED_OUT
        }
    }

    fun createHaven(name: String, userName: String) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            apiManager.createHaven(name, userName)
                .onSuccess { _authState.value = AuthState.READY }
                .onFailure { _error.value = it.message ?: "Failed to create circle" }
            _isLoading.value = false
        }
    }

    fun joinHaven(inviteCode: String, userName: String) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            apiManager.joinHaven(inviteCode, userName)
                .onSuccess { _authState.value = AuthState.READY }
                .onFailure { _error.value = it.message ?: "Failed to join circle" }
            _isLoading.value = false
        }
    }

    fun clearError() { _error.value = null }
}
