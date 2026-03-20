package com.haven.app.data.preferences

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "haven_prefs")

@Singleton
class UserPreferences @Inject constructor(
    @ApplicationContext private val context: Context
) {
    companion object {
        val THEME_KEY = stringPreferencesKey("theme")
        val PUSH_ALERTS = booleanPreferencesKey("push_alerts")
        val LOCATION_ALERTS = booleanPreferencesKey("location_alerts")
        val BATTERY_ALERTS = booleanPreferencesKey("battery_alerts")
        val SPEED_ALERTS = booleanPreferencesKey("speed_alerts")
        val QUIET_HOURS = booleanPreferencesKey("quiet_hours")
        val LOCATION_SHARING = booleanPreferencesKey("location_sharing")
        val HIGH_PRECISION = booleanPreferencesKey("high_precision")
        val BACKGROUND_UPDATES = booleanPreferencesKey("background_updates")
        val WIFI_ONLY = booleanPreferencesKey("wifi_only")
        val LOCATION_HISTORY = booleanPreferencesKey("location_history")
        val GHOST_MODE = booleanPreferencesKey("ghost_mode")
        val HIDE_ADDRESS = booleanPreferencesKey("hide_address")
        val DATA_SHARING = booleanPreferencesKey("data_sharing")
        val CRASH_DETECTION = booleanPreferencesKey("crash_detection")
        val USER_NAME = stringPreferencesKey("user_name")
        val FAMILY_NAME = stringPreferencesKey("family_name")
    }

    val theme: Flow<String> = context.dataStore.data.map { it[THEME_KEY] ?: "sand" }
    val pushAlerts: Flow<Boolean> = context.dataStore.data.map { it[PUSH_ALERTS] ?: true }
    val locationAlerts: Flow<Boolean> = context.dataStore.data.map { it[LOCATION_ALERTS] ?: true }
    val batteryAlerts: Flow<Boolean> = context.dataStore.data.map { it[BATTERY_ALERTS] ?: true }
    val speedAlerts: Flow<Boolean> = context.dataStore.data.map { it[SPEED_ALERTS] ?: true }
    val quietHours: Flow<Boolean> = context.dataStore.data.map { it[QUIET_HOURS] ?: false }
    val locationSharing: Flow<Boolean> = context.dataStore.data.map { it[LOCATION_SHARING] ?: true }
    val highPrecision: Flow<Boolean> = context.dataStore.data.map { it[HIGH_PRECISION] ?: true }
    val backgroundUpdates: Flow<Boolean> = context.dataStore.data.map { it[BACKGROUND_UPDATES] ?: true }
    val wifiOnly: Flow<Boolean> = context.dataStore.data.map { it[WIFI_ONLY] ?: false }
    val locationHistory: Flow<Boolean> = context.dataStore.data.map { it[LOCATION_HISTORY] ?: true }
    val ghostMode: Flow<Boolean> = context.dataStore.data.map { it[GHOST_MODE] ?: false }
    val hideAddress: Flow<Boolean> = context.dataStore.data.map { it[HIDE_ADDRESS] ?: false }
    val dataSharing: Flow<Boolean> = context.dataStore.data.map { it[DATA_SHARING] ?: true }
    val crashDetection: Flow<Boolean> = context.dataStore.data.map { it[CRASH_DETECTION] ?: true }
    val userName: Flow<String> = context.dataStore.data.map { it[USER_NAME] ?: "" }
    val familyName: Flow<String> = context.dataStore.data.map { it[FAMILY_NAME] ?: "My Family" }

    suspend fun setTheme(theme: String) {
        context.dataStore.edit { it[THEME_KEY] = theme }
    }

    suspend fun setPreference(key: Preferences.Key<Boolean>, value: Boolean) {
        context.dataStore.edit { it[key] = value }
    }

    suspend fun setStringPreference(key: Preferences.Key<String>, value: String) {
        context.dataStore.edit { it[key] = value }
    }
}
