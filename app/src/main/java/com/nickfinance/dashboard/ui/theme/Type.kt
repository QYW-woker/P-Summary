package com.nickfinance.dashboard.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

val RobotoMono = FontFamily(
    Font(android.R.font.roboto, FontWeight.Normal)
)

val MonoFontFamily = FontFamily.Monospace

val AppTypography = Typography(
    // Title large - 20sp bold
    titleLarge = TextStyle(
        fontWeight = FontWeight.Bold,
        fontSize = 20.sp,
        lineHeight = 28.sp
    ),
    // Title medium - 16sp bold
    titleMedium = TextStyle(
        fontWeight = FontWeight.Bold,
        fontSize = 16.sp,
        lineHeight = 24.sp
    ),
    // Title small - 14sp bold
    titleSmall = TextStyle(
        fontWeight = FontWeight.Bold,
        fontSize = 14.sp,
        lineHeight = 20.sp
    ),
    // Body large - 16sp
    bodyLarge = TextStyle(
        fontSize = 16.sp,
        lineHeight = 24.sp
    ),
    // Body medium - 14sp (default body)
    bodyMedium = TextStyle(
        fontSize = 14.sp,
        lineHeight = 20.sp
    ),
    // Body small - 12sp
    bodySmall = TextStyle(
        fontSize = 12.sp,
        lineHeight = 16.sp
    ),
    // Label large - 14sp
    labelLarge = TextStyle(
        fontWeight = FontWeight.Medium,
        fontSize = 14.sp,
        lineHeight = 20.sp
    ),
    // Label medium - 12sp
    labelMedium = TextStyle(
        fontSize = 12.sp,
        lineHeight = 16.sp
    ),
    // Label small - 10sp
    labelSmall = TextStyle(
        fontSize = 10.sp,
        lineHeight = 14.sp
    )
)
