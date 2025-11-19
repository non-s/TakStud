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
import androidx.compose.ui.platform.LocalContext

// ===== DARK COLOR SCHEME (Tema Claro Profissional - Branco/Azul Marinho) =====
private val DarkColorScheme = darkColorScheme(
    primary = NavyBlue,                  // Azul Marinho ✨
    onPrimary = PureWhite,               // Branco para texto
    primaryContainer = LightNavy,        // Container azul marinho claro
    onPrimaryContainer = NavyBlue,       // Texto azul marinho
    secondary = PureWhite,               // Branco ✨
    onSecondary = NavyBlue,              // Texto azul marinho
    secondaryContainer = LightGray,      // Container cinza claro
    onSecondaryContainer = PureWhite,
    tertiary = AccentBlue,
    onTertiary = PureWhite,
    tertiaryContainer = SkyBlue.copy(alpha = 0.2f),
    onTertiaryContainer = AccentBlue,
    error = ErrorRed,
    onError = PureWhite,
    errorContainer = ErrorRed.copy(alpha = 0.1f),
    onErrorContainer = ErrorRed,
    background = PureWhite,              // Fundo branco
    onBackground = NavyBlue,             // Texto azul marinho
    surface = LightGray,                 // Superfícies cinza claro
    onSurface = NavyBlue,                // Texto azul marinho
    surfaceVariant = MediumGray,
    onSurfaceVariant = DarkGray,
    outline = NavyBlue,                  // Bordas azul marinho
    outlineVariant = AccentBlue,
    scrim = androidx.compose.ui.graphics.Color.Black
)

// ===== LIGHT COLOR SCHEME (Tema Claro Profissional - Branco/Azul Marinho) =====
private val LightColorScheme = lightColorScheme(
    primary = NavyBlue,                  // Azul Marinho ✨
    onPrimary = PureWhite,
    primaryContainer = LightNavy,        // Container azul marinho claro
    onPrimaryContainer = NavyBlue,       // Texto azul marinho
    secondary = PureWhite,               // Branco ✨
    onSecondary = NavyBlue,
    secondaryContainer = LightGray,      // Container cinza claro
    onSecondaryContainer = PureWhite,
    tertiary = AccentBlue,
    onTertiary = PureWhite,
    tertiaryContainer = SkyBlue.copy(alpha = 0.15f),
    onTertiaryContainer = AccentBlue,
    error = ErrorRed,
    onError = PureWhite,
    errorContainer = ErrorRed.copy(alpha = 0.15f),
    onErrorContainer = ErrorRed,
    background = PureWhite,              // Fundo branco
    onBackground = NavyBlue,             // Texto azul marinho
    surface = LightGray,                 // Superfícies cinza claro
    onSurface = NavyBlue,                // Texto azul marinho
    surfaceVariant = MediumGray,
    onSurfaceVariant = DarkGray,
    outline = NavyBlue,                  // Bordas azul marinho
    outlineVariant = AccentBlue,
    scrim = androidx.compose.ui.graphics.Color.Black
)

/**
 * TakStudTheme - Composable que aplica o tema da aplicação
 * Suporta tema claro/escuro, cores dinâmicas e preferências persistidas do usuário
 *
 * @param darkTheme Boolean para forçar tema escuro (null = usar preferência do usuário/sistema)
 * @param dynamicColor Se true, usa cores dinâmicas do Material You (Android 12+)
 * @param content O conteúdo a ser renderizado com o tema aplicado
 */
@Composable
fun TakStudTheme(
    darkTheme: Boolean? = null,
    // Dynamic color is available on Android 12+
    dynamicColor: Boolean = true,
    content: @Composable () -> Unit
) {
    val context = LocalContext.current
    val isDarkMode = when {
        darkTheme != null -> darkTheme
        else -> isSystemInDarkTheme()
    }

    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            if (isDarkMode) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }

        isDarkMode -> DarkColorScheme
        else -> LightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}