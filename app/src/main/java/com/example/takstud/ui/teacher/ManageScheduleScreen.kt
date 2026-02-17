package com.example.takstud.ui.teacher

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
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
import com.example.takstud.model.Period
import com.example.takstud.model.Schedule

@Composable
fun ManageScheduleScreen(
    modifier: Modifier = Modifier,
    scheduleToEdit: Schedule?,
    onSave: (Schedule) -> Unit,
    onBack: () -> Unit
) {
    val isNew = scheduleToEdit == null
    var studentClass by remember(scheduleToEdit) { mutableStateOf(scheduleToEdit?.studentClass ?: "") }
    var details by remember(scheduleToEdit) { mutableStateOf(scheduleToEdit?.details ?: "") }
    var periodo by remember(scheduleToEdit) { mutableStateOf(scheduleToEdit?.periodo ?: Period.MANHA) }

    Column(modifier = modifier.fillMaxSize().padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(if (isNew) stringResource(R.string.new_schedule) else stringResource(R.string.edit_schedule), style = MaterialTheme.typography.headlineMedium)
        
        if (isNew) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceAround, verticalAlignment = Alignment.CenterVertically) {
                Period.values().forEach { p ->
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        RadioButton(
                            selected = p == periodo,
                            onClick = { periodo = p }
                        )
                        Text(p.name)
                    }
                }
            }
        }

        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            OutlinedTextField(
                value = studentClass,
                onValueChange = { studentClass = it },
                label = { Text(stringResource(R.string.class_name_hint)) },
                modifier = Modifier.weight(1f),
                readOnly = !isNew
            )
            Button(
                onClick = { studentClass = "" },
                modifier = Modifier.padding(start = 8.dp),
                enabled = isNew
            ) {
                Text("TODOS")
            }
        }
        OutlinedTextField(
            value = details,
            onValueChange = { details = it },
            label = { Text(stringResource(R.string.schedule_details_hint)) },
            modifier = Modifier.fillMaxWidth().weight(1f)
        )
        
        Button(onClick = {
            onSave(Schedule(id = scheduleToEdit?.id ?: "", studentClass = studentClass, details = details, periodo = periodo))
        }, modifier = Modifier.fillMaxWidth()) { Text(stringResource(R.string.save)) }
        
        Button(onClick = onBack, modifier = Modifier.fillMaxWidth()) { Text(stringResource(R.string.back)) }
    }
}
