@file:OptIn(ExperimentalMaterial3Api::class)

package com.example.takstud.ui.components.schedule

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.takstud.model.schedule.*
import com.example.takstud.ui.theme.*

/**
 * 📅 Card de TimeSlot Individual
 */
@Composable
fun TimeSlotCard(
    timeSlot: TimeSlot,
    modifier: Modifier = Modifier,
    onClick: () -> Unit = {},
    onEdit: () -> Unit = {},
    onDelete: () -> Unit = {},
    showActions: Boolean = true
) {
    val color = if (timeSlot.isBreak) {
        WarningYellow
    } else if (timeSlot.isSpecialEvent) {
        Color(0xFFFF6B6B)
    } else {
        Color(timeSlot.subjectColor.toULong())
    }

    var expanded by remember { mutableStateOf(false) }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable { if (showActions) expanded = !expanded else onClick() },
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = color.copy(alpha = 0.15f)
        ),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Column(
            modifier = Modifier.padding(12.dp)
        ) {
            // Cabeçalho com horário
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Indicador de cor
                    Box(
                        modifier = Modifier
                            .size(12.dp)
                            .clip(CircleShape)
                            .background(color)
                    )

                    // Horário
                    Text(
                        text = timeSlot.getTimeRange(),
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Bold,
                        color = NavyBlue
                    )
                }

                // Ícone do tipo
                Icon(
                    imageVector = when {
                        timeSlot.isBreak -> Icons.Default.Coffee
                        timeSlot.isSpecialEvent -> Icons.Default.Event
                        timeSlot.isSubstitute -> Icons.Default.SwapHoriz
                        else -> Icons.Default.MenuBook
                    },
                    contentDescription = null,
                    tint = color,
                    modifier = Modifier.size(20.dp)
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Título principal
            Text(
                text = when {
                    timeSlot.isBreak -> "🍽️ Intervalo"
                    timeSlot.isSpecialEvent -> timeSlot.eventTitle
                    else -> timeSlot.subjectName
                },
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = NavyBlue
            )

            if (!timeSlot.isBreak && !timeSlot.isSpecialEvent) {
                // Professor
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Person,
                        contentDescription = null,
                        tint = DarkGray,
                        modifier = Modifier.size(14.dp)
                    )
                    Text(
                        text = if (timeSlot.isSubstitute) {
                            "${timeSlot.substituteTeacher} (Substituto)"
                        } else {
                            timeSlot.teacherName
                        },
                        style = MaterialTheme.typography.bodySmall,
                        color = DarkGray
                    )
                }

                // Sala
                if (timeSlot.classroom.isNotBlank()) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Room,
                            contentDescription = null,
                            tint = DarkGray,
                            modifier = Modifier.size(14.dp)
                        )
                        Text(
                            text = timeSlot.classroom,
                            style = MaterialTheme.typography.bodySmall,
                            color = DarkGray
                        )
                    }
                }
            }

            // Detalhes expandidos
            AnimatedVisibility(visible = expanded) {
                Column(
                    modifier = Modifier.padding(top = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Divider()

                    // Notas
                    if (timeSlot.notes.isNotBlank()) {
                        Text(
                            text = timeSlot.notes,
                            style = MaterialTheme.typography.bodySmall,
                            color = DarkGray,
                            fontStyle = androidx.compose.ui.text.font.FontStyle.Italic
                        )
                    }

                    // Ações
                    if (showActions) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Button(
                                onClick = onEdit,
                                modifier = Modifier.weight(1f),
                                colors = ButtonDefaults.buttonColors(containerColor = AccentBlue)
                            ) {
                                Icon(Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Editar", fontSize = 12.sp)
                            }

                            Button(
                                onClick = onDelete,
                                modifier = Modifier.weight(1f),
                                colors = ButtonDefaults.buttonColors(containerColor = ErrorRed)
                            ) {
                                Icon(Icons.Default.Delete, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Remover", fontSize = 12.sp)
                            }
                        }
                    }
                }
            }
        }
    }
}

/**
 * 📊 Card de Disciplina
 */
@Composable
fun SubjectCard(
    subject: Subject,
    modifier: Modifier = Modifier,
    onClick: () -> Unit = {},
    onEdit: () -> Unit = {},
    onDelete: () -> Unit = {}
) {
    var showActions by remember { mutableStateOf(false) }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable { showActions = !showActions },
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Cor da disciplina
            Box(
                modifier = Modifier
                    .size(50.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(subject.getComposeColor()),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = subject.shortName,
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }

            // Informações
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = subject.name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = NavyBlue
                )

                if (subject.teacherName.isNotBlank()) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Person,
                            contentDescription = null,
                            tint = DarkGray,
                            modifier = Modifier.size(14.dp)
                        )
                        Text(
                            text = subject.teacherName,
                            style = MaterialTheme.typography.bodySmall,
                            color = DarkGray
                        )
                    }
                }

                if (subject.weeklyHours > 0) {
                    Text(
                        text = "${subject.weeklyHours}h/semana",
                        style = MaterialTheme.typography.bodySmall,
                        color = AccentBlue,
                        fontWeight = FontWeight.Medium
                    )
                }
            }

            // Status
            if (!subject.isActive) {
                Chip(
                    label = "Inativa",
                    color = ErrorRed
                )
            }
        }

        AnimatedVisibility(visible = showActions) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedButton(
                    onClick = onEdit,
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Editar")
                }

                OutlinedButton(
                    onClick = onDelete,
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = ErrorRed)
                ) {
                    Icon(Icons.Default.Delete, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Remover")
                }
            }
        }
    }
}

/**
 * 🎨 Chip simples
 */
@Composable
fun Chip(
    label: String,
    color: Color,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        color = color.copy(alpha = 0.2f)
    ) {
        Text(
            text = label,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
            style = MaterialTheme.typography.labelSmall,
            color = color,
            fontWeight = FontWeight.Bold
        )
    }
}

/**
 * ⚠️ Card de Conflito
 */
@Composable
fun ConflictCard(
    conflict: ScheduleConflict,
    modifier: Modifier = Modifier,
    onResolve: () -> Unit = {}
) {
    val color = when (conflict.severity) {
        ConflictSeverity.CRITICAL -> ErrorRed
        ConflictSeverity.WARNING -> WarningYellow
        ConflictSeverity.INFO -> AccentBlue
    }

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = color.copy(alpha = 0.1f)
        ),
        border = androidx.compose.foundation.BorderStroke(1.dp, color)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Warning,
                    contentDescription = null,
                    tint = color
                )
                Text(
                    text = conflict.type.displayName,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = color
                )
                Chip(
                    label = conflict.severity.displayName,
                    color = color
                )
            }

            Text(
                text = conflict.message,
                style = MaterialTheme.typography.bodyMedium,
                color = DarkGray
            )

            if (conflict.suggestions.isNotEmpty()) {
                Text(
                    text = "Sugestões:",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = NavyBlue
                )

                conflict.suggestions.forEach { suggestion ->
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.Top
                    ) {
                        Text("•", color = AccentBlue)
                        Text(
                            text = suggestion,
                            style = MaterialTheme.typography.bodySmall,
                            color = DarkGray
                        )
                    }
                }
            }
        }
    }
}

/**
 * 📊 Estatísticas da Grade
 */
@Composable
fun ScheduleStatsCard(
    stats: ScheduleStats,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = LightGray.copy(alpha = 0.3f))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            StatItem(
                icon = Icons.Default.Class,
                value = stats.totalSlots.toString(),
                label = "Aulas",
                color = AccentBlue
            )

            StatItem(
                icon = Icons.Default.MenuBook,
                value = stats.uniqueSubjects.toString(),
                label = "Disciplinas",
                color = SuccessGreen
            )

            StatItem(
                icon = Icons.Default.Schedule,
                value = "%.1fh".format(stats.totalWeeklyHours),
                label = "Horas/Sem",
                color = WarningYellow
            )

            StatItem(
                icon = Icons.Default.CalendarToday,
                value = stats.daysWithClasses.toString(),
                label = "Dias",
                color = Color(0xFF9C27B0)
            )
        }
    }
}

@Composable
private fun StatItem(
    icon: ImageVector,
    value: String,
    label: String,
    color: Color
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = color,
            modifier = Modifier.size(24.dp)
        )
        Text(
            text = value,
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = NavyBlue
        )
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = DarkGray
        )
    }
}
