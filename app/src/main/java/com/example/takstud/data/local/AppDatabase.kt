package com.example.takstud.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

import com.example.takstud.data.local.dao.EventDao
import com.example.takstud.data.local.dao.NoticeDao
import com.example.takstud.data.local.dao.NotificationDao
import com.example.takstud.data.local.dao.ScheduleDao
import com.example.takstud.data.local.dao.TaskDao
import com.example.takstud.data.local.entity.EventEntity
import com.example.takstud.data.local.entity.NoticeEntity
import com.example.takstud.data.local.entity.NotificationEntity
import com.example.takstud.data.local.entity.ScheduleEntity
import com.example.takstud.data.local.entity.TaskEntity

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
        EventEntity::class,
        NotificationEntity::class
    ],
    version = 3,
    exportSchema = true
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun taskDao(): TaskDao
    abstract fun noticeDao(): NoticeDao
    abstract fun scheduleDao(): ScheduleDao
    abstract fun eventDao(): EventDao
    abstract fun notificationDao(): NotificationDao

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
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
