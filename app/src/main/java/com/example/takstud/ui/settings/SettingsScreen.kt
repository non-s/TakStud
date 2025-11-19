@file:OptIn(ExperimentalMaterial3Api::class)

package com.example.takstud.ui.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.example.takstud.util.ThemeManager
import kotlinx.coroutines.launch

/**
 * SettingsScreen - Tela de configurações da aplicação
 * Permite ao usuário alterar preferências como tema escuro/claro
 */
@Composable
fun SettingsScreen(
    modifier: Modifier = Modifier,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val themeManager = remember { ThemeManager(context) }
    val coroutineScope = rememberCoroutineScope()

    // Observar estado do dark mode
    val isDarkMode = themeManager.isDarkModeFlow().collectAsState(initial = false)
    val isDynamicColors = themeManager.isDynamicColorsFlow().collectAsState(initial = true)

    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = { Text("Configurações") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Default.ArrowBack, "Voltar")
                    }
                }
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            // Seção: Tema
            item {
                SettingsSectionHeader(title = "Aparência")
            }

            item {
                SettingToggle(
                    title = "Modo Escuro",
                    subtitle = "Alternar entre tema claro e escuro",
                    isChecked = isDarkMode.value,
                    icon = {
                        Icon(
                            imageVector = if (isDarkMode.value) Icons.Default.DarkMode else Icons.Default.LightMode,
                            contentDescription = "Ícone de tema"
                        )
                    },
                    onToggle = { enabled ->
                        coroutineScope.launch {
                            themeManager.setDarkMode(enabled)
                        }
                    }
                )
            }

            item {
                SettingToggle(
                    title = "Cores Dinâmicas",
                    subtitle = "Usar cores do Material You (Android 12+)",
                    isChecked = isDynamicColors.value,
                    icon = {
                        Icon(
                            imageVector = Icons.Default.LightMode,
                            contentDescription = "Ícone de cores"
                        )
                    },
                    onToggle = { enabled ->
                        coroutineScope.launch {
                            themeManager.setDynamicColors(enabled)
                        }
                    }
                )
            }

            // Seção: Informações
            item {
                SettingsSectionHeader(title = "Informações")
            }

            item {
                SettingsInfo(
                    title = "Versão da App",
                    subtitle = "1.0"
                )
            }

            item {
                SettingsInfo(
                    title = "Desenvolvido por",
                    subtitle = "TakStud Team"
                )
            }
        }
    }
}

/**
 * SettingsSectionHeader - Cabeçalho de uma seção de configurações
 */
@Composable
fun SettingsSectionHeader(
    title: String,
    modifier: Modifier = Modifier
) {
    Text(
        text = title,
        style = MaterialTheme.typography.titleSmall,
        color = MaterialTheme.colorScheme.primary,
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp)
    )
}

/**
 * SettingToggle - Item de configuração com toggle
 */
@Composable
fun SettingToggle(
    title: String,
    subtitle: String = "",
    isChecked: Boolean,
    onToggle: (Boolean) -> Unit,
    icon: @Composable (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(
            modifier = Modifier
                .weight(1f)
                .padding(end = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (icon != null) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .background(
                            color = MaterialTheme.colorScheme.primaryContainer,
                            shape = MaterialTheme.shapes.small
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    icon()
                }
                Spacer(modifier = Modifier.width(16.dp))
            }

            Column(
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurface
                )
                if (subtitle.isNotEmpty()) {
                    Text(
                        text = subtitle,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        Switch(
            checked = isChecked,
            onCheckedChange = onToggle,
            modifier = Modifier.padding(start = 8.dp)
        )
    }
}

/**
 * SettingsInfo - Item informativo de configurações (somente leitura)
 */
@Composable
fun SettingsInfo(
    title: String,
    subtitle: String = "",
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp)
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurface
        )
        if (subtitle.isNotEmpty()) {
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
