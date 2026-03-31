package com.example.takstud.data.local.dao

import androidx.room.*
import com.example.takstud.data.local.entity.GradeEntity
import kotlinx.coroutines.flow.Flow

/**
 * 📝 DAO para Notas dos Alunos - COMPLETO
 */
@Dao
interface StudentGradeDao {

    // ==================== CRUD BÁSICO ====================

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(grade: GradeEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(grades: List<GradeEntity>)

    @Update
    suspend fun update(grade: GradeEntity)

    @Delete
    suspend fun delete(grade: GradeEntity)

    @Query("DELETE FROM student_grades WHERE id = :id")
    suspend fun deleteById(id: String)

    // ==================== QUERIES ====================

    @Query("SELECT * FROM student_grades WHERE id = :id")
    suspend fun getById(id: String): GradeEntity?

    @Query("SELECT * FROM student_grades WHERE id = :id")
    fun getByIdFlow(id: String): Flow<GradeEntity?>

    @Query("SELECT * FROM student_grades WHERE assessmentId = :assessmentId ORDER BY studentName ASC")
    fun getByAssessment(assessmentId: String): Flow<List<GradeEntity>>

    @Query("SELECT * FROM student_grades WHERE studentId = :studentId ORDER BY gradedAt DESC")
    fun getByStudent(studentId: String): Flow<List<GradeEntity>>

    @Query("""
        SELECT * FROM student_grades
        WHERE studentId = :studentId
        AND assessmentId = :assessmentId
    """)
    suspend fun getByStudentAndAssessment(studentId: String, assessmentId: String): GradeEntity?

    // ==================== PUBLICADAS ====================

    @Query("""
        SELECT * FROM student_grades
        WHERE studentId = :studentId
        AND isPublished = 1
        ORDER BY publishedAt DESC
    """)
    fun getPublishedByStudent(studentId: String): Flow<List<GradeEntity>>

    @Query("""
        SELECT * FROM student_grades
        WHERE assessmentId = :assessmentId
        AND isPublished = 1
    """)
    fun getPublishedByAssessment(assessmentId: String): Flow<List<GradeEntity>>

    // ==================== ESTATÍSTICAS ====================

    @Query("SELECT COUNT(*) FROM student_grades WHERE assessmentId = :assessmentId")
    fun countByAssessment(assessmentId: String): Flow<Int>

    @Query("SELECT COUNT(*) FROM student_grades WHERE assessmentId = :assessmentId AND gradedAt IS NOT NULL")
    fun countGradedByAssessment(assessmentId: String): Flow<Int>

    @Query("SELECT AVG(score) FROM student_grades WHERE assessmentId = :assessmentId AND score IS NOT NULL")
    suspend fun getAverageByAssessment(assessmentId: String): Double?

    @Query("SELECT MAX(score) FROM student_grades WHERE assessmentId = :assessmentId")
    suspend fun getHighestScore(assessmentId: String): Double?

    @Query("SELECT MIN(score) FROM student_grades WHERE assessmentId = :assessmentId")
    suspend fun getLowestScore(assessmentId: String): Double?

    @Query("""
        SELECT AVG(score) FROM student_grades
        WHERE studentId = :studentId
        AND score IS NOT NULL
        AND isPublished = 1
    """)
    suspend fun getStudentAverage(studentId: String): Double?

    // ==================== FILTROS ====================

    @Query("""
        SELECT * FROM student_grades
        WHERE assessmentId = :assessmentId
        AND isAbsent = 1
    """)
    fun getAbsentStudents(assessmentId: String): Flow<List<GradeEntity>>

    @Query("""
        SELECT * FROM student_grades
        WHERE assessmentId = :assessmentId
        AND score < :passingScore
    """)
    fun getFailingGrades(assessmentId: String, passingScore: Double): Flow<List<GradeEntity>>

    @Query("""
        SELECT * FROM student_grades
        WHERE assessmentId = :assessmentId
        AND gradedAt IS NULL
    """)
    fun getPendingGrades(assessmentId: String): Flow<List<GradeEntity>>

    // ==================== SINCRONIZAÇÃO ====================

    @Query("SELECT * FROM student_grades WHERE isSynced = 0")
    suspend fun getUnsynced(): List<GradeEntity>

    @Query("UPDATE student_grades SET isSynced = 1 WHERE id IN (:ids)")
    suspend fun markAsSynced(ids: List<String>)

    // ==================== BATCH ====================

    @Query("UPDATE student_grades SET isPublished = 1, publishedAt = :publishedAt WHERE assessmentId = :assessmentId")
    suspend fun publishAllGrades(assessmentId: String, publishedAt: Long)

    @Query("DELETE FROM student_grades WHERE assessmentId = :assessmentId")
    suspend fun deleteByAssessment(assessmentId: String)

    @Query("DELETE FROM student_grades")
    suspend fun deleteAll()
}
