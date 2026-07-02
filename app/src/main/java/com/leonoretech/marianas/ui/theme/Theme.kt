package com.leonoretech.marianas.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable

private val MarianasDarkColors = darkColorScheme(
    primary       = Purple,
    secondary     = Cyan,
    tertiary      = Blue,
    background    = BgDark,
    surface       = BgPanel,
    onPrimary     = BgDark,
    onBackground  = TextPrimary,
    onSurface     = TextPrimary,
    error         = DangerRed
)

@Composable
fun MarianasTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = MarianasDarkColors,
        typography  = MarianasTypography,
        content     = content
    )
}
