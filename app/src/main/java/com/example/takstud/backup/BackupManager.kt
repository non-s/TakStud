package com.example.takstud.backup

import android.content.Context
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.tasks.await
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

/**
 * BackupManager - Gerencia backup e restauração de dados
 * Permite ao usuário fazer backup de seus dados na nuvem
 */
class BackupManager(
    private val context: Context,
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance(),
    private val storage: FirebaseStorage = FirebaseStorage.getInstance()
) {

    /**
     * Fazer backup de todos os dados do usuário
     */
    suspend fun createBackup(userId: String): Result<String> {
        return try {
            val timestamp = LocalDateTime.now()
                .format(DateTimeFormatter.ofPattern("yyyy-MM-dd_HH-mm-ss"))
            val backupName = "backup_${userId}_$timestamp"

            // Coletar dados de todas as coleções
            val tasksSnapshot = firestore.collection("tasks").get().await()
            val noticesSnapshot = firestore.collection("notices").get().await()
            val schedulesSnapshot = firestore.collection("schedules").get().await()
            val studentsSnapshot = firestore.collection("students").get().await()
            val gradesSnapshot = firestore.collection("grades").get().await()
            val attendanceSnapshot = firestore.collection("attendance").get().await()

            // Criar objeto de backup
            val backupData = mapOf(
                "userId" to userId,
                "timestamp" to System.currentTimeMillis(),
                "data" to mapOf(
                    "tasks" to tasksSnapshot.documents.map { it.data },
                    "notices" to noticesSnapshot.documents.map { it.data },
                    "schedules" to schedulesSnapshot.documents.map { it.data },
                    "students" to studentsSnapshot.documents.map { it.data },
                    "grades" to gradesSnapshot.documents.map { it.data },
                    "attendance" to attendanceSnapshot.documents.map { it.data }
                )
            )

            // Salvar backup em Firestore
            firestore.collection("backups")
                .document(backupName)
                .set(backupData)
                .await()

            Result.success(backupName)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Obter lista de backups disponíveis
     */
    suspend fun listAvailableBackups(userId: String): Result<List<BackupInfo>> {
        return try {
            val snapshot = firestore.collection("backups")
                .whereEqualTo("userId", userId)
                .orderBy("timestamp", com.google.firebase.firestore.Query.Direction.DESCENDING)
                .get()
                .await()

            val backups = snapshot.documents.mapNotNull { doc ->
                val data = doc.data ?: return@mapNotNull null
                val timestamp = data["timestamp"] as? Long ?: 0L
                BackupInfo(
                    name = doc.id,
                    createdAt = timestamp,
                    size = estimateBackupSize(doc.data)
                )
            }

            Result.success(backups)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Restaurar backup para o dispositivo
     */
    suspend fun restoreBackup(backupName: String): Result<Boolean> {
        return try {
            val backupDoc = firestore.collection("backups")
                .document(backupName)
                .get()
                .await()

            @Suppress("UNCHECKED_CAST")
            val backupData = (backupDoc.data?.get("data") as? Map<String, Any>)
                ?: return Result.failure(Exception("Formato de backup inválido"))

            // Restaurar cada coleção
            val batch = firestore.batch()

            // Restaurar tarefas
            (backupData["tasks"] as? List<*>)?.forEach { task ->
                if (task is Map<*, *>) {
                    val taskId = (task["id"] as? String) ?: return@forEach
                    batch.set(firestore.collection("tasks").document(taskId), task)
                }
            }

            // Restaurar avisos
            (backupData["notices"] as? List<*>)?.forEach { notice ->
                if (notice is Map<*, *>) {
                    val noticeId = (notice["id"] as? String) ?: return@forEach
                    batch.set(firestore.collection("notices").document(noticeId), notice)
                }
            }

            // Restaurar horários
            (backupData["schedules"] as? List<*>)?.forEach { schedule ->
                if (schedule is Map<*, *>) {
                    val scheduleId = (schedule["id"] as? String) ?: return@forEach
                    batch.set(firestore.collection("schedules").document(scheduleId), schedule)
                }
            }

            // Restaurar alunos
            (backupData["students"] as? List<*>)?.forEach { student ->
                if (student is Map<*, *>) {
                    val studentId = (student["id"] as? String) ?: return@forEach
                    batch.set(firestore.collection("students").document(studentId), student)
                }
            }

            // Restaurar notas
            (backupData["grades"] as? List<*>)?.forEach { grade ->
                if (grade is Map<*, *>) {
                    val gradeId = (grade["id"] as? String) ?: return@forEach
                    batch.set(firestore.collection("grades").document(gradeId), grade)
                }
            }

            // Restaurar presença
            (backupData["attendance"] as? List<*>)?.forEach { attendance ->
                if (attendance is Map<*, *>) {
                    val attendanceId = (attendance["id"] as? String) ?: return@forEach
                    batch.set(firestore.collection("attendance").document(attendanceId), attendance)
                }
            }

            batch.commit().await()
            Result.success(true)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Deletar um backup
     */
    suspend fun deleteBackup(backupName: String): Result<Boolean> {
        return try {
            firestore.collection("backups")
                .document(backupName)
                .delete()
                .await()
            Result.success(true)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Agendar backup automático
     * Executa periodicamente em background
     */
    suspend fun scheduleAutomaticBackups(userId: String): Result<Boolean> {
        return try {
            // Usar WorkManager para agendar
            // Backup automático a cada 7 dias
            androidx.work.WorkManager.getInstance(context)
                .enqueueUniquePeriodicWork(
                    "automatic_backup",
                    androidx.work.ExistingPeriodicWorkPolicy.KEEP,
                    androidx.work.PeriodicWorkRequestBuilder<BackupWorker>(
                        7, java.util.concurrent.TimeUnit.DAYS
                    )
                        .setConstraints(
                            androidx.work.Constraints.Builder()
                                .setRequiredNetworkType(androidx.work.NetworkType.CONNECTED)
                                .build()
                        )
                        .build()
                )
            Result.success(true)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Estimar tamanho do backup
     */
    private fun estimateBackupSize(data: Any?): Long {
        return when (data) {
            is String -> data.length.toLong()
            is Number -> 8
            is Map<*, *> -> data.values.sumOf { estimateBackupSize(it) }
            is List<*> -> data.sumOf { estimateBackupSize(it) }
            else -> 0L
        }
    }
}

/**
 * BackupInfo - Informações sobre um backup
 */
data class BackupInfo(
    val name: String,
    val createdAt: Long,
    val size: Long
) {
    fun getFormattedDate(): String {
        val date = java.time.Instant.ofEpochMilli(createdAt)
            .atZone(java.time.ZoneId.systemDefault())
            .toLocalDate()
        val formatter = java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")
        return date.atStartOfDay().format(formatter)
    }

    fun getFormattedSize(): String {
        return when {
            size < 1024 -> "$size B"
            size < 1024 * 1024 -> "${size / 1024} KB"
            else -> "${size / (1024 * 1024)} MB"
        }
    }
}

/**
 * BackupWorker - Worker para backups automáticos
 */
class BackupWorker(context: android.content.Context, params: androidx.work.WorkerParameters) :
    androidx.work.CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        return try {
            val backupManager = BackupManager(applicationContext)
            // TODO: Obter userId do SessionStorage
            val userId = "default_user" // Placeholder
            backupManager.createBackup(userId).getOrNull()
            Result.success()
        } catch (e: Exception) {
            if (runAttemptCount < 3) Result.retry() else Result.failure()
        }
    }
}
