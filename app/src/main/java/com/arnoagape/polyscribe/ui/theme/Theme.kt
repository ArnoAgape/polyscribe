package com.arnoagape.polyscribe.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

private val DarkColorScheme = darkColorScheme(
    primary = LightBlue,
    onPrimary = Color.White,
    surfaceContainerLow = Color(0xFF020913),
    primaryContainer = Blue,
    secondaryContainer = LightBlue,
    onSecondaryContainer = LightYellow,
    onPrimaryContainer = Color.White,
    surfaceContainer = Color(0xFF051429),
    secondary = LightYellow,
    tertiary = White80,
    error = Color(0xFFB3261E),
    outlineVariant = Blue,
    surfaceVariant = Color(0xFF49454F),
    onSurface = Color(0xFFE6E1E5),
    onSurfaceVariant = Color(0xFFCAC4D0),
    surface = Color(0xFF051429),
    background = Color(0xFF051429)
)

private val LightColorScheme = lightColorScheme(
    primary = Blue,
    onPrimary = Color.White,
    surfaceContainerLow = Color(0xFF91BFFF),
    primaryContainer = Blue,
    secondaryContainer = Blue,
    onSecondaryContainer = Yellow,
    onPrimaryContainer = Color.White,
    surfaceContainer = VeryLightBlue,
    secondary = Yellow,
    tertiary = White40,
    error = Color(0xFFB3261E),
    outlineVariant = Blue,
    surfaceVariant = Color(0xFFE7E0EC),
    onSurface = Color(0xFF1C1B1F),
    onSurfaceVariant = Color.Black,
    surface = VeryLightBlue,
    background = Color(0xFFBEDAFF)

    /* Other default colors to override
    background = Color(0xFFFFFBFE),
    surface = Color(0xFFFFFBFE),
    onPrimary = Color.White,
    onSecondary = Color.White,
    onTertiary = Color.White,
    onBackground = Color(0xFF1C1B1F),
    onSurface = Color(0xFF1C1B1F),
    */
)

@Composable
fun PolyscribeTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    // Dynamic color is available on Android 12+
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }

        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}