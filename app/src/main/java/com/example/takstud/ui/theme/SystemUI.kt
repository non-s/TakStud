package com.example.takstud.ui.theme

import android.app.Activity
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import com.google.accompanist.systemuicontroller.rememberSystemUiController

/**
 * Customiza cores da Status Bar e Navigation Bar para a marca TakStud
 */
@Composable
fun SetupSystemUI(
    statusBarColor: Color = PrimaryBrand,
    navigationBarColor: Color = MaterialTheme.colorScheme.surface,
    darkIcons: Boolean = false
) {
    val view = LocalView.current
    val systemUiController = rememberSystemUiController()
    val useDarkIcons = darkIcons

    SideEffect {
        if (view.isAttachedToWindow) {
            // Configurar Status Bar
            systemUiController.setStatusBarColor(
                color = statusBarColor,
                darkIcons = useDarkIcons
            )

            // Configurar Navigation Bar
            systemUiController.setNavigationBarColor(
                color = navigationBarColor,
                darkIcons = useDarkIcons
            )

            // Sistema de barras de sistema (Android 5.0+)
            (view.context as? Activity)?.window?.statusBarColor = statusBarColor.toArgb()
        }
    }
}

/**
 * Tema específico para telas do responsável (tom ciano)
 */
@Composable
fun SetupParentTheme() {
    SetupSystemUI(
        statusBarColor = SecondaryBrand,
        navigationBarColor = SecondaryBrand.copy(alpha = 0.9f),
        darkIcons = false
    )
}

/**
 * Tema específico para telas do professor (tom roxo)
 */
@Composable
fun SetupTeacherTheme() {
    SetupSystemUI(
        statusBarColor = PrimaryBrand,
        navigationBarColor = PrimaryBrand.copy(alpha = 0.9f),
        darkIcons = false
    )
}

/**
 * Tema para telas de login (gradiente)
 */
@Composable
fun SetupLoginTheme() {
    SetupSystemUI(
        statusBarColor = PrimaryBrand,
        navigationBarColor = Color.White,
        darkIcons = true
    )
}
