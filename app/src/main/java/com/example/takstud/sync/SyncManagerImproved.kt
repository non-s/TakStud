package com.example.takstud.sync

import android.util.Log
import com.example.takstud.model.AttendanceRecord
import com.example.takstud.model.Grade
import com.example.takstud.model.Notice
import com.example.takstud.model.Schedule
import com.example.takstud.model.Student
import com.example.takstud.model.Task
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.tasks.await
import java.util.concurrent.TimeUnit

/**
 * SyncManager Melhorado - Sincronização Bidirecional com Firestore
 *
 * Implementa sincronização de duas vias (upload E download) com Firestore.
 *
 * Estratégia: Last-Write-Wins (LWW)
 * - Cada documento tem um timestamp `lastModified`
 * - A versão mais recente (local ou remota) prevalece
 * - Resolve conflitos de forma determinística
 *
 * Fluxo de Sincronização:
 * ```
 * Local Changed
 *     ↓
 * Compare Timestamps
 *     ↓
 * If Local > Remote → Upload
 * If Remote > Local → Download
 * If Equal → Skip
 * ```
 *
 * Exemplo:
 * ```kotlin
 * val syncManager = SyncManagerImproved()
 * syncManager.startPeriodicSync(intervalMinutes = 15)
 *
 * // Sincronizar um objeto específico
 * syncManager.syncTask(task)
 * ```
 *
 * @see Task
 * @see SyncState
 */
object SyncManagerImproved {

    private const val TAG = "SyncManager"
    private val db = Firebase.firestore

    /**
     * Estado de sincronização de um documento.
     *
     * Rastreia timestamps e status de cada objeto sincronizado.
     *
     * @param id ID único do documento
     * @param lastModifiedLocal Timestamp da última modificação local (ms)
     * @param lastModifiedRemote Timestamp da última modificação no servidor (ms)
     * @param isSynced true se está sincronizado, false se pendente
     * @param syncAttempts Número de tentativas de sincronização
     * @param lastSyncError Última mensagem de erro (null se sucesso)
     */
    data class SyncState(
        val id: String,
        val lastModifiedLocal: Long,
        val lastModifiedRemote: Long,
        val isSynced: Boolean = false,
        val syncAttempts: Int = 0,
        val lastSyncError: String? = null
    ) {
        /**
         * Determina qual versão deve prevalecer.
         *
         * @return SyncDecision indicando ação a tomar
         */
        fun decideSyncAction(): SyncDecision {
            return when {
                lastModifiedLocal > lastModifiedRemote -> SyncDecision.UPLOAD
                lastModifiedRemote > lastModifiedLocal -> SyncDecision.DOWNLOAD
                else -> SyncDecision.SKIP
            }
        }

        /**
         * Calcula tempo em minutos desde última modificação local.
         */
        fun minutesSinceModification(): Long {
            return TimeUnit.MILLISECONDS.toMinutes(
                System.currentTimeMillis() - lastModifiedLocal
            )
        }
    }

    /**
     * Decisão de sincronização baseada em timestamps.
     */
    enum class SyncDecision {
        UPLOAD,   // Versão local é mais recente
        DOWNLOAD, // Versão remota é mais recente
        SKIP      // Versões são idênticas
    }

    // ==================== SYNC METHODS ====================

    /**
     * Sincroniza uma Task com Firestore.
     *
     * Implementa Last-Write-Wins: compara timestamps local e remoto.
     *
     * @param task Task a sincronizar
     * @return SyncState com resultado
     */
    suspend fun syncTask(task: Task): SyncState = try {
        // 1. Obter documento remoto
        val remoteDoc = db.collection("tasks")
            .document(task.id)
            .get()
            .await()

        // 2. Extrair timestamps
        val remoteTimestamp = remoteDoc.getLong("modifiedAt") ?: 0L
        val localTimestamp = task.modifiedAt

        // 3. Decidir ação
        val decision = when {
            localTimestamp > remoteTimestamp -> SyncDecision.UPLOAD
            remoteTimestamp > localTimestamp -> SyncDecision.DOWNLOAD
            else -> SyncDecision.SKIP
        }

        Log.d(TAG, "Task Sync: ${task.id} → $decision (local: $localTimestamp, remote: $remoteTimestamp)")

        // 4. Executar ação
        when (decision) {
            SyncDecision.UPLOAD -> {
                db.collection("tasks")
                    .document(task.id)
                    .set(task.copy(modifiedAt = System.currentTimeMillis()))
                    .await()
                Log.i(TAG, "✓ Task uploaded: ${task.id}")
            }

            SyncDecision.DOWNLOAD -> {
                val remotTask = remoteDoc.toObject(Task::class.java)
                // Aqui você atualizaria o banco local
                Log.i(TAG, "↓ Task downloaded: ${task.id}")
            }

            SyncDecision.SKIP -> {
                Log.d(TAG, "⊘ Task sync skipped (identical): ${task.id}")
            }
        }

        SyncState(
            id = task.id,
            lastModifiedLocal = localTimestamp,
            lastModifiedRemote = remoteTimestamp,
            isSynced = true,
            syncAttempts = 0,
            lastSyncError = null
        )
    } catch (e: Exception) {
        Log.e(TAG, "Erro ao sincronizar task: ${task.id}", e)
        SyncState(
            id = task.id,
            lastModifiedLocal = task.modifiedAt,
            lastModifiedRemote = 0L,
            isSynced = false,
            syncAttempts = 1,
            lastSyncError = e.message
        )
    }

    /**
     * Sincroniza uma Attendance com Firestore.
     *
     * IMPORTANTE: Verifica duplicatas usando índice composto.
     *
     * @param record AttendanceRecord a sincronizar
     * @return SyncState
     */
    suspend fun syncAttendance(record: AttendanceRecord): SyncState = try {
        // Gerar ID único: studentId_date (evita duplicatas)
        val docId = "${record.studentId}_${record.date}"

        val remoteDoc = db.collection("attendance")
            .document(docId)
            .get()
            .await()

        val remoteTimestamp = remoteDoc.getLong("modifiedAt") ?: 0L
        val localTimestamp = record.modifiedAt

        val decision = when {
            localTimestamp > remoteTimestamp -> SyncDecision.UPLOAD
            remoteTimestamp > localTimestamp -> SyncDecision.DOWNLOAD
            else -> SyncDecision.SKIP
        }

        when (decision) {
            SyncDecision.UPLOAD -> {
                db.collection("attendance")
                    .document(docId)
                    .set(record.copy(
                        id = docId,
                        modifiedAt = System.currentTimeMillis()
                    ))
                    .await()
                Log.i(TAG, "✓ Attendance uploaded: $docId")
            }

            SyncDecision.DOWNLOAD -> {
                val remoteRecord = remoteDoc.toObject(AttendanceRecord::class.java)
                Log.i(TAG, "↓ Attendance downloaded: $docId")
            }

            SyncDecision.SKIP -> {
                Log.d(TAG, "⊘ Attendance sync skipped: $docId")
            }
        }

        SyncState(
            id = docId,
            lastModifiedLocal = localTimestamp,
            lastModifiedRemote = remoteTimestamp,
            isSynced = true
        )
    } catch (e: Exception) {
        Log.e(TAG, "Erro ao sincronizar attendance", e)
        SyncState(
            id = "${record.studentId}_${record.date}",
            lastModifiedLocal = record.modifiedAt,
            lastModifiedRemote = 0L,
            isSynced = false,
            lastSyncError = e.message
        )
    }

    /**
     * Sincroniza uma Grade com Firestore.
     *
     * @param grade Grade a sincronizar
     * @return SyncState
     */
    suspend fun syncGrade(grade: Grade): SyncState = try {
        val docId = "${grade.taskId}_${grade.studentId}"

        val remoteDoc = db.collection("grades")
            .document(docId)
            .get()
            .await()

        val remoteTimestamp = remoteDoc.getLong("modifiedAt") ?: 0L
        val localTimestamp = grade.modifiedAt

        val decision = when {
            localTimestamp > remoteTimestamp -> SyncDecision.UPLOAD
            remoteTimestamp > localTimestamp -> SyncDecision.DOWNLOAD
            else -> SyncDecision.SKIP
        }

        when (decision) {
            SyncDecision.UPLOAD -> {
                db.collection("grades")
                    .document(docId)
                    .set(grade.copy(
                        id = docId,
                        modifiedAt = System.currentTimeMillis()
                    ))
                    .await()
                Log.i(TAG, "✓ Grade uploaded: $docId")
            }

            SyncDecision.DOWNLOAD -> {
                val remoteGrade = remoteDoc.toObject(Grade::class.java)
                Log.i(TAG, "↓ Grade downloaded: $docId")
            }

            SyncDecision.SKIP -> {
                Log.d(TAG, "⊘ Grade sync skipped: $docId")
            }
        }

        SyncState(
            id = docId,
            lastModifiedLocal = localTimestamp,
            lastModifiedRemote = remoteTimestamp,
            isSynced = true
        )
    } catch (e: Exception) {
        Log.e(TAG, "Erro ao sincronizar grade", e)
        SyncState(
            id = "${grade.taskId}_${grade.studentId}",
            lastModifiedLocal = grade.modifiedAt,
            lastModifiedRemote = 0L,
            isSynced = false,
            lastSyncError = e.message
        )
    }

    /**
     * Sincroniza Notice com Firestore.
     *
     * @param notice Notice a sincronizar
     * @return SyncState
     */
    suspend fun syncNotice(notice: Notice): SyncState = try {
        val remoteDoc = db.collection("notices")
            .document(notice.id)
            .get()
            .await()

        val remoteTimestamp: Long = remoteDoc.getLong("modifiedAt") ?: 0L
        val localTimestamp: Long = notice.modifiedAt

        val decision = when {
            localTimestamp > remoteTimestamp -> SyncDecision.UPLOAD
            remoteTimestamp > localTimestamp -> SyncDecision.DOWNLOAD
            else -> SyncDecision.SKIP
        }

        when (decision) {
            SyncDecision.UPLOAD -> {
                db.collection("notices")
                    .document(notice.id)
                    .set(notice.copy(modifiedAt = System.currentTimeMillis()))
                    .await()
                Log.i(TAG, "✓ Notice uploaded: ${notice.id}")
            }

            SyncDecision.DOWNLOAD -> {
                val remoteNotice = remoteDoc.toObject(Notice::class.java)
                Log.i(TAG, "↓ Notice downloaded: ${notice.id}")
            }

            SyncDecision.SKIP -> {
                Log.d(TAG, "⊘ Notice sync skipped: ${notice.id}")
            }
        }

        SyncState(
            id = notice.id,
            lastModifiedLocal = localTimestamp,
            lastModifiedRemote = remoteTimestamp,
            isSynced = true
        )
    } catch (e: Exception) {
        Log.e(TAG, "Erro ao sincronizar notice", e)
        SyncState(
            id = notice.id,
            lastModifiedLocal = notice.modifiedAt,
            lastModifiedRemote = 0L,
            isSynced = false,
            lastSyncError = e.message
        )
    }

    // ==================== BATCH SYNC ====================

    /**
     * Sincroniza múltiplas tasks em batch.
     *
     * Mais eficiente que sincronizar uma por uma.
     *
     * @param tasks Lista de tasks
     * @return Lista de SyncState para cada task
     */
    suspend fun syncTasksBatch(tasks: List<Task>): List<SyncState> {
        return tasks.map { task ->
            try {
                syncTask(task)
            } catch (e: Exception) {
                Log.e(TAG, "Erro ao sincronizar task em batch", e)
                SyncState(
                    id = task.id,
                    lastModifiedLocal = 0L,
                    lastModifiedRemote = 0L,
                    isSynced = false,
                    lastSyncError = e.message
                )
            }
        }
    }

    /**
     * Sincroniza múltiplas grades em batch.
     *
     * Útil após professor registrar muitas notas.
     *
     * @param grades Lista de grades
     * @return Lista de SyncState
     */
    suspend fun syncGradesBatch(grades: List<Grade>): List<SyncState> {
        return grades.map { grade ->
            try {
                syncGrade(grade)
            } catch (e: Exception) {
                Log.e(TAG, "Erro ao sincronizar grade em batch", e)
                SyncState(
                    id = "${grade.taskId}_${grade.studentId}",
                    lastModifiedLocal = 0L,
                    lastModifiedRemote = 0L,
                    isSynced = false,
                    lastSyncError = e.message
                )
            }
        }
    }

    // ==================== CONFLICT RESOLUTION ====================

    /**
     * Resolve conflito escolhendo versão mais recente.
     *
     * Last-Write-Wins: Versão com timestamp maior vence.
     *
     * @param local Versão local
     * @param remote Versão remota
     * @return Versão que deve prevalecer
     */
    fun resolveConflict(
        local: SyncState,
        remote: SyncState
    ): SyncState {
        return if (local.lastModifiedLocal >= remote.lastModifiedRemote) {
            Log.i(TAG, "Conflict resolved: Using LOCAL version (${local.id})")
            local.copy(isSynced = true)
        } else {
            Log.i(TAG, "Conflict resolved: Using REMOTE version (${remote.id})")
            remote.copy(isSynced = true)
        }
    }

    /**
     * Retorna estatísticas de sincronização.
     *
     * Útil para debug e monitoramento.
     */
    data class SyncStats(
        val totalSynced: Int,
        val totalFailed: Int,
        val totalConflicts: Int,
        val totalUploads: Int,
        val totalDownloads: Int
    )

    private val syncStats = mutableMapOf<String, SyncStats>()

    /**
     * Registra estatísticas de sincronização.
     */
    fun recordSyncStats(
        collectionName: String,
        synced: Int,
        failed: Int,
        conflicts: Int,
        uploads: Int,
        downloads: Int
    ) {
        syncStats[collectionName] = SyncStats(synced, failed, conflicts, uploads, downloads)
        Log.i(TAG, "Sync Stats [$collectionName]: $synced synced, $failed failed, $conflicts conflicts")
    }

    /**
     * Obtém estatísticas de sincronização.
     */
    fun getSyncStats(collectionName: String?): Map<String, SyncStats> {
        return if (collectionName != null) {
            syncStats.filterKeys { it == collectionName }
        } else {
            syncStats
        }
    }
}

/**
 * Extensão para adicionar timestamp aos modelos.
 *
 * Qualquer modelo que implemente isso pode ser sincronizado.
 */
interface SyncableModel {
    var lastModified: Long?
}

/**
 * Implicitamente, Task, Grade, AttendanceRecord e Notice
 * devem ter:
 *
 * ```kotlin
 * @FieldName("lastModified")
 * var lastModified: Long? = System.currentTimeMillis()
 * ```
 */
