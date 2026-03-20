package com.haven.app.ui.screens

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ChevronLeft
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.datastore.preferences.core.Preferences
import androidx.hilt.navigation.compose.hiltViewModel
import com.haven.app.data.preferences.UserPreferences
import com.haven.app.ui.components.HavenCard
import com.haven.app.ui.components.HavenToggle
import com.haven.app.ui.theme.LocalHavenColors
import com.haven.app.ui.theme.OutfitFamily
import com.haven.app.ui.theme.SpaceMonoFamily
import com.haven.app.ui.viewmodel.SettingsSubViewModel

@Composable
fun SettingsSubScreen(
    title: String,
    items: List<String>,
    onBack: () -> Unit,
    viewModel: SettingsSubViewModel = hiltViewModel()
) {
    val t = LocalHavenColors.current

    // Map item names to preference keys
    val prefKeys = remember {
        mapOf(
            "Push Alerts" to UserPreferences.PUSH_ALERTS,
            "Location Alerts" to UserPreferences.LOCATION_ALERTS,
            "Battery Alerts" to UserPreferences.BATTERY_ALERTS,
            "Speed Alerts" to UserPreferences.SPEED_ALERTS,
            "Quiet Hours" to UserPreferences.QUIET_HOURS,
            "Share with Circle" to UserPreferences.LOCATION_SHARING,
            "Precision" to UserPreferences.HIGH_PRECISION,
            "Background Updates" to UserPreferences.BACKGROUND_UPDATES,
            "Wi-Fi Only" to UserPreferences.WIFI_ONLY,
            "History" to UserPreferences.LOCATION_HISTORY,
            "Ghost Mode" to UserPreferences.GHOST_MODE,
            "Hide Address" to UserPreferences.HIDE_ADDRESS,
            "Block Requests" to null,
            "Data Sharing" to UserPreferences.DATA_SHARING,
            "Clear History" to null,
        )
    }

    Column(modifier = Modifier.fillMaxSize()) {
        // Header
        Row(
            modifier = Modifier.padding(horizontal = 20.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .background(t.accentBg, RoundedCornerShape(10.dp))
                    .clickable { onBack() },
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Outlined.ChevronLeft, "Back", Modifier.size(16.dp), tint = t.accent)
            }
            Text(
                title, fontSize = 22.sp,
                fontWeight = FontWeight.ExtraBold, color = t.text,
                fontFamily = OutfitFamily, letterSpacing = (-0.5).sp
            )
        }

        // Hero description
        HavenCard(modifier = Modifier.fillMaxWidth().padding(horizontal = 18.dp)) {
            Column(
                modifier = Modifier.fillMaxWidth().padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    when (title) {
                        "Notifications" -> "Control what alerts you receive and when you receive them."
                        "Location" -> "Manage how your location is shared with circle members."
                        "Privacy" -> "Control who sees your information and manage your data."
                        else -> "Manage your $title settings."
                    },
                    fontSize = 11.sp, color = t.textFade, fontFamily = OutfitFamily,
                    lineHeight = 16.sp,
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                )
            }
        }

        Spacer(Modifier.height(8.dp))

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 18.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            items.forEach { item ->
                val key = prefKeys[item]
                if (key != null) {
                    val isOn by viewModel.getPreference(key).collectAsState(initial = true)
                    HavenCard(modifier = Modifier.fillMaxWidth()) {
                        Row(
                            modifier = Modifier.padding(horizontal = 18.dp, vertical = 14.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    item, fontSize = 14.sp, color = t.text,
                                    fontWeight = FontWeight.Medium, fontFamily = OutfitFamily
                                )
                                Text(
                                    getItemDescription(item),
                                    fontSize = 10.sp, color = t.textFade,
                                    fontFamily = OutfitFamily
                                )
                            }
                            Spacer(Modifier.width(12.dp))
                            HavenToggle(
                                checked = isOn,
                                onCheckedChange = { viewModel.setPreference(key, it) }
                            )
                        }
                    }
                } else {
                    // Non-toggle items (like Clear History)
                    HavenCard(modifier = Modifier.fillMaxWidth()) {
                        Row(
                            modifier = Modifier.padding(horizontal = 18.dp, vertical = 14.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    item, fontSize = 14.sp, color = t.text,
                                    fontWeight = FontWeight.Medium, fontFamily = OutfitFamily
                                )
                                Text(
                                    getItemDescription(item),
                                    fontSize = 10.sp, color = t.textFade,
                                    fontFamily = OutfitFamily
                                )
                            }
                            Text("\u203A", fontSize = 14.sp, color = t.textFade)
                        }
                    }
                }
            }
            Spacer(Modifier.height(18.dp))
        }
    }
}

private fun getItemDescription(item: String): String = when (item) {
    "Push Alerts" -> "Get alerted on your lock screen"
    "Location Alerts" -> "When members arrive or leave places"
    "Battery Alerts" -> "When a member drops below 20%"
    "Speed Alerts" -> "When someone exceeds speed limits"
    "Quiet Hours" -> "Silence alerts on a schedule"
    "Share with Circle" -> "Share your live location"
    "Precision" -> "Uses more battery for accuracy"
    "Background Updates" -> "Update location in background"
    "Wi-Fi Only" -> "Only update on Wi-Fi networks"
    "History" -> "Keep location history"
    "Ghost Mode" -> "Pause location sharing temporarily"
    "Hide Address" -> "Show area instead of exact address"
    "Block Requests" -> "Prevent new circle invitations"
    "Data Sharing" -> "Anonymous usage analytics"
    "Clear History" -> "Delete all stored locations"
    else -> ""
}
