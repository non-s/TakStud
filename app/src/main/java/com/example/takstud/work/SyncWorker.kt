package com.example.takstud.work

import android.content.Context
import androidx.work.*
import com.example.takstud.data.local.AppDatabase
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import java.util.concurrent.TimeUnit

/**
 * SyncWorker - Background worker para sincronizar dados offline com Firestore
 * Executado periodicamente quando dispositivo tem conectividade
 */
class SyncWorker(
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {

    private val db = AppDatabase.getInstance(context)
    private val firestore = FirebaseFirestore.getInstance()

    override suspend fun doWork(): Result {
        return try {
            // Sincronizar cada coleção
            // TODO: Implementar syncTasks() quando TaskDao tiver métodos de sync
            // syncTasks()
            syncNotices()
            syncSchedules()
            // TODO: Implementar syncStudents() quando StudentDao tiver métodos de sync
            // syncStudents()
            syncGrades()
            syncAttendance()

            // Limpar itens sincronizados da fila
            db.syncQueueDao().clearSyncedItems()

            Result.success()
        } catch (e: Exception) {
            // Retry com backoff exponencial
            if (runAttemptCount < 3) {
                Result.retry()
            } else {
                Result.failure()
            }
        }
    }

    // TODO: Implementar quando TaskEntity tiver campo isSynced e TaskDao tiver métodos de sync
    /*
    private suspend fun syncTasks() {
        val unsynced = db.taskDao().getUnsyncedTasks()
        unsynced.forEach { taskEntity ->
            firestore.collection("tasks").document(taskEntity.id)
                .set(mapOf(
                    "title" to taskEntity.title,
                    "studentClass" to taskEntity.studentClass,
                    "dueDate" to taskEntity.dueDate,
                    "description" to taskEntity.description,
                    "createdAt" to taskEntity.createdAt
                ))
                .await()
        }
        if (unsynced.isNotEmpty()) {
            db.taskDao().markAsSynced(unsynced.map { it.id })
        }
    }
    */

    private suspend fun syncNotices() {
        val unsynced = db.noticeDao().getUnsyncedNotices()
        unsynced.forEach { noticeEntity ->
            firestore.collection("notices").document(noticeEntity.id)
                .set(mapOf(
                    "title" to noticeEntity.title,
                    "content" to noticeEntity.content,
                    "studentClass" to noticeEntity.studentClass,
                    "createdAt" to noticeEntity.createdAt
                ))
                .await()
        }
        if (unsynced.isNotEmpty()) {
            db.noticeDao().markAsSynced(unsynced.map { it.id })
        }
    }

    private suspend fun syncSchedules() {
        val unsynced = db.scheduleDao().getUnsyncedSchedules()
        unsynced.forEach { scheduleEntity ->
            firestore.collection("schedules").document(scheduleEntity.id)
                .set(mapOf(
                    "classCode" to scheduleEntity.classCode,
                    "studentClass" to scheduleEntity.studentClass,
                    "period" to scheduleEntity.period
                ))
                .await()
        }
        if (unsynced.isNotEmpty()) {
            db.scheduleDao().markAsSynced(unsynced.map { it.id })
        }
    }

    // TODO: Implementar quando StudentEntity tiver campo isSynced e StudentDao tiver métodos de sync
    /*
    private suspend fun syncStudents() {
        val unsynced = db.studentDao().getUnsyncedStudents()
        unsynced.forEach { studentEntity ->
            firestore.collection("students").document(studentEntity.id)
                .set(mapOf(
                    "name" to studentEntity.name,
                    "ra" to studentEntity.ra,
                    "studentClass" to studentEntity.studentClass
                ))
                .await()
        }
        if (unsynced.isNotEmpty()) {
            db.studentDao().markAsSynced(unsynced.map { it.id })
        }
    }
    */

    private suspend fun syncGrades() {
        val unsynced = db.gradeDao().getUnsyncedGrades()
        unsynced.forEach { gradeEntity ->
            firestore.collection("grades").document(gradeEntity.id)
                .set(mapOf(
                    "taskId" to gradeEntity.taskId,
                    "studentId" to gradeEntity.studentId,
                    "score" to gradeEntity.score,
                    "timestamp" to gradeEntity.timestamp
                ))
                .await()
        }
        if (unsynced.isNotEmpty()) {
            db.gradeDao().markAsSynced(unsynced.map { it.id })
        }
    }

    private suspend fun syncAttendance() {
        val unsynced = db.attendanceDao().getUnsyncedAttendance()
        unsynced.forEach { attendanceEntity ->
            firestore.collection("attendance").document(attendanceEntity.id)
                .set(mapOf(
                    "studentId" to attendanceEntity.studentId,
                    "studentClass" to attendanceEntity.studentClass,
                    "date" to attendanceEntity.date,
                    "isPresent" to attendanceEntity.isPresent,
                    "timestamp" to attendanceEntity.timestamp
                ))
                .await()
        }
        if (unsynced.isNotEmpty()) {
            db.attendanceDao().markAsSynced(unsynced.map { it.id })
        }
    }

    companion object {
        const val SYNC_WORK_TAG = "sync_work"

        /**
         * Agendar sincronização periódica
         * Executa a cada 15 minutos quando dispositivo tiver conectividade
         */
        fun scheduleSyncWork(context: Context) {
            val syncRequest = PeriodicWorkRequestBuilder<SyncWorker>(
                15, TimeUnit.MINUTES
            )
                .setConstraints(
                    Constraints.Builder()
                        .setRequiredNetworkType(NetworkType.CONNECTED)
                        .build()
                )
                .addTag(SYNC_WORK_TAG)
                .build()

            WorkManager.getInstance(context).enqueueUniquePeriodicWork(
                "sync_database",
                ExistingPeriodicWorkPolicy.KEEP,
                syncRequest
            )
        }

        /**
         * Cancelar sincronização periódica
         */
        fun cancelSyncWork(context: Context) {
            WorkManager.getInstance(context).cancelAllWorkByTag(SYNC_WORK_TAG)
        }
    }
}
