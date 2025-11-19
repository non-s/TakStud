package com.example.takstud.ui.theme

import androidx.compose.ui.graphics.Color

/**
 * TakStud Brand Colors - Material Design 3 PROFISSIONAL 🎓
 * Paleta Branco + Azul Marinho (Clean, Profissional, Corporativo)
 */

// ===== CORES PRIMÁRIAS (Azul Marinho) =====
val NavyBlue = Color(0xFF001F3F)              // Azul Marinho (Primary) ✨
val DarkNavy = Color(0xFF000D1A)              // Azul Marinho Muito Escuro
val LightNavy = Color(0xFF1A4D7A)             // Azul Marinho Claro

// ===== CORES SECUNDÁRIAS (Branco) =====
val PureWhite = Color(0xFFFFFFFF)             // Branco Puro
val LightGray = Color(0xFFF5F5F5)             // Cinza Muito Claro
val MediumGray = Color(0xFFE0E0E0)            // Cinza Médio
val DarkGray = Color(0xFF757575)              // Cinza Escuro

// ===== CORES COMPLEMENTARES =====
val SkyBlue = Color(0xFF42A5F5)               // Sky Blue (Destaque)
val AccentBlue = Color(0xFF1976D2)            // Azul de Destaque
val Teal = Color(0xFF00897B)                  // Teal (Secundário)
val LightBlue = Color(0xFFB3E5FC)             // Azul Claro

// ===== CORES DE STATUS =====
val SuccessGreen = Color(0xFF4CAF50)          // Success Green
val ErrorRed = Color(0xFFD32F2F)              // Error Red
val WarningYellow = Color(0xFFFBC02D)         // Warning Yellow
val InfoBlue = Color(0xFF1976D2)              // Info Blue

// ===== COMPATIBILIDADE COM TEMA ANTIGO (DEPRECATED) =====
// Mantém compatibilidade com código existente
val PrimaryBrand = NavyBlue                   // Azul marinho
val PrimaryDark = DarkNavy                    // Azul marinho escuro
val PrimaryLight = LightNavy                  // Azul marinho claro
val SecondaryBrand = PureWhite                // Branco puro
val SecondaryDark = DarkGray                  // Cinza escuro
val SecondaryLight = LightGray                // Cinza claro
val TertiaryBrand = AccentBlue                // Azul de destaque

// === Material Design 3 Fallback ===
val Purple80 = Color(0xFFEBDEF8)
val PurpleGrey80 = Color(0xFFCCC7D0)
val Pink80 = Color(0xFFEFB8C8)

val Purple40 = Color(0xFF6650a4)
val PurpleGrey40 = Color(0xFF625b71)
val Pink40 = Color(0xFF7D5260)

// === SEMANTIC COLORS - Light Mode (Profissional) ===
val BackgroundLight = PureWhite              // Fundo branco puro
val SurfaceLight = LightGray                 // Superfícies cinza claro
val OutlineLight = NavyBlue                  // Bordas azul marinho
val OutlineVariantLight = AccentBlue         // Bordas azul de destaque

// === SEMANTIC COLORS - Dark Mode (Profissional) ===
val BackgroundDark = PureWhite               // Fundo branco puro
val SurfaceDark = LightGray                  // Superfícies cinza claro
val OutlineDark = NavyBlue                   // Bordas azul marinho
val OutlineVariantDark = AccentBlue          // Bordas azul de destaque

// === STATUS COLORS =====
val ErrorColor = ErrorRed                    // Erro/Aviso (Red)
val SuccessColor = SuccessGreen              // Sucesso (Success Green)
val WarningColor = WarningYellow             // Aviso (Warning Yellow)
val InfoColor = InfoBlue                     // Informação (Info Blue)

// === ATTENDANCE COLORS ===
val AttendancePresent = SuccessGreen         // Presença (Verde)
val AttendanceAbsent = ErrorRed              // Falta (Vermelho)
val AttendanceLate = WarningYellow           // Atraso (Amarelo)

// === GRADE COLORS ===
val GradeA = SuccessGreen                    // A - Verde
val GradeB = AccentBlue                      // B - Azul
val GradeC = WarningYellow                   // C - Amarelo
val GradeD = ErrorRed                        // D - Vermelho
val GradeF = ErrorRed                        // F - Vermelho

// === GRAY SCALE (Temas Escuros) ===
val Gray100 = Color(0xFFFAFAFA)
val Gray200 = Color(0xFFF5F5F5)
val Gray300 = Color(0xFFEEEEEE)
val Gray400 = Color(0xFFBDBDBD)
val Gray500 = Color(0xFF9E9E9E)
val Gray600 = Color(0xFF757575)
val Gray700 = Color(0xFF616161)
val Gray800 = Color(0xFF424242)
val Gray900 = Color(0xFF212121)

// === GRADIENTES PROFISSIONAIS ===
val GradientNavyWhite = listOf(NavyBlue, PureWhite)
val GradientNavyAccent = listOf(NavyBlue, AccentBlue)
val GradientProfessional = listOf(PureWhite, LightGray)

// ===== CORES FUTURISTA NEON (Modo escuro) =====
val VeryDarkBg = Color(0xFF0A0A0A)      // Fundo super escuro (quase preto)
val DarkGreen = Color(0xFF003300)      // Verde escuro para base
val TechCyan = Color(0xFF00FFFF)       // Ciano brilhante para tecnologia
val VividGreen = Color(0xFF00FF00)     // Verde vivo e saturado
val FreshGreen = Color(0xFF32CD32)     // Verde fresco (Lime Green)
val MintyGreen = Color(0xFF98FF98)     // Verde menta claro
