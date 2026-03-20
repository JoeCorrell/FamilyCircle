package com.haven.app.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.haven.app.data.preferences.UserPreferences
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ThemeViewModel @Inject constructor(
    private val userPreferences: UserPreferences
) : ViewModel() {

    val currentThemeKey: StateFlow<String> = userPreferences.theme
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), "sand")

    fun setTheme(key: String) {
        viewModelScope.launch {
            userPreferences.setTheme(key)
        }
    }
}
