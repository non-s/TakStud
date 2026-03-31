package com.example.takstud.data.local.dao

import androidx.room.*
import com.example.takstud.data.local.entity.GradeEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface GradeDao {
    @Query("SELECT * FROM grades ORDER BY timestamp DESC")
    fun getAllGrades(): Flow<List<GradeEntity>>

    @Query("SELECT * FROM grades WHERE taskId = :taskId ORDER BY timestamp DESC")
    fun getGradesByTask(taskId: String): Flow<List<GradeEntity>>

    @Query("SELECT * FROM grades WHERE studentId = :studentId ORDER BY timestamp DESC")
    fun getGradesByStudent(studentId: String): Flow<List<GradeEntity>>

    @Query("SELECT * FROM grades WHERE id = :gradeId")
    suspend fun getGradeById(gradeId: String): GradeEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertGrade(grade: GradeEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertGrades(grades: List<GradeEntity>)

    @Update
    suspend fun updateGrade(grade: GradeEntity)

    @Delete
    suspend fun deleteGrade(grade: GradeEntity)

    @Query("DELETE FROM grades WHERE id = :gradeId")
    suspend fun deleteGradeById(gradeId: String)

    @Query("UPDATE grades SET isSynced = 1 WHERE id IN (:gradeIds)")
    suspend fun markAsSynced(gradeIds: List<String>)

    @Query("SELECT * FROM grades WHERE isSynced = 0")
    suspend fun getUnsyncedGrades(): List<GradeEntity>

    @Query("DELETE FROM grades")
    suspend fun deleteAll()
}
