@file:OptIn(ExperimentalMaterial3Api::class)

package com.example.takstud.ui.components.schedule

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.takstud.model.schedule.*
import com.example.takstud.ui.theme.*
import java.util.UUID

/**
 * ⏰ Diálogo de Adicionar/Editar TimeSlot
 * - Configuração completa de horários
 * - Seleção de disciplina
 * - Intervalos e eventos especiais
 * - Substituições
 */
@Composable
fun TimeSlotDialog(
    timeSlot: TimeSlot?,
    scheduleId: String,
    availableSubjects: List<Subject>,
    initialDay: DayOfWeek? = null,
    initialTime: String? = null,
    onDismiss: () -> Unit,
    onSave: (TimeSlot) -> Unit,
    modifier: Modifier = Modifier
) {
    var dayOfWeek by remember { mutableStateOf(timeSlot?.dayOfWeek ?: initialDay ?: DayOfWeek.MONDAY) }
    var startTime by remember { mutableStateOf(timeSlot?.startTime ?: initialTime ?: "07:00") }
    var endTime by remember { mutableStateOf(timeSlot?.endTime ?: "07:50") }
    var selectedSubject by remember { mutableStateOf<Subject?>(
        timeSlot?.let { slot ->
            availableSubjects.find { it.id == slot.subjectId }
        }
    ) }
    var classroom by remember { mutableStateOf(timeSlot?.classroom ?: "") }
    var isBreak by remember { mutableStateOf(timeSlot?.isBreak ?: false) }
    var isSpecialEvent by remember { mutableStateOf(timeSlot?.isSpecialEvent ?: false) }
    var eventTitle by remember { mutableStateOf(timeSlot?.eventTitle ?: "") }
    var notes by remember { mutableStateOf(timeSlot?.notes ?: "") }
    var isSubstitute by remember { mutableStateOf(timeSlot?.isSubstitute ?: false) }
    var substituteTeacher by remember { mutableStateOf(timeSlot?.substituteTeacher ?: "") }

    var showSubjectPicker by remember { mutableStateOf(false) }
    var errors by remember { mutableStateOf<Map<String, String>>(emptyMap()) }

    val isEditing = timeSlot != null

    AlertDialog(
        onDismissRequest = onDismiss,
        modifier = modifier.fillMaxWidth(),
        title = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    when {
                        isBreak -> Icons.Default.Coffee
                        isSpecialEvent -> Icons.Default.Event
                        else -> Icons.Default.Schedule
                    },
                    contentDescription = null,
                    tint = AccentBlue
                )
                Text(
                    if (isEditing) "Editar Horário" else "Novo Horário",
                    fontWeight = FontWeight.Bold
                )
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 500.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Tipo de horário
                Card(
                    colors = CardDefaults.cardColors(containerColor = LightGray.copy(alpha = 0.3f))
                ) {
                    Column(
                        modifier = Modifier.padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            "Tipo de Horário",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            FilterChip(
                                selected = !isBreak && !isSpecialEvent,
                                onClick = {
                                    isBreak = false
                                    isSpecialEvent = false
                                },
                                label = { Text("Aula") },
                                leadingIcon = { Icon(Icons.Default.MenuBook, contentDescription = null) },
                                modifier = Modifier.weight(1f)
                            )
                            FilterChip(
                                selected = isBreak,
                                onClick = {
                                    isBreak = true
                                    isSpecialEvent = false
                                },
                                label = { Text("Intervalo") },
                                leadingIcon = { Icon(Icons.Default.Coffee, contentDescription = null) },
                                modifier = Modifier.weight(1f)
                            )
                            FilterChip(
                                selected = isSpecialEvent,
                                onClick = {
                                    isBreak = false
                                    isSpecialEvent = true
                                },
                                label = { Text("Evento") },
                                leadingIcon = { Icon(Icons.Default.Event, contentDescription = null) },
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }

                // Dia da semana
                DaySelector(
                    selectedDay = dayOfWeek,
                    onDaySelect = { dayOfWeek = it }
                )

                // Horários
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    TimeField(
                        value = startTime,
                        onValueChange = { startTime = it },
                        label = "Início",
                        error = errors["startTime"],
                        modifier = Modifier.weight(1f)
                    )

                    Icon(
                        Icons.Default.ArrowForward,
                        contentDescription = null,
                        modifier = Modifier.align(Alignment.CenterVertically)
                    )

                    TimeField(
                        value = endTime,
                        onValueChange = { endTime = it },
                        label = "Fim",
                        error = errors["endTime"],
                        modifier = Modifier.weight(1f)
                    )
                }

                // Campos específicos por tipo
                when {
                    isBreak -> {
                        // Intervalo não precisa de mais campos
                        Text(
                            "💡 Intervalos não necessitam de informações adicionais",
                            style = MaterialTheme.typography.bodySmall,
                            color = DarkGray
                        )
                    }

                    isSpecialEvent -> {
                        // Evento especial
                        OutlinedTextField(
                            value = eventTitle,
                            onValueChange = { eventTitle = it },
                            label = { Text("Título do Evento") },
                            modifier = Modifier.fillMaxWidth(),
                            leadingIcon = { Icon(Icons.Default.Event, contentDescription = null) },
                            isError = errors.containsKey("eventTitle"),
                            supportingText = errors["eventTitle"]?.let { { Text(it) } }
                        )
                    }

                    else -> {
                        // Aula normal

                        // Seleção de disciplina
                        OutlinedCard(
                            onClick = { showSubjectPicker = true },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        "Disciplina",
                                        style = MaterialTheme.typography.labelMedium,
                                        color = DarkGray
                                    )
                                    Text(
                                        selectedSubject?.name ?: "Selecione uma disciplina",
                                        style = MaterialTheme.typography.bodyLarge,
                                        color = if (selectedSubject != null) NavyBlue else ErrorRed,
                                        fontWeight = if (selectedSubject != null) FontWeight.Medium else FontWeight.Normal
                                    )
                                }
                                Icon(
                                    Icons.Default.ChevronRight,
                                    contentDescription = null,
                                    tint = DarkGray
                                )
                            }
                        }

                        if (errors.containsKey("subject")) {
                            Text(
                                errors["subject"]!!,
                                style = MaterialTheme.typography.bodySmall,
                                color = ErrorRed
                            )
                        }

                        // Sala
                        OutlinedTextField(
                            value = classroom,
                            onValueChange = { classroom = it },
                            label = { Text("Sala/Local") },
                            modifier = Modifier.fillMaxWidth(),
                            leadingIcon = { Icon(Icons.Default.Room, contentDescription = null) },
                            placeholder = { Text(selectedSubject?.classroom ?: "Ex: Sala 101") }
                        )

                        // Professor substituto
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("Professor Substituto?")
                            Switch(
                                checked = isSubstitute,
                                onCheckedChange = { isSubstitute = it }
                            )
                        }

                        AnimatedVisibility(visible = isSubstitute) {
                            OutlinedTextField(
                                value = substituteTeacher,
                                onValueChange = { substituteTeacher = it },
                                label = { Text("Nome do Substituto") },
                                modifier = Modifier.fillMaxWidth(),
                                leadingIcon = { Icon(Icons.Default.SwapHoriz, contentDescription = null) }
                            )
                        }
                    }
                }

                // Observações (para todos os tipos)
                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = { Text("Observações") },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 2,
                    maxLines = 3,
                    leadingIcon = { Icon(Icons.Default.Notes, contentDescription = null) }
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    // Validação
                    val newErrors = mutableMapOf<String, String>()

                    if (startTime.isBlank()) newErrors["startTime"] = "Obrigatório"
                    if (endTime.isBlank()) newErrors["endTime"] = "Obrigatório"

                    if (!isBreak && !isSpecialEvent && selectedSubject == null) {
                        newErrors["subject"] = "Selecione uma disciplina"
                    }

                    if (isSpecialEvent && eventTitle.isBlank()) {
                        newErrors["eventTitle"] = "Título obrigatório"
                    }

                    if (newErrors.isNotEmpty()) {
                        errors = newErrors
                        return@Button
                    }

                    // Criar/atualizar TimeSlot
                    val newTimeSlot = TimeSlot(
                        id = timeSlot?.id ?: UUID.randomUUID().toString(),
                        scheduleId = scheduleId,
                        dayOfWeek = dayOfWeek,
                        startTime = startTime,
                        endTime = endTime,
                        subjectId = selectedSubject?.id ?: "",
                        subjectName = selectedSubject?.name ?: "",
                        subjectShortName = selectedSubject?.shortName ?: "",
                        teacherName = selectedSubject?.teacherName ?: "",
                        classroom = classroom.ifBlank { selectedSubject?.classroom ?: "" },
                        subjectColor = selectedSubject?.color ?: 0L,
                        isBreak = isBreak,
                        isSpecialEvent = isSpecialEvent,
                        eventTitle = eventTitle,
                        notes = notes,
                        isSubstitute = isSubstitute,
                        substituteTeacher = substituteTeacher,
                        createdAt = timeSlot?.createdAt ?: System.currentTimeMillis(),
                        updatedAt = System.currentTimeMillis()
                    )

                    onSave(newTimeSlot)
                },
                colors = ButtonDefaults.buttonColors(containerColor = AccentBlue)
            ) {
                Text(if (isEditing) "Salvar" else "Adicionar")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancelar")
            }
        }
    )

    // Diálogo de seleção de disciplina
    if (showSubjectPicker) {
        SubjectPickerDialog(
            subjects = availableSubjects,
            selectedSubject = selectedSubject,
            onDismiss = { showSubjectPicker = false },
            onSelect = { subject ->
                selectedSubject = subject
                if (classroom.isBlank()) {
                    classroom = subject.classroom
                }
                showSubjectPicker = false
            }
        )
    }
}

/**
 * 📅 Seletor de Dia da Semana
 */
@Composable
private fun DaySelector(
    selectedDay: DayOfWeek,
    onDaySelect: (DayOfWeek) -> Unit
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(
            "Dia da Semana",
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.Bold
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            DayOfWeek.weekDays().forEach { day ->
                FilterChip(
                    selected = day == selectedDay,
                    onClick = { onDaySelect(day) },
                    label = { Text(day.shortName) },
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

/**
 * ⏰ Campo de Horário
 */
@Composable
private fun TimeField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    error: String?,
    modifier: Modifier = Modifier
) {
    OutlinedTextField(
        value = value,
        onValueChange = { newValue ->
            // Validação e formatação de horário HH:mm
            val filtered = newValue.filter { it.isDigit() || it == ':' }
            if (filtered.length <= 5) {
                onValueChange(filtered)
            }
        },
        label = { Text(label) },
        modifier = modifier,
        isError = error != null,
        supportingText = error?.let { { Text(it) } },
        leadingIcon = { Icon(Icons.Default.AccessTime, contentDescription = null) },
        placeholder = { Text("00:00") },
        singleLine = true
    )
}

/**
 * 📚 Diálogo de Seleção de Disciplina
 */
@Composable
private fun SubjectPickerDialog(
    subjects: List<Subject>,
    selectedSubject: Subject?,
    onDismiss: () -> Unit,
    onSelect: (Subject) -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Selecionar Disciplina", fontWeight = FontWeight.Bold) },
        text = {
            if (subjects.isEmpty()) {
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
                            Icons.Default.MenuBook,
                            contentDescription = null,
                            tint = LightGray,
                            modifier = Modifier.size(48.dp)
                        )
                        Text(
                            "Nenhuma disciplina cadastrada",
                            style = MaterialTheme.typography.bodyMedium,
                            color = DarkGray
                        )
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 400.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(subjects) { subject ->
                        SubjectPickerItem(
                            subject = subject,
                            isSelected = subject.id == selectedSubject?.id,
                            onClick = { onSelect(subject) }
                        )
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Fechar")
            }
        }
    )
}

@Composable
private fun SubjectPickerItem(
    subject: Subject,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) {
                AccentBlue.copy(alpha = 0.2f)
            } else {
                LightGray.copy(alpha = 0.3f)
            }
        ),
        onClick = onClick
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Cor + Sigla
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(subject.getComposeColor()),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = subject.shortName,
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = androidx.compose.ui.graphics.Color.White
                )
            }

            // Info
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = subject.name,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Medium,
                    color = NavyBlue
                )
                if (subject.teacherName.isNotBlank()) {
                    Text(
                        text = subject.teacherName,
                        style = MaterialTheme.typography.bodySmall,
                        color = DarkGray
                    )
                }
            }

            // Check
            if (isSelected) {
                Icon(
                    Icons.Default.CheckCircle,
                    contentDescription = null,
                    tint = AccentBlue
                )
            }
        }
    }
}
