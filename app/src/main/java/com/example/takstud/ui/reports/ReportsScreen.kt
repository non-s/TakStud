@file:OptIn(ExperimentalMaterial3Api::class)

package com.example.takstud.ui.reports

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp

/**
 * ReportsScreen - Tela de relatórios e estatísticas da aplicação
 */
@Composable
fun ReportsScreen(
    modifier: Modifier = Modifier,
    onBack: () -> Unit
) {
    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = { Text("Relatórios e Estatísticas") },
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
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Seção: Resumo Geral
            item {
                ReportSectionHeader(title = "Resumo Geral")
            }

            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    ReportStatCard(
                        title = "Tarefas",
                        value = "12",
                        subtitle = "Criadas este mês",
                        icon = Icons.Default.BarChart,
                        modifier = Modifier.weight(1f)
                    )
                    ReportStatCard(
                        title = "Avisos",
                        value = "8",
                        subtitle = "Publicados",
                        icon = Icons.Default.TrendingUp,
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    ReportStatCard(
                        title = "Turmas",
                        value = "4",
                        subtitle = "Gerenciadas",
                        icon = Icons.Default.BarChart,
                        modifier = Modifier.weight(1f)
                    )
                    ReportStatCard(
                        title = "Alunos",
                        value = "120",
                        subtitle = "Registrados",
                        icon = Icons.Default.TrendingUp,
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            // Seção: Desempenho
            item {
                ReportSectionHeader(title = "Desempenho Acadêmico")
            }

            item {
                ReportDetailCard(
                    title = "Média de Notas",
                    value = "7.8",
                    subtitle = "Todas as turmas",
                    percentage = 78
                )
            }

            item {
                ReportDetailCard(
                    title = "Taxa de Presença",
                    value = "92%",
                    subtitle = "Média geral",
                    percentage = 92
                )
            }

            item {
                ReportDetailCard(
                    title = "Alunos Ativos",
                    value = "115/120",
                    subtitle = "95.8% de participação",
                    percentage = 96
                )
            }

            // Seção: Engajamento
            item {
                ReportSectionHeader(title = "Engajamento")
            }

            item {
                ReportDetailCard(
                    title = "Acessos na App",
                    value = "428",
                    subtitle = "Últimos 30 dias",
                    percentage = 68
                )
            }

            item {
                ReportDetailCard(
                    title = "Notificações Lidas",
                    value = "85%",
                    subtitle = "Taxa de engajamento",
                    percentage = 85
                )
            }

            // Rodapé
            item {
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "Dados atualizados em: ${getCurrentDate()}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 16.dp),
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                )
            }
        }
    }
}

/**
 * ReportSectionHeader - Cabeçalho de seção de relatório
 */
@Composable
fun ReportSectionHeader(
    title: String,
    modifier: Modifier = Modifier
) {
    Text(
        text = title,
        style = MaterialTheme.typography.titleMedium,
        color = MaterialTheme.colorScheme.primary,
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
    )
}

/**
 * ReportStatCard - Card com estatística simples
 */
@Composable
fun ReportStatCard(
    title: String,
    value: String,
    subtitle: String,
    icon: ImageVector,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(4.dp),
        shape = MaterialTheme.shapes.medium,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = title,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(24.dp)
            )
            Text(
                text = value,
                style = MaterialTheme.typography.headlineSmall,
                color = MaterialTheme.colorScheme.onPrimaryContainer
            )
            Text(
                text = title,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onPrimaryContainer
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
            )
        }
    }
}

/**
 * ReportDetailCard - Card com detalhes e barra de progresso
 */
@Composable
fun ReportDetailCard(
    title: String,
    value: String,
    subtitle: String,
    percentage: Int,
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
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = value,
                        style = MaterialTheme.typography.headlineMedium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
                Text(
                    text = "$percentage%",
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.primary
                )
            }

            LinearProgressIndicator(
                progress = { percentage / 100f },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp),
                color = MaterialTheme.colorScheme.primary,
                trackColor = MaterialTheme.colorScheme.surfaceVariant,
            )

            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

/**
 * Obter data atual formatada
 */
private fun getCurrentDate(): String {
    val today = java.time.LocalDate.now()
    val formatter = java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy")
    return today.format(formatter)
}
