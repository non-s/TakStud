package com.example.takstud.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.takstud.model.Notification
import com.example.takstud.model.NotificationPriority
import com.example.takstud.model.NotificationType

/**
 * NotificationEntity - Entidade Room para cache local de notificações
 */
@Entity(tableName = "notifications")
data class NotificationEntity(
    @PrimaryKey
    val id: String,
    val title: String,
    val message: String,
    val type: String, // NotificationType.name
    val priority: String, // NotificationPriority.name
    val targetUserId: String,
    val targetRole: String,
    val targetClass: String,
    val senderId: String,
    val senderName: String,
    val relatedEntityId: String,
    val relatedEntityType: String,
    val isRead: Boolean,
    val actionUrl: String,
    val imageUrl: String,
    val createdAt: Long,
    val expiresAt: Long,
    val isSynced: Boolean,
    val isActive: Boolean = true,
    val lastSyncAt: Long = 0L
)

/**
 * Conversão de NotificationEntity para Notification (domain model)
 */
fun NotificationEntity.toDomainModel(): Notification {
    return Notification(
        id = this.id,
        title = this.title,
        message = this.message,
        type = try { NotificationType.valueOf(this.type) } catch (e: Exception) { NotificationType.GENERAL },
        priority = try { NotificationPriority.valueOf(this.priority) } catch (e: Exception) { NotificationPriority.NORMAL },
        targetUserId = this.targetUserId,
        targetRole = this.targetRole,
        targetClass = this.targetClass,
        senderId = this.senderId,
        senderName = this.senderName,
        relatedEntityId = this.relatedEntityId,
        relatedEntityType = this.relatedEntityType,
        isRead = this.isRead,
        actionUrl = this.actionUrl,
        imageUrl = this.imageUrl,
        createdAt = this.createdAt,
        expiresAt = this.expiresAt,
        isSynced = this.isSynced
    )
}

/**
 * Conversão de Notification para NotificationEntity
 */
fun Notification.toEntity(): NotificationEntity {
    return NotificationEntity(
        id = this.id,
        title = this.title,
        message = this.message,
        type = this.type.name,
        priority = this.priority.name,
        targetUserId = this.targetUserId,
        targetRole = this.targetRole,
        targetClass = this.targetClass,
        senderId = this.senderId,
        senderName = this.senderName,
        relatedEntityId = this.relatedEntityId,
        relatedEntityType = this.relatedEntityType,
        isRead = this.isRead,
        actionUrl = this.actionUrl,
        imageUrl = this.imageUrl,
        createdAt = this.createdAt,
        expiresAt = this.expiresAt,
        isSynced = this.isSynced,
        isActive = true,
        lastSyncAt = System.currentTimeMillis()
    )
}
