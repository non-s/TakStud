package com.example.takstud.data.repository

import com.example.takstud.model.Notification
import com.example.takstud.model.NotificationPriority
import com.example.takstud.model.NotificationSettings
import com.example.takstud.model.NotificationType
import com.example.takstud.util.firestoreCollectionFlow
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class NotificationRepository @Inject constructor(
    private val db: FirebaseFirestore
) {

    /**
     * Obtém todas as notificações
     */
    fun getNotifications(): Flow<List<Notification>> = firestoreCollectionFlow(
        db.collection("notifications"),
        Notification::class.java,
        "TakStud"
    )

    /**
     * Obtém notificações de um usuário específico
     */
    fun getNotificationsByUser(userId: String): Flow<List<Notification>> =
        getNotifications().map { notifications ->
            notifications.filter {
                it.targetUserId == userId || it.targetUserId.isEmpty()
            }.sortedByDescending { it.createdAt }
        }

    /**
     * Obtém notificações não lidas de um usuário
     */
    fun getUnreadNotifications(userId: String): Flow<List<Notification>> =
        getNotificationsByUser(userId).map { notifications ->
            notifications.filter { !it.isRead }
        }

    /**
     * Obtém contagem de notificações não lidas
     */
    fun getUnreadCount(userId: String): Flow<Int> =
        getUnreadNotifications(userId).map { it.size }

    /**
     * Obtém notificações por tipo
     */
    fun getNotificationsByType(
        userId: String,
        type: NotificationType
    ): Flow<List<Notification>> =
        getNotificationsByUser(userId).map { notifications ->
            notifications.filter { it.type == type }
        }

    /**
     * Obtém notificações por prioridade
     */
    fun getNotificationsByPriority(
        userId: String,
        priority: NotificationPriority
    ): Flow<List<Notification>> =
        getNotificationsByUser(userId).map { notifications ->
            notifications.filter { it.priority == priority }
        }

    /**
     * Obtém notificações de uma turma
     */
    fun getNotificationsByClass(classId: String): Flow<List<Notification>> =
        getNotifications().map { notifications ->
            notifications.filter { it.targetClass == classId }
                .sortedByDescending { it.createdAt }
        }

    /**
     * Obtém notificações de um role específico
     */
    fun getNotificationsByRole(role: String): Flow<List<Notification>> =
        getNotifications().map { notifications ->
            notifications.filter { it.targetRole == role }
                .sortedByDescending { it.createdAt }
        }

    /**
     * Salva ou atualiza uma notificação
     */
    suspend fun saveNotification(notification: Notification): Result<Notification> {
        return try {
            val notificationRef = if (notification.id.isBlank()) {
                db.collection("notifications").document()
            } else {
                db.collection("notifications").document(notification.id)
            }

            val updatedNotification = notification.copy(
                id = notificationRef.id,
                isSynced = true
            )

            notificationRef.set(updatedNotification).await()
            Result.success(updatedNotification)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Envia notificação para usuário específico
     */
    suspend fun sendToUser(
        userId: String,
        title: String,
        message: String,
        type: NotificationType = NotificationType.GENERAL,
        priority: NotificationPriority = NotificationPriority.NORMAL,
        senderId: String = "",
        senderName: String = "",
        relatedEntityId: String = "",
        relatedEntityType: String = ""
    ): Result<Notification> {
        val notification = Notification(
            title = title,
            message = message,
            type = type,
            priority = priority,
            targetUserId = userId,
            senderId = senderId,
            senderName = senderName,
            relatedEntityId = relatedEntityId,
            relatedEntityType = relatedEntityType,
            createdAt = System.currentTimeMillis()
        )
        return saveNotification(notification)
    }

    /**
     * Envia notificação para todos de um role
     */
    suspend fun sendToRole(
        role: String,
        title: String,
        message: String,
        type: NotificationType = NotificationType.GENERAL,
        priority: NotificationPriority = NotificationPriority.NORMAL,
        senderId: String = "",
        senderName: String = ""
    ): Result<Notification> {
        val notification = Notification(
            title = title,
            message = message,
            type = type,
            priority = priority,
            targetRole = role,
            senderId = senderId,
            senderName = senderName,
            createdAt = System.currentTimeMillis()
        )
        return saveNotification(notification)
    }

    /**
     * Envia notificação para uma turma
     */
    suspend fun sendToClass(
        classId: String,
        title: String,
        message: String,
        type: NotificationType = NotificationType.GENERAL,
        priority: NotificationPriority = NotificationPriority.NORMAL,
        senderId: String = "",
        senderName: String = ""
    ): Result<Notification> {
        val notification = Notification(
            title = title,
            message = message,
            type = type,
            priority = priority,
            targetClass = classId,
            senderId = senderId,
            senderName = senderName,
            createdAt = System.currentTimeMillis()
        )
        return saveNotification(notification)
    }

    /**
     * Marca notificação como lida
     */
    suspend fun markAsRead(notificationId: String): Result<Unit> {
        return try {
            db.collection("notifications")
                .document(notificationId)
                .update("isRead", true)
                .await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Marca todas notificações do usuário como lidas
     */
    suspend fun markAllAsRead(userId: String): Result<Unit> {
        return try {
            val snapshot = db.collection("notifications")
                .whereEqualTo("targetUserId", userId)
                .whereEqualTo("isRead", false)
                .get()
                .await()

            val batch = db.batch()
            snapshot.documents.forEach { doc ->
                batch.update(doc.reference, "isRead", true)
            }
            batch.commit().await()

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Deleta uma notificação
     */
    suspend fun deleteNotification(notificationId: String): Result<Unit> {
        return try {
            db.collection("notifications").document(notificationId).delete().await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Deleta notificações antigas/expiradas
     */
    suspend fun deleteExpiredNotifications(): Result<Int> {
        return try {
            val now = System.currentTimeMillis()
            val snapshot = db.collection("notifications")
                .whereLessThan("expiresAt", now)
                .whereGreaterThan("expiresAt", 0)
                .get()
                .await()

            val batch = db.batch()
            snapshot.documents.forEach { doc ->
                batch.delete(doc.reference)
            }
            batch.commit().await()

            Result.success(snapshot.size())
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Obtém configurações de notificação do usuário
     */
    suspend fun getNotificationSettings(userId: String): NotificationSettings? {
        return try {
            db.collection("notificationSettings")
                .document(userId)
                .get()
                .await()
                .toObject(NotificationSettings::class.java)
        } catch (e: Exception) {
            null
        }
    }

    /**
     * Salva configurações de notificação do usuário
     */
    suspend fun saveNotificationSettings(settings: NotificationSettings): Result<Unit> {
        return try {
            db.collection("notificationSettings")
                .document(settings.userId)
                .set(settings)
                .await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
