@file:OptIn(ExperimentalMaterial3Api::class)

package com.example.takstud.ui.teacher

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.takstud.ui.theme.*
import java.time.LocalDate
import java.time.format.DateTimeFormatter

/**
 * AttendanceScreen Premium - Seleção de turma para chamada
 */
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

    val periods = listOf("MANHA", "TARDE", "EJA")
    var selectedTabIndex by remember { mutableStateOf(0) }
    val currentPeriod = periods.getOrNull(selectedTabIndex) ?: "MANHA"
    val classesForCurrentPeriod = classesByPeriod[currentPeriod] ?: emptyList()

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        AccentOrange.copy(alpha = 0.03f),
                        Neutral50
                    )
                )
            )
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            // Header
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        brush = Brush.verticalGradient(
                            colors = listOf(AccentOrange, Color(0xFFEA580C))
                        )
                    )
                    .padding(top = 24.dp)
                    .padding(16.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = onBack) {
                        Icon(
                            Icons.AutoMirrored.Default.ArrowBack,
                            "Voltar",
                            tint = Color.White
                        )
                    }
                    Column {
                        Text(
                            text = "Controle de Presença",
                            style = MaterialTheme.typography.headlineSmall,
                            color = Color.White,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Selecione a turma e data",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.White.copy(alpha = 0.8f)
                        )
                    }
                }
            }

            // Tabs
            ScrollableTabRow(
                selectedTabIndex = selectedTabIndex,
                containerColor = Color.White,
                contentColor = AccentOrange,
                edgePadding = 0.dp
            ) {
                periods.forEachIndexed { index, period ->
                    val periodLabel = when (period) {
                        "MANHA" -> "☀️ Manhã"
                        "TARDE" -> "🌤️ Tarde"
                        "EJA" -> "🌙 EJA"
                        else -> period
                    }
                    Tab(
                        selected = selectedTabIndex == index,
                        onClick = { 
                            selectedTabIndex = index
                            selectedClass = ""
                        },
                        text = {
                            Text(
                                periodLabel,
                                fontWeight = if (selectedTabIndex == index) FontWeight.Bold else FontWeight.Normal
                            )
                        }
                    )
                }
            }

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Date Card
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                Icons.Default.CalendarToday,
                                contentDescription = null,
                                tint = AccentOrange
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                "Data da Chamada",
                                style = MaterialTheme.typography.labelLarge,
                                fontWeight = FontWeight.SemiBold,
                                color = Neutral700
                            )
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        OutlinedTextField(
                            value = date,
                            onValueChange = { date = it },
                            modifier = Modifier.fillMaxWidth(),
                            placeholder = { Text("dd/MM/yyyy") },
                            shape = RoundedCornerShape(12.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = AccentOrange,
                                unfocusedBorderColor = Neutral300
                            )
                        )
                    }
                }

                // Classes List
                Text(
                    "Turmas - $currentPeriod",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = Neutral900
                )

                if (classesForCurrentPeriod.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(32.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(text = "📚", fontSize = 48.sp)
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                "Nenhuma turma neste período",
                                style = MaterialTheme.typography.bodyMedium,
                                color = Neutral500
                            )
                        }
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(classesForCurrentPeriod) { className ->
                            ClassSelectionCard(
                                className = className,
                                isSelected = selectedClass == className,
                                onClick = { selectedClass = className }
                            )
                        }
                    }
                }

                // Action Button
                Button(
                    onClick = {
                        if (selectedClass.isNotBlank() && date.isNotBlank()) {
                            onTakeAttendance(selectedClass, date)
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    enabled = selectedClass.isNotBlank() && date.isNotBlank(),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = AccentOrange
                    ),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Icon(Icons.Default.CheckCircle, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        "Fazer Chamada",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

@Composable
fun ClassSelectionCard(
    className: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        onClick = onClick,
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) AccentOrange.copy(alpha = 0.1f) else Color.White
        ),
        border = if (isSelected) 
            androidx.compose.foundation.BorderStroke(2.dp, AccentOrange) 
        else null,
        elevation = CardDefaults.cardElevation(
            defaultElevation = if (isSelected) 4.dp else 2.dp
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = className,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                color = if (isSelected) AccentOrange else Neutral900
            )
            if (isSelected) {
                Icon(
                    Icons.Default.CheckCircle,
                    contentDescription = "Selecionado",
                    tint = AccentOrange
                )
            }
        }
    }
}
