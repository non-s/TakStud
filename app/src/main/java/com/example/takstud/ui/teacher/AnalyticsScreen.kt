@file:OptIn(ExperimentalMaterial3Api::class)

package com.example.takstud.ui.teacher

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.takstud.ui.components.BarChart
import com.example.takstud.ui.components.CircularProgressCard
import com.example.takstud.ui.components.LineChart
import com.example.takstud.ui.theme.*

/**
 * AnalyticsScreen Premium - Dashboard completo com gráficos
 */
@Composable
fun AnalyticsScreen(
    modifier: Modifier = Modifier,
    onBack: () -> Unit
) {
    // Dados de exemplo - em produção viriam do ViewModel
    val evolutionData = listOf(3.5f, 4.0f, 3.8f, 4.2f, 4.5f)
    val evolutionLabels = listOf("Jan", "Fev", "Mar", "Abr", "Mai")
    
    val subjectGrades = listOf(
        "Matemática" to 4.5f,
        "Português" to 4.2f,
        "História" to 3.8f,
        "Geografia" to 4.0f,
        "Ciências" to 4.3f
    )
    
    val attendancePercentage = 92f
    val averageGrade = 4.16f
    val completedTasks = 85f

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        PrimaryBlue.copy(alpha = 0.03f),
                        Neutral50
                    )
                )
            )
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            // Header
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        brush = Brush.verticalGradient(
                            colors = listOf(PrimaryBlue, PrimaryDark)
                        )
                    )
                    .padding(16.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = onBack) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            "Voltar",
                            tint = Color.White
                        )
                    }
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                Icons.AutoMirrored.Filled.TrendingUp,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(28.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Analytics",
                                style = MaterialTheme.typography.headlineSmall,
                                color = Color.White,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Text(
                            text = "Visão geral do desempenho",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.White.copy(alpha = 0.8f)
                        )
                    }
                }
            }

            // Content
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // KPIs Row
                Text(
                    "Indicadores Principais",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = Neutral900
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    CircularProgressCard(
                        title = "Frequência",
                        value = attendancePercentage,
                        maxValue = 100f,
                        modifier = Modifier.weight(1f),
                        color = Success,
                        icon = "📊"
                    )
                    
                    CircularProgressCard(
                        title = "Tarefas",
                        value = completedTasks,
                        maxValue = 100f,
                        modifier = Modifier.weight(1f),
                        color = AccentPurple,
                        icon = "✅"
                    )
                }

                // Average Grade Card
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            "Média Geral",
                            style = MaterialTheme.typography.labelLarge,
                            color = Neutral500
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = String.format("%.2f", averageGrade),
                            style = MaterialTheme.typography.displayLarge,
                            fontWeight = FontWeight.Bold,
                            color = PrimaryBlue
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = Success.copy(alpha = 0.1f)
                        ) {
                            Text(
                                text = "Acima da média da turma",
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                                color = Success,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Evolution Chart
                Text(
                    "Evolução das Notas",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = Neutral900
                )

                LineChart(
                    modifier = Modifier,
                    data = evolutionData,
                    labels = evolutionLabels,
                    title = "Últimos 5 Meses",
                    maxValue = 5f,
                    color = PrimaryBlue
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Subject Grades
                Text(
                    "Desempenho por Matéria",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = Neutral900
                )

                BarChart(
                    modifier = Modifier,
                    data = subjectGrades,
                    title = "Notas Atuais",
                    maxValue = 5f,
                    color = AccentTeal
                )

                // Insights Card
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = AccentPurple.copy(alpha = 0.1f)
                    ),
                    elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "💡",
                                fontSize = 24.sp
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                "Insights",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = AccentPurple
                            )
                        }
                        Spacer(modifier = Modifier.height(12.dp))
                        InsightItem("Melhor desempenho: Matemática (4.5)")
                        InsightItem("Área para melhorar: História (3.8)")
                        InsightItem("Tendência: Crescimento constante")
                        InsightItem("Frequência excelente: 92%")
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }
}

@Composable
fun InsightItem(text: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(6.dp)
                .background(AccentPurple, shape = RoundedCornerShape(3.dp))
        )
        Spacer(modifier = Modifier.width(12.dp))
        Text(
            text = text,
            style = MaterialTheme.typography.bodyMedium,
            color = Neutral700
        )
    }
}
