package com.haven.app.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.haven.app.R

val OutfitFamily = FontFamily(
    Font(R.font.plusjakarta_regular, FontWeight.Normal),
    Font(R.font.plusjakarta_medium, FontWeight.Medium),
    Font(R.font.plusjakarta_semibold, FontWeight.SemiBold),
    Font(R.font.plusjakarta_bold, FontWeight.Bold),
    Font(R.font.plusjakarta_extrabold, FontWeight.ExtraBold),
    Font(R.font.plusjakarta_extrabold, FontWeight.Black),
)

val SpaceMonoFamily = FontFamily(
    Font(R.font.jetbrainsmono_regular, FontWeight.Normal),
    Font(R.font.jetbrainsmono_bold, FontWeight.Bold),
)

val HavenTypography = Typography(
    displayLarge = TextStyle(fontFamily = OutfitFamily, fontWeight = FontWeight.Black, fontSize = 56.sp),
    displayMedium = TextStyle(fontFamily = OutfitFamily, fontWeight = FontWeight.ExtraBold, fontSize = 40.sp),
    displaySmall = TextStyle(fontFamily = OutfitFamily, fontWeight = FontWeight.Bold, fontSize = 30.sp),
    headlineLarge = TextStyle(fontFamily = OutfitFamily, fontWeight = FontWeight.ExtraBold, fontSize = 22.sp, letterSpacing = (-0.5).sp),
    headlineMedium = TextStyle(fontFamily = OutfitFamily, fontWeight = FontWeight.Bold, fontSize = 18.sp),
    headlineSmall = TextStyle(fontFamily = OutfitFamily, fontWeight = FontWeight.Bold, fontSize = 15.sp),
    titleLarge = TextStyle(fontFamily = OutfitFamily, fontWeight = FontWeight.Bold, fontSize = 17.sp),
    titleMedium = TextStyle(fontFamily = OutfitFamily, fontWeight = FontWeight.SemiBold, fontSize = 14.sp),
    titleSmall = TextStyle(fontFamily = OutfitFamily, fontWeight = FontWeight.SemiBold, fontSize = 13.sp),
    bodyLarge = TextStyle(fontFamily = OutfitFamily, fontWeight = FontWeight.Normal, fontSize = 14.sp),
    bodyMedium = TextStyle(fontFamily = OutfitFamily, fontWeight = FontWeight.Normal, fontSize = 13.sp),
    bodySmall = TextStyle(fontFamily = OutfitFamily, fontWeight = FontWeight.Normal, fontSize = 12.sp),
    labelLarge = TextStyle(fontFamily = SpaceMonoFamily, fontWeight = FontWeight.Bold, fontSize = 10.sp, letterSpacing = 1.5.sp),
    labelMedium = TextStyle(fontFamily = SpaceMonoFamily, fontWeight = FontWeight.Bold, fontSize = 9.sp, letterSpacing = 1.sp),
    labelSmall = TextStyle(fontFamily = SpaceMonoFamily, fontWeight = FontWeight.Normal, fontSize = 8.sp, letterSpacing = 0.8.sp),
)
