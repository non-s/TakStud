@file:OptIn(ExperimentalMaterial3Api::class)

package com.example.takstud.ui.parent

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.takstud.model.AttendanceRecord
import com.example.takstud.model.Grade
import com.example.takstud.model.Notice
import com.example.takstud.model.Schedule
import com.example.takstud.model.Student
import com.example.takstud.model.Task
import com.example.takstud.ui.components.ExpandableCard
import com.example.takstud.ui.components.GradeIndicator
import com.example.takstud.ui.components.ProgressBarCard
import com.example.takstud.ui.components.StatisticCard
import com.example.takstud.ui.theme.AccentBlue
import com.example.takstud.ui.theme.DarkGray
import com.example.takstud.ui.theme.ErrorRed
import com.example.takstud.ui.theme.LightGray
import com.example.takstud.ui.theme.NavyBlue
import com.example.takstud.ui.theme.PureWhite
import com.example.takstud.ui.theme.SuccessGreen
import com.example.takstud.ui.theme.WarningYellow

/**
 * 👨‍👩‍👧 ParentScreenEnhanced - Dashboard moderno do responsável
 * Inclui visualização de dados com estatísticas, gráficos e cards expandíveis
 */
@Composable
fun ParentScreenEnhanced(
    modifier: Modifier = Modifier,
    student: Student,
    tasks: List<Task>,
    notices: List<Notice>,
    schedules: List<Schedule>,
    grades: List<Grade>,
    attendance: List<AttendanceRecord>,
    onLogout: () -> Unit,
    onSettings: () -> Unit = {},
    onScheduleClick: (Schedule) -> Unit = {},
    onNoticeClick: (Notice) -> Unit = {}
) {
    var showNoticeDialog by remember { mutableStateOf<Notice?>(null) }

    // Calcular estatísticas
    val averageGrade = if (grades.isNotEmpty()) {
        grades.mapNotNull { it.score.toDoubleOrNull() }.average().toFloat()
    } else {
        0f
    }

    val presentCount = attendance.count { it.isPresent }
    val attendancePercentage = if (attendance.isNotEmpty()) {
        (presentCount * 100) / attendance.size
    } else {
        0
    }

    val pendingTasks = tasks.count { task ->
        grades.find { it.taskId == task.id } == null
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = LightGray,
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            "👨‍👩‍👧 Responsável",
                            color = PureWhite,
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp
                        )
                        Text(
                            "${student.name}",
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
                            "Sair",
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
            // 📊 Resumo Rápido
            item {
                Text(
                    "📊 Desempenho Geral",
                    color = NavyBlue,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    modifier = Modifier.padding(horizontal = 4.dp)
                )
            }

            // Cards de estatísticas em grid
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    StatisticCard(
                        modifier = Modifier.weight(1f),
                        title = "Média de Notas",
                        value = String.format("%.1f", averageGrade),
                        unit = "",
                        icon = "📚",
                        backgroundColor = AccentBlue.copy(alpha = 0.1f)
                    )
                    StatisticCard(
                        modifier = Modifier.weight(1f),
                        title = "Presença",
                        value = attendancePercentage.toString(),
                        unit = "%",
                        icon = "✅",
                        backgroundColor = SuccessGreen.copy(alpha = 0.1f)
                    )
                }
            }

            item {
                StatisticCard(
                    title = "Tarefas Pendentes",
                    value = pendingTasks.toString(),
                    icon = "📋",
                    backgroundColor = WarningYellow.copy(alpha = 0.1f)
                )
            }

            // 📈 Gráfico de Progresso
            item {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    "📈 Progresso das Tarefas",
                    color = NavyBlue,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    modifier = Modifier.padding(horizontal = 4.dp)
                )
            }

            val completionRate = if (tasks.isNotEmpty()) {
                (tasks.size - pendingTasks) / tasks.size.toFloat()
            } else {
                0f
            }

            item {
                ProgressBarCard(
                    title = "Taxa de Conclusão",
                    progress = completionRate,
                    label = "${tasks.size - pendingTasks} de ${tasks.size} concluídas",
                    icon = "🎯"
                )
            }

            // 📚 Notas/Grades
            if (grades.isNotEmpty()) {
                item {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        "📚 Suas Notas",
                        color = NavyBlue,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        modifier = Modifier.padding(horizontal = 4.dp)
                    )
                }

                item {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 4.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        grades.take(3).forEach { grade ->
                            val gradeValue = grade.score.toFloatOrNull() ?: 0f
                            GradeIndicator(
                                grade = gradeValue,
                                label = "Tarefa ${grade.taskId.take(4)}"
                            )
                        }
                    }
                }
            }

            // 📅 Horários Próximos
            if (schedules.isNotEmpty()) {
                item {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        "📅 Próximas Aulas",
                        color = NavyBlue,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        modifier = Modifier.padding(horizontal = 4.dp)
                    )
                }

                items(schedules.take(3)) { schedule ->
                    ExpandableCard(
                        title = schedule.studentClass,
                        icon = "📅",
                        initiallyExpanded = false,
                        onClick = { onScheduleClick(schedule) }
                    ) {
                        Text(
                            text = "Turma: ${schedule.studentClass}",
                            color = DarkGray,
                            fontSize = 12.sp
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Consulte o calendário para detalhes",
                            color = DarkGray,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }

            // 📢 Avisos Importantes
            if (notices.isNotEmpty()) {
                item {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        "📢 Avisos Importantes",
                        color = NavyBlue,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        modifier = Modifier.padding(horizontal = 4.dp)
                    )
                }

                items(notices.take(3)) { notice ->
                    ExpandableCard(
                        title = notice.title,
                        icon = "📢",
                        initiallyExpanded = false,
                        onClick = { showNoticeDialog = notice }
                    ) {
                        Text(
                            text = notice.description,
                            color = DarkGray,
                            fontSize = 12.sp
                        )
                    }
                }
            }

            // ✅ Resumo de Presença
            if (attendance.isNotEmpty()) {
                item {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        "✅ Frequência",
                        color = NavyBlue,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        modifier = Modifier.padding(horizontal = 4.dp)
                    )
                }

                item {
                    ProgressBarCard(
                        title = "Taxa de Frequência",
                        progress = attendancePercentage / 100f,
                        label = "$presentCount de ${attendance.size} presenças",
                        icon = "✅"
                    )
                }
            }

            // 📋 Tarefas Pendentes
            if (pendingTasks > 0) {
                item {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        "📋 Tarefas Pendentes",
                        color = ErrorRed,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        modifier = Modifier.padding(horizontal = 4.dp)
                    )
                }

                val pendingTasksList = tasks.filter { task ->
                    grades.find { it.taskId == task.id } == null
                }

                items(pendingTasksList.take(3)) { task ->
                    ExpandableCard(
                        title = task.title,
                        icon = "📋",
                        initiallyExpanded = false
                    ) {
                        Text(
                            text = "Descrição: ${task.description}",
                            color = DarkGray,
                            fontSize = 11.sp
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Prazo: ${task.dueDate}",
                            color = ErrorRed,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }

            // Espaço final
            item {
                Spacer(modifier = Modifier.height(20.dp))
            }
        }
    }

    // Diálogo de aviso
    showNoticeDialog?.let { notice ->
        AlertDialog(
            onDismissRequest = { showNoticeDialog = null },
            title = {
                Text(
                    notice.title,
                    fontWeight = FontWeight.Bold,
                    color = NavyBlue,
                    fontSize = 18.sp
                )
            },
            text = {
                Text(
                    notice.description,
                    color = DarkGray,
                    fontSize = 14.sp
                )
            },
            confirmButton = {
                Button(
                    onClick = { showNoticeDialog = null },
                    colors = ButtonDefaults.buttonColors(containerColor = NavyBlue),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text("Ok", color = PureWhite)
                }
            },
            containerColor = PureWhite
        )
    }
}