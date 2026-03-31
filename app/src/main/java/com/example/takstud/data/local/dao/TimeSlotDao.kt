package com.example.takstud.data.local.dao

import androidx.room.*
import com.example.takstud.data.local.entity.TimeSlotEntity
import kotlinx.coroutines.flow.Flow

/**
 * ⏰ DAO para Slots de Horário
 */
@Dao
interface TimeSlotDao {

    @Query("SELECT * FROM time_slots WHERE scheduleId = :scheduleId ORDER BY dayOfWeek, startTime ASC")
    fun getBySchedule(scheduleId: String): Flow<List<TimeSlotEntity>>

    @Query("SELECT * FROM time_slots WHERE scheduleId = :scheduleId AND isBreak = 0 ORDER BY dayOfWeek, startTime ASC")
    suspend fun getByScheduleSync(scheduleId: String): List<TimeSlotEntity>

    @Query("SELECT * FROM time_slots WHERE scheduleId = :scheduleId AND dayOfWeek = :dayOfWeek ORDER BY startTime ASC")
    fun getByScheduleAndDay(scheduleId: String, dayOfWeek: String): Flow<List<TimeSlotEntity>>

    @Query("SELECT * FROM time_slots WHERE scheduleId = :scheduleId AND isBreak = 1 ORDER BY dayOfWeek, startTime ASC")
    fun getBreaksBySchedule(scheduleId: String): Flow<List<TimeSlotEntity>>

    @Query("SELECT * FROM time_slots WHERE scheduleId = :scheduleId AND isBreak = 1 ORDER BY dayOfWeek, startTime ASC")
    suspend fun getBreaksByScheduleSync(scheduleId: String): List<TimeSlotEntity>

    @Query("SELECT * FROM time_slots WHERE id = :id")
    suspend fun getById(id: String): TimeSlotEntity?

    @Query("SELECT * FROM time_slots WHERE subjectId = :subjectId")
    fun getBySubject(subjectId: String): Flow<List<TimeSlotEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(timeSlot: TimeSlotEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(timeSlots: List<TimeSlotEntity>)

    @Update
    suspend fun update(timeSlot: TimeSlotEntity)

    @Delete
    suspend fun delete(timeSlot: TimeSlotEntity)

    @Query("DELETE FROM time_slots WHERE id = :id")
    suspend fun deleteById(id: String)

    @Query("DELETE FROM time_slots WHERE scheduleId = :scheduleId")
    suspend fun deleteBySchedule(scheduleId: String)

    @Query("SELECT COUNT(*) FROM time_slots WHERE scheduleId = :scheduleId")
    suspend fun getCountBySchedule(scheduleId: String): Int
}
