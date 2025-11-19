@file:OptIn(ExperimentalMaterial3Api::class)

package com.example.takstud.ui.parent

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.takstud.R
import com.example.takstud.model.AttendanceRecord
import com.example.takstud.model.Grade
import com.example.takstud.model.Notice
import com.example.takstud.model.Schedule
import com.example.takstud.model.Student
import com.example.takstud.model.Task
import com.example.takstud.ui.theme.AccentBlue
import com.example.takstud.ui.theme.DarkGray
import com.example.takstud.ui.theme.ErrorRed
import com.example.takstud.ui.theme.LightGray
import com.example.takstud.ui.theme.NavyBlue
import com.example.takstud.ui.theme.PureWhite
import com.example.takstud.ui.theme.SuccessGreen
import com.example.takstud.ui.theme.WarningYellow

/**
 * 👨‍👩‍👧 ParentScreen - Dashboard do responsável com design PROFISSIONAL
 * Exibe informações do aluno com cards e layout limpo.
 */
@Composable
fun ParentScreen(
    modifier: Modifier = Modifier,
    student: Student,
    tasks: List<Task>,
    notices: List<Notice>,
    schedules: List<Schedule>,
    grades: List<Grade>,
    attendance: List<AttendanceRecord>,
    onLogout: () -> Unit,
    onScheduleClick: (Schedule) -> Unit
) {
    var showNoticeDialog by remember { mutableStateOf<Notice?>(null) }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = PureWhite,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "👨‍👩‍👧 ${stringResource(R.string.parent_area)} - ${student.name}",
                        color = PureWhite,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = NavyBlue
                ),
                actions = {
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
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // 📅 Horários
            item {
                ParentSectionHeader("📅 Próximas Aulas")
            }
            if (schedules.isEmpty()) {
                item { Text(stringResource(R.string.no_schedules_for_class), color = DarkGray) }
            } else {
                items(schedules) { schedule ->
                    ParentScheduleCard(
                        schedule = schedule,
                        onClick = { onScheduleClick(schedule) }
                    )
                }
            }

            // 📢 Avisos
            item {
                ParentSectionHeader("📢 Avisos Importantes")
            }
            if (notices.isEmpty()) {
                item { Text(stringResource(R.string.no_notices_yet), color = DarkGray) }
            } else {
                items(notices) { notice ->
                    ParentNoticeCard(
                        notice = notice,
                        onClick = { showNoticeDialog = notice }
                    )
                }
            }

            // 📋 Tarefas
            item {
                ParentSectionHeader("📋 ${stringResource(R.string.tasks_and_tests_label)}")
            }
            if (tasks.isEmpty()) {
                item { Text(stringResource(R.string.no_tasks_yet), color = DarkGray) }
            } else {
                items(tasks) { task ->
                    val gradeRecord = grades.find { it.taskId == task.id }
                    ParentTaskCard(task = task, grade = gradeRecord?.score)
                }
            }

            // ✅ Presença
            item {
                ParentSectionHeader("✅ ${stringResource(R.string.attendance)}")
            }
            if (attendance.isEmpty()) {
                item { Text(stringResource(R.string.no_attendance_records), color = DarkGray) }
            } else {
                item {
                    val presentCount = attendance.count { it.isPresent }
                    val attendancePercentage = if (attendance.isNotEmpty()) (presentCount * 100) / attendance.size else 0
                    ParentAttendanceSummary(
                        total = attendance.size,
                        present = presentCount,
                        percentage = attendancePercentage
                    )
                }
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
                    color = NavyBlue
                )
            },
            text = {
                Text(notice.description, color = DarkGray)
            },
            confirmButton = {
                Button(
                    onClick = { showNoticeDialog = null },
                    colors = ButtonDefaults.buttonColors(containerColor = NavyBlue)
                ) {
                    Text(stringResource(R.string.ok), color = PureWhite)
                }
            },
            containerColor = PureWhite
        )
    }
}

@Composable
fun ParentSectionHeader(title: String) {
    Text(
        text = title,
        color = NavyBlue,
        fontWeight = FontWeight.Bold,
        fontSize = 18.sp,
        modifier = Modifier.padding(top = 8.dp, bottom = 8.dp)
    )
}

@Composable
fun ProfessionalCard(modifier: Modifier = Modifier, content: @Composable () -> Unit) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .border(1.dp, LightGray, RoundedCornerShape(8.dp)),
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(containerColor = PureWhite),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        content()
    }
}

@Composable
fun ProfessionalBadge(
    text: String,
    backgroundColor: Color,
    textColor: Color,
    modifier: Modifier = Modifier,
    emoji: String = ""
) {
    Box(
        modifier = modifier
            .background(backgroundColor, RoundedCornerShape(12.dp))
            .clip(RoundedCornerShape(12.dp))
            .padding(horizontal = 8.dp, vertical = 4.dp)
    ) {
        Text(
            text = if (emoji.isNotEmpty()) "$emoji $text" else text,
            color = textColor,
            fontSize = 12.sp,
            fontWeight = FontWeight.SemiBold
        )
    }
}

@Composable
fun ParentScheduleCard(schedule: Schedule, onClick: () -> Unit) {
    ProfessionalCard(modifier = Modifier.clickable { onClick() }) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "📅 ${schedule.studentClass}",
                    color = NavyBlue,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )
                ProfessionalBadge(
                    text = "Clique para ver",
                    backgroundColor = AccentBlue,
                    textColor = PureWhite,
                    emoji = "👁️"
                )
            }
            Text(
                text = "Toque para visualizar horários completos",
                color = DarkGray,
                fontSize = 12.sp,
                modifier = Modifier.padding(top = 8.dp)
            )
        }
    }
}

@Composable
fun ParentNoticeCard(notice: Notice, onClick: () -> Unit) {
    ProfessionalCard(modifier = Modifier.clickable { onClick() }) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = notice.title,
                color = NavyBlue,
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp,
                maxLines = 1
            )
            Text(
                text = notice.description,
                color = DarkGray,
                fontSize = 12.sp,
                modifier = Modifier.padding(top = 8.dp),
                maxLines = 2
            )
            ProfessionalBadge(
                text = "Leia mais",
                backgroundColor = AccentBlue,
                textColor = PureWhite,
                emoji = "📖",
                modifier = Modifier.padding(top = 8.dp)
            )
        }
    }
}

@Composable
fun ParentTaskCard(task: Task, grade: String?) {
    ProfessionalCard {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = task.title,
                    color = NavyBlue,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    modifier = Modifier.weight(1f)
                )
                if (grade != null && grade.isNotEmpty()) {
                    val gradeValue = grade.toDoubleOrNull() ?: 0.0
                    ProfessionalBadge(
                        text = "$grade/5.0",
                        backgroundColor = if (gradeValue >= 3) SuccessGreen else ErrorRed,
                        textColor = PureWhite,
                        emoji = if (gradeValue >= 3) "✅" else "❌"
                    )
                } else {
                    ProfessionalBadge(
                        text = "Sem nota",
                        backgroundColor = WarningYellow,
                        textColor = PureWhite,
                        emoji = "⏳"
                    )
                }
            }
            Text(
                text = "Prazo: ${task.dueDate}",
                color = DarkGray,
                fontSize = 12.sp,
                modifier = Modifier.padding(top = 8.dp)
            )
        }
    }
}

@Composable
fun ParentAttendanceSummary(total: Int, present: Int, percentage: Int) {
    ProfessionalCard {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Frequência Escolar",
                        color = NavyBlue,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                    )
                    Text(
                        text = "$present de $total aulas",
                        color = DarkGray,
                        fontSize = 12.sp,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }
                ProfessionalBadge(
                    text = "$percentage%",
                    backgroundColor = if (percentage >= 85) SuccessGreen else if (percentage >= 75) WarningYellow else ErrorRed,
                    textColor = PureWhite,
                    emoji = if (percentage >= 85) "🎉" else if (percentage >= 75) "⚠️" else "❌"
                )
            }
        }
    }
}