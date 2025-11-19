@file:OptIn(ExperimentalMaterial3Api::class)

package com.example.takstud.ui.teacher

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.takstud.R
import java.time.LocalDate
import java.time.format.DateTimeFormatter

@Composable
fun AttendanceScreen(
    modifier: Modifier = Modifier,
    schedules: List<String> = emptyList(),
    classesByPeriod: Map<String, List<String>> = emptyMap(),
    onTakeAttendance: (String, String) -> Unit,
    onBack: () -> Unit
) {
    var selectedClass by remember { mutableStateOf("") }
    var date by remember { mutableStateOf(LocalDate.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy"))) }
    var showErrorMessage by remember { mutableStateOf("") }

    // Abas: MANHA, TARDE, EJA
    val periods = listOf("MANHA", "TARDE", "EJA")
    var selectedTabIndex by remember { mutableStateOf(0) }
    val currentPeriod = periods.getOrNull(selectedTabIndex) ?: "MANHA"
    val classesForCurrentPeriod = classesByPeriod[currentPeriod] ?: emptyList()

    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.attendance_control)) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Default.ArrowBack, stringResource(R.string.back))
                    }
                }
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
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Data
                OutlinedTextField(
                    value = date,
                    onValueChange = { date = it },
                    label = { Text(stringResource(R.string.date_format)) },
                    modifier = Modifier.fillMaxWidth()
                )

                // Turmas disponíveis no período
                Text("Selecione a Turma - $currentPeriod", fontWeight = FontWeight.Bold)

                if (classesForCurrentPeriod.isEmpty()) {
                    Text("Nenhuma turma disponível neste período.")
                } else {
                    classesForCurrentPeriod.forEach { className ->
                        Button(
                            onClick = { selectedClass = className },
                            modifier = Modifier.fillMaxWidth(),
                            enabled = true
                        ) {
                            Text(className)
                        }
                    }
                }

                // Botão Fazer Chamada
                Button(
                    onClick = {
                        if (selectedClass.isBlank()) {
                            showErrorMessage = "Selecione uma turma"
                        } else if (date.isBlank()) {
                            showErrorMessage = "Defina uma data"
                        } else {
                            try {
                                onTakeAttendance(selectedClass, date)
                            } catch (e: Exception) {
                                showErrorMessage = "Erro ao fazer chamada: ${e.message}"
                            }
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = selectedClass.isNotBlank() && date.isNotBlank()
                ) {
                    Text(stringResource(R.string.take_attendance_button))
                }

                // Mostrar mensagem de erro se houver
                if (showErrorMessage.isNotBlank()) {
                    Text(
                        showErrorMessage,
                        color = androidx.compose.material3.MaterialTheme.colorScheme.error,
                        modifier = Modifier.padding(top = 8.dp)
                    )
                }
            }
        }
    }
}
