package com.farukzahra.muslimplus.ui.theme

import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.ui.graphics.Color

val Emerald = Color(0xFF2FB98A)
val EmeraldDeep = Color(0xFF14503F)
val Gold = Color(0xFFD9B25F)
val Midnight = Color(0xFF08131C)
val MidnightSoft = Color(0xFF0E1B26)

val DarkScheme = darkColorScheme(
    primary = Emerald,
    onPrimary = Color(0xFF04231A),
    primaryContainer = EmeraldDeep,
    onPrimaryContainer = Color(0xFFC9F5E4),
    secondary = Gold,
    onSecondary = Color(0xFF2A1F05),
    secondaryContainer = Color(0xFF3E2F0C),
    onSecondaryContainer = Color(0xFFF6E2B0),
    tertiary = Color(0xFF7FB7D9),
    onTertiary = Color(0xFF04202F),
    background = Midnight,
    onBackground = Color(0xFFE9EEF4),
    surface = MidnightSoft,
    onSurface = Color(0xFFE9EEF4),
    surfaceVariant = Color(0xFF24384A),
    onSurfaceVariant = Color(0xFF9FB0C2),
    surfaceContainerLowest = Color(0xFF050D14),
    surfaceContainerLow = Color(0xFF0B1822),
    surfaceContainer = Color(0xFF13232F),
    surfaceContainerHigh = Color(0xFF1A2C3A),
    surfaceContainerHighest = Color(0xFF223646),
    outline = Color(0xFF3A4C5E),
    outlineVariant = Color(0xFF223444),
    error = Color(0xFFF2B8B5),
    onError = Color(0xFF601410),
)

val LightScheme = lightColorScheme(
    primary = Color(0xFF1D7A63),
    onPrimary = Color(0xFFFFFFFF),
    primaryContainer = Color(0xFFC9F5E4),
    onPrimaryContainer = Color(0xFF05372B),
    secondary = Color(0xFF8A6A1F),
    onSecondary = Color(0xFFFFFFFF),
    secondaryContainer = Color(0xFFF6E2B0),
    onSecondaryContainer = Color(0xFF2A1F05),
    tertiary = Color(0xFF2F6480),
    onTertiary = Color(0xFFFFFFFF),
    background = Color(0xFFF7F3EA),
    onBackground = Color(0xFF14202C),
    surface = Color(0xFFFFFDF7),
    onSurface = Color(0xFF14202C),
    surfaceVariant = Color(0xFFE7E0D2),
    onSurfaceVariant = Color(0xFF5D6B7A),
    surfaceContainerLowest = Color(0xFFFFFFFF),
    surfaceContainerLow = Color(0xFFFBF7EE),
    surfaceContainer = Color(0xFFF1EBDD),
    surfaceContainerHigh = Color(0xFFEBE5D7),
    surfaceContainerHighest = Color(0xFFE5DFD1),
    outline = Color(0xFF8A9AA8),
    outlineVariant = Color(0xFFD6D0C4),
    error = Color(0xFFB3261E),
    onError = Color(0xFFFFFFFF),
)
