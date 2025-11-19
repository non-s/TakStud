package com.example.takstud.data

import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import com.example.takstud.model.AttendanceRecord
import com.example.takstud.model.Grade
import com.example.takstud.model.Notice
import com.example.takstud.model.Schedule
import com.example.takstud.model.Student
import com.example.takstud.model.Task
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.flow.Flow

/**
 * PagingRepository - Provides paginated access to Firestore collections
 * Implements efficient pagination with Paging 3 library
 */
class PagingRepository(private val db: FirebaseFirestore = FirebaseFirestore.getInstance()) {

    private companion object {
        const val PAGE_SIZE = 20
    }

    /**
     * Get paginated tasks
     */
    fun getTasksPaged(): Flow<PagingData<Task>> {
        return Pager(
            config = PagingConfig(pageSize = PAGE_SIZE, enablePlaceholders = false),
            pagingSourceFactory = {
                FirestorePagingSource(
                    query = db.collection("tasks").orderBy("createdAt", Query.Direction.DESCENDING),
                    pageSize = PAGE_SIZE,
                    mapper = { snapshot -> snapshot.toObject(Task::class.java) ?: Task() }
                )
            }
        ).flow
    }

    /**
     * Get paginated tasks for a specific student
     */
    fun getTasksForStudentPaged(studentClass: String): Flow<PagingData<Task>> {
        return Pager(
            config = PagingConfig(pageSize = PAGE_SIZE, enablePlaceholders = false),
            pagingSourceFactory = {
                FirestorePagingSource(
                    query = db.collection("tasks")
                        .whereEqualTo("studentClass", studentClass)
                        .orderBy("dueDate", Query.Direction.DESCENDING),
                    pageSize = PAGE_SIZE,
                    mapper = { snapshot -> snapshot.toObject(Task::class.java) ?: Task() }
                )
            }
        ).flow
    }

    /**
     * Get paginated notices
     */
    fun getNoticesPaged(): Flow<PagingData<Notice>> {
        return Pager(
            config = PagingConfig(pageSize = PAGE_SIZE, enablePlaceholders = false),
            pagingSourceFactory = {
                FirestorePagingSource(
                    query = db.collection("notices").orderBy("createdAt", Query.Direction.DESCENDING),
                    pageSize = PAGE_SIZE,
                    mapper = { snapshot -> snapshot.toObject(Notice::class.java) ?: Notice() }
                )
            }
        ).flow
    }

    /**
     * Get paginated notices for a specific student
     */
    fun getNoticesForStudentPaged(studentClass: String): Flow<PagingData<Notice>> {
        return Pager(
            config = PagingConfig(pageSize = PAGE_SIZE, enablePlaceholders = false),
            pagingSourceFactory = {
                FirestorePagingSource(
                    query = db.collection("notices")
                        .whereEqualTo("studentClass", studentClass)
                        .orderBy("createdAt", Query.Direction.DESCENDING),
                    pageSize = PAGE_SIZE,
                    mapper = { snapshot -> snapshot.toObject(Notice::class.java) ?: Notice() }
                )
            }
        ).flow
    }

    /**
     * Get paginated students
     */
    fun getStudentsPaged(): Flow<PagingData<Student>> {
        return Pager(
            config = PagingConfig(pageSize = PAGE_SIZE, enablePlaceholders = false),
            pagingSourceFactory = {
                FirestorePagingSource(
                    query = db.collection("students").orderBy("name", Query.Direction.ASCENDING),
                    pageSize = PAGE_SIZE,
                    mapper = { snapshot -> snapshot.toObject(Student::class.java) ?: Student() }
                )
            }
        ).flow
    }

    /**
     * Get paginated students for a specific class
     */
    fun getStudentsForClassPaged(studentClass: String): Flow<PagingData<Student>> {
        return Pager(
            config = PagingConfig(pageSize = PAGE_SIZE, enablePlaceholders = false),
            pagingSourceFactory = {
                FirestorePagingSource(
                    query = db.collection("students")
                        .whereEqualTo("studentClass", studentClass)
                        .orderBy("name", Query.Direction.ASCENDING),
                    pageSize = PAGE_SIZE,
                    mapper = { snapshot -> snapshot.toObject(Student::class.java) ?: Student() }
                )
            }
        ).flow
    }

    /**
     * Get paginated schedules
     */
    fun getSchedulesPaged(): Flow<PagingData<Schedule>> {
        return Pager(
            config = PagingConfig(pageSize = PAGE_SIZE, enablePlaceholders = false),
            pagingSourceFactory = {
                FirestorePagingSource(
                    query = db.collection("schedules").orderBy("period", Query.Direction.ASCENDING),
                    pageSize = PAGE_SIZE,
                    mapper = { snapshot -> snapshot.toObject(Schedule::class.java) ?: Schedule() }
                )
            }
        ).flow
    }

    /**
     * Get paginated grades for a specific task
     */
    fun getGradesForTaskPaged(taskId: String): Flow<PagingData<Grade>> {
        return Pager(
            config = PagingConfig(pageSize = PAGE_SIZE, enablePlaceholders = false),
            pagingSourceFactory = {
                FirestorePagingSource(
                    query = db.collection("grades")
                        .whereEqualTo("taskId", taskId)
                        .orderBy("timestamp", Query.Direction.DESCENDING),
                    pageSize = PAGE_SIZE,
                    mapper = { snapshot -> snapshot.toObject(Grade::class.java) ?: Grade() }
                )
            }
        ).flow
    }

    /**
     * Get paginated attendance records for a specific student
     */
    fun getAttendanceForStudentPaged(studentId: String): Flow<PagingData<AttendanceRecord>> {
        return Pager(
            config = PagingConfig(pageSize = PAGE_SIZE, enablePlaceholders = false),
            pagingSourceFactory = {
                FirestorePagingSource(
                    query = db.collection("attendance")
                        .whereEqualTo("studentId", studentId)
                        .orderBy("date", Query.Direction.DESCENDING),
                    pageSize = PAGE_SIZE,
                    mapper = { snapshot -> snapshot.toObject(AttendanceRecord::class.java) ?: AttendanceRecord() }
                )
            }
        ).flow
    }

    /**
     * Get paginated attendance records for a class on a specific date
     */
    fun getAttendanceForClassByDatePaged(studentClass: String, date: String): Flow<PagingData<AttendanceRecord>> {
        return Pager(
            config = PagingConfig(pageSize = PAGE_SIZE, enablePlaceholders = false),
            pagingSourceFactory = {
                FirestorePagingSource(
                    query = db.collection("attendance")
                        .whereEqualTo("studentClass", studentClass)
                        .whereEqualTo("date", date),
                    pageSize = PAGE_SIZE,
                    mapper = { snapshot -> snapshot.toObject(AttendanceRecord::class.java) ?: AttendanceRecord() }
                )
            }
        ).flow
    }
}
