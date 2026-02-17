package com.example.takstud.ui.student.task

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.AttachFile
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.example.takstud.model.task.TaskSubmission
import com.example.takstud.ui.common.UiState
import com.example.takstud.viewmodel.TaskViewModel
import java.util.UUID

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StudentTaskSubmissionScreen(
    navController: NavController,
    taskId: String,
    studentId: String,
    viewModel: TaskViewModel = hiltViewModel()
) {
    val taskState by viewModel.currentTask.collectAsState()
    var answerText by remember { mutableStateOf("") }
    
    LaunchedEffect(taskId) {
        viewModel.loadTask(taskId)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Detalhes da Tarefa") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Voltar")
                    }
                }
            )
        },
        bottomBar = {
            BottomAppBar {
                Spacer(modifier = Modifier.weight(1f))
                Button(
                    onClick = {
                        val submission = TaskSubmission(
                            id = UUID.randomUUID().toString(),
                            taskId = taskId,
                            studentId = studentId,
                            content = answerText,
                            submittedAt = System.currentTimeMillis()
                        )
                        viewModel.submitTask(taskId, submission)
                        navController.popBackStack()
                    },
                    modifier = Modifier.padding(horizontal = 16.dp)
                ) {
                    Icon(Icons.AutoMirrored.Filled.Send, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Enviar Resposta")
                }
            }
        }
    ) { padding ->
        Box(modifier = Modifier.padding(padding)) {
            when (val state = taskState) {
                is UiState.Loading -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator()
                    }
                }
                is UiState.Success -> {
                    val task = state.data
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp)
                            .verticalScroll(rememberScrollState()),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        // Header
                        Text(
                            text = task.title,
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.Bold
                        )
                        
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            AssistChip(
                                onClick = {},
                                label = { Text(task.type.displayName) }
                            )
                            if (task.isOverdue()) {
                                AssistChip(
                                    onClick = {},
                                    label = { Text("Atrasada") },
                                    colors = AssistChipDefaults.assistChipColors(
                                        containerColor = MaterialTheme.colorScheme.errorContainer,
                                        labelColor = MaterialTheme.colorScheme.onErrorContainer
                                    )
                                )
                            } else {
                                AssistChip(
                                    onClick = {},
                                    label = { Text("Pendente") },
                                    colors = AssistChipDefaults.assistChipColors(
                                        containerColor = MaterialTheme.colorScheme.primaryContainer,
                                        labelColor = MaterialTheme.colorScheme.onPrimaryContainer
                                    )
                                )
                            }
                        }

                        HorizontalDivider()

                        // Description
                        Text("Instruções", style = MaterialTheme.typography.titleMedium)
                        Text(task.description, style = MaterialTheme.typography.bodyLarge)

                        Spacer(modifier = Modifier.height(16.dp))
                        HorizontalDivider()

                        // Submission Area
                        Text("Sua Resposta", style = MaterialTheme.typography.titleMedium)
                        OutlinedTextField(
                            value = answerText,
                            onValueChange = { answerText = it },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(200.dp),
                            placeholder = { Text("Digite sua resposta aqui...") }
                        )

                        OutlinedButton(
                            onClick = { /* TODO: Attach file */ },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(Icons.Filled.AttachFile, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Anexar Arquivo")
                        }
                    }
                }
                is UiState.Error -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text("Erro: ${state.message}")
                    }
                }
                else -> {}
            }
        }
    }
}