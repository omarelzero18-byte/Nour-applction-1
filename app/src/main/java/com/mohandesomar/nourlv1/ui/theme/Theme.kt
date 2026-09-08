package com.mohandesomar.nourlv1.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val DarkColorScheme = darkColorScheme(
    primary = NourViolet,
    onPrimary = TextPrimaryDark,
    primaryContainer = ObsidianCardElevated,
    onPrimaryContainer = NourCyan,
    secondary = NourCyan,
    onSecondary = ObsidianBlack,
    secondaryContainer = ObsidianCard,
    onSecondaryContainer = NourGoldLight,
    tertiary = NourGold,
    onTertiary = ObsidianBlack,
    background = ObsidianBlack,
    onBackground = TextPrimaryDark,
    surface = ObsidianSurface,
    onSurface = TextPrimaryDark,
    surfaceVariant = ObsidianCard,
    onSurfaceVariant = TextSecondaryDark,
    outline = ObsidianCardBorder
)

private val LightColorScheme = lightColorScheme(
    primary = NourViolet,
    onPrimary = TextPrimaryDark,
    primaryContainer = LightCardElevated,
    onPrimaryContainer = NourVioletDark,
    secondary = NourBlue,
    onSecondary = TextPrimaryDark,
    secondaryContainer = LightCard,
    onSecondaryContainer = NourVioletDark,
    tertiary = NourGoldDark,
    onTertiary = TextPrimaryDark,
    background = LightBackground,
    onBackground = TextPrimaryLight,
    surface = LightSurface,
    onSurface = TextPrimaryLight,
    surfaceVariant = LightCard,
    onSurfaceVariant = TextSecondaryLight,
    outline = LightBorder
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = true,
    fontScale: Float = 1.0f,
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = colorScheme.background.toArgb()
            window.navigationBarColor = colorScheme.background.toArgb()
            WindowCompat.getInsetsController(window, view).apply {
                isAppearanceLightStatusBars = !darkTheme
                isAppearanceLightNavigationBars = !darkTheme
            }
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = getTypography(fontScale),
        content = content
    )
}

