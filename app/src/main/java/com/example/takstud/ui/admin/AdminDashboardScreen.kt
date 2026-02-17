package com.example.takstud.ui.admin

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.takstud.ui.components.*
import com.example.takstud.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminDashboardScreen(navController: NavController) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Painel Administrativo") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Voltar")
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
            // Cabeçalho
            Text(
                "Visão Geral da Escola",
                style = MaterialTheme.typography.headlineSmall,
                color = NavyBlue
            )
            Spacer(modifier = Modifier.height(8.dp))

            // Estatísticas Principais (Grid 2x2)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                StatisticCard(
                    title = "Total de Alunos",
                    value = "1.247",
                    icon = "👥",
                    backgroundColor = AccentBlue.copy(alpha = 0.1f),
                    modifier = Modifier.weight(1f)
                )
                StatisticCard(
                    title = "Professores",
                    value = "87",
                    icon = "👨‍🏫",
                    backgroundColor = SuccessGreen.copy(alpha = 0.1f),
                    modifier = Modifier.weight(1f)
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                StatisticCard(
                    title = "Turmas",
                    value = "42",
                    icon = "🏫",
                    backgroundColor = AccentPurple.copy(alpha = 0.1f),
                    modifier = Modifier.weight(1f)
                )
                StatisticCard(
                    title = "Taxa de Presença",
                    value = "94.2",
                    unit = "%",
                    icon = "📊",
                    backgroundColor = WarningYellow.copy(alpha = 0.1f),
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Desempenho Geral
            SectionCard(
                title = "Desempenho Geral",
                icon = "📈"
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    ProgressBarCard(
                        title = "Média de Notas da Escola",
                        progress = 0.78f,
                        label = "78% de aproveitamento",
                        icon = "📝"
                    )

                    ProgressBarCard(
                        title = "Taxa de Conclusão de Tarefas",
                        progress = 0.85f,
                        label = "85% das tarefas concluídas",
                        icon = "✅"
                    )

                    ProgressBarCard(
                        title = "Engajamento dos Professores",
                        progress = 0.92f,
                        label = "92% ativos na plataforma",
                        icon = "💼"
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Notas por Categoria
            SectionCard(
                title = "Distribuição de Desempenho",
                icon = "🎯"
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    GradeIndicator(grade = 92f, label = "Excelente")
                    GradeIndicator(grade = 78f, label = "Bom")
                    GradeIndicator(grade = 65f, label = "Regular")
                    GradeIndicator(grade = 48f, label = "Abaixo")
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Ações Rápidas
            ExpandableCard(
                title = "Ações Administrativas",
                icon = "⚡",
                initiallyExpanded = false
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    ActionCard(
                        title = "Gerenciar Usuários",
                        description = "Adicionar, editar ou remover usuários do sistema",
                        icon = "👤",
                        onClick = { /* TODO: Navigate */ }
                    )
                    ActionCard(
                        title = "Configurações da Escola",
                        description = "Alterar configurações gerais da instituição",
                        icon = "⚙️",
                        onClick = { /* TODO: Navigate */ }
                    )
                    ActionCard(
                        title = "Relatórios Gerais",
                        description = "Gerar relatórios consolidados de desempenho",
                        icon = "📄",
                        badge = "Novo",
                        onClick = { /* TODO: Navigate */ }
                    )
                    ActionCard(
                        title = "Backup de Dados",
                        description = "Fazer backup ou restaurar dados do sistema",
                        icon = "💾",
                        onClick = { /* TODO: Navigate */ }
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Alertas e Notificações
            ExpandableCard(
                title = "Alertas e Notificações",
                icon = "🔔",
                initiallyExpanded = false
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Row(
                        verticalAlignment = androidx.compose.ui.Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        NotificationBadge(count = 12, color = ErrorRed)
                        Spacer(modifier = Modifier.width(12.dp))
                        Text("Alunos com presença abaixo de 75%", style = MaterialTheme.typography.bodyMedium)
                    }

                    Row(
                        verticalAlignment = androidx.compose.ui.Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        NotificationBadge(count = 5, color = WarningYellow)
                        Spacer(modifier = Modifier.width(12.dp))
                        Text("Tarefas atrasadas sem lançamento de notas", style = MaterialTheme.typography.bodyMedium)
                    }

                    Row(
                        verticalAlignment = androidx.compose.ui.Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        NotificationBadge(count = 3, color = AccentBlue)
                        Spacer(modifier = Modifier.width(12.dp))
                        Text("Novos professores aguardando aprovação", style = MaterialTheme.typography.bodyMedium)
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}