package com.example.takstud.ui.components.student

import androidx.compose.animation.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.takstud.model.student.*
import java.text.SimpleDateFormat
import java.util.*

/**
 * 🎨 Componentes UI Reutilizáveis para Alunos
 */

// ==================== STUDENT CARD ====================

/**
 * Card compacto de aluno para listas
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StudentCard(
    student: StudentExtended,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    showStats: Boolean = true,
    selected: Boolean = false
) {
    Card(
        onClick = onClick,
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = if (selected) {
                MaterialTheme.colorScheme.primaryContainer
            } else {
                MaterialTheme.colorScheme.surface
            }
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = if (selected) 4.dp else 2.dp
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Avatar
            StudentAvatar(
                photoUrl = student.personalInfo.photoUrl,
                name = student.personalInfo.fullName,
                size = 56.dp
            )

            // Info
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                // Nome
                Text(
                    text = student.personalInfo.fullName,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                // Matrícula
                Text(
                    text = "RA: ${student.academicInfo.registrationNumber}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                // Turma
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.School,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = "${student.academicInfo.className} - ${student.academicInfo.grade}",
                        style = MaterialTheme.typography.bodySmall
                    )
                }

                // Stats
                if (showStats) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        modifier = Modifier.padding(top = 4.dp)
                    ) {
                        if (student.academicInfo.gpa > 0) {
                            StatChip(
                                icon = Icons.Default.Grade,
                                label = "%.1f".format(student.academicInfo.gpa),
                                color = when {
                                    student.academicInfo.gpa >= 7.0 -> Color(0xFF4CAF50)
                                    student.academicInfo.gpa >= 5.0 -> Color(0xFFFFC107)
                                    else -> Color(0xFFF44336)
                                }
                            )
                        }

                        if (student.academicInfo.attendanceRate > 0) {
                            StatChip(
                                icon = Icons.Default.EventAvailable,
                                label = "${student.academicInfo.attendanceRate.toInt()}%",
                                color = when {
                                    student.academicInfo.attendanceRate >= 75.0 -> Color(0xFF4CAF50)
                                    student.academicInfo.attendanceRate >= 50.0 -> Color(0xFFFFC107)
                                    else -> Color(0xFFF44336)
                                }
                            )
                        }
                    }
                }
            }

            // Status badge
            StatusBadge(status = student.academicInfo.status)

            // Special indicators
            Column(
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                if (student.academicInfo.isScholarship) {
                    Icon(
                        imageVector = Icons.Default.CardGiftcard,
                        contentDescription = "Bolsista",
                        modifier = Modifier.size(20.dp),
                        tint = Color(0xFFFF9800)
                    )
                }

                if (student.hasSpecialNeeds()) {
                    Icon(
                        imageVector = Icons.Default.AccessibleForward,
                        contentDescription = "Necessidades especiais",
                        modifier = Modifier.size(20.dp),
                        tint = Color(0xFF2196F3)
                    )
                }
            }
        }
    }
}

// ==================== STUDENT AVATAR ====================

@Composable
fun StudentAvatar(
    photoUrl: String,
    name: String,
    modifier: Modifier = Modifier,
    size: androidx.compose.ui.unit.Dp = 40.dp
) {
    Box(
        modifier = modifier
            .size(size)
            .clip(CircleShape)
            .background(MaterialTheme.colorScheme.primaryContainer),
        contentAlignment = Alignment.Center
    ) {
        if (photoUrl.isNotBlank()) {
            // TODO: Load image from URL with Coil
            // AsyncImage(model = photoUrl, contentDescription = name)
            Text(
                text = name.firstOrNull()?.uppercase() ?: "?",
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onPrimaryContainer
            )
        } else {
            Text(
                text = name.firstOrNull()?.uppercase() ?: "?",
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onPrimaryContainer
            )
        }
    }
}

// ==================== STATUS BADGE ====================

@Composable
fun StatusBadge(
    status: StudentStatus,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(12.dp),
        color = when (status) {
            StudentStatus.ACTIVE -> Color(0xFF4CAF50)
            StudentStatus.INACTIVE -> Color(0xFF9E9E9E)
            StudentStatus.TRANSFERRED -> Color(0xFF2196F3)
            StudentStatus.DROPPED_OUT -> Color(0xFFF44336)
            StudentStatus.GRADUATED -> Color(0xFFFF9800)
            StudentStatus.SUSPENDED -> Color(0xFFFFC107)
        }.copy(alpha = 0.2f)
    ) {
        Text(
            text = status.displayName,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            style = MaterialTheme.typography.labelSmall,
            color = when (status) {
                StudentStatus.ACTIVE -> Color(0xFF4CAF50)
                StudentStatus.INACTIVE -> Color(0xFF9E9E9E)
                StudentStatus.TRANSFERRED -> Color(0xFF2196F3)
                StudentStatus.DROPPED_OUT -> Color(0xFFF44336)
                StudentStatus.GRADUATED -> Color(0xFFFF9800)
                StudentStatus.SUSPENDED -> Color(0xFFFFC107)
            }
        )
    }
}

// ==================== STAT CHIP ====================

@Composable
fun StatChip(
    icon: ImageVector,
    label: String,
    color: Color,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(8.dp),
        color = color.copy(alpha = 0.1f)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.size(14.dp),
                tint = color
            )
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = color,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

// ==================== GUARDIAN CARD ====================

@Composable
fun GuardianCard(
    guardian: Guardian,
    onEdit: (() -> Unit)? = null,
    onDelete: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    OutlinedCard(
        modifier = modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = guardian.name,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = guardian.relationship.displayName,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Row(
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    if (guardian.isFinancialResponsible) {
                        AssistChip(
                            onClick = { },
                            label = { Text("Resp. Financeiro") },
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Default.AttachMoney,
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp)
                                )
                            },
                            colors = AssistChipDefaults.assistChipColors(
                                containerColor = Color(0xFF4CAF50).copy(alpha = 0.1f),
                                labelColor = Color(0xFF4CAF50)
                            )
                        )
                    }
                }
            }

            // Contact Info
            Column(
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                if (guardian.phone.isNotBlank()) {
                    InfoRow(
                        icon = Icons.Default.Phone,
                        label = guardian.getFormattedPhone()
                    )
                }

                if (guardian.email.isNotBlank()) {
                    InfoRow(
                        icon = Icons.Default.Email,
                        label = guardian.email
                    )
                }

                if (guardian.occupation.isNotBlank()) {
                    InfoRow(
                        icon = Icons.Default.Work,
                        label = guardian.occupation
                    )
                }
            }

            // Actions
            if (onEdit != null || onDelete != null) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (onEdit != null) {
                        IconButton(onClick = onEdit) {
                            Icon(
                                imageVector = Icons.Default.Edit,
                                contentDescription = "Editar"
                            )
                        }
                    }

                    if (onDelete != null) {
                        IconButton(onClick = onDelete) {
                            Icon(
                                imageVector = Icons.Default.Delete,
                                contentDescription = "Remover",
                                tint = MaterialTheme.colorScheme.error
                            )
                        }
                    }
                }
            }
        }
    }
}

// ==================== INFO ROW ====================

@Composable
fun InfoRow(
    icon: ImageVector,
    label: String,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            modifier = Modifier.size(18.dp),
            tint = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium
        )
    }
}

// ==================== TIMELINE EVENT CARD ====================

@Composable
fun TimelineEventCard(
    event: StudentTimelineEvent,
    onDelete: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Icon
            Surface(
                shape = CircleShape,
                color = getTimelineEventColor(event.type).copy(alpha = 0.2f)
            ) {
                Box(
                    modifier = Modifier.size(40.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = event.type.icon,
                        style = MaterialTheme.typography.titleMedium
                    )
                }
            }

            // Content
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = event.title,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold
                    )

                    if (onDelete != null) {
                        IconButton(
                            onClick = onDelete,
                            modifier = Modifier.size(24.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Delete,
                                contentDescription = "Remover",
                                modifier = Modifier.size(16.dp),
                                tint = MaterialTheme.colorScheme.error
                            )
                        }
                    }
                }

                Text(
                    text = event.description,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Text(
                    text = formatTimestamp(event.timestamp),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                // Type badge
                Surface(
                    shape = RoundedCornerShape(4.dp),
                    color = getTimelineEventColor(event.type).copy(alpha = 0.2f)
                ) {
                    Text(
                        text = event.type.displayName,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                        style = MaterialTheme.typography.labelSmall,
                        color = getTimelineEventColor(event.type)
                    )
                }
            }
        }
    }
}

private fun getTimelineEventColor(type: TimelineEventType): Color {
    return when (type) {
        TimelineEventType.ENROLLMENT -> Color(0xFF4CAF50)
        TimelineEventType.CLASS_CHANGE -> Color(0xFF2196F3)
        TimelineEventType.GRADE_CHANGE -> Color(0xFF2196F3)
        TimelineEventType.DISCIPLINARY -> Color(0xFFF44336)
        TimelineEventType.ACHIEVEMENT -> Color(0xFFFF9800)
        TimelineEventType.MEETING -> Color(0xFF9C27B0)
        TimelineEventType.MEDICAL -> Color(0xFFE91E63)
        TimelineEventType.TRANSFER -> Color(0xFF00BCD4)
        TimelineEventType.DROPOUT -> Color(0xFFF44336)
        TimelineEventType.GRADUATION -> Color(0xFFFF9800)
        TimelineEventType.OTHER -> Color(0xFF9E9E9E)
    }
}

// ==================== STATS CARD ====================

@Composable
fun StudentStatsCard(
    stats: StudentStats,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = "Estatísticas",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )

            // Performance
            StatsSection(
                title = "Desempenho",
                items = listOf(
                    StatItem(
                        label = "Média Geral",
                        value = "%.2f".format(stats.averageGrade),
                        icon = Icons.Default.Grade,
                        color = when {
                            stats.averageGrade >= 7.0 -> Color(0xFF4CAF50)
                            stats.averageGrade >= 5.0 -> Color(0xFFFFC107)
                            else -> Color(0xFFF44336)
                        }
                    ),
                    StatItem(
                        label = "Maior Nota",
                        value = "%.2f".format(stats.highestGrade),
                        icon = Icons.Default.TrendingUp,
                        color = Color(0xFF4CAF50)
                    ),
                    StatItem(
                        label = "Menor Nota",
                        value = "%.2f".format(stats.lowestGrade),
                        icon = Icons.Default.TrendingDown,
                        color = Color(0xFFF44336)
                    )
                )
            )

            Divider()

            // Attendance
            StatsSection(
                title = "Frequência",
                items = listOf(
                    StatItem(
                        label = "Taxa de Presença",
                        value = "${stats.attendanceRate.toInt()}%",
                        icon = Icons.Default.EventAvailable,
                        color = when {
                            stats.attendanceRate >= 75.0 -> Color(0xFF4CAF50)
                            stats.attendanceRate >= 50.0 -> Color(0xFFFFC107)
                            else -> Color(0xFFF44336)
                        }
                    ),
                    StatItem(
                        label = "Aulas Presentes",
                        value = "${stats.attendedClasses}",
                        icon = Icons.Default.Check,
                        color = Color(0xFF4CAF50)
                    ),
                    StatItem(
                        label = "Faltas",
                        value = "${stats.absentClasses}",
                        icon = Icons.Default.Close,
                        color = Color(0xFFF44336)
                    )
                )
            )

            Divider()

            // Tasks
            StatsSection(
                title = "Tarefas",
                items = listOf(
                    StatItem(
                        label = "Taxa de Conclusão",
                        value = "${stats.taskCompletionRate.toInt()}%",
                        icon = Icons.Default.Assignment,
                        color = when {
                            stats.taskCompletionRate >= 75.0 -> Color(0xFF4CAF50)
                            stats.taskCompletionRate >= 50.0 -> Color(0xFFFFC107)
                            else -> Color(0xFFF44336)
                        }
                    ),
                    StatItem(
                        label = "Concluídas",
                        value = "${stats.completedTasks}",
                        icon = Icons.Default.CheckCircle,
                        color = Color(0xFF4CAF50)
                    ),
                    StatItem(
                        label = "Pendentes",
                        value = "${stats.pendingTasks}",
                        icon = Icons.Default.PendingActions,
                        color = Color(0xFFFFC107)
                    )
                )
            )
        }
    }
}

@Composable
private fun StatsSection(
    title: String,
    items: List<StatItem>,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )

        items.forEach { item ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = item.icon,
                        contentDescription = null,
                        modifier = Modifier.size(20.dp),
                        tint = item.color
                    )
                    Text(
                        text = item.label,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }

                Text(
                    text = item.value,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = item.color
                )
            }
        }
    }
}

private data class StatItem(
    val label: String,
    val value: String,
    val icon: ImageVector,
    val color: Color
)

// ==================== HELPERS ====================

private fun formatTimestamp(timestamp: Long): String {
    val sdf = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
    return sdf.format(Date(timestamp))
}

// ==================== EMPTY STATE ====================

@Composable
fun EmptyStudentList(
    message: String = "Nenhum aluno encontrado",
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Icon(
                imageVector = Icons.Default.PersonOff,
                contentDescription = null,
                modifier = Modifier.size(64.dp),
                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
            )
            Text(
                text = message,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

// ==================== LOADING ====================

@Composable
fun StudentListLoading(
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        CircularProgressIndicator()
    }
}
