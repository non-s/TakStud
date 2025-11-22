package com.example.takstud.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.Index

// TaskEntity moved to TaskEntities.kt

/**
 * NoticeEntity - Room entity para armazenar avisos localmente
 */
@Entity(tableName = "notices", indices = [Index("studentClass")])
data class NoticeEntity(
    @PrimaryKey val id: String = "",
    val title: String = "",
    val content: String = "",
    val studentClass: String = "",
    val createdAt: String = "",
    val isSynced: Boolean = false,
    val lastModified: Long = System.currentTimeMillis()
)

/**
 * ScheduleEntity - Room entity para armazenar aulas localmente
 */
@Entity(tableName = "schedules", indices = [Index("studentClass")])
data class ScheduleEntity(
    @PrimaryKey val id: String = "",
    val classCode: String = "",
    val studentClass: String = "",
    val period: String = "",
    val isSynced: Boolean = false,
    val lastModified: Long = System.currentTimeMillis()
)

// StudentEntity moved to StudentEntities.kt

/**
 * GradeEntity - Room entity para armazenar notas localmente
 */
@Entity(tableName = "grades", indices = [Index("taskId"), Index("studentId")])
data class GradeEntity(
    @PrimaryKey val id: String = "",
    val taskId: String = "",
    val studentId: String = "",
    val score: Double = 0.0,
    val timestamp: String = "",
    val isSynced: Boolean = false,
    val lastModified: Long = System.currentTimeMillis()
)

/**
 * AttendanceEntity - Room entity para armazenar presença localmente
 */
@Entity(tableName = "attendance", indices = [Index("studentId"), Index("date"), Index("studentClass"), Index("classId")])
data class AttendanceEntity(
    @PrimaryKey val id: String = "",
    val studentId: String = "",
    val studentRa: String = "",
    val studentName: String = "",
    val studentClass: String = "",
    val classId: String = "",
    val date: String = "",
    val isPresent: Boolean = true,
    val timestamp: String = "",
    val isSynced: Boolean = false,
    val lastModified: Long = System.currentTimeMillis()
)

/**
 * SyncQueueEntity - Fila de sincronização para rastrear operações pendentes
 * Usada para manter controle de mudanças que precisam ser sincronizadas com Firestore
 */
@Entity(tableName = "sync_queue")
data class SyncQueueEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val operation: String = "", // CREATE, UPDATE, DELETE
    val collection: String = "", // tasks, notices, students, etc.
    val documentId: String = "",
    val data: String = "", // JSON string
    val timestamp: Long = System.currentTimeMillis(),
    val synced: Boolean = false
)

/**
 * ClassEntity - Room entity para armazenar turmas localmente
 */
@Entity(tableName = "classes", indices = [Index("name")])
data class ClassEntity(
    @PrimaryKey val id: String = "",
    val name: String = "",
    val grade: String = "",
    val year: String = "",
    val isSynced: Boolean = false,
    val lastModified: Long = System.currentTimeMillis()
)
