package com.example.takstud.accessibility

import android.content.Context
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.floatPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

/**
 * AccessibilityManager - Gerencia configurações de acessibilidade (A11y)
 * Implementa suporte a:
 * - Texto ampliado
 * - Alto contraste
 * - Modo de cores reduzidas
 * - Feedback por vibração
 * - Navegação facilitada
 */
class AccessibilityManager(private val context: Context) {

    companion object {
        private const val PREFS_NAME = "accessibility_preferences"
        private val LARGE_TEXT_KEY = booleanPreferencesKey("large_text")
        private val HIGH_CONTRAST_KEY = booleanPreferencesKey("high_contrast")
        private val REDUCED_MOTION_KEY = booleanPreferencesKey("reduced_motion")
        private val HAPTIC_FEEDBACK_KEY = booleanPreferencesKey("haptic_feedback")
        private val TEXT_SCALE_KEY = floatPreferencesKey("text_scale")
    }

    private val dataStore: androidx.datastore.core.DataStore<Preferences>
        get() = context.accessibilityPreferencesDataStore

    /**
     * Verificar se texto ampliado está ativado
     */
    fun isLargeTextEnabledFlow(): Flow<Boolean> {
        return dataStore.data.map { preferences ->
            preferences[LARGE_TEXT_KEY] ?: false
        }
    }

    /**
     * Ativar/desativar texto ampliado
     */
    suspend fun setLargeTextEnabled(enabled: Boolean) {
        dataStore.edit { preferences ->
            preferences[LARGE_TEXT_KEY] = enabled
        }
    }

    /**
     * Verificar se alto contraste está ativado
     */
    fun isHighContrastEnabledFlow(): Flow<Boolean> {
        return dataStore.data.map { preferences ->
            preferences[HIGH_CONTRAST_KEY] ?: false
        }
    }

    /**
     * Ativar/desativar alto contraste
     */
    suspend fun setHighContrastEnabled(enabled: Boolean) {
        dataStore.edit { preferences ->
            preferences[HIGH_CONTRAST_KEY] = enabled
        }
    }

    /**
     * Verificar se movimento reduzido está ativado
     */
    fun isReducedMotionEnabledFlow(): Flow<Boolean> {
        return dataStore.data.map { preferences ->
            preferences[REDUCED_MOTION_KEY] ?: false
        }
    }

    /**
     * Ativar/desativar movimento reduzido
     */
    suspend fun setReducedMotionEnabled(enabled: Boolean) {
        dataStore.edit { preferences ->
            preferences[REDUCED_MOTION_KEY] = enabled
        }
    }

    /**
     * Verificar se feedback háptico está ativado
     */
    fun isHapticFeedbackEnabledFlow(): Flow<Boolean> {
        return dataStore.data.map { preferences ->
            preferences[HAPTIC_FEEDBACK_KEY] ?: true
        }
    }

    /**
     * Ativar/desativar feedback háptico
     */
    suspend fun setHapticFeedbackEnabled(enabled: Boolean) {
        dataStore.edit { preferences ->
            preferences[HAPTIC_FEEDBACK_KEY] = enabled
        }
    }

    /**
     * Obter escala de texto (1.0 = normal, 1.2 = 20% maior, etc)
     */
    fun getTextScaleFlow(): Flow<Float> {
        return dataStore.data.map { preferences ->
            preferences[TEXT_SCALE_KEY] ?: 1.0f
        }
    }

    /**
     * Definir escala de texto
     */
    suspend fun setTextScale(scale: Float) {
        if (scale < 0.8f || scale > 2.0f) {
            throw IllegalArgumentException("Escala deve estar entre 0.8 e 2.0")
        }
        dataStore.edit { preferences ->
            preferences[TEXT_SCALE_KEY] = scale
        }
    }

    /**
     * Resetar todas as configurações de acessibilidade
     */
    suspend fun resetAllSettings() {
        dataStore.edit { preferences ->
            preferences.remove(LARGE_TEXT_KEY)
            preferences.remove(HIGH_CONTRAST_KEY)
            preferences.remove(REDUCED_MOTION_KEY)
            preferences.remove(HAPTIC_FEEDBACK_KEY)
            preferences.remove(TEXT_SCALE_KEY)
        }
    }

    /**
     * Preset: Ativar configurações para usuários com visão baixa
     */
    suspend fun enableLowVisionPreset() {
        dataStore.edit { preferences ->
            preferences[LARGE_TEXT_KEY] = true
            preferences[HIGH_CONTRAST_KEY] = true
            preferences[TEXT_SCALE_KEY] = 1.5f
        }
    }

    /**
     * Preset: Ativar configurações para usuários sensíveis a movimento
     */
    suspend fun enableMotionSensitivityPreset() {
        dataStore.edit { preferences ->
            preferences[REDUCED_MOTION_KEY] = true
            preferences[HAPTIC_FEEDBACK_KEY] = false
        }
    }

    /**
     * Preset: Ativar todas as configurações de acessibilidade
     */
    suspend fun enableAllAccessibilityFeatures() {
        dataStore.edit { preferences ->
            preferences[LARGE_TEXT_KEY] = true
            preferences[HIGH_CONTRAST_KEY] = true
            preferences[REDUCED_MOTION_KEY] = true
            preferences[HAPTIC_FEEDBACK_KEY] = true
            preferences[TEXT_SCALE_KEY] = 1.3f
        }
    }
}

// Extensão para criar DataStore
private val android.content.Context.accessibilityPreferencesDataStore: androidx.datastore.core.DataStore<Preferences> by androidx.datastore.preferences.preferencesDataStore(
    name = "accessibility_preferences"
)

/**
 * Extensão para aplicar escala de texto em Composables
 */
fun androidx.compose.material3.Typography.withAccessibilityScale(scale: Float): androidx.compose.material3.Typography {
    return androidx.compose.material3.Typography(
        displayLarge = this.displayLarge.copy(fontSize = this.displayLarge.fontSize * scale),
        displayMedium = this.displayMedium.copy(fontSize = this.displayMedium.fontSize * scale),
        displaySmall = this.displaySmall.copy(fontSize = this.displaySmall.fontSize * scale),
        headlineLarge = this.headlineLarge.copy(fontSize = this.headlineLarge.fontSize * scale),
        headlineMedium = this.headlineMedium.copy(fontSize = this.headlineMedium.fontSize * scale),
        headlineSmall = this.headlineSmall.copy(fontSize = this.headlineSmall.fontSize * scale),
        titleLarge = this.titleLarge.copy(fontSize = this.titleLarge.fontSize * scale),
        titleMedium = this.titleMedium.copy(fontSize = this.titleMedium.fontSize * scale),
        titleSmall = this.titleSmall.copy(fontSize = this.titleSmall.fontSize * scale),
        bodyLarge = this.bodyLarge.copy(fontSize = this.bodyLarge.fontSize * scale),
        bodyMedium = this.bodyMedium.copy(fontSize = this.bodyMedium.fontSize * scale),
        bodySmall = this.bodySmall.copy(fontSize = this.bodySmall.fontSize * scale),
        labelLarge = this.labelLarge.copy(fontSize = this.labelLarge.fontSize * scale),
        labelMedium = this.labelMedium.copy(fontSize = this.labelMedium.fontSize * scale),
        labelSmall = this.labelSmall.copy(fontSize = this.labelSmall.fontSize * scale)
    )
}
