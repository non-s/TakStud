package com.example.takstud.ui.teacher

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.takstud.R
import com.example.takstud.model.Schedule
import com.example.takstud.util.InputValidator
import com.example.takstud.util.Result
import com.example.takstud.viewmodel.StudentManagementViewModel

/**
 * Teacher screen for registering new students in the system.
 * Teachers use this to add students by their RA and assign them to classes.
 * Integrates with StudentManagementViewModel for registration and validation.
 *
 * @param modifier Composable modifier
 * @param viewModel StudentManagementViewModel para gerenciar registro de estudantes
 * @param schedules Available schedules/classes for assignment
 * @param onBack Callback to return to previous screen
 */
@Composable
fun RegisterStudentScreen(
    modifier: Modifier = Modifier,
    viewModel: StudentManagementViewModel = remember { StudentManagementViewModel() },
    schedules: List<Schedule>,
    onBack: () -> Unit
) {
    var newRa by remember { mutableStateOf("") }
    var newName by remember { mutableStateOf("") }
    var newStudentClass by remember { mutableStateOf("") }
    var newParent by remember { mutableStateOf("") }
    var newPhone by remember { mutableStateOf("") }
    var expanded by remember { mutableStateOf(false) }

    var raError by remember { mutableStateOf<String?>(null) }
    var nameError by remember { mutableStateOf<String?>(null) }
    var classError by remember { mutableStateOf<String?>(null) }
    var parentError by remember { mutableStateOf<String?>(null) }
    var phoneError by remember { mutableStateOf<String?>(null) }
    var successMessage by remember { mutableStateOf<String?>(null) }

    val isLoading by viewModel.isLoading.collectAsState()
    val registrationState by viewModel.registrationState.collectAsState()
    val allStudents by viewModel.allStudents.collectAsState()

    // Handle registration result
    when (registrationState) {
        is Result.Success -> {
            successMessage = "Estudante cadastrado com sucesso!"
            // Clear form
            newRa = ""
            newName = ""
            newStudentClass = ""
            newParent = ""
            newPhone = ""
            viewModel.resetRegistrationState()
        }
        is Result.Error -> {
            raError = (registrationState as Result.Error).exception.message
        }
        is Result.Loading -> {}
    }

    Column(
        modifier = modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(
            "Cadastrar Novo Estudante",
            style = MaterialTheme.typography.headlineMedium
        )

        // RA input with validation
        OutlinedTextField(
            value = newRa,
            onValueChange = {
                newRa = it
                // Validate in real-time
                raError = if (it.isNotEmpty() && !InputValidator.isValidRA(it)) {
                    "RA deve ter 2-20 caracteres (letras, números, - e _)"
                } else null
            },
            label = { Text(stringResource(R.string.student_ra_hint)) },
            isError = raError != null,
            modifier = Modifier.fillMaxWidth()
        )
        if (raError != null) {
            Text(raError!!, color = Color.Red, fontSize = 12.sp, modifier = Modifier.padding(start = 4.dp))
        }

        // Name input
        OutlinedTextField(
            value = newName,
            onValueChange = {
                newName = it
                nameError = if (it.isNotEmpty() && it.length < 2) {
                    "Nome deve ter pelo menos 2 caracteres"
                } else null
            },
            label = { Text("Nome do Estudante") },
            isError = nameError != null,
            modifier = Modifier.fillMaxWidth()
        )
        if (nameError != null) {
            Text(nameError!!, color = Color.Red, fontSize = 12.sp, modifier = Modifier.padding(start = 4.dp))
        }

        // Class selection with validation
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(modifier = Modifier.weight(1f)) {
                OutlinedTextField(
                    value = newStudentClass,
                    onValueChange = { },
                    label = { Text(stringResource(R.string.class_hint)) },
                    isError = classError != null,
                    modifier = Modifier.fillMaxWidth(),
                    readOnly = true
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
                            text = { Text(stringResource(R.string.no_class_registered)) },
                            onClick = { expanded = false }
                        )
                    } else {
                        schedules.forEach { schedule ->
                            DropdownMenuItem(
                                text = { Text(schedule.studentClass) },
                                onClick = {
                                    newStudentClass = schedule.studentClass
                                    classError = null
                                    expanded = false
                                }
                            )
                        }
                    }
                }
            }
        }
        if (classError != null) {
            Text(classError!!, color = Color.Red, fontSize = 12.sp, modifier = Modifier.padding(start = 4.dp))
        }

        // Parent name input
        OutlinedTextField(
            value = newParent,
            onValueChange = {
                newParent = it
                parentError = if (it.isNotEmpty() && it.length < 2) {
                    "Nome do responsável deve ter pelo menos 2 caracteres"
                } else null
            },
            label = { Text("Nome do Responsável") },
            isError = parentError != null,
            modifier = Modifier.fillMaxWidth()
        )
        if (parentError != null) {
            Text(parentError!!, color = Color.Red, fontSize = 12.sp, modifier = Modifier.padding(start = 4.dp))
        }

        // Phone input
        OutlinedTextField(
            value = newPhone,
            onValueChange = {
                newPhone = it
                phoneError = if (it.isNotEmpty() && it.length < 8) {
                    "Telefone deve ter pelo menos 8 dígitos"
                } else null
            },
            label = { Text("Telefone") },
            isError = phoneError != null,
            modifier = Modifier.fillMaxWidth()
        )
        if (phoneError != null) {
            Text(phoneError!!, color = Color.Red, fontSize = 12.sp, modifier = Modifier.padding(start = 4.dp))
        }

        // Success message
        if (successMessage != null) {
            Text(
                successMessage!!,
                color = Color.Green,
                fontSize = 12.sp,
                modifier = Modifier.padding(start = 4.dp)
            )
        }

        Spacer(Modifier.height(8.dp))

        // Register button
        Button(
            onClick = {
                // Validate all fields
                var hasErrors = false

                if (!InputValidator.isValidRA(newRa)) {
                    raError = "RA deve ter 2-20 caracteres (letras, números, - e _)"
                    hasErrors = true
                }

                if (newName.isBlank() || newName.length < 2) {
                    nameError = "Nome deve ter pelo menos 2 caracteres"
                    hasErrors = true
                }

                if (!InputValidator.isValidClass(newStudentClass)) {
                    classError = "Classe deve ser selecionada"
                    hasErrors = true
                }

                if (newParent.isBlank() || newParent.length < 2) {
                    parentError = "Nome do responsável deve ter pelo menos 2 caracteres"
                    hasErrors = true
                }

                if (newPhone.isBlank() || newPhone.length < 8) {
                    phoneError = "Telefone deve ter pelo menos 8 dígitos"
                    hasErrors = true
                }

                if (!hasErrors) {
                    successMessage = null
                    viewModel.registerStudent(newRa, newName, newStudentClass, newParent, newPhone)
                }
            },
            enabled = raError == null && classError == null && !isLoading && newRa.isNotEmpty(),
            modifier = Modifier.fillMaxWidth()
        ) {
            if (isLoading) {
                CircularProgressIndicator(modifier = Modifier.width(20.dp), strokeWidth = 2.dp)
            } else {
                Text(stringResource(R.string.save))
            }
        }

        Spacer(Modifier.height(16.dp))

        // List of registered students
        Text("Estudantes Cadastrados:", style = MaterialTheme.typography.titleMedium)
        LazyColumn(modifier = Modifier.weight(1f)) {
            when (allStudents) {
                is Result.Success -> {
                    val students = (allStudents as Result.Success).data
                    items(students, key = { it.id }) { student ->
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(vertical = 4.dp)
                        ) {
                            Text(
                                "${student.ra} - ${student.name}",
                                modifier = Modifier.weight(1f)
                            )
                            IconButton(onClick = { viewModel.deleteStudent(student.ra) }) {
                                Icon(Icons.Default.Delete, "Deletar RA")
                            }
                        }
                    }
                }
                is Result.Error -> {
                    item {
                        Text(
                            "Erro ao carregar estudantes: ${(allStudents as Result.Error).exception.message}",
                            color = Color.Red
                        )
                    }
                }
                is Result.Loading -> {
                    item {
                        CircularProgressIndicator()
                    }
                }
            }
        }

        Button(onClick = onBack, modifier = Modifier.fillMaxWidth()) {
            Text(stringResource(R.string.back))
        }
    }
}