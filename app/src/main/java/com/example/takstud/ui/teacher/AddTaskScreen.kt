package com.example.takstud.ui.teacher

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
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
import com.example.takstud.model.Task
import com.example.takstud.util.InputValidator

/**
 * Task creation and editing screen for teachers.
 * Validates all task fields before allowing submission.
 *
 * @param modifier Composable modifier
 * @param taskToEdit Task to edit (null for new task)
 * @param schedules Available schedules/classes for selection
 * @param onSave Callback when task is saved with validation
 * @param onBack Callback to return to previous screen
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

    // Validation error states
    var titleError by remember { mutableStateOf<String?>(null) }
    var descriptionError by remember { mutableStateOf<String?>(null) }
    var dueDateError by remember { mutableStateOf<String?>(null) }
    var classError by remember { mutableStateOf<String?>(null) }

    Column(
        modifier = modifier.fillMaxSize().padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(if (taskToEdit == null) stringResource(R.string.add_task) else stringResource(R.string.edit_task), style = MaterialTheme.typography.headlineMedium)
        Spacer(Modifier.height(16.dp))

        // Título da tarefa com validação
        OutlinedTextField(
            value = title,
            onValueChange = {
                title = it
                // Validar conforme digita
                titleError = if (it.isNotEmpty() && !InputValidator.isValidTitle(it)) {
                    "Título deve ter 3-200 caracteres"
                } else null
            },
            label = { Text(stringResource(R.string.title_hint)) },
            isError = titleError != null,
            modifier = Modifier.fillMaxWidth()
        )
        if (titleError != null) {
            Text(titleError!!, color = Color.Red, fontSize = 12.sp, modifier = Modifier.padding(start = 4.dp))
        }

        // Descrição da tarefa com validação
        OutlinedTextField(
            value = description,
            onValueChange = {
                description = it
                descriptionError = if (it.isNotEmpty() && !InputValidator.isValidDescription(it)) {
                    "Descrição deve ter no máximo 5000 caracteres"
                } else null
            },
            label = { Text(stringResource(R.string.description_hint)) },
            isError = descriptionError != null,
            modifier = Modifier.fillMaxWidth()
        )
        if (descriptionError != null) {
            Text(descriptionError!!, color = Color.Red, fontSize = 12.sp, modifier = Modifier.padding(start = 4.dp))
        }

        // Data de entrega com validação
        OutlinedTextField(
            value = dueDate,
            onValueChange = {
                dueDate = it
                dueDateError = if (it.isNotEmpty() && !InputValidator.isValidDate(it)) {
                    "Formato: dd/MM/yyyy"
                } else null
            },
            label = { Text(stringResource(R.string.due_date_hint)) },
            isError = dueDateError != null,
            modifier = Modifier.fillMaxWidth()
        )
        if (dueDateError != null) {
            Text(dueDateError!!, color = Color.Red, fontSize = 12.sp, modifier = Modifier.padding(start = 4.dp))
        }

        // Classe com validação (selecionada via dropdown)
        Box {
            OutlinedTextField(
                value = studentClass,
                onValueChange = { },
                label = { Text(stringResource(R.string.class_hint)) },
                isError = classError != null,
                modifier = Modifier.fillMaxWidth().clickable { expanded = true },
                readOnly = true
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
                                studentClass = schedule.studentClass
                                classError = null // Limpar erro ao selecionar
                                expanded = false
                            }
                        )
                    }
                }
            }
        }
        if (classError != null) {
            Text(classError!!, color = Color.Red, fontSize = 12.sp, modifier = Modifier.padding(start = 4.dp))
        }

        Spacer(Modifier.weight(1f))

        // Botão salvar com validação completa
        Button(
            onClick = {
                // Validar todos os campos
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
                    classError = "Classe deve ser selecionada (2-50 caracteres)"
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
            modifier = Modifier.fillMaxWidth(),
            enabled = titleError == null && descriptionError == null && dueDateError == null && classError == null && title.isNotEmpty()
        ) {
            Text(stringResource(R.string.save))
        }

        Button(onClick = onBack, modifier = Modifier.fillMaxWidth()) {
            Text(stringResource(R.string.back))
        }
    }
}
