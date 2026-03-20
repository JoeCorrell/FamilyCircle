package com.haven.app.ui.screens

import android.Manifest
import android.annotation.SuppressLint
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.spring
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
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
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.haven.app.ui.components.ProfileImage
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.*
import com.haven.app.data.model.FamilyMember
import com.haven.app.ui.components.BatteryIndicator
import com.haven.app.ui.components.HavenCard
import com.haven.app.ui.theme.LocalHavenColors
import com.haven.app.ui.theme.OutfitFamily
import com.haven.app.ui.theme.SpaceMonoFamily
import com.haven.app.ui.viewmodel.MapViewModel
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

@SuppressLint("MissingPermission")
@Composable
fun MapScreen(
    onMemberClick: (FamilyMember) -> Unit,
    viewModel: MapViewModel = hiltViewModel()
) {
    val t = LocalHavenColors.current
    val context = LocalContext.current
    val members by viewModel.members.collectAsStateWithLifecycle()
    val places by viewModel.places.collectAsStateWithLifecycle()
    val selectedMember by viewModel.selectedMember.collectAsStateWithLifecycle()
    val scope = rememberCoroutineScope()

    val hasLocationPermission = ContextCompat.checkSelfPermission(
        context, Manifest.permission.ACCESS_FINE_LOCATION
    ) == PackageManager.PERMISSION_GRANTED

    var isSatellite by remember { mutableStateOf(false) }

    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(LatLng(40.7580, -73.9855), 13f)
    }

    LaunchedEffect(hasLocationPermission) {
        if (hasLocationPermission) {
            try {
                val loc = LocationServices.getFusedLocationProviderClient(context).lastLocation.await()
                if (loc != null) {
                    cameraPositionState.animate(CameraUpdateFactory.newLatLngZoom(LatLng(loc.latitude, loc.longitude), 15f), 800)
                }
            } catch (_: Exception) {}
        }
    }

    Column(modifier = Modifier.fillMaxSize().background(t.bg)) {
        // ═══ MAP SECTION ═══
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
        ) {
            // Map
            GoogleMap(
                modifier = Modifier.fillMaxSize(),
                cameraPositionState = cameraPositionState,
                properties = MapProperties(
                    isMyLocationEnabled = hasLocationPermission,
                    mapType = if (isSatellite) MapType.HYBRID else MapType.NORMAL
                ),
                uiSettings = MapUiSettings(
                    zoomControlsEnabled = false, zoomGesturesEnabled = true,
                    scrollGesturesEnabled = true, rotationGesturesEnabled = true,
                    tiltGesturesEnabled = true, myLocationButtonEnabled = false, compassEnabled = false
                ),
                onMapClick = { viewModel.selectMember(null) }
            ) {
                // Place geofence circles
                places.forEach { place ->
                    if (place.lat != 0.0 || place.lng != 0.0) {
                        val placeColor = Color(place.color)
                        Circle(
                            center = LatLng(place.lat, place.lng),
                            radius = place.radiusMeters.toDouble(),
                            fillColor = placeColor.copy(alpha = 0.12f),
                            strokeColor = placeColor.copy(alpha = 0.4f),
                            strokeWidth = 3f
                        )
                        MarkerComposable(
                            state = MarkerState(position = LatLng(place.lat, place.lng)),
                            title = place.name, onClick = { true }
                        ) {
                            Row(
                                modifier = Modifier.shadow(4.dp, RoundedCornerShape(10.dp))
                                    .background(Color.White, RoundedCornerShape(10.dp))
                                    .border(1.dp, placeColor.copy(alpha = 0.3f), RoundedCornerShape(10.dp))
                                    .padding(horizontal = 8.dp, vertical = 5.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Box(modifier = Modifier.size(6.dp).background(placeColor, CircleShape))
                                Text(place.name, fontSize = 9.sp, fontWeight = FontWeight.Bold, color = placeColor, fontFamily = SpaceMonoFamily)
                            }
                        }
                    }
                }

                // Member markers
                members.forEach { member ->
                    if (member.latitude != 0.0 || member.longitude != 0.0) {
                        key(member.id) {
                            val pos = LatLng(member.latitude, member.longitude)
                            val isSel = selectedMember?.id == member.id
                            val mc = Color(member.color)
                            MarkerComposable(
                                state = MarkerState(position = pos),
                                title = member.name,
                                onClick = {
                                    viewModel.selectMember(member)
                                    scope.launch { cameraPositionState.animate(CameraUpdateFactory.newLatLngZoom(pos, 16f), 600) }
                                    true
                                }
                            ) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Box(
                                            modifier = Modifier
                                                .size(if (isSel) 52.dp else 44.dp)
                                                .shadow(if (isSel) 10.dp else 4.dp, CircleShape, ambientColor = mc)
                                                .background(if (isSel) mc else Color.White, CircleShape)
                                                .border(3.dp, mc, CircleShape),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            if (member.photoUrl.isNotEmpty()) {
                                                ProfileImage(photoUrl = member.photoUrl, contentDescription = member.name, contentScale = ContentScale.Crop,
                                                    modifier = Modifier.size(if (isSel) 46.dp else 38.dp).clip(CircleShape))
                                            } else {
                                                Text(member.initials, fontSize = if (isSel) 20.sp else 16.sp, fontWeight = FontWeight.Black,
                                                    color = if (isSel) Color.White else mc, fontFamily = OutfitFamily)
                                            }
                                        }
                                        if (member.isOnline) {
                                            Box(modifier = Modifier.align(Alignment.BottomEnd).size(12.dp).background(t.ok, CircleShape).border(2.dp, Color.White, CircleShape))
                                        }
                                    }
                                    if (member.speed > 0) {
                                        Box(modifier = Modifier.background(t.warn, RoundedCornerShape(6.dp)).padding(horizontal = 5.dp, vertical = 1.dp)) {
                                            Text("${member.speed.toInt()} mph", fontSize = 7.sp, fontWeight = FontWeight.Bold, color = Color.White)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // Floating controls
            Row(
                modifier = Modifier.align(Alignment.TopEnd).padding(12.dp),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                // LIVE badge
                Row(
                    modifier = Modifier
                        .shadow(4.dp, RoundedCornerShape(10.dp))
                        .background(t.card.copy(alpha = 0.92f), RoundedCornerShape(10.dp))
                        .border(1.dp, t.border, RoundedCornerShape(10.dp))
                        .padding(horizontal = 10.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(5.dp)
                ) {
                    Box(modifier = Modifier.size(6.dp).background(t.ok, CircleShape))
                    Text("LIVE", fontSize = 8.sp, fontWeight = FontWeight.Bold, color = t.ok, fontFamily = SpaceMonoFamily)
                }
                Spacer(Modifier.weight(1f))
                Box(
                    modifier = Modifier.size(36.dp).shadow(4.dp, RoundedCornerShape(11.dp))
                        .background(if (isSatellite) t.accent else t.card.copy(alpha = 0.92f), RoundedCornerShape(11.dp))
                        .border(1.dp, if (isSatellite) t.accent else t.border, RoundedCornerShape(11.dp))
                        .clickable { isSatellite = !isSatellite },
                    contentAlignment = Alignment.Center
                ) { Icon(Icons.Outlined.Satellite, "Satellite", Modifier.size(16.dp), tint = if (isSatellite) Color.White else t.text) }
                Box(
                    modifier = Modifier.size(36.dp).shadow(4.dp, RoundedCornerShape(11.dp))
                        .background(t.card.copy(alpha = 0.92f), RoundedCornerShape(11.dp))
                        .border(1.dp, t.border, RoundedCornerShape(11.dp))
                        .clickable {
                            if (hasLocationPermission) {
                                scope.launch {
                                    try {
                                        val loc = LocationServices.getFusedLocationProviderClient(context).lastLocation.await()
                                        if (loc != null) cameraPositionState.animate(CameraUpdateFactory.newLatLngZoom(LatLng(loc.latitude, loc.longitude), 15f), 600)
                                    } catch (_: Exception) {}
                                }
                            }
                        },
                    contentAlignment = Alignment.Center
                ) { Icon(Icons.Outlined.MyLocation, "Recenter", Modifier.size(16.dp), tint = t.text) }
            }
        }

        // ═══ MEMBER PANEL ═══
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .animateContentSize(spring(dampingRatio = 0.8f, stiffness = 400f))
                .background(
                    t.card,
                    RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
                )
                .border(
                    1.dp, t.border,
                    RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
                )
        ) {
            // Handle bar
            Box(
                modifier = Modifier.fillMaxWidth().padding(top = 8.dp, bottom = 4.dp),
                contentAlignment = Alignment.Center
            ) {
                Box(modifier = Modifier.width(40.dp).height(4.dp).background(t.border, RoundedCornerShape(2.dp)))
            }

            // Selected member detail or member list
            selectedMember?.let { member ->
                val memberColor = Color(member.color)
                val history by viewModel.selectedMemberHistory.collectAsStateWithLifecycle()

                Column(
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    // Member info row
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Box(
                            modifier = Modifier.size(48.dp)
                                .background(memberColor.copy(alpha = 0.1f), RoundedCornerShape(15.dp))
                                .border(2.5.dp, memberColor, RoundedCornerShape(15.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            if (member.photoUrl.isNotEmpty()) {
                                ProfileImage(photoUrl = member.photoUrl, contentDescription = member.name, contentScale = ContentScale.Crop,
                                    modifier = Modifier.size(42.dp).clip(RoundedCornerShape(13.dp)))
                            } else {
                                Text(member.initials, fontSize = 19.sp, fontWeight = FontWeight.Black, color = memberColor, fontFamily = OutfitFamily)
                            }
                        }
                        Column(modifier = Modifier.weight(1f)) {
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                Text(member.name, fontSize = 16.sp, fontWeight = FontWeight.Bold, color = t.text, fontFamily = OutfitFamily)
                                Box(
                                    modifier = Modifier
                                        .background(if (member.speed > 0) t.warn.copy(alpha = 0.12f) else t.ok.copy(alpha = 0.12f), RoundedCornerShape(6.dp))
                                        .padding(horizontal = 7.dp, vertical = 2.dp)
                                ) {
                                    Text(member.status.displayName().uppercase(), fontSize = 8.sp, fontWeight = FontWeight.Bold,
                                        color = if (member.speed > 0) t.warn else t.ok, fontFamily = SpaceMonoFamily)
                                }
                            }
                            Text(member.currentAddress.ifEmpty { "Unknown" }, fontSize = 11.sp, color = t.textMid, fontFamily = OutfitFamily, maxLines = 1, overflow = TextOverflow.Ellipsis)
                        }
                        Box(
                            modifier = Modifier.size(30.dp).background(if (t.isDark) t.surfaceAlt else t.bgSub, RoundedCornerShape(9.dp))
                                .border(1.dp, t.border, RoundedCornerShape(9.dp)).clickable { viewModel.selectMember(null) },
                            contentAlignment = Alignment.Center
                        ) { Icon(Icons.Outlined.Close, "Close", Modifier.size(13.dp), tint = t.textFade) }
                    }

                    // Stats
                    Row(
                        modifier = Modifier.fillMaxWidth().background(if (t.isDark) t.surfaceAlt else t.bgSub, RoundedCornerShape(14.dp)).padding(vertical = 10.dp),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            BatteryIndicator(value = member.batteryLevel, size = 24.dp)
                            Text("${member.batteryLevel}%", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = t.textMid, fontFamily = SpaceMonoFamily)
                        }
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("${member.speed.toInt()}", fontSize = 20.sp, fontWeight = FontWeight.Black, color = if (member.speed > 0) t.warn else t.ok, fontFamily = SpaceMonoFamily)
                            Text("MPH", fontSize = 8.sp, color = t.textFade, fontFamily = SpaceMonoFamily)
                        }
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            val seen = if (member.isOnline) "Now" else {
                                val m = ((System.currentTimeMillis() - member.lastSeenTimestamp) / 60000).toInt()
                                when { m < 1 -> "Now"; m < 60 -> "${m}m"; else -> "${m / 60}h" }
                            }
                            Text(seen, fontSize = 20.sp, fontWeight = FontWeight.Black, color = t.accent, fontFamily = SpaceMonoFamily)
                            Text("SEEN", fontSize = 8.sp, color = t.textFade, fontFamily = SpaceMonoFamily)
                        }
                    }

                    // Actions
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Box(
                            modifier = Modifier.weight(1f).clip(RoundedCornerShape(12.dp))
                                .background(Brush.linearGradient(listOf(t.accent, t.accentMid)))
                                .clickable { onMemberClick(member) }.padding(vertical = 10.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Row(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Outlined.Person, "Details", Modifier.size(14.dp), tint = Color.White)
                                Text("Profile", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.White, fontFamily = OutfitFamily)
                            }
                        }
                        Box(
                            modifier = Modifier.weight(1f).clip(RoundedCornerShape(12.dp))
                                .background(if (t.isDark) t.surfaceAlt else t.bgSub)
                                .border(1.dp, t.border, RoundedCornerShape(12.dp))
                                .clickable {
                                    if (member.latitude != 0.0 || member.longitude != 0.0) {
                                        context.startActivity(Intent(Intent.ACTION_VIEW,
                                            Uri.parse("geo:${member.latitude},${member.longitude}?q=${member.latitude},${member.longitude}(${Uri.encode(member.name)})")))
                                    }
                                }.padding(vertical = 10.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Row(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Outlined.NearMe, "Navigate", Modifier.size(14.dp), tint = t.text)
                                Text("Navigate", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = t.text, fontFamily = OutfitFamily)
                            }
                        }
                    }

                    // Recent locations
                    if (history.isNotEmpty()) {
                        Text("RECENT", fontSize = 8.sp, fontWeight = FontWeight.Bold, color = t.textFade, fontFamily = SpaceMonoFamily, letterSpacing = 1.sp)
                        history.take(3).forEach { entry ->
                            Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                Box(modifier = Modifier.size(5.dp).background(t.accent.copy(alpha = 0.4f), CircleShape))
                                Text(entry.address, fontSize = 10.sp, color = t.textMid, fontFamily = OutfitFamily, modifier = Modifier.weight(1f), maxLines = 1, overflow = TextOverflow.Ellipsis)
                                if (entry.speed > 2) Text("${entry.speed.toInt()}mph", fontSize = 8.sp, fontWeight = FontWeight.Bold, color = t.warn, fontFamily = SpaceMonoFamily)
                                val mins = ((System.currentTimeMillis() - entry.timestamp) / 60000).toInt()
                                Text(when { mins < 1 -> "now"; mins < 60 -> "${mins}m"; mins < 1440 -> "${mins / 60}h"; else -> "${mins / 1440}d" },
                                    fontSize = 8.sp, color = t.textFade, fontFamily = SpaceMonoFamily)
                            }
                        }
                    }
                }
                Spacer(Modifier.height(8.dp))

            } ?: run {
                // ── Member Cards Row (no selection) ──
                LazyRow(
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(members, key = { it.id }) { member ->
                        val mc = Color(member.color)
                        HavenCard(
                            modifier = Modifier.width(80.dp),
                            cornerRadius = 16.dp,
                            onClick = {
                                viewModel.selectMember(member)
                                if (member.latitude != 0.0 || member.longitude != 0.0) {
                                    scope.launch { cameraPositionState.animate(CameraUpdateFactory.newLatLngZoom(LatLng(member.latitude, member.longitude), 16f), 800) }
                                }
                            }
                        ) {
                            Column(
                                modifier = Modifier.fillMaxWidth().padding(8.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Box(modifier = Modifier.size(48.dp)) {
                                    if (member.photoUrl.isNotEmpty()) {
                                        ProfileImage(
                                            photoUrl = member.photoUrl, contentDescription = member.name,
                                            contentScale = ContentScale.Crop,
                                            modifier = Modifier.size(48.dp).clip(RoundedCornerShape(14.dp)).border(2.5.dp, mc, RoundedCornerShape(14.dp))
                                        )
                                    } else {
                                        Box(
                                            modifier = Modifier.size(48.dp).background(mc.copy(alpha = 0.1f), RoundedCornerShape(14.dp)).border(2.5.dp, mc, RoundedCornerShape(14.dp)),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Text(member.initials, fontSize = 18.sp, fontWeight = FontWeight.Black, color = mc, fontFamily = OutfitFamily)
                                        }
                                    }
                                    if (member.isOnline) {
                                        Box(modifier = Modifier.align(Alignment.BottomEnd).size(12.dp).background(t.ok, CircleShape).border(2.dp, t.card, CircleShape))
                                    }
                                }
                                Spacer(Modifier.height(6.dp))
                                Text(member.name, fontSize = 10.sp, fontWeight = FontWeight.Bold, color = t.text, fontFamily = OutfitFamily, maxLines = 1)
                            }
                        }
                    }
                }
            }
        }
    }
}
