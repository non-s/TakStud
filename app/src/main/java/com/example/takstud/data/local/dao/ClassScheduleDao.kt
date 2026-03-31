package com.example.takstud.data.local.dao

import androidx.room.*
import com.example.takstud.data.local.entity.ClassScheduleEntity
import kotlinx.coroutines.flow.Flow

/**
 * 📅 DAO para Grades Horárias
 */
@Dao
interface ClassScheduleDao {

    @Query("SELECT * FROM class_schedules WHERE isActive = 1 AND isTemplate = 0 ORDER BY className ASC")
    fun getAllActive(): Flow<List<ClassScheduleEntity>>

    @Query("SELECT * FROM class_schedules WHERE isTemplate = 1 ORDER BY templateName ASC")
    fun getAllTemplates(): Flow<List<ClassScheduleEntity>>

    @Query("SELECT * FROM class_schedules WHERE id = :id")
    suspend fun getById(id: String): ClassScheduleEntity?

    @Query("SELECT * FROM class_schedules WHERE id = :id")
    fun getByIdFlow(id: String): Flow<ClassScheduleEntity?>

    @Query("SELECT * FROM class_schedules WHERE className = :className AND year = :year AND isActive = 1")
    suspend fun getByClassAndYear(className: String, year: Int): ClassScheduleEntity?

    @Query("SELECT * FROM class_schedules WHERE period = :period AND isActive = 1")
    fun getByPeriod(period: String): Flow<List<ClassScheduleEntity>>

    @Query("SELECT * FROM class_schedules WHERE year = :year AND isActive = 1")
    fun getByYear(year: Int): Flow<List<ClassScheduleEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(schedule: ClassScheduleEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(schedules: List<ClassScheduleEntity>)

    @Update
    suspend fun update(schedule: ClassScheduleEntity)

    @Delete
    suspend fun delete(schedule: ClassScheduleEntity)

    @Query("DELETE FROM class_schedules WHERE id = :id")
    suspend fun deleteById(id: String)

    @Query("UPDATE class_schedules SET isActive = 0 WHERE id = :id")
    suspend fun deactivate(id: String)

    @Query("SELECT COUNT(*) FROM class_schedules WHERE isActive = 1 AND isTemplate = 0")
    suspend fun getActiveCount(): Int

    @Query("SELECT COUNT(*) FROM class_schedules WHERE isTemplate = 1")
    suspend fun getTemplateCount(): Int
}
