package com.example.takstud.ui.teacher

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
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
import androidx.compose.foundation.layout.Row
import androidx.compose.ui.unit.sp
import com.example.takstud.R
import com.example.takstud.model.Notice
import com.example.takstud.model.Schedule
import com.example.takstud.util.InputValidator

/**
 * Notice creation and editing screen for teachers.
 * Validates all notice fields before allowing submission.
 *
 * @param modifier Composable modifier
 * @param noticeToEdit Notice to edit (null for new notice)
 * @param schedules Available schedules/classes for selection
 * @param onSave Callback when notice is saved with validation
 * @param onBack Callback to return to previous screen
 */
@Composable
fun AddNoticeScreen(
    modifier: Modifier = Modifier,
    noticeToEdit: Notice?,
    schedules: List<Schedule>,
    onSave: (Notice) -> Unit,
    onBack: () -> Unit
) {
    var title by remember(noticeToEdit) { mutableStateOf(noticeToEdit?.title ?: "") }
    var description by remember(noticeToEdit) { mutableStateOf(noticeToEdit?.description ?: "") }
    var studentClass by remember(noticeToEdit) { mutableStateOf(noticeToEdit?.studentClass ?: "") }
    var expanded by remember { mutableStateOf(false) }

    // Validation error states
    var titleError by remember { mutableStateOf<String?>(null) }
    var descriptionError by remember { mutableStateOf<String?>(null) }
    var classError by remember { mutableStateOf<String?>(null) }

    Column(modifier = modifier.fillMaxSize().padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(if (noticeToEdit == null) stringResource(R.string.add_notice_title) else stringResource(R.string.edit_notice_title), style = MaterialTheme.typography.headlineMedium)

        // Título do aviso com validação
        OutlinedTextField(
            value = title,
            onValueChange = {
                title = it
                // Validar conforme digita
                titleError = if (it.isNotEmpty() && !InputValidator.isValidTitle(it)) {
                    "Título deve ter 3-200 caracteres"
                } else null
            },
            label = { Text(stringResource(R.string.notice_title_hint)) },
            isError = titleError != null,
            modifier = Modifier.fillMaxWidth()
        )
        if (titleError != null) {
            Text(titleError!!, color = Color.Red, fontSize = 12.sp, modifier = Modifier.padding(start = 4.dp))
        }

        // Descrição do aviso com validação
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
            modifier = Modifier.fillMaxWidth().weight(1f)
        )
        if (descriptionError != null) {
            Text(descriptionError!!, color = Color.Red, fontSize = 12.sp, modifier = Modifier.padding(start = 4.dp))
        }
        
        // Classe com validação (selecionada via dropdown)
        Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Box(modifier = Modifier.weight(1f)) {
                OutlinedTextField(
                    value = if (studentClass.isEmpty()) "TODOS" else studentClass,
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
                                    studentClass = schedule.studentClass
                                    classError = null // Limpar erro ao selecionar
                                    expanded = false
                                }
                            )
                        }
                    }
                }
            }
            Button(
                onClick = { studentClass = "" },
                modifier = Modifier.padding(start = 8.dp)
            ) {
                Text("TODOS")
            }
        }
        if (classError != null) {
            Text(classError!!, color = Color.Red, fontSize = 12.sp, modifier = Modifier.padding(start = 4.dp))
        }

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

                if (studentClass.isNotEmpty() && !InputValidator.isValidClass(studentClass)) {
                    classError = "Classe deve ser selecionada (2-50 caracteres)"
                    hasErrors = true
                }

                if (!hasErrors) {
                    val notice = noticeToEdit?.copy(
                        title = title,
                        description = description,
                        studentClass = studentClass
                    ) ?: Notice(
                        title = title,
                        description = description,
                        studentClass = studentClass
                    )
                    onSave(notice)
                }
            },
            modifier = Modifier.fillMaxWidth(),
            enabled = titleError == null && descriptionError == null && classError == null && title.isNotEmpty()
        ) {
            Text(stringResource(R.string.save))
        }

        Button(onClick = onBack, modifier = Modifier.fillMaxWidth()) {
            Text(stringResource(R.string.back))
        }
    }
}
