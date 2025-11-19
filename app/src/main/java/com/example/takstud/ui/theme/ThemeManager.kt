package com.example.takstud.ui.theme

import android.app.Application
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import androidx.compose.runtime.LaunchedEffect

/**
 * ThemeManager - Gerencia temas e modo escuro.
 *
 * FUNCIONALIDADES:
 * - Dark mode automático
 * - Material You dynamic colors
 * - Temas customizáveis
 * - Persistência de preferências
 * - Suporte a light/dark/auto
 *
 * MODOS:
 * - AUTO: Segue configuração do sistema
 * - LIGHT: Sempre modo claro
 * - DARK: Sempre modo escuro
 *
 * EXEMPLO DE USO:
 * val themeManager = ThemeManager(context)
 * val isDarkMode by themeManager.isDarkMode.collectAsState()
 * themeManager.setThemeMode(ThemeMode.DARK)
 */
class ThemeManager(private val application: Application) {

    enum class ThemeMode {
        AUTO,      // Segue configuração do sistema
        LIGHT,     // Sempre claro
        DARK       // Sempre escuro
    }

    private val _themeMode = MutableStateFlow(ThemeMode.AUTO)
    val themeMode: StateFlow<ThemeMode> = _themeMode.asStateFlow()

    private val _isDarkMode = MutableStateFlow(false)
    val isDarkMode: StateFlow<Boolean> = _isDarkMode.asStateFlow()

    private val _useDynamicColors = MutableStateFlow(true)
    val useDynamicColors: StateFlow<Boolean> = _useDynamicColors.asStateFlow()

    init {
        loadThemePreferences()
    }

    /**
     * Define o modo do tema (chame do Composable).
     */
    fun setThemeMode(mode: ThemeMode, systemDarkMode: Boolean = false) {
        _themeMode.value = mode
        updateDarkMode(systemDarkMode)
        saveThemePreferences()
    }

    /**
     * Atualiza estado de dark mode baseado no modo selecionado.
     */
    fun updateDarkMode(systemDarkMode: Boolean) {
        val isDark = when (_themeMode.value) {
            ThemeMode.AUTO -> systemDarkMode
            ThemeMode.LIGHT -> false
            ThemeMode.DARK -> true
        }
        _isDarkMode.value = isDark
    }

    /**
     * Atualiza dark mode baseado no sistema (chamado do Composable).
     */
    @Composable
    fun updateSystemDarkMode() {
        val systemDarkMode = isSystemInDarkTheme()
        LaunchedEffect(systemDarkMode) {
            if (_themeMode.value == ThemeMode.AUTO) {
                updateDarkMode(systemDarkMode)
            }
        }
    }

    /**
     * Habilita/desabilita cores dinâmicas (Material You).
     */
    fun setDynamicColors(enabled: Boolean) {
        _useDynamicColors.value = enabled
        saveThemePreferences()
    }

    /**
     * Carrega preferências de tema.
     */
    private fun loadThemePreferences() {
        val prefs = application.getSharedPreferences("theme", Application.MODE_PRIVATE)
        val themeModeName = prefs.getString("theme_mode", "AUTO") ?: "AUTO"
        val useDynamicColors = prefs.getBoolean("dynamic_colors", true)

        _themeMode.value = ThemeMode.valueOf(themeModeName)
        _useDynamicColors.value = useDynamicColors
    }

    /**
     * Salva preferências de tema.
     */
    private fun saveThemePreferences() {
        val prefs = application.getSharedPreferences("theme", Application.MODE_PRIVATE)
        prefs.edit().apply {
            putString("theme_mode", _themeMode.value.name)
            putBoolean("dynamic_colors", _useDynamicColors.value)
            apply()
        }
    }
}

/**
 * Nota: As paletas de cores estão definidas em Theme.kt
 * Este arquivo apenas gerencia o estado do tema e preferências
 */
