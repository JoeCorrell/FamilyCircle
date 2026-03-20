package com.haven.app.ui.screens

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
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
import com.haven.app.ui.components.HavenCard
import com.haven.app.ui.theme.LocalHavenColors
import com.haven.app.ui.theme.OutfitFamily
import com.haven.app.ui.theme.SpaceMonoFamily
import com.haven.app.ui.viewmodel.SavedPlacesViewModel

@Composable
fun SavedPlacesScreen(
    onBack: () -> Unit,
    onAddPlace: () -> Unit,
    viewModel: SavedPlacesViewModel = hiltViewModel()
) {
    val t = LocalHavenColors.current
    val places by viewModel.places.collectAsStateWithLifecycle()
    var confirmDeleteId by remember { mutableStateOf<String?>(null) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
    ) {
        // Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
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
                    "Saved Places", fontSize = 22.sp,
                    fontWeight = FontWeight.ExtraBold, color = t.text,
                    fontFamily = OutfitFamily, letterSpacing = (-0.5).sp
                )
            }
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .background(t.accentBg, RoundedCornerShape(10.dp))
                    .clickable { onAddPlace() },
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Outlined.Add, "Add", Modifier.size(16.dp), tint = t.accent)
            }
        }

        Column(
            modifier = Modifier.padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            if (places.isEmpty()) {
                // Empty state
                HavenCard(modifier = Modifier.fillMaxWidth()) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(32.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Box(
                            modifier = Modifier
                                .size(56.dp)
                                .background(t.accentBg, RoundedCornerShape(16.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Outlined.LocationOn, "Places", Modifier.size(28.dp), tint = t.accent)
                        }
                        Spacer(Modifier.height(16.dp))
                        Text(
                            "No Saved Places", fontSize = 16.sp,
                            fontWeight = FontWeight.Bold, color = t.text, fontFamily = OutfitFamily
                        )
                        Spacer(Modifier.height(6.dp))
                        Text(
                            "Add places like Home, Work, or School to get arrival and departure alerts.",
                            fontSize = 12.sp, color = t.textMid, fontFamily = OutfitFamily,
                            lineHeight = 18.sp,
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                        )
                        Spacer(Modifier.height(16.dp))
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(12.dp))
                                .background(Brush.linearGradient(listOf(t.accent, t.accentMid)))
                                .clickable { onAddPlace() }
                                .padding(horizontal = 24.dp, vertical = 12.dp)
                        ) {
                            Text(
                                "Add Your First Place", fontSize = 13.sp,
                                fontWeight = FontWeight.Bold, color = Color.White, fontFamily = OutfitFamily
                            )
                        }
                    }
                }
            } else {
                places.forEach { place ->
                    val placeColor = Color(place.color)
                    val isConfirming = confirmDeleteId == place.id

                    HavenCard(modifier = Modifier.fillMaxWidth()) {
                        Row(
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(14.dp)
                        ) {
                            // Color dot
                            Box(
                                modifier = Modifier
                                    .size(42.dp)
                                    .background(placeColor.copy(alpha = 0.12f), RoundedCornerShape(14.dp)),
                                contentAlignment = Alignment.Center
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(14.dp)
                                        .background(placeColor, RoundedCornerShape(7.dp))
                                )
                            }

                            // Info
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    place.name, fontSize = 15.sp,
                                    fontWeight = FontWeight.SemiBold, color = t.text, fontFamily = OutfitFamily
                                )
                                Text(
                                    place.address.ifEmpty { "No address" },
                                    fontSize = 11.sp, color = t.textFade, fontFamily = OutfitFamily,
                                    maxLines = 1
                                )
                                Text(
                                    "${place.radiusMeters.toInt()}m radius",
                                    fontSize = 9.sp, color = t.textFade, fontFamily = SpaceMonoFamily,
                                    letterSpacing = 0.5.sp
                                )
                            }

                            // Delete button / confirm
                            if (isConfirming) {
                                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(8.dp))
                                            .background(if (t.isDark) t.surfaceAlt else t.bgSub)
                                            .clickable { confirmDeleteId = null }
                                            .padding(horizontal = 10.dp, vertical = 5.dp)
                                    ) {
                                        Text("No", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = t.textMid, fontFamily = SpaceMonoFamily)
                                    }
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(8.dp))
                                            .background(t.danger.copy(alpha = 0.12f))
                                            .clickable {
                                                viewModel.deletePlace(place.id)
                                                confirmDeleteId = null
                                            }
                                            .padding(horizontal = 10.dp, vertical = 5.dp)
                                    ) {
                                        Text("Delete", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = t.danger, fontFamily = SpaceMonoFamily)
                                    }
                                }
                            } else {
                                Box(
                                    modifier = Modifier
                                        .size(32.dp)
                                        .background(t.danger.copy(alpha = 0.06f), RoundedCornerShape(10.dp))
                                        .clickable { confirmDeleteId = place.id },
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(Icons.Outlined.Delete, "Delete", Modifier.size(16.dp), tint = t.danger)
                                }
                            }
                        }
                    }
                }

                // Add more button
                Spacer(Modifier.height(4.dp))
                HavenCard(
                    modifier = Modifier.fillMaxWidth(),
                    onClick = onAddPlace
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Outlined.Add, "Add", Modifier.size(16.dp), tint = t.accent)
                        Spacer(Modifier.width(8.dp))
                        Text(
                            "Add Another Place", fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold, color = t.accent, fontFamily = OutfitFamily
                        )
                    }
                }
            }
        }
        Spacer(Modifier.height(24.dp))
    }
}
