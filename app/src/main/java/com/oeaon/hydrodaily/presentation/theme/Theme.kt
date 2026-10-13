package com.oeaon.hydrodaily.presentation.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

private val LightColors = lightColorScheme(
    primary = Ocean40,
    onPrimary = Color.White,
    primaryContainer = Ocean90,
    onPrimaryContainer = Ocean10,
    secondary = Aqua40,
    onSecondary = Color.White,
    secondaryContainer = Aqua90,
    onSecondaryContainer = Aqua30,
    tertiary = Mint40,
    background = Mist99,
    onBackground = Slate10,
    surface = Mist99,
    onSurface = Slate10,
    surfaceVariant = Slate90,
    onSurfaceVariant = Slate50,
    surfaceContainerLowest = Color.White,
    surfaceContainerLow = Mist95,
    surfaceContainer = Mist95,
    surfaceContainerHigh = Slate90,
    outline = Slate80,
    outlineVariant = Slate90
)

private val DarkColors = darkColorScheme(
    primary = Ocean80,
    onPrimary = Ocean20,
    primaryContainer = Ocean30,
    onPrimaryContainer = Ocean90,
    secondary = Aqua80,
    onSecondary = Aqua30,
    secondaryContainer = Aqua30,
    onSecondaryContainer = Aqua90,
    tertiary = Mint80,
    background = Slate10,
    onBackground = Slate90,
    surface = Slate10,
    onSurface = Slate90,
    surfaceVariant = Slate20,
    onSurfaceVariant = Slate80,
    surfaceContainerLowest = Slate10,
    surfaceContainerLow = Slate15,
    surfaceContainer = Slate15,
    surfaceContainerHigh = Slate20,
    outline = Slate30,
    outlineVariant = Slate20
)

@Immutable
data class WaterColors(val start: Color, val end: Color, val track: Color)

val LocalWaterColors = staticCompositionLocalOf { WaterColors(Aqua40, Ocean40, Slate90) }

private val AppShapes = Shapes(
    small = RoundedCornerShape(12.dp),
    medium = RoundedCornerShape(20.dp),
    large = RoundedCornerShape(28.dp)
)

@Composable
fun HydroDailyTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colors = if (darkTheme) DarkColors else LightColors
    val waterColors = if (darkTheme) {
        WaterColors(start = Aqua80, end = Ocean80, track = Slate20)
    } else {
        WaterColors(start = Aqua40, end = Ocean40, track = Slate90)
    }
    CompositionLocalProvider(LocalWaterColors provides waterColors) {
        MaterialTheme(
            colorScheme = colors,
            typography = AppTypography,
            shapes = AppShapes,
            content = content
        )
    }
}
