package com.example.takstud.ui.admin

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.takstud.ui.components.MetricCard
import com.example.takstud.ui.components.SimpleBarChart

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminDashboardScreen(navController: NavController) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Painel Administrativo") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Voltar")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text("Visão Geral da Escola", style = MaterialTheme.typography.headlineSmall)

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                MetricCard(
                    title = "Total Alunos",
                    value = "450",
                    modifier = Modifier.weight(1f)
                )
                MetricCard(
                    title = "Total Turmas",
                    value = "12",
                    modifier = Modifier.weight(1f)
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                MetricCard(
                    title = "Frequência Geral",
                    value = "88%",
                    trend = "-1% vs mês anterior",
                    isPositive = false,
                    modifier = Modifier.weight(1f)
                )
                MetricCard(
                    title = "Alertas Críticos",
                    value = "5",
                    trend = "Ação Necessária",
                    isPositive = false,
                    modifier = Modifier.weight(1f)
                )
            }

            Card {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Desempenho por Série", style = MaterialTheme.typography.titleMedium)
                    Spacer(modifier = Modifier.height(16.dp))
                    SimpleBarChart(
                        data = mapOf(
                            "6º Ano" to 75,
                            "7º Ano" to 82,
                            "8º Ano" to 78,
                            "9º Ano" to 70,
                            "1º EM" to 65,
                            "2º EM" to 72,
                            "3º EM" to 80
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(200.dp)
                    )
                }
            }
            
            Button(
                onClick = { /* TODO: Generate Full Report */ },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Gerar Relatório Completo")
            }
        }
    }
}
