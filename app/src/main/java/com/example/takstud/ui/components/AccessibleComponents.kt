package com.example.takstud.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp

/**
 * AccessibleComponents - Componentes com acessibilidade nativa.
 *
 * FUNCIONALIDADES:
 * - Componentes seguros WCAG 2.1
 * - Content descriptions
 * - Tamanhos mínimos de toque (48dp)
 * - Contraste adequado
 * - Navegação por teclado
 *
 * EXEMPLO DE USO:
 * AccessibleButton(
 *     text = "Enviar",
 *     onClick = { },
 *     contentDescription = "Clique para enviar formulário"
 * )
 */

/**
 * Button acessível com tamanho mínimo de toque (48x48dp).
 */
@Composable
fun AccessibleButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    contentDescription: String = text,
    enabled: Boolean = true,
    isLoading: Boolean = false
) {
    Button(
        onClick = onClick,
        enabled = enabled && !isLoading,
        modifier = modifier
            .height(48.dp)
            .semantics {
                this.contentDescription = contentDescription
            }
    ) {
        if (isLoading) {
            CircularProgressIndicator(
                modifier = Modifier
                    .size(20.dp)
                    .padding(end = 8.dp),
                strokeWidth = 2.dp,
                color = MaterialTheme.colorScheme.onPrimary
            )
        }
        Text(text)
    }
}

/**
 * Text field acessível com rótulo e mensagem de erro.
 */
@Composable
fun AccessibleTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    modifier: Modifier = Modifier,
    error: String? = null,
    placeholder: String? = null,
    isRequired: Boolean = false
) {
    Column(modifier = modifier) {
        val errorText = if (isRequired && value.isEmpty()) "Campo obrigatório" else error

        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            label = {
                Text(if (isRequired) "$label *" else label)
            },
            placeholder = placeholder?.let { { Text(it) } },
            isError = errorText != null,
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
                .semantics {
                    this.contentDescription = label
                },
            singleLine = true
        )

        if (errorText != null) {
            Text(
                text = errorText,
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.labelSmall,
                modifier = Modifier
                    .padding(top = 4.dp)
                    .semantics {
                        this.contentDescription = "Erro: $errorText"
                    }
            )
        }
    }
}

/**
 * Checkbox acessível.
 */
@Composable
fun AccessibleCheckbox(
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    label: String,
    modifier: Modifier = Modifier,
    enabled: Boolean = true
) {
    Row(
        modifier = modifier
            .clickable(
                enabled = enabled,
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = { onCheckedChange(!checked) }
            )
            .semantics {
                this.contentDescription = label
            }
            .padding(12.dp)
            .height(48.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Checkbox(
            checked = checked,
            onCheckedChange = onCheckedChange,
            enabled = enabled
        )
        Spacer(modifier = Modifier.width(12.dp))
        Text(
            text = label,
            modifier = Modifier
                .padding(vertical = 8.dp)
                .weight(1f)
        )
    }
}

/**
 * Radio button acessível.
 */
@Composable
fun AccessibleRadioButton(
    selected: Boolean,
    onClick: () -> Unit,
    label: String,
    modifier: Modifier = Modifier,
    enabled: Boolean = true
) {
    Row(
        modifier = modifier
            .clickable(
                enabled = enabled,
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onClick
            )
            .semantics {
                this.contentDescription = label
            }
            .padding(12.dp)
            .height(48.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        RadioButton(
            selected = selected,
            onClick = onClick,
            enabled = enabled
        )
        Spacer(modifier = Modifier.width(12.dp))
        Text(
            text = label,
            modifier = Modifier
                .padding(vertical = 8.dp)
                .weight(1f)
        )
    }
}

/**
 * Card acessível com possível ação.
 */
@Composable
fun AccessibleCard(
    modifier: Modifier = Modifier,
    title: String,
    onClick: (() -> Unit)? = null,
    contentDescription: String? = null,
    content: @Composable () -> Unit
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .then(
                if (onClick != null) {
                    Modifier.clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null,
                        onClick = onClick
                    )
                } else {
                    Modifier
                }
            )
            .semantics {
                this.contentDescription = contentDescription ?: title
            }
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium
            )
            Spacer(modifier = Modifier.height(8.dp))
            content()
        }
    }
}

/**
 * Alert dialog acessível.
 */
@Composable
fun AccessibleAlertDialog(
    showDialog: Boolean,
    title: String,
    message: String,
    confirmText: String = "OK",
    dismissText: String = "Cancelar",
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    if (showDialog) {
        AlertDialog(
            onDismissRequest = onDismiss,
            title = { Text(title) },
            text = { Text(message) },
            confirmButton = {
                TextButton(
                    onClick = onConfirm,
                    modifier = Modifier.height(48.dp)
                ) {
                    Text(confirmText)
                }
            },
            dismissButton = {
                TextButton(
                    onClick = onDismiss,
                    modifier = Modifier.height(48.dp)
                ) {
                    Text(dismissText)
                }
            }
        )
    }
}

/**
 * Tab row acessível.
 */
@Composable
fun AccessibleTabRow(
    selectedTabIndex: Int,
    tabs: List<String>,
    onTabSelected: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    TabRow(
        selectedTabIndex = selectedTabIndex,
        modifier = modifier
            .semantics {
                this.contentDescription = "Abas: ${tabs.joinToString(", ")}"
            }
    ) {
        tabs.forEachIndexed { index, tab ->
            Tab(
                selected = index == selectedTabIndex,
                onClick = { onTabSelected(index) },
                modifier = Modifier
                    .height(48.dp)
                    .semantics {
                        contentDescription = tab
                    }
            ) {
                Text(tab)
            }
        }
    }
}

/**
 * Snackbar acessível.
 */
@Composable
fun AccessibleSnackbar(
    message: String,
    actionLabel: String? = null,
    onAction: (() -> Unit)? = null,
    modifier: Modifier = Modifier,
    duration: Long = 4000
) {
    Snackbar(
        modifier = modifier
            .semantics {
                this.contentDescription = message
            },
        action = if (actionLabel != null && onAction != null) {
            {
                TextButton(
                    onClick = onAction,
                    modifier = Modifier.height(40.dp)
                ) {
                    Text(actionLabel)
                }
            }
        } else null
    ) {
        Text(message)
    }
}

/**
 * Floating Action Button acessível.
 */
@Composable
fun AccessibleFAB(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    contentDescription: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    FloatingActionButton(
        onClick = onClick,
        modifier = modifier
            .size(56.dp)
            .semantics {
                this.contentDescription = contentDescription
            }
    ) {
        Icon(icon, contentDescription)
    }
}

/**
 * Dismiss button (X) acessível.
 */
@Composable
fun AccessibleDismissButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    contentDescription: String = "Fechar"
) {
    IconButton(
        onClick = onClick,
        modifier = modifier
            .size(48.dp)
            .semantics {
                this.contentDescription = contentDescription
            }
    ) {
        Icon(Icons.Default.Close, contentDescription)
    }
}
