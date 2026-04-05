package com.example.webcamstreaming.ui.theme

import android.app.Activity
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

private val LightColorScheme = lightColorScheme(
    primary = Primary,
    onPrimary = OnPrimary,
    primaryContainer = Primary.copy(alpha = 0.2f),
    background = Surface,
    onBackground = OnSurface,
    surface = Surface,
    onSurface = OnSurface,
    surfaceVariant = SurfaceVariant,
    onSurfaceVariant = OnSurfaceVariant,
    error = Error,
    outline = Outline,
    outlineVariant = Color(0xFFCAC4D0)
)

private val IrisDarkColorScheme = darkColorScheme(
    primary = Primary,
    onPrimary = OnPrimary,
    primaryContainer = PrimaryDark.copy(alpha = 0.45f),
    onPrimaryContainer = Color(0xFFB2EBF2),
    secondary = Primary.copy(alpha = 0.85f),
    onSecondary = OnPrimary,
    secondaryContainer = IrisDarkSurfaceVariant,
    onSecondaryContainer = IrisDarkOnSurface,
    tertiary = PrimaryDark,
    onTertiary = OnPrimary,
    background = IrisDarkBackground,
    onBackground = IrisDarkOnBackground,
    surface = IrisDarkSurface,
    onSurface = IrisDarkOnSurface,
    surfaceVariant = IrisDarkSurfaceVariant,
    onSurfaceVariant = IrisDarkOnSurfaceVariant,
    surfaceTint = Primary.copy(alpha = 0.12f),
    error = Color(0xFFFFB4AB),
    onError = Color(0xFF690005),
    errorContainer = Color(0xFF93000A),
    onErrorContainer = Color(0xFFFFDAD6),
    outline = IrisDarkOutline,
    outlineVariant = IrisDarkOutlineVariant,
    scrim = Color(0xFF000000)
)

/** Material 3 theme for the Iris app. */
@Composable
fun WebcamStreamingTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) IrisDarkColorScheme else LightColorScheme
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            val controller = WindowCompat.getInsetsController(window, view)
            if (darkTheme) {
                window.statusBarColor = colorScheme.surface.toArgb()
                window.navigationBarColor = colorScheme.surface.toArgb()
                controller.isAppearanceLightStatusBars = false
                controller.isAppearanceLightNavigationBars = false
            } else {
                // Light mode: cyan status bar (Iris accent), white nav bar — wie zuvor
                window.statusBarColor = colorScheme.primary.toArgb()
                window.navigationBarColor = colorScheme.surface.toArgb()
                controller.isAppearanceLightStatusBars = false
                controller.isAppearanceLightNavigationBars = true
            }
        }
    }
    MaterialTheme(
        colorScheme = colorScheme,
        content = content
    )
}
