package com.haven.app.ui.theme

import androidx.compose.ui.graphics.Color

data class HavenColors(
    val name: String,
    val key: String,
    val bg: Color,
    val bgSub: Color,
    val surface: Color,
    val surfaceAlt: Color,
    val card: Color,
    val cardAlt: Color,
    val accent: Color,
    val accentSoft: Color,
    val accentMid: Color,
    val accentBg: Color,
    val text: Color,
    val textMid: Color,
    val textFade: Color,
    val ok: Color,
    val warn: Color,
    val danger: Color,
    val border: Color,
    val borderStrong: Color,
    val isDark: Boolean
)

object HavenThemes {
    val Sand = HavenColors(
        name = "Sand", key = "sand",
        bg = Color(0xFFF5F0E8), bgSub = Color(0xFFECE5D8),
        surface = Color(0xFFFFFFFF), surfaceAlt = Color(0xFFFAF7F2),
        card = Color(0xFFFFFFFF), cardAlt = Color(0xFFF9F5ED),
        accent = Color(0xFFC2410C), accentSoft = Color(0xFFFFF7ED),
        accentMid = Color(0xFFFB923C), accentBg = Color(0x12C2410C),
        text = Color(0xFF1C1917), textMid = Color(0xFF57534E), textFade = Color(0xFFA8A29E),
        ok = Color(0xFF15803D), warn = Color(0xFFA16207), danger = Color(0xFFB91C1C),
        border = Color(0x0F000000), borderStrong = Color(0x1A000000),
        isDark = false
    )

    val Charcoal = HavenColors(
        name = "Charcoal", key = "charcoal",
        bg = Color(0xFF171717), bgSub = Color(0xFF1C1C1C),
        surface = Color(0xFF222222), surfaceAlt = Color(0xFF262626),
        card = Color(0xFF222222), cardAlt = Color(0xFF2A2A2A),
        accent = Color(0xFFFB923C), accentSoft = Color(0x1AFB923C),
        accentMid = Color(0xFFFDBA74), accentBg = Color(0x0FFB923C),
        text = Color(0xFFFAFAF9), textMid = Color(0xFFA8A29E), textFade = Color(0xFF57534E),
        ok = Color(0xFF4ADE80), warn = Color(0xFFFBBF24), danger = Color(0xFFF87171),
        border = Color(0x0FFFFFFF), borderStrong = Color(0x1AFFFFFF),
        isDark = true
    )

    val Ocean = HavenColors(
        name = "Ocean", key = "ocean",
        bg = Color(0xFF0C1A2E), bgSub = Color(0xFF0F2035),
        surface = Color(0xFF142840), surfaceAlt = Color(0xFF183050),
        card = Color(0xFF142840), cardAlt = Color(0xFF183050),
        accent = Color(0xFF38BDF8), accentSoft = Color(0x1438BDF8),
        accentMid = Color(0xFF7DD3FC), accentBg = Color(0x0D38BDF8),
        text = Color(0xFFF0F9FF), textMid = Color(0xFF7DD3FC), textFade = Color(0xFF365880),
        ok = Color(0xFF4ADE80), warn = Color(0xFFFDE68A), danger = Color(0xFFFCA5A5),
        border = Color(0x1438BDF8), borderStrong = Color(0x2638BDF8),
        isDark = true
    )

    val Flora = HavenColors(
        name = "Flora", key = "flora",
        bg = Color(0xFFF0F7F1), bgSub = Color(0xFFE4EFE6),
        surface = Color(0xFFFFFFFF), surfaceAlt = Color(0xFFF5FAF5),
        card = Color(0xFFFFFFFF), cardAlt = Color(0xFFF5FAF6),
        accent = Color(0xFF16A34A), accentSoft = Color(0xFFF0FDF4),
        accentMid = Color(0xFF4ADE80), accentBg = Color(0x0F16A34A),
        text = Color(0xFF14532D), textMid = Color(0xFF3F6B50), textFade = Color(0xFF86B898),
        ok = Color(0xFF16A34A), warn = Color(0xFFCA8A04), danger = Color(0xFFDC2626),
        border = Color(0x0D000000), borderStrong = Color(0x17000000),
        isDark = false
    )

    val Dusk = HavenColors(
        name = "Dusk", key = "dusk",
        bg = Color(0xFF1E1028), bgSub = Color(0xFF241335),
        surface = Color(0xFF2C1A40), surfaceAlt = Color(0xFF341F4D),
        card = Color(0xFF2C1A40), cardAlt = Color(0xFF341F4D),
        accent = Color(0xFFC084FC), accentSoft = Color(0x14C084FC),
        accentMid = Color(0xFFD8B4FE), accentBg = Color(0x0DC084FC),
        text = Color(0xFFFAF5FF), textMid = Color(0xFFD8B4FE), textFade = Color(0xFF5C3D80),
        ok = Color(0xFF4ADE80), warn = Color(0xFFFDE68A), danger = Color(0xFFFCA5A5),
        border = Color(0x12C084FC), borderStrong = Color(0x24C084FC),
        isDark = true
    )

    val all = listOf(Sand, Charcoal, Ocean, Flora, Dusk)

    fun fromKey(key: String): HavenColors = when (key) {
        "charcoal" -> Charcoal
        "ocean" -> Ocean
        "flora" -> Flora
        "dusk" -> Dusk
        else -> Sand
    }
}
