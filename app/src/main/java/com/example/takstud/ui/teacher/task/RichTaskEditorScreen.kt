package com.example.takstud.ui.teacher.task

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.AttachFile
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.example.takstud.model.task.TaskExtended
import com.example.takstud.model.task.TaskType
import com.example.takstud.viewmodel.TaskViewModel
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RichTaskEditorScreen(
    navController: NavController,
    taskId: String? = null,
    viewModel: TaskViewModel = hiltViewModel()
) {
    var title by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var dueDate by remember { mutableStateOf("") }
    var selectedType by remember { mutableStateOf(TaskType.EXERCISE) }
    var className by remember { mutableStateOf("") }
    
    // Load existing task if editing
    LaunchedEffect(taskId) {
        if (taskId != null) {
            viewModel.loadTask(taskId)
        }
    }

    // Observe current task for editing
    // TODO: Populate fields when task loads

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (taskId == null) "Nova Tarefa" else "Editar Tarefa") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Voltar")
                    }
                },
                actions = {
                    IconButton(onClick = {
                        val newTask = TaskExtended(
                            id = taskId ?: UUID.randomUUID().toString(),
                            title = title,
                            description = description,
                            type = selectedType,
                            className = className,
                            // TODO: Parse date properly
                            dueDate = System.currentTimeMillis() + 7 * 24 * 60 * 60 * 1000 // +7 dias default
                        )
                        viewModel.saveTask(newTask) {
                            navController.popBackStack()
                        }
                    }) {
                        Icon(Icons.Default.Check, contentDescription = "Salvar")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            OutlinedTextField(
                value = title,
                onValueChange = { title = it },
                label = { Text("Título") },
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = description,
                onValueChange = { description = it },
                label = { Text("Descrição Detalhada") },
                modifier = Modifier.fillMaxWidth(),
                minLines = 5
            )

            OutlinedTextField(
                value = className,
                onValueChange = { className = it },
                label = { Text("Turma (Ex: 9A)") },
                modifier = Modifier.fillMaxWidth()
            )

            // Date Picker Simulator
            OutlinedTextField(
                value = dueDate,
                onValueChange = { dueDate = it },
                label = { Text("Data de Entrega (dd/mm/aaaa)") },
                modifier = Modifier.fillMaxWidth()
            )

            // Type Selector
            Text("Tipo de Atividade", style = MaterialTheme.typography.titleMedium)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                FilterChip(
                    selected = selectedType == TaskType.EXERCISE,
                    onClick = { selectedType = TaskType.EXERCISE },
                    label = { Text("Exercício") }
                )
                FilterChip(
                    selected = selectedType == TaskType.HOMEWORK,
                    onClick = { selectedType = TaskType.HOMEWORK },
                    label = { Text("Dever") }
                )
                FilterChip(
                    selected = selectedType == TaskType.TEST,
                    onClick = { selectedType = TaskType.TEST },
                    label = { Text("Prova") }
                )
            }

            // Attachments Section
            Divider()
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Anexos", style = MaterialTheme.typography.titleMedium)
                TextButton(onClick = { /* TODO: Open file picker */ }) {
                    Icon(Icons.Default.AttachFile, contentDescription = null)
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Adicionar")
                }
            }
            
            // Placeholder for attachments list
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                modifier = Modifier.fillMaxWidth()
            ) {
                Box(
                    modifier = Modifier
                        .padding(16.dp)
                        .fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    Text("Nenhum anexo selecionado", color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        }
    }
}
