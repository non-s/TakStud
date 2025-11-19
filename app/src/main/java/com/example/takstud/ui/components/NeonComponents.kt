package com.example.takstud.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.takstud.ui.theme.DarkGreen
import com.example.takstud.ui.theme.FreshGreen
import com.example.takstud.ui.theme.MintyGreen
import com.example.takstud.ui.theme.TechCyan
import com.example.takstud.ui.theme.VeryDarkBg
import com.example.takstud.ui.theme.VividGreen

/**
 * 🌟 NeonButton - Botão com efeito Neon futurista
 * Características: Gradiente, sombra com brilho neon, animação ao pressionar
 */
@Composable
fun NeonButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    colors: List<Color> = listOf(VividGreen, TechCyan),
    enabled: Boolean = true,
    emoji: String = ""
) {
    var isPressed by remember { mutableStateOf(false) }

    val shadowColor by animateColorAsState(
        targetValue = if (isPressed) colors[0].copy(alpha = 0.8f) else colors[0].copy(alpha = 0.4f),
        label = "Shadow Color"
    )

    Button(
        onClick = onClick,
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .shadow(
                elevation = if (isPressed) 20.dp else 12.dp,
                shape = RoundedCornerShape(12.dp),
                spotColor = shadowColor
            )
            .border(
                width = 2.dp,
                brush = Brush.horizontalGradient(colors),
                shape = RoundedCornerShape(12.dp)
            ),
        colors = ButtonDefaults.buttonColors(
            containerColor = Color.Transparent,
            disabledContainerColor = Color.Transparent
        ),
        shape = RoundedCornerShape(12.dp),
        enabled = enabled,
        interactionSource = remember { MutableInteractionSource() }
            .also {
                // Simulação de pressão
                it.interactions
            }
    ) {
        Text(
            text = if (emoji.isNotEmpty()) "$emoji $text" else text,
            color = Color.White,
            fontWeight = FontWeight.Bold,
            fontSize = 16.sp,
            modifier = Modifier.padding(8.dp)
        )
    }
}

/**
 * 🎨 NeonCard - Card com borda Neon e efeito de brilho
 * Características: Borda neon animada, sombra neon, fundo escuro
 */
@Composable
fun NeonCard(
    modifier: Modifier = Modifier,
    neonColor: Color = VividGreen,
    content: @Composable () -> Unit
) {
    Card(
        modifier = modifier
            .border(
                width = 2.dp,
                color = neonColor,
                shape = RoundedCornerShape(12.dp)
            )
            .shadow(
                elevation = 20.dp,
                shape = RoundedCornerShape(12.dp),
                spotColor = neonColor.copy(alpha = 0.4f)
            ),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = DarkGreen.copy(alpha = 0.8f)
        )
    ) {
        content()
    }
}

/**
 * ✨ GradientCard - Card com gradient background
 * Características: Background em gradiente, borda neon
 */
@Composable
fun GradientCard(
    modifier: Modifier = Modifier,
    gradientColors: List<Color> = listOf(VividGreen, TechCyan),
    neonBorderColor: Color = TechCyan,
    content: @Composable () -> Unit
) {
    Box(
        modifier = modifier
            .background(
                brush = Brush.linearGradient(
                    colors = listOf(
                        gradientColors[0].copy(alpha = 0.1f),
                        gradientColors.getOrNull(1)?.copy(alpha = 0.1f) ?: Color.Transparent
                    )
                ),
                shape = RoundedCornerShape(12.dp)
            )
            .border(
                width = 2.dp,
                color = neonBorderColor,
                shape = RoundedCornerShape(12.dp)
            )
            .shadow(
                elevation = 16.dp,
                shape = RoundedCornerShape(12.dp),
                spotColor = neonBorderColor.copy(alpha = 0.3f)
            )
            .clip(RoundedCornerShape(12.dp))
            .padding(1.dp)
    ) {
        content()
    }
}

/**
 * 🎯 StatusBadge - Badge com cores de status neon
 * Características: Fundo com cor de status, brilho neon
 */
@Composable
fun StatusBadge(
    text: String,
    backgroundColor: Color = VividGreen,
    textColor: Color = Color.White,
    modifier: Modifier = Modifier,
    emoji: String = ""
) {
    Box(
        modifier = modifier
            .background(
                color = backgroundColor.copy(alpha = 0.2f),
                shape = RoundedCornerShape(20.dp)
            )
            .border(
                width = 1.5.dp,
                color = backgroundColor,
                shape = RoundedCornerShape(20.dp)
            )
            .shadow(
                elevation = 8.dp,
                shape = RoundedCornerShape(20.dp),
                spotColor = backgroundColor.copy(alpha = 0.3f)
            )
            .padding(horizontal = 12.dp, vertical = 6.dp)
    ) {
        Text(
            text = if (emoji.isNotEmpty()) "$emoji $text" else text,
            color = textColor,
            fontSize = 12.sp,
            fontWeight = FontWeight.SemiBold
        )
    }
}

/**
 * 📊 NeonInfoCard - Card para exibir informações com efeito neon
 * Características: Título, valor, ícone, cor neon
 */
@Composable
fun NeonInfoCard(
    title: String,
    value: String,
    icon: String = "",
    neonColor: Color = TechCyan,
    modifier: Modifier = Modifier,
    content: @Composable (() -> Unit)? = null
) {
    NeonCard(
        modifier = modifier,
        neonColor = neonColor
    ) {
        Box(
            modifier = Modifier.padding(16.dp)
        ) {
            if (content != null) {
                content()
            } else {
                androidx.compose.foundation.layout.Column {
                    if (icon.isNotEmpty()) {
                        Text(
                            text = icon,
                            fontSize = 32.sp,
                            modifier = Modifier.padding(bottom = 8.dp)
                        )
                    }

                    Text(
                        text = title,
                        color = neonColor,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Light,
                        modifier = Modifier.padding(bottom = 4.dp)
                    )

                    Text(
                        text = value,
                        color = Color.White,
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}
