package com.nickfinance.dashboard.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

data class ExtendedColors(
    val cardSurface: Color,
    val cardSurfaceSecondary: Color,
    val border: Color,
    val borderLight: Color,
    val textPrimary: Color,
    val textSecondary: Color,
    val textTertiary: Color,
    val accentBlue: Color,
    val accentBlueBg: Color,
    val semanticGreen: Color,
    val semanticGreenBg: Color,
    val semanticRed: Color,
    val semanticRedBg: Color,
    val semanticYellow: Color,
    val semanticYellowBg: Color,
    val semanticPurple: Color,
    val semanticCyan: Color,
    val semanticOrange: Color
)

val DarkExtendedColors = ExtendedColors(
    cardSurface = DarkCardSurface,
    cardSurfaceSecondary = DarkCardSurfaceSecondary,
    border = DarkBorder,
    borderLight = DarkBorderLight,
    textPrimary = DarkTextPrimary,
    textSecondary = DarkTextSecondary,
    textTertiary = DarkTextTertiary,
    accentBlue = AccentBlue,
    accentBlueBg = AccentBlueBg,
    semanticGreen = SemanticGreen,
    semanticGreenBg = SemanticGreenBg,
    semanticRed = SemanticRed,
    semanticRedBg = SemanticRedBg,
    semanticYellow = SemanticYellow,
    semanticYellowBg = SemanticYellowBg,
    semanticPurple = SemanticPurple,
    semanticCyan = SemanticCyan,
    semanticOrange = SemanticOrange
)

val LightExtendedColors = ExtendedColors(
    cardSurface = LightCardSurface,
    cardSurfaceSecondary = Color(0xFFF0F2F5),
    border = LightBorder,
    borderLight = Color(0xFFE5E7EB),
    textPrimary = LightTextPrimary,
    textSecondary = LightTextSecondary,
    textTertiary = Color(0xFF9CA3AF),
    accentBlue = AccentBlue,
    accentBlueBg = AccentBlueBg,
    semanticGreen = SemanticGreen,
    semanticGreenBg = SemanticGreenBg,
    semanticRed = SemanticRed,
    semanticRedBg = SemanticRedBg,
    semanticYellow = SemanticYellow,
    semanticYellowBg = SemanticYellowBg,
    semanticPurple = SemanticPurple,
    semanticCyan = SemanticCyan,
    semanticOrange = SemanticOrange
)

val LocalExtendedColors = staticCompositionLocalOf { DarkExtendedColors }

private val DarkColorScheme = darkColorScheme(
    primary = AccentBlue,
    onPrimary = Color.White,
    background = DarkBackground,
    onBackground = DarkTextPrimary,
    surface = DarkCardSurface,
    onSurface = DarkTextPrimary,
    surfaceVariant = DarkCardSurfaceSecondary,
    onSurfaceVariant = DarkTextSecondary,
    outline = DarkBorder,
    outlineVariant = DarkBorderLight,
    error = SemanticRed,
    onError = Color.White
)

private val LightColorScheme = lightColorScheme(
    primary = AccentBlue,
    onPrimary = Color.White,
    background = LightBackground,
    onBackground = LightTextPrimary,
    surface = LightCardSurface,
    onSurface = LightTextPrimary,
    surfaceVariant = Color(0xFFF0F2F5),
    onSurfaceVariant = LightTextSecondary,
    outline = LightBorder,
    outlineVariant = Color(0xFFE5E7EB),
    error = SemanticRed,
    onError = Color.White
)

enum class ThemeMode {
    DARK, LIGHT, SYSTEM
}

@Composable
fun FinanceDashboardTheme(
    themeMode: ThemeMode = ThemeMode.DARK,
    content: @Composable () -> Unit
) {
    val darkTheme = when (themeMode) {
        ThemeMode.DARK -> true
        ThemeMode.LIGHT -> false
        ThemeMode.SYSTEM -> isSystemInDarkTheme()
    }

    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme
    val extendedColors = if (darkTheme) DarkExtendedColors else LightExtendedColors

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = Color.Transparent.toArgb()
            window.navigationBarColor = Color.Transparent.toArgb()
            WindowCompat.getInsetsController(window, view).apply {
                isAppearanceLightStatusBars = !darkTheme
                isAppearanceLightNavigationBars = !darkTheme
            }
        }
    }

    CompositionLocalProvider(LocalExtendedColors provides extendedColors) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = AppTypography,
            content = content
        )
    }
}

object AppTheme {
    val colors: ExtendedColors
        @Composable
        get() = LocalExtendedColors.current
}
