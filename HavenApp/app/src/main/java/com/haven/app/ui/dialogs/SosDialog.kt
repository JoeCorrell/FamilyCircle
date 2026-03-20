package com.haven.app.ui.dialogs

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Check
import androidx.compose.material.icons.outlined.Warning
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.haven.app.ui.theme.LocalHavenColors
import com.haven.app.ui.theme.OutfitFamily
import com.haven.app.ui.theme.SpaceMonoFamily
import kotlinx.coroutines.delay

enum class SosState { CONFIRM, COUNTDOWN, SUCCESS }

@Composable
fun SosDialog(
    onDismiss: () -> Unit,
    onActivate: () -> Unit
) {
    val t = LocalHavenColors.current
    var state by remember { mutableStateOf(SosState.CONFIRM) }
    var countdown by remember { mutableIntStateOf(5) }

    // Countdown timer
    LaunchedEffect(state) {
        if (state == SosState.COUNTDOWN) {
            countdown = 5
            while (countdown > 0) {
                delay(1000)
                countdown--
            }
            onActivate()
            state = SosState.SUCCESS
            delay(1400)
            onDismiss()
        }
    }

    Dialog(
        onDismissRequest = {
            if (state != SosState.SUCCESS) onDismiss()
        },
        properties = DialogProperties(dismissOnBackPress = state != SosState.SUCCESS)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(28.dp))
                .background(t.surface)
                .border(1.dp, t.border, RoundedCornerShape(28.dp))
                .padding(30.dp),
            contentAlignment = Alignment.Center
        ) {
            when (state) {
                SosState.CONFIRM -> {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Box(
                            modifier = Modifier
                                .size(64.dp)
                                .clip(RoundedCornerShape(20.dp))
                                .background(
                                    Brush.linearGradient(
                                        listOf(Color(0xFFDC2626), Color(0xFF991B1B))
                                    )
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                Icons.Outlined.Warning, "Emergency",
                                Modifier.size(28.dp), tint = Color.White
                            )
                        }
                        Spacer(Modifier.height(18.dp))
                        Text(
                            "Emergency", fontSize = 20.sp,
                            fontWeight = FontWeight.Black, color = t.danger,
                            fontFamily = OutfitFamily
                        )
                        Spacer(Modifier.height(10.dp))
                        Text(
                            "Alerts all family members with your live location and contacts emergency services.",
                            fontSize = 12.5.sp, color = t.textMid,
                            lineHeight = 20.sp, fontFamily = OutfitFamily,
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                        )
                        Spacer(Modifier.height(22.dp))
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(16.dp))
                                .background(
                                    Brush.linearGradient(
                                        listOf(Color(0xFFDC2626), Color(0xFF991B1B))
                                    )
                                )
                                .clickable { state = SosState.COUNTDOWN }
                                .padding(15.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                "Activate SOS", fontSize = 15.sp,
                                fontWeight = FontWeight.ExtraBold, color = Color.White,
                                fontFamily = OutfitFamily
                            )
                        }
                        Spacer(Modifier.height(6.dp))
                        Text(
                            "Cancel", fontSize = 13.sp, color = t.textFade,
                            fontFamily = OutfitFamily,
                            modifier = Modifier
                                .clickable { onDismiss() }
                                .padding(10.dp)
                        )
                    }
                }

                SosState.COUNTDOWN -> {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            "$countdown", fontSize = 56.sp,
                            fontWeight = FontWeight.Black, color = t.danger,
                            fontFamily = SpaceMonoFamily
                        )
                        Spacer(Modifier.height(8.dp))
                        Text(
                            "Sending alert...", fontSize = 14.sp,
                            fontWeight = FontWeight.SemiBold, color = t.text,
                            fontFamily = OutfitFamily
                        )
                        Spacer(Modifier.height(20.dp))
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(14.dp))
                                .background(t.card)
                                .border(1.dp, t.border, RoundedCornerShape(14.dp))
                                .clickable {
                                    state = SosState.CONFIRM
                                }
                                .padding(horizontal = 28.dp, vertical = 13.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                "Cancel", fontSize = 14.sp,
                                fontWeight = FontWeight.Bold, color = t.text,
                                fontFamily = OutfitFamily
                            )
                        }
                    }
                }

                SosState.SUCCESS -> {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Box(
                            modifier = Modifier
                                .size(64.dp)
                                .clip(RoundedCornerShape(20.dp))
                                .background(t.ok.copy(alpha = 0.08f))
                                .border(3.dp, t.ok, RoundedCornerShape(20.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                Icons.Outlined.Check, "Success",
                                Modifier.size(30.dp), tint = t.ok
                            )
                        }
                        Spacer(Modifier.height(16.dp))
                        Text(
                            "Alert Sent", fontSize = 18.sp,
                            fontWeight = FontWeight.ExtraBold, color = t.ok,
                            fontFamily = OutfitFamily
                        )
                        Spacer(Modifier.height(8.dp))
                        Text(
                            "All members notified", fontSize = 12.sp,
                            color = t.textMid, fontFamily = OutfitFamily
                        )
                    }
                }
            }
        }
    }
}
