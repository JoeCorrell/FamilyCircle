package com.haven.app.ui.screens

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Check
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.haven.app.ui.components.HavenCard
import com.haven.app.ui.theme.HavenThemes
import com.haven.app.ui.theme.LocalHavenColors
import com.haven.app.ui.theme.OutfitFamily
import com.haven.app.ui.viewmodel.ThemeViewModel

@Composable
fun ThemesScreen(
    onBack: () -> Unit,
    viewModel: ThemeViewModel = hiltViewModel()
) {
    val t = LocalHavenColors.current
    val currentThemeKey by viewModel.currentThemeKey.collectAsStateWithLifecycle()

    Column(modifier = Modifier.fillMaxSize()) {
        LazyColumn(
            modifier = Modifier
                .weight(1f)
                .padding(horizontal = 18.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
            contentPadding = PaddingValues(bottom = 18.dp)
        ) {
            items(HavenThemes.all, key = { it.key }) { theme ->
                val isSelected = theme.key == currentThemeKey

                HavenCard(
                    modifier = Modifier
                        .fillMaxWidth()
                        .then(
                            if (isSelected) Modifier.border(
                                2.5.dp, theme.accent, RoundedCornerShape(20.dp)
                            ) else Modifier
                        ),
                    onClick = { viewModel.setTheme(theme.key) }
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 18.dp, vertical = 16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        // Theme preview swatch
                        Column(
                            modifier = Modifier
                                .size(48.dp)
                                .clip(RoundedCornerShape(14.dp))
                                .border(1.dp, theme.accent.copy(alpha = 0.2f), RoundedCornerShape(14.dp))
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .weight(1f)
                                    .background(theme.bg)
                            )
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(4.dp)
                                    .background(theme.accent)
                            )
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .weight(0.5f)
                                    .background(theme.surface)
                            )
                        }

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                theme.name, fontSize = 15.sp,
                                fontWeight = FontWeight.Bold, color = t.text,
                                fontFamily = OutfitFamily
                            )
                            Spacer(Modifier.height(6.dp))
                            Row(horizontalArrangement = Arrangement.spacedBy(5.dp)) {
                                listOf(theme.accent, theme.ok, theme.warn, theme.danger).forEach { color ->
                                    Box(
                                        modifier = Modifier
                                            .size(14.dp)
                                            .background(color, RoundedCornerShape(6.dp))
                                    )
                                }
                            }
                        }

                        if (isSelected) {
                            Box(
                                modifier = Modifier
                                    .size(26.dp)
                                    .background(theme.accent, CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    Icons.Outlined.Check, "Selected",
                                    Modifier.size(13.dp), tint = Color.White
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
