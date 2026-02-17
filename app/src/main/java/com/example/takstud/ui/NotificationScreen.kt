package com.example.takstud.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.takstud.model.Notification
import com.example.takstud.model.NotificationPriority
import com.example.takstud.model.NotificationType
import com.example.takstud.ui.theme.*
import com.example.takstud.viewmodel.NotificationViewModel
import java.text.SimpleDateFormat
import java.util.*

/**
 * NotificationScreen - Tela de Notificações
 *
 * Funcionalidades:
 * - Lista de notificações do usuário
 * - Filtros por tipo e prioridade
 * - Marcar como lida/não lida
 * - Visualização de detalhes
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotificationScreen(
    viewModel: NotificationViewModel = hiltViewModel(),
    onBack: () -> Unit = {}
) {
    val filteredNotifications by viewModel.filteredNotifications.collectAsState()
    val unreadCount by viewModel.unreadCount.collectAsState()
    val selectedType by viewModel.selectedType.collectAsState()
    val showOnlyUnread by viewModel.showOnlyUnread.collectAsState()
    val uiState by viewModel.uiState.collectAsState()

    var showFilterDialog by remember { mutableStateOf(false) }
    var showDetailsDialog by remember { mutableStateOf(false) }
    val selectedNotification by viewModel.selectedNotification.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            "Notificações",
                            style = MaterialTheme.typography.titleLarge.copy(
                                fontWeight = FontWeight.Bold
                            )
                        )
                        if (unreadCount > 0) {
                            Text(
                                "$unreadCount não lida${if (unreadCount > 1) "s" else ""}",
                                style = MaterialTheme.typography.labelSmall,
                                color = Color.White.copy(alpha = 0.8f)
                            )
                        }
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Voltar")
                    }
                },
                actions = {
                    IconButton(onClick = { showFilterDialog = true }) {
                        Icon(Icons.Filled.FilterList, "Filtros")
                    }
                    if (unreadCount > 0) {
                        IconButton(onClick = { viewModel.markAllAsRead() }) {
                            Icon(Icons.Filled.DoneAll, "Marcar todas como lidas")
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = PrimaryBlue,
                    titleContentColor = Color.White,
                    navigationIconContentColor = Color.White,
                    actionIconContentColor = Color.White
                )
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(Neutral50)
        ) {
            // Filtro rápido
            FilterChips(
                selectedType = selectedType,
                showOnlyUnread = showOnlyUnread,
                onTypeSelected = { viewModel.filterByType(it) },
                onToggleUnread = { viewModel.toggleShowOnlyUnread() },
                onClearFilters = { viewModel.clearFilters() }
            )

            // Lista de notificações
            if (filteredNotifications.isEmpty()) {
                EmptyNotificationsView()
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(filteredNotifications, key = { it.id }) { notification ->
                        NotificationCard(
                            notification = notification,
                            onClick = {
                                viewModel.selectNotification(notification)
                                showDetailsDialog = true
                            },
                            onMarkAsRead = {
                                viewModel.markAsRead(notification.id)
                            }
                        )
                    }
                }
            }
        }
    }

    // Dialog de filtros
    if (showFilterDialog) {
        FilterDialog(
            currentType = selectedType,
            onDismiss = { showFilterDialog = false },
            onTypeSelected = {
                viewModel.filterByType(it)
                showFilterDialog = false
            }
        )
    }

    // Dialog de detalhes
    if (showDetailsDialog && selectedNotification != null) {
        NotificationDetailsDialog(
            notification = selectedNotification!!,
            onDismiss = {
                showDetailsDialog = false
                viewModel.selectNotification(null)
            },
            onDelete = {
                viewModel.deleteNotification(selectedNotification!!.id)
                showDetailsDialog = false
                viewModel.selectNotification(null)
            }
        )
    }
}

@Composable
fun FilterChips(
    selectedType: NotificationType?,
    showOnlyUnread: Boolean,
    onTypeSelected: (NotificationType?) -> Unit,
    onToggleUnread: () -> Unit,
    onClearFilters: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        LazyRow(
            modifier = Modifier.weight(1f),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            item {
                FilterChip(
                    selected = showOnlyUnread,
                    onClick = onToggleUnread,
                    label = { Text("Não lidas") },
                    leadingIcon = if (showOnlyUnread) {
                        { Icon(Icons.Filled.Check, null, Modifier.size(18.dp)) }
                    } else null
                )
            }

            item {
                FilterChip(
                    selected = selectedType != null,
                    onClick = { if (selectedType != null) onClearFilters() },
                    label = {
                        Text(selectedType?.displayName ?: "Todos os tipos")
                    },
                    trailingIcon = if (selectedType != null) {
                        { Icon(Icons.Filled.Close, null, Modifier.size(18.dp)) }
                    } else null
                )
            }
        }
    }
}

@Composable
fun NotificationCard(
    notification: Notification,
    onClick: () -> Unit,
    onMarkAsRead: () -> Unit
) {
    val priorityColor = when (notification.priority) {
        NotificationPriority.LOW -> Color(0xFF4CAF50)
        NotificationPriority.NORMAL -> PrimaryBlue
        NotificationPriority.HIGH -> Color(0xFFFF9800)
        NotificationPriority.URGENT -> Color(0xFFF44336)
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(
            containerColor = if (!notification.isRead) {
                Color.White
            } else {
                Neutral100
            }
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = if (!notification.isRead) 2.dp else 1.dp
        ),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.Top
        ) {
            // Indicador de não lida
            if (!notification.isRead) {
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .clip(CircleShape)
                        .background(PrimaryBlue)
                        .align(Alignment.Top)
                )
                Spacer(modifier = Modifier.width(12.dp))
            }

            // Ícone do tipo
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(priorityColor.copy(alpha = 0.1f)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = notification.type.icon,
                    style = MaterialTheme.typography.titleMedium
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                // Título
                Text(
                    text = notification.title,
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = if (!notification.isRead) FontWeight.Bold else FontWeight.Normal
                    ),
                    color = Neutral900,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                // Mensagem
                Text(
                    text = notification.message,
                    style = MaterialTheme.typography.bodyMedium,
                    color = Neutral600,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.padding(top = 4.dp)
                )

                // Info adicional
                Row(
                    modifier = Modifier.padding(top = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Tipo
                    Text(
                        text = notification.type.displayName,
                        style = MaterialTheme.typography.labelSmall,
                        color = priorityColor
                    )

                    // Data
                    Text(
                        text = "• ${formatTimestamp(notification.createdAt)}",
                        style = MaterialTheme.typography.labelSmall,
                        color = Neutral500
                    )

                    // Prioridade se for alta ou urgente
                    if (notification.priority == NotificationPriority.HIGH ||
                        notification.priority == NotificationPriority.URGENT) {
                        Text(
                            text = "• ${notification.priority.displayName}",
                            style = MaterialTheme.typography.labelSmall,
                            color = priorityColor,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            // Ação
            if (!notification.isRead) {
                IconButton(
                    onClick = onMarkAsRead,
                    modifier = Modifier.size(24.dp)
                ) {
                    Icon(
                        imageVector = Icons.Filled.Done,
                        contentDescription = "Marcar como lida",
                        tint = PrimaryBlue,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun EmptyNotificationsView() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Icon(
                imageVector = Icons.Filled.Notifications,
                contentDescription = null,
                modifier = Modifier.size(64.dp),
                tint = Neutral300
            )
            Text(
                text = "Nenhuma notificação",
                style = MaterialTheme.typography.titleMedium,
                color = Neutral500
            )
            Text(
                text = "Você está em dia!",
                style = MaterialTheme.typography.bodyMedium,
                color = Neutral400
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FilterDialog(
    currentType: NotificationType?,
    onDismiss: () -> Unit,
    onTypeSelected: (NotificationType?) -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                "Filtrar por tipo",
                style = MaterialTheme.typography.titleLarge.copy(
                    fontWeight = FontWeight.Bold
                )
            )
        },
        text = {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                item {
                    FilterTypeItem(
                        type = null,
                        label = "Todos",
                        icon = "📋",
                        isSelected = currentType == null,
                        onClick = { onTypeSelected(null) }
                    )
                }

                items(NotificationType.values()) { type ->
                    FilterTypeItem(
                        type = type,
                        label = type.displayName,
                        icon = type.icon,
                        isSelected = currentType == type,
                        onClick = { onTypeSelected(type) }
                    )
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Fechar")
            }
        }
    )
}

@Composable
fun FilterTypeItem(
    type: NotificationType?,
    label: String,
    icon: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) PrimaryBlue.copy(alpha = 0.1f) else Color.White
        ),
        border = if (isSelected) {
            androidx.compose.foundation.BorderStroke(2.dp, PrimaryBlue)
        } else null
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = icon,
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                text = label,
                style = MaterialTheme.typography.bodyLarge,
                color = if (isSelected) PrimaryBlue else Neutral900,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
            )
            Spacer(modifier = Modifier.weight(1f))
            if (isSelected) {
                Icon(
                    imageVector = Icons.Filled.Check,
                    contentDescription = null,
                    tint = PrimaryBlue
                )
            }
        }
    }
}

@Composable
fun NotificationDetailsDialog(
    notification: Notification,
    onDismiss: () -> Unit,
    onDelete: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(notification.type.icon)
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    notification.title,
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.Bold
                    )
                )
            }
        },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    text = notification.message,
                    style = MaterialTheme.typography.bodyMedium,
                    color = Neutral700
                )

                HorizontalDivider()

                // Informações adicionais
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    DetailRow(
                        label = "Tipo",
                        value = notification.type.displayName
                    )
                    DetailRow(
                        label = "Prioridade",
                        value = notification.priority.displayName
                    )
                    DetailRow(
                        label = "Data",
                        value = formatFullDate(notification.createdAt)
                    )
                    if (notification.senderName.isNotEmpty()) {
                        DetailRow(
                            label = "De",
                            value = notification.senderName
                        )
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Fechar")
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDelete,
                colors = ButtonDefaults.textButtonColors(
                    contentColor = MaterialTheme.colorScheme.error
                )
            ) {
                Text("Excluir")
            }
        }
    )
}

@Composable
fun DetailRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            color = Neutral600
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            color = Neutral900,
            fontWeight = FontWeight.Medium
        )
    }
}

private fun formatTimestamp(timestamp: Long): String {
    val now = System.currentTimeMillis()
    val diff = now - timestamp

    return when {
        diff < 60000 -> "Agora"
        diff < 3600000 -> "${diff / 60000}min"
        diff < 86400000 -> "${diff / 3600000}h"
        diff < 604800000 -> "${diff / 86400000}d"
        else -> {
            val sdf = SimpleDateFormat("dd/MM", Locale.getDefault())
            sdf.format(Date(timestamp))
        }
    }
}

private fun formatFullDate(timestamp: Long): String {
    val sdf = SimpleDateFormat("dd/MM/yyyy 'às' HH:mm", Locale.getDefault())
    return sdf.format(Date(timestamp))
}