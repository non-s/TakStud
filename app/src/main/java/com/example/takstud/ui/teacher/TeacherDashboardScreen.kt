package com.example.takstud.ui.teacher

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Analytics
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.example.takstud.ui.components.MetricCard
import com.example.takstud.ui.components.SimpleBarChart
import com.example.takstud.ui.components.SimplePieChart
import com.example.takstud.viewmodel.TaskViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TeacherDashboardScreen(
    navController: NavController,
    viewModel: TaskViewModel = hiltViewModel()
) {
    var selectedTab by remember { mutableStateOf(0) }

    Scaffold(
        topBar = {
            TopAppBar(title = { Text("Painel do Professor") })
        },
        bottomBar = {
            NavigationBar {
                NavigationBarItem(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    icon = { Icon(Icons.Default.Home, contentDescription = "Início") },
                    label = { Text("Início") }
                )
                NavigationBarItem(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    icon = { Icon(Icons.Default.Analytics, contentDescription = "Analytics") },
                    label = { Text("Analytics") }
                )
                NavigationBarItem(
                    selected = selectedTab == 2,
                    onClick = { selectedTab = 2 },
                    icon = { Icon(Icons.Default.Person, contentDescription = "Alunos") },
                    label = { Text("Alunos") }
                )
            }
        },
        floatingActionButton = {
            if (selectedTab == 0) {
                FloatingActionButton(onClick = { navController.navigate("task_editor") }) {
                    Icon(Icons.Default.Add, contentDescription = "Nova Tarefa")
                }
            }
        }
    ) { padding ->
        Box(modifier = Modifier.padding(padding)) {
            when (selectedTab) {
                0 -> TeacherHomeContent(navController)
                1 -> TeacherAnalyticsContent()
                2 -> Text("Lista de Alunos (TODO)")
            }
        }
    }
}

@Composable
fun TeacherHomeContent(navController: NavController) {
    Column(modifier = Modifier.padding(16.dp)) {
        Text("Bem-vindo, Professor!", style = MaterialTheme.typography.headlineSmall)
        Spacer(modifier = Modifier.height(16.dp))
        // TODO: List recent tasks
        Text("Tarefas Recentes", style = MaterialTheme.typography.titleMedium)
        Button(onClick = { navController.navigate("task_list") }) {
            Text("Ver todas as tarefas")
        }
    }
}

@Composable
fun TeacherAnalyticsContent() {
    Column(
        modifier = Modifier
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text("Visão Geral da Turma", style = MaterialTheme.typography.headlineSmall)

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            MetricCard(
                title = "Frequência Média",
                value = "92%",
                trend = "+2% vs semana passada",
                modifier = Modifier.weight(1f)
            )
            MetricCard(
                title = "Média de Notas",
                value = "7.8",
                trend = "Estável",
                isPositive = true,
                modifier = Modifier.weight(1f)
            )
        }

        Card {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("Distribuição de Notas", style = MaterialTheme.typography.titleMedium)
                Spacer(modifier = Modifier.height(16.dp))
                SimpleBarChart(
                    data = mapOf(
                        "A" to 12,
                        "B" to 18,
                        "C" to 5,
                        "D" to 2
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp)
                )
            }
        }

        Card {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("Status das Tarefas", style = MaterialTheme.typography.titleMedium)
                Spacer(modifier = Modifier.height(16.dp))
                SimplePieChart(
                    data = mapOf(
                        "Concluído" to 75f,
                        "Pendente" to 20f,
                        "Atrasado" to 5f
                    ),
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}
