package com.example.takstud.ui.components

import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHostState
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

/**
 * SnackbarManager - Gerencia mensagens de feedback visual
 * Usado para mostrar sucesso, erro, warning, info
 */
class SnackbarManager(
    private val snackbarHostState: SnackbarHostState,
    private val coroutineScope: CoroutineScope
) {

    fun showSuccess(message: String, duration: SnackbarDuration = SnackbarDuration.Short) {
        coroutineScope.launch {
            snackbarHostState.showSnackbar(
                message = message,
                duration = duration
            )
        }
    }

    fun showError(message: String, duration: SnackbarDuration = SnackbarDuration.Long) {
        coroutineScope.launch {
            snackbarHostState.showSnackbar(
                message = "❌ $message",
                duration = duration
            )
        }
    }

    fun showWarning(message: String, duration: SnackbarDuration = SnackbarDuration.Short) {
        coroutineScope.launch {
            snackbarHostState.showSnackbar(
                message = "⚠️ $message",
                duration = duration
            )
        }
    }

    fun showInfo(message: String, duration: SnackbarDuration = SnackbarDuration.Short) {
        coroutineScope.launch {
            snackbarHostState.showSnackbar(
                message = "ℹ️ $message",
                duration = duration
            )
        }
    }

    fun show(message: String, duration: SnackbarDuration = SnackbarDuration.Short) {
        coroutineScope.launch {
            snackbarHostState.showSnackbar(
                message = message,
                duration = duration
            )
        }
    }
}
