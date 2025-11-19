@file:OptIn(ExperimentalMaterial3Api::class)

package com.example.takstud.ui.teacher

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.example.takstud.R
import com.example.takstud.model.AttendanceRecord
import com.example.takstud.model.Student

@Composable
fun TakeAttendanceScreen(
    modifier: Modifier = Modifier,
    studentClass: String,
    date: String,
    students: List<Student>,
    records: List<AttendanceRecord>,
    onSaveAttendance: (AttendanceRecord) -> Unit,
    onBack: () -> Unit
) {
    val studentPresence = remember { mutableStateMapOf<String, Boolean>() }

    LaunchedEffect(students, records) {
        students.forEach { student ->
            val existingRecord = records.find { it.studentId == student.id }?.isPresent ?: true
            studentPresence[student.id] = existingRecord
        }
    }

    Scaffold(
        modifier = modifier,
        topBar = { TopAppBar(title = { Text(stringResource(R.string.attendance_for, studentClass, date)) }, navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Default.ArrowBack, stringResource(R.string.back)) } }) }
    ) {
        LazyColumn(modifier = Modifier.fillMaxSize().padding(it).padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            if (students.isEmpty()) {
                item { Text(stringResource(R.string.no_students_in_class)) }
            } else {
                items(students, key = { it.id }) { student ->
                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.clickable {
                        studentPresence[student.id] = !(studentPresence[student.id] ?: true)
                    }) {
                        Checkbox(
                            checked = studentPresence[student.id] ?: true,
                            onCheckedChange = { isChecked -> studentPresence[student.id] = isChecked }
                        )
                        Text(stringResource(R.string.student_ra_label, student.ra), modifier = Modifier.padding(start = 8.dp))
                    }
                }
                item {
                    Spacer(Modifier.height(16.dp))
                    Button(
                        onClick = {
                            students.forEach { student ->
                                val isPresent = studentPresence[student.id] ?: true
                                val existingRecord = records.find { it.studentId == student.id }
                                val recordToSave = existingRecord?.copy(isPresent = isPresent) ?: AttendanceRecord(date = date, studentId = student.id, studentRa = student.ra, studentClass = studentClass, isPresent = isPresent)
                                onSaveAttendance(recordToSave)
                            }
                            onBack()
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) { Text(stringResource(R.string.save_attendance)) }
                }
            }
        }
    }
}
