package com.haven.app.ui.screens

import android.Manifest
import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.location.Geocoder
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ChevronLeft
import androidx.compose.material.icons.outlined.MyLocation
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.hilt.navigation.compose.hiltViewModel
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.*
import com.haven.app.ui.components.HavenCard
import com.haven.app.ui.theme.LocalHavenColors
import com.haven.app.ui.theme.OutfitFamily
import com.haven.app.ui.theme.SpaceMonoFamily
import com.haven.app.ui.viewmodel.AddPlaceViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import java.util.Locale

@SuppressLint("MissingPermission")
@Composable
fun AddPlaceScreen(
    onBack: () -> Unit,
    viewModel: AddPlaceViewModel = hiltViewModel()
) {
    val t = LocalHavenColors.current
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    var name by remember { mutableStateOf("") }
    var address by remember { mutableStateOf("") }
    var radius by remember { mutableFloatStateOf(150f) }
    var selectedLatLng by remember { mutableStateOf<LatLng?>(null) }
    var saving by remember { mutableStateOf(false) }
    var saved by remember { mutableStateOf(false) }

    val hasLocationPermission = ContextCompat.checkSelfPermission(
        context, Manifest.permission.ACCESS_FINE_LOCATION
    ) == PackageManager.PERMISSION_GRANTED

    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(LatLng(40.7580, -73.9855), 15f)
    }

    // Center on current location at launch
    LaunchedEffect(hasLocationPermission) {
        if (hasLocationPermission) {
            try {
                val location = LocationServices.getFusedLocationProviderClient(context).lastLocation.await()
                if (location != null) {
                    val pos = LatLng(location.latitude, location.longitude)
                    cameraPositionState.animate(
                        CameraUpdateFactory.newLatLngZoom(pos, 16f), 600
                    )
                }
            } catch (_: Exception) {}
        }
    }

    // Reverse geocode when user taps a location
    fun reverseGeocode(latLng: LatLng) {
        scope.launch {
            val addr = withContext(Dispatchers.IO) {
                try {
                    val geocoder = Geocoder(context, Locale.getDefault())
                    @Suppress("DEPRECATION")
                    val results = geocoder.getFromLocation(latLng.latitude, latLng.longitude, 1)
                    results?.firstOrNull()?.let { a ->
                        val parts = mutableListOf<String>()
                        if (a.thoroughfare != null) parts.add(a.thoroughfare)
                        if (a.subThoroughfare != null) parts.add(0, a.subThoroughfare)
                        if (a.locality != null) parts.add(a.locality)
                        if (a.adminArea != null) parts.add(a.adminArea)
                        parts.joinToString(", ").ifEmpty { "${latLng.latitude}, ${latLng.longitude}" }
                    } ?: "${latLng.latitude}, ${latLng.longitude}"
                } catch (_: Exception) { "${latLng.latitude}, ${latLng.longitude}" }
            }
            address = addr
        }
    }

    // Forward geocode when user types address and presses done
    fun forwardGeocode(addr: String) {
        if (addr.isBlank()) return
        scope.launch {
            val result = withContext(Dispatchers.IO) {
                try {
                    val geocoder = Geocoder(context, Locale.getDefault())
                    @Suppress("DEPRECATION")
                    val results = geocoder.getFromLocationName(addr, 1)
                    results?.firstOrNull()?.let { LatLng(it.latitude, it.longitude) }
                } catch (_: Exception) { null }
            }
            if (result != null) {
                selectedLatLng = result
                cameraPositionState.animate(
                    CameraUpdateFactory.newLatLngZoom(result, 16f), 600
                )
            }
        }
    }

    Column(modifier = Modifier.fillMaxSize()) {
        // Header
        Text(
            "New Place", fontSize = 22.sp,
            fontWeight = FontWeight.ExtraBold, color = t.text,
            fontFamily = OutfitFamily, letterSpacing = (-0.5).sp,
            modifier = Modifier.padding(horizontal = 20.dp, vertical = 12.dp)
        )

        // Map for selecting location
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(280.dp)
                .padding(horizontal = 14.dp)
                .clip(RoundedCornerShape(20.dp))
                .border(1.dp, t.border, RoundedCornerShape(20.dp))
        ) {
            GoogleMap(
                modifier = Modifier.fillMaxSize(),
                cameraPositionState = cameraPositionState,
                properties = MapProperties(
                    isMyLocationEnabled = hasLocationPermission,
                    mapType = MapType.NORMAL
                ),
                uiSettings = MapUiSettings(
                    zoomControlsEnabled = false,
                    myLocationButtonEnabled = false,
                    compassEnabled = false
                ),
                onMapClick = { latLng ->
                    selectedLatLng = latLng
                    reverseGeocode(latLng)
                }
            ) {
                // Show selected location with radius
                selectedLatLng?.let { pos ->
                    Circle(
                        center = pos,
                        radius = radius.toDouble(),
                        fillColor = Color(0xFF38BDF8).copy(alpha = 0.15f),
                        strokeColor = Color(0xFF38BDF8).copy(alpha = 0.5f),
                        strokeWidth = 3f
                    )
                    MarkerComposable(
                        state = MarkerState(position = pos),
                        title = name.ifEmpty { "Selected location" }
                    ) {
                        Box(
                            modifier = Modifier
                                .size(20.dp)
                                .background(t.accent, RoundedCornerShape(10.dp))
                                .border(3.dp, Color.White, RoundedCornerShape(10.dp))
                        )
                    }
                }
            }

            // Tap hint
            if (selectedLatLng == null) {
                Box(
                    modifier = Modifier
                        .align(Alignment.Center)
                        .background(t.card.copy(alpha = 0.9f), RoundedCornerShape(12.dp))
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                ) {
                    Text(
                        "Tap the map to select a location",
                        fontSize = 12.sp, color = t.textMid, fontFamily = OutfitFamily
                    )
                }
            }

            // My location button
            if (hasLocationPermission) {
                Box(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(8.dp)
                        .size(36.dp)
                        .background(t.card, RoundedCornerShape(10.dp))
                        .border(1.dp, t.border, RoundedCornerShape(10.dp))
                        .clickable {
                            scope.launch {
                                try {
                                    val loc = LocationServices.getFusedLocationProviderClient(context).lastLocation.await()
                                    if (loc != null) {
                                        val pos = LatLng(loc.latitude, loc.longitude)
                                        selectedLatLng = pos
                                        reverseGeocode(pos)
                                        cameraPositionState.animate(
                                            CameraUpdateFactory.newLatLngZoom(pos, 16f), 600
                                        )
                                    }
                                } catch (_: Exception) {}
                            }
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Outlined.MyLocation, "My location", Modifier.size(18.dp), tint = t.accent)
                }
            }
        }

        // Form fields
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 18.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Name
            Column {
                Text("NAME", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = t.textFade, fontFamily = SpaceMonoFamily, letterSpacing = 1.5.sp)
                Spacer(Modifier.height(6.dp))
                OutlinedTextField(
                    value = name, onValueChange = { name = it },
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = { Text("e.g. Home, School, Work", color = t.textFade) },
                    shape = RoundedCornerShape(14.dp), singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = t.accent, unfocusedBorderColor = t.border,
                        focusedContainerColor = if (t.isDark) t.surfaceAlt else t.bgSub,
                        unfocusedContainerColor = if (t.isDark) t.surfaceAlt else t.bgSub,
                        cursorColor = t.accent, focusedTextColor = t.text, unfocusedTextColor = t.text,
                    ),
                    textStyle = LocalTextStyle.current.copy(fontFamily = OutfitFamily, fontSize = 14.sp)
                )
            }

            // Address (with geocode on done)
            Column {
                Text("ADDRESS", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = t.textFade, fontFamily = SpaceMonoFamily, letterSpacing = 1.5.sp)
                Spacer(Modifier.height(6.dp))
                OutlinedTextField(
                    value = address, onValueChange = { address = it },
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = { Text("Type address or tap map", color = t.textFade) },
                    shape = RoundedCornerShape(14.dp), singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = t.accent, unfocusedBorderColor = t.border,
                        focusedContainerColor = if (t.isDark) t.surfaceAlt else t.bgSub,
                        unfocusedContainerColor = if (t.isDark) t.surfaceAlt else t.bgSub,
                        cursorColor = t.accent, focusedTextColor = t.text, unfocusedTextColor = t.text,
                    ),
                    textStyle = LocalTextStyle.current.copy(fontFamily = OutfitFamily, fontSize = 14.sp),
                    keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(
                        imeAction = androidx.compose.ui.text.input.ImeAction.Search
                    ),
                    keyboardActions = androidx.compose.foundation.text.KeyboardActions(
                        onSearch = { forwardGeocode(address) }
                    )
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    "Type an address and press search, or tap on the map above",
                    fontSize = 10.sp, color = t.textFade, fontFamily = OutfitFamily
                )
            }

            // Radius slider
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("RADIUS", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = t.textFade, fontFamily = SpaceMonoFamily, letterSpacing = 1.5.sp)
                    Text("${radius.toInt()}m", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = t.accent, fontFamily = SpaceMonoFamily)
                }
                Spacer(Modifier.height(6.dp))
                Slider(
                    value = radius, onValueChange = { radius = it },
                    valueRange = 50f..500f,
                    colors = SliderDefaults.colors(thumbColor = t.accent, activeTrackColor = t.accent, inactiveTrackColor = t.border)
                )
            }

            // Save button
            val canSave = name.isNotBlank() && selectedLatLng != null && !saving
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .then(
                        if (saved) Modifier
                            .background(t.ok.copy(alpha = 0.07f))
                            .border(1.5.dp, t.ok.copy(alpha = 0.2f), RoundedCornerShape(16.dp))
                        else if (canSave) Modifier.background(Brush.linearGradient(listOf(t.accent, t.accentMid)))
                        else Modifier.background(if (t.isDark) t.surfaceAlt else t.bgSub)
                    )
                    .clickable(enabled = canSave) {
                        saving = true
                        val latLng = selectedLatLng!!
                        viewModel.savePlaceWithCoords(
                            name = name,
                            address = address,
                            lat = latLng.latitude,
                            lng = latLng.longitude,
                            radiusMeters = radius,
                            onComplete = {
                                saving = false
                                saved = true
                            }
                        )
                    }
                    .padding(15.dp),
                contentAlignment = Alignment.Center
            ) {
                if (saving) {
                    CircularProgressIndicator(modifier = Modifier.size(20.dp), color = Color.White, strokeWidth = 2.dp)
                } else {
                    Text(
                        if (saved) "Place Saved" else "Save Place",
                        fontSize = 14.sp, fontWeight = FontWeight.Bold,
                        color = if (saved) t.ok else if (canSave) Color.White else t.textFade,
                        fontFamily = OutfitFamily
                    )
                }
            }
        }
    }
}
