package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val AstroStrikeColorScheme = darkColorScheme(
    primary = NeonCyan,
    onPrimary = Color.Black,
    primaryContainer = NeonCyanDim,
    onPrimaryContainer = Color.Black,
    secondary = NeonMagenta,
    onSecondary = Color.White,
    secondaryContainer = NeonPurple,
    onSecondaryContainer = Color.White,
    tertiary = SolarGold,
    onTertiary = Color.Black,
    background = SpaceBlack,
    onBackground = TextWhite,
    surface = SpaceDark,
    onSurface = TextWhite,
    surfaceVariant = SpaceCard,
    onSurfaceVariant = TextMuted,
    outline = SpaceCardBorder,
    error = PlasmaOrange,
    onError = Color.White
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = true,
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = AstroStrikeColorScheme,
        typography = Typography,
        content = content
    )
}
