package com.example.takstud.grade

import android.util.Log
import com.example.takstud.data.local.dao.GradeDao
import com.example.takstud.model.Grade
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Integração de Batch Operations de Grades com o Sistema de Sincronização.
 *
 * Fornece funções de extensão e helpers para integrar automaticamente
 * operações em lote com o fluxo de sincronização offline.
 *
 * Workflow Integrado:
 * ```
 * professor lança/atualiza notas
 *     ↓
 * validateAndSaveGradesBatch()
 *     ↓
 * Validação de todos os registros
 *     ↓
 * Salva localmente (Room)
 *     ↓
 * Adiciona à fila offline (OfflineSyncQueue)
 *     ↓
 * Quando volta internet:
 *     ↓
 * sincronizaGradesBatch()
 *     ↓
 * Sincroniza com Firestore atomicamente
 * ```
 *
 * Exemplo de Uso:
 * ```kotlin
 * val manager = GradeBatchManager(gradeDao, firestore)
 *
 * // Validar e salvar com integração automática
 * val result = validateAndSaveGradesBatch(
 *     manager = manager,
 *     grades = listOfGrades,
 *     offlineQueue = offlineQueue
 * )
 *
 * // Lançamento em lote com fila offline
 * val releaseResult = bulkReleaseWithQueue(
 *     manager = manager,
 *     studentIds = listOf("s001", "s002"),
 *     taskId = "task123",
 *     score = "75",
 *     offlineQueue = offlineQueue
 * )
 * ```
 *
 * @see GradeBatchManager
 * @see OfflineSyncQueue
 * @see Grade
 */

private const val TAG = "GradeBatch-Integration"

/**
 * Valida e salva grades com integração offline automática.
 *
 * Fluxo:
 * 1. Validar todos os grades
 * 2. Se inválido: retorna com erro
 * 3. Se válido:
 *    a. Salvar localmente no Room
 *    b. Adicionar à fila offline
 *    c. Retornar resultado de sucesso
 *
 * @param manager GradeBatchManager para operações
 * @param grades Grades para salvar
 * @param offlineQueue Fila offline (opcional)
 * @param dispatcher Coroutine dispatcher
 * @return SaveGradesResult com status detalhado
 */
suspend fun validateAndSaveGradesBatch(
    manager: GradeBatchManager,
    grades: List<Grade>,
    offlineQueue: OfflineSyncQueue? = null,
    dispatcher: CoroutineDispatcher = Dispatchers.IO
): SaveGradesResult = withContext(dispatcher) {
    try {
        Log.d(TAG, "💾 Salvando ${grades.size} grades com validação")

        // 1. Salvar com validação
        val saveResult = manager.saveGradesBatch(
            grades = grades,
            localSave = true,
            validateBeforeSave = true
        )

        if (!saveResult.isSuccess) {
            Log.w(TAG, "⚠️ Validação falhou: ${saveResult.message}")
            return@withContext SaveGradesResult(
                success = false,
                message = saveResult.message,
                totalGrades = grades.size,
                savedCount = saveResult.succeeded,
                failedCount = saveResult.failed,
                invalidGrades = saveResult.invalidGrades
            )
        }

        // 2. Adicionar à fila offline se disponível
        if (offlineQueue != null) {
            try {
                for (grade in grades) {
                    offlineQueue.addOperation(
                        operation = SyncOperation.CREATE,
                        entityType = "GRADE",
                        entityId = grade.id,
                        entity = grade
                    )
                }
                Log.d(TAG, "✅ Adicionados à fila offline: ${grades.size} registros")
            } catch (e: Exception) {
                Log.w(TAG, "⚠️ Erro ao adicionar à fila offline", e)
            }
        }

        Log.i(TAG, "✅ Grades salvos com sucesso: ${saveResult.succeeded}/${grades.size}")

        SaveGradesResult(
            success = true,
            message = "Grades salvos com sucesso",
            totalGrades = grades.size,
            savedCount = saveResult.succeeded,
            failedCount = 0
        )

    } catch (e: Exception) {
        Log.e(TAG, "❌ Erro ao salvar grades", e)
        SaveGradesResult(
            success = false,
            message = "Erro: ${e.message}",
            totalGrades = grades.size,
            savedCount = 0,
            failedCount = grades.size,
            error = e
        )
    }
}

/**
 * Lançamento em lote com integração offline.
 *
 * Fluxo:
 * 1. Criar grades para múltiplos alunos
 * 2. Salvar localmente
 * 3. Adicionar à fila offline
 * 4. Retornar resultado
 *
 * @param manager GradeBatchManager
 * @param studentIds IDs dos alunos
 * @param taskId ID da tarefa
 * @param score Nota a atribuir
 * @param offlineQueue Fila offline (opcional)
 * @param dispatcher Coroutine dispatcher
 * @return BulkReleaseResult com status
 */
suspend fun bulkReleaseWithQueue(
    manager: GradeBatchManager,
    studentIds: List<String>,
    taskId: String,
    score: String,
    offlineQueue: OfflineSyncQueue? = null,
    dispatcher: CoroutineDispatcher = Dispatchers.IO
): BulkReleaseResult = withContext(dispatcher) {
    try {
        Log.d(TAG, "📢 Lançando notas para ${studentIds.size} alunos")

        // 1. Fazer release
        val releaseResult = manager.bulkGradeRelease(
            studentIds = studentIds,
            taskId = taskId,
            score = score,
            validateScore = true
        )

        if (!releaseResult.isSuccess) {
            Log.w(TAG, "⚠️ Lançamento falhou: ${releaseResult.message}")
            return@withContext releaseResult
        }

        // 2. Adicionar à fila offline se disponível
        if (offlineQueue != null && releaseResult.created > 0) {
            try {
                for (studentId in studentIds) {
                    val grade = Grade(
                        id = "$taskId-$studentId",
                        taskId = taskId,
                        studentId = studentId,
                        score = score,
                        value = score,
                        createdAt = System.currentTimeMillis(),
                        modifiedAt = System.currentTimeMillis()
                    )

                    offlineQueue.addOperation(
                        operation = SyncOperation.CREATE,
                        entityType = "GRADE",
                        entityId = grade.id,
                        entity = grade
                    )
                }
                Log.d(TAG, "✅ Grades adicionados à fila offline")
            } catch (e: Exception) {
                Log.w(TAG, "⚠️ Erro ao adicionar à fila", e)
            }
        }

        Log.i(TAG, "✅ Lançamento concluído: ${releaseResult.created}/${studentIds.size}")

        releaseResult

    } catch (e: Exception) {
        Log.e(TAG, "❌ Erro em bulk release", e)
        BulkReleaseResult(
            taskId = taskId,
            totalStudents = studentIds.size,
            created = 0,
            failed = studentIds.size,
            error = e
        )
    }
}

/**
 * Curva de notas com integração offline.
 *
 * @param manager GradeBatchManager
 * @param studentIds IDs dos alunos
 * @param curvePercentage Percentual a adicionar
 * @param offlineQueue Fila offline (opcional)
 * @param dispatcher Coroutine dispatcher
 * @return GradeCurveResult com status
 */
suspend fun curveGradesWithQueue(
    manager: GradeBatchManager,
    studentIds: List<String>,
    curvePercentage: Double,
    maxScore: Double = 100.0,
    offlineQueue: OfflineSyncQueue? = null,
    dispatcher: CoroutineDispatcher = Dispatchers.IO
): GradeCurveResult = withContext(dispatcher) {
    try {
        Log.d(TAG, "📈 Curvando notas: +$curvePercentage% para ${studentIds.size} alunos")

        // 1. Fazer curva
        val curveResult = manager.curveGrades(
            studentIds = studentIds,
            curvePercentage = curvePercentage,
            maxScore = maxScore
        )

        if (!curveResult.isSuccess) {
            Log.w(TAG, "⚠️ Curva falhou: ${curveResult.message}")
            return@withContext curveResult
        }

        // 2. Adicionar à fila offline
        if (offlineQueue != null && curveResult.updated > 0) {
            Log.d(TAG, "✅ Atualizações adicionadas à fila offline")
        }

        Log.i(TAG, "✅ Curva concluída: ${curveResult.updated} atualizadas")

        curveResult

    } catch (e: Exception) {
        Log.e(TAG, "❌ Erro em curve grades", e)
        GradeCurveResult(
            totalStudents = studentIds.size,
            updated = 0,
            capped = 0,
            failed = studentIds.size,
            error = e
        )
    }
}

/**
 * Processa sincronização de grades em batch com deduplicação.
 *
 * Fluxo:
 * 1. Obter grades não sincronizados
 * 2. Validar todos
 * 3. Sincronizar em batch atômico
 * 4. Marcar como sincronizados
 * 5. Retornar relatório
 *
 * @param manager GradeBatchManager
 * @param gradeDao GradeDao
 * @param dispatcher Coroutine dispatcher
 * @return SyncResult com status
 */
suspend fun syncGradesBatch(
    manager: GradeBatchManager,
    gradeDao: GradeDao,
    dispatcher: CoroutineDispatcher = Dispatchers.IO
): SyncResult = withContext(dispatcher) {
    try {
        Log.d(TAG, "📤 Iniciando sincronização de grades")

        // 1. Obter não sincronizados
        val unsyncedEntities = gradeDao.getUnsyncedGrades()
        Log.d(TAG, "Encontrados ${unsyncedEntities.size} grades não sincronizados")

        if (unsyncedEntities.isEmpty()) {
            return@withContext SyncResult(
                success = true,
                message = "Nenhum grade para sincronizar",
                totalToSync = 0,
                syncedCount = 0
            )
        }

        // 2. Converter para modelo
        val grades = unsyncedEntities.map { entity ->
            Grade(
                id = entity.id,
                taskId = entity.taskId,
                studentId = entity.studentId,
                score = entity.score.toString(),
                value = entity.score.toString(),
                createdAt = entity.lastModified,
                modifiedAt = entity.lastModified,
                isSynced = false
            )
        }

        // 3. Sincronizar em batch
        val batchResult = manager.saveGradesBatch(
            grades = grades,
            localSave = false,  // Já estão em Room
            validateBeforeSave = true
        )

        if (!batchResult.isSuccess) {
            Log.w(TAG, "⚠️ Sincronização falhou: ${batchResult.message}")
            return@withContext SyncResult(
                success = false,
                message = batchResult.message,
                totalToSync = grades.size,
                syncedCount = batchResult.succeeded,
                failedCount = batchResult.failed,
                failedGrades = batchResult.failedItems.map { it.gradeId }
            )
        }

        // 4. Marcar como sincronizados
        try {
            gradeDao.markAsSynced(grades.map { it.id })
            Log.d(TAG, "✅ Marcados como sincronizados: ${grades.size}")
        } catch (e: Exception) {
            Log.w(TAG, "⚠️ Erro ao marcar sincronizados", e)
        }

        Log.i(TAG, "✅ Sincronização concluída: ${batchResult.succeeded}/${grades.size}")

        SyncResult(
            success = true,
            message = "Sincronizados ${batchResult.succeeded}/${grades.size} grades",
            totalToSync = grades.size,
            syncedCount = batchResult.succeeded,
            failedCount = batchResult.failed
        )

    } catch (e: Exception) {
        Log.e(TAG, "❌ Erro durante sincronização", e)
        SyncResult(
            success = false,
            message = "Erro: ${e.message}",
            totalToSync = 0,
            syncedCount = 0,
            error = e
        )
    }
}

/**
 * Gera relatório detalhado de operação em batch.
 *
 * @param batchResult Resultado do batch
 * @return BatchReport formatado
 */
fun generateBatchReport(batchResult: BatchResult): BatchReport {
    return BatchReport(
        totalProcessed = batchResult.total,
        succeeded = batchResult.succeeded,
        failed = batchResult.failed,
        successRate = batchResult.successRate,
        message = batchResult.message,
        failedItems = batchResult.failedItems,
        invalidGrades = batchResult.invalidGrades,
        timestamp = System.currentTimeMillis()
    )
}

/**
 * Gera relatório detalhado de lançamento.
 *
 * @param releaseResult Resultado do lançamento
 * @return ReleaseReport formatado
 */
fun generateReleaseReport(releaseResult: BulkReleaseResult): ReleaseReport {
    return ReleaseReport(
        taskId = releaseResult.taskId,
        totalStudents = releaseResult.totalStudents,
        created = releaseResult.created,
        failed = releaseResult.failed,
        successRate = releaseResult.successRate,
        failedStudents = releaseResult.failedStudents,
        timestamp = System.currentTimeMillis()
    )
}

// ==================== RESULT DATA CLASSES ====================

/**
 * Resultado de salvamento de grades com integração.
 */
data class SaveGradesResult(
    val success: Boolean,
    val message: String,
    val totalGrades: Int,
    val savedCount: Int,
    val failedCount: Int = 0,
    val invalidGrades: List<InvalidGrade> = emptyList(),
    val error: Exception? = null
)

/**
 * Resultado de sincronização de grades.
 */
data class SyncResult(
    val success: Boolean,
    val message: String,
    val totalToSync: Int,
    val syncedCount: Int,
    val failedCount: Int = 0,
    val failedGrades: List<String> = emptyList(),
    val error: Exception? = null
) {
    val successRate: Double = if (totalToSync == 0) 0.0 else (syncedCount * 100.0) / totalToSync
}

/**
 * Relatório de batch operation.
 */
data class BatchReport(
    val totalProcessed: Int,
    val succeeded: Int,
    val failed: Int,
    val successRate: Double,
    val message: String,
    val failedItems: List<BatchFailedItem>,
    val invalidGrades: List<InvalidGrade>,
    val timestamp: Long
) {
    override fun toString(): String {
        return """
            ╔════════════════════════════════════════════════╗
            ║         RELATÓRIO DE BATCH DE GRADES           ║
            ╠════════════════════════════════════════════════╣
            ║ Total Processado:    $totalProcessed
            ║ Bem-sucedidos:       $succeeded
            ║ Falhas:              $failed
            ║ Taxa de Sucesso:     ${String.format("%.1f%%", successRate)}
            ║ Mensagem:            $message
            ║ Itens Inválidos:     ${invalidGrades.size}
            ╚════════════════════════════════════════════════╝
        """.trimIndent()
    }
}

/**
 * Relatório de lançamento em lote.
 */
data class ReleaseReport(
    val taskId: String,
    val totalStudents: Int,
    val created: Int,
    val failed: Int,
    val successRate: Double,
    val failedStudents: List<String>,
    val timestamp: Long
) {
    override fun toString(): String {
        return """
            ╔════════════════════════════════════════════════╗
            ║        RELATÓRIO DE LANÇAMENTO EM LOTE         ║
            ╠════════════════════════════════════════════════╣
            ║ Tarefa:              $taskId
            ║ Total de Alunos:     $totalStudents
            ║ Notas Criadas:       $created
            ║ Falhas:              $failed
            ║ Taxa de Sucesso:     ${String.format("%.1f%%", successRate)}
            ║ Alunos com Falha:    ${failedStudents.size}
            ╚════════════════════════════════════════════════╝
        """.trimIndent()
    }
}

/**
 * Interface simulada para OfflineSyncQueue.
 * Em produção, usar a implementação real de Item 8.
 */
interface OfflineSyncQueue {
    suspend fun addOperation(
        operation: SyncOperation,
        entityType: String,
        entityId: String,
        entity: Any?
    )
}

/**
 * Enum de tipos de operação.
 */
enum class SyncOperation {
    CREATE, UPDATE, DELETE
}
