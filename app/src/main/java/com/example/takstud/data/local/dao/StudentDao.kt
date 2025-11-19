package com.example.takstud.data.local.dao

import androidx.room.*
import com.example.takstud.data.local.entity.StudentEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface StudentDao {
    @Query("SELECT * FROM students ORDER BY name")
    fun getAllStudents(): Flow<List<StudentEntity>>

    @Query("SELECT * FROM students WHERE studentClass = :studentClass ORDER BY name")
    fun getStudentsByClass(studentClass: String): Flow<List<StudentEntity>>

    @Query("SELECT * FROM students WHERE id = :studentId")
    suspend fun getStudentById(studentId: String): StudentEntity?

    @Query("SELECT * FROM students WHERE ra = :ra")
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

    @Query("UPDATE students SET isSynced = 1 WHERE id IN (:studentIds)")
    suspend fun markAsSynced(studentIds: List<String>)

    @Query("SELECT * FROM students WHERE isSynced = 0")
    suspend fun getUnsyncedStudents(): List<StudentEntity>

    @Query("DELETE FROM students")
    suspend fun deleteAll()
}
