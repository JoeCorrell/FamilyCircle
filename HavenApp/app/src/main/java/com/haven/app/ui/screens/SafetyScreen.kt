package com.haven.app.ui.screens

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Shield
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
import com.haven.app.ui.components.ScoreArc
import com.haven.app.ui.theme.LocalHavenColors
import com.haven.app.ui.theme.OutfitFamily
import com.haven.app.ui.theme.SpaceMonoFamily
import androidx.compose.material.icons.outlined.ChevronLeft
import com.haven.app.ui.components.HavenToggle
import com.haven.app.ui.viewmodel.SafetyViewModel

@Composable
fun SafetyScreen(
    onDriveClick: (Drive) -> Unit,
    onBack: () -> Unit = {},
    viewModel: SafetyViewModel = hiltViewModel()
) {
    val t = LocalHavenColors.current
    val drives by viewModel.drives.collectAsStateWithLifecycle()
    val familyScore by viewModel.familyScore.collectAsStateWithLifecycle()
    val weeklyScores by viewModel.weeklyScores.collectAsStateWithLifecycle()
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
                "Safety", fontSize = 22.sp,
                fontWeight = FontWeight.ExtraBold,
                color = t.text,
                fontFamily = OutfitFamily,
                letterSpacing = (-0.5).sp
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

            // Weekly Score Card
            HavenCard(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(horizontal = 18.dp, vertical = 16.dp)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        ScoreArc(value = familyScore, size = 58.dp)
                        Column {
                            Text(
                                "Family Score", fontSize = 15.sp,
                                fontWeight = FontWeight.Bold, color = t.text,
                                fontFamily = OutfitFamily
                            )
                            Text(
                                if (familyScore >= 90) "EXCELLENT" else if (familyScore >= 70) "GOOD" else "NEEDS WORK",
                                fontSize = 11.sp,
                                color = if (familyScore >= 90) t.ok else if (familyScore >= 70) t.warn else t.danger,
                                fontFamily = SpaceMonoFamily
                            )
                        }
                    }
                    Spacer(Modifier.height(14.dp))

                    // Weekly bar chart
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(36.dp),
                        horizontalArrangement = Arrangement.spacedBy(3.dp),
                        verticalAlignment = Alignment.Bottom
                    ) {
                        weeklyScores.forEachIndexed { index, score ->
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .fillMaxHeight(score / 100f)
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(
                                        if (index == weeklyScores.lastIndex) t.accent
                                        else t.accent.copy(alpha = 0.16f)
                                    )
                            )
                        }
                    }
                    Spacer(Modifier.height(4.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        listOf("M", "T", "W", "T", "F", "S", "S").forEach { day ->
                            Text(
                                day, modifier = Modifier.weight(1f),
                                fontSize = 8.sp, color = t.textFade,
                                fontFamily = SpaceMonoFamily,
                                textAlign = androidx.compose.ui.text.style.TextAlign.Center
                            )
                        }
                    }
                }
            }

            // Drives header
            Text(
                "DRIVES",
                fontSize = 10.sp, fontWeight = FontWeight.Bold,
                color = t.textFade, fontFamily = SpaceMonoFamily,
                letterSpacing = 1.5.sp
            )

            // Drive list
            if (drives.isEmpty()) {
                HavenCard(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        "No drives recorded yet.\nDrives will appear here automatically when detected.",
                        modifier = Modifier.padding(20.dp),
                        fontSize = 13.sp, color = t.textMid,
                        fontFamily = OutfitFamily,
                        lineHeight = 20.sp
                    )
                }
            } else {
                drives.forEach { drive ->
                    HavenCard(
                        modifier = Modifier.fillMaxWidth(),
                        onClick = { onDriveClick(drive) }
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(14.dp)
                        ) {
                            ScoreArc(value = drive.score, size = 40.dp)
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    "${drive.memberName} \u2014 ${drive.fromLocation} \u2192 ${drive.toLocation}",
                                    fontSize = 13.sp, fontWeight = FontWeight.SemiBold,
                                    color = t.text, fontFamily = OutfitFamily
                                )
                                Text(
                                    "${drive.formattedDate()} \u2022 ${drive.formattedDistance()}",
                                    fontSize = 10.sp, color = t.textFade,
                                    fontFamily = SpaceMonoFamily
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
