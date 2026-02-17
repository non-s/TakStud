package com.example.takstud.data.local.dao

import androidx.room.*
import com.example.takstud.data.local.entity.EventEntity
import kotlinx.coroutines.flow.Flow

/**
 * 📅 DAO COMPLETO para Eventos da Agenda
 * Operações completas com queries avançadas para agenda digital
 */
@Dao
interface EventDao {

    // ==================== BASIC CRUD ====================

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(event: EventEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(events: List<EventEntity>)

    @Update
    suspend fun update(event: EventEntity)

    @Delete
    suspend fun delete(event: EventEntity)

    @Query("DELETE FROM events WHERE id = :eventId")
    suspend fun deleteById(eventId: String)

    @Query("DELETE FROM events")
    suspend fun deleteAll()

    // ==================== QUERIES - SINGLE ====================

    @Query("SELECT * FROM events WHERE id = :eventId AND isActive = 1")
    suspend fun getById(eventId: String): EventEntity?

    @Query("SELECT * FROM events WHERE id = :eventId AND isActive = 1")
    fun getByIdFlow(eventId: String): Flow<EventEntity?>

    // ==================== QUERIES - LISTS ====================

    @Query("SELECT * FROM events WHERE isActive = 1 ORDER BY date ASC, startTime ASC")
    fun getAllActive(): Flow<List<EventEntity>>

    @Query("SELECT * FROM events ORDER BY date ASC, startTime ASC")
    fun getAll(): Flow<List<EventEntity>>

    @Query("SELECT * FROM events WHERE date = :date AND isActive = 1 ORDER BY startTime ASC")
    fun getByDate(date: String): Flow<List<EventEntity>>

    @Query("""
        SELECT * FROM events
        WHERE (studentClass = :classId OR studentClass = '')
        AND isActive = 1
        ORDER BY date ASC, startTime ASC
    """)
    fun getByClass(classId: String): Flow<List<EventEntity>>

    @Query("""
        SELECT * FROM events
        WHERE eventType = :eventType
        AND isActive = 1
        ORDER BY date ASC, startTime ASC
    """)
    fun getByEventType(eventType: String): Flow<List<EventEntity>>

    @Query("""
        SELECT * FROM events
        WHERE (createdBy = :userId OR participants LIKE '%' || :userId || '%')
        AND isActive = 1
        ORDER BY date DESC
    """)
    fun getByUser(userId: String): Flow<List<EventEntity>>

    // ==================== QUERIES - FILTROS AVANÇADOS ====================

    @Query("""
        SELECT * FROM events
        WHERE date >= :startDate
        AND date <= :endDate
        AND isActive = 1
        ORDER BY date ASC, startTime ASC
    """)
    fun getByDateRange(startDate: String, endDate: String): Flow<List<EventEntity>>

    @Query("""
        SELECT * FROM events
        WHERE date >= :currentDate
        AND isActive = 1
        ORDER BY date ASC, startTime ASC
        LIMIT :limit
    """)
    fun getUpcoming(currentDate: String, limit: Int = 10): Flow<List<EventEntity>>

    @Query("""
        SELECT * FROM events
        WHERE date < :currentDate
        AND isActive = 1
        ORDER BY date DESC, startTime DESC
        LIMIT :limit
    """)
    fun getPast(currentDate: String, limit: Int = 10): Flow<List<EventEntity>>

    @Query("""
        SELECT * FROM events
        WHERE isAllDay = 1
        AND isActive = 1
        ORDER BY date ASC
    """)
    fun getAllDayEvents(): Flow<List<EventEntity>>

    @Query("""
        SELECT * FROM events
        WHERE reminder != 'NONE'
        AND date >= :currentDate
        AND isActive = 1
        ORDER BY date ASC, startTime ASC
    """)
    fun getEventsWithReminders(currentDate: String): Flow<List<EventEntity>>

    // ==================== QUERIES - ESTATÍSTICAS ====================

    @Query("SELECT COUNT(*) FROM events WHERE isActive = 1")
    fun getActiveCount(): Flow<Int>

    @Query("SELECT COUNT(*) FROM events WHERE date = :date AND isActive = 1")
    suspend fun getCountByDate(date: String): Int

    @Query("""
        SELECT COUNT(*) FROM events
        WHERE eventType = :eventType
        AND isActive = 1
    """)
    fun getCountByType(eventType: String): Flow<Int>

    @Query("""
        SELECT COUNT(*) FROM events
        WHERE date >= :startDate
        AND date <= :endDate
        AND isActive = 1
    """)
    suspend fun getCountInDateRange(startDate: String, endDate: String): Int

    // ==================== SYNC OPERATIONS ====================

    @Query("SELECT * FROM events WHERE isSynced = 0 AND isActive = 1")
    suspend fun getUnsyncedEvents(): List<EventEntity>

    @Query("UPDATE events SET isSynced = 1, lastSyncAt = :syncTime WHERE id = :eventId")
    suspend fun markAsSynced(eventId: String, syncTime: Long = System.currentTimeMillis())

    @Query("UPDATE events SET isSynced = 0 WHERE id = :eventId")
    suspend fun markAsUnsynced(eventId: String)

    @Query("UPDATE events SET isSynced = 1, lastSyncAt = :syncTime WHERE id IN (:eventIds)")
    suspend fun markMultipleAsSynced(eventIds: List<String>, syncTime: Long = System.currentTimeMillis())

    // ==================== SOFT DELETE ====================

    @Query("UPDATE events SET isActive = 0 WHERE id = :eventId")
    suspend fun softDelete(eventId: String)

    @Query("UPDATE events SET isActive = 1 WHERE id = :eventId")
    suspend fun restore(eventId: String)

    // ==================== PARTICIPANTS MANAGEMENT ====================

    @Query("""
        SELECT * FROM events
        WHERE participants LIKE '%' || :userId || '%'
        AND isActive = 1
        ORDER BY date ASC
    """)
    fun getByParticipant(userId: String): Flow<List<EventEntity>>

    // ==================== SEARCH ====================

    @Query("""
        SELECT * FROM events
        WHERE (title LIKE '%' || :query || '%'
        OR description LIKE '%' || :query || '%'
        OR location LIKE '%' || :query || '%')
        AND isActive = 1
        ORDER BY date DESC
    """)
    fun searchEvents(query: String): Flow<List<EventEntity>>

    // ==================== CLEANUP ====================

    @Query("""
        DELETE FROM events
        WHERE date < :cutoffDate
        AND isActive = 0
    """)
    suspend fun deleteOldInactiveEvents(cutoffDate: String)
}
