package com.example.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val YouTubeDarkColorScheme = darkColorScheme(
    primary = YouTubeRed,
    onPrimary = Color.White,
    primaryContainer = YouTubeDarkRed,
    onPrimaryContainer = Color.White,
    secondary = YouTubeBlue,
    onSecondary = Color.White,
    secondaryContainer = YouTubeDarkSurfaceVariant,
    onSecondaryContainer = Color.White,
    tertiary = YouTubePurple,
    background = YouTubeDarkBackground,
    onBackground = YouTubeTextPrimary,
    surface = YouTubeDarkBackground,
    onSurface = YouTubeTextPrimary,
    surfaceVariant = YouTubeDarkSurfaceVariant,
    onSurfaceVariant = YouTubeTextSecondary,
    outline = YouTubeDarkOutline
)

private val YouTubeLightColorScheme = lightColorScheme(
    primary = YouTubeRed,
    onPrimary = Color.White,
    primaryContainer = YouTubeLightRed,
    onPrimaryContainer = Color.White,
    secondary = YouTubeBlue,
    onSecondary = Color.White,
    secondaryContainer = YouTubeLightSurfaceVariant,
    onSecondaryContainer = YouTubeLightTextPrimary,
    tertiary = YouTubePurple,
    background = YouTubeLightBackground,
    onBackground = YouTubeLightTextPrimary,
    surface = YouTubeLightBackground,
    onSurface = YouTubeLightTextPrimary,
    surfaceVariant = YouTubeLightSurface,
    onSurfaceVariant = YouTubeLightTextSecondary,
    outline = YouTubeLightOutline
)

@Composable
fun RomiTubeTheme(
    darkTheme: Boolean = true,
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    MyApplicationTheme(darkTheme = darkTheme, dynamicColor = dynamicColor, content = content)
}

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = true, // Default to YouTube's signature dark theme
    dynamicColor: Boolean = false, // Keep authentic YouTube branding colors
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) YouTubeDarkColorScheme else YouTubeLightColorScheme
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as? Activity)?.window ?: return@SideEffect
            window.statusBarColor = colorScheme.background.toArgb()
            window.navigationBarColor = colorScheme.background.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !darkTheme
            WindowCompat.getInsetsController(window, view).isAppearanceLightNavigationBars = !darkTheme
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
