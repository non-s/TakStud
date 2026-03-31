package com.example.takstud.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp

/**
 * Empty State Genérico
 */
@Composable
fun EmptyState(
    icon: ImageVector,
    title: String,
    description: String,
    actionText: String? = null,
    onAction: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier.padding(32.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.size(96.dp),
                tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.6f)
            )

            Text(
                text = title,
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onSurface,
                textAlign = TextAlign.Center
            )

            Text(
                text = description,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(horizontal = 16.dp)
            )

            if (actionText != null && onAction != null) {
                Button(
                    onClick = onAction,
                    modifier = Modifier.padding(top = 8.dp)
                ) {
                    Text(actionText)
                }
            }
        }
    }
}

/**
 * Empty State para Lista de Estudantes
 */
@Composable
fun EmptyStudentsList(
    onAddStudent: () -> Unit,
    modifier: Modifier = Modifier
) {
    EmptyState(
        icon = Icons.Default.PersonAdd,
        title = "Nenhum estudante cadastrado",
        description = "Comece adicionando estudantes à sua turma",
        actionText = "Adicionar Estudante",
        onAction = onAddStudent,
        modifier = modifier
    )
}

/**
 * Empty State para Lista de Tarefas
 */
@Composable
fun EmptyTasksList(
    onAddTask: () -> Unit,
    modifier: Modifier = Modifier
) {
    EmptyState(
        icon = Icons.Default.Assignment,
        title = "Nenhuma tarefa disponível",
        description = "Crie tarefas para seus estudantes começarem a trabalhar",
        actionText = "Criar Tarefa",
        onAction = onAddTask,
        modifier = modifier
    )
}

/**
 * Empty State para Lista de Notas
 */
@Composable
fun EmptyGradesList(
    modifier: Modifier = Modifier
) {
    EmptyState(
        icon = Icons.Default.Grade,
        title = "Nenhuma nota lançada",
        description = "As notas aparecerão aqui quando forem lançadas",
        modifier = modifier
    )
}

/**
 * Empty State para Frequência
 */
@Composable
fun EmptyAttendanceList(
    onTakeAttendance: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    EmptyState(
        icon = Icons.Default.CheckCircle,
        title = "Nenhuma chamada registrada",
        description = "Registre a presença dos estudantes",
        actionText = if (onTakeAttendance != null) "Fazer Chamada" else null,
        onAction = onTakeAttendance,
        modifier = modifier
    )
}

/**
 * Empty State para Notificações
 */
@Composable
fun EmptyNotificationsList(
    modifier: Modifier = Modifier
) {
    EmptyState(
        icon = Icons.Default.Notifications,
        title = "Nenhuma notificação",
        description = "Você está em dia! Não há notificações pendentes.",
        modifier = modifier
    )
}

/**
 * Empty State para Calendário
 */
@Composable
fun EmptyCalendar(
    onAddEvent: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    EmptyState(
        icon = Icons.Default.Event,
        title = "Nenhum evento",
        description = "Não há eventos agendados para este período",
        actionText = if (onAddEvent != null) "Adicionar Evento" else null,
        onAction = onAddEvent,
        modifier = modifier
    )
}

/**
 * Empty State para Busca sem Resultados
 */
@Composable
fun EmptySearchResults(
    searchQuery: String,
    modifier: Modifier = Modifier
) {
    EmptyState(
        icon = Icons.Default.SearchOff,
        title = "Nenhum resultado encontrado",
        description = "Não encontramos nada para \"$searchQuery\".\nTente buscar com outros termos.",
        modifier = modifier
    )
}

/**
 * Empty State para Mensagens/Chat
 */
@Composable
fun EmptyMessagesList(
    modifier: Modifier = Modifier
) {
    EmptyState(
        icon = Icons.Default.Message,
        title = "Nenhuma mensagem",
        description = "Inicie uma conversa ou aguarde novas mensagens",
        modifier = modifier
    )
}

/**
 * Empty State para Biblioteca de Conteúdo
 */
@Composable
fun EmptyContentLibrary(
    onUploadContent: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    EmptyState(
        icon = Icons.Default.LibraryBooks,
        title = "Biblioteca vazia",
        description = "Adicione materiais de estudo para seus alunos",
        actionText = if (onUploadContent != null) "Adicionar Material" else null,
        onAction = onUploadContent,
        modifier = modifier
    )
}

/**
 * Empty State Card (versão compacta)
 */
@Composable
fun EmptyStateCard(
    icon: ImageVector,
    text: String,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.size(48.dp),
                tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.6f)
            )

            Text(
                text = text,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
