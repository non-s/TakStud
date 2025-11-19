package com.example.takstud.offline

import android.util.Log
import com.example.takstud.model.AttendanceRecord
import com.example.takstud.model.Grade
import com.example.takstud.model.Task

/**
 * Gerenciador de fila de sincronização para Offline Mode - Implementação Stub
 *
 * Esta é uma implementação simplificada que satisfaz a interface OfflineSyncQueue
 * para permitir compilação. Para uma implementação completa com persistência Room,
 * estenda este stub com os DAOs e lógica necessária.
 */
class OfflineSyncQueueImpl : OfflineSyncQueue {

    companion object {
        private const val TAG = "OfflineSyncQueue"
    }

    override suspend fun enqueueOperation(
        type: OfflineSyncQueue.OperationType,
        collection: String,
        documentId: String,
        data: Map<String, Any>,
        priority: OfflineSyncQueue.Priority
    ): OfflineSyncQueue.QueuedOperation {
        Log.d(TAG, "enqueueOperation: $type for $collection/$documentId")
        return OfflineSyncQueue.QueuedOperation(
            type = type,
            collection = collection,
            documentId = documentId,
            data = data,
            priority = priority
        )
    }

    override suspend fun enqueueTaskCreate(task: Task) {
        Log.d(TAG, "enqueueTaskCreate: ${task.id}")
    }

    override suspend fun enqueueGradeUpdate(grade: Grade) {
        Log.d(TAG, "enqueueGradeUpdate: ${grade.id}")
    }

    override suspend fun enqueueAttendanceCreate(record: AttendanceRecord) {
        Log.d(TAG, "enqueueAttendanceCreate: ${record.id}")
    }

    override suspend fun processSyncQueue(
        queuedOps: List<OfflineSyncQueue.QueuedOperation>
    ): OfflineSyncQueue.SyncQueueResult {
        Log.d(TAG, "processSyncQueue called with ${queuedOps.size} operations")
        return OfflineSyncQueue.SyncQueueResult(
            synced = queuedOps.size,
            failed = 0
        )
    }

    override suspend fun removeFromQueue(operationId: String) {
        Log.d(TAG, "removeFromQueue: $operationId")
    }

    override suspend fun cleanupOldSuccessfulOps(daysOld: Int) {
        Log.d(TAG, "cleanupOldSuccessfulOps: $daysOld days")
    }

    override suspend fun getQueueStats(ops: List<OfflineSyncQueue.QueuedOperation>): OfflineSyncQueue.QueueStatistics {
        return OfflineSyncQueue.QueueStatistics(
            totalOps = ops.size,
            pending = ops.count { it.status == OfflineSyncQueue.SyncStatus.PENDING },
            syncing = ops.count { it.status == OfflineSyncQueue.SyncStatus.SYNCING },
            synced = ops.count { it.status == OfflineSyncQueue.SyncStatus.SYNCED },
            failed = ops.count { it.status == OfflineSyncQueue.SyncStatus.FAILED },
            criticalPending = ops.count {
                it.status == OfflineSyncQueue.SyncStatus.PENDING &&
                it.priority == OfflineSyncQueue.Priority.CRITICAL
            },
            oldestOp = ops.minOfOrNull { it.createdAt } ?: 0L
        )
    }

    override suspend fun deduplicateQueue(ops: List<OfflineSyncQueue.QueuedOperation>): List<OfflineSyncQueue.QueuedOperation> {
        Log.d(TAG, "deduplicateQueue: ${ops.size} operations")
        return ops.distinctBy { "${it.collection}/${it.documentId}" }
    }
}
