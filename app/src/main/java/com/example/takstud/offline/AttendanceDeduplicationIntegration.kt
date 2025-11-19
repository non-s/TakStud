package com.example.takstud.offline

import android.util.Log
import com.example.takstud.data.local.dao.AttendanceDao
import com.example.takstud.model.AttendanceRecord
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Integração de Deduplicação de Presença com o Sistema de Sincronização Offline.
 *
 * Fornece funções de extensão e helpers para integrar automaticamente
 * a deduplicação com o fluxo de salvar e sincronizar presença.
 *
 * Workflow Integrado:
 * ```
 * usuário marca presença
 *     ↓
 * saveAttendanceWithDeduplication()
 *     ↓
 * Valida duplicatas
 *     ↓
 * Salva no Room (local)
 *     ↓
 * Adiciona à fila offline (OfflineSyncQueue)
 *     ↓
 * Quando volta internet:
 *     ↓
 * deduplicateBeforeSync()
 *     ↓
 * Sincroniza com Firestore
 * ```
 *
 * Exemplo de Uso:
 * ```kotlin
 * val dedup = AttendanceDeduplicationManager(attendanceDao)
 * val offlineQueue = OfflineSyncQueueImpl(database)
 *
 * // Salvar com deduplicação integrada
 * dedup.saveAndQueueAttendance(
 *     record = newAttendance,
 *     offlineQueue = offlineQueue
 * )
 *
 * // Sincronizar com deduplicação automática
 * offlineQueue.syncAll { item ->
 *     dedup.deduplicateBeforeSync(listOf(convertToRecord(item)))
 *     firestore.saveAttendance(convertToRecord(item))
 * }
 * ```
 *
 * @see AttendanceDeduplicationManager
 * @see OfflineSyncQueue
 * @see SyncManager
 */

private const val TAG = "AttendanceDedup-Integration"

/**
 * Salva presença com deduplicação e fila offline integrados.
 *
 * Fluxo:
 * 1. Valida duplicatas no banco local
 * 2. Se não duplicata: salva no Room
 * 3. Adiciona à fila offline para sincronizar depois
 * 4. Retorna resultado com status
 *
 * @param deduplicationManager Manager para deduplicação
 * @param offlineQueue Fila offline para sincronização
 * @param record Registro de presença a salvar
 * @param dispatcher Coroutine dispatcher (default: IO)
 * @return SaveAttendanceResult com status e detalhes
 */
suspend fun saveAttendanceWithDeduplicationAndQueue(
    deduplicationManager: AttendanceDeduplicationManager,
    offlineQueue: OfflineSyncQueue,
    record: AttendanceRecord,
    dispatcher: CoroutineDispatcher = Dispatchers.IO
): SaveAttendanceResult = withContext(dispatcher) {
    try {
        Log.d(TAG, "💾 Salvando presença com deduplicação integrada: ${record.studentId}")

        // 1. Tentar salvar com deduplicação
        val saveResult = deduplicationManager.saveAttendanceWithDeduplication(record)

        if (!saveResult) {
            // Duplicata descartada
            Log.w(TAG, "⚠️ Presença descartada como duplicata")
            return@withContext SaveAttendanceResult(
                success = false,
                message = "Duplicata detectada e descartada",
                isDuplicate = true,
                queued = false
            )
        }

        // 2. Adicionar à fila offline para sincronização
        offlineQueue.enqueueAttendanceCreate(record)

        Log.i(TAG, "✅ Presença salva e adicionada à fila de sincronização")

        SaveAttendanceResult(
            success = true,
            message = "Presença salva e aguardando sincronização",
            isDuplicate = false,
            queued = true,
            recordId = "${record.studentId}-${record.date}"
        )
    } catch (e: Exception) {
        Log.e(TAG, "❌ Erro ao salvar presença com deduplicação", e)
        SaveAttendanceResult(
            success = false,
            message = "Erro ao salvar: ${e.message}",
            isDuplicate = false,
            queued = false,
            error = e
        )
    }
}

/**
 * Processa sincronização de presença com deduplicação automática.
 *
 * Fluxo:
 * 1. Deduplica registros antes de enviar
 * 2. Filtra registros para sincronizar
 * 3. Executa callback de sincronização
 * 4. Retorna relatório de sucesso/falha
 *
 * @param deduplicationManager Manager para deduplicação
 * @param records Registros para sincronizar
 * @param syncCallback Função que sincroniza cada registro
 * @param dispatcher Coroutine dispatcher
 * @return SyncProcessingResult com estatísticas
 */
suspend fun processSyncWithDeduplication(
    deduplicationManager: AttendanceDeduplicationManager,
    records: List<AttendanceRecord>,
    syncCallback: suspend (AttendanceRecord) -> Boolean,
    dispatcher: CoroutineDispatcher = Dispatchers.IO
): SyncProcessingResult = withContext(dispatcher) {
    try {
        Log.d(TAG, "📤 Iniciando sincronização com deduplicação: ${records.size} registros")

        // 1. Deduplica registros
        val deduplicatedRecords = deduplicationManager.deduplicateBeforeSync(records)
        val duplicatesRemoved = records.size - deduplicatedRecords.size

        Log.i(TAG, "✓ Deduplicação: ${deduplicatedRecords.size} únicos (removidas $duplicatesRemoved duplicatas)")

        // 2. Sincroniza registros
        var successCount = 0
        var failureCount = 0
        val failedRecords = mutableListOf<AttendanceRecord>()

        for (record in deduplicatedRecords) {
            try {
                val result = syncCallback(record)
                if (result) {
                    successCount++
                    Log.d(TAG, "✅ Sincronizado: ${record.studentId} em ${record.date}")
                } else {
                    failureCount++
                    failedRecords.add(record)
                    Log.w(TAG, "⚠️ Falha ao sincronizar: ${record.studentId}")
                }
            } catch (e: Exception) {
                failureCount++
                failedRecords.add(record)
                Log.e(TAG, "❌ Erro ao sincronizar ${record.studentId}", e)
            }
        }

        val result = SyncProcessingResult(
            totalRecords = records.size,
            duplicatesRemoved = duplicatesRemoved,
            totalToSync = deduplicatedRecords.size,
            successCount = successCount,
            failureCount = failureCount,
            failedRecords = failedRecords,
            timestamp = System.currentTimeMillis()
        )

        Log.i(TAG, "✅ Sincronização com deduplicação completa:")
        Log.i(TAG, "   - Total: ${result.totalRecords}")
        Log.i(TAG, "   - Duplicatas removidas: ${result.duplicatesRemoved}")
        Log.i(TAG, "   - Sincronizados: ${result.successCount}")
        Log.i(TAG, "   - Falhas: ${result.failureCount}")

        result
    } catch (e: Exception) {
        Log.e(TAG, "❌ Erro durante sincronização com deduplicação", e)
        SyncProcessingResult(
            totalRecords = records.size,
            duplicatesRemoved = 0,
            totalToSync = 0,
            successCount = 0,
            failureCount = records.size,
            failedRecords = records,
            error = e,
            timestamp = System.currentTimeMillis()
        )
    }
}

/**
 * Executa limpeza de duplicatas em batch de registros já sincronizados.
 *
 * Útil para manutenção periodica e auditoria.
 *
 * @param deduplicationManager Manager para deduplicação
 * @param attendanceDao DAO para persistência
 * @param records Registros para limpar
 * @param dispatcher Coroutine dispatcher
 * @return CleanupResult com estatísticas
 */
suspend fun cleanupDuplicateAttendance(
    deduplicationManager: AttendanceDeduplicationManager,
    attendanceDao: AttendanceDao,
    records: List<AttendanceRecord>,
    dispatcher: CoroutineDispatcher = Dispatchers.IO
): CleanupResult = withContext(dispatcher) {
    try {
        Log.d(TAG, "🧹 Iniciando limpeza de duplicatas")

        // 1. Detectar duplicatas
        val duplicateResult = deduplicationManager.detectDuplicates(records)

        Log.i(TAG, "Duplicatas detectadas: ${duplicateResult.removedCount}")

        // 2. Remover duplicatas do banco
        var removedCount = 0
        for (duplicate in duplicateResult.duplicates) {
            try {
                attendanceDao.deleteAttendanceById(duplicate.id)
                removedCount++
                Log.d(TAG, "🗑️  Duplicata removida: ${duplicate.id}")
            } catch (e: Exception) {
                Log.e(TAG, "Erro ao remover duplicata: ${duplicate.id}", e)
            }
        }

        CleanupResult(
            totalRecordsProcessed = records.size,
            duplicatesFound = duplicateResult.removedCount,
            duplicatesRemoved = removedCount,
            timestamp = System.currentTimeMillis()
        )
    } catch (e: Exception) {
        Log.e(TAG, "❌ Erro durante limpeza de duplicatas", e)
        CleanupResult(
            totalRecordsProcessed = records.size,
            duplicatesFound = 0,
            duplicatesRemoved = 0,
            error = e,
            timestamp = System.currentTimeMillis()
        )
    }
}

/**
 * Gera relatório de integridade e deduplicação para análise.
 *
 * @param deduplicationManager Manager para deduplicação
 * @param records Registros para analisar
 * @param includeDetails Se deve incluir detalhes de cada duplicata
 * @return ComprehensiveReport com análise completa
 */
suspend fun generateComprehensiveAttendanceReport(
    deduplicationManager: AttendanceDeduplicationManager,
    records: List<AttendanceRecord>,
    includeDetails: Boolean = false
): ComprehensiveReport {
    Log.d(TAG, "📊 Gerando relatório abrangente de presença")

    // 1. Validação
    val validationResult = deduplicationManager.validateBatch(records)

    // 2. Detecção de duplicatas
    val duplicateResult = deduplicationManager.detectDuplicates(records)

    // 3. Relatório detalhado
    val deduplicationReport = deduplicationManager.generateDeduplicationReport(records)

    // 4. Análise por data
    val recordsByDate = records.groupBy { it.date }
    val dateAnalysis = recordsByDate.mapValues { (date, recordsOnDate) ->
        DateAttendanceAnalysis(
            date = date,
            totalRecords = recordsOnDate.size,
            presentCount = recordsOnDate.count { it.isPresent },
            absentCount = recordsOnDate.count { !it.isPresent },
            duplicateCount = deduplicationReport.duplicatesByDate[date]?.size ?: 0
        )
    }

    // 5. Análise por aluno
    val recordsByStudent = records.groupBy { it.studentId }
    val studentAnalysis = recordsByStudent.mapValues { (studentId, recordsOfStudent) ->
        StudentAttendanceAnalysis(
            studentId = studentId,
            studentName = recordsOfStudent.firstOrNull()?.studentName ?: "Unknown",
            totalRecords = recordsOfStudent.size,
            presentCount = recordsOfStudent.count { it.isPresent },
            absentCount = recordsOfStudent.count { !it.isPresent },
            duplicateCount = deduplicationReport.duplicatesByStudent[studentId]?.size ?: 0
        )
    }

    val report = ComprehensiveReport(
        totalRecords = records.size,
        validRecords = validationResult.validRecords,
        issuesFound = validationResult.issues.size,
        duplicatesFound = duplicateResult.removedCount,
        deduplicationRate = deduplicationReport.deduplicationRate,
        validationReport = validationResult,
        deduplicationReport = deduplicationReport,
        dateAnalysis = dateAnalysis,
        studentAnalysis = studentAnalysis,
        timestamp = System.currentTimeMillis()
    )

    Log.i(TAG, "✅ Relatório gerado: ${report.totalRecords} registros analisados")

    return report
}

// ==================== RESULT DATA CLASSES ====================

/**
 * Resultado do salvamento de presença com deduplicação.
 */
data class SaveAttendanceResult(
    val success: Boolean,
    val message: String,
    val isDuplicate: Boolean = false,
    val queued: Boolean = false,
    val recordId: String? = null,
    val error: Exception? = null
)

/**
 * Resultado do processamento de sincronização.
 */
data class SyncProcessingResult(
    val totalRecords: Int,
    val duplicatesRemoved: Int,
    val totalToSync: Int,
    val successCount: Int,
    val failureCount: Int,
    val failedRecords: List<AttendanceRecord> = emptyList(),
    val error: Exception? = null,
    val timestamp: Long = System.currentTimeMillis()
) {
    val successRate: Double = if (totalToSync > 0) (successCount.toDouble() / totalToSync) * 100 else 0.0
}

/**
 * Resultado da limpeza de duplicatas.
 */
data class CleanupResult(
    val totalRecordsProcessed: Int,
    val duplicatesFound: Int,
    val duplicatesRemoved: Int,
    val error: Exception? = null,
    val timestamp: Long = System.currentTimeMillis()
) {
    val isSuccessful: Boolean = duplicatesFound == duplicatesRemoved && error == null
}

/**
 * Análise de presença por data.
 */
data class DateAttendanceAnalysis(
    val date: String,
    val totalRecords: Int,
    val presentCount: Int,
    val absentCount: Int,
    val duplicateCount: Int
) {
    val attendanceRate: Double = if (totalRecords > 0) (presentCount.toDouble() / totalRecords) * 100 else 0.0
}

/**
 * Análise de presença por aluno.
 */
data class StudentAttendanceAnalysis(
    val studentId: String,
    val studentName: String,
    val totalRecords: Int,
    val presentCount: Int,
    val absentCount: Int,
    val duplicateCount: Int
) {
    val attendanceRate: Double = if (totalRecords > 0) (presentCount.toDouble() / totalRecords) * 100 else 0.0
}

/**
 * Relatório abrangente de presença com deduplicação.
 */
data class ComprehensiveReport(
    val totalRecords: Int,
    val validRecords: Int,
    val issuesFound: Int,
    val duplicatesFound: Int,
    val deduplicationRate: Double,
    val validationReport: ValidationResult,
    val deduplicationReport: DeduplicationReport,
    val dateAnalysis: Map<String, DateAttendanceAnalysis>,
    val studentAnalysis: Map<String, StudentAttendanceAnalysis>,
    val timestamp: Long = System.currentTimeMillis()
) {
    override fun toString(): String {
        return """
            ╔════════════════════════════════════════════════════════════════╗
            ║           RELATÓRIO ABRANGENTE DE PRESENÇA                    ║
            ╠════════════════════════════════════════════════════════════════╣
            ║ Total de Registros:        $totalRecords
            ║ Registros Válidos:         $validRecords
            ║ Problemas Encontrados:     $issuesFound
            ║ Duplicatas Detectadas:     $duplicatesFound
            ║ Taxa de Deduplicação:      ${String.format("%.2f%%", deduplicationRate)}
            ║
            ║ Análise por Data:          ${dateAnalysis.size} datas
            ║ Análise por Aluno:         ${studentAnalysis.size} alunos
            ║
            ║ Status Geral:              ${if (issuesFound == 0 && duplicatesFound == 0) "✓ Íntegro" else "⚠️  Problemas detectados"}
            ╚════════════════════════════════════════════════════════════════╝
        """.trimIndent()
    }
}
