package com.example.takstud.ui.teacher

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.example.takstud.ui.components.ActionCard
import com.example.takstud.ui.components.MetricCard
import com.example.takstud.ui.theme.*
import com.example.takstud.viewmodel.TaskViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TeacherDashboardScreen(
    navController: NavController,
    viewModel: TaskViewModel = hiltViewModel()
) {
    Scaffold(
        topBar = {
            TopAppBar(title = { Text("Painel do Professor") })
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { navController.navigate("task_editor") }) {
                Icon(Icons.Default.Add, contentDescription = "Nova Tarefa")
            }
        }
    ) { padding ->
        Box(modifier = Modifier.padding(padding)) {
            TeacherHomeContent(navController)
        }
    }
}

@Composable
fun TeacherHomeContent(navController: NavController) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        Neutral50,
                        Color.White
                    )
                )
            )
            .padding(20.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        // Saudação com estilo
        Text(
            "Bem-vindo, Professor! 👋",
            style = MaterialTheme.typography.headlineMedium.copy(
                fontWeight = FontWeight.Bold
            ),
            color = Neutral900
        )
        
        Text(
            "Gerencie suas turmas com facilidade",
            style = MaterialTheme.typography.bodyLarge,
            color = Neutral600
        )

        Spacer(modifier = Modifier.height(8.dp))

        // Card de Tarefas com gradiente azul-roxo
        ActionCard(
            title = "Tarefas",
            description = "Gerencie e crie tarefas para os alunos",
            icon = Icons.Default.Assignment,
            gradientColors = listOf(PrimaryBlue, VibrantPurple),
            onClick = { navController.navigate("task_list") },
            modifier = Modifier.fillMaxWidth()
        )

        // Card de Avisos com gradiente teal-cyan
        ActionCard(
            title = "Avisos",
            description = "Envie avisos e comunicados para alunos e responsáveis",
            icon = Icons.Default.Notifications,
            gradientColors = listOf(AccentTeal, ElectricBlue),
            onClick = { navController.navigate("notice_list") },
            modifier = Modifier.fillMaxWidth()
        )

        // Card de Horários com gradiente laranja-rosa
        ActionCard(
            title = "Horários",
            description = "Visualize e gerencie os horários das turmas",
            icon = Icons.Default.Schedule,
            gradientColors = listOf(AccentOrange, AccentPink),
            onClick = { navController.navigate("schedules_list") },
            modifier = Modifier.fillMaxWidth()
        )
    }
}
