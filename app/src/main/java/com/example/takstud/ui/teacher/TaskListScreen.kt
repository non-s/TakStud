@file:OptIn(ExperimentalMaterial3Api::class)

package com.example.takstud.ui.teacher

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Grade
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.takstud.model.Task
import com.example.takstud.ui.theme.*

/**
 * TaskListScreen Premium - Lista de tarefas moderna
 */
@Composable
fun TaskListScreen(
    modifier: Modifier = Modifier,
    tasks: List<Task>,
    onAddTask: () -> Unit,
    onTaskClick: (Task) -> Unit,
    onDeleteTask: (Task) -> Unit,
    onBack: () -> Unit
) {
    var taskToDelete by remember { mutableStateOf<Task?>(null) }
    var searchQuery by remember { mutableStateOf("") }
    var showSearch by remember { mutableStateOf(false) }

    // Filter tasks
    val filteredTasks = remember(tasks, searchQuery) {
        if (searchQuery.isBlank()) {
            tasks
        } else {
            tasks.filter { task ->
                task.title.contains(searchQuery, ignoreCase = true) ||
                task.studentClass.contains(searchQuery, ignoreCase = true) ||
                task.dueDate.contains(searchQuery, ignoreCase = true)
            }
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        PrimaryBlue.copy(alpha = 0.03f),
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
                            colors = listOf(PrimaryBlue, PrimaryDark)
                        )
                    )
                    .padding(top = 24.dp)
                    .padding(16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        IconButton(onClick = onBack) {
                            Icon(
                                Icons.AutoMirrored.Default.ArrowBack,
                                "Voltar",
                                tint = Color.White
                            )
                        }
                        Column {
                            Text(
                                text = "Tarefas",
                                style = MaterialTheme.typography.headlineSmall,
                                color = Color.White,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "${tasks.size} ${if (tasks.size == 1) "tarefa" else "tarefas"}",
                                style = MaterialTheme.typography.bodySmall,
                                color = Color.White.copy(alpha = 0.8f)
                            )
                        }
                    }
                    IconButton(onClick = { showSearch = !showSearch }) {
                        Icon(
                            Icons.Default.Search,
                            "Buscar",
                            tint = Color.White
                        )
                    }
                }
            }

            // Search Bar
            AnimatedVisibility(visible = showSearch) {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    placeholder = { Text("Buscar tarefas...") },
                    leadingIcon = {
                        Icon(Icons.Default.Search, contentDescription = null)
                    },
                    shape = RoundedCornerShape(16.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = PrimaryBlue,
                        unfocusedBorderColor = Neutral300
                    )
                )
            }

            // Task List
            if (filteredTasks.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "📋",
                            fontSize = 64.sp
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = if (searchQuery.isBlank()) 
                                "Nenhuma tarefa cadastrada" 
                            else 
                                "Nenhuma tarefa encontrada",
                            style = MaterialTheme.typography.titleMedium,
                            color = Neutral500
                        )
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(filteredTasks, key = { it.id }) { task ->
                        TaskCardPremium(
                            task = task,
                            onClick = { onTaskClick(task) },
                            onDelete = { taskToDelete = task }
                        )
                    }
                }
            }
        }

        // FAB
        FloatingActionButton(
            onClick = onAddTask,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(24.dp),
            containerColor = AccentPurple,
            contentColor = Color.White
        ) {
            Icon(Icons.Default.Add, "Adicionar Tarefa")
        }
    }

    // Delete Dialog
    taskToDelete?.let { task ->
        AlertDialog(
            onDismissRequest = { taskToDelete = null },
            icon = {
                Icon(
                    Icons.Default.Delete,
                    contentDescription = null,
                    tint = Error,
                    modifier = Modifier.size(32.dp)
                )
            },
            title = {
                Text(
                    "Excluir Tarefa?",
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Text("Tem certeza que deseja excluir \"${task.title}\"? Esta ação não pode ser desfeita.")
            },
            confirmButton = {
                Button(
                    onClick = {
                        onDeleteTask(task)
                        taskToDelete = null
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Error
                    )
                ) {
                    Text("Excluir")
                }
            },
            dismissButton = {
                TextButton(onClick = { taskToDelete = null }) {
                    Text("Cancelar")
                }
            },
            shape = RoundedCornerShape(24.dp)
        )
    }
}

@Composable
fun TaskCardPremium(
    task: Task,
    onClick: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 2.dp,
            pressedElevation = 6.dp
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = task.title,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = Neutral900
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = PrimaryBlue.copy(alpha = 0.1f)
                        ) {
                            Text(
                                text = task.studentClass,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                color = PrimaryBlue,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "📅 ${task.dueDate}",
                            style = MaterialTheme.typography.bodySmall,
                            color = Neutral500
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            OutlinedButton(
                onClick = onDelete,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = Error
                )
            ) {
                Icon(
                    Icons.Default.Delete,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text("Excluir", fontSize = 12.sp)
            }
        }
    }
}
