package com.haven.app.ui.viewmodel

import androidx.datastore.preferences.core.Preferences
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.haven.app.data.preferences.UserPreferences
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SettingsSubViewModel @Inject constructor(
    private val userPreferences: UserPreferences
) : ViewModel() {

    private val flowCache = mutableMapOf<Preferences.Key<Boolean>, Flow<Boolean>>()

    fun getPreference(key: Preferences.Key<Boolean>): Flow<Boolean> {
        return flowCache.getOrPut(key) {
            when (key) {
                UserPreferences.PUSH_ALERTS -> userPreferences.pushAlerts
                UserPreferences.LOCATION_ALERTS -> userPreferences.locationAlerts
                UserPreferences.BATTERY_ALERTS -> userPreferences.batteryAlerts
                UserPreferences.SPEED_ALERTS -> userPreferences.speedAlerts
                UserPreferences.QUIET_HOURS -> userPreferences.quietHours
                UserPreferences.LOCATION_SHARING -> userPreferences.locationSharing
                UserPreferences.HIGH_PRECISION -> userPreferences.highPrecision
                UserPreferences.BACKGROUND_UPDATES -> userPreferences.backgroundUpdates
                UserPreferences.WIFI_ONLY -> userPreferences.wifiOnly
                UserPreferences.LOCATION_HISTORY -> userPreferences.locationHistory
                UserPreferences.GHOST_MODE -> userPreferences.ghostMode
                UserPreferences.HIDE_ADDRESS -> userPreferences.hideAddress
                UserPreferences.DATA_SHARING -> userPreferences.dataSharing
                UserPreferences.CRASH_DETECTION -> userPreferences.crashDetection
                else -> userPreferences.pushAlerts
            }
        }
    }

    fun setPreference(key: Preferences.Key<Boolean>, value: Boolean) {
        viewModelScope.launch {
            userPreferences.setPreference(key, value)
        }
    }
}
