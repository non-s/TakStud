package com.example.takstud.data.local.dao

import androidx.room.*
import com.example.takstud.data.local.entity.TaskEntity
import com.example.takstud.data.local.entity.TaskSubmissionEntity
import com.example.takstud.data.local.entity.TaskStatsEntity
import kotlinx.coroutines.flow.Flow

/**
 * 🗄️ DAO EXPANDIDO para Tarefas
 * Operações completas com queries avançadas
 */
@Dao
interface TaskDao {

    // ==================== BASIC CRUD ====================

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(task: TaskEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(tasks: List<TaskEntity>)

    @Update
    suspend fun update(task: TaskEntity)

    @Delete
    suspend fun delete(task: TaskEntity)

    @Query("DELETE FROM tasks WHERE id = :taskId")
    suspend fun deleteById(taskId: String)

    // ==================== QUERIES - SINGLE ====================

    @Query("SELECT * FROM tasks WHERE id = :taskId")
    suspend fun getById(taskId: String): TaskEntity?

    @Query("SELECT * FROM tasks WHERE id = :taskId")
    fun getByIdFlow(taskId: String): Flow<TaskEntity?>

    // ==================== QUERIES - LISTS ====================

    @Query("SELECT * FROM tasks WHERE isActive = 1 ORDER BY dueDate DESC")
    fun getAllActive(): Flow<List<TaskEntity>>

    @Query("SELECT * FROM tasks ORDER BY dueDate DESC")
    fun getAll(): Flow<List<TaskEntity>>

    @Query("SELECT * FROM tasks WHERE classId = :classId AND isActive = 1 ORDER BY dueDate ASC")
    fun getByClass(classId: String): Flow<List<TaskEntity>>

    @Query("SELECT * FROM tasks WHERE subjectId = :subjectId AND isActive = 1 ORDER BY dueDate ASC")
    fun getBySubject(subjectId: String): Flow<List<TaskEntity>>

    @Query("SELECT * FROM tasks WHERE teacherId = :teacherId AND isActive = 1 ORDER BY dueDate DESC")
    fun getByTeacher(teacherId: String): Flow<List<TaskEntity>>

    @Query("SELECT * FROM tasks WHERE type = :type AND isActive = 1 ORDER BY dueDate ASC")
    fun getByType(type: String): Flow<List<TaskEntity>>

    @Query("SELECT * FROM tasks WHERE status = :status AND isActive = 1 ORDER BY dueDate ASC")
    fun getByStatus(status: String): Flow<List<TaskEntity>>

    @Query("SELECT * FROM tasks WHERE priority = :priority AND isActive = 1 ORDER BY dueDate ASC")
    fun getByPriority(priority: String): Flow<List<TaskEntity>>

    // ==================== PUBLISHED / DRAFT ====================

    @Query("SELECT * FROM tasks WHERE isPublished = 1 AND isActive = 1 ORDER BY dueDate ASC")
    fun getPublished(): Flow<List<TaskEntity>>

    @Query("SELECT * FROM tasks WHERE isDraft = 1 AND isActive = 1 ORDER BY updatedAt DESC")
    fun getDrafts(): Flow<List<TaskEntity>>

    @Query("SELECT * FROM tasks WHERE isPublished = 1 AND classId = :classId AND isActive = 1 ORDER BY dueDate ASC")
    fun getPublishedByClass(classId: String): Flow<List<TaskEntity>>

    // ==================== DUE DATES ====================

    @Query("""
        SELECT * FROM tasks
        WHERE dueDate IS NOT NULL
        AND dueDate > :now
        AND isPublished = 1
        AND isActive = 1
        ORDER BY dueDate ASC
    """)
    fun getUpcoming(now: Long = System.currentTimeMillis()): Flow<List<TaskEntity>>

    @Query("""
        SELECT * FROM tasks
        WHERE dueDate IS NOT NULL
        AND dueDate < :now
        AND status != 'COMPLETED'
        AND isPublished = 1
        AND isActive = 1
        ORDER BY dueDate DESC
    """)
    fun getOverdue(now: Long = System.currentTimeMillis()): Flow<List<TaskEntity>>

    @Query("""
        SELECT * FROM tasks
        WHERE dueDate IS NOT NULL
        AND dueDate BETWEEN :startOfDay AND :endOfDay
        AND isPublished = 1
        AND isActive = 1
        ORDER BY dueDate ASC
    """)
    fun getDueToday(startOfDay: Long, endOfDay: Long): Flow<List<TaskEntity>>

    @Query("""
        SELECT * FROM tasks
        WHERE dueDate IS NOT NULL
        AND dueDate BETWEEN :startOfWeek AND :endOfWeek
        AND isPublished = 1
        AND isActive = 1
        ORDER BY dueDate ASC
    """)
    fun getDueThisWeek(startOfWeek: Long, endOfWeek: Long): Flow<List<TaskEntity>>

    // ==================== SEARCH ====================

    @Query("""
        SELECT * FROM tasks
        WHERE (title LIKE '%' || :query || '%' OR description LIKE '%' || :query || '%')
        AND isActive = 1
        ORDER BY dueDate ASC
    """)
    fun search(query: String): Flow<List<TaskEntity>>

    @Query("""
        SELECT * FROM tasks
        WHERE tagsJson LIKE '%' || :tag || '%'
        AND isActive = 1
        ORDER BY dueDate ASC
    """)
    fun searchByTag(tag: String): Flow<List<TaskEntity>>

    // ==================== ADVANCED FILTERS ====================

    @Query("""
        SELECT * FROM tasks
        WHERE (:classId IS NULL OR classId = :classId)
        AND (:subjectId IS NULL OR subjectId = :subjectId)
        AND (:type IS NULL OR type = :type)
        AND (:status IS NULL OR status = :status)
        AND (:priority IS NULL OR priority = :priority)
        AND (:isPublished IS NULL OR isPublished = :isPublished)
        AND isActive = 1
        ORDER BY dueDate ASC
    """)
    fun filter(
        classId: String?,
        subjectId: String?,
        type: String?,
        status: String?,
        priority: String?,
        isPublished: Boolean?
    ): Flow<List<TaskEntity>>

    // ==================== GRADING ====================

    @Query("""
        SELECT * FROM tasks
        WHERE pendingGrading > 0
        AND isPublished = 1
        AND isActive = 1
        ORDER BY dueDate ASC
    """)
    fun getWithPendingGrading(): Flow<List<TaskEntity>>

    @Query("""
        SELECT * FROM tasks
        WHERE gradedSubmissions > 0
        AND isPublished = 1
        AND isActive = 1
        ORDER BY dueDate DESC
    """)
    fun getGraded(): Flow<List<TaskEntity>>

    // ==================== STATISTICS ====================

    @Query("SELECT COUNT(*) FROM tasks WHERE isActive = 1")
    fun countActive(): Flow<Int>

    @Query("SELECT COUNT(*) FROM tasks WHERE classId = :classId AND isActive = 1")
    fun countByClass(classId: String): Flow<Int>

    @Query("SELECT COUNT(*) FROM tasks WHERE isPublished = 1 AND isActive = 1")
    suspend fun countPublished(): Int

    @Query("SELECT COUNT(*) FROM tasks WHERE isDraft = 1 AND isActive = 1")
    suspend fun countDrafts(): Int

    @Query("SELECT AVG(averageGrade) FROM tasks WHERE averageGrade > 0 AND isActive = 1")
    suspend fun getAverageGrade(): Double?

    @Query("SELECT AVG(completionRate) FROM tasks WHERE completionRate > 0 AND isActive = 1")
    suspend fun getAverageCompletionRate(): Double?

    @Query("SELECT DISTINCT classId FROM tasks WHERE isActive = 1 ORDER BY className ASC")
    fun getDistinctClasses(): Flow<List<String>>

    @Query("SELECT DISTINCT subjectId FROM tasks WHERE isActive = 1 ORDER BY subjectName ASC")
    fun getDistinctSubjects(): Flow<List<String>>

    // ==================== BATCH OPERATIONS ====================

    @Query("""
        UPDATE tasks
        SET isPublished = :isPublished, updatedAt = :timestamp
        WHERE id IN (:taskIds)
    """)
    suspend fun updatePublishStatus(
        taskIds: List<String>,
        isPublished: Boolean,
        timestamp: Long = System.currentTimeMillis()
    )

    @Query("""
        UPDATE tasks
        SET status = :status, updatedAt = :timestamp
        WHERE id IN (:taskIds)
    """)
    suspend fun updateStatus(
        taskIds: List<String>,
        status: String,
        timestamp: Long = System.currentTimeMillis()
    )

    @Query("SELECT * FROM tasks WHERE id IN (:taskIds)")
    suspend fun getMultipleByIds(taskIds: List<String>): List<TaskEntity>

    @Query("DELETE FROM tasks WHERE id IN (:taskIds)")
    suspend fun deleteMultiple(taskIds: List<String>)

    // ==================== DATA CLEANUP ====================

    @Query("""
        DELETE FROM tasks
        WHERE isActive = 0
        AND updatedAt < :cutoffTimestamp
    """)
    suspend fun cleanupOld(cutoffTimestamp: Long)

    @Query("SELECT COUNT(*) FROM tasks")
    suspend fun getTotalCount(): Int

    @Query("DELETE FROM tasks")
    suspend fun deleteAll()

    // ==================== LEGACY COMPATIBILITY ====================

    @Query("SELECT * FROM tasks ORDER BY dueDate DESC")
    fun getAllTasks(): Flow<List<TaskEntity>>

    @Query("SELECT * FROM tasks WHERE classId = :studentClass ORDER BY dueDate DESC")
    fun getTasksByClass(studentClass: String): Flow<List<TaskEntity>>

    @Query("SELECT * FROM tasks WHERE id = :taskId")
    suspend fun getTaskById(taskId: String): TaskEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTask(task: TaskEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTasks(tasks: List<TaskEntity>)

    @Update
    suspend fun updateTask(task: TaskEntity)

    @Delete
    suspend fun deleteTask(task: TaskEntity)

    @Query("DELETE FROM tasks WHERE id = :taskId")
    suspend fun deleteTaskById(taskId: String)
}

// ==================== SUBMISSION DAO ====================

@Dao
interface TaskSubmissionDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(submission: TaskSubmissionEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(submissions: List<TaskSubmissionEntity>)

    @Update
    suspend fun update(submission: TaskSubmissionEntity)

    @Delete
    suspend fun delete(submission: TaskSubmissionEntity)

    @Query("DELETE FROM task_submissions WHERE id = :submissionId")
    suspend fun deleteById(submissionId: String)

    @Query("SELECT * FROM task_submissions WHERE id = :submissionId")
    suspend fun getById(submissionId: String): TaskSubmissionEntity?

    @Query("SELECT * FROM task_submissions WHERE id = :submissionId")
    fun getByIdFlow(submissionId: String): Flow<TaskSubmissionEntity?>

    @Query("""
        SELECT * FROM task_submissions
        WHERE taskId = :taskId
        ORDER BY submittedAt DESC
    """)
    fun getByTask(taskId: String): Flow<List<TaskSubmissionEntity>>

    @Query("""
        SELECT * FROM task_submissions
        WHERE studentId = :studentId
        ORDER BY submittedAt DESC
    """)
    fun getByStudent(studentId: String): Flow<List<TaskSubmissionEntity>>

    @Query("""
        SELECT * FROM task_submissions
        WHERE taskId = :taskId AND studentId = :studentId
        ORDER BY attemptNumber DESC
    """)
    fun getByTaskAndStudent(taskId: String, studentId: String): Flow<List<TaskSubmissionEntity>>

    @Query("""
        SELECT * FROM task_submissions
        WHERE taskId = :taskId AND status = :status
        ORDER BY submittedAt DESC
    """)
    fun getByTaskAndStatus(taskId: String, status: String): Flow<List<TaskSubmissionEntity>>

    @Query("""
        SELECT * FROM task_submissions
        WHERE taskId = :taskId AND grade IS NULL
        ORDER BY submittedAt ASC
    """)
    fun getPendingGrading(taskId: String): Flow<List<TaskSubmissionEntity>>

    @Query("""
        SELECT * FROM task_submissions
        WHERE taskId = :taskId AND isLate = 1
        ORDER BY submittedAt DESC
    """)
    fun getLateSubmissions(taskId: String): Flow<List<TaskSubmissionEntity>>

    @Query("DELETE FROM task_submissions WHERE taskId = :taskId")
    suspend fun deleteAllByTask(taskId: String)

    @Query("DELETE FROM task_submissions WHERE studentId = :studentId")
    suspend fun deleteAllByStudent(studentId: String)
}

// ==================== STATS DAO ====================

@Dao
interface TaskStatsDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(stats: TaskStatsEntity)

    @Update
    suspend fun update(stats: TaskStatsEntity)

    @Delete
    suspend fun delete(stats: TaskStatsEntity)

    @Query("SELECT * FROM task_stats WHERE taskId = :taskId")
    suspend fun getByTask(taskId: String): TaskStatsEntity?

    @Query("SELECT * FROM task_stats WHERE taskId = :taskId")
    fun getByTaskFlow(taskId: String): Flow<TaskStatsEntity?>

    @Query("DELETE FROM task_stats WHERE taskId = :taskId")
    suspend fun deleteByTask(taskId: String)

    @Query("""
        SELECT * FROM task_stats
        WHERE averageGrade > 0
        ORDER BY averageGrade DESC
        LIMIT :limit
    """)
    fun getTopPerforming(limit: Int = 10): Flow<List<TaskStatsEntity>>

    @Query("""
        SELECT * FROM task_stats
        WHERE lastUpdated < :cutoffTimestamp
    """)
    suspend fun getOutdated(cutoffTimestamp: Long): List<TaskStatsEntity>
}
