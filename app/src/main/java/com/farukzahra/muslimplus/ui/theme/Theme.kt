package com.farukzahra.muslimplus.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

private val DarkScheme = darkColorScheme(
    primary = Emerald,
    onPrimary = Midnight,
    secondary = Gold,
    background = Midnight,
    onBackground = Sand,
    surface = MidnightSoft,
    onSurface = Sand,
)

private val LightScheme = lightColorScheme(
    primary = EmeraldDark,
    onPrimary = SandSoft,
    secondary = Gold,
    background = Sand,
    onBackground = Ink,
    surface = SandSoft,
    onSurface = Ink,
)

@Composable
fun MuslimPlusTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit,
) {
    MaterialTheme(
        colorScheme = if (darkTheme) DarkScheme else LightScheme,
        content = content,
    )
}
