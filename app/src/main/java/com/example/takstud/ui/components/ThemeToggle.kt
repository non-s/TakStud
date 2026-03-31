package com.example.takstud.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.takstud.viewmodel.ThemeViewModel

/**
 * Componente de alternância de tema (Light/Dark)
 *
 * Pode ser usado em configurações ou como botão flutuante
 */
@Composable
fun ThemeToggle(
    modifier: Modifier = Modifier,
    showLabel: Boolean = true,
    themeViewModel: ThemeViewModel = viewModel()
) {
    val isDarkMode by themeViewModel.isDarkMode.collectAsState()

    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (showLabel) {
            Icon(
                imageVector = if (isDarkMode) Icons.Default.DarkMode else Icons.Default.LightMode,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary
            )

            Text(
                text = if (isDarkMode) "Modo Escuro" else "Modo Claro",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurface
            )

            Spacer(modifier = Modifier.weight(1f))
        }

        Switch(
            checked = isDarkMode,
            onCheckedChange = { themeViewModel.toggleTheme() },
            colors = SwitchDefaults.colors(
                checkedThumbColor = MaterialTheme.colorScheme.primary,
                checkedTrackColor = MaterialTheme.colorScheme.primaryContainer,
                uncheckedThumbColor = MaterialTheme.colorScheme.outline,
                uncheckedTrackColor = MaterialTheme.colorScheme.surfaceVariant
            )
        )
    }
}

/**
 * Botão de alternância de tema (ícone apenas)
 */
@Composable
fun ThemeToggleButton(
    modifier: Modifier = Modifier,
    themeViewModel: ThemeViewModel = viewModel()
) {
    val isDarkMode by themeViewModel.isDarkMode.collectAsState()

    IconButton(
        onClick = { themeViewModel.toggleTheme() },
        modifier = modifier
    ) {
        Icon(
            imageVector = if (isDarkMode) Icons.Default.LightMode else Icons.Default.DarkMode,
            contentDescription = if (isDarkMode) "Mudar para modo claro" else "Mudar para modo escuro",
            tint = MaterialTheme.colorScheme.onSurface
        )
    }
}

/**
 * Card de configuração de tema
 */
@Composable
fun ThemeSettingCard(
    modifier: Modifier = Modifier,
    themeViewModel: ThemeViewModel = viewModel()
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = "Aparência",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface
            )

            Text(
                text = "Escolha entre tema claro ou escuro",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(8.dp))

            ThemeToggle(themeViewModel = themeViewModel)
        }
    }
}
