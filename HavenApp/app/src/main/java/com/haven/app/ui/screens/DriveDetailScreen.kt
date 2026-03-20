package com.haven.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ChevronLeft
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.haven.app.data.model.Drive
import com.haven.app.ui.components.HavenCard
import com.haven.app.ui.components.ScoreArc
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
                "Drive", fontSize = 22.sp,
                fontWeight = FontWeight.ExtraBold, color = t.text,
                fontFamily = OutfitFamily, letterSpacing = (-0.5).sp
            )
        }

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 18.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Big score
            ScoreArc(value = drive.score, size = 84.dp)
            Text(
                "DRIVE SCORE",
                fontSize = 10.sp, fontWeight = FontWeight.Bold,
                color = t.textFade, fontFamily = SpaceMonoFamily,
                letterSpacing = 1.5.sp
            )

            // Route card
            HavenCard(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp)) {
                    Text(
                        "ROUTE", fontSize = 10.sp, fontWeight = FontWeight.Bold,
                        color = t.textFade, fontFamily = SpaceMonoFamily, letterSpacing = 1.5.sp
                    )
                    Spacer(Modifier.height(6.dp))
                    Text(
                        "${drive.fromLocation} \u2192 ${drive.toLocation}",
                        fontSize = 15.sp, fontWeight = FontWeight.Bold,
                        color = t.text, fontFamily = OutfitFamily
                    )
                    Spacer(Modifier.height(3.dp))
                    Text(
                        drive.formattedDate(),
                        fontSize = 10.sp, color = t.textFade, fontFamily = SpaceMonoFamily
                    )
                }
            }

            // Stats grid
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                data class Stat(val label: String, val value: String, val color: androidx.compose.ui.graphics.Color? = null)
                listOf(
                    Stat("DIST", drive.formattedDistance()),
                    Stat("TIME", drive.formattedDuration())
                ).forEach { stat ->
                    HavenCard(modifier = Modifier.weight(1f)) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 14.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                stat.label, fontSize = 10.sp, fontWeight = FontWeight.Bold,
                                color = t.textFade, fontFamily = SpaceMonoFamily, letterSpacing = 1.5.sp
                            )
                            Spacer(Modifier.height(6.dp))
                            Text(
                                stat.value, fontSize = 18.sp, fontWeight = FontWeight.ExtraBold,
                                color = stat.color ?: t.text, fontFamily = SpaceMonoFamily
                            )
                        }
                    }
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                HavenCard(modifier = Modifier.weight(1f)) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 14.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            "TOP", fontSize = 10.sp, fontWeight = FontWeight.Bold,
                            color = t.textFade, fontFamily = SpaceMonoFamily, letterSpacing = 1.5.sp
                        )
                        Spacer(Modifier.height(6.dp))
                        Text(
                            "${drive.topSpeedMph}mph", fontSize = 18.sp, fontWeight = FontWeight.ExtraBold,
                            color = if (drive.topSpeedMph > 55) t.warn else t.ok,
                            fontFamily = SpaceMonoFamily
                        )
                    }
                }
                HavenCard(modifier = Modifier.weight(1f)) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 14.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            "BRAKES", fontSize = 10.sp, fontWeight = FontWeight.Bold,
                            color = t.textFade, fontFamily = SpaceMonoFamily, letterSpacing = 1.5.sp
                        )
                        Spacer(Modifier.height(6.dp))
                        Text(
                            "${drive.harshBrakes}", fontSize = 18.sp, fontWeight = FontWeight.ExtraBold,
                            color = if (drive.harshBrakes > 1) t.danger else t.ok,
                            fontFamily = SpaceMonoFamily
                        )
                    }
                }
            }
        }
        Spacer(Modifier.height(18.dp))
    }
}
