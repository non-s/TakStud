package com.example.takstud.ui.teacher

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.takstud.R
import com.example.takstud.model.Student
import com.example.takstud.ui.theme.DarkGray
import com.example.takstud.ui.theme.LightGray
import com.example.takstud.ui.theme.NavyBlue
import com.example.takstud.ui.theme.PureWhite
import com.example.takstud.ui.theme.ErrorRed
import com.example.takstud.util.InputValidator

/**
 * Tela de gerenciamento de alunos com ABAS POR PERÍODO.
 * Professor seleciona período (Manhã, Tarde, EJA) → turmas aparecem → seleciona turma → cadastra aluno
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ManageStudentsScreen(
    modifier: Modifier = Modifier,
    students: List<Student>,
    classesByPeriod: Map<String, List<String>> = emptyMap(),
    onRegisterStudent: (String, String, String) -> Unit,
    onDeleteStudent: (Student) -> Unit,
    onBack: () -> Unit
) {
    var newStudentName by remember { mutableStateOf("") }
    var newStudentRa by remember { mutableStateOf("") }
    var selectedClass by remember { mutableStateOf("") }
    var nameError by remember { mutableStateOf<String?>(null) }
    var raError by remember { mutableStateOf<String?>(null) }
    var classError by remember { mutableStateOf<String?>(null) }

    // Abas: MANHA, TARDE, EJA
    val periods = listOf("MANHA", "TARDE", "EJA")
    var selectedTabIndex by remember { mutableStateOf(0) }
    val currentPeriod = periods.getOrNull(selectedTabIndex) ?: "MANHA"
    val classesForCurrentPeriod = classesByPeriod[currentPeriod] ?: emptyList()

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = PureWhite,
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.manage_students_title), color = PureWhite, fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = stringResource(R.string.back), tint = PureWhite)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = NavyBlue)
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            verticalArrangement = Arrangement.spacedBy(0.dp)
        ) {
            // ===== ABAS POR PERÍODO =====
            TabRow(selectedTabIndex = selectedTabIndex) {
                periods.forEachIndexed { index, period ->
                    val periodLabel = when (period) {
                        "MANHA" -> "☀️ Manhã"
                        "TARDE" -> "🌤️ Tarde"
                        "EJA" -> "🌙 EJA"
                        else -> period
                    }
                    Tab(
                        selected = selectedTabIndex == index,
                        onClick = { selectedTabIndex = index },
                        text = { Text(periodLabel, fontWeight = FontWeight.Bold, fontSize = 12.sp) }
                    )
                }
            }

            // ===== CONTEÚDO DA ABA =====
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Seção de Cadastro
                Text("Cadastrar Novo Aluno", style = MaterialTheme.typography.titleLarge, color = NavyBlue)

                // Nome do Aluno
                OutlinedTextField(
                    value = newStudentName,
                    onValueChange = {
                        newStudentName = it
                        nameError = if (it.isNotBlank()) null else "O nome é obrigatório"
                    },
                    label = { Text("Nome do Aluno") },
                    isError = nameError != null,
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = NavyBlue, unfocusedBorderColor = DarkGray)
                )
                if (nameError != null) {
                    Text(nameError!!, color = ErrorRed, fontSize = 12.sp)
                }

                // RA do Aluno
                OutlinedTextField(
                    value = newStudentRa,
                    onValueChange = {
                        newStudentRa = it
                        raError = if (InputValidator.isValidRA(it)) null else "RA deve ter de 2 a 20 caracteres."
                    },
                    label = { Text("RA do Aluno (Registro Acadêmico)") },
                    isError = raError != null,
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = NavyBlue, unfocusedBorderColor = DarkGray)
                )
                if (raError != null) {
                    Text(raError!!, color = ErrorRed, fontSize = 12.sp)
                }

                // ===== SELEÇÃO DE TURMA POR PERÍODO =====
                Text("Selecionar Turma - Período: $currentPeriod", style = MaterialTheme.typography.labelLarge, color = NavyBlue)

                if (classesForCurrentPeriod.isEmpty()) {
                    Text("Nenhuma turma disponível neste período.", color = ErrorRed, fontSize = 12.sp, modifier = Modifier.padding(top = 8.dp))
                } else {
                    Column(modifier = Modifier.fillMaxWidth().border(1.dp, LightGray, RoundedCornerShape(8.dp)).padding(8.dp)) {
                        classesForCurrentPeriod.forEach { className ->
                            Button(
                                onClick = {
                                    selectedClass = className
                                    classError = null
                                },
                                modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = if (selectedClass == className) NavyBlue else LightGray
                                )
                            ) {
                                Text(className, color = if (selectedClass == className) PureWhite else NavyBlue, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
                if (classError != null) {
                    Text(classError!!, color = ErrorRed, fontSize = 12.sp)
                }

                // Botão Cadastrar
                Button(
                    onClick = {
                        val isNameValid = newStudentName.isNotBlank()
                        val isRaValid = InputValidator.isValidRA(newStudentRa)
                        val isClassValid = selectedClass.isNotBlank()
                        nameError = if (isNameValid) null else "O nome é obrigatório"
                        raError = if (isRaValid) null else "RA deve ter de 2 a 20 caracteres."
                        classError = if (isClassValid) null else "Selecione uma turma"

                        if (isNameValid && isRaValid && isClassValid) {
                            onRegisterStudent(newStudentName, newStudentRa, selectedClass)
                            newStudentName = ""
                            newStudentRa = ""
                            selectedClass = ""
                        }
                    },
                    modifier = Modifier.fillMaxWidth().height(48.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = NavyBlue),
                    enabled = selectedClass.isNotBlank()
                ) {
                    Text("Cadastrar Aluno", color = PureWhite, fontWeight = FontWeight.Bold)
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Lista de Alunos - Filtar por turma
                Text("Alunos Cadastrados - Período: $currentPeriod", style = MaterialTheme.typography.titleLarge, color = NavyBlue)

                LazyColumn(modifier = Modifier.weight(1f)) {
                    if (students.isEmpty()) {
                        item {
                            Text(
                                "Nenhum aluno cadastrado ainda.",
                                style = MaterialTheme.typography.bodyMedium,
                                color = DarkGray,
                                modifier = Modifier.padding(vertical = 16.dp)
                            )
                        }
                    } else {
                        // Agrupar alunos por turma do período selecionado
                        val studentsByClass = classesForCurrentPeriod.associateWith { className ->
                            students.filter { it.studentClass == className }
                        }

                        // Exibir turmas e seus alunos
                        studentsByClass.forEach { (className, classStudents) ->
                            item {
                                Text(
                                    "Turma: $className",
                                    style = MaterialTheme.typography.labelLarge,
                                    color = NavyBlue,
                                    modifier = Modifier.padding(top = 12.dp, bottom = 8.dp),
                                    fontWeight = FontWeight.Bold
                                )
                            }

                            if (classStudents.isEmpty()) {
                                item {
                                    Text(
                                        "Nenhum aluno nesta turma",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = DarkGray,
                                        modifier = Modifier.padding(start = 16.dp, bottom = 8.dp)
                                    )
                                }
                            } else {
                                items(classStudents, key = { it.id }) { student ->
                                    StudentListItem(student = student, onDelete = { onDeleteStudent(student) })
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun StudentListItem(student: Student, onDelete: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(containerColor = LightGray.copy(alpha = 0.5f)),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .padding(horizontal = 16.dp, vertical = 12.dp)
                .fillMaxWidth()
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(student.name, fontWeight = FontWeight.Bold, color = NavyBlue, fontSize = 16.sp)
                Text("RA: ${student.ra}", color = DarkGray, fontSize = 14.sp)
                if (student.studentClass.isNotEmpty()) {
                    Text("Turma: ${student.studentClass}", color = DarkGray, fontSize = 12.sp)
                }
            }
            IconButton(onClick = onDelete) {
                Icon(Icons.Default.Delete, contentDescription = "Deletar Aluno", tint = ErrorRed)
            }
        }
    }
}
