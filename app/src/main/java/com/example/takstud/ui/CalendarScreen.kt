package com.example.takstud.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.takstud.model.EventCalendar
import com.example.takstud.model.EventType
import com.example.takstud.model.ReminderTime
import com.example.takstud.ui.theme.*
import com.example.takstud.ui.calendar.WeeklyCalendarView
import com.example.takstud.viewmodel.CalendarViewModel
import com.example.takstud.viewmodel.CalendarViewMode
import java.text.SimpleDateFormat
import java.util.*

/**
 * CalendarScreen - Tela de Agenda Digital
 *
 * Funcionalidades:
 * - Visualização de calendário mensal
 * - Lista de eventos do dia
 * - Criação e edição de eventos
 * - Filtros por tipo e turma
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CalendarScreen(
    viewModel: CalendarViewModel = hiltViewModel(),
    onBack: () -> Unit = {}
) {
    val selectedDate by viewModel.selectedDate.collectAsState()
    val dayEvents by viewModel.dayEvents.collectAsState()
    val upcomingEvents by viewModel.upcomingEvents.collectAsState()
    val viewMode by viewModel.viewMode.collectAsState()
    val uiState by viewModel.uiState.collectAsState()

    var showAddEventDialog by remember { mutableStateOf(false) }
    var showEventDetails by remember { mutableStateOf(false) }
    val selectedEvent by viewModel.selectedEvent.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Agenda Digital",
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.Bold
                        )
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Voltar")
                    }
                },
                actions = {
                    IconButton(onClick = { viewModel.goToToday() }) {
                        Icon(Icons.Filled.Today, "Hoje")
                    }
                    IconButton(onClick = { showAddEventDialog = true }) {
                        Icon(Icons.Filled.Add, "Novo Evento")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = PrimaryBlue,
                    titleContentColor = Color.White,
                    navigationIconContentColor = Color.White,
                    actionIconContentColor = Color.White
                )
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showAddEventDialog = true },
                containerColor = AccentOrange
            ) {
                Icon(Icons.Filled.Add, "Adicionar Evento", tint = Color.White)
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(Neutral50)
        ) {
            // Navegação e seleção de modo de visualização
            CalendarNavigation(
                currentDate = selectedDate,
                viewMode = viewMode,
                onPrevious = {
                    if (viewMode == CalendarViewMode.MONTH) viewModel.previousMonth()
                    else viewModel.previousWeek()
                },
                onNext = {
                    if (viewMode == CalendarViewMode.MONTH) viewModel.nextMonth()
                    else viewModel.nextWeek()
                },
                onViewModeChange = { newMode -> viewModel.setViewMode(newMode) }
            )

            // Calendário
            AnimatedVisibility(visible = viewMode == CalendarViewMode.MONTH) {
                CalendarGrid(
                    currentDate = selectedDate,
                    events = dayEvents,
                    onDateSelected = { calendar ->
                        viewModel.selectDate(calendar)
                    },
                    hasEventsOnDate = { date ->
                        viewModel.hasEvents(date)
                    }
                )
            }

            AnimatedVisibility(visible = viewMode == CalendarViewMode.WEEK) {
                WeeklyCalendarView(
                    currentDate = selectedDate,
                    onDateSelected = { calendar ->
                        viewModel.selectDate(calendar)
                    },
                    hasEventsOnDate = { date ->
                        viewModel.hasEvents(date)
                    }
                )
            }

            HorizontalDivider(modifier = Modifier.padding(vertical = 16.dp))

            // Lista de eventos do dia
            Text(
                text = "Eventos do Dia",
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Bold
                ),
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                color = Neutral700
            )

            if (dayEvents.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Nenhum evento neste dia",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Neutral400
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(dayEvents) { event ->
                        EventCard(
                            event = event,
                            onClick = {
                                viewModel.selectEvent(event)
                                showEventDetails = true
                            }
                        )
                    }
                }
            }
        }
    }

    // Dialog de adicionar evento
    if (showAddEventDialog) {
        AddEventDialog(
            onDismiss = { showAddEventDialog = false },
            onConfirm = { event ->
                viewModel.createEvent(event)
                showAddEventDialog = false
            },
            selectedDate = selectedDate
        )
    }

    // Dialog de detalhes do evento
    if (showEventDetails && selectedEvent != null) {
        EventDetailsDialog(
            event = selectedEvent!!,
            onDismiss = {
                showEventDetails = false
                viewModel.selectEvent(null)
            },
            onDelete = {
                viewModel.deleteEvent(selectedEvent!!.id)
                showEventDetails = false
                viewModel.selectEvent(null)
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CalendarNavigation(
    currentDate: Calendar,
    viewMode: CalendarViewMode,
    onPrevious: () -> Unit,
    onNext: () -> Unit,
    onViewModeChange: (CalendarViewMode) -> Unit
) {
    val monthFormat = SimpleDateFormat("MMMM yyyy", Locale("pt", "BR"))
    val weekFormat = SimpleDateFormat("dd 'de' MMMM", Locale("pt", "BR"))

    val currentWeek = remember(currentDate) {
        val startOfWeek = currentDate.clone() as Calendar
        startOfWeek.set(Calendar.DAY_OF_WEEK, startOfWeek.firstDayOfWeek)
        val endOfWeek = startOfWeek.clone() as Calendar
        endOfWeek.add(Calendar.DAY_OF_WEEK, 6)
        "${weekFormat.format(startOfWeek.time)} - ${weekFormat.format(endOfWeek.time)}"
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onPrevious) {
                    Icon(Icons.Filled.ChevronLeft, "Anterior", tint = PrimaryBlue)
                }

                Text(
                    text = if (viewMode == CalendarViewMode.MONTH) {
                        monthFormat.format(currentDate.time).replaceFirstChar { it.uppercase() }
                    } else {
                        currentWeek
                    },
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold
                    ),
                    color = Neutral900,
                    textAlign = TextAlign.Center
                )

                IconButton(onClick = onNext) {
                    Icon(Icons.Filled.ChevronRight, "Próximo", tint = PrimaryBlue)
                }
            }

            SingleChoiceSegmentedButtonRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            ) {
                SegmentedButton(
                    selected = viewMode == CalendarViewMode.MONTH,
                    onClick = { onViewModeChange(CalendarViewMode.MONTH) },
                    shape = RoundedCornerShape(topStart = 50.dp, bottomStart = 50.dp)
                ) {
                    Text("Mês")
                }
                SegmentedButton(
                    selected = viewMode == CalendarViewMode.WEEK,
                    onClick = { onViewModeChange(CalendarViewMode.WEEK) },
                    shape = RoundedCornerShape(topEnd = 50.dp, bottomEnd = 50.dp)
                ) {
                    Text("Semana")
                }
            }
        }
    }
}

@Composable
fun CalendarGrid(
    currentDate: Calendar,
    events: List<EventCalendar>,
    onDateSelected: (Calendar) -> Unit,
    hasEventsOnDate: (Calendar) -> Boolean
) {
    val daysOfWeek = listOf("Dom", "Seg", "Ter", "Qua", "Qui", "Sex", "Sáb")

    Column(modifier = Modifier.padding(horizontal = 16.dp)) {
        // Cabeçalho dos dias da semana
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            daysOfWeek.forEach { day ->
                Text(
                    text = day,
                    style = MaterialTheme.typography.labelMedium.copy(
                        fontWeight = FontWeight.Bold
                    ),
                    modifier = Modifier.weight(1f),
                    textAlign = TextAlign.Center,
                    color = Neutral600
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Grid de dias
        val calendar = currentDate.clone() as Calendar
        calendar.set(Calendar.DAY_OF_MONTH, 1)
        val firstDayOfWeek = calendar.get(Calendar.DAY_OF_WEEK) - 1
        val daysInMonth = calendar.getActualMaximum(Calendar.DAY_OF_MONTH)

        val weeks = (firstDayOfWeek + daysInMonth + 6) / 7

        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            repeat(weeks) { week ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    repeat(7) { day ->
                        val dayNumber = week * 7 + day - firstDayOfWeek + 1
                        if (dayNumber in 1..daysInMonth) {
                            val dayCalendar = currentDate.clone() as Calendar
                            dayCalendar.set(Calendar.DAY_OF_MONTH, dayNumber)

                            DayCell(
                                day = dayNumber,
                                isSelected = dayNumber == currentDate.get(Calendar.DAY_OF_MONTH),
                                hasEvents = hasEventsOnDate(dayCalendar),
                                onClick = {
                                    onDateSelected(dayCalendar)
                                }
                            )
                        } else {
                            Spacer(modifier = Modifier.weight(1f))
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun RowScope.DayCell(
    day: Int,
    isSelected: Boolean,
    hasEvents: Boolean,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .weight(1f)
            .aspectRatio(1f)
            .clip(CircleShape)
            .background(
                when {
                    isSelected -> PrimaryBlue
                    else -> Color.Transparent
                }
            )
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = day.toString(),
                style = MaterialTheme.typography.bodyMedium,
                color = when {
                    isSelected -> Color.White
                    else -> Neutral900
                },
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
            )

            if (hasEvents && !isSelected) {
                Box(
                    modifier = Modifier
                        .size(4.dp)
                        .clip(CircleShape)
                        .background(AccentOrange)
                )
            }
        }
    }
}

@Composable
fun EventCard(
    event: EventCalendar,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Barra de cor do tipo de evento
            Box(
                modifier = Modifier
                    .width(4.dp)
                    .height(60.dp)
                    .clip(RoundedCornerShape(2.dp))
                    .background(Color(android.graphics.Color.parseColor(event.color)))
            )

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = event.title,
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold
                    ),
                    color = Neutral900
                )

                if (event.description.isNotEmpty()) {
                    Text(
                        text = event.description,
                        style = MaterialTheme.typography.bodySmall,
                        color = Neutral600,
                        maxLines = 2
                    )
                }

                Row(
                    modifier = Modifier.padding(top = 4.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    if (event.startTime.isNotEmpty()) {
                        Text(
                            text = "${event.startTime} - ${event.endTime}",
                            style = MaterialTheme.typography.labelSmall,
                            color = PrimaryBlue
                        )
                    }

                    Text(
                        text = event.eventType.displayName,
                        style = MaterialTheme.typography.labelSmall,
                        color = Neutral500
                    )
                }
            }

            Icon(
                imageVector = Icons.Filled.ChevronRight,
                contentDescription = "Ver detalhes",
                tint = Neutral400
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEventDialog(
    onDismiss: () -> Unit,
    onConfirm: (EventCalendar) -> Unit,
    selectedDate: Calendar
) {
    var title by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var selectedType by remember { mutableStateOf(EventType.OTHER) }
    var startTime by remember { mutableStateOf("") }
    var endTime by remember { mutableStateOf("") }
    var location by remember { mutableStateOf("") }
    var isAllDay by remember { mutableStateOf(false) }

    val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                "Novo Evento",
                style = MaterialTheme.typography.titleLarge.copy(
                    fontWeight = FontWeight.Bold
                )
            )
        },
        text = {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                item {
                    OutlinedTextField(
                        value = title,
                        onValueChange = { title = it },
                        label = { Text("Título") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                }

                item {
                    OutlinedTextField(
                        value = description,
                        onValueChange = { description = it },
                        label = { Text("Descrição") },
                        modifier = Modifier.fillMaxWidth(),
                        minLines = 3
                    )
                }

                item {
                    var expanded by remember { mutableStateOf(false) }

                    Box {
                        OutlinedTextField(
                            value = selectedType.displayName,
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Tipo de Evento") },
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { expanded = !expanded }
                        )

                        DropdownMenu(
                            expanded = expanded,
                            onDismissRequest = { expanded = false }
                        ) {
                            EventType.values().forEach { type ->
                                DropdownMenuItem(
                                    text = { Text(type.displayName) },
                                    onClick = {
                                        selectedType = type
                                        expanded = false
                                    }
                                )
                            }
                        }
                    }
                }

                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedTextField(
                            value = startTime,
                            onValueChange = { startTime = it },
                            label = { Text("Início") },
                            placeholder = { Text("HH:mm") },
                            modifier = Modifier.weight(1f),
                            enabled = !isAllDay
                        )

                        OutlinedTextField(
                            value = endTime,
                            onValueChange = { endTime = it },
                            label = { Text("Fim") },
                            placeholder = { Text("HH:mm") },
                            modifier = Modifier.weight(1f),
                            enabled = !isAllDay
                        )
                    }
                }

                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Checkbox(
                            checked = isAllDay,
                            onCheckedChange = { isAllDay = it }
                        )
                        Text("Dia inteiro")
                    }
                }

                item {
                    OutlinedTextField(
                        value = location,
                        onValueChange = { location = it },
                        label = { Text("Local") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val event = EventCalendar(
                        title = title,
                        description = description,
                        eventType = selectedType,
                        date = dateFormat.format(selectedDate.time),
                        startTime = if (isAllDay) "" else startTime,
                        endTime = if (isAllDay) "" else endTime,
                        location = location,
                        isAllDay = isAllDay
                    )
                    onConfirm(event)
                },
                enabled = title.isNotEmpty()
            ) {
                Text("Criar")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancelar")
            }
        }
    )
}

@Composable
fun EventDetailsDialog(
    event: EventCalendar,
    onDismiss: () -> Unit,
    onDelete: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                event.title,
                style = MaterialTheme.typography.titleLarge.copy(
                    fontWeight = FontWeight.Bold
                )
            )
        },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                if (event.description.isNotEmpty()) {
                    Text(
                        text = event.description,
                        style = MaterialTheme.typography.bodyMedium,
                        color = Neutral700
                    )
                }

                DetailRow(icon = Icons.Filled.Event, text = event.date)

                if (!event.isAllDay && event.startTime.isNotEmpty()) {
                    DetailRow(
                        icon = Icons.Filled.Schedule,
                        text = "${event.startTime} - ${event.endTime}"
                    )
                }

                DetailRow(icon = Icons.Filled.Category, text = event.eventType.displayName)

                if (event.location.isNotEmpty()) {
                    DetailRow(icon = Icons.Filled.LocationOn, text = event.location)
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Fechar")
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDelete,
                colors = ButtonDefaults.textButtonColors(
                    contentColor = MaterialTheme.colorScheme.error
                )
            ) {
                Text("Excluir")
            }
        }
    )
}

@Composable
fun DetailRow(icon: androidx.compose.ui.graphics.vector.ImageVector, text: String) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = PrimaryBlue,
            modifier = Modifier.size(20.dp)
        )
        Text(
            text = text,
            style = MaterialTheme.typography.bodyMedium,
            color = Neutral700
        )
    }
}
