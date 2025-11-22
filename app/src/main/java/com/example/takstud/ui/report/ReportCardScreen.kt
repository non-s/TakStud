package com.example.takstud.ui.report

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.example.takstud.model.grade.ReportCard
import com.example.takstud.model.grade.SubjectGrade
import com.example.takstud.ui.common.UiState
import com.example.takstud.viewmodel.ReportCardViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReportCardScreen(
    studentId: String,
    navController: NavController,
    viewModel: ReportCardViewModel = hiltViewModel()
) {
    val uiState by viewModel.reportCard.collectAsState()

    LaunchedEffect(studentId) {
        viewModel.loadReportCard(studentId)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Boletim Digital") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Voltar")
                    }
                },
                actions = {
                    IconButton(onClick = { /* TODO: Export PDF */ }) {
                        Icon(Icons.Default.Share, contentDescription = "Exportar")
                    }
                }
            )
        }
    ) { padding ->
        Box(modifier = Modifier.padding(padding)) {
            when (val state = uiState) {
                is UiState.Loading -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator()
                    }
                }
                is UiState.Error -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text("Erro: ${state.message}", color = MaterialTheme.colorScheme.error)
                    }
                }
                is UiState.Success -> {
                    ReportCardContent(report = state.data)
                }
                else -> {}
            }
        }
    }
}

@Composable
fun ReportCardContent(report: ReportCard) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Header
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = report.studentName,
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold
                    )
                    Text("RA: ${report.studentRegistrationNumber}")
                    Text("Turma: ${report.className}")
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text("Média Geral", style = MaterialTheme.typography.labelMedium)
                            Text(
                                text = String.format("%.1f", report.overallAverage),
                                style = MaterialTheme.typography.headlineMedium,
                                color = if (report.isPassed) Color(0xFF4CAF50) else Color(0xFFF44336)
                            )
                        }
                        Column {
                            Text("Status", style = MaterialTheme.typography.labelMedium)
                            Text(
                                text = if (report.isPassed) "APROVADO" else "EM RECUPERAÇÃO",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = if (report.isPassed) Color(0xFF4CAF50) else Color(0xFFF44336)
                            )
                        }
                    }
                }
            }
        }

        // Subjects
        item {
            Text(
                "Disciplinas",
                style = MaterialTheme.typography.titleLarge,
                modifier = Modifier.padding(vertical = 8.dp)
            )
        }

        items(report.subjectGrades) { subject ->
            SubjectGradeItem(subject)
        }
    }
}

@Composable
fun SubjectGradeItem(subject: SubjectGrade) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = subject.subjectName,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Badge(
                    containerColor = if (subject.isPassed) Color(0xFFE8F5E9) else Color(0xFFFFEBEE),
                    contentColor = if (subject.isPassed) Color(0xFF2E7D32) else Color(0xFFC62828)
                ) {
                    Text(
                        text = String.format("%.1f", subject.finalScore),
                        style = MaterialTheme.typography.titleMedium,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }
            
            if (subject.needsRecovery) {
                Text(
                    "Necessita Recuperação",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }
        }
    }
}
