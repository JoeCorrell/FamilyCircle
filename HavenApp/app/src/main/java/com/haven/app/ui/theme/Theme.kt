package com.haven.app.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

val LocalHavenColors = staticCompositionLocalOf { HavenThemes.Sand }

@Composable
fun HavenAppTheme(
    havenColors: HavenColors = HavenThemes.Sand,
    content: @Composable () -> Unit
) {
    val colorScheme = if (havenColors.isDark) {
        darkColorScheme(
            primary = havenColors.accent,
            onPrimary = havenColors.text,
            surface = havenColors.surface,
            onSurface = havenColors.text,
            background = havenColors.bg,
            onBackground = havenColors.text,
            surfaceVariant = havenColors.surfaceAlt,
            outline = havenColors.border,
        )
    } else {
        lightColorScheme(
            primary = havenColors.accent,
            onPrimary = havenColors.text,
            surface = havenColors.surface,
            onSurface = havenColors.text,
            background = havenColors.bg,
            onBackground = havenColors.text,
            surfaceVariant = havenColors.surfaceAlt,
            outline = havenColors.border,
        )
    }

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            val insetsController = WindowCompat.getInsetsController(window, view)
            insetsController.isAppearanceLightStatusBars = !havenColors.isDark
            insetsController.isAppearanceLightNavigationBars = !havenColors.isDark
        }
    }

    CompositionLocalProvider(LocalHavenColors provides havenColors) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = HavenTypography,
            content = content
        )
    }
}
