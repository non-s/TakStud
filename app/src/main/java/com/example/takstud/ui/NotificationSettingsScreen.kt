package com.example.takstud.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Announcement
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.takstud.model.NotificationSettings
import com.example.takstud.ui.theme.*
import com.example.takstud.viewmodel.NotificationViewModel

/**
 * NotificationSettingsScreen - Tela de Configurações de Notificações
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotificationSettingsScreen(
    viewModel: NotificationViewModel = hiltViewModel(),
    onBack: () -> Unit = {}
) {
    val settings by viewModel.notificationSettings.collectAsState()
    val uiState by viewModel.uiState.collectAsState()

    var currentSettings by remember {
        mutableStateOf(settings ?: NotificationSettings())
    }

    LaunchedEffect(settings) {
        settings?.let { currentSettings = it }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Configurações de Notificações",
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.Bold
                        )
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Voltar")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = PrimaryBlue,
                    titleContentColor = Color.White,
                    navigationIconContentColor = Color.White
                )
            )
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                Text(
                    "Geral",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold
                    ),
                    color = Neutral700
                )
            }

            item {
                SettingSwitch(
                    title = "Notificações Push",
                    description = "Receber notificações no dispositivo",
                    checked = currentSettings.enablePushNotifications,
                    onCheckedChange = {
                        currentSettings = currentSettings.copy(enablePushNotifications = it)
                    },
                    icon = Icons.Filled.Notifications
                )
            }

            item {
                SettingSwitch(
                    title = "Notificações por Email",
                    description = "Receber resumo diário por email",
                    checked = currentSettings.enableEmailNotifications,
                    onCheckedChange = {
                        currentSettings = currentSettings.copy(enableEmailNotifications = it)
                    },
                    icon = Icons.Filled.Email
                )
            }

            item {
                HorizontalDivider()
            }

            item {
                Text(
                    "Tipos de Notificação",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold
                    ),
                    color = Neutral700
                )
            }

            item {
                SettingSwitch(
                    title = "Tarefas",
                    description = "Novos lembretes de tarefas e prazos",
                    checked = currentSettings.enableTaskReminders,
                    onCheckedChange = {
                        currentSettings = currentSettings.copy(enableTaskReminders = it)
                    },
                    icon = Icons.Filled.Task
                )
            }

            item {
                SettingSwitch(
                    title = "Notas",
                    description = "Quando novas notas são lançadas",
                    checked = currentSettings.enableGradeNotifications,
                    onCheckedChange = {
                        currentSettings = currentSettings.copy(enableGradeNotifications = it)
                    },
                    icon = Icons.Filled.Grade
                )
            }

            item {
                SettingSwitch(
                    title = "Frequência",
                    description = "Alertas de presença e faltas",
                    checked = currentSettings.enableAttendanceNotifications,
                    onCheckedChange = {
                        currentSettings = currentSettings.copy(enableAttendanceNotifications = it)
                    },
                    icon = Icons.Filled.CheckCircle
                )
            }

            item {
                SettingSwitch(
                    title = "Eventos",
                    description = "Lembretes de eventos e atividades",
                    checked = currentSettings.enableEventReminders,
                    onCheckedChange = {
                        currentSettings = currentSettings.copy(enableEventReminders = it)
                    },
                    icon = Icons.Filled.Event
                )
            }

            item {
                SettingSwitch(
                    title = "Avisos Gerais",
                    description = "Comunicados e anúncios da escola",
                    checked = currentSettings.enableGeneralAnnouncements,
                    onCheckedChange = {
                        currentSettings = currentSettings.copy(enableGeneralAnnouncements = it)
                    },
                    icon = Icons.AutoMirrored.Filled.Announcement
                )
            }

            item {
                HorizontalDivider()
            }

            item {
                Text(
                    "Horário Silencioso",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold
                    ),
                    color = Neutral700
                )
            }

            item {
                SettingSwitch(
                    title = "Ativar Horário Silencioso",
                    description = "Não receber notificações em horários específicos",
                    checked = currentSettings.enableQuietHours,
                    onCheckedChange = {
                        currentSettings = currentSettings.copy(enableQuietHours = it)
                    },
                    icon = Icons.Filled.Bedtime
                )
            }

            if (currentSettings.enableQuietHours) {
                item {
                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = Neutral100
                        )
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            TimePickerField(
                                label = "Início",
                                value = currentSettings.quietHoursStart,
                                onValueChange = {
                                    currentSettings = currentSettings.copy(quietHoursStart = it)
                                }
                            )

                            TimePickerField(
                                label = "Fim",
                                value = currentSettings.quietHoursEnd,
                                onValueChange = {
                                    currentSettings = currentSettings.copy(quietHoursEnd = it)
                                }
                            )
                        }
                    }
                }
            }

            item {
                Spacer(modifier = Modifier.height(16.dp))
            }

            item {
                Button(
                    onClick = {
                        viewModel.saveNotificationSettings(currentSettings)
                    },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = PrimaryBlue
                    )
                ) {
                    Icon(Icons.Filled.Save, "Salvar")
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Salvar Configurações")
                }
            }
        }
    }
}

@Composable
fun SettingSwitch(
    title: String,
    description: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    icon: androidx.compose.ui.graphics.vector.ImageVector
) {
    Card(
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = PrimaryBlue,
                modifier = Modifier.size(24.dp)
            )

            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.bodyLarge.copy(
                        fontWeight = FontWeight.Medium
                    ),
                    color = Neutral900
                )
                Text(
                    text = description,
                    style = MaterialTheme.typography.bodySmall,
                    color = Neutral600
                )
            }

            Switch(
                checked = checked,
                onCheckedChange = onCheckedChange,
                colors = SwitchDefaults.colors(
                    checkedThumbColor = Color.White,
                    checkedTrackColor = PrimaryBlue,
                    uncheckedThumbColor = Color.White,
                    uncheckedTrackColor = Neutral300
                )
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TimePickerField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label) },
        placeholder = { Text("HH:mm") },
        modifier = Modifier.fillMaxWidth(),
        singleLine = true,
        trailingIcon = {
            Icon(Icons.Filled.AccessTime, null)
        }
    )
}