package com.example.takstud.ui

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.School
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.takstud.ui.theme.*
import kotlinx.coroutines.delay

/**
 * HomeScreen Premium 2.0 - Tela inicial do TakStud
 * Design ultra-moderno com gradientes vibrantes, animações fluidas e visual energético
 * Experiência visual premium que encanta o usuário
 */
@Composable
fun HomeScreen(
    onProfessorClick: () -> Unit,
    onAlunoClick: () -> Unit
) {
    // Estado de animação de entrada
    var isVisible by remember { mutableStateOf(false) }
    
    LaunchedEffect(Unit) {
        delay(100)
        isVisible = true
    }

    // Animação de pulsação para o logo
    val infiniteTransition = rememberInfiniteTransition(label = "logo_pulse")
    val logoScale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.08f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "logo_scale"
    )
    
    // Animação de rotação suave para o gradiente de fundo
    val gradientRotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(20000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "gradient_rotation"
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        GradientStart,
                        GradientMiddle,
                        GradientEnd
                    )
                )
            ),
        contentAlignment = Alignment.Center
    ) {
        // Efeitos de fundo decorativos
        BackgroundDecorations()

        // Conteúdo principal
        AnimatedVisibility(
            visible = isVisible,
            enter = fadeIn(animationSpec = tween(800)) + 
                    slideInVertically(
                        initialOffsetY = { it / 3 },
                        animationSpec = tween(800, easing = FastOutSlowInEasing)
                    )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                // Logo com animação e efeito de brilho
                Box(
                    modifier = Modifier.size(120.dp),
                    contentAlignment = Alignment.Center
                ) {
                    // Círculo de fundo com gradiente
                    Surface(
                        modifier = Modifier
                            .size(100.dp)
                            .scale(logoScale)
                            .blur(20.dp),
                        shape = CircleShape,
                        color = ElectricBlue.copy(alpha = 0.6f)
                    ) {}
                    
                    Surface(
                        modifier = Modifier
                            .size(90.dp)
                            .scale(logoScale),
                        shape = CircleShape,
                        color = Color.White,
                        shadowElevation = 16.dp
                    ) {
                        Box(
                            modifier = Modifier
                                .background(
                                    brush = Brush.radialGradient(
                                        colors = listOf(PrimaryBlue, VibrantPurple)
                                    )
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Filled.School,
                                contentDescription = "TakStud Logo",
                                modifier = Modifier.size(50.dp),
                                tint = Color.White
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Título Principal com gradiente
                Text(
                    text = "TakStud",
                    style = MaterialTheme.typography.displayLarge.copy(
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 64.sp,
                        brush = Brush.linearGradient(
                            colors = listOf(Color.White, Color.White.copy(alpha = 0.9f))
                        )
                    ),
                    color = Color.White,
                    textAlign = TextAlign.Center
                )

                // Subtítulo
                Text(
                    text = "Gestão Escolar Inteligente",
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.Medium
                    ),
                    color = Color.White.copy(alpha = 0.95f),
                    modifier = Modifier.padding(top = 8.dp),
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(56.dp))

                // Card Glassmorphism de Seleção de Perfil
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp),
                    shape = RoundedCornerShape(32.dp),
                    color = GlassMorphLight.copy(alpha = 0.25f),
                    shadowElevation = 24.dp,
                    border = BorderStroke(1.dp, Color.White.copy(alpha = 0.3f))
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(
                                brush = Brush.verticalGradient(
                                    colors = listOf(
                                        Color.White.copy(alpha = 0.15f),
                                        Color.White.copy(alpha = 0.05f)
                                    )
                                )
                            )
                            .padding(28.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "Selecione seu perfil",
                            style = MaterialTheme.typography.headlineSmall.copy(
                                fontWeight = FontWeight.Bold
                            ),
                            color = Color.White,
                            textAlign = TextAlign.Center
                        )

                        Spacer(modifier = Modifier.height(28.dp))

                        // Botão Professor - Gradiente vibrante
                        Button(
                            onClick = onProfessorClick,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(70.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color.Transparent
                            ),
                            shape = RoundedCornerShape(20.dp),
                            elevation = ButtonDefaults.buttonElevation(
                                defaultElevation = 8.dp,
                                pressedElevation = 12.dp
                            ),
                            contentPadding = PaddingValues(0.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .background(
                                        brush = Brush.horizontalGradient(
                                            colors = listOf(PrimaryBlue, ElectricBlue)
                                        )
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.Center
                                ) {
                                    Text(
                                        text = "👨‍🏫",
                                        fontSize = 28.sp,
                                        modifier = Modifier.padding(end = 12.dp)
                                    )
                                    Text(
                                        text = "Sou Professor",
                                        fontSize = 20.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(20.dp))

                        // Divisor estilizado
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            HorizontalDivider(
                                modifier = Modifier.weight(1f),
                                thickness = 1.dp,
                                color = Color.White.copy(alpha = 0.3f)
                            )
                            Text(
                                text = "ou",
                                modifier = Modifier.padding(horizontal = 16.dp),
                                style = MaterialTheme.typography.bodyMedium,
                                color = Color.White.copy(alpha = 0.8f),
                                fontWeight = FontWeight.Medium
                            )
                            HorizontalDivider(
                                modifier = Modifier.weight(1f),
                                thickness = 1.dp,
                                color = Color.White.copy(alpha = 0.3f)
                            )
                        }

                        Spacer(modifier = Modifier.height(20.dp))

                        // Botão Aluno/Responsável - Outline com gradiente
                        OutlinedButton(
                            onClick = onAlunoClick,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(70.dp),
                            border = BorderStroke(
                                width = 2.5.dp,
                                brush = Brush.horizontalGradient(
                                    colors = listOf(AccentTeal, AccentPink)
                                )
                            ),
                            shape = RoundedCornerShape(20.dp),
                            colors = ButtonDefaults.outlinedButtonColors(
                                containerColor = Color.White.copy(alpha = 0.15f)
                            )
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.Center
                            ) {
                                Text(
                                    text = "👨‍👩‍👧",
                                    fontSize = 28.sp,
                                    modifier = Modifier.padding(end = 12.dp)
                                )
                                Text(
                                    text = "Sou Aluno/Responsável",
                                    fontSize = 20.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.weight(1f))

                // Rodapé com efeito de brilho
                Text(
                    text = "TakStud 2.0 Premium Edition",
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontWeight = FontWeight.Medium
                    ),
                    color = Color.White.copy(alpha = 0.7f),
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}

/**
 * Decorações de fundo animadas para criar profundidade visual
 */
@Composable
fun BoxScope.BackgroundDecorations() {
    // Círculos decorativos com blur
    Box(
        modifier = Modifier
            .size(300.dp)
            .align(Alignment.TopStart)
            .offset(x = (-100).dp, y = (-100).dp)
            .blur(80.dp)
            .background(
                color = AccentPink.copy(alpha = 0.3f),
                shape = CircleShape
            )
    )
    
    Box(
        modifier = Modifier
            .size(250.dp)
            .align(Alignment.BottomEnd)
            .offset(x = 100.dp, y = 100.dp)
            .blur(80.dp)
            .background(
                color = AccentTeal.copy(alpha = 0.3f),
                shape = CircleShape
            )
    )
    
    Box(
        modifier = Modifier
            .size(200.dp)
            .align(Alignment.CenterStart)
            .offset(x = (-50).dp)
            .blur(60.dp)
            .background(
                color = ElectricBlue.copy(alpha = 0.2f),
                shape = CircleShape
            )
    )
}