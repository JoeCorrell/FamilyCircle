package com.haven.app.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

// To use custom Outfit + Space Mono fonts:
// 1. Download from Google Fonts (https://fonts.google.com)
// 2. Place .ttf files in app/src/main/res/font/
// 3. Uncomment the Font() lines below and import com.haven.app.R + Font
//
// val OutfitFamily = FontFamily(
//     Font(R.font.outfit_regular, FontWeight.Normal),
//     Font(R.font.outfit_medium, FontWeight.Medium),
//     Font(R.font.outfit_semibold, FontWeight.SemiBold),
//     Font(R.font.outfit_bold, FontWeight.Bold),
//     Font(R.font.outfit_extrabold, FontWeight.ExtraBold),
//     Font(R.font.outfit_black, FontWeight.Black),
// )
//
// val SpaceMonoFamily = FontFamily(
//     Font(R.font.spacemono_regular, FontWeight.Normal),
//     Font(R.font.spacemono_bold, FontWeight.Bold),
// )

val OutfitFamily = FontFamily.SansSerif
val SpaceMonoFamily = FontFamily.Monospace

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
