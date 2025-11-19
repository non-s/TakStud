@file:OptIn(ExperimentalMaterial3Api::class)

package com.example.takstud.ui.teacher

import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
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
import com.example.takstud.model.Grade
import com.example.takstud.model.Student
import com.example.takstud.model.Task

@Composable
fun ManageGradesScreen(
    modifier: Modifier = Modifier,
    task: Task,
    students: List<Student>,
    grades: List<Grade>,
    onSaveGrade: (Grade) -> Unit,
    onBack: () -> Unit
) {
    val studentGrades = remember { mutableStateMapOf<String, String>() }

    LaunchedEffect(students, grades) {
        students.forEach { student ->
            val existingGrade = grades.find { it.taskId == task.id && it.studentId == student.id }?.score ?: ""
            studentGrades[student.id] = existingGrade
        }
    }

    Scaffold(
        modifier = modifier,
        topBar = { TopAppBar(title = { Text(stringResource(R.string.grades_for_task, task.title)) }, navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Default.ArrowBack, stringResource(R.string.back)) } }) }
    ) {
        LazyColumn(modifier = Modifier.fillMaxSize().padding(it).padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            if (students.isEmpty()) {
                item { Text(stringResource(R.string.no_students_in_class)) }
            } else {
                items(students, key = { it.id }) { student ->
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(stringResource(R.string.student_ra_label, student.ra), modifier = Modifier.weight(1f))
                        OutlinedTextField(
                            value = studentGrades[student.id] ?: "",
                            onValueChange = { newScore -> studentGrades[student.id] = newScore },
                            label = { Text(stringResource(R.string.grade_hint)) },
                            modifier = Modifier.width(100.dp)
                        )
                    }
                }
                item {
                    Spacer(Modifier.height(16.dp))
                    Button(
                        onClick = {
                            studentGrades.forEach { (studentId, score) ->
                                val existingGrade = grades.find { it.taskId == task.id && it.studentId == studentId }
                                val student = students.find { it.id == studentId }!!
                                val gradeToSave = existingGrade?.copy(score = score) ?: Grade(taskId = task.id, studentId = studentId, studentRa = student.ra, score = score)
                                onSaveGrade(gradeToSave)
                            }
                            onBack()
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) { Text(stringResource(R.string.save_all_grades)) }
                }
            }
        }
    }
}
