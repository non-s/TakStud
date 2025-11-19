package com.example.takstud.ui.teacher

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.example.takstud.R
import com.example.takstud.model.Class

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ManageClassesScreen(
    modifier: Modifier = Modifier,
    classes: List<Class>,
    onAddClass: () -> Unit,
    onClassClick: (Class) -> Unit,
    onDeleteClass: (Class) -> Unit,
    onBack: () -> Unit
) {
    var classToDelete by remember { mutableStateOf<Class?>(null) }

    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = { Text("Gerenciar Turmas") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Default.ArrowBack, "Voltar")
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Button(
                onClick = onAddClass,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp)
            ) {
                Icon(Icons.Default.Add, contentDescription = "Adicionar")
                Text("Adicionar Turma", modifier = Modifier.padding(start = 8.dp))
            }

            if (classes.isEmpty()) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Text("Nenhuma turma criada", style = MaterialTheme.typography.bodyLarge)
                }
            } else {
                LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(classes, key = { it.id }) { schoolClass ->
                        ClassCard(
                            schoolClass = schoolClass,
                            onEdit = { onClassClick(schoolClass) },
                            onDelete = { classToDelete = schoolClass }
                        )
                    }
                }
            }
        }
    }

    if (classToDelete != null) {
        androidx.compose.material3.AlertDialog(
            onDismissRequest = { classToDelete = null },
            title = { Text("Excluir Turma") },
            text = { Text("Tem certeza que deseja excluir a turma ${classToDelete?.name}?") },
            confirmButton = {
                Button(
                    onClick = {
                        onDeleteClass(classToDelete!!)
                        classToDelete = null
                    }
                ) {
                    Text("Excluir")
                }
            },
            dismissButton = {
                Button(onClick = { classToDelete = null }) {
                    Text("Cancelar")
                }
            }
        )
    }
}

@Composable
private fun ClassCard(
    schoolClass: Class,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp)
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
                    text = "Turma: ${schoolClass.name}",
                    style = MaterialTheme.typography.titleMedium
                )
                if (schoolClass.grade.isNotEmpty()) {
                    Text(
                        text = "Série: ${schoolClass.grade}",
                        style = MaterialTheme.typography.bodySmall
                    )
                }
                if (schoolClass.year.isNotEmpty()) {
                    Text(
                        text = "Ano: ${schoolClass.year}",
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
            Row {
                Button(onClick = onEdit, modifier = Modifier.padding(end = 8.dp)) {
                    Text("Editar")
                }
                IconButton(onClick = onDelete) {
                    Icon(Icons.Default.Delete, contentDescription = "Deletar")
                }
            }
        }
    }
}
