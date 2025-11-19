@file:OptIn(ExperimentalMaterial3Api::class)

package com.example.takstud.ui.calendar

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.DateTimeFormatter

/**
 * CalendarScreen - Tela de calendário para visualizar aulas e tarefas
 */
@Composable
fun CalendarScreen(
    modifier: Modifier = Modifier,
    onBack: () -> Unit,
    onDateSelected: (LocalDate) -> Unit = {}
) {
    var currentMonth by remember { mutableStateOf(YearMonth.now()) }
    var selectedDate by remember { mutableStateOf<LocalDate?>(null) }

    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = { Text("Calendário") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Default.ArrowBack, "Voltar")
                    }
                }
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                CalendarMonthView(
                    yearMonth = currentMonth,
                    selectedDate = selectedDate,
                    onPreviousMonth = { currentMonth = currentMonth.minusMonths(1) },
                    onNextMonth = { currentMonth = currentMonth.plusMonths(1) },
                    onDateSelected = { date ->
                        selectedDate = date
                        onDateSelected(date)
                    }
                )
            }

            if (selectedDate != null) {
                item {
                    SelectedDateInfo(date = selectedDate!!)
                }
            }

            item {
                UpcomingEventsPreview()
            }
        }
    }
}

/**
 * CalendarMonthView - Vista mensal do calendário
 */
@Composable
fun CalendarMonthView(
    yearMonth: YearMonth,
    selectedDate: LocalDate?,
    onPreviousMonth: () -> Unit,
    onNextMonth: () -> Unit,
    onDateSelected: (LocalDate) -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.medium
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // Cabeçalho com navegação
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onPreviousMonth) {
                    Icon(Icons.Default.ChevronLeft, "Mês anterior")
                }

                Text(
                    text = yearMonth.format(DateTimeFormatter.ofPattern("MMMM yyyy")),
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.weight(1f)
                )

                IconButton(onClick = onNextMonth) {
                    Icon(Icons.Default.ChevronRight, "Próximo mês")
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Dias da semana (cabeçalho)
            val daysOfWeek = listOf("Dom", "Seg", "Ter", "Qua", "Qui", "Sex", "Sab")
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                daysOfWeek.forEach { day ->
                    Text(
                        text = day,
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier
                            .weight(1f)
                            .padding(8.dp),
                        textAlign = TextAlign.Center,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Dias do mês
            val firstDayOfMonth = LocalDate.of(yearMonth.year, yearMonth.month, 1)
            val firstDayOfWeek = firstDayOfMonth.dayOfWeek.value % 7 // 0 = domingo
            val daysInMonth = yearMonth.lengthOfMonth()

            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                var dayCount = 1
                repeat((firstDayOfWeek + daysInMonth + 6) / 7) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        repeat(7) {
                            if (dayCount <= firstDayOfWeek || dayCount > firstDayOfWeek + daysInMonth) {
                                // Dia vazio
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .aspectRatio(1f)
                                )
                            } else {
                                val day = dayCount - firstDayOfWeek
                                val date = LocalDate.of(yearMonth.year, yearMonth.month, day)
                                val isSelected = selectedDate == date
                                val isToday = date == LocalDate.now()

                                CalendarDayCell(
                                    day = day,
                                    isSelected = isSelected,
                                    isToday = isToday,
                                    hasEvent = hasEventOnDate(date),
                                    modifier = Modifier
                                        .weight(1f)
                                        .aspectRatio(1f),
                                    onClick = { onDateSelected(date) }
                                )
                            }
                            dayCount++
                        }
                    }
                }
            }
        }
    }
}

/**
 * CalendarDayCell - Célula de um dia do calendário
 */
@Composable
fun CalendarDayCell(
    day: Int,
    isSelected: Boolean,
    isToday: Boolean,
    hasEvent: Boolean,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Surface(
        modifier = modifier
            .clickable { onClick() }
            .padding(2.dp),
        color = when {
            isSelected -> MaterialTheme.colorScheme.primary
            isToday -> MaterialTheme.colorScheme.secondaryContainer
            else -> MaterialTheme.colorScheme.surface
        },
        shape = MaterialTheme.shapes.small,
        border = if (isToday) androidx.compose.foundation.BorderStroke(
            1.dp,
            MaterialTheme.colorScheme.secondary
        ) else null
    ) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
                modifier = Modifier.fillMaxSize()
            ) {
                Text(
                    text = day.toString(),
                    style = MaterialTheme.typography.labelMedium,
                    color = when {
                        isSelected -> MaterialTheme.colorScheme.onPrimary
                        isToday -> MaterialTheme.colorScheme.onSecondaryContainer
                        else -> MaterialTheme.colorScheme.onSurface
                    },
                    fontWeight = if (isToday) FontWeight.Bold else FontWeight.Normal
                )

                if (hasEvent) {
                    Box(
                        modifier = Modifier
                            .size(4.dp)
                            .background(
                                color = MaterialTheme.colorScheme.tertiary,
                                shape = MaterialTheme.shapes.small
                            )
                    )
                }
            }
        }
    }
}

/**
 * SelectedDateInfo - Informações da data selecionada
 */
@Composable
fun SelectedDateInfo(
    date: LocalDate,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.medium,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = "Data selecionada",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onPrimaryContainer
            )
            Text(
                text = date.format(DateTimeFormatter.ofPattern("d 'de' MMMM 'de' yyyy")),
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onPrimaryContainer
            )
            Text(
                text = date.format(DateTimeFormatter.ofPattern("EEEE")),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
            )
        }
    }
}

/**
 * UpcomingEventsPreview - Prévia de eventos próximos
 */
@Composable
fun UpcomingEventsPreview(
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.medium
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = "Próximos eventos",
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.primary
            )

            UpcomingEventItem(
                title = "Prova de Matemática",
                date = "15 de dezembro",
                type = "Tarefa"
            )

            UpcomingEventItem(
                title = "Aviso: Reunião de Pais",
                date = "18 de dezembro",
                type = "Aviso"
            )

            UpcomingEventItem(
                title = "Aula de Português",
                date = "20 de dezembro",
                type = "Aula"
            )
        }
    }
}

/**
 * UpcomingEventItem - Item de evento próximo
 */
@Composable
fun UpcomingEventItem(
    title: String,
    date: String,
    type: String,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(8.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Surface(
            shape = MaterialTheme.shapes.small,
            color = MaterialTheme.colorScheme.tertiaryContainer,
            modifier = Modifier.size(8.dp)
        ) {}

        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.labelMedium
            )
            Text(
                text = date,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        Text(
            text = type,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

/**
 * Verificar se uma data tem evento (simulado)
 */
private fun hasEventOnDate(date: LocalDate): Boolean {
    val eventsWithDates = setOf(
        LocalDate.of(date.year, date.month, 15), // Evento no dia 15
        LocalDate.of(date.year, date.month, 18), // Evento no dia 18
        LocalDate.of(date.year, date.month, 20)  // Evento no dia 20
    )
    return date in eventsWithDates
}
