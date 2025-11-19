@file:OptIn(ExperimentalMaterial3Api::class)

package com.example.takstud.ui.settings

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Accessibility
import androidx.compose.material.icons.filled.TextFields
import androidx.compose.material.icons.filled.Contrast
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.example.takstud.accessibility.AccessibilityManager
import kotlinx.coroutines.launch

/**
 * AccessibilitySettingsScreen - Tela de configurações de acessibilidade
 */
@Composable
fun AccessibilitySettingsScreen(
    modifier: Modifier = Modifier,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val accessibilityManager = remember { AccessibilityManager(context) }
    val coroutineScope = rememberCoroutineScope()

    // Estados de acessibilidade
    val largeTextEnabled = accessibilityManager.isLargeTextEnabledFlow().collectAsState(initial = false)
    val highContrastEnabled = accessibilityManager.isHighContrastEnabledFlow().collectAsState(initial = false)
    val reducedMotionEnabled = accessibilityManager.isReducedMotionEnabledFlow().collectAsState(initial = false)
    val hapticFeedbackEnabled = accessibilityManager.isHapticFeedbackEnabledFlow().collectAsState(initial = true)
    val textScale = accessibilityManager.getTextScaleFlow().collectAsState(initial = 1.0f)

    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = { Text("Acessibilidade") },
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
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Seção: Visão
            item {
                Text(
                    text = "Visão",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.primary
                )
            }

            item {
                AccessibilityToggleItem(
                    title = "Texto Ampliado",
                    subtitle = "Aumentar tamanho de fontes",
                    isEnabled = largeTextEnabled.value,
                    icon = Icons.Default.TextFields,
                    onToggle = { enabled ->
                        coroutineScope.launch {
                            accessibilityManager.setLargeTextEnabled(enabled)
                        }
                    }
                )
            }

            item {
                AccessibilityToggleItem(
                    title = "Alto Contraste",
                    subtitle = "Melhorar contraste de cores",
                    isEnabled = highContrastEnabled.value,
                    icon = Icons.Default.Contrast,
                    onToggle = { enabled ->
                        coroutineScope.launch {
                            accessibilityManager.setHighContrastEnabled(enabled)
                        }
                    }
                )
            }

            item {
                AccessibilitySliderItem(
                    title = "Escala de Texto",
                    subtitle = "Ajustar tamanho de toda a interface",
                    currentValue = textScale.value,
                    minValue = 0.8f,
                    maxValue = 2.0f,
                    onValueChange = { scale ->
                        coroutineScope.launch {
                            accessibilityManager.setTextScale(scale)
                        }
                    }
                )
            }

            // Seção: Movimento
            item {
                Text(
                    text = "Movimento",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(top = 16.dp)
                )
            }

            item {
                AccessibilityToggleItem(
                    title = "Movimento Reduzido",
                    subtitle = "Desabilitar animações e transições",
                    isEnabled = reducedMotionEnabled.value,
                    icon = Icons.Default.Accessibility,
                    onToggle = { enabled ->
                        coroutineScope.launch {
                            accessibilityManager.setReducedMotionEnabled(enabled)
                        }
                    }
                )
            }

            item {
                AccessibilityToggleItem(
                    title = "Feedback Háptico",
                    subtitle = "Vibrações ao interagir com botões",
                    isEnabled = hapticFeedbackEnabled.value,
                    icon = Icons.Default.Accessibility,
                    onToggle = { enabled ->
                        coroutineScope.launch {
                            accessibilityManager.setHapticFeedbackEnabled(enabled)
                        }
                    }
                )
            }

            // Seção: Presets
            item {
                Text(
                    text = "Presets",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(top = 16.dp)
                )
            }

            item {
                AccessibilityPresetButton(
                    title = "Visão Baixa",
                    subtitle = "Texto ampliado, alto contraste",
                    onClick = {
                        coroutineScope.launch {
                            accessibilityManager.enableLowVisionPreset()
                        }
                    }
                )
            }

            item {
                AccessibilityPresetButton(
                    title = "Sensibilidade a Movimento",
                    subtitle = "Movimento reduzido, sem vibração",
                    onClick = {
                        coroutineScope.launch {
                            accessibilityManager.enableMotionSensitivityPreset()
                        }
                    }
                )
            }

            item {
                AccessibilityPresetButton(
                    title = "Habilitar Tudo",
                    subtitle = "Ativar todas as configurações",
                    onClick = {
                        coroutineScope.launch {
                            accessibilityManager.enableAllAccessibilityFeatures()
                        }
                    }
                )
            }

            // Botão para resetar
            item {
                OutlinedButton(
                    onClick = {
                        coroutineScope.launch {
                            accessibilityManager.resetAllSettings()
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 16.dp)
                ) {
                    Text("Resetar para Padrão")
                }
            }
        }
    }
}

/**
 * AccessibilityToggleItem - Item com toggle para acessibilidade
 */
@Composable
fun AccessibilityToggleItem(
    title: String,
    subtitle: String,
    isEnabled: Boolean,
    icon: ImageVector,
    onToggle: (Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.medium
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                modifier = Modifier.weight(1f),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = title,
                    modifier = Modifier.size(32.dp)
                )
                Column {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleSmall
                    )
                    Text(
                        text = subtitle,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Switch(
                checked = isEnabled,
                onCheckedChange = onToggle
            )
        }
    }
}

/**
 * AccessibilitySliderItem - Item com slider para acessibilidade
 */
@Composable
fun AccessibilitySliderItem(
    title: String,
    subtitle: String,
    currentValue: Float,
    minValue: Float,
    maxValue: Float,
    onValueChange: (Float) -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.medium
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Column {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleSmall
                )
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Slider(
                value = currentValue,
                onValueChange = onValueChange,
                valueRange = minValue..maxValue,
                steps = 12,
                modifier = Modifier.fillMaxWidth()
            )

            Text(
                text = "%.1fx".format(currentValue),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.primary
            )
        }
    }
}

/**
 * AccessibilityPresetButton - Botão para preset de acessibilidade
 */
@Composable
fun AccessibilityPresetButton(
    title: String,
    subtitle: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = MaterialTheme.shapes.medium,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.secondaryContainer
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.onSecondaryContainer
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.7f)
            )
        }
    }
}
