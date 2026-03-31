package com.example.takstud.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.takstud.data.repository.NotificationRepository
import com.example.takstud.model.Notification
import com.example.takstud.model.NotificationPriority
import com.example.takstud.model.NotificationSettings
import com.example.takstud.model.NotificationType
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ExpandedNotificationViewModel - Gerencia sistema completo de notificações.
 *
 * Funcionalidades:
 * - Listagem e filtragem de notificações
 * - Marcação como lida
 * - Envio de notificações
 * - Gerenciamento de configurações
 * - Contadores e badges
 */
@HiltViewModel
class ExpandedNotificationViewModel @Inject constructor(
    private val notificationRepository: NotificationRepository
) : ViewModel() {

    // Estado do usuário atual
    private val _currentUserId = MutableStateFlow("")
    val currentUserId: StateFlow<String> = _currentUserId.asStateFlow()

    // Todas as notificações do usuário
    private val _userNotifications = MutableStateFlow<List<Notification>>(emptyList())
    val userNotifications: StateFlow<List<Notification>> = _userNotifications.asStateFlow()

    // Notificações não lidas
    private val _unreadNotifications = MutableStateFlow<List<Notification>>(emptyList())
    val unreadNotifications: StateFlow<List<Notification>> = _unreadNotifications.asStateFlow()

    // Contador de não lidas
    val unreadCount: StateFlow<Int> = MutableStateFlow(0).apply {
        viewModelScope.launch {
            currentUserId.collect { userId ->
                if (userId.isNotEmpty()) {
                    notificationRepository.getUnreadCount(userId).collect { count ->
                        value = count
                    }
                }
            }
        }
    }

    // Filtros
    private val _selectedType = MutableStateFlow<NotificationType?>(null)
    val selectedType: StateFlow<NotificationType?> = _selectedType.asStateFlow()

    private val _selectedPriority = MutableStateFlow<NotificationPriority?>(null)
    val selectedPriority: StateFlow<NotificationPriority?> = _selectedPriority.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    // Notificações filtradas
    private val _filteredNotifications = MutableStateFlow<List<Notification>>(emptyList())
    val filteredNotifications: StateFlow<List<Notification>> = _filteredNotifications.asStateFlow()

    // Configurações do usuário
    private val _notificationSettings = MutableStateFlow<NotificationSettings?>(null)
    val notificationSettings: StateFlow<NotificationSettings?> = _notificationSettings.asStateFlow()

    // Estado de UI
    private val _uiState = MutableStateFlow<NotificationUiState>(NotificationUiState.Idle)
    val uiState: StateFlow<NotificationUiState> = _uiState.asStateFlow()

    // Notificação selecionada
    private val _selectedNotification = MutableStateFlow<Notification?>(null)
    val selectedNotification: StateFlow<Notification?> = _selectedNotification.asStateFlow()

    /**
     * Define o usuário atual e carrega suas notificações
     */
    fun setCurrentUser(userId: String) {
        _currentUserId.value = userId
        loadUserNotifications(userId)
        loadNotificationSettings(userId)
    }

    /**
     * Carrega todas as notificações do usuário
     */
    private fun loadUserNotifications(userId: String) {
        viewModelScope.launch {
            notificationRepository.getNotificationsByUser(userId).collect { notifications ->
                _userNotifications.value = notifications
                applyFilters()
            }
        }
    }

    /**
     * Carrega notificações não lidas
     */
    fun loadUnreadNotifications(userId: String) {
        viewModelScope.launch {
            notificationRepository.getUnreadNotifications(userId).collect { notifications ->
                _unreadNotifications.value = notifications
            }
        }
    }

    /**
     * Carrega configurações de notificação
     */
    private fun loadNotificationSettings(userId: String) {
        viewModelScope.launch {
            val settings = notificationRepository.getNotificationSettings(userId)
            _notificationSettings.value = settings ?: NotificationSettings(userId = userId)
        }
    }

    /**
     * Marca notificação como lida
     */
    fun markAsRead(notificationId: String) {
        viewModelScope.launch {
            val result = notificationRepository.markAsRead(notificationId)
            if (result.isFailure) {
                _uiState.value = NotificationUiState.Error("Erro ao marcar como lida")
            }
        }
    }

    /**
     * Marca todas como lidas
     */
    fun markAllAsRead() {
        viewModelScope.launch {
            _uiState.value = NotificationUiState.Loading
            val result = notificationRepository.markAllAsRead(_currentUserId.value)
            _uiState.value = if (result.isSuccess) {
                NotificationUiState.Success("Todas marcadas como lidas")
            } else {
                NotificationUiState.Error("Erro ao marcar todas como lidas")
            }
        }
    }

    /**
     * Envia notificação para usuário
     */
    fun sendToUser(
        targetUserId: String,
        title: String,
        message: String,
        type: NotificationType = NotificationType.GENERAL,
        priority: NotificationPriority = NotificationPriority.NORMAL,
        relatedEntityId: String = "",
        relatedEntityType: String = ""
    ) {
        viewModelScope.launch {
            _uiState.value = NotificationUiState.Loading
            val result = notificationRepository.sendToUser(
                userId = targetUserId,
                title = title,
                message = message,
                type = type,
                priority = priority,
                senderId = _currentUserId.value,
                relatedEntityId = relatedEntityId,
                relatedEntityType = relatedEntityType
            )
            _uiState.value = if (result.isSuccess) {
                NotificationUiState.Success("Notificação enviada")
            } else {
                NotificationUiState.Error("Erro ao enviar notificação")
            }
        }
    }

    /**
     * Envia notificação para role (todos professores, todos pais, etc)
     */
    fun sendToRole(
        role: String,
        title: String,
        message: String,
        type: NotificationType = NotificationType.GENERAL,
        priority: NotificationPriority = NotificationPriority.NORMAL
    ) {
        viewModelScope.launch {
            _uiState.value = NotificationUiState.Loading
            val result = notificationRepository.sendToRole(
                role = role,
                title = title,
                message = message,
                type = type,
                priority = priority,
                senderId = _currentUserId.value
            )
            _uiState.value = if (result.isSuccess) {
                NotificationUiState.Success("Notificação enviada para $role")
            } else {
                NotificationUiState.Error("Erro ao enviar notificação")
            }
        }
    }

    /**
     * Envia notificação para turma
     */
    fun sendToClass(
        classId: String,
        title: String,
        message: String,
        type: NotificationType = NotificationType.GENERAL,
        priority: NotificationPriority = NotificationPriority.NORMAL
    ) {
        viewModelScope.launch {
            _uiState.value = NotificationUiState.Loading
            val result = notificationRepository.sendToClass(
                classId = classId,
                title = title,
                message = message,
                type = type,
                priority = priority,
                senderId = _currentUserId.value
            )
            _uiState.value = if (result.isSuccess) {
                NotificationUiState.Success("Notificação enviada para a turma")
            } else {
                NotificationUiState.Error("Erro ao enviar notificação")
            }
        }
    }

    /**
     * Deleta notificação
     */
    fun deleteNotification(notificationId: String) {
        viewModelScope.launch {
            val result = notificationRepository.deleteNotification(notificationId)
            if (result.isFailure) {
                _uiState.value = NotificationUiState.Error("Erro ao deletar notificação")
            }
        }
    }

    /**
     * Deleta notificações expiradas
     */
    fun cleanupExpiredNotifications() {
        viewModelScope.launch {
            notificationRepository.deleteExpiredNotifications()
        }
    }

    /**
     * Atualiza configurações de notificação
     */
    fun updateSettings(settings: NotificationSettings) {
        viewModelScope.launch {
            _uiState.value = NotificationUiState.Loading
            val result = notificationRepository.saveNotificationSettings(settings)
            _uiState.value = if (result.isSuccess) {
                _notificationSettings.value = settings
                NotificationUiState.Success("Configurações atualizadas")
            } else {
                NotificationUiState.Error("Erro ao atualizar configurações")
            }
        }
    }

    /**
     * Filtra por tipo
     */
    fun filterByType(type: NotificationType?) {
        _selectedType.value = type
        applyFilters()
    }

    /**
     * Filtra por prioridade
     */
    fun filterByPriority(priority: NotificationPriority?) {
        _selectedPriority.value = priority
        applyFilters()
    }

    /**
     * Busca por texto
     */
    fun search(query: String) {
        _searchQuery.value = query
        applyFilters()
    }

    /**
     * Aplica todos os filtros
     */
    private fun applyFilters() {
        var filtered = _userNotifications.value

        // Filtro por tipo
        _selectedType.value?.let { type ->
            filtered = filtered.filter { it.type == type }
        }

        // Filtro por prioridade
        _selectedPriority.value?.let { priority ->
            filtered = filtered.filter { it.priority == priority }
        }

        // Busca por texto
        if (_searchQuery.value.isNotEmpty()) {
            filtered = filtered.filter {
                it.title.contains(_searchQuery.value, ignoreCase = true) ||
                it.message.contains(_searchQuery.value, ignoreCase = true)
            }
        }

        _filteredNotifications.value = filtered
    }

    /**
     * Limpa todos os filtros
     */
    fun clearFilters() {
        _selectedType.value = null
        _selectedPriority.value = null
        _searchQuery.value = ""
        applyFilters()
    }

    /**
     * Seleciona notificação
     */
    fun selectNotification(notification: Notification?) {
        _selectedNotification.value = notification
        notification?.let {
            if (!it.isRead) {
                markAsRead(it.id)
            }
        }
    }

    /**
     * Limpa estado de UI
     */
    fun clearUiState() {
        _uiState.value = NotificationUiState.Idle
    }
}

// NotificationUiState movido para NotificationViewModel.kt para evitar duplicação
