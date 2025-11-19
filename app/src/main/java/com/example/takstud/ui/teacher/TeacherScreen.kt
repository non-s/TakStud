@file:OptIn(ExperimentalMaterial3Api::class)

package com.example.takstud.ui.teacher

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.takstud.R
import com.example.takstud.ui.components.ActionCard
import com.example.takstud.ui.components.StatisticCard
import com.example.takstud.ui.theme.AccentBlue
import com.example.takstud.ui.theme.DarkGray
import com.example.takstud.ui.theme.LightGray
import com.example.takstud.ui.theme.NavyBlue
import com.example.takstud.ui.theme.PureWhite
import com.example.takstud.ui.theme.SuccessGreen

/**
 * 👨‍🏫 TeacherScreen - Dashboard moderno do professor com estatísticas e cards de ação
 * Inclui resumo rápido de dados importantes e navegação intuitiva
 */
@Composable
fun TeacherScreen(
    modifier: Modifier = Modifier,
    onManageTasks: () -> Unit,
    onManageNotices: () -> Unit,
    onManageSchedules: () -> Unit,
    onManageStudents: () -> Unit,
    onManageAttendance: () -> Unit,
    onLogout: () -> Unit,
    onSettings: () -> Unit = {},
    tasksCount: Int = 0,
    studentsCount: Int = 0,
    averageAttendance: Float = 0f
) {
    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = LightGray,
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            "👨‍🏫 ${stringResource(R.string.teacher_area)}",
                            color = PureWhite,
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp
                        )
                        Text(
                            "Dashboard",
                            color = PureWhite.copy(alpha = 0.7f),
                            fontSize = 12.sp
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = NavyBlue
                ),
                actions = {
                    IconButton(onClick = onSettings) {
                        Icon(
                            Icons.Default.Settings,
                            "Configurações",
                            tint = PureWhite
                        )
                    }
                    IconButton(onClick = onLogout) {
                        Icon(
                            Icons.AutoMirrored.Default.ExitToApp,
                            stringResource(R.string.logout),
                            tint = PureWhite
                        )
                    }
                }
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // 📊 Seção de Estatísticas Rápidas
            item {
                Text(
                    "📊 Resumo Rápido",
                    color = NavyBlue,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    modifier = Modifier.padding(horizontal = 4.dp)
                )
            }

            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    StatisticCard(
                        modifier = Modifier.weight(1f),
                        title = "Tarefas Ativas",
                        value = tasksCount.toString(),
                        icon = "📋",
                        backgroundColor = AccentBlue.copy(alpha = 0.1f),
                        onClick = onManageTasks
                    )
                    StatisticCard(
                        modifier = Modifier.weight(1f),
                        title = "Alunos",
                        value = studentsCount.toString(),
                        icon = "👥",
                        backgroundColor = SuccessGreen.copy(alpha = 0.1f),
                        onClick = onManageStudents
                    )
                }
            }

            item {
                StatisticCard(
                    title = "Presença Média",
                    value = String.format("%.1f", averageAttendance),
                    unit = "%",
                    icon = "✅",
                    backgroundColor = SuccessGreen.copy(alpha = 0.1f),
                    onClick = onManageAttendance
                )
            }

            // 🎯 Seção de Ações Principais
            item {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    "🎯 Ações Principais",
                    color = NavyBlue,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    modifier = Modifier.padding(horizontal = 4.dp)
                )
            }

            // 📋 Gerenciar Tarefas
            item {
                ActionCard(
                    title = "Gerenciar Tarefas",
                    description = "Crie, edite e acompanhe tarefas e avaliações da turma.",
                    icon = "📋",
                    badge = if (tasksCount > 0) "Ativo" else null,
                    onClick = onManageTasks
                )
            }

            // 👥 Gerenciar Alunos
            item {
                ActionCard(
                    title = "Gerenciar Alunos",
                    description = "Cadastre, edite e organize alunos nas suas turmas.",
                    icon = "👥",
                    onClick = onManageStudents
                )
            }

            // ✅ Gerenciar Presença
            item {
                ActionCard(
                    title = "Gerenciar Presença",
                    description = "Realize chamadas e acompanhe a frequência dos alunos.",
                    icon = "✅",
                    onClick = onManageAttendance
                )
            }

            // 📅 Gerenciar Horários
            item {
                ActionCard(
                    title = "Gerenciar Horários",
                    description = "Organize e mantenha a grade de aulas atualizada.",
                    icon = "📅",
                    onClick = onManageSchedules
                )
            }

            // 📢 Gerenciar Avisos
            item {
                ActionCard(
                    title = "Gerenciar Avisos",
                    description = "Envie comunicados importantes para os pais.",
                    icon = "📢",
                    onClick = onManageNotices
                )
            }

            // Espaço final
            item {
                Spacer(modifier = Modifier.height(20.dp))
            }
        }
    }
}

