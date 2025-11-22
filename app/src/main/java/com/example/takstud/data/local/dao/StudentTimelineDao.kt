package com.example.takstud.data.local.dao

import androidx.room.*
import com.example.takstud.data.local.entity.StudentTimelineEventEntity
import kotlinx.coroutines.flow.Flow

/**
 * 📅 DAO para Timeline de eventos do aluno
 * Registra histórico cronológico de eventos importantes
 */
@Dao
interface StudentTimelineDao {

    // ==================== READ ====================

    /**
     * Obter evento por ID
     */
    @Query("SELECT * FROM student_timeline_events WHERE id = :id")
    suspend fun getById(id: String): StudentTimelineEventEntity?

    /**
     * Obter todos os eventos de um aluno (ordenados por data decrescente)
     */
    @Query("""
        SELECT * FROM student_timeline_events
        WHERE studentId = :studentId
        ORDER BY timestamp DESC
    """)
    fun getByStudent(studentId: String): Flow<List<StudentTimelineEventEntity>>

    /**
     * Obter eventos por aluno e tipo
     */
    @Query("""
        SELECT * FROM student_timeline_events
        WHERE studentId = :studentId AND type = :type
        ORDER BY timestamp DESC
    """)
    fun getByStudentAndType(
        studentId: String,
        type: String
    ): Flow<List<StudentTimelineEventEntity>>

    /**
     * Obter eventos recentes de um aluno
     */
    @Query("""
        SELECT * FROM student_timeline_events
        WHERE studentId = :studentId
        ORDER BY timestamp DESC
        LIMIT :limit
    """)
    fun getRecentByStudent(
        studentId: String,
        limit: Int
    ): Flow<List<StudentTimelineEventEntity>>

    /**
     * Obter eventos em um período
     */
    @Query("""
        SELECT * FROM student_timeline_events
        WHERE studentId = :studentId
            AND timestamp >= :startTimestamp
            AND timestamp <= :endTimestamp
        ORDER BY timestamp DESC
    """)
    fun getByStudentAndPeriod(
        studentId: String,
        startTimestamp: Long,
        endTimestamp: Long
    ): Flow<List<StudentTimelineEventEntity>>

    /**
     * Obter eventos por criador
     */
    @Query("""
        SELECT * FROM student_timeline_events
        WHERE studentId = :studentId AND createdBy = :createdBy
        ORDER BY timestamp DESC
    """)
    fun getByStudentAndCreator(
        studentId: String,
        createdBy: String
    ): Flow<List<StudentTimelineEventEntity>>

    /**
     * Buscar eventos por título ou descrição
     */
    @Query("""
        SELECT * FROM student_timeline_events
        WHERE studentId = :studentId
            AND (title LIKE '%' || :query || '%' OR description LIKE '%' || :query || '%')
        ORDER BY timestamp DESC
    """)
    fun searchByStudent(
        studentId: String,
        query: String
    ): Flow<List<StudentTimelineEventEntity>>

    // ==================== WRITE ====================

    /**
     * Inserir evento
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(event: StudentTimelineEventEntity)

    /**
     * Inserir múltiplos eventos
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(events: List<StudentTimelineEventEntity>)

    /**
     * Atualizar evento
     */
    @Update
    suspend fun update(event: StudentTimelineEventEntity)

    // ==================== DELETE ====================

    /**
     * Deletar evento por ID
     */
    @Query("DELETE FROM student_timeline_events WHERE id = :id")
    suspend fun deleteById(id: String)

    /**
     * Deletar todos os eventos de um aluno
     */
    @Query("DELETE FROM student_timeline_events WHERE studentId = :studentId")
    suspend fun deleteAllByStudent(studentId: String)

    /**
     * Deletar eventos por tipo
     */
    @Query("DELETE FROM student_timeline_events WHERE studentId = :studentId AND type = :type")
    suspend fun deleteByStudentAndType(studentId: String, type: String)

    /**
     * Deletar eventos antigos (mais de X dias)
     */
    @Query("DELETE FROM student_timeline_events WHERE timestamp < :cutoffTimestamp")
    suspend fun deleteOldEvents(cutoffTimestamp: Long)

    /**
     * Deletar todos os eventos
     */
    @Query("DELETE FROM student_timeline_events")
    suspend fun deleteAll()

    // ==================== ANALYTICS ====================

    /**
     * Contar eventos de um aluno
     */
    @Query("SELECT COUNT(*) FROM student_timeline_events WHERE studentId = :studentId")
    suspend fun countByStudent(studentId: String): Int

    /**
     * Contar eventos por tipo
     */
    @Query("""
        SELECT COUNT(*) FROM student_timeline_events
        WHERE studentId = :studentId AND type = :type
    """)
    suspend fun countByStudentAndType(studentId: String, type: String): Int

    /**
     * Obter evento mais recente de um aluno
     */
    @Query("""
        SELECT * FROM student_timeline_events
        WHERE studentId = :studentId
        ORDER BY timestamp DESC
        LIMIT 1
    """)
    suspend fun getLatestByStudent(studentId: String): StudentTimelineEventEntity?
}
