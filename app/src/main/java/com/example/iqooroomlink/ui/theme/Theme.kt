package com.example.iqooroomlink.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DarkColorScheme = darkColorScheme(
    primary = ElectricBlue,
    onPrimary = CommandText,
    primaryContainer = ElectricViolet,
    onPrimaryContainer = CommandText,
    secondary = ElectricViolet,
    onSecondary = CommandText,
    tertiary = SignalGreen,
    background = CommandBlack,
    onBackground = CommandText,
    surface = CommandSurface,
    onSurface = CommandText,
    surfaceVariant = CommandSurfaceElevated,
    onSurfaceVariant = CommandMuted,
    outline = Color(0xFF383A4D),
    error = SignalRed,
    onError = CommandText
)

@Composable
fun IQOORoomLinkTheme(
    darkTheme: Boolean = true,
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = DarkColorScheme,
        typography = Typography,
        content = content
    )
}