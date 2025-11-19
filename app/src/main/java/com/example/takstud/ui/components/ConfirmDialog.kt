package com.example.takstud.ui.components

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color

/**
 * ConfirmDialog - Dialog de confirmação reutilizável
 * Usado para confirmar ações como deletar
 */
@Composable
fun ConfirmDialog(
    title: String,
    message: String,
    confirmText: String = "Sim",
    cancelText: String = "Cancelar",
    isDangerous: Boolean = false,
    onConfirm: () -> Unit,
    onCancel: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = title,
                style = MaterialTheme.typography.headlineSmall
            )
        },
        text = {
            Text(
                text = message,
                style = MaterialTheme.typography.bodyMedium
            )
        },
        confirmButton = {
            Button(
                onClick = {
                    onConfirm()
                    onDismiss()
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (isDangerous) Color(0xFFD32F2F) else MaterialTheme.colorScheme.primary
                )
            ) {
                Text(confirmText)
            }
        },
        dismissButton = {
            TextButton(onClick = onCancel) {
                Text(cancelText)
            }
        }
    )
}

/**
 * ConfirmDeleteDialog - Dialog específico para deletar
 */
@Composable
fun ConfirmDeleteDialog(
    itemName: String,
    onConfirm: () -> Unit,
    onCancel: () -> Unit,
    onDismiss: () -> Unit
) {
    ConfirmDialog(
        title = "Deletar $itemName",
        message = "Tem certeza que deseja deletar este(a) $itemName? Esta ação não pode ser desfeita.",
        confirmText = "Deletar",
        cancelText = "Cancelar",
        isDangerous = true,
        onConfirm = onConfirm,
        onCancel = onCancel,
        onDismiss = onDismiss
    )
}

/**
 * ConfirmLogoutDialog - Dialog para logout
 */
@Composable
fun ConfirmLogoutDialog(
    onConfirm: () -> Unit,
    onCancel: () -> Unit,
    onDismiss: () -> Unit
) {
    ConfirmDialog(
        title = "Sair",
        message = "Tem certeza que deseja sair?",
        confirmText = "Sair",
        cancelText = "Cancelar",
        isDangerous = false,
        onConfirm = onConfirm,
        onCancel = onCancel,
        onDismiss = onDismiss
    )
}
