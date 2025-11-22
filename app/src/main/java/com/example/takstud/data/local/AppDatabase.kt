package com.example.takstud.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.takstud.data.local.dao.*
import com.example.takstud.data.local.entity.*

/**
 * AppDatabase - Room Database para armazenamento local offline
 * Sincroniza com Firebase quando conexão está disponível
 *
 * Implementa o padrão de cache-first com sync-on-reconnect
 */
@Database(
    entities = [
        TaskEntity::class,
        NoticeEntity::class,
        ScheduleEntity::class,
        StudentEntity::class,
        GradeEntity::class,
        AttendanceEntity::class,
        SyncQueueEntity::class,
        SubjectEntity::class,
        TimeSlotEntity::class,
        ClassScheduleEntity::class,
        StudentTimelineEventEntity::class,
        StudentStatsEntity::class,
        AssessmentEntity::class
    ],
    version = 2,
    exportSchema = true
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun taskDao(): TaskDao
    abstract fun noticeDao(): NoticeDao
    abstract fun scheduleDao(): ScheduleDao
    abstract fun studentDao(): StudentDao
    abstract fun gradeDao(): GradeDao
    abstract fun attendanceDao(): AttendanceDao
    abstract fun syncQueueDao(): SyncQueueDao
    abstract fun subjectDao(): SubjectDao
    abstract fun timeSlotDao(): TimeSlotDao
    abstract fun classScheduleDao(): ClassScheduleDao
    abstract fun studentTimelineDao(): StudentTimelineDao
    abstract fun studentStatsDao(): StudentStatsDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "takstud_database"
                )
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
