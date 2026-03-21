package com.haven.app.ui.screens

import android.widget.Toast
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ChevronLeft
import androidx.compose.material.icons.outlined.OpenInNew
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.haven.app.ui.components.HavenCard
import com.haven.app.ui.theme.LocalHavenColors
import com.haven.app.ui.theme.OutfitFamily
import com.haven.app.ui.theme.SpaceMonoFamily

@Composable
fun AboutScreen(onBack: () -> Unit) {
    val t = LocalHavenColors.current
    val context = LocalContext.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
    ) {
        // Header
        Text(
            "About", fontSize = 22.sp,
            fontWeight = FontWeight.ExtraBold, color = t.text,
            fontFamily = OutfitFamily, letterSpacing = (-0.5).sp,
            modifier = Modifier.padding(horizontal = 20.dp, vertical = 12.dp)
        )

        Column(
            modifier = Modifier.padding(horizontal = 18.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Logo hero
            HavenCard(modifier = Modifier.fillMaxWidth()) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 32.dp, horizontal = 20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Box(
                        modifier = Modifier
                            .size(68.dp)
                            .clip(RoundedCornerShape(20.dp))
                            .background(Brush.linearGradient(listOf(t.accent, t.accentMid))),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            "H", fontSize = 24.sp, fontWeight = FontWeight.Black,
                            color = Color.White, fontFamily = SpaceMonoFamily, letterSpacing = 3.sp
                        )
                    }
                    Spacer(Modifier.height(16.dp))
                    Text(
                        "HAVEN", fontSize = 22.sp, fontWeight = FontWeight.Black,
                        color = t.text, fontFamily = SpaceMonoFamily, letterSpacing = 3.sp
                    )
                    Spacer(Modifier.height(6.dp))
                    Text(
                        "FAMILY SAFETY REIMAGINED", fontSize = 10.sp,
                        color = t.textFade, fontFamily = SpaceMonoFamily, letterSpacing = 1.5.sp
                    )
                    Spacer(Modifier.height(12.dp))
                    Box(
                        modifier = Modifier
                            .background(t.accentBg, RoundedCornerShape(10.dp))
                            .padding(horizontal = 14.dp, vertical = 5.dp)
                    ) {
                        Text(
                            "v1.0", fontSize = 11.sp, fontWeight = FontWeight.Bold,
                            color = t.accent, fontFamily = SpaceMonoFamily
                        )
                    }
                }
            }

            // Info rows
            listOf(
                "Version" to "1.0.0",
                "Platform" to "iOS & Android",
                "Release" to "March 2026"
            ).forEach { (label, value) ->
                HavenCard(modifier = Modifier.fillMaxWidth()) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 18.dp, vertical = 13.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(label, fontSize = 13.sp, color = t.textMid, fontFamily = OutfitFamily)
                        Text(value, fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = t.text, fontFamily = SpaceMonoFamily)
                    }
                }
            }

            // Legal section
            Text(
                "LEGAL",
                modifier = Modifier.padding(top = 6.dp, bottom = 2.dp, start = 4.dp),
                fontSize = 10.sp, fontWeight = FontWeight.Bold,
                color = t.textFade, fontFamily = SpaceMonoFamily, letterSpacing = 1.5.sp
            )

            listOf("Terms of Service", "Privacy Policy", "Open Source Licenses").forEach { label ->
                HavenCard(
                    modifier = Modifier.fillMaxWidth(),
                    onClick = { Toast.makeText(context, "Opening $label...", Toast.LENGTH_SHORT).show() }
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 18.dp, vertical = 13.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(label, fontSize = 13.sp, color = t.text, fontFamily = OutfitFamily)
                        Icon(Icons.Outlined.OpenInNew, "Open", Modifier.size(14.dp), tint = t.textFade)
                    }
                }
            }

            // Footer
            Text(
                "Haven keeps families connected and safe.\nYour location data is encrypted end-to-end.",
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 12.dp),
                fontSize = 10.sp, color = t.textFade,
                fontFamily = OutfitFamily,
                lineHeight = 16.sp,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )
        }
        Spacer(Modifier.height(18.dp))
    }
}
