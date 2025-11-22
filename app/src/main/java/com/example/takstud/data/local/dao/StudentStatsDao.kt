package com.example.takstud.data.local.dao

import androidx.room.*
import com.example.takstud.data.local.entity.StudentStatsEntity
import kotlinx.coroutines.flow.Flow

/**
 * 📊 DAO para estatísticas dos alunos
 * Mantém estatísticas agregadas para consultas rápidas
 */
@Dao
interface StudentStatsDao {

    // ==================== READ ====================

    /**
     * Obter estatísticas de um aluno
     */
    @Query("SELECT * FROM student_stats WHERE studentId = :studentId")
    suspend fun getByStudent(studentId: String): StudentStatsEntity?

    /**
     * Obter estatísticas (Flow)
     */
    @Query("SELECT * FROM student_stats WHERE studentId = :studentId")
    fun getByStudentFlow(studentId: String): Flow<StudentStatsEntity?>

    /**
     * Obter top performers por média de notas
     */
    @Query("""
        SELECT * FROM student_stats
        ORDER BY averageGrade DESC
        LIMIT :limit
    """)
    fun getTopPerformers(limit: Int): Flow<List<StudentStatsEntity>>

    /**
     * Obter alunos com baixo desempenho
     */
    @Query("""
        SELECT * FROM student_stats
        WHERE averageGrade < :threshold
        ORDER BY averageGrade ASC
    """)
    fun getLowPerformers(threshold: Double): Flow<List<StudentStatsEntity>>

    /**
     * Obter alunos com baixa frequência
     */
    @Query("""
        SELECT * FROM student_stats
        WHERE attendanceRate < :threshold
        ORDER BY attendanceRate ASC
    """)
    fun getLowAttendance(threshold: Double): Flow<List<StudentStatsEntity>>

    /**
     * Obter média geral de notas
     */
    @Query("SELECT AVG(averageGrade) FROM student_stats")
    suspend fun getAverageGrade(): Double?

    /**
     * Obter média geral de frequência
     */
    @Query("SELECT AVG(attendanceRate) FROM student_stats")
    suspend fun getAverageAttendance(): Double?

    // ==================== WRITE ====================

    /**
     * Inserir ou atualizar estatísticas
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(stats: StudentStatsEntity)

    /**
     * Inserir múltiplas estatísticas
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(stats: List<StudentStatsEntity>)

    /**
     * Atualizar estatísticas
     */
    @Update
    suspend fun update(stats: StudentStatsEntity)

    /**
     * Atualizar apenas frequência
     */
    @Query("""
        UPDATE student_stats
        SET totalClasses = :total,
            attendedClasses = :attended,
            absentClasses = :absent,
            attendanceRate = :rate,
            lastUpdated = :timestamp
        WHERE studentId = :studentId
    """)
    suspend fun updateAttendance(
        studentId: String,
        rate: Double,
        attended: Int,
        absent: Int,
        total: Int,
        timestamp: Long = System.currentTimeMillis()
    )

    /**
     * Atualizar apenas notas
     */
    @Query("""
        UPDATE student_stats
        SET averageGrade = :average,
            highestGrade = :highest,
            lowestGrade = :lowest,
            lastUpdated = :timestamp
        WHERE studentId = :studentId
    """)
    suspend fun updateGrades(
        studentId: String,
        average: Double,
        highest: Double,
        lowest: Double,
        timestamp: Long = System.currentTimeMillis()
    )

    /**
     * Atualizar apenas tarefas
     */
    @Query("""
        UPDATE student_stats
        SET totalTasks = :total,
            completedTasks = :completed,
            pendingTasks = :pending,
            taskCompletionRate = :rate,
            lastUpdated = :timestamp
        WHERE studentId = :studentId
    """)
    suspend fun updateTasks(
        studentId: String,
        total: Int,
        completed: Int,
        pending: Int,
        rate: Double,
        timestamp: Long = System.currentTimeMillis()
    )

    // ==================== DELETE ====================

    /**
     * Deletar estatísticas de um aluno
     */
    @Query("DELETE FROM student_stats WHERE studentId = :studentId")
    suspend fun deleteByStudent(studentId: String)

    /**
     * Deletar todas as estatísticas
     */
    @Query("DELETE FROM student_stats")
    suspend fun deleteAll()

    // ==================== ANALYTICS ====================

    /**
     * Contar alunos com estatísticas
     */
    @Query("SELECT COUNT(*) FROM student_stats")
    suspend fun count(): Int

    /**
     * Obter estatísticas desatualizadas (não atualizadas há mais de X dias)
     */
    @Query("""
        SELECT * FROM student_stats
        WHERE lastUpdated < :cutoffTimestamp
    """)
    fun getOutdatedStats(cutoffTimestamp: Long): Flow<List<StudentStatsEntity>>
}
