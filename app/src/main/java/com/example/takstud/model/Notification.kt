package com.example.takstud.model

/**
 * Modelo de Notificação expandido para sistema de comunicação.
 *
 * Representa notificações enviadas para usuários sobre eventos,
 * tarefas, avisos, mensagens e outras informações importantes.
 */
data class Notification(
    val id: String = "",
    val title: String = "",
    val message: String = "",
    val type: NotificationType = NotificationType.GENERAL,
    val priority: NotificationPriority = NotificationPriority.NORMAL,
    val targetUserId: String = "", // destinatário específico
    val targetRole: String = "", // ou enviar para todos de um role
    val targetClass: String = "", // ou para toda uma turma
    val senderId: String = "", // quem enviou
    val senderName: String = "",
    val relatedEntityId: String = "", // ID de tarefa, evento, etc.
    val relatedEntityType: String = "", // "task", "event", "grade", etc.
    val isRead: Boolean = false,
    val actionUrl: String = "", // deeplink para ação relacionada
    val imageUrl: String = "",
    val createdAt: Long = System.currentTimeMillis(),
    val expiresAt: Long = 0, // 0 = não expira
    val isSynced: Boolean = false
)

/**
 * Tipos de notificação
 */
enum class NotificationType(val displayName: String, val icon: String) {
    GENERAL("Geral", "📢"),
    TASK("Tarefa", "📝"),
    GRADE("Nota", "📊"),
    ATTENDANCE("Presença", "✓"),
    EVENT("Evento", "📅"),
    MESSAGE("Mensagem", "💬"),
    REMINDER("Lembrete", "⏰"),
    ANNOUNCEMENT("Comunicado", "📣"),
    EMERGENCY("Emergência", "🚨"),
    ACHIEVEMENT("Conquista", "🏆")
}

/**
 * Prioridade da notificação
 */
enum class NotificationPriority(val displayName: String, val color: String) {
    LOW("Baixa", "#4CAF50"),
    NORMAL("Normal", "#2196F3"),
    HIGH("Alta", "#FF9800"),
    URGENT("Urgente", "#F44336")
}

/**
 * Configurações de notificação do usuário
 */
data class NotificationSettings(
    val userId: String = "",
    val enablePushNotifications: Boolean = true,
    val enableEmailNotifications: Boolean = false,
    val enableTaskReminders: Boolean = true,
    val enableGradeNotifications: Boolean = true,
    val enableAttendanceNotifications: Boolean = true,
    val enableEventReminders: Boolean = true,
    val enableGeneralAnnouncements: Boolean = true,
    val quietHoursStart: String = "22:00",
    val quietHoursEnd: String = "07:00",
    val enableQuietHours: Boolean = false
)
