package com.example.ui.theme

import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color

// Primary Gradient Spectrum (Violet -> Cyan -> Gold)
val NourViolet = Color(0xFF6C47FF)
val NourVioletLight = Color(0xFF8E71FF)
val NourVioletDark = Color(0xFF4527A0)

val NourCyan = Color(0xFF00C6FF)
val NourBlue = Color(0xFF0072FF)
val NourSky = Color(0xFF38E1FF)

val NourGold = Color(0xFFFFB800)
val NourGoldLight = Color(0xFFFFD566)
val NourGoldDark = Color(0xFFE5A100)

// Premium Backgrounds & Obsidian Dark Theme
val ObsidianBlack = Color(0xFF080711)
val ObsidianSurface = Color(0xFF100D22)
val ObsidianCard = Color(0xFF181432)
val ObsidianCardElevated = Color(0xFF231E44)
val ObsidianCardBorder = Color(0x338E71FF)

// Light Theme Palettes
val LightBackground = Color(0xFFF7F8FD)
val LightSurface = Color(0xFFFFFFFF)
val LightCard = Color(0xFFFFFFFF)
val LightCardElevated = Color(0xFFF0F3FF)
val LightBorder = Color(0xFFE2E6F5)

// Functional Status
val SuccessMint = Color(0xFF00E676)
val NourEmerald = Color(0xFF00E676)
val DangerCoral = Color(0xFFFF5252)
val NourCoral = Color(0xFFFF5252)
val WarningAmber = Color(0xFFFFAB00)
val InfoSky = Color(0xFF40C4FF)

// Text Colors
val TextPrimaryDark = Color(0xFFFFFFFF)
val TextSecondaryDark = Color(0xFFB0A8D0)
val TextTertiaryDark = Color(0xFF7E76A0)

val TextPrimaryLight = Color(0xFF120E29)
val TextSecondaryLight = Color(0xFF5A527A)
val TextTertiaryLight = Color(0xFF8E88AA)

// Brushes
val NourPrimaryGradient = Brush.horizontalGradient(
    listOf(NourViolet, NourCyan)
)

val NourHeroGradient = Brush.linearGradient(
    listOf(NourViolet, Color(0xFF7C3AED), NourCyan, NourGold)
)

val NourOrbGradient = Brush.radialGradient(
    listOf(NourGoldLight, NourCyan, NourViolet, Color.Transparent)
)

val EmergencyGradient = Brush.horizontalGradient(
    listOf(Color(0xFFFF3366), Color(0xFFFF7A00))
)

val SuccessPathGradient = Brush.horizontalGradient(
    listOf(NourCyan, NourViolet, NourGold)
)

val GoldRewardGradient = Brush.linearGradient(
    listOf(Color(0xFFFFDF70), Color(0xFFFFB800), Color(0xFFE08E00))
)

val CardGlassGradient = Brush.verticalGradient(
    listOf(Color(0x332A2255), Color(0x1A1535))
)
