package com.haven.app.ui.dialogs

import android.content.Intent
import android.net.Uri
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.haven.app.ui.theme.OutfitFamily
import com.haven.app.ui.theme.SpaceMonoFamily

@Composable
fun SosReceivedOverlay(
    senderName: String,
    latitude: Double,
    longitude: Double,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current

    // Pulsing animation
    val infiniteTransition = rememberInfiniteTransition(label = "sos")
    val pulse by infiniteTransition.animateFloat(
        initialValue = 1f, targetValue = 1.15f,
        animationSpec = infiniteRepeatable(tween(800), RepeatMode.Reverse),
        label = "pulse"
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xEE991B1B)),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Pulsing warning icon
            Box(
                modifier = Modifier
                    .size(100.dp)
                    .scale(pulse)
                    .clip(CircleShape)
                    .background(Color.White.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Box(
                    modifier = Modifier
                        .size(72.dp)
                        .clip(CircleShape)
                        .background(Color.White.copy(alpha = 0.2f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Outlined.Warning, "SOS", Modifier.size(40.dp), tint = Color.White)
                }
            }

            Spacer(Modifier.height(28.dp))

            Text(
                "SOS ALERT", fontSize = 32.sp, fontWeight = FontWeight.Black,
                color = Color.White, fontFamily = OutfitFamily, letterSpacing = 2.sp
            )

            Spacer(Modifier.height(12.dp))

            Text(
                "$senderName needs help!", fontSize = 18.sp, fontWeight = FontWeight.Bold,
                color = Color.White.copy(alpha = 0.9f), fontFamily = OutfitFamily
            )

            Spacer(Modifier.height(24.dp))

            // Location info
            if (latitude != 0.0 || longitude != 0.0) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .background(Color.White.copy(alpha = 0.1f))
                        .padding(16.dp)
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Outlined.LocationOn, "Location", Modifier.size(18.dp), tint = Color.White.copy(alpha = 0.7f))
                            Text("LAST KNOWN LOCATION", fontSize = 10.sp, fontWeight = FontWeight.Bold,
                                color = Color.White.copy(alpha = 0.6f), fontFamily = SpaceMonoFamily, letterSpacing = 1.5.sp)
                        }
                        Spacer(Modifier.height(8.dp))
                        Text(
                            "%.6f, %.6f".format(latitude, longitude),
                            fontSize = 14.sp, fontWeight = FontWeight.Bold,
                            color = Color.White, fontFamily = SpaceMonoFamily
                        )
                    }
                }

                Spacer(Modifier.height(16.dp))

                // Navigate to them
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .background(Color.White.copy(alpha = 0.15f))
                        .clickable {
                            val uri = Uri.parse("geo:$latitude,$longitude?q=$latitude,$longitude(${Uri.encode(senderName)})")
                            context.startActivity(Intent(Intent.ACTION_VIEW, uri))
                        }
                        .padding(14.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Outlined.NearMe, "Navigate", Modifier.size(20.dp), tint = Color.White)
                        Text("Navigate to $senderName", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color.White, fontFamily = OutfitFamily)
                    }
                }
            }

            Spacer(Modifier.height(16.dp))

            // Call 911
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(Color.White)
                    .clickable {
                        val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:911"))
                        context.startActivity(intent)
                    }
                    .padding(16.dp),
                contentAlignment = Alignment.Center
            ) {
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp), verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Outlined.Phone, "Call", Modifier.size(22.dp), tint = Color(0xFFDC2626))
                    Text("Call 911", fontSize = 18.sp, fontWeight = FontWeight.Black, color = Color(0xFFDC2626), fontFamily = OutfitFamily)
                }
            }

            Spacer(Modifier.height(20.dp))

            // Dismiss
            Text(
                "Dismiss", fontSize = 14.sp, fontWeight = FontWeight.SemiBold,
                color = Color.White.copy(alpha = 0.5f), fontFamily = OutfitFamily,
                modifier = Modifier.clickable { onDismiss() }.padding(12.dp)
            )
        }
    }
}
