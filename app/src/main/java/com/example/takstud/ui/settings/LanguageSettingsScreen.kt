@file:OptIn(ExperimentalMaterial3Api::class)

package com.example.takstud.ui.settings

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.example.takstud.util.LocalizationManager
import kotlinx.coroutines.launch

/**
 * LanguageSettingsScreen - Tela para selecionar idioma da aplicação
 */
@Composable
fun LanguageSettingsScreen(
    modifier: Modifier = Modifier,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val localizationManager = remember { LocalizationManager(context) }
    val coroutineScope = rememberCoroutineScope()

    // Observar idioma atual
    val currentLanguage = localizationManager.getCurrentLanguageFlow().collectAsState(
        initial = LocalizationManager.PT_BR
    )

    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = { Text("Idioma") },
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
            item {
                Text(
                    text = "Selecione o idioma da aplicação",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(16.dp)
                )
            }

            items(LocalizationManager.SUPPORTED_LANGUAGES.size) { index ->
                val (languageCode, languageName) = LocalizationManager.SUPPORTED_LANGUAGES.entries.toList()[index]
                val isSelected = currentLanguage.value == languageCode

                LanguageOptionCard(
                    languageName = languageName,
                    languageCode = languageCode,
                    isSelected = isSelected,
                    onSelect = {
                        coroutineScope.launch {
                            localizationManager.setLanguage(languageCode)
                        }
                    }
                )
            }

            item {
                Divider(modifier = Modifier.padding(vertical = 16.dp))
            }

            item {
                ResetLanguageButton(
                    onReset = {
                        coroutineScope.launch {
                            localizationManager.resetToSystemLanguage()
                        }
                    }
                )
            }
        }
    }
}

/**
 * LanguageOptionCard - Card para selecionar um idioma
 */
@Composable
fun LanguageOptionCard(
    languageName: String,
    languageCode: String,
    isSelected: Boolean,
    onSelect: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .clickable { onSelect() },
        shape = MaterialTheme.shapes.medium,
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected)
                MaterialTheme.colorScheme.primaryContainer
            else
                MaterialTheme.colorScheme.surface,
            contentColor = if (isSelected)
                MaterialTheme.colorScheme.onPrimaryContainer
            else
                MaterialTheme.colorScheme.onSurface
        ),
        border = if (isSelected)
            androidx.compose.foundation.BorderStroke(
                2.dp,
                MaterialTheme.colorScheme.primary
            )
        else
            null
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = languageName,
                    style = MaterialTheme.typography.titleMedium
                )
                Text(
                    text = languageCode,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            if (isSelected) {
                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = "Selecionado",
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(24.dp)
                )
            }
        }
    }
}

/**
 * ResetLanguageButton - Botão para resetar para idioma do sistema
 */
@Composable
fun ResetLanguageButton(
    onReset: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(16.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        OutlinedButton(
            onClick = onReset,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Resetar para idioma do sistema")
        }

        Text(
            text = "Usar o idioma configurado no seu dispositivo",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier
                .padding(top = 8.dp)
                .fillMaxWidth(),
            textAlign = androidx.compose.ui.text.style.TextAlign.Center
        )
    }
}
