package com.haven.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ChevronLeft
import androidx.compose.material.icons.outlined.DirectionsCar
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.haven.app.data.model.Drive
import com.haven.app.ui.components.HavenCard
import com.haven.app.ui.theme.LocalHavenColors
import com.haven.app.ui.theme.OutfitFamily
import com.haven.app.ui.theme.SpaceMonoFamily

@Composable
fun DriveDetailScreen(
    drive: Drive,
    onBack: () -> Unit
) {
    val t = LocalHavenColors.current

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
                "Trip Details", fontSize = 22.sp,
                fontWeight = FontWeight.ExtraBold, color = t.text,
                fontFamily = OutfitFamily, letterSpacing = (-0.5).sp
            )
        }

        Column(
            modifier = Modifier.padding(horizontal = 18.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Hero card with route
            HavenCard(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.fillMaxWidth()) {
                    // Gradient header
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp))
                            .background(Brush.linearGradient(listOf(t.accent, t.accentMid)))
                            .padding(20.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(14.dp)
                        ) {
                            Icon(Icons.Outlined.DirectionsCar, "Car", Modifier.size(28.dp), tint = Color.White)
                            Column {
                                Text(drive.memberName, fontSize = 12.sp, color = Color.White.copy(alpha = 0.7f), fontFamily = SpaceMonoFamily)
                                Text(
                                    "${drive.fromLocation} to ${drive.toLocation}",
                                    fontSize = 18.sp, fontWeight = FontWeight.Bold,
                                    color = Color.White, fontFamily = OutfitFamily
                                )
                            }
                        }
                    }
                    // Date
                    Text(
                        drive.formattedDate(),
                        modifier = Modifier.padding(horizontal = 20.dp, vertical = 12.dp),
                        fontSize = 11.sp, color = t.textFade, fontFamily = SpaceMonoFamily
                    )
                }
            }

            // Stats grid
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                StatCard("Distance", drive.formattedDistance(), t.text, t, Modifier.weight(1f))
                StatCard("Duration", drive.formattedDuration(), t.text, t, Modifier.weight(1f))
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                StatCard(
                    "Top Speed", "${drive.topSpeedMph.toInt()} mph",
                    if (drive.topSpeedMph > 80) t.danger else if (drive.topSpeedMph > 55) t.warn else t.ok,
                    t, Modifier.weight(1f)
                )
                StatCard(
                    "Hard Brakes", "${drive.harshBrakes}",
                    if (drive.harshBrakes > 2) t.danger else if (drive.harshBrakes > 0) t.warn else t.ok,
                    t, Modifier.weight(1f)
                )
            }
        }
        Spacer(Modifier.height(18.dp))
    }
}

@Composable
private fun StatCard(label: String, value: String, valueColor: Color, t: com.haven.app.ui.theme.HavenColors, modifier: Modifier = Modifier) {
    HavenCard(modifier = modifier) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(vertical = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(label.uppercase(), fontSize = 9.sp, fontWeight = FontWeight.Bold, color = t.textFade, fontFamily = SpaceMonoFamily, letterSpacing = 1.sp)
            Spacer(Modifier.height(8.dp))
            Text(value, fontSize = 22.sp, fontWeight = FontWeight.Black, color = valueColor, fontFamily = SpaceMonoFamily)
        }
    }
}
