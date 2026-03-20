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

    val Slate = HavenColors(
        name = "Slate", key = "slate",
        bg = Color(0xFFF1F5F9), bgSub = Color(0xFFE2E8F0),
        surface = Color(0xFFFFFFFF), surfaceAlt = Color(0xFFF8FAFC),
        card = Color(0xFFFFFFFF), cardAlt = Color(0xFFF1F5F9),
        accent = Color(0xFF4F46E5), accentSoft = Color(0xFFEEF2FF),
        accentMid = Color(0xFF818CF8), accentBg = Color(0x104F46E5),
        text = Color(0xFF1E293B), textMid = Color(0xFF475569), textFade = Color(0xFF94A3B8),
        ok = Color(0xFF16A34A), warn = Color(0xFFD97706), danger = Color(0xFFDC2626),
        border = Color(0x0F000000), borderStrong = Color(0x1A000000),
        isDark = false
    )

    val Midnight = HavenColors(
        name = "Midnight", key = "midnight",
        bg = Color(0xFF08111E), bgSub = Color(0xFF0D1928),
        surface = Color(0xFF121F30), surfaceAlt = Color(0xFF172538),
        card = Color(0xFF121F30), cardAlt = Color(0xFF172538),
        accent = Color(0xFF22D3EE), accentSoft = Color(0x0D22D3EE),
        accentMid = Color(0xFF67E8F9), accentBg = Color(0x0A22D3EE),
        text = Color(0xFFF0FEFF), textMid = Color(0xFF67E8F9), textFade = Color(0xFF1F4A5A),
        ok = Color(0xFF4ADE80), warn = Color(0xFFFDE68A), danger = Color(0xFFFCA5A5),
        border = Color(0x1222D3EE), borderStrong = Color(0x2022D3EE),
        isDark = true
    )

    val Rose = HavenColors(
        name = "Rose", key = "rose",
        bg = Color(0xFFFFF5F7), bgSub = Color(0xFFFFE8ED),
        surface = Color(0xFFFFFFFF), surfaceAlt = Color(0xFFFFF0F3),
        card = Color(0xFFFFFFFF), cardAlt = Color(0xFFFFF5F7),
        accent = Color(0xFFE11D48), accentSoft = Color(0xFFFFF1F2),
        accentMid = Color(0xFFFB7185), accentBg = Color(0x0DE11D48),
        text = Color(0xFF4C0519), textMid = Color(0xFF9F1239), textFade = Color(0xFFFDA4AF),
        ok = Color(0xFF16A34A), warn = Color(0xFFD97706), danger = Color(0xFFB91C1C),
        border = Color(0x0D000000), borderStrong = Color(0x17000000),
        isDark = false
    )

    val Ember = HavenColors(
        name = "Ember", key = "ember",
        bg = Color(0xFF1A0A05), bgSub = Color(0xFF230E07),
        surface = Color(0xFF2C120A), surfaceAlt = Color(0xFF35160C),
        card = Color(0xFF2C120A), cardAlt = Color(0xFF35160C),
        accent = Color(0xFFEF4444), accentSoft = Color(0x0DEF4444),
        accentMid = Color(0xFFFCA5A5), accentBg = Color(0x0AEF4444),
        text = Color(0xFFFFF5F5), textMid = Color(0xFFFCA5A5), textFade = Color(0xFF7F2020),
        ok = Color(0xFF4ADE80), warn = Color(0xFFFBBF24), danger = Color(0xFFF87171),
        border = Color(0x12EF4444), borderStrong = Color(0x22EF4444),
        isDark = true
    )

    val Arctic = HavenColors(
        name = "Arctic", key = "arctic",
        bg = Color(0xFFF0FAFA), bgSub = Color(0xFFDFF4F4),
        surface = Color(0xFFFFFFFF), surfaceAlt = Color(0xFFF5FBFB),
        card = Color(0xFFFFFFFF), cardAlt = Color(0xFFF0FAFA),
        accent = Color(0xFF0D9488), accentSoft = Color(0xFFF0FDFA),
        accentMid = Color(0xFF2DD4BF), accentBg = Color(0x0D0D9488),
        text = Color(0xFF134E4A), textMid = Color(0xFF2D7A74), textFade = Color(0xFF7BC8C4),
        ok = Color(0xFF059669), warn = Color(0xFFD97706), danger = Color(0xFFDC2626),
        border = Color(0x0D000000), borderStrong = Color(0x17000000),
        isDark = false
    )

    val all = listOf(Sand, Charcoal, Ocean, Flora, Dusk, Slate, Midnight, Rose, Ember, Arctic)

    fun fromKey(key: String): HavenColors = when (key) {
        "charcoal" -> Charcoal
        "ocean" -> Ocean
        "flora" -> Flora
        "dusk" -> Dusk
        "slate" -> Slate
        "midnight" -> Midnight
        "rose" -> Rose
        "ember" -> Ember
        "arctic" -> Arctic
        else -> Sand
    }
}
