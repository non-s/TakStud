@file:OptIn(ExperimentalMaterial3Api::class)

package com.example.takstud.ui.admin

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp

/**
 * AdminDashboardScreen - Dashboard administrativo para gerenciar sistema
 * Acesso apenas com código de admin (58239617)
 */
@Composable
fun AdminDashboardScreen(
    modifier: Modifier = Modifier,
    onBack: () -> Unit,
    onManageTeachers: () -> Unit = {},
    onManageStudents: () -> Unit = {},
    onViewReports: () -> Unit = {},
    onManageSettings: () -> Unit = {}
) {
    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = { Text("Painel Administrativo") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Default.ArrowBack, "Voltar")
                    }
                }
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Cards de Ações Principais
            item {
                Text(
                    text = "Gerenciamento",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.primary
                )
            }

            item {
                AdminActionCard(
                    title = "Gerenciar Professores",
                    subtitle = "Adicionar, editar ou remover professores",
                    icon = Icons.Default.School,
                    onClick = onManageTeachers
                )
            }

            item {
                AdminActionCard(
                    title = "Gerenciar Alunos",
                    subtitle = "Visualizar e gerenciar todos os alunos",
                    icon = Icons.Default.Groups,
                    onClick = onManageStudents
                )
            }

            item {
                AdminActionCard(
                    title = "Configurações do Sistema",
                    subtitle = "Ajustar parâmetros e preferências",
                    icon = Icons.Default.Settings,
                    onClick = onManageSettings
                )
            }

            // Cards de Análise
            item {
                Text(
                    text = "Análise e Relatórios",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(top = 16.dp)
                )
            }

            item {
                AdminActionCard(
                    title = "Relatórios Completos",
                    subtitle = "Visualizar dados e estatísticas",
                    icon = Icons.Default.BarChart,
                    onClick = onViewReports
                )
            }

            // Seção de Status do Sistema
            item {
                Text(
                    text = "Status do Sistema",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(top = 16.dp)
                )
            }

            item {
                SystemStatusCard()
            }

            // Seção de Informações
            item {
                Text(
                    text = "Informações",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(top = 16.dp)
                )
            }

            item {
                AdminInfoCard(
                    label = "Versão do App",
                    value = "1.0",
                    icon = Icons.Default.Info
                )
            }

            item {
                AdminInfoCard(
                    label = "Banco de Dados",
                    value = "Sincronizado",
                    icon = Icons.Default.Storage,
                    color = MaterialTheme.colorScheme.primary
                )
            }

            item {
                AdminInfoCard(
                    label = "Último Backup",
                    value = "Hoje às 10:30",
                    icon = Icons.Default.Backup
                )
            }

            item {
                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }
}

/**
 * AdminActionCard - Card para ações principais do admin
 */
@Composable
fun AdminActionCard(
    title: String,
    subtitle: String,
    icon: ImageVector,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = MaterialTheme.shapes.medium,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = title,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(48.dp)
            )

            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                )
            }

            Icon(
                imageVector = Icons.Default.ChevronRight,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary
            )
        }
    }
}

/**
 * SystemStatusCard - Card com status do sistema
 */
@Composable
fun SystemStatusCard(
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.medium
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Status Geral",
                    style = MaterialTheme.typography.titleSmall
                )
                Surface(
                    shape = MaterialTheme.shapes.small,
                    color = MaterialTheme.colorScheme.tertiary.copy(alpha = 0.2f)
                ) {
                    Text(
                        text = "✓ Online",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.tertiary,
                        modifier = Modifier.padding(8.dp, 4.dp)
                    )
                }
            }

            Divider()

            SystemStatusItem(
                label = "Conexão Firebase",
                status = "Conectado",
                isHealthy = true
            )

            SystemStatusItem(
                label = "Banco de Dados Local",
                status = "Operacional",
                isHealthy = true
            )

            SystemStatusItem(
                label = "Notificações Push",
                status = "Ativas",
                isHealthy = true
            )

            SystemStatusItem(
                label = "Sincronização",
                status = "Em dia",
                isHealthy = true
            )
        }
    }
}

/**
 * SystemStatusItem - Item de status individual
 */
@Composable
fun SystemStatusItem(
    label: String,
    status: String,
    isHealthy: Boolean,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall
        )

        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                shape = MaterialTheme.shapes.small,
                color = if (isHealthy)
                    MaterialTheme.colorScheme.tertiary.copy(alpha = 0.2f)
                else
                    MaterialTheme.colorScheme.error.copy(alpha = 0.2f)
            ) {
                Text(
                    text = status,
                    style = MaterialTheme.typography.bodySmall,
                    color = if (isHealthy)
                        MaterialTheme.colorScheme.tertiary
                    else
                        MaterialTheme.colorScheme.error,
                    modifier = Modifier.padding(8.dp, 4.dp)
                )
            }
        }
    }
}

/**
 * AdminInfoCard - Card com informações do sistema
 */
@Composable
fun AdminInfoCard(
    label: String,
    value: String,
    icon: ImageVector,
    color: androidx.compose.ui.graphics.Color = MaterialTheme.colorScheme.onSurfaceVariant,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.medium
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = label,
                    tint = color,
                    modifier = Modifier.size(24.dp)
                )
                Column {
                    Text(
                        text = label,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = value,
                        style = MaterialTheme.typography.titleSmall
                    )
                }
            }
        }
    }
}
