@file:OptIn(ExperimentalMaterial3Api::class)

package com.example.takstud.ui.teacher.schedule

import androidx.compose.animation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.takstud.model.schedule.*
import com.example.takstud.ui.components.schedule.*
import com.example.takstud.ui.theme.*

/**
 * 📅 Tela Principal de Gerenciamento de Horários
 * - Visualizações múltiplas (Grade, Lista, Calendário)
 * - Gestão completa de horários
 * - Detecção de conflitos
 * - Templates
 * - Estatísticas
 */
@Composable
fun ScheduleManagementScreen(
    schedules: List<ClassSchedule>,
    subjects: List<Subject>,
    conflicts: List<ScheduleConflict>,
    onCreateSchedule: () -> Unit,
    onEditSchedule: (ClassSchedule) -> Unit,
    onDeleteSchedule: (ClassSchedule) -> Unit,
    onSelectSchedule: (ClassSchedule) -> Unit,
    onManageSubjects: () -> Unit,
    onExportSchedule: (ClassSchedule) -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedTabIndex by remember { mutableStateOf(0) }
    var selectedSchedule by remember { mutableStateOf<ClassSchedule?>(null) }
    var showFilters by remember { mutableStateOf(false) }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Gerenciamento de Horários",
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Voltar")
                    }
                },
                actions = {
                    // Filtros
                    IconButton(onClick = { showFilters = !showFilters }) {
                        Icon(Icons.Default.FilterList, contentDescription = "Filtros")
                    }

                    // Menu de ações
                    var showMenu by remember { mutableStateOf(false) }
                    Box {
                        IconButton(onClick = { showMenu = true }) {
                            Icon(Icons.Default.MoreVert, contentDescription = "Mais opções")
                        }
                        DropdownMenu(
                            expanded = showMenu,
                            onDismissRequest = { showMenu = false }
                        ) {
                            DropdownMenuItem(
                                text = { Text("Gerenciar Disciplinas") },
                                onClick = {
                                    showMenu = false
                                    onManageSubjects()
                                },
                                leadingIcon = {
                                    Icon(Icons.Default.MenuBook, contentDescription = null)
                                }
                            )
                            DropdownMenuItem(
                                text = { Text("Templates") },
                                onClick = {
                                    showMenu = false
                                    // TODO: Navegar para templates
                                },
                                leadingIcon = {
                                    Icon(Icons.Default.FileCopy, contentDescription = null)
                                }
                            )
                            DropdownMenuItem(
                                text = { Text("Configurações") },
                                onClick = {
                                    showMenu = false
                                    // TODO: Configurações
                                },
                                leadingIcon = {
                                    Icon(Icons.Default.Settings, contentDescription = null)
                                }
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = NavyBlue,
                    titleContentColor = PureWhite,
                    navigationIconContentColor = PureWhite,
                    actionIconContentColor = PureWhite
                )
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = onCreateSchedule,
                icon = { Icon(Icons.Default.Add, contentDescription = null) },
                text = { Text("Novo Horário") },
                containerColor = AccentBlue
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            // Abas de navegação
            TabRow(
                selectedTabIndex = selectedTabIndex,
                containerColor = NavyBlue,
                contentColor = PureWhite
            ) {
                Tab(
                    selected = selectedTabIndex == 0,
                    onClick = { selectedTabIndex = 0 },
                    text = { Text("Horários") },
                    icon = { Icon(Icons.Default.Schedule, contentDescription = null) }
                )
                Tab(
                    selected = selectedTabIndex == 1,
                    onClick = { selectedTabIndex = 1 },
                    text = { Text("Conflitos") },
                    icon = {
                        BadgedBox(
                            badge = {
                                if (conflicts.isNotEmpty()) {
                                    Badge { Text(conflicts.size.toString()) }
                                }
                            }
                        ) {
                            Icon(Icons.Default.Warning, contentDescription = null)
                        }
                    }
                )
                Tab(
                    selected = selectedTabIndex == 2,
                    onClick = { selectedTabIndex = 2 },
                    text = { Text("Estatísticas") },
                    icon = { Icon(Icons.Default.BarChart, contentDescription = null) }
                )
            }

            // Filtros expandíveis
            AnimatedVisibility(visible = showFilters) {
                FilterSection(
                    schedules = schedules,
                    onFilterApply = { /* TODO */ }
                )
            }

            // Conteúdo das abas
            when (selectedTabIndex) {
                0 -> SchedulesTabContent(
                    schedules = schedules,
                    subjects = subjects,
                    selectedSchedule = selectedSchedule,
                    onScheduleSelect = {
                        selectedSchedule = it
                        onSelectSchedule(it)
                    },
                    onEditSchedule = onEditSchedule,
                    onDeleteSchedule = onDeleteSchedule,
                    onExportSchedule = onExportSchedule
                )

                1 -> ConflictsTabContent(
                    conflicts = conflicts,
                    onResolveConflict = { /* TODO */ }
                )

                2 -> StatisticsTabContent(
                    schedules = schedules,
                    subjects = subjects
                )
            }
        }
    }
}

/**
 * Conteúdo da aba de Horários
 */
@Composable
private fun SchedulesTabContent(
    schedules: List<ClassSchedule>,
    subjects: List<Subject>,
    selectedSchedule: ClassSchedule?,
    onScheduleSelect: (ClassSchedule) -> Unit,
    onEditSchedule: (ClassSchedule) -> Unit,
    onDeleteSchedule: (ClassSchedule) -> Unit,
    onExportSchedule: (ClassSchedule) -> Unit
) {
    var viewMode by remember { mutableStateOf(ViewMode.LIST) }

    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        // Seletor de visualização
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            FilterChip(
                selected = viewMode == ViewMode.LIST,
                onClick = { viewMode = ViewMode.LIST },
                label = { Text("Lista") },
                leadingIcon = { Icon(Icons.Default.List, contentDescription = null) }
            )
            FilterChip(
                selected = viewMode == ViewMode.GRID,
                onClick = { viewMode = ViewMode.GRID },
                label = { Text("Grade") },
                leadingIcon = { Icon(Icons.Default.GridView, contentDescription = null) }
            )
            FilterChip(
                selected = viewMode == ViewMode.CALENDAR,
                onClick = { viewMode = ViewMode.CALENDAR },
                label = { Text("Calendário") },
                leadingIcon = { Icon(Icons.Default.CalendarToday, contentDescription = null) }
            )
        }

        // Conteúdo baseado no modo de visualização
        when (viewMode) {
            ViewMode.LIST -> ScheduleListView(
                schedules = schedules,
                onScheduleSelect = onScheduleSelect,
                onEditSchedule = onEditSchedule,
                onDeleteSchedule = onDeleteSchedule,
                onExportSchedule = onExportSchedule
            )

            ViewMode.GRID -> {
                if (selectedSchedule != null) {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        item {
                            WeeklyScheduleGrid(
                                schedule = selectedSchedule,
                                editable = false
                            )
                        }
                    }
                } else {
                    EmptyState(
                        icon = Icons.Default.Schedule,
                        message = "Selecione um horário para visualizar"
                    )
                }
            }

            ViewMode.CALENDAR -> {
                EmptyState(
                    icon = Icons.Default.CalendarToday,
                    message = "Visualização em calendário em breve"
                )
            }
        }
    }
}

/**
 * Lista de horários
 */
@Composable
private fun ScheduleListView(
    schedules: List<ClassSchedule>,
    onScheduleSelect: (ClassSchedule) -> Unit,
    onEditSchedule: (ClassSchedule) -> Unit,
    onDeleteSchedule: (ClassSchedule) -> Unit,
    onExportSchedule: (ClassSchedule) -> Unit
) {
    if (schedules.isEmpty()) {
        EmptyState(
            icon = Icons.Default.Schedule,
            message = "Nenhum horário cadastrado",
            action = "Crie seu primeiro horário clicando no botão abaixo"
        )
    } else {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(schedules, key = { it.id }) { schedule ->
                ScheduleCard(
                    schedule = schedule,
                    onSelect = { onScheduleSelect(schedule) },
                    onEdit = { onEditSchedule(schedule) },
                    onDelete = { onDeleteSchedule(schedule) },
                    onExport = { onExportSchedule(schedule) }
                )
            }
        }
    }
}

/**
 * Card de horário na lista
 */
@Composable
private fun ScheduleCard(
    schedule: ClassSchedule,
    onSelect: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    onExport: () -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    val stats = schedule.getStats()

    Card(
        modifier = Modifier
            .fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(2.dp),
        onClick = { expanded = !expanded }
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Cabeçalho
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = schedule.className,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = NavyBlue
                    )
                    Text(
                        text = "${schedule.period.name} • ${schedule.year}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = DarkGray
                    )
                }

                if (schedule.isTemplate) {
                    Chip(
                        label = "Template",
                        color = AccentBlue
                    )
                }
            }

            // Estatísticas rápidas
            ScheduleStatsCard(stats = stats)

            // Ações expandidas
            AnimatedVisibility(visible = expanded) {
                Column(
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Divider()

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedButton(
                            onClick = onSelect,
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(Icons.Default.Visibility, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Ver")
                        }

                        OutlinedButton(
                            onClick = onEdit,
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Editar")
                        }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedButton(
                            onClick = onExport,
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(Icons.Default.FileDownload, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Exportar")
                        }

                        OutlinedButton(
                            onClick = onDelete,
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = ErrorRed)
                        ) {
                            Icon(Icons.Default.Delete, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Excluir")
                        }
                    }
                }
            }
        }
    }
}

/**
 * Conteúdo da aba de Conflitos
 */
@Composable
private fun ConflictsTabContent(
    conflicts: List<ScheduleConflict>,
    onResolveConflict: (ScheduleConflict) -> Unit
) {
    if (conflicts.isEmpty()) {
        EmptyState(
            icon = Icons.Default.CheckCircle,
            message = "Nenhum conflito detectado",
            action = "Todas as grades estão configuradas corretamente!"
        )
    } else {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = ErrorRed.copy(alpha = 0.1f))
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Default.Warning,
                            contentDescription = null,
                            tint = ErrorRed,
                            modifier = Modifier.size(32.dp)
                        )
                        Column {
                            Text(
                                "${conflicts.size} ${if (conflicts.size == 1) "conflito encontrado" else "conflitos encontrados"}",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = ErrorRed
                            )
                            Text(
                                "Revise e resolva os conflitos abaixo",
                                style = MaterialTheme.typography.bodySmall,
                                color = DarkGray
                            )
                        }
                    }
                }
            }

            items(conflicts) { conflict ->
                ConflictCard(
                    conflict = conflict,
                    onResolve = { onResolveConflict(conflict) }
                )
            }
        }
    }
}

/**
 * Conteúdo da aba de Estatísticas
 */
@Composable
private fun StatisticsTabContent(
    schedules: List<ClassSchedule>,
    subjects: List<Subject>
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Text(
                "Estatísticas Gerais",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = NavyBlue
            )
        }

        item {
            OverallStatsCard(
                totalSchedules = schedules.size,
                totalSubjects = subjects.size,
                totalSlots = schedules.sumOf { it.timeSlots.size }
            )
        }

        // TODO: Adicionar mais gráficos e estatísticas
    }
}

@Composable
private fun OverallStatsCard(
    totalSchedules: Int,
    totalSubjects: Int,
    totalSlots: Int
) {
    // TODO: Implementar card de estatísticas gerais
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                "Resumo Geral",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            // Stats grid
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                StatItem(totalSchedules.toString(), "Horários")
                StatItem(totalSubjects.toString(), "Disciplinas")
                StatItem(totalSlots.toString(), "Aulas")
            }
        }
    }
}

@Composable
private fun StatItem(value: String, label: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            value,
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            color = AccentBlue
        )
        Text(
            label,
            style = MaterialTheme.typography.bodySmall,
            color = DarkGray
        )
    }
}

/**
 * Seção de filtros
 */
@Composable
private fun FilterSection(
    schedules: List<ClassSchedule>,
    onFilterApply: (FilterOptions) -> Unit
) {
    // TODO: Implementar filtros
    Surface(
        color = LightGray.copy(alpha = 0.3f),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text("Filtros", style = MaterialTheme.typography.titleMedium)
            // Adicionar controles de filtro aqui
        }
    }
}

/**
 * Estado vazio
 */
@Composable
private fun EmptyState(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    message: String,
    action: String = ""
) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier.padding(32.dp)
        ) {
            Icon(
                icon,
                contentDescription = null,
                tint = LightGray,
                modifier = Modifier.size(64.dp)
            )
            Text(
                message,
                style = MaterialTheme.typography.titleMedium,
                color = DarkGray,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )
            if (action.isNotEmpty()) {
                Text(
                    action,
                    style = MaterialTheme.typography.bodySmall,
                    color = DarkGray,
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                )
            }
        }
    }
}

enum class ViewMode {
    LIST, GRID, CALENDAR
}

data class FilterOptions(
    val period: String? = null,
    val year: Int? = null,
    val className: String? = null
)
