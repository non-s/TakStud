package com.example.takstud.ui.teacher

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
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
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.example.takstud.R
import com.example.takstud.model.Class
import java.util.UUID

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddClassScreen(
    modifier: Modifier = Modifier,
    classToEdit: Class?,
    onSave: (Class) -> Unit,
    onBack: () -> Unit
) {
    val isNew = classToEdit == null
    var name by remember(classToEdit) { mutableStateOf(classToEdit?.name ?: "") }
    var grade by remember(classToEdit) { mutableStateOf(classToEdit?.grade ?: "") }
    var year by remember(classToEdit) { mutableStateOf(classToEdit?.year ?: "") }

    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = { Text(if (isNew) "Nova Turma" else "Editar Turma") },
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
            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text("Nome da Turma (ex: 6A, 7B)") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            OutlinedTextField(
                value = grade,
                onValueChange = { grade = it },
                label = { Text("Série/Grade (ex: 6º ano, 1º Ano)") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            OutlinedTextField(
                value = year,
                onValueChange = { year = it },
                label = { Text("Ano Letivo (ex: 2024)") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 24.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = {
                        if (name.isNotBlank()) {
                            val newClass = Class(
                                id = classToEdit?.id ?: UUID.randomUUID().toString(),
                                name = name,
                                grade = grade,
                                year = year,
                                createdAt = classToEdit?.createdAt ?: System.currentTimeMillis()
                            )
                            onSave(newClass)
                            onBack()
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = name.isNotBlank()
                ) {
                    Text(if (isNew) "Criar Turma" else "Atualizar")
                }

                Button(
                    onClick = onBack,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Cancelar")
                }
            }
        }
    }
}
