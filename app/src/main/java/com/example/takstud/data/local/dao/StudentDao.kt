package com.example.takstud.data.local.dao

import androidx.room.*
import com.example.takstud.data.local.entity.StudentEntity
import com.example.takstud.data.local.entity.StudentStatsEntity
import com.example.takstud.data.local.entity.StudentTimelineEventEntity
import kotlinx.coroutines.flow.Flow

/**
 * 🗄️ DAO EXPANDIDO para Alunos
 * Operações completas de banco de dados com queries avançadas
 */
@Dao
interface StudentDao {

    // ==================== BASIC CRUD ====================

    /**
     * Inserir aluno
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(student: StudentEntity)

    /**
     * Inserir múltiplos alunos
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(students: List<StudentEntity>)

    /**
     * Atualizar aluno
     */
    @Update
    suspend fun update(student: StudentEntity)

    /**
     * Deletar aluno
     */
    @Delete
    suspend fun delete(student: StudentEntity)

    /**
     * Deletar por ID
     */
    @Query("DELETE FROM students WHERE id = :studentId")
    suspend fun deleteById(studentId: String)

    /**
     * Soft delete (desativar)
     */
    @Query("UPDATE students SET isActive = 0, updatedAt = :timestamp WHERE id = :studentId")
    suspend fun deactivate(studentId: String, timestamp: Long = System.currentTimeMillis())

    /**
     * Reativar aluno
     */
    @Query("UPDATE students SET isActive = 1, updatedAt = :timestamp WHERE id = :studentId")
    suspend fun reactivate(studentId: String, timestamp: Long = System.currentTimeMillis())

    // ==================== QUERIES - SINGLE ====================

    /**
     * Buscar por ID
     */
    @Query("SELECT * FROM students WHERE id = :studentId")
    suspend fun getById(studentId: String): StudentEntity?

    /**
     * Buscar por ID (Flow - real-time)
     */
    @Query("SELECT * FROM students WHERE id = :studentId")
    fun getByIdFlow(studentId: String): Flow<StudentEntity?>

    /**
     * Buscar por CPF
     */
    @Query("SELECT * FROM students WHERE cpf = :cpf LIMIT 1")
    suspend fun getByCpf(cpf: String): StudentEntity?

    /**
     * Buscar por matrícula
     */
    @Query("SELECT * FROM students WHERE registrationNumber = :registrationNumber LIMIT 1")
    suspend fun getByRegistrationNumber(registrationNumber: String): StudentEntity?

    // ==================== QUERIES - LISTS ====================

    /**
     * Obter todos os alunos ativos
     */
    @Query("SELECT * FROM students WHERE isActive = 1 ORDER BY fullName ASC")
    fun getAllActive(): Flow<List<StudentEntity>>

    /**
     * Obter todos os alunos (ativos + inativos)
     */
    @Query("SELECT * FROM students ORDER BY fullName ASC")
    fun getAll(): Flow<List<StudentEntity>>

    /**
     * Obter alunos por turma
     */
    @Query("SELECT * FROM students WHERE className = :className AND isActive = 1 ORDER BY fullName ASC")
    fun getByClass(className: String): Flow<List<StudentEntity>>

    /**
     * Obter alunos por série
     */
    @Query("SELECT * FROM students WHERE grade = :grade AND isActive = 1 ORDER BY fullName ASC")
    fun getByGrade(grade: String): Flow<List<StudentEntity>>

    /**
     * Obter alunos por período
     */
    @Query("SELECT * FROM students WHERE period = :period AND isActive = 1 ORDER BY fullName ASC")
    fun getByPeriod(period: String): Flow<List<StudentEntity>>

    /**
     * Obter alunos por status
     */
    @Query("SELECT * FROM students WHERE status = :status ORDER BY fullName ASC")
    fun getByStatus(status: String): Flow<List<StudentEntity>>

    /**
     * Obter bolsistas
     */
    @Query("SELECT * FROM students WHERE isScholarship = 1 AND isActive = 1 ORDER BY scholarshipPercentage DESC")
    fun getScholarshipStudents(): Flow<List<StudentEntity>>

    // ==================== SEARCH ====================

    /**
     * Buscar por nome
     */
    @Query("""
        SELECT * FROM students
        WHERE (fullName LIKE '%' || :query || '%' OR preferredName LIKE '%' || :query || '%')
        AND isActive = 1
        ORDER BY fullName ASC
    """)
    fun searchByName(query: String): Flow<List<StudentEntity>>

    /**
     * Buscar por qualquer campo
     */
    @Query("""
        SELECT * FROM students
        WHERE (
            fullName LIKE '%' || :query || '%' OR
            preferredName LIKE '%' || :query || '%' OR
            cpf LIKE '%' || :query || '%' OR
            registrationNumber LIKE '%' || :query || '%' OR
            email LIKE '%' || :query || '%' OR
            phone LIKE '%' || :query || '%'
        )
        AND isActive = 1
        ORDER BY fullName ASC
    """)
    fun searchAll(query: String): Flow<List<StudentEntity>>

    /**
     * Buscar por tags
     */
    @Query("""
        SELECT * FROM students
        WHERE tagsJson LIKE '%' || :tag || '%'
        AND isActive = 1
        ORDER BY fullName ASC
    """)
    fun searchByTag(tag: String): Flow<List<StudentEntity>>

    // ==================== ADVANCED FILTERS ====================

    /**
     * Filtrar por múltiplos critérios
     */
    @Query("""
        SELECT * FROM students
        WHERE (:className IS NULL OR className = :className)
        AND (:grade IS NULL OR grade = :grade)
        AND (:period IS NULL OR period = :period)
        AND (:status IS NULL OR status = :status)
        AND (:isScholarship IS NULL OR isScholarship = :isScholarship)
        AND isActive = 1
        ORDER BY fullName ASC
    """)
    fun filterStudents(
        className: String?,
        grade: String?,
        period: String?,
        status: String?,
        isScholarship: Boolean?
    ): Flow<List<StudentEntity>>

    /**
     * Obter alunos com baixo desempenho (GPA < threshold)
     */
    @Query("""
        SELECT * FROM students
        WHERE gpa < :threshold AND gpa > 0
        AND isActive = 1
        ORDER BY gpa ASC
    """)
    fun getLowPerformanceStudents(threshold: Double = 5.0): Flow<List<StudentEntity>>

    /**
     * Obter alunos com baixa frequência (attendance < threshold)
     */
    @Query("""
        SELECT * FROM students
        WHERE attendanceRate < :threshold AND attendanceRate > 0
        AND isActive = 1
        ORDER BY attendanceRate ASC
    """)
    fun getLowAttendanceStudents(threshold: Double = 75.0): Flow<List<StudentEntity>>

    /**
     * Obter alunos com necessidades especiais
     */
    @Query("""
        SELECT * FROM students
        WHERE healthInfoJson LIKE '%specialNeeds%'
        AND isActive = 1
        ORDER BY fullName ASC
    """)
    fun getStudentsWithSpecialNeeds(): Flow<List<StudentEntity>>

    /**
     * Obter aniversariantes do mês
     */
    @Query("""
        SELECT * FROM students
        WHERE birthDate IS NOT NULL
        AND isActive = 1
        ORDER BY birthDate ASC
    """)
    fun getAllWithBirthdays(): Flow<List<StudentEntity>>

    // ==================== STATISTICS ====================

    /**
     * Contar alunos ativos
     */
    @Query("SELECT COUNT(*) FROM students WHERE isActive = 1")
    fun countActive(): Flow<Int>

    /**
     * Contar alunos por turma
     */
    @Query("SELECT COUNT(*) FROM students WHERE className = :className AND isActive = 1")
    fun countByClass(className: String): Flow<Int>

    /**
     * Contar alunos por status
     */
    @Query("SELECT COUNT(*) FROM students WHERE status = :status")
    suspend fun countByStatus(status: String): Int

    /**
     * Obter média geral de todos os alunos
     */
    @Query("SELECT AVG(gpa) FROM students WHERE isActive = 1 AND gpa > 0")
    suspend fun getAverageGPA(): Double?

    /**
     * Obter média de frequência
     */
    @Query("SELECT AVG(attendanceRate) FROM students WHERE isActive = 1 AND attendanceRate > 0")
    suspend fun getAverageAttendance(): Double?

    /**
     * Obter lista de turmas únicas
     */
    @Query("SELECT DISTINCT className FROM students WHERE isActive = 1 ORDER BY className ASC")
    fun getDistinctClasses(): Flow<List<String>>

    /**
     * Obter lista de séries únicas
     */
    @Query("SELECT DISTINCT grade FROM students WHERE isActive = 1 ORDER BY grade ASC")
    fun getDistinctGrades(): Flow<List<String>>

    // ==================== BATCH OPERATIONS ====================

    /**
     * Atualizar turma de múltiplos alunos
     */
    @Query("""
        UPDATE students
        SET className = :newClassName, updatedAt = :timestamp
        WHERE id IN (:studentIds)
    """)
    suspend fun updateClassForMultiple(
        studentIds: List<String>,
        newClassName: String,
        timestamp: Long = System.currentTimeMillis()
    )

    /**
     * Atualizar status de múltiplos alunos
     */
    @Query("""
        UPDATE students
        SET status = :newStatus, updatedAt = :timestamp
        WHERE id IN (:studentIds)
    """)
    suspend fun updateStatusForMultiple(
        studentIds: List<String>,
        newStatus: String,
        timestamp: Long = System.currentTimeMillis()
    )

    /**
     * Obter múltiplos por IDs
     */
    @Query("SELECT * FROM students WHERE id IN (:studentIds)")
    suspend fun getMultipleByIds(studentIds: List<String>): List<StudentEntity>

    /**
     * Deletar múltiplos alunos
     */
    @Query("DELETE FROM students WHERE id IN (:studentIds)")
    suspend fun deleteMultiple(studentIds: List<String>)

    /**
     * Desativar múltiplos alunos
     */
    @Query("""
        UPDATE students
        SET isActive = 0, updatedAt = :timestamp
        WHERE id IN (:studentIds)
    """)
    suspend fun deactivateMultiple(
        studentIds: List<String>,
        timestamp: Long = System.currentTimeMillis()
    )

    // ==================== DATA CLEANUP ====================

    /**
     * Limpar alunos inativos antigos (mais de X dias)
     */
    @Query("""
        DELETE FROM students
        WHERE isActive = 0
        AND updatedAt < :cutoffTimestamp
    """)
    suspend fun cleanupOldInactive(cutoffTimestamp: Long)

    /**
     * Obter contagem total (debug)
     */
    @Query("SELECT COUNT(*) FROM students")
    suspend fun getTotalCount(): Int

    /**
     * Deletar todos
     */
    @Query("DELETE FROM students")
    suspend fun deleteAll()

    // ==================== LEGACY COMPATIBILITY ====================
    // Manter métodos antigos para compatibilidade

    @Query("SELECT * FROM students ORDER BY fullName")
    fun getAllStudents(): Flow<List<StudentEntity>>

    @Query("SELECT * FROM students WHERE className = :studentClass ORDER BY fullName")
    fun getStudentsByClass(studentClass: String): Flow<List<StudentEntity>>

    @Query("SELECT * FROM students WHERE id = :studentId")
    suspend fun getStudentById(studentId: String): StudentEntity?

    @Query("SELECT * FROM students WHERE registrationNumber = :ra")
    suspend fun getStudentByRa(ra: String): StudentEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertStudent(student: StudentEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertStudents(students: List<StudentEntity>)

    @Update
    suspend fun updateStudent(student: StudentEntity)

    @Delete
    suspend fun deleteStudent(student: StudentEntity)

    @Query("DELETE FROM students WHERE id = :studentId")
    suspend fun deleteStudentById(studentId: String)
}
