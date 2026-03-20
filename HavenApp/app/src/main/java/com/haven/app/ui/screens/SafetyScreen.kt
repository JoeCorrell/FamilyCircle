package com.haven.app.ui.screens

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.haven.app.data.model.Drive
import com.haven.app.ui.components.HavenCard
import com.haven.app.ui.components.HavenToggle
import com.haven.app.ui.theme.LocalHavenColors
import com.haven.app.ui.theme.OutfitFamily
import com.haven.app.ui.theme.SpaceMonoFamily
import com.haven.app.ui.viewmodel.SafetyViewModel

@Composable
fun SafetyScreen(
    onDriveClick: (Drive) -> Unit,
    onBack: () -> Unit = {},
    viewModel: SafetyViewModel = hiltViewModel()
) {
    val t = LocalHavenColors.current
    val drives by viewModel.drives.collectAsStateWithLifecycle()
    val memberCount by viewModel.memberCount.collectAsStateWithLifecycle()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
    ) {
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
                "Driving", fontSize = 22.sp,
                fontWeight = FontWeight.ExtraBold, color = t.text,
                fontFamily = OutfitFamily, letterSpacing = (-0.5).sp
            )
        }

        Column(
            modifier = Modifier.padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Crash Detection Banner
            var crashOn by remember { mutableStateOf(true) }
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(20.dp))
                    .background(Brush.linearGradient(listOf(t.accent, t.accentMid)))
                    .padding(horizontal = 20.dp, vertical = 18.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Icon(Icons.Outlined.Shield, "Shield", Modifier.size(26.dp), tint = Color.White)
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        if (crashOn) "Crash Detection On" else "Crash Detection Off",
                        fontSize = 15.sp, fontWeight = FontWeight.ExtraBold,
                        color = Color.White, fontFamily = OutfitFamily
                    )
                    Text(
                        if (crashOn) "ALL $memberCount MEMBERS" else "DISABLED",
                        fontSize = 11.sp, color = Color.White.copy(alpha = 0.65f),
                        fontFamily = SpaceMonoFamily
                    )
                }
                HavenToggle(checked = crashOn, onCheckedChange = { crashOn = it })
            }

            // Trips header
            Text(
                "TRIPS", fontSize = 10.sp, fontWeight = FontWeight.Bold,
                color = t.textFade, fontFamily = SpaceMonoFamily, letterSpacing = 1.5.sp
            )

            if (drives.isEmpty()) {
                HavenCard(modifier = Modifier.fillMaxWidth()) {
                    Column(
                        modifier = Modifier.fillMaxWidth().padding(28.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(Icons.Outlined.DirectionsCar, "No trips", Modifier.size(36.dp), tint = t.textFade)
                        Spacer(Modifier.height(12.dp))
                        Text(
                            "No trips yet", fontSize = 16.sp, fontWeight = FontWeight.Bold,
                            color = t.text, fontFamily = OutfitFamily
                        )
                        Spacer(Modifier.height(4.dp))
                        Text(
                            "Trips are detected automatically when\nyou drive above 15 mph.",
                            fontSize = 12.sp, color = t.textMid, fontFamily = OutfitFamily,
                            lineHeight = 18.sp,
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                        )
                    }
                }
            } else {
                drives.forEach { drive ->
                    HavenCard(
                        modifier = Modifier.fillMaxWidth(),
                        onClick = { onDriveClick(drive) }
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            // Route + time
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Box(
                                    modifier = Modifier.size(38.dp)
                                        .background(t.accentBg, RoundedCornerShape(12.dp)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(Icons.Outlined.DirectionsCar, "Drive", Modifier.size(18.dp), tint = t.accent)
                                }
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        "${drive.fromLocation} to ${drive.toLocation}",
                                        fontSize = 14.sp, fontWeight = FontWeight.Bold,
                                        color = t.text, fontFamily = OutfitFamily
                                    )
                                    Text(
                                        "${drive.formattedDate()} - ${drive.memberName}",
                                        fontSize = 10.sp, color = t.textFade, fontFamily = SpaceMonoFamily
                                    )
                                }
                            }
                            Spacer(Modifier.height(12.dp))
                            // Stats row
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceEvenly
                            ) {
                                TripStat(label = "DIST", value = drive.formattedDistance(), color = t.text, t = t)
                                TripStat(label = "TIME", value = drive.formattedDuration(), color = t.text, t = t)
                                TripStat(
                                    label = "TOP", value = "${drive.topSpeedMph.toInt()} mph",
                                    color = if (drive.topSpeedMph > 80) t.danger else if (drive.topSpeedMph > 55) t.warn else t.ok, t = t
                                )
                                TripStat(
                                    label = "BRAKES", value = "${drive.harshBrakes}",
                                    color = if (drive.harshBrakes > 2) t.danger else if (drive.harshBrakes > 0) t.warn else t.ok, t = t
                                )
                            }
                        }
                    }
                }
            }
        }
        Spacer(Modifier.height(16.dp))
    }
}

@Composable
private fun TripStat(label: String, value: String, color: Color, t: com.haven.app.ui.theme.HavenColors) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(value, fontSize = 15.sp, fontWeight = FontWeight.Black, color = color, fontFamily = SpaceMonoFamily)
        Text(label, fontSize = 7.sp, fontWeight = FontWeight.Bold, color = t.textFade, fontFamily = SpaceMonoFamily, letterSpacing = 1.sp)
    }
}
