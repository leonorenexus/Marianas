package com.leonoretech.marianas.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

/**
 * TODO (custom fonts): The web app uses "JetBrains Mono" for body text and
 * "Orbitron" for headings/brand text. To use the real fonts here:
 *   1. Download the .ttf files and place them in app/src/main/res/font/
 *      (e.g. jetbrains_mono_regular.ttf, orbitron_bold.ttf)
 *   2. Replace FontFamily.Monospace below with:
 *      FontFamily(Font(R.font.jetbrains_mono_regular, FontWeight.Normal), ...)
 * Until then, this uses Android's built-in monospace font as a close stand-in.
 */
val MonoFontFamily = FontFamily.Monospace

val MarianasTypography = Typography(
    headlineLarge = TextStyle(
        fontFamily = MonoFontFamily,
        fontWeight = FontWeight.Black,
        fontSize = 28.sp,
        letterSpacing = 2.sp
    ),
    titleLarge = TextStyle(
        fontFamily = MonoFontFamily,
        fontWeight = FontWeight.Bold,
        fontSize = 18.sp,
        letterSpacing = 1.sp
    ),
    titleMedium = TextStyle(
        fontFamily = MonoFontFamily,
        fontWeight = FontWeight.Bold,
        fontSize = 14.sp,
        letterSpacing = 1.sp
    ),
    bodyLarge = TextStyle(
        fontFamily = MonoFontFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 15.sp,
        lineHeight = 22.sp
    ),
    bodyMedium = TextStyle(
        fontFamily = MonoFontFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 13.sp,
        lineHeight = 19.sp
    ),
    labelSmall = TextStyle(
        fontFamily = MonoFontFamily,
        fontWeight = FontWeight.Medium,
        fontSize = 11.sp,
        letterSpacing = 1.sp
    )
)
