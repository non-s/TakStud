package com.example.takstud.ui.theme

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

/**
 * Gerenciador de preferências de tema usando DataStore
 *
 * Responsabilidades:
 * - Armazenar preferência de tema (Light/Dark)
 * - Fornecer Flow para observar mudanças
 * - Persistir escolha do usuário
 */
class ThemePreferences(private val context: Context) {

    companion object {
        private val Context.dataStore: DataStore<Preferences> by preferencesDataStore("theme_preferences")
        private val IS_DARK_MODE = booleanPreferencesKey("is_dark_mode")
    }

    /**
     * Flow que emite o estado atual do tema
     * true = Dark Mode, false = Light Mode
     */
    val isDarkMode: Flow<Boolean> = context.dataStore.data
        .map { preferences ->
            preferences[IS_DARK_MODE] ?: false // Default: Light Mode
        }

    /**
     * Salva a preferência de tema
     */
    suspend fun setDarkMode(isDark: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[IS_DARK_MODE] = isDark
        }
    }

    /**
     * Alterna entre Light e Dark mode
     */
    suspend fun toggleTheme() {
        context.dataStore.edit { preferences ->
            val current = preferences[IS_DARK_MODE] ?: false
            preferences[IS_DARK_MODE] = !current
        }
    }
}
