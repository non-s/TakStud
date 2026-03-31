@file:OptIn(ExperimentalMaterial3Api::class)

package com.example.takstud.ui.components.schedule

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.takstud.model.schedule.*
import com.example.takstud.ui.theme.*

/**
 * 📅 Grade Horária Semanal Completa
 * Exibe a grade em formato de tabela com dias da semana e horários
 */
@Composable
fun WeeklyScheduleGrid(
    schedule: ClassSchedule,
    modifier: Modifier = Modifier,
    onSlotClick: (TimeSlot) -> Unit = {},
    onAddSlot: (DayOfWeek, String) -> Unit = { _, _ -> },
    editable: Boolean = true,
    viewMode: ScheduleViewMode = ScheduleViewMode.WEEK
) {
    val days = when (viewMode) {
        ScheduleViewMode.WEEK -> DayOfWeek.weekDays()
        ScheduleViewMode.ALL_DAYS -> DayOfWeek.allDays()
    }

    // Obter todos os horários únicos ordenados
    val allTimeSlots = schedule.timeSlots + schedule.breaks
    val uniqueTimes = allTimeSlots
        .map { it.startTime to it.endTime }
        .distinct()
        .sortedBy { it.first }

    val horizontalScrollState = rememberScrollState()
    val verticalScrollState = rememberScrollState()

    Column(
        modifier = modifier
            .fillMaxWidth()
            .verticalScroll(verticalScrollState)
    ) {
        // Cabeçalho - Dias da Semana
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(horizontalScrollState)
                .background(NavyBlue.copy(alpha = 0.1f))
                .padding(vertical = 8.dp)
        ) {
            // Coluna de horários (vazia no cabeçalho)
            Box(
                modifier = Modifier
                    .width(80.dp)
                    .padding(4.dp)
            )

            // Colunas dos dias
            days.forEach { day ->
                DayHeader(
                    day = day,
                    modifier = Modifier.width(120.dp)
                )
            }
        }

        // Linhas de horários
        uniqueTimes.forEach { (startTime, endTime) ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(horizontalScrollState)
                    .border(
                        width = 0.5.dp,
                        color = LightGray,
                        shape = RoundedCornerShape(0.dp)
                    )
            ) {
                // Coluna de horário
                TimeLabel(
                    startTime = startTime,
                    endTime = endTime,
                    modifier = Modifier
                        .width(80.dp)
                        .padding(8.dp)
                )

                // Células de cada dia
                days.forEach { day ->
                    val slot = schedule.getAllSlotsByDay(day)
                        .find { it.startTime == startTime && it.endTime == endTime }

                    ScheduleCell(
                        timeSlot = slot,
                        day = day,
                        timeRange = "$startTime-$endTime",
                        onSlotClick = { if (slot != null) onSlotClick(slot) },
                        onAddClick = { if (editable) onAddSlot(day, startTime) },
                        modifier = Modifier.width(120.dp)
                    )
                }
            }
        }

        // Botão para adicionar novo horário
        if (editable) {
            OutlinedButton(
                onClick = { /* TODO: Mostrar diálogo para adicionar horário */ },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Icon(Icons.Default.Add, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Adicionar Novo Horário")
            }
        }
    }
}

/**
 * Cabeçalho do dia da semana
 */
@Composable
private fun DayHeader(
    day: DayOfWeek,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .padding(4.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(NavyBlue)
            .padding(8.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = day.shortName,
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                textAlign = TextAlign.Center
            )
            Text(
                text = day.displayName.split("-")[0],
                style = MaterialTheme.typography.bodySmall,
                color = Color.White.copy(alpha = 0.7f),
                textAlign = TextAlign.Center,
                fontSize = 10.sp
            )
        }
    }
}

/**
 * Label de horário
 */
@Composable
private fun TimeLabel(
    startTime: String,
    endTime: String,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = startTime,
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold,
                color = NavyBlue,
                fontSize = 11.sp
            )
            Text(
                text = "↓",
                fontSize = 8.sp,
                color = DarkGray
            )
            Text(
                text = endTime,
                style = MaterialTheme.typography.labelSmall,
                color = DarkGray,
                fontSize = 11.sp
            )
        }
    }
}

/**
 * Célula da grade (pode conter aula ou estar vazia)
 */
@Composable
private fun ScheduleCell(
    timeSlot: TimeSlot?,
    day: DayOfWeek,
    timeRange: String,
    onSlotClick: () -> Unit,
    onAddClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .height(100.dp)
            .padding(2.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(
                if (timeSlot != null) {
                    when {
                        timeSlot.isBreak -> WarningYellow.copy(alpha = 0.2f)
                        timeSlot.isSpecialEvent -> Color(0xFFFF6B6B).copy(alpha = 0.2f)
                        else -> Color(timeSlot.subjectColor.toULong()).copy(alpha = 0.2f)
                    }
                } else {
                    LightGray.copy(alpha = 0.1f)
                }
            )
            .border(
                width = 1.dp,
                color = if (timeSlot != null) {
                    when {
                        timeSlot.isBreak -> WarningYellow
                        timeSlot.isSpecialEvent -> Color(0xFFFF6B6B)
                        else -> Color(timeSlot.subjectColor.toULong())
                    }
                } else {
                    LightGray
                },
                shape = RoundedCornerShape(8.dp)
            )
            .clickable {
                if (timeSlot != null) onSlotClick() else onAddClick()
            }
            .padding(8.dp),
        contentAlignment = Alignment.Center
    ) {
        if (timeSlot != null) {
            // Exibir informações da aula
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
                modifier = Modifier.fillMaxSize()
            ) {
                // Nome curto ou ícone
                if (timeSlot.isBreak) {
                    Icon(
                        imageVector = Icons.Default.Coffee,
                        contentDescription = null,
                        tint = WarningYellow,
                        modifier = Modifier.size(24.dp)
                    )
                    Text(
                        text = "Intervalo",
                        style = MaterialTheme.typography.labelSmall,
                        color = DarkGray,
                        textAlign = TextAlign.Center,
                        fontSize = 10.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                } else if (timeSlot.isSpecialEvent) {
                    Icon(
                        imageVector = Icons.Default.Event,
                        contentDescription = null,
                        tint = Color(0xFFFF6B6B),
                        modifier = Modifier.size(24.dp)
                    )
                    Text(
                        text = timeSlot.eventTitle,
                        style = MaterialTheme.typography.labelSmall,
                        color = DarkGray,
                        textAlign = TextAlign.Center,
                        fontSize = 10.sp,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                } else {
                    // Disciplina normal
                    Text(
                        text = timeSlot.subjectShortName.ifBlank { timeSlot.subjectName.take(3) },
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color(timeSlot.subjectColor.toULong()),
                        textAlign = TextAlign.Center,
                        maxLines = 1
                    )

                    if (timeSlot.classroom.isNotBlank()) {
                        Text(
                            text = timeSlot.classroom,
                            style = MaterialTheme.typography.labelSmall,
                            color = DarkGray,
                            textAlign = TextAlign.Center,
                            fontSize = 9.sp,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }

                    if (timeSlot.isSubstitute) {
                        Icon(
                            imageVector = Icons.Default.SwapHoriz,
                            contentDescription = "Substituto",
                            tint = WarningYellow,
                            modifier = Modifier.size(12.dp)
                        )
                    }
                }
            }
        } else {
            // Célula vazia - Mostrar botão de adicionar
            Icon(
                imageVector = Icons.Default.Add,
                contentDescription = "Adicionar aula",
                tint = LightGray,
                modifier = Modifier.size(24.dp)
            )
        }
    }
}

/**
 * Modos de visualização da grade
 */
enum class ScheduleViewMode {
    WEEK,       // Segunda a Sexta
    ALL_DAYS    // Todos os dias incluindo fim de semana
}

/**
 * 📱 Visualização Compacta (Lista por Dia)
 */
@Composable
fun DailyScheduleView(
    schedule: ClassSchedule,
    selectedDay: DayOfWeek,
    modifier: Modifier = Modifier,
    onSlotClick: (TimeSlot) -> Unit = {}
) {
    val daySlots = schedule.getAllSlotsByDay(selectedDay)

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // Cabeçalho do dia
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = NavyBlue)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = selectedDay.displayName,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
                Text(
                    text = "${daySlots.size} ${if (daySlots.size == 1) "aula" else "aulas"}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.White.copy(alpha = 0.7f)
                )
            }
        }

        // Lista de slots
        if (daySlots.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(32.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.CalendarToday,
                        contentDescription = null,
                        tint = LightGray,
                        modifier = Modifier.size(48.dp)
                    )
                    Text(
                        text = "Nenhuma aula agendada",
                        style = MaterialTheme.typography.bodyLarge,
                        color = DarkGray
                    )
                }
            }
        } else {
            daySlots.forEach { slot ->
                TimeSlotCard(
                    timeSlot = slot,
                    onClick = { onSlotClick(slot) },
                    showActions = false
                )
            }
        }
    }
}
