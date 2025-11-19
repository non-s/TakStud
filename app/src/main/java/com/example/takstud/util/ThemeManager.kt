package com.example.takstud.util

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

/**
 * ThemeManager - Gerencia preferência de tema (claro/escuro)
 * Usa DataStore para armazenar preferência do usuário
 */
class ThemeManager(private val context: Context) {

    companion object {
        private const val PREFS_NAME = "theme_preferences"
        private val DARK_MODE_KEY = booleanPreferencesKey("dark_mode")
        private val DYNAMIC_COLORS_KEY = booleanPreferencesKey("dynamic_colors")
    }

    private val dataStore: DataStore<Preferences>
        get() = context.themePreferencesDataStore

    /**
     * Obter preferência de dark mode como Flow
     */
    fun isDarkModeFlow(): Flow<Boolean> {
        return dataStore.data.map { preferences ->
            preferences[DARK_MODE_KEY] ?: false // Default: light mode
        }
    }

    /**
     * Obter preferência de cores dinâmicas como Flow
     */
    fun isDynamicColorsFlow(): Flow<Boolean> {
        return dataStore.data.map { preferences ->
            preferences[DYNAMIC_COLORS_KEY] ?: true // Default: true (se Android 12+)
        }
    }

    /**
     * Setar dark mode
     */
    suspend fun setDarkMode(enabled: Boolean) {
        dataStore.edit { preferences ->
            preferences[DARK_MODE_KEY] = enabled
        }
    }

    /**
     * Toggle dark mode
     */
    suspend fun toggleDarkMode() {
        dataStore.edit { preferences ->
            val current = preferences[DARK_MODE_KEY] ?: false
            preferences[DARK_MODE_KEY] = !current
        }
    }

    /**
     * Setar cores dinâmicas
     */
    suspend fun setDynamicColors(enabled: Boolean) {
        dataStore.edit { preferences ->
            preferences[DYNAMIC_COLORS_KEY] = enabled
        }
    }

    /**
     * Reset para valores padrão
     */
    suspend fun reset() {
        dataStore.edit { preferences ->
            preferences.clear()
        }
    }
}

// Extensão para criar DataStore
private val Context.themePreferencesDataStore: DataStore<Preferences> by preferencesDataStore(
    name = "theme_preferences"
)
