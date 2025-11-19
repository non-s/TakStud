package com.example.takstud.data.local.dao

import androidx.room.*
import com.example.takstud.data.local.entity.ScheduleEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ScheduleDao {
    @Query("SELECT * FROM schedules ORDER BY studentClass, period")
    fun getAllSchedules(): Flow<List<ScheduleEntity>>

    @Query("SELECT * FROM schedules WHERE studentClass = :studentClass ORDER BY period")
    fun getSchedulesByClass(studentClass: String): Flow<List<ScheduleEntity>>

    @Query("SELECT * FROM schedules WHERE id = :scheduleId")
    suspend fun getScheduleById(scheduleId: String): ScheduleEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSchedule(schedule: ScheduleEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSchedules(schedules: List<ScheduleEntity>)

    @Update
    suspend fun updateSchedule(schedule: ScheduleEntity)

    @Delete
    suspend fun deleteSchedule(schedule: ScheduleEntity)

    @Query("DELETE FROM schedules WHERE id = :scheduleId")
    suspend fun deleteScheduleById(scheduleId: String)

    @Query("UPDATE schedules SET isSynced = 1 WHERE id IN (:scheduleIds)")
    suspend fun markAsSynced(scheduleIds: List<String>)

    @Query("SELECT * FROM schedules WHERE isSynced = 0")
    suspend fun getUnsyncedSchedules(): List<ScheduleEntity>

    @Query("DELETE FROM schedules")
    suspend fun deleteAll()
}
