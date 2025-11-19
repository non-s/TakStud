@file:OptIn(ExperimentalMaterial3Api::class)

package com.example.takstud.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.takstud.ui.theme.AccentBlue
import com.example.takstud.ui.theme.DarkGray
import com.example.takstud.ui.theme.ErrorRed
import com.example.takstud.ui.theme.LightGray
import com.example.takstud.ui.theme.NavyBlue
import com.example.takstud.ui.theme.PureWhite
import com.example.takstud.ui.theme.SuccessGreen
import com.example.takstud.ui.theme.WarningYellow

/**
 * 📊 Componentes modernos para Dashboard
 * Inclui cards expandíveis, estatísticas, e navegação melhorada
 */

/**
 * 📈 StatisticCard - Card de estatística com ícone e valor
 * @param title Título da estatística
 * @param value Valor a ser exibido
 * @param unit Unidade da medida (ex: %, hrs)
 * @param icon Ícone em emoji
 * @param backgroundColor Cor de fundo do card
 * @param modifier Modifier para customizar o card
 * @param onClick Callback quando clicado
 */
@Composable
fun StatisticCard(
    title: String,
    value: String,
    unit: String = "",
    icon: String = "📊",
    backgroundColor: Color = AccentBlue.copy(alpha = 0.1f),
    modifier: Modifier = Modifier,
    onClick: () -> Unit = {}
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onClick
            )
            .shadow(4.dp, shape = RoundedCornerShape(12.dp)),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = backgroundColor),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    color = DarkGray,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(4.dp))
                Row(verticalAlignment = Alignment.
                CenterVertically) {
                    Text(
                        text = value,
                        color = NavyBlue,
                        fontSize = 28.sp,
                        fontWeight = FontWeight.Bold
                    )
                    if (unit.isNotEmpty()) {
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = unit,
                            color = DarkGray,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .background(
                        color = Color.White,
                        shape = RoundedCornerShape(12.dp)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(text = icon, fontSize = 28.sp)
            }
        }
    }
}

/**
 * 🎯 ActionCard - Card de ação com título, descrição e ícone
 * @param title Título da ação
 * @param description Descrição da ação
 * @param icon Ícone em emoji
 * @param badge Opcionalmente mostrar um badge (ex: "Nova")
 * @param onClick Callback quando clicado
 */
@Composable
fun ActionCard(
    title: String,
    description: String,
    icon: String = "➡️",
    badge: String? = null,
    onClick: () -> Unit
) {
    var isPressed by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = { onClick() }
            )
            .shadow(
                elevation = if (isPressed) 8.dp else 4.dp,
                shape = RoundedCornerShape(12.dp)
            )
            .graphicsLayer {
                scaleX = if (isPressed) 0.98f else 1f
                scaleY = if (isPressed) 0.98f else 1f
            },
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = PureWhite),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Box(
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Ícone principal
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .background(
                            color = AccentBlue.copy(alpha = 0.1f),
                            shape = RoundedCornerShape(8.dp)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text(text = icon, fontSize = 24.sp)
                }

                // Conteúdo
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        text = title,
                        color = NavyBlue,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = description,
                        color = DarkGray,
                        fontSize = 12.sp,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                // Arrow
                Icon(
                    imageVector = Icons.Default.ArrowDropDown,
                    contentDescription = "Abrir",
                    tint = AccentBlue,
                    modifier = Modifier.size(24.dp)
                )
            }

            // Badge
            if (badge != null) {
                Surface(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(8.dp)
                        .clip(RoundedCornerShape(8.dp)),
                    color = ErrorRed
                ) {
                    Text(
                        text = badge,
                        color = PureWhite,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }
        }
    }
}

/**
 * 📋 ExpandableCard - Card que pode ser expandido para mostrar mais conteúdo
 * @param title Título do card
 * @param icon Ícone do card
 * @param initiallyExpanded Se deve começar expandido
 * @param onClick Callback quando título é clicado
 * @param content Conteúdo expandível
 */
@Composable
fun ExpandableCard(
    title: String,
    icon: String = "📌",
    initiallyExpanded: Boolean = false,
    onClick: () -> Unit = {},
    content: @Composable ColumnScope.() -> Unit
) {
    var isExpanded by remember { mutableStateOf(initiallyExpanded) }
    val backgroundColor by animateColorAsState(
        targetValue = if (isExpanded) AccentBlue.copy(alpha = 0.05f) else PureWhite,
        label = "cardBackgroundColor"
    )

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(4.dp, shape = RoundedCornerShape(12.dp)),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = backgroundColor),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            // Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable {
                        isExpanded = !isExpanded
                        onClick()
                    }
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(text = icon, fontSize = 20.sp)
                Text(
                    text = title,
                    color = NavyBlue,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    modifier = Modifier.weight(1f)
                )
                Icon(
                    imageVector = Icons.Default.ArrowDropDown,
                    contentDescription = if (isExpanded) "Fechar" else "Expandir",
                    tint = AccentBlue,
                    modifier = Modifier
                        .size(24.dp)
                        .graphicsLayer {
                            rotationZ = if (isExpanded) 180f else 0f
                        }
                )
            }

            // Conteúdo expandível
            AnimatedVisibility(
                visible = isExpanded,
                enter = expandVertically() + fadeIn(),
                exit = shrinkVertically() + fadeOut()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                        .padding(bottom = 16.dp),
                    content = content
                )
            }
        }
    }
}

/**
 * 🎨 GradeIndicator - Indicador visual de nota/grade
 * @param grade Nota (0-100)
 * @param label Rótulo (ex: "Média")
 */
@Composable
fun GradeIndicator(
    grade: Float,
    label: String = "Nota"
) {
    val color = when {
        grade >= 90 -> SuccessGreen
        grade >= 80 -> AccentBlue
        grade >= 70 -> WarningYellow
        else -> ErrorRed
    }

    val percentage = (grade / 100f).coerceIn(0f, 1f)

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.padding(8.dp)
    ) {
        Text(
            text = label,
            color = DarkGray,
            fontSize = 12.sp
        )
        Spacer(modifier = Modifier.height(8.dp))

        // Circular progress indicator
        Box(
            modifier = Modifier
                .size(60.dp)
                .background(color = LightGray, shape = RoundedCornerShape(50)),
            contentAlignment = Alignment.Center
        ) {
            Box(
                modifier = Modifier
                    .size(52.dp)
                    .background(color = color.copy(alpha = percentage), shape = RoundedCornerShape(50))
            )
            Text(
                text = String.format("%.0f", grade),
                color = NavyBlue,
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp
            )
        }
    }
}

/**
 * 📊 ProgressBarCard - Card com barra de progresso
 * @param title Título
 * @param progress Progresso (0-1)
 * @param label Rótulo do progresso
 * @param icon Ícone
 */
@Composable
fun ProgressBarCard(
    title: String,
    progress: Float,
    label: String = "",
    icon: String = "📈"
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(4.dp, shape = RoundedCornerShape(12.dp)),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = PureWhite),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(text = icon, fontSize = 20.sp)
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = title,
                        color = NavyBlue,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )
                    if (label.isNotEmpty()) {
                        Text(
                            text = label,
                            color = DarkGray,
                            fontSize = 11.sp
                        )
                    }
                }
                Text(
                    text = "${(progress * 100).toInt()}%",
                    color = AccentBlue,
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp
                )
            }

            // Barra de progresso
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp)
                    .background(color = LightGray, shape = RoundedCornerShape(4.dp))
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth(progress)
                        .height(8.dp)
                        .background(color = AccentBlue, shape = RoundedCornerShape(4.dp))
                )
            }
        }
    }
}

/**
 * 🔔 NotificationBadge - Badge para mostrar contagem
 * @param count Número a exibir
 * @param color Cor da badge
 */
@Composable
fun NotificationBadge(
    count: Int,
    color: Color = ErrorRed
) {
    if (count > 0) {
        Surface(
            modifier = Modifier
                .size(24.dp)
                .clip(RoundedCornerShape(12.dp)),
            color = color
        ) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = if (count > 99) "99+" else count.toString(),
                    color = PureWhite,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

/**
 * 📌 SectionCard - Card para agrupar seções
 * @param title Título da seção
 * @param icon Ícone
 * @param content Conteúdo da seção
 */
@Composable
fun SectionCard(
    title: String,
    icon: String = "📌",
    content: @Composable () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(2.dp, shape = RoundedCornerShape(8.dp)),
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(containerColor = PureWhite),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            // Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(text = icon, fontSize = 18.sp)
                Text(
                    text = title,
                    color = NavyBlue,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 13.sp
                )
            }

            // Divider
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(1.dp)
                    .background(color = LightGray)
            )

            // Content
            Box(modifier = Modifier.padding(12.dp)) {
                content()
            }
        }
    }
}