@file:OptIn(ExperimentalMaterial3Api::class)

package com.example.takstud.ui.teacher.schedule

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.takstud.model.schedule.Subject
import com.example.takstud.ui.components.schedule.SubjectCard
import com.example.takstud.ui.theme.*
import java.util.UUID

/**
 * 📚 Tela de Gerenciamento de Disciplinas
 * - CRUD completo
 * - Cores personalizadas
 * - Busca e filtros
 * - Estatísticas
 */
@Composable
fun SubjectManagementScreen(
    subjects: List<Subject>,
    onSaveSubject: (Subject) -> Unit,
    onDeleteSubject: (String) -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    var showAddDialog by remember { mutableStateOf(false) }
    var selectedSubject by remember { mutableStateOf<Subject?>(null) }
    var searchQuery by remember { mutableStateOf("") }
    var showInactive by remember { mutableStateOf(false) }

    val filteredSubjects = remember(subjects, searchQuery, showInactive) {
        subjects.filter { subject ->
            val matchesSearch = subject.name.contains(searchQuery, ignoreCase = true) ||
                               subject.shortName.contains(searchQuery, ignoreCase = true) ||
                               subject.teacherName.contains(searchQuery, ignoreCase = true)
            val matchesStatus = showInactive || subject.isActive
            matchesSearch && matchesStatus
        }
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = { Text("Gerenciar Disciplinas", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Voltar")
                    }
                },
                actions = {
                    // Toggle mostrar inativos
                    IconButton(onClick = { showInactive = !showInactive }) {
                        Icon(
                            if (showInactive) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                            contentDescription = "Mostrar inativos",
                            tint = if (showInactive) WarningYellow else PureWhite
                        )
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
                onClick = {
                    selectedSubject = null
                    showAddDialog = true
                },
                icon = { Icon(Icons.Default.Add, contentDescription = null) },
                text = { Text("Nova Disciplina") },
                containerColor = AccentBlue
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            // Barra de busca
            SearchBar(
                query = searchQuery,
                onQueryChange = { searchQuery = it },
                modifier = Modifier.padding(16.dp)
            )

            // Estatísticas rápidas
            SubjectStats(
                total = subjects.size,
                active = subjects.count { it.isActive },
                totalWeeklyHours = subjects.filter { it.isActive }.sumOf { it.weeklyHours }
            )

            // Lista de disciplinas
            if (filteredSubjects.isEmpty()) {
                EmptySubjectsState(
                    hasSearch = searchQuery.isNotEmpty(),
                    onAddClick = {
                        selectedSubject = null
                        showAddDialog = true
                    }
                )
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    contentPadding = PaddingValues(vertical = 16.dp)
                ) {
                    items(filteredSubjects, key = { it.id }) { subject ->
                        SubjectCard(
                            subject = subject,
                            onEdit = {
                                selectedSubject = subject
                                showAddDialog = true
                            },
                            onDelete = { onDeleteSubject(subject.id) }
                        )
                    }
                }
            }
        }
    }

    // Diálogo de adicionar/editar
    if (showAddDialog) {
        SubjectDialog(
            subject = selectedSubject,
            onDismiss = {
                showAddDialog = false
                selectedSubject = null
            },
            onSave = { subject ->
                onSaveSubject(subject)
                showAddDialog = false
                selectedSubject = null
            }
        )
    }
}

/**
 * 🔍 Barra de Busca
 */
@Composable
private fun SearchBar(
    query: String,
    onQueryChange: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    OutlinedTextField(
        value = query,
        onValueChange = onQueryChange,
        modifier = modifier.fillMaxWidth(),
        placeholder = { Text("Buscar disciplinas...") },
        leadingIcon = {
            Icon(Icons.Default.Search, contentDescription = null)
        },
        trailingIcon = {
            if (query.isNotEmpty()) {
                IconButton(onClick = { onQueryChange("") }) {
                    Icon(Icons.Default.Close, contentDescription = "Limpar")
                }
            }
        },
        shape = RoundedCornerShape(12.dp),
        singleLine = true
    )
}

/**
 * 📊 Estatísticas de Disciplinas
 */
@Composable
private fun SubjectStats(
    total: Int,
    active: Int,
    totalWeeklyHours: Int
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = LightGray.copy(alpha = 0.3f))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            StatItem(
                value = total.toString(),
                label = "Total",
                icon = Icons.Default.MenuBook,
                color = AccentBlue
            )
            StatItem(
                value = active.toString(),
                label = "Ativas",
                icon = Icons.Default.CheckCircle,
                color = SuccessGreen
            )
            StatItem(
                value = "${totalWeeklyHours}h",
                label = "Carga Semanal",
                icon = Icons.Default.Schedule,
                color = WarningYellow
            )
        }
    }
}

@Composable
private fun StatItem(
    value: String,
    label: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    color: Color
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = color,
            modifier = Modifier.size(24.dp)
        )
        Text(
            text = value,
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = NavyBlue
        )
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = DarkGray
        )
    }
}

/**
 * 📭 Estado Vazio
 */
@Composable
private fun EmptySubjectsState(
    hasSearch: Boolean,
    onAddClick: () -> Unit
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
                if (hasSearch) Icons.Default.SearchOff else Icons.Default.MenuBook,
                contentDescription = null,
                tint = LightGray,
                modifier = Modifier.size(64.dp)
            )
            Text(
                text = if (hasSearch) "Nenhuma disciplina encontrada" else "Nenhuma disciplina cadastrada",
                style = MaterialTheme.typography.titleMedium,
                color = DarkGray,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )
            if (!hasSearch) {
                Button(
                    onClick = onAddClick,
                    colors = ButtonDefaults.buttonColors(containerColor = AccentBlue)
                ) {
                    Icon(Icons.Default.Add, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Criar primeira disciplina")
                }
            }
        }
    }
}

/**
 * 📝 Diálogo de Adicionar/Editar Disciplina
 */
@Composable
private fun SubjectDialog(
    subject: Subject?,
    onDismiss: () -> Unit,
    onSave: (Subject) -> Unit
) {
    var name by remember { mutableStateOf(subject?.name ?: "") }
    var shortName by remember { mutableStateOf(subject?.shortName ?: "") }
    var teacherName by remember { mutableStateOf(subject?.teacherName ?: "") }
    var classroom by remember { mutableStateOf(subject?.classroom ?: "") }
    var weeklyHours by remember { mutableStateOf(subject?.weeklyHours?.toString() ?: "") }
    var description by remember { mutableStateOf(subject?.description ?: "") }
    var selectedColor by remember { mutableStateOf(subject?.color ?: AccentBlue.value.toLong()) }
    var materials by remember { mutableStateOf(subject?.requiredMaterials?.joinToString("\n") ?: "") }

    var nameError by remember { mutableStateOf<String?>(null) }
    var shortNameError by remember { mutableStateOf<String?>(null) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                if (subject == null) "Nova Disciplina" else "Editar Disciplina",
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                item {
                    // Nome da disciplina
                    OutlinedTextField(
                        value = name,
                        onValueChange = {
                            name = it
                            nameError = if (it.isBlank()) "Campo obrigatório" else null
                        },
                        label = { Text("Nome da Disciplina *") },
                        isError = nameError != null,
                        supportingText = nameError?.let { { Text(it) } },
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                item {
                    // Nome curto (sigla)
                    OutlinedTextField(
                        value = shortName,
                        onValueChange = {
                            if (it.length <= 4) {
                                shortName = it.uppercase()
                                shortNameError = if (it.isBlank()) "Campo obrigatório" else null
                            }
                        },
                        label = { Text("Sigla (ex: MAT) *") },
                        isError = shortNameError != null,
                        supportingText = shortNameError?.let { { Text(it) } },
                        modifier = Modifier.fillMaxWidth(),
                        placeholder = { Text("Máx 4 letras") }
                    )
                }

                item {
                    // Professor
                    OutlinedTextField(
                        value = teacherName,
                        onValueChange = { teacherName = it },
                        label = { Text("Professor(a)") },
                        modifier = Modifier.fillMaxWidth(),
                        leadingIcon = { Icon(Icons.Default.Person, contentDescription = null) }
                    )
                }

                item {
                    // Sala
                    OutlinedTextField(
                        value = classroom,
                        onValueChange = { classroom = it },
                        label = { Text("Sala/Local") },
                        modifier = Modifier.fillMaxWidth(),
                        leadingIcon = { Icon(Icons.Default.Room, contentDescription = null) }
                    )
                }

                item {
                    // Carga horária
                    OutlinedTextField(
                        value = weeklyHours,
                        onValueChange = {
                            if (it.isEmpty() || it.toIntOrNull() != null) {
                                weeklyHours = it
                            }
                        },
                        label = { Text("Horas/Semana") },
                        modifier = Modifier.fillMaxWidth(),
                        leadingIcon = { Icon(Icons.Default.Schedule, contentDescription = null) },
                        suffix = { Text("h") }
                    )
                }

                item {
                    // Seletor de cor
                    Text(
                        "Cor da Disciplina",
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Bold
                    )
                    ColorSelector(
                        selectedColor = selectedColor,
                        onColorSelect = { selectedColor = it }
                    )
                }

                item {
                    // Descrição
                    OutlinedTextField(
                        value = description,
                        onValueChange = { description = it },
                        label = { Text("Descrição") },
                        modifier = Modifier.fillMaxWidth(),
                        minLines = 2,
                        maxLines = 3
                    )
                }

                item {
                    // Materiais necessários
                    OutlinedTextField(
                        value = materials,
                        onValueChange = { materials = it },
                        label = { Text("Materiais Necessários") },
                        modifier = Modifier.fillMaxWidth(),
                        minLines = 2,
                        maxLines = 3,
                        placeholder = { Text("Um por linha") },
                        supportingText = { Text("Separe por linhas") }
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    // Validação
                    if (name.isBlank()) {
                        nameError = "Campo obrigatório"
                        return@Button
                    }
                    if (shortName.isBlank()) {
                        shortNameError = "Campo obrigatório"
                        return@Button
                    }

                    val newSubject = Subject(
                        id = subject?.id ?: UUID.randomUUID().toString(),
                        name = name.trim(),
                        shortName = shortName.trim().uppercase(),
                        teacherName = teacherName.trim(),
                        teacherId = subject?.teacherId ?: "",
                        classroom = classroom.trim(),
                        color = selectedColor,
                        weeklyHours = weeklyHours.toIntOrNull() ?: 0,
                        description = description.trim(),
                        requiredMaterials = materials.split("\n")
                            .map { it.trim() }
                            .filter { it.isNotBlank() },
                        isActive = subject?.isActive ?: true,
                        createdAt = subject?.createdAt ?: System.currentTimeMillis(),
                        updatedAt = System.currentTimeMillis()
                    )

                    onSave(newSubject)
                },
                colors = ButtonDefaults.buttonColors(containerColor = AccentBlue)
            ) {
                Text(if (subject == null) "Criar" else "Salvar")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancelar")
            }
        }
    )
}

/**
 * 🎨 Seletor de Cores
 */
@Composable
private fun ColorSelector(
    selectedColor: Long,
    onColorSelect: (Long) -> Unit
) {
    val colors = listOf(
        Color(0xFF1976D2), // Blue
        Color(0xFF388E3C), // Green
        Color(0xFFD32F2F), // Red
        Color(0xFFF57C00), // Orange
        Color(0xFF7B1FA2), // Purple
        Color(0xFFC2185B), // Pink
        Color(0xFF0097A7), // Cyan
        Color(0xFF689F38), // Light Green
        Color(0xFFFBC02D), // Yellow
        Color(0xFF5D4037), // Brown
        Color(0xFF455A64), // Blue Grey
        Color(0xFF512DA8)  // Deep Purple
    )

    LazyRow(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        contentPadding = PaddingValues(vertical = 8.dp)
    ) {
        items(colors) { color ->
            ColorOption(
                color = color,
                isSelected = color.value.toLong() == selectedColor,
                onClick = { onColorSelect(color.value.toLong()) }
            )
        }
    }
}

@Composable
private fun ColorOption(
    color: Color,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .size(if (isSelected) 56.dp else 48.dp)
            .clip(CircleShape)
            .background(color)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        if (isSelected) {
            Icon(
                Icons.Default.Check,
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size(24.dp)
            )
        }
    }
}
