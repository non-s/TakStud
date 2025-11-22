package com.example.takstud.data.local.dao

import androidx.room.*
import com.example.takstud.data.local.entity.AssessmentEntity
import kotlinx.coroutines.flow.Flow

/**
 * 📊 DAO para Avaliações - COMPLETO
 */
@Dao
interface AssessmentDao {

    // ==================== CRUD BÁSICO ====================

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(assessment: AssessmentEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(assessments: List<AssessmentEntity>)

    @Update
    suspend fun update(assessment: AssessmentEntity)

    @Delete
    suspend fun delete(assessment: AssessmentEntity)

    @Query("DELETE FROM assessments WHERE id = :id")
    suspend fun deleteById(id: String)

    // ==================== QUERIES ====================

    @Query("SELECT * FROM assessments WHERE id = :id")
    suspend fun getById(id: String): AssessmentEntity?

    @Query("SELECT * FROM assessments WHERE id = :id")
    fun getByIdFlow(id: String): Flow<AssessmentEntity?>

    @Query("SELECT * FROM assessments WHERE isActive = 1 ORDER BY scheduledDate DESC")
    fun getAll(): Flow<List<AssessmentEntity>>

    @Query("SELECT * FROM assessments WHERE classId = :classId AND isActive = 1 ORDER BY scheduledDate DESC")
    fun getByClass(classId: String): Flow<List<AssessmentEntity>>

    @Query("SELECT * FROM assessments WHERE subjectId = :subjectId AND isActive = 1 ORDER BY scheduledDate DESC")
    fun getBySubject(subjectId: String): Flow<List<AssessmentEntity>>

    @Query("SELECT * FROM assessments WHERE teacherId = :teacherId AND isActive = 1 ORDER BY scheduledDate DESC")
    fun getByTeacher(teacherId: String): Flow<List<AssessmentEntity>>

    @Query("SELECT * FROM assessments WHERE type = :type AND isActive = 1 ORDER BY scheduledDate DESC")
    fun getByType(type: String): Flow<List<AssessmentEntity>>

    @Query("SELECT * FROM assessments WHERE status = :status AND isActive = 1 ORDER BY scheduledDate DESC")
    fun getByStatus(status: String): Flow<List<AssessmentEntity>>

    @Query("""
        SELECT * FROM assessments
        WHERE classId = :classId
        AND subjectId = :subjectId
        AND isActive = 1
        ORDER BY scheduledDate DESC
    """)
    fun getByClassAndSubject(classId: String, subjectId: String): Flow<List<AssessmentEntity>>

    // ==================== DATAS ====================

    @Query("""
        SELECT * FROM assessments
        WHERE scheduledDate BETWEEN :startDate AND :endDate
        AND isActive = 1
        ORDER BY scheduledDate ASC
    """)
    fun getByDateRange(startDate: Long, endDate: Long): Flow<List<AssessmentEntity>>

    @Query("""
        SELECT * FROM assessments
        WHERE dueDate <= :date
        AND status != 'COMPLETED'
        AND isActive = 1
        ORDER BY dueDate ASC
    """)
    fun getUpcoming(date: Long): Flow<List<AssessmentEntity>>

    // ==================== ESTATÍSTICAS ====================

    @Query("SELECT COUNT(*) FROM assessments WHERE classId = :classId AND isActive = 1")
    fun countByClass(classId: String): Flow<Int>

    @Query("SELECT AVG(averageScore) FROM assessments WHERE classId = :classId AND status = 'GRADED'")
    suspend fun getClassAverage(classId: String): Double?

    @Query("""
        SELECT * FROM assessments
        WHERE averageScore < :threshold
        AND status = 'GRADED'
        AND isActive = 1
        ORDER BY averageScore ASC
    """)
    fun getLowPerformance(threshold: Double): Flow<List<AssessmentEntity>>

    // ==================== SINCRONIZAÇÃO ====================

    @Query("SELECT * FROM assessments WHERE isSynced = 0")
    suspend fun getUnsynced(): List<AssessmentEntity>

    @Query("UPDATE assessments SET isSynced = 1 WHERE id IN (:ids)")
    suspend fun markAsSynced(ids: List<String>)

    // ==================== LIMPEZA ====================

    @Query("DELETE FROM assessments WHERE isActive = 0 AND updatedAt < :cutoffDate")
    suspend fun deleteOldInactive(cutoffDate: Long)

    @Query("DELETE FROM assessments")
    suspend fun deleteAll()
}
