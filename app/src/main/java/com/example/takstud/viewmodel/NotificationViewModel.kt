package com.example.takstud.viewmodel

import androidx.lifecycle.viewModelScope
import com.example.takstud.service.PushNotification
import com.example.takstud.ui.common.BaseViewModel
import com.example.takstud.ui.common.UiState
import com.example.takstud.util.NotificationManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import android.content.Context

/**
 * NotificationViewModel - Gerencia notificações no app.
 *
 * FUNCIONALIDADES:
 * - Carregamento de notificações não lidas
 * - Marcação como lida
 * - Gerenciamento de preferências
 * - Busca e filtro de notificações
 *
 * ESTADOS:
 * - Loading: Carregando notificações
 * - Success: Lista de notificações carregada
 * - Error: Erro ao carregar
 * - Empty: Nenhuma notificação
 *
 * EXEMPLO DE USO:
 * val viewModel = NotificationViewModel(context)
 * val uiState by viewModel.uiState.collectAsState()
 * val preferences by viewModel.preferences.collectAsState()
 *
 * // Carregar notificações
 * viewModel.loadNotifications(userId)
 *
 * // Marcar como lida
 * viewModel.markAsRead(userId, notificationId)
 */
class NotificationViewModel(context: Context) :
    BaseViewModel<List<PushNotification>>() {

    private val notificationManager = NotificationManager(context)

    // Estado para preferências
    private val _preferences = MutableStateFlow(NotificationManager.NotificationPreferences())
    val preferences: StateFlow<NotificationManager.NotificationPreferences> = _preferences.asStateFlow()

    // Estado para notificações filtradas
    private val _filteredNotifications = MutableStateFlow<List<PushNotification>>(emptyList())
    val filteredNotifications: StateFlow<List<PushNotification>> = _filteredNotifications.asStateFlow()

    // Filtro ativo
    private val _activeFilter = MutableStateFlow<String?>(null)
    val activeFilter: StateFlow<String?> = _activeFilter.asStateFlow()

    /**
     * Carrega notificações não lidas do usuário.
     */
    fun loadNotifications(userId: String) = launchUI("Carregando notificações...") {
        try {
            val notifications = notificationManager.getUnreadNotifications(userId)

            if (notifications.isEmpty()) {
                setEmpty("Nenhuma notificação não lida")
            } else {
                setSuccess(notifications, "Notificações carregadas")
                _filteredNotifications.value = notifications
            }

        } catch (e: Exception) {
            setError(
                "Erro ao carregar notificações: ${e.message}",
                e,
                retryable = true
            )
        }
    }

    /**
     * Marca uma notificação como lida.
     */
    fun markAsRead(userId: String, notificationId: String) = viewModelScope.launch {
        try {
            val success = notificationManager.markNotificationAsRead(userId, notificationId)
            if (success) {
                // Remover da lista filtrada
                val current = _filteredNotifications.value.toMutableList()
                // Note: em um app real, você teria o ID da notificação no objeto
                _filteredNotifications.value = current
            }
        } catch (e: Exception) {
            // Log error
        }
    }

    /**
     * Carrega preferências de notificação do usuário.
     */
    fun loadPreferences(userId: String) = viewModelScope.launch {
        try {
            val prefs = notificationManager.getNotificationPreferences(userId)
            _preferences.value = prefs
        } catch (e: Exception) {
            // Log error
        }
    }

    /**
     * Atualiza preferências de notificação.
     */
    fun updatePreferences(
        userId: String,
        preferences: NotificationManager.NotificationPreferences
    ) = viewModelScope.launch {
        try {
            val success = notificationManager.updateNotificationPreferences(userId, preferences)
            if (success) {
                _preferences.value = preferences
            }
        } catch (e: Exception) {
            // Log error
        }
    }

    /**
     * Filtra notificações por tipo.
     */
    fun filterByType(type: String?) {
        val current = (uiState.value as? UiState.Success)?.data ?: return

        _activeFilter.value = type

        if (type == null) {
            _filteredNotifications.value = current
        } else {
            _filteredNotifications.value = current.filter { it.type == type }
        }
    }

    /**
     * Filtra notificações por texto.
     */
    fun searchNotifications(query: String) {
        val current = (uiState.value as? UiState.Success)?.data ?: return

        if (query.isEmpty()) {
            _filteredNotifications.value = current
        } else {
            _filteredNotifications.value = current.filter {
                it.title.contains(query, ignoreCase = true) ||
                it.body.contains(query, ignoreCase = true)
            }
        }
    }

    /**
     * Tipo de notificação para filtro.
     */
    enum class NotificationType(val displayName: String, val typeId: String) {
        GRADES("Notas", "grade_released"),
        ATTENDANCE("Frequência", "absence_alert"),
        ANNOUNCEMENTS("Avisos", "announcement"),
        EVENTS("Eventos", "event"),
        MESSAGES("Mensagens", "parent_message")
    }

    /**
     * Retry da última operação.
     */
    override fun retry() {
        // Implementar retry da última operação
    }
}
