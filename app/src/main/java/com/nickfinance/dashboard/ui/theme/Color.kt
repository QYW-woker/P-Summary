package com.nickfinance.dashboard.ui.theme

import androidx.compose.ui.graphics.Color

// ══ Dark Theme ══
val DarkBackground = Color(0xFF0B0F14)
val DarkCardSurface = Color(0xFF131920)
val DarkCardSurfaceSecondary = Color(0xFF1A2230)
val DarkBorder = Color(0xFF252E3B)
val DarkBorderLight = Color(0xFF2D3748)

val DarkTextPrimary = Color(0xFFE2E8F0)
val DarkTextSecondary = Color(0xFF7A8BA2)
val DarkTextTertiary = Color(0xFF4A5B6E)

// ══ Light Theme ══
val LightBackground = Color(0xFFF6F8FA)
val LightCardSurface = Color(0xFFFFFFFF)
val LightBorder = Color(0xFFD0D7DE)
val LightTextPrimary = Color(0xFF1F2937)
val LightTextSecondary = Color(0xFF6B7280)

// ══ Accent Colors ══
val AccentBlue = Color(0xFF3E96FF)
val AccentBlueBg = Color(0x1F3E96FF) // 12% opacity

// ══ Semantic Colors ══
val SemanticGreen = Color(0xFF2ECC71)
val SemanticGreenBg = Color(0x1F2ECC71)
val SemanticRed = Color(0xFFE74C56)
val SemanticRedBg = Color(0x1FE74C56)
val SemanticYellow = Color(0xFFF0B840)
val SemanticYellowBg = Color(0x1FF0B840)
val SemanticPurple = Color(0xFF9B6DFF)
val SemanticCyan = Color(0xFF2DD4A8)
val SemanticOrange = Color(0xFFE8724A)

// ══ Chart Color Sequence ══
val ChartColors = listOf(
    AccentBlue,
    SemanticGreen,
    SemanticPurple,
    SemanticYellow,
    SemanticRed,
    SemanticCyan,
    SemanticOrange
)

// ══ Group Color Presets ══
val GroupColorPresets = listOf(
    SemanticGreen,
    SemanticRed,
    SemanticYellow,
    AccentBlue,
    SemanticPurple,
    SemanticCyan
)

fun parseHexColor(hex: String): Color {
    return try {
        val cleanHex = hex.removePrefix("#")
        Color(android.graphics.Color.parseColor("#$cleanHex"))
    } catch (e: Exception) {
        Color(0xFF9CA3AF)
    }
}
