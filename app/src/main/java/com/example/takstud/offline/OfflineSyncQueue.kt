package com.example.takstud.offline

import android.util.Log
import com.example.takstud.model.*
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.coroutines.tasks.await

/**
 * Interface para Fila de Sincronização Offline.
 *
 * Define contrato para gerenciar fila de operações offline que serão sincronizadas
 * com Firestore quando aplicação voltar online.
 *
 * Funcionalidades:
 * - Persistência de operações enquanto offline
 * - Sincronização automática ao reconectar
 * - Retry com backoff exponencial
 * - Deduplicação de operações (evita duplicatas)
 * - Priorização crítico vs normal vs baixo
 *
 * Fluxo:
 * ```
 * Usuário faz operação (offline)
 *      ↓
 * Operação adicionada à fila (enqueueOperation)
 *      ↓
 * Persiste em Room/SQLite
 *      ↓
 * App detecta internet (ConnectivityMonitor)
 *      ↓
 * SyncWorker processa fila (processSyncQueue)
 *      ↓
 * Operações sincronizadas com Firestore
 *      ↓
 * Se sucesso: remove da fila
 * Se erro: manter para retry
 * ```
 *
 * Implementações:
 * - OfflineSyncQueueImpl: Implementação completa com WorkManager
 *
 * @see OfflineSyncQueueImpl
 * @see ConnectivityMonitor
 * @see SyncWorker
 */
interface OfflineSyncQueue {

    /**
     * Tipos de operação para sincronização.
     */
    enum class OperationType {
        CREATE,    // Novo documento
        UPDATE,    // Modifica documento existente
        DELETE     // Remove documento
    }

    /**
     * Níveis de prioridade para sincronização.
     */
    enum class Priority {
        CRITICAL,  // Grades, frequência (dados críticos - sincronizar primeiro)
        NORMAL,    // Tasks, notices (dados normais)
        LOW        // Metadados (dados que podem ser recalculados)
    }

    /**
     * Estados possíveis de uma operação na fila.
     */
    enum class SyncStatus {
        PENDING,   // Aguardando sincronização
        SYNCING,   // Em processo de sincronização
        SYNCED,    // Sincronizado com sucesso
        FAILED     // Falhou após max attempts
    }

    /**
     * Operação que será sincronizada com Firestore.
     */
    data class QueuedOperation(
        val id: String = "",
        val type: OperationType,
        val collection: String,
        val documentId: String,
        val data: Map<String, Any>,
        val priority: Priority = Priority.NORMAL,
        val status: SyncStatus = SyncStatus.PENDING,
        val attemptCount: Int = 0,
        val maxAttempts: Int = 3,
        val createdAt: Long = System.currentTimeMillis(),
        val lastAttemptAt: Long = 0,
        val error: String? = null
    )

    /**
     * Estatísticas da fila de sincronização.
     */
    data class QueueStatistics(
        val totalOps: Int,
        val pending: Int,
        val syncing: Int,
        val synced: Int,
        val failed: Int,
        val criticalPending: Int,
        val oldestOp: Long
    ) {
        fun hasWork(): Boolean = pending > 0 || syncing > 0
        fun hasCriticalWork(): Boolean = criticalPending > 0
        fun isEmpty(): Boolean = totalOps == 0
    }

    /**
     * Resultado do processamento da fila.
     */
    data class SyncQueueResult(
        var synced: Int = 0,
        var failed: Int = 0,
        var skipped: Int = 0
    )

    // ============== ADICIONAR PARA FILA ==============

    /**
     * Adiciona nova operação à fila de sincronização.
     */
    suspend fun enqueueOperation(
        type: OperationType,
        collection: String,
        documentId: String,
        data: Map<String, Any>,
        priority: Priority = Priority.NORMAL
    ): QueuedOperation

    /**
     * Enqueue helper para tarefa.
     */
    suspend fun enqueueTaskCreate(task: Task)

    /**
     * Enqueue helper para nota.
     */
    suspend fun enqueueGradeUpdate(grade: Grade)

    /**
     * Enqueue helper para presença.
     */
    suspend fun enqueueAttendanceCreate(record: AttendanceRecord)

    // ============== PROCESSAR FILA ==============

    /**
     * Processa a fila de sincronização.
     * Chamado quando app detecta conexão com internet.
     */
    suspend fun processSyncQueue(
        queuedOps: List<QueuedOperation>
    ): SyncQueueResult

    /**
     * Remove operação da fila após sucesso.
     */
    suspend fun removeFromQueue(operationId: String)

    /**
     * Limpa operações antigas com sucesso.
     */
    suspend fun cleanupOldSuccessfulOps(daysOld: Int = 7)

    // ============== STATUS E MONITORAMENTO ==============

    /**
     * Obtém estatísticas da fila.
     */
    suspend fun getQueueStats(ops: List<QueuedOperation>): QueueStatistics

    /**
     * Detecta e remove operações duplicadas na fila.
     */
    suspend fun deduplicateQueue(ops: List<QueuedOperation>): List<QueuedOperation>
}
