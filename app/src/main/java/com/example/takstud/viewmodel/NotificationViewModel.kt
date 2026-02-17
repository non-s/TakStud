package com.example.takstud.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.takstud.data.repository.NotificationRepository
import com.example.takstud.model.Notification
import com.example.takstud.model.NotificationPriority
import com.example.takstud.model.NotificationSettings
import com.example.takstud.model.NotificationType
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * NotificationViewModel - Gerencia estado do sistema de notificações
 *
 * Funcionalidades:
 * - Visualização de notificações por usuário
 * - Filtragem por tipo, prioridade e status
 * - Marcar como lida/não lida
 * - Envio de notificações
 * - Gerenciamento de configurações
 */
@HiltViewModel
class NotificationViewModel @Inject constructor(
    private val notificationRepository: NotificationRepository
) : ViewModel() {

    // ==================== ESTADO DO USUÁRIO ====================

    private val _currentUserId = MutableStateFlow("")
    val currentUserId: StateFlow<String> = _currentUserId.asStateFlow()

    private val _currentUserRole = MutableStateFlow("")
    val currentUserRole: StateFlow<String> = _currentUserRole.asStateFlow()

    // ==================== NOTIFICAÇÕES ====================

    // Todas as notificações do usuário
    val userNotifications: StateFlow<List<Notification>> = _currentUserId
        .flatMapLatest { userId ->
            if (userId.isNotEmpty()) {
                notificationRepository.getNotificationsByUser(userId)
            } else {
                flowOf(emptyList())
            }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Notificações não lidas
    val unreadNotifications: StateFlow<List<Notification>> = _currentUserId
        .flatMapLatest { userId ->
            if (userId.isNotEmpty()) {
                notificationRepository.getUnreadNotifications(userId)
            } else {
                flowOf(emptyList())
            }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Contagem de não lidas
    val unreadCount: StateFlow<Int> = _currentUserId
        .flatMapLatest { userId ->
            if (userId.isNotEmpty()) {
                notificationRepository.getUnreadCount(userId)
            } else {
                flowOf(0)
            }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    // ==================== FILTROS ====================

    private val _selectedType = MutableStateFlow<NotificationType?>(null)
    val selectedType: StateFlow<NotificationType?> = _selectedType.asStateFlow()

    private val _selectedPriority = MutableStateFlow<NotificationPriority?>(null)
    val selectedPriority: StateFlow<NotificationPriority?> = _selectedPriority.asStateFlow()

    private val _showOnlyUnread = MutableStateFlow(false)
    val showOnlyUnread: StateFlow<Boolean> = _showOnlyUnread.asStateFlow()

    // Notificações filtradas
    val filteredNotifications: StateFlow<List<Notification>> = combine(
        userNotifications,
        _selectedType,
        _selectedPriority,
        _showOnlyUnread
    ) { notifications, type, priority, onlyUnread ->
        notifications
            .filter { notification ->
                (type == null || notification.type == type) &&
                (priority == null || notification.priority == priority) &&
                (!onlyUnread || !notification.isRead)
            }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // ==================== NOTIFICAÇÕES POR TIPO ====================

    val taskNotifications: StateFlow<List<Notification>> = _currentUserId
        .flatMapLatest { userId ->
            if (userId.isNotEmpty()) {
                notificationRepository.getNotificationsByType(userId, NotificationType.TASK)
            } else {
                flowOf(emptyList())
            }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val gradeNotifications: StateFlow<List<Notification>> = _currentUserId
        .flatMapLatest { userId ->
            if (userId.isNotEmpty()) {
                notificationRepository.getNotificationsByType(userId, NotificationType.GRADE)
            } else {
                flowOf(emptyList())
            }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val eventNotifications: StateFlow<List<Notification>> = _currentUserId
        .flatMapLatest { userId ->
            if (userId.isNotEmpty()) {
                notificationRepository.getNotificationsByType(userId, NotificationType.EVENT)
            } else {
                flowOf(emptyList())
            }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // ==================== ESTADO DE UI ====================

    private val _uiState = MutableStateFlow<NotificationUiState>(NotificationUiState.Idle)
    val uiState: StateFlow<NotificationUiState> = _uiState.asStateFlow()

    private val _selectedNotification = MutableStateFlow<Notification?>(null)
    val selectedNotification: StateFlow<Notification?> = _selectedNotification.asStateFlow()

    // ==================== CONFIGURAÇÕES ====================

    private val _notificationSettings = MutableStateFlow<NotificationSettings?>(null)
    val notificationSettings: StateFlow<NotificationSettings?> = _notificationSettings.asStateFlow()

    // ==================== FUNÇÕES PÚBLICAS ====================

    /**
     * Define o usuário atual
     */
    fun setCurrentUser(userId: String, role: String = "") {
        _currentUserId.value = userId
        _currentUserRole.value = role
        loadNotificationSettings(userId)
    }

    /**
     * Seleciona uma notificação para visualização
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
     * Marca notificação como lida
     */
    fun markAsRead(notificationId: String) {
        viewModelScope.launch {
            notificationRepository.markAsRead(notificationId)
        }
    }

    /**
     * Marca todas como lidas
     */
    fun markAllAsRead() {
        viewModelScope.launch {
            _uiState.value = NotificationUiState.Loading
            val userId = _currentUserId.value
            val result = notificationRepository.markAllAsRead(userId)
            _uiState.value = if (result.isSuccess) {
                NotificationUiState.Success("Todas notificações marcadas como lidas")
            } else {
                NotificationUiState.Error(result.exceptionOrNull()?.message ?: "Erro ao marcar notificações")
            }
        }
    }

    /**
     * Deleta uma notificação
     */
    fun deleteNotification(notificationId: String) {
        viewModelScope.launch {
            _uiState.value = NotificationUiState.Loading
            val result = notificationRepository.deleteNotification(notificationId)
            _uiState.value = if (result.isSuccess) {
                _selectedNotification.value = null
                NotificationUiState.Success("Notificação deletada")
            } else {
                NotificationUiState.Error(result.exceptionOrNull()?.message ?: "Erro ao deletar notificação")
            }
        }
    }

    /**
     * Envia notificação para um usuário
     */
    fun sendToUser(
        userId: String,
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
                userId = userId,
                title = title,
                message = message,
                type = type,
                priority = priority,
                senderId = _currentUserId.value,
                senderName = "",
                relatedEntityId = relatedEntityId,
                relatedEntityType = relatedEntityType
            )
            _uiState.value = if (result.isSuccess) {
                NotificationUiState.Success("Notificação enviada com sucesso")
            } else {
                NotificationUiState.Error(result.exceptionOrNull()?.message ?: "Erro ao enviar notificação")
            }
        }
    }

    /**
     * Envia notificação para todos de um role
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
                senderId = _currentUserId.value,
                senderName = ""
            )
            _uiState.value = if (result.isSuccess) {
                NotificationUiState.Success("Notificação enviada para $role")
            } else {
                NotificationUiState.Error(result.exceptionOrNull()?.message ?: "Erro ao enviar notificação")
            }
        }
    }

    /**
     * Envia notificação para uma turma
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
                senderId = _currentUserId.value,
                senderName = ""
            )
            _uiState.value = if (result.isSuccess) {
                NotificationUiState.Success("Notificação enviada para a turma")
            } else {
                NotificationUiState.Error(result.exceptionOrNull()?.message ?: "Erro ao enviar notificação")
            }
        }
    }

    /**
     * Aplica filtro por tipo
     */
    fun filterByType(type: NotificationType?) {
        _selectedType.value = type
    }

    /**
     * Aplica filtro por prioridade
     */
    fun filterByPriority(priority: NotificationPriority?) {
        _selectedPriority.value = priority
    }

    /**
     * Mostra apenas não lidas
     */
    fun toggleShowOnlyUnread() {
        _showOnlyUnread.value = !_showOnlyUnread.value
    }

    /**
     * Limpa todos os filtros
     */
    fun clearFilters() {
        _selectedType.value = null
        _selectedPriority.value = null
        _showOnlyUnread.value = false
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
     * Salva configurações de notificação
     */
    fun saveNotificationSettings(settings: NotificationSettings) {
        viewModelScope.launch {
            _uiState.value = NotificationUiState.Loading
            val result = notificationRepository.saveNotificationSettings(settings)
            _uiState.value = if (result.isSuccess) {
                _notificationSettings.value = settings
                NotificationUiState.Success("Configurações salvas com sucesso")
            } else {
                NotificationUiState.Error(result.exceptionOrNull()?.message ?: "Erro ao salvar configurações")
            }
        }
    }

    /**
     * Deleta notificações expiradas
     */
    fun deleteExpiredNotifications() {
        viewModelScope.launch {
            notificationRepository.deleteExpiredNotifications()
        }
    }

    /**
     * Limpa o estado de UI
     */
    fun clearUiState() {
        _uiState.value = NotificationUiState.Idle
    }

    /**
     * Obtém estatísticas de notificações
     */
    fun getNotificationStats(): NotificationStats {
        val notifications = userNotifications.value
        return NotificationStats(
            total = notifications.size,
            unread = notifications.count { !it.isRead },
            byType = NotificationType.values().associateWith { type ->
                notifications.count { it.type == type }
            },
            byPriority = NotificationPriority.values().associateWith { priority ->
                notifications.count { it.priority == priority }
            },
            urgent = notifications.count { it.priority == NotificationPriority.URGENT && !it.isRead }
        )
    }
}

/**
 * Estados de UI do sistema de notificações
 */
sealed class NotificationUiState {
    object Idle : NotificationUiState()
    object Loading : NotificationUiState()
    data class Success(val message: String) : NotificationUiState()
    data class Error(val message: String) : NotificationUiState()
}

/**
 * Estatísticas de notificações
 */
data class NotificationStats(
    val total: Int,
    val unread: Int,
    val byType: Map<NotificationType, Int>,
    val byPriority: Map<NotificationPriority, Int>,
    val urgent: Int
)
