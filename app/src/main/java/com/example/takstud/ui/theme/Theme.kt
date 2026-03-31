package com.example.takstud.ui.theme

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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

// Dark Theme - Elegante, Profundo e Vibrante
private val DarkColorScheme = darkColorScheme(
    primary = ElectricBlue,
    onPrimary = Color.White,
    primaryContainer = PrimaryBlue,
    onPrimaryContainer = Color.White,
    secondary = AccentTeal,
    onSecondary = DarkBackground,
    secondaryContainer = VibrantPurple,
    onSecondaryContainer = Color.White,
    tertiary = AccentPink,
    onTertiary = Color.White,
    tertiaryContainer = DeepPurple,
    onTertiaryContainer = LightPurple,
    background = DarkBackground,
    onBackground = Neutral100,
    surface = DarkSurface,
    onSurface = Neutral100,
    surfaceVariant = DarkSurfaceVariant,
    onSurfaceVariant = Neutral200,
    error = ErrorBright,
    onError = Color.White,
    outline = Neutral600,
    surfaceTint = ElectricBlue
)

// Light Theme - Limpo, Vibrante e Energético
private val LightColorScheme = lightColorScheme(
    primary = PrimaryBlue,
    onPrimary = Color.White,
    primaryContainer = PrimaryLight.copy(alpha = 0.15f),
    onPrimaryContainer = PrimaryDark,
    secondary = AccentTeal,
    onSecondary = Color.White,
    secondaryContainer = AccentTeal.copy(alpha = 0.12f),
    onSecondaryContainer = Color(0xFF006B5D),
    tertiary = VibrantPurple,
    onTertiary = Color.White,
    tertiaryContainer = LightPurple.copy(alpha = 0.15f),
    onTertiaryContainer = DeepPurple,
    background = Neutral50,
    onBackground = Neutral900,
    surface = Color.White,
    onSurface = Neutral900,
    surfaceVariant = Neutral100,
    onSurfaceVariant = Neutral700,
    error = Error,
    onError = Color.White,
    outline = Neutral300,
    surfaceTint = PrimaryBlue
)

@Composable
fun TakStudTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    // Dynamic color is available on Android 12+
    dynamicColor: Boolean = false, // Desabilitando dynamic color por padrão para manter a identidade visual da marca
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

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = colorScheme.background.toArgb() // Status bar combina com o fundo
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !darkTheme
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        shapes = Shapes,
        content = content
    )
}