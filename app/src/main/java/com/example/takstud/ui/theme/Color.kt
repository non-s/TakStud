package com.example.takstud.ui.theme

import androidx.compose.ui.graphics.Color

/**
 * TakStud Premium Colors 2.0
 * Paleta vibrante, moderna e energética com cores vivas e gradientes dinâmicos
 * Design inspirado em interfaces premium e aplicativos modernos
 */

// ========== PRIMARY COLORS - Azuis Elétricos ==========
val PrimaryBlue = Color(0xFF0066FF)        // Azul elétrico vibrante
val PrimaryDark = Color(0xFF0047AB)        // Azul cobalto profundo
val PrimaryLight = Color(0xFF4D94FF)       // Azul céu brilhante
val ElectricBlue = Color(0xFF00D4FF)       // Azul neon elétrico

// ========== SECONDARY COLORS - Roxos Vibrantes ==========
val VibrantPurple = Color(0xFF9D4EDD)      // Roxo vibrante principal
val DeepPurple = Color(0xFF7209B7)         // Roxo profundo intenso
val LightPurple = Color(0xFFC77DFF)        // Roxo claro luminoso
val NeonPurple = Color(0xFFBF40BF)         // Roxo neon brilhante

// ========== ACCENT COLORS - Cores Energéticas ==========
val AccentTeal = Color(0xFF00D9C0)         // Teal neon vibrante
val AccentCyan = Color(0xFF00F5FF)         // Ciano elétrico
val AccentPink = Color(0xFFFF006E)         // Rosa vibrante
val AccentOrange = Color(0xFFFF6B35)       // Laranja energético
val AccentYellow = Color(0xFFFFD60A)       // Amarelo vibrante
val AccentGreen = Color(0xFF06FFA5)        // Verde neon

// ========== GRADIENT COLORS - Para efeitos visuais ==========
val GradientStart = Color(0xFF667EEA)      // Início gradiente (Azul-Roxo)
val GradientMiddle = Color(0xFF764BA2)     // Meio gradiente (Roxo)
val GradientEnd = Color(0xFFF093FB)        // Fim gradiente (Rosa-Roxo)

val GradientOcean = Color(0xFF2E3192)      // Oceano profundo
val GradientSky = Color(0xFF1BFFFF)        // Céu brilhante

// ========== NEUTRAL COLORS - Tons Modernos ==========
val Neutral50 = Color(0xFFFAFAFC)          // Branco levemente azulado
val Neutral100 = Color(0xFFF4F5F9)         // Cinza muito claro
val Neutral200 = Color(0xFFE5E7EB)         // Cinza claro
val Neutral300 = Color(0xFFD1D5DB)         // Cinza médio-claro
val Neutral400 = Color(0xFF9CA3AF)         // Cinza médio
val Neutral500 = Color(0xFF6B7280)         // Cinza
val Neutral600 = Color(0xFF4B5563)         // Cinza escuro
val Neutral700 = Color(0xFF374151)         // Cinza muito escuro
val Neutral800 = Color(0xFF1F2937)         // Quase preto
val Neutral900 = Color(0xFF111827)         // Preto suave

// ========== DARK MODE COLORS ==========
val DarkBackground = Color(0xFF0A0E27)     // Azul escuro profundo
val DarkSurface = Color(0xFF151B3D)        // Superfície azul escuro
val DarkSurfaceVariant = Color(0xFF1E2749) // Variante mais clara
val DarkElevated = Color(0xFF252D52)       // Elementos elevados

// ========== SEMANTIC COLORS - Vibrantes ==========
val Success = Color(0xFF10B981)            // Verde sucesso moderno
val SuccessBright = Color(0xFF34D399)      // Verde sucesso brilhante
val Error = Color(0xFFEF4444)              // Vermelho erro
val ErrorBright = Color(0xFFF87171)        // Vermelho brilhante
val Warning = Color(0xFFFBBF24)            // Amarelo alerta
val WarningBright = Color(0xFFFCD34D)      // Amarelo brilhante
val Info = Color(0xFF3B82F6)               // Azul informação
val InfoBright = Color(0xFF60A5FA)         // Azul brilhante

// ========== SPECIAL EFFECTS ==========
val GlassMorphLight = Color(0xCCFFFFFF)    // Branco translúcido para glassmorphism
val GlassMorphDark = Color(0x99000000)     // Preto translúcido
val Overlay = Color(0x80000000)            // Overlay escuro
val OverlayLight = Color(0x40000000)       // Overlay claro

// ========== GRADIENTS - Listas de cores ==========
val GradientPrimary = listOf(PrimaryBlue, VibrantPurple)
val GradientSecondary = listOf(AccentTeal, ElectricBlue)
val GradientSunset = listOf(AccentOrange, AccentPink, VibrantPurple)
val GradientOceanWave = listOf(GradientOcean, AccentTeal, GradientSky)
val GradientNeon = listOf(AccentCyan, AccentPink, AccentYellow)
val GradientDark = listOf(DarkBackground, DarkSurface, DarkElevated)
val GradientPurple = listOf(DeepPurple, VibrantPurple, LightPurple)

// ========== LEGACY COMPATIBILITY ==========
val NavyBlue = PrimaryDark
val PureWhite = Color.White
val LightGray = Neutral100
val MediumGray = Neutral200
val DarkGray = Neutral700
val SkyBlue = PrimaryLight
val AccentBlue = PrimaryBlue
val SuccessGreen = Success
val ErrorRed = Error
val WarningYellow = Warning
val AccentPurple = VibrantPurple

// ========== NEON COLORS - Para componentes tech ==========
val VeryDarkBg = Color(0xFF0A0A0A)
val DarkGreen = Color(0xFF003300)
val TechCyan = Color(0xFF00FFFF)
val VividGreen = Color(0xFF00FF00)
val FreshGreen = Color(0xFF32CD32)
val MintyGreen = Color(0xFF98FF98)
