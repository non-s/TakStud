package com.example.takstud.ui.theme

import android.app.Application
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.runtime.*
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * ThemeCustomization - Customização avançada de temas.
 *
 * FUNCIONALIDADES:
 * - Paletas de cores customizadas
 * - Gradientes e efeitos visuais
 * - Tipografia customizável
 * - Modo noturno com filtros
 * - Presets de tema predefinidos
 * - Sincronização com preferências do usuário
 *
 * TEMAS PRÉ-DEFINIDOS:
 * - SYSTEM: Segue configuração do sistema (padrão)
 * - LIGHT: Modo claro
 * - DARK: Modo escuro
 * - FOREST: Verde e marrom (tema natural)
 * - OCEAN: Azul e ciano (tema aquático)
 * - SUNSET: Laranja e rosa (tema pôr do sol)
 * - MIDNIGHT: Azul profundo (tema noturno)
 * - CUSTOM: Cores personalizadas
 *
 * EXEMPLO DE USO:
 * val themeCustomizer = ThemeCustomizer(context)
 * themeCustomizer.setThemePreset(ThemePreset.OCEAN)
 * val colors by themeCustomizer.colors.collectAsState()
 */

/**
 * Presets de tema predefinidos com paletas de cores.
 */
enum class ThemePreset {
    SYSTEM,     // Sistema padrão
    LIGHT,      // Claro
    DARK,       // Escuro
    FOREST,     // Verde e marrom
    OCEAN,      // Azul e ciano
    SUNSET,     // Laranja e rosa
    MIDNIGHT,   // Azul profundo
    CUSTOM      // Personalizado
}

/**
 * Modelo de configuração de cores customizadas.
 */
data class CustomColorConfig(
    val primary: Color = Color(0xFF6200EE),
    val secondary: Color = Color(0xFF03DAC6),
    val tertiary: Color = Color(0xFF3700B3),
    val background: Color = Color(0xFFFFFFFF),
    val surface: Color = Color(0xFFFAFAFA),
    val error: Color = Color(0xFFB00020),
    val onPrimary: Color = Color(0xFFFFFFFF),
    val onSecondary: Color = Color(0xFF000000),
    val onTertiary: Color = Color(0xFFFFFFFF),
    val onBackground: Color = Color(0xFF000000),
    val onSurface: Color = Color(0xFF000000),
    val onError: Color = Color(0xFFFFFFFF)
)

/**
 * Gerenciador de customização de temas.
 */
class ThemeCustomizer(private val application: Application) {
    private val _currentPreset = MutableStateFlow(ThemePreset.SYSTEM)
    val currentPreset: StateFlow<ThemePreset> = _currentPreset.asStateFlow()

    private val _customColors = MutableStateFlow(CustomColorConfig())
    val customColors: StateFlow<CustomColorConfig> = _customColors.asStateFlow()

    private val _colors = MutableStateFlow(CustomColorConfig())
    val colors: StateFlow<CustomColorConfig> = _colors.asStateFlow()

    private val _useDynamicColors = MutableStateFlow(true)
    val useDynamicColors: StateFlow<Boolean> = _useDynamicColors.asStateFlow()

    private val _nightModeIntensity = MutableStateFlow(0.5f) // 0.0 a 1.0
    val nightModeIntensity: StateFlow<Float> = _nightModeIntensity.asStateFlow()

    init {
        loadThemePreferences()
    }

    /**
     * Define o preset de tema.
     */
    fun setThemePreset(preset: ThemePreset) {
        _currentPreset.value = preset
        _colors.value = getPresetColors(preset)
        saveThemePreferences()
    }

    /**
     * Define cores customizadas.
     */
    fun setCustomColors(config: CustomColorConfig) {
        _currentPreset.value = ThemePreset.CUSTOM
        _customColors.value = config
        _colors.value = config
        saveThemePreferences()
    }

    /**
     * Ajusta intensidade do modo noturno.
     */
    fun setNightModeIntensity(intensity: Float) {
        val clamped = intensity.coerceIn(0f, 1f)
        _nightModeIntensity.value = clamped
        saveThemePreferences()
    }

    /**
     * Define se deve usar cores dinâmicas (Material You).
     */
    fun setDynamicColors(enabled: Boolean) {
        _useDynamicColors.value = enabled
        saveThemePreferences()
    }

    /**
     * Obtém cores para um preset específico.
     */
    private fun getPresetColors(preset: ThemePreset): CustomColorConfig {
        return when (preset) {
            ThemePreset.LIGHT -> CustomColorConfig(
                primary = Color(0xFF6200EE),
                secondary = Color(0xFF03DAC6),
                tertiary = Color(0xFF3700B3),
                background = Color(0xFFFFFFFF),
                surface = Color(0xFFFAFAFA),
                error = Color(0xFFB00020)
            )
            ThemePreset.DARK -> CustomColorConfig(
                primary = Color(0xFFBB86FC),
                secondary = Color(0xFF03DAC6),
                tertiary = Color(0xFF03DAC6),
                background = Color(0xFF121212),
                surface = Color(0xFF1F1F1F),
                error = Color(0xFFCF6679)
            )
            ThemePreset.FOREST -> CustomColorConfig(
                primary = Color(0xFF2D5016),
                secondary = Color(0xFF6B8E23),
                tertiary = Color(0xFF8FBC8F),
                background = Color(0xFFF5F5F0),
                surface = Color(0xFFEEEEE0),
                error = Color(0xFFDC143C)
            )
            ThemePreset.OCEAN -> CustomColorConfig(
                primary = Color(0xFF0077BE),
                secondary = Color(0xFF00D9FF),
                tertiary = Color(0xFF0099E5),
                background = Color(0xFFE8F4F8),
                surface = Color(0xFFD4E8F0),
                error = Color(0xFFFF6B6B)
            )
            ThemePreset.SUNSET -> CustomColorConfig(
                primary = Color(0xFFFF6B35),
                secondary = Color(0xFFFFB703),
                tertiary = Color(0xFFFB5607),
                background = Color(0xFFFFF8F3),
                surface = Color(0xFFFFEDE0),
                error = Color(0xFFCC0000)
            )
            ThemePreset.MIDNIGHT -> CustomColorConfig(
                primary = Color(0xFF1A237E),
                secondary = Color(0xFF283593),
                tertiary = Color(0xFF3F51B5),
                background = Color(0xFF0D1B2A),
                surface = Color(0xFF1B263B),
                error = Color(0xFFEE0000)
            )
            ThemePreset.SYSTEM, ThemePreset.CUSTOM -> CustomColorConfig()
        }
    }

    /**
     * Carrega preferências de tema.
     */
    private fun loadThemePreferences() {
        val prefs = application.getSharedPreferences("theme_customization", Application.MODE_PRIVATE)
        val presetName = prefs.getString("theme_preset", "SYSTEM") ?: "SYSTEM"
        val dynamicColors = prefs.getBoolean("dynamic_colors", true)
        val nightIntensity = prefs.getFloat("night_intensity", 0.5f)

        try {
            _currentPreset.value = ThemePreset.valueOf(presetName)
        } catch (e: IllegalArgumentException) {
            _currentPreset.value = ThemePreset.SYSTEM
        }

        _useDynamicColors.value = dynamicColors
        _nightModeIntensity.value = nightIntensity
        _colors.value = getPresetColors(_currentPreset.value)
    }

    /**
     * Salva preferências de tema.
     */
    private fun saveThemePreferences() {
        val prefs = application.getSharedPreferences("theme_customization", Application.MODE_PRIVATE)
        prefs.edit().apply {
            putString("theme_preset", _currentPreset.value.name)
            putBoolean("dynamic_colors", _useDynamicColors.value)
            putFloat("night_intensity", _nightModeIntensity.value)
            apply()
        }
    }
}

/**
 * Utilidades para efeitos visuais de temas.
 */
object ThemeEffects {
    /**
     * Aplica filtro de modo noturno a uma cor.
     */
    fun applyNightModeFilter(color: Color, intensity: Float): Color {
        val r = (color.red * (1 - intensity * 0.3f)).coerceIn(0f, 1f)
        val g = (color.green * (1 - intensity * 0.3f)).coerceIn(0f, 1f)
        val b = (color.blue * (1 + intensity * 0.2f)).coerceIn(0f, 1f)
        return Color(r, g, b, color.alpha)
    }

    /**
     * Gera tom mais claro de uma cor.
     */
    fun lighten(color: Color, factor: Float = 0.2f): Color {
        return Color(
            red = (color.red + (1 - color.red) * factor).coerceIn(0f, 1f),
            green = (color.green + (1 - color.green) * factor).coerceIn(0f, 1f),
            blue = (color.blue + (1 - color.blue) * factor).coerceIn(0f, 1f),
            alpha = color.alpha
        )
    }

    /**
     * Gera tom mais escuro de uma cor.
     */
    fun darken(color: Color, factor: Float = 0.2f): Color {
        return Color(
            red = (color.red * (1 - factor)).coerceIn(0f, 1f),
            green = (color.green * (1 - factor)).coerceIn(0f, 1f),
            blue = (color.blue * (1 - factor)).coerceIn(0f, 1f),
            alpha = color.alpha
        )
    }

    /**
     * Calcula cor complementar (oposta na roda de cores).
     */
    fun complement(color: Color): Color {
        return Color(
            red = 1 - color.red,
            green = 1 - color.green,
            blue = 1 - color.blue,
            alpha = color.alpha
        )
    }

    /**
     * Cria gradiente de cores.
     */
    fun gradient(startColor: Color, endColor: Color, steps: Int = 10): List<Color> {
        return (0 until steps).map { step ->
            val t = step / (steps - 1).coerceAtLeast(1).toFloat()
            Color(
                red = startColor.red + (endColor.red - startColor.red) * t,
                green = startColor.green + (endColor.green - startColor.green) * t,
                blue = startColor.blue + (endColor.blue - startColor.blue) * t,
                alpha = startColor.alpha + (endColor.alpha - startColor.alpha) * t
            )
        }
    }
}

/**
 * Construtor para CustomColorConfig usando DSL.
 */
class ColorConfigBuilder {
    var primary: Color = Color(0xFF6200EE)
    var secondary: Color = Color(0xFF03DAC6)
    var tertiary: Color = Color(0xFF3700B3)
    var background: Color = Color(0xFFFFFFFF)
    var surface: Color = Color(0xFFFAFAFA)
    var error: Color = Color(0xFFB00020)
    var onPrimary: Color = Color(0xFFFFFFFF)
    var onSecondary: Color = Color(0xFF000000)
    var onTertiary: Color = Color(0xFFFFFFFF)
    var onBackground: Color = Color(0xFF000000)
    var onSurface: Color = Color(0xFF000000)
    var onError: Color = Color(0xFFFFFFFF)

    fun build(): CustomColorConfig {
        return CustomColorConfig(
            primary = primary,
            secondary = secondary,
            tertiary = tertiary,
            background = background,
            surface = surface,
            error = error,
            onPrimary = onPrimary,
            onSecondary = onSecondary,
            onTertiary = onTertiary,
            onBackground = onBackground,
            onSurface = onSurface,
            onError = onError
        )
    }
}

/**
 * DSL para criar CustomColorConfig.
 */
fun customColorConfig(block: ColorConfigBuilder.() -> Unit): CustomColorConfig {
    return ColorConfigBuilder().apply(block).build()
}

/**
 * Paleta de cores predefinidas.
 */
object ColorPalettes {
    val forest = customColorConfig {
        primary = Color(0xFF2D5016)
        secondary = Color(0xFF6B8E23)
        tertiary = Color(0xFF8FBC8F)
    }

    val ocean = customColorConfig {
        primary = Color(0xFF0077BE)
        secondary = Color(0xFF00D9FF)
        tertiary = Color(0xFF0099E5)
    }

    val sunset = customColorConfig {
        primary = Color(0xFFFF6B35)
        secondary = Color(0xFFFFB703)
        tertiary = Color(0xFFFB5607)
    }

    val midnight = customColorConfig {
        primary = Color(0xFF1A237E)
        secondary = Color(0xFF283593)
        tertiary = Color(0xFF3F51B5)
    }

    val lavender = customColorConfig {
        primary = Color(0xFF9D7BE0)
        secondary = Color(0xFFE0B0FF)
        tertiary = Color(0xFFC4B5FD)
    }

    val coral = customColorConfig {
        primary = Color(0xFFFF6B6B)
        secondary = Color(0xFFFFB3BA)
        tertiary = Color(0xFFFF8787)
    }
}
