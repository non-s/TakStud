package com.example.takstud.data.local.dao

import androidx.room.*
import com.example.takstud.data.local.entity.AttendanceEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface AttendanceDao {
    @Query("SELECT * FROM attendance ORDER BY date DESC")
    fun getAllAttendance(): Flow<List<AttendanceEntity>>

    @Query("SELECT * FROM attendance WHERE studentId = :studentId ORDER BY date DESC")
    fun getAttendanceByStudent(studentId: String): Flow<List<AttendanceEntity>>

    @Query("SELECT * FROM attendance WHERE studentClass = :studentClass AND date = :date ORDER BY studentId")
    fun getAttendanceForClassByDate(studentClass: String, date: String): Flow<List<AttendanceEntity>>

    @Query("SELECT * FROM attendance WHERE id = :attendanceId")
    suspend fun getAttendanceById(attendanceId: String): AttendanceEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAttendance(attendance: AttendanceEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAttendances(attendances: List<AttendanceEntity>)

    @Update
    suspend fun updateAttendance(attendance: AttendanceEntity)

    @Delete
    suspend fun deleteAttendance(attendance: AttendanceEntity)

    @Query("DELETE FROM attendance WHERE id = :attendanceId")
    suspend fun deleteAttendanceById(attendanceId: String)

    @Query("UPDATE attendance SET isSynced = 1 WHERE id IN (:attendanceIds)")
    suspend fun markAsSynced(attendanceIds: List<String>)

    @Query("SELECT * FROM attendance WHERE isSynced = 0")
    suspend fun getUnsyncedAttendance(): List<AttendanceEntity>

    @Query("DELETE FROM attendance")
    suspend fun deleteAll()
}
