package com.openmusic.app.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val DarkColorScheme = darkColorScheme(
    primary = OpenMusicPrimaryDark,
    onPrimary = OpenMusicOnPrimaryDark,
    primaryContainer = OpenMusicPrimaryContainerDark,
    onPrimaryContainer = OpenMusicOnPrimaryContainerDark,
    background = OpenMusicBackgroundDark,
    onBackground = OpenMusicOnBackgroundDark,
    surface = OpenMusicSurfaceDark,
    onSurface = OpenMusicOnSurfaceDark,
    surfaceVariant = OpenMusicSurfaceVariantDark,
    onSurfaceVariant = OpenMusicOnSurfaceVariantDark
)

private val AmoledDarkColorScheme = DarkColorScheme.copy(
    background = androidx.compose.ui.graphics.Color.Black,
    surface = androidx.compose.ui.graphics.Color.Black,
    surfaceVariant = androidx.compose.ui.graphics.Color(0xFF101010)
)

private val LightColorScheme = lightColorScheme(
    primary = OpenMusicPrimaryLight,
    onPrimary = OpenMusicOnPrimaryLight,
    primaryContainer = OpenMusicPrimaryContainerLight,
    onPrimaryContainer = OpenMusicOnPrimaryContainerLight,
    background = OpenMusicBackgroundLight,
    onBackground = OpenMusicOnBackgroundLight,
    surface = OpenMusicSurfaceLight,
    onSurface = OpenMusicOnSurfaceLight,
    surfaceVariant = OpenMusicSurfaceVariantLight,
    onSurfaceVariant = OpenMusicOnSurfaceVariantLight
)

@Composable
fun OpenMusicTheme(
    // Setting darkTheme = true forces the app to default to Dark Theme!
    darkTheme: Boolean = true,
    amoledBlack: Boolean = false,
    // Disabling dynamic colors to enforce our custom Music app palettes!
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme && amoledBlack -> AmoledDarkColorScheme
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = colorScheme.background.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !darkTheme
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
