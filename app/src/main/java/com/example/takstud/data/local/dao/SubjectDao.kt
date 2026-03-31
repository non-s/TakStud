package com.example.takstud.data.local.dao

import androidx.room.*
import com.example.takstud.data.local.entity.SubjectEntity
import kotlinx.coroutines.flow.Flow

/**
 * 📚 DAO para Disciplinas
 */
@Dao
interface SubjectDao {

    @Query("SELECT * FROM subjects WHERE isActive = 1 ORDER BY name ASC")
    fun getAllActive(): Flow<List<SubjectEntity>>

    @Query("SELECT * FROM subjects ORDER BY name ASC")
    fun getAll(): Flow<List<SubjectEntity>>

    @Query("SELECT * FROM subjects WHERE id = :id")
    suspend fun getById(id: String): SubjectEntity?

    @Query("SELECT * FROM subjects WHERE teacherId = :teacherId AND isActive = 1")
    fun getByTeacher(teacherId: String): Flow<List<SubjectEntity>>

    @Query("SELECT * FROM subjects WHERE name LIKE '%' || :query || '%' OR shortName LIKE '%' || :query || '%'")
    fun search(query: String): Flow<List<SubjectEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(subject: SubjectEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(subjects: List<SubjectEntity>)

    @Update
    suspend fun update(subject: SubjectEntity)

    @Delete
    suspend fun delete(subject: SubjectEntity)

    @Query("DELETE FROM subjects WHERE id = :id")
    suspend fun deleteById(id: String)

    @Query("UPDATE subjects SET isActive = 0 WHERE id = :id")
    suspend fun deactivate(id: String)

    @Query("SELECT COUNT(*) FROM subjects WHERE isActive = 1")
    suspend fun getActiveCount(): Int
}
