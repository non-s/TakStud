@file:OptIn(ExperimentalMaterial3Api::class)

package com.example.takstud.ui.teacher

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.takstud.model.Schedule
import com.example.takstud.model.Task
import com.example.takstud.ui.theme.*
import com.example.takstud.util.InputValidator

/**
 * AddTaskScreen Premium - Criação/edição de tarefas moderna
 */
@Composable
fun AddTaskScreen(
    modifier: Modifier = Modifier,
    taskToEdit: Task?,
    schedules: List<Schedule>,
    onSave: (Task) -> Unit,
    onBack: () -> Unit
) {
    var title by remember(taskToEdit) { mutableStateOf(taskToEdit?.title ?: "") }
    var description by remember(taskToEdit) { mutableStateOf(taskToEdit?.description ?: "") }
    var dueDate by remember(taskToEdit) { mutableStateOf(taskToEdit?.dueDate ?: "") }
    var studentClass by remember(taskToEdit) { mutableStateOf(taskToEdit?.studentClass ?: "") }
    var expanded by remember { mutableStateOf(false) }

    // Validation errors
    var titleError by remember { mutableStateOf<String?>(null) }
    var descriptionError by remember { mutableStateOf<String?>(null) }
    var dueDateError by remember { mutableStateOf<String?>(null) }
    var classError by remember { mutableStateOf<String?>(null) }

    val isEditing = taskToEdit != null

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        AccentPurple.copy(alpha = 0.03f),
                        Neutral50
                    )
                )
            )
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            // Header
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        brush = Brush.verticalGradient(
                            colors = listOf(AccentPurple, Color(0xFF7C3AED))
                        )
                    )
                    .padding(16.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = onBack) {
                        Icon(
                            Icons.AutoMirrored.Default.ArrowBack,
                            "Voltar",
                            tint = Color.White
                        )
                    }
                    Column {
                        Text(
                            text = if (isEditing) "Editar Tarefa" else "Nova Tarefa",
                            style = MaterialTheme.typography.headlineSmall,
                            color = Color.White,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = if (isEditing) "Atualize as informações" else "Preencha os dados",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.White.copy(alpha = 0.8f)
                        )
                    }
                }
            }

            // Form
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Título
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            "Título da Tarefa",
                            style = MaterialTheme.typography.labelLarge,
                            color = Neutral700,
                            fontWeight = FontWeight.SemiBold
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        OutlinedTextField(
                            value = title,
                            onValueChange = {
                                title = it
                                titleError = if (it.isNotEmpty() && !InputValidator.isValidTitle(it)) {
                                    "Título deve ter 3-200 caracteres"
                                } else null
                            },
                            modifier = Modifier.fillMaxWidth(),
                            placeholder = { Text("Ex: Trabalho de Matemática") },
                            isError = titleError != null,
                            shape = RoundedCornerShape(12.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = AccentPurple,
                                unfocusedBorderColor = Neutral300
                            )
                        )
                        AnimatedVisibility(visible = titleError != null) {
                            Text(
                                text = titleError ?: "",
                                color = Error,
                                style = MaterialTheme.typography.bodySmall,
                                modifier = Modifier.padding(top = 4.dp)
                            )
                        }
                    }
                }

                // Descrição
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            "Descrição",
                            style = MaterialTheme.typography.labelLarge,
                            color = Neutral700,
                            fontWeight = FontWeight.SemiBold
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        OutlinedTextField(
                            value = description,
                            onValueChange = {
                                description = it
                                descriptionError = if (it.isNotEmpty() && !InputValidator.isValidDescription(it)) {
                                    "Descrição deve ter no máximo 5000 caracteres"
                                } else null
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(120.dp),
                            placeholder = { Text("Descreva a tarefa...") },
                            isError = descriptionError != null,
                            shape = RoundedCornerShape(12.dp),
                            maxLines = 5,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = AccentPurple,
                                unfocusedBorderColor = Neutral300
                            )
                        )
                        AnimatedVisibility(visible = descriptionError != null) {
                            Text(
                                text = descriptionError ?: "",
                                color = Error,
                                style = MaterialTheme.typography.bodySmall,
                                modifier = Modifier.padding(top = 4.dp)
                            )
                        }
                    }
                }

                // Data de Entrega
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            "Data de Entrega",
                            style = MaterialTheme.typography.labelLarge,
                            color = Neutral700,
                            fontWeight = FontWeight.SemiBold
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        OutlinedTextField(
                            value = dueDate,
                            onValueChange = {
                                dueDate = it
                                dueDateError = if (it.isNotEmpty() && !InputValidator.isValidDate(it)) {
                                    "Formato: dd/MM/yyyy"
                                } else null
                            },
                            modifier = Modifier.fillMaxWidth(),
                            placeholder = { Text("dd/MM/yyyy") },
                            isError = dueDateError != null,
                            shape = RoundedCornerShape(12.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = AccentPurple,
                                unfocusedBorderColor = Neutral300
                            )
                        )
                        AnimatedVisibility(visible = dueDateError != null) {
                            Text(
                                text = dueDateError ?: "",
                                color = Error,
                                style = MaterialTheme.typography.bodySmall,
                                modifier = Modifier.padding(top = 4.dp)
                            )
                        }
                    }
                }

                // Turma
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            "Turma",
                            style = MaterialTheme.typography.labelLarge,
                            color = Neutral700,
                            fontWeight = FontWeight.SemiBold
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Box {
                            OutlinedTextField(
                                value = studentClass,
                                onValueChange = { },
                                modifier = Modifier.fillMaxWidth(),
                                placeholder = { Text("Selecione a turma") },
                                isError = classError != null,
                                readOnly = true,
                                shape = RoundedCornerShape(12.dp),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = AccentPurple,
                                    unfocusedBorderColor = Neutral300
                                )
                            )
                            // Box clicável transparente sobre o TextField
                            Box(
                                modifier = Modifier
                                    .matchParentSize()
                                    .clickable { expanded = true }
                            )
                            DropdownMenu(
                                expanded = expanded,
                                onDismissRequest = { expanded = false }
                            ) {
                                if (schedules.isEmpty()) {
                                    DropdownMenuItem(
                                        text = { Text("Nenhuma turma cadastrada") },
                                        onClick = { expanded = false }
                                    )
                                } else {
                                    schedules.forEach { schedule ->
                                        DropdownMenuItem(
                                            text = { Text(schedule.studentClass) },
                                            onClick = {
                                                studentClass = schedule.studentClass
                                                classError = null
                                                expanded = false
                                            }
                                        )
                                    }
                                }
                            }
                        }
                        AnimatedVisibility(visible = classError != null) {
                            Text(
                                text = classError ?: "",
                                color = Error,
                                style = MaterialTheme.typography.bodySmall,
                                modifier = Modifier.padding(top = 4.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Botão Salvar
                Button(
                    onClick = {
                        var hasErrors = false

                        if (!InputValidator.isValidTitle(title)) {
                            titleError = "Título deve ter 3-200 caracteres"
                            hasErrors = true
                        }
                        if (!InputValidator.isValidDescription(description)) {
                            descriptionError = "Descrição deve ter no máximo 5000 caracteres"
                            hasErrors = true
                        }
                        if (!InputValidator.isValidDate(dueDate)) {
                            dueDateError = "Formato: dd/MM/yyyy"
                            hasErrors = true
                        }
                        if (!InputValidator.isValidClass(studentClass)) {
                            classError = "Selecione uma turma"
                            hasErrors = true
                        }

                        if (!hasErrors) {
                            val task = taskToEdit?.copy(
                                title = title,
                                description = description,
                                dueDate = dueDate,
                                studentClass = studentClass
                            ) ?: Task(
                                title = title,
                                description = description,
                                dueDate = dueDate,
                                studentClass = studentClass
                            )
                            onSave(task)
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    enabled = title.isNotEmpty() && description.isNotEmpty() && 
                              dueDate.isNotEmpty() && studentClass.isNotEmpty(),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = AccentPurple
                    ),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Icon(Icons.Default.Check, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        if (isEditing) "Atualizar Tarefa" else "Criar Tarefa",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }
}
