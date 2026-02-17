package com.example.takstud.data.local.dao

import androidx.room.*
import com.example.takstud.data.local.entity.NotificationEntity
import kotlinx.coroutines.flow.Flow

/**
 * 🔔 DAO COMPLETO para Notificações
 * Operações completas com queries avançadas para sistema de notificações
 */
@Dao
interface NotificationDao {

    // ==================== BASIC CRUD ====================

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(notification: NotificationEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(notifications: List<NotificationEntity>)

    @Update
    suspend fun update(notification: NotificationEntity)

    @Delete
    suspend fun delete(notification: NotificationEntity)

    @Query("DELETE FROM notifications WHERE id = :notificationId")
    suspend fun deleteById(notificationId: String)

    @Query("DELETE FROM notifications")
    suspend fun deleteAll()

    // ==================== QUERIES - SINGLE ====================

    @Query("SELECT * FROM notifications WHERE id = :notificationId AND isActive = 1")
    suspend fun getById(notificationId: String): NotificationEntity?

    @Query("SELECT * FROM notifications WHERE id = :notificationId AND isActive = 1")
    fun getByIdFlow(notificationId: String): Flow<NotificationEntity?>

    // ==================== QUERIES - LISTS ====================

    @Query("SELECT * FROM notifications WHERE isActive = 1 ORDER BY createdAt DESC")
    fun getAllActive(): Flow<List<NotificationEntity>>

    @Query("SELECT * FROM notifications ORDER BY createdAt DESC")
    fun getAll(): Flow<List<NotificationEntity>>

    @Query("""
        SELECT * FROM notifications
        WHERE (targetUserId = :userId OR targetUserId = '')
        AND isActive = 1
        ORDER BY createdAt DESC
    """)
    fun getByUser(userId: String): Flow<List<NotificationEntity>>

    @Query("""
        SELECT * FROM notifications
        WHERE (targetUserId = :userId OR targetUserId = '')
        AND isRead = 0
        AND isActive = 1
        ORDER BY createdAt DESC
    """)
    fun getUnreadByUser(userId: String): Flow<List<NotificationEntity>>

    @Query("""
        SELECT * FROM notifications
        WHERE (targetUserId = :userId OR targetUserId = '')
        AND isRead = 1
        AND isActive = 1
        ORDER BY createdAt DESC
    """)
    fun getReadByUser(userId: String): Flow<List<NotificationEntity>>

    // ==================== QUERIES - FILTROS POR TIPO ====================

    @Query("""
        SELECT * FROM notifications
        WHERE (targetUserId = :userId OR targetUserId = '')
        AND type = :type
        AND isActive = 1
        ORDER BY createdAt DESC
    """)
    fun getByType(userId: String, type: String): Flow<List<NotificationEntity>>

    @Query("""
        SELECT * FROM notifications
        WHERE (targetUserId = :userId OR targetUserId = '')
        AND priority = :priority
        AND isActive = 1
        ORDER BY createdAt DESC
    """)
    fun getByPriority(userId: String, priority: String): Flow<List<NotificationEntity>>

    // ==================== QUERIES - FILTROS POR TARGET ====================

    @Query("""
        SELECT * FROM notifications
        WHERE targetRole = :role
        AND isActive = 1
        ORDER BY createdAt DESC
    """)
    fun getByRole(role: String): Flow<List<NotificationEntity>>

    @Query("""
        SELECT * FROM notifications
        WHERE targetClass = :classId
        AND isActive = 1
        ORDER BY createdAt DESC
    """)
    fun getByClass(classId: String): Flow<List<NotificationEntity>>

    // ==================== QUERIES - RELACIONADAS ====================

    @Query("""
        SELECT * FROM notifications
        WHERE relatedEntityId = :entityId
        AND relatedEntityType = :entityType
        AND isActive = 1
        ORDER BY createdAt DESC
    """)
    fun getByRelatedEntity(entityId: String, entityType: String): Flow<List<NotificationEntity>>

    // ==================== QUERIES - ESTATÍSTICAS ====================

    @Query("""
        SELECT COUNT(*) FROM notifications
        WHERE (targetUserId = :userId OR targetUserId = '')
        AND isRead = 0
        AND isActive = 1
    """)
    fun getUnreadCount(userId: String): Flow<Int>

    @Query("""
        SELECT COUNT(*) FROM notifications
        WHERE (targetUserId = :userId OR targetUserId = '')
        AND isActive = 1
    """)
    fun getTotalCount(userId: String): Flow<Int>

    @Query("""
        SELECT COUNT(*) FROM notifications
        WHERE (targetUserId = :userId OR targetUserId = '')
        AND type = :type
        AND isActive = 1
    """)
    fun getCountByType(userId: String, type: String): Flow<Int>

    @Query("""
        SELECT COUNT(*) FROM notifications
        WHERE (targetUserId = :userId OR targetUserId = '')
        AND priority = :priority
        AND isRead = 0
        AND isActive = 1
    """)
    fun getUnreadCountByPriority(userId: String, priority: String): Flow<Int>

    // ==================== MARK AS READ OPERATIONS ====================

    @Query("UPDATE notifications SET isRead = 1 WHERE id = :notificationId")
    suspend fun markAsRead(notificationId: String)

    @Query("""
        UPDATE notifications
        SET isRead = 1
        WHERE (targetUserId = :userId OR targetUserId = '')
        AND isRead = 0
    """)
    suspend fun markAllAsReadForUser(userId: String)

    @Query("UPDATE notifications SET isRead = 1 WHERE id IN (:notificationIds)")
    suspend fun markMultipleAsRead(notificationIds: List<String>)

    @Query("""
        UPDATE notifications
        SET isRead = 1
        WHERE (targetUserId = :userId OR targetUserId = '')
        AND type = :type
        AND isRead = 0
    """)
    suspend fun markAllAsReadByType(userId: String, type: String)

    // ==================== SYNC OPERATIONS ====================

    @Query("SELECT * FROM notifications WHERE isSynced = 0 AND isActive = 1")
    suspend fun getUnsyncedNotifications(): List<NotificationEntity>

    @Query("UPDATE notifications SET isSynced = 1, lastSyncAt = :syncTime WHERE id = :notificationId")
    suspend fun markAsSynced(notificationId: String, syncTime: Long = System.currentTimeMillis())

    @Query("UPDATE notifications SET isSynced = 0 WHERE id = :notificationId")
    suspend fun markAsUnsynced(notificationId: String)

    @Query("UPDATE notifications SET isSynced = 1, lastSyncAt = :syncTime WHERE id IN (:notificationIds)")
    suspend fun markMultipleAsSynced(notificationIds: List<String>, syncTime: Long = System.currentTimeMillis())

    // ==================== SOFT DELETE ====================

    @Query("UPDATE notifications SET isActive = 0 WHERE id = :notificationId")
    suspend fun softDelete(notificationId: String)

    @Query("UPDATE notifications SET isActive = 1 WHERE id = :notificationId")
    suspend fun restore(notificationId: String)

    @Query("""
        UPDATE notifications
        SET isActive = 0
        WHERE (targetUserId = :userId OR targetUserId = '')
        AND isRead = 1
    """)
    suspend fun deleteAllReadForUser(userId: String)

    // ==================== EXPIRATION ====================

    @Query("""
        SELECT * FROM notifications
        WHERE expiresAt > 0
        AND expiresAt < :currentTime
        AND isActive = 1
    """)
    suspend fun getExpiredNotifications(currentTime: Long = System.currentTimeMillis()): List<NotificationEntity>

    @Query("""
        UPDATE notifications
        SET isActive = 0
        WHERE expiresAt > 0
        AND expiresAt < :currentTime
    """)
    suspend fun deleteExpiredNotifications(currentTime: Long = System.currentTimeMillis())

    // ==================== SEARCH ====================

    @Query("""
        SELECT * FROM notifications
        WHERE (targetUserId = :userId OR targetUserId = '')
        AND (title LIKE '%' || :query || '%'
        OR message LIKE '%' || :query || '%'
        OR senderName LIKE '%' || :query || '%')
        AND isActive = 1
        ORDER BY createdAt DESC
    """)
    fun searchNotifications(userId: String, query: String): Flow<List<NotificationEntity>>

    // ==================== CLEANUP ====================

    @Query("""
        DELETE FROM notifications
        WHERE createdAt < :cutoffTime
        AND isActive = 0
    """)
    suspend fun deleteOldInactiveNotifications(cutoffTime: Long)

    @Query("""
        DELETE FROM notifications
        WHERE isRead = 1
        AND createdAt < :cutoffTime
    """)
    suspend fun deleteOldReadNotifications(cutoffTime: Long)

    // ==================== RECENT NOTIFICATIONS ====================

    @Query("""
        SELECT * FROM notifications
        WHERE (targetUserId = :userId OR targetUserId = '')
        AND isActive = 1
        ORDER BY createdAt DESC
        LIMIT :limit
    """)
    fun getRecentNotifications(userId: String, limit: Int = 20): Flow<List<NotificationEntity>>

    @Query("""
        SELECT * FROM notifications
        WHERE (targetUserId = :userId OR targetUserId = '')
        AND createdAt >= :since
        AND isActive = 1
        ORDER BY createdAt DESC
    """)
    fun getNotificationsSince(userId: String, since: Long): Flow<List<NotificationEntity>>
}
