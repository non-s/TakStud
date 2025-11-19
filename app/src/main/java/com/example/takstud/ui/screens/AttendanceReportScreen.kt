package com.example.takstud.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.takstud.model.*
import com.example.takstud.ui.common.UiState
import com.example.takstud.viewmodel.AttendanceReportViewModel

/**
 * AttendanceReportScreen - Tela para exibição de relatórios de frequência.
 *
 * FUNCIONALIDADES:
 * - Exibição de relatório com estado (Loading, Success, Error, Empty)
 * - Filtros por período
 * - Alertas de risco com cores
 * - Exportação em CSV
 * - Retry automático em caso de erro
 *
 * EXEMPLO DE USO:
 * AttendanceReportScreen(
 *     studentId = "student1",
 *     viewModel = hiltViewModel()
 * )
 */
@Composable
fun AttendanceReportScreen(
    studentId: String,
    viewModel: AttendanceReportViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val filters by viewModel.currentFilters.collectAsState()

    LaunchedEffect(studentId) {
        viewModel.loadStudentReport(studentId)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // Header com título
        ReportHeader(
            title = "Relatório de Frequência",
            onRefresh = { viewModel.loadStudentReport(studentId) }
        )

        // Conteúdo principal baseado no estado
        Box(
            modifier = Modifier
                .fillMaxSize()
                .weight(1f)
        ) {
            when (uiState) {
                is UiState.Loading -> {
                    LoadingState(
                        message = (uiState as UiState.Loading).message,
                        modifier = Modifier.align(Alignment.Center)
                    )
                }
                is UiState.Success -> {
                    val report = (uiState as UiState.Success<AttendanceReport>).data
                    ReportContent(
                        report = report,
                        onExport = { viewModel.exportCurrentReport() }
                    )
                }
                is UiState.Error -> {
                    ErrorState(
                        message = (uiState as UiState.Error<AttendanceReport>).message,
                        retryable = (uiState as UiState.Error<AttendanceReport>).retryable,
                        onRetry = { viewModel.retry() },
                        modifier = Modifier.align(Alignment.Center)
                    )
                }
                is UiState.Empty -> {
                    EmptyState(
                        message = (uiState as UiState.Empty<AttendanceReport>).message,
                        modifier = Modifier.align(Alignment.Center)
                    )
                }
            }
        }
    }
}

/**
 * Cabeçalho do relatório com título e botões de ação.
 */
@OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)
@Composable
fun ReportHeader(
    title: String,
    onRefresh: () -> Unit,
    modifier: Modifier = Modifier
) {
    TopAppBar(
        title = { Text(title) },
        actions = {
            IconButton(onClick = onRefresh) {
                Icon(
                    imageVector = Icons.Default.Refresh,
                    contentDescription = "Atualizar"
                )
            }
        },
        modifier = modifier
    )
}

/**
 * Conteúdo do relatório com resumo e detalhes.
 */
@Composable
fun ReportContent(
    report: AttendanceReport,
    onExport: () -> String?,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        // Card de resumo principal
        AttendanceCard(report = report)

        Spacer(modifier = Modifier.height(16.dp))

        // Detalhes
        Text(
            text = "Detalhes",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        AttendanceDetails(report = report)

        Spacer(modifier = Modifier.height(16.dp))

        // Status de risco
        if (report.isCritical || report.isLow) {
            RiskAlert(report = report)
            Spacer(modifier = Modifier.height(16.dp))
        }

        // Botão de exportação
        ExportButton(
            onClick = { onExport() },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(16.dp))
    }
}

/**
 * Card principal mostrando percentual de frequência.
 */
@OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)
@Composable
fun AttendanceCard(
    report: AttendanceReport,
    modifier: Modifier = Modifier
) {
    val backgroundColor = when {
        report.isCritical -> Color(0xFFFFEBEE)  // Red light
        report.isLow -> Color(0xFFFFF3E0)       // Orange light
        else -> Color(0xFFE8F5E9)               // Green light
    }

    val contentColor = when {
        report.isCritical -> Color(0xFFC62828)  // Red dark
        report.isLow -> Color(0xFFE65100)       // Orange dark
        else -> Color(0xFF2E7D32)               // Green dark
    }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .background(backgroundColor),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Frequência",
                style = MaterialTheme.typography.labelMedium,
                color = contentColor
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = report.formatAttendancePercentage(),
                style = MaterialTheme.typography.displayLarge,
                color = contentColor,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "${report.presentDays}/${report.totalDays} dias presentes",
                style = MaterialTheme.typography.bodyMedium,
                color = contentColor
            )

            Spacer(modifier = Modifier.height(8.dp))

            StatusBadge(report = report)
        }
    }
}

/**
 * Badge mostrando status da frequência.
 */
@Composable
fun StatusBadge(
    report: AttendanceReport,
    modifier: Modifier = Modifier
) {
    val statusText = when {
        report.isCritical -> "Crítica"
        report.isLow -> "Baixa"
        else -> "Adequada"
    }

    val statusColor = when {
        report.isCritical -> Color.Red
        report.isLow -> Color(0xFFFFA726)
        else -> Color.Green
    }

    Surface(
        modifier = modifier
            .padding(top = 8.dp),
        color = statusColor,
        shape = MaterialTheme.shapes.small
    ) {
        Text(
            text = statusText,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
            color = Color.White,
            style = MaterialTheme.typography.labelSmall
        )
    }
}

/**
 * Detalhes do relatório em forma de lista.
 */
@Composable
fun AttendanceDetails(
    report: AttendanceReport,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            DetailRow("Estudante", report.studentName)
            Divider(modifier = Modifier.padding(vertical = 8.dp))

            DetailRow("RA", report.studentRa)
            Divider(modifier = Modifier.padding(vertical = 8.dp))

            DetailRow("Turma", report.className)
            Divider(modifier = Modifier.padding(vertical = 8.dp))

            DetailRow("Período", report.period)
            Divider(modifier = Modifier.padding(vertical = 8.dp))

            DetailRow("Total de Dias", "${report.totalDays}")
            Divider(modifier = Modifier.padding(vertical = 8.dp))

            DetailRow("Dias Presentes", "${report.presentDays}")
            Divider(modifier = Modifier.padding(vertical = 8.dp))

            DetailRow("Faltas", "${report.absentDays}")
        }
    }
}

/**
 * Linha de detalhe com rótulo e valor.
 */
@Composable
fun DetailRow(
    label: String,
    value: String,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}

/**
 * Alerta de risco quando frequência está baixa ou crítica.
 */
@Composable
fun RiskAlert(
    report: AttendanceReport,
    modifier: Modifier = Modifier
) {
    val backgroundColor = if (report.isCritical) Color(0xFFFFEBEE) else Color(0xFFFFF3E0)
    val borderColor = if (report.isCritical) Color.Red else Color(0xFFFFA726)

    Card(
        modifier = modifier
            .fillMaxWidth()
            .border(2.dp, borderColor, MaterialTheme.shapes.medium),
        colors = CardDefaults.cardColors(containerColor = backgroundColor)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.Top
        ) {
            Icon(
                imageVector = if (report.isCritical) Icons.Default.Error else Icons.Default.Warning,
                contentDescription = null,
                tint = borderColor,
                modifier = Modifier.size(24.dp)
            )

            Spacer(modifier = Modifier.width(12.dp))

            Column {
                Text(
                    text = if (report.isCritical) "Frequência Crítica" else "Frequência Baixa",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = borderColor
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = if (report.isCritical) {
                        "Contato imediato com responsável é recomendado."
                    } else {
                        "Monitoramento intenso necessário. Verifique as causas."
                    },
                    style = MaterialTheme.typography.bodySmall,
                    color = borderColor
                )
            }
        }
    }
}

/**
 * Botão de exportação em CSV.
 */
@Composable
fun ExportButton(
    onClick: () -> String?,
    modifier: Modifier = Modifier
) {
    Button(
        onClick = { onClick() },
        modifier = modifier.height(48.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = MaterialTheme.colorScheme.primary
        )
    ) {
        Icon(
            imageVector = Icons.Default.Download,
            contentDescription = null,
            modifier = Modifier.size(20.dp)
        )

        Spacer(modifier = Modifier.width(8.dp))

        Text("Exportar em CSV")
    }
}

/**
 * Estado de carregamento.
 */
@Composable
fun LoadingState(
    message: String,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        CircularProgressIndicator()

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = message,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

/**
 * Estado de erro com opção de retry.
 */
@Composable
fun ErrorState(
    message: String,
    retryable: Boolean,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Default.ErrorOutline,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.error,
            modifier = Modifier.size(48.dp)
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "Erro",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.error
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = message,
            style = MaterialTheme.typography.bodyMedium,
            textAlign = androidx.compose.ui.text.style.TextAlign.Center
        )

        if (retryable) {
            Spacer(modifier = Modifier.height(24.dp))

            Button(onClick = onRetry) {
                Icon(
                    imageVector = Icons.Default.Refresh,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )

                Spacer(modifier = Modifier.width(8.dp))

                Text("Tentar Novamente")
            }
        }
    }
}

/**
 * Estado vazio (sem dados).
 */
@Composable
fun EmptyState(
    message: String,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Default.Info,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.size(48.dp)
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = message,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
private fun Divider() {
    Divider(
        color = MaterialTheme.colorScheme.outlineVariant,
        thickness = 1.dp
    )
}
