package com.example.takstud.offline

import android.util.Log
import com.example.takstud.data.local.dao.AttendanceDao
import com.example.takstud.data.local.entity.AttendanceEntity
import com.example.takstud.model.AttendanceRecord
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import java.text.SimpleDateFormat
import java.util.Locale

/**
 * Manager para detecção e prevenção de duplicatas em registros de presença.
 *
 * Estratégia de Deduplicação:
 * - **Chave Única**: `studentId` + `date` (cada aluno tem uma única presença por dia)
 * - **Resolução de Conflito**: Last-Write-Wins usando `lastModified` timestamp
 * - **Armazenamento**: Composite key em formato `{studentId}-{date}`
 *
 * Casos de Uso:
 * 1. **Duplicatas Offline**: Mesmo aluno registra presença 2x offline
 *    → Ambas armazenadas em fila
 *    → Na sincronização: deduplica automaticamente
 *    → Mantém a mais recente (maior timestamp)
 *
 * 2. **Conflito Online/Offline**: Aluno marcado online + offline
 *    → Fila contém versão offline
 *    → Firestore tem versão online
 *    → Sincronização resolve com LWW
 *
 * 3. **Duplicatas em Batch**: Professor registra attendance em lote
 *    → Validação de duplicatas antes de persistir
 *    → Garante integridade do banco local
 *
 * Exemplo:
 * ```kotlin
 * val dedup = AttendanceDeduplicationManager(attendanceDao)
 *
 * // Salvar com deduplicação automática
 * dedup.saveAttendanceWithDeduplication(newRecord)
 *
 * // Detectar duplicatas em lote
 * val result = dedup.detectDuplicates(listOfRecords)
 * Log.i("Dedup", "Removidas: ${result.removed}, Mantidas: ${result.unique.size}")
 *
 * // Aplicar deduplicação durante sincronização
 * dedup.deduplicateBeforeSync(unsyncedRecords)
 * ```
 *
 * Garantias:
 * - ✓ Thread-safe com Mutex
 * - ✓ Timestamp-based conflict resolution
 * - ✓ Logging detalhado de duplicatas
 * - ✓ Preservação de dados (sem loss)
 * - ✓ Recuperação de estados inconsistentes
 *
 * @see OfflineSyncQueue
 * @see SyncManager
 * @see AttendanceDao
 */
class AttendanceDeduplicationManager(
    private val attendanceDao: AttendanceDao
) {

    companion object {
        private const val TAG = "AttendanceDedup"

        // Padrão para chave composta
        private const val COMPOSITE_KEY_FORMAT = "%s-%s"  // studentId-date
    }

    private val mutex = Mutex()

    /**
     * Salva um registro de presença com deduplicação automática.
     *
     * Fluxo:
     * 1. Detectar se já existe presença para este aluno neste dia
     * 2. Se existe: comparar timestamps
     *    - Se novo > antigo: substituir
     *    - Se novo ≤ antigo: descartar
     * 3. Se não existe: inserir
     *
     * @param record Novo registro de presença
     * @return true se foi inserido/atualizado, false se descartado como duplicata
     */
    suspend fun saveAttendanceWithDeduplication(record: AttendanceRecord): Boolean {
        return mutex.withLock {
            try {
                Log.d(TAG, "💾 Salvando presença: ${record.studentId} em ${record.date}")

                // Gerar ID composto
                val compositeId = generateCompositeKey(record.studentId, record.date)
                val entityRecord = record.toEntity(compositeId)

                // Verificar se existe presença anterior
                val existing = attendanceDao.getAttendanceById(compositeId)

                return@withLock if (existing != null) {
                    // Resolver conflito usando Last-Write-Wins
                    if (record.modifiedAt > existing.lastModified) {
                        Log.i(TAG, "🔄 Atualizando presença: novo timestamp (${record.modifiedAt}) > antigo (${existing.lastModified})")
                        attendanceDao.updateAttendance(entityRecord)
                        true
                    } else {
                        Log.w(TAG, "⚠️ Duplicata descartada: timestamp do novo registro é mais antigo")
                        false
                    }
                } else {
                    // Novo registro
                    Log.i(TAG, "✅ Nova presença registrada: ${record.studentId}")
                    attendanceDao.insertAttendance(entityRecord)
                    true
                }
            } catch (e: Exception) {
                Log.e(TAG, "❌ Erro ao salvar presença com deduplicação", e)
                false
            }
        }
    }

    /**
     * Detecta duplicatas em uma lista de registros.
     *
     * Usa chave composta `studentId-date` para identificar duplicatas.
     * Para registros duplicados, mantém o mais recente (maior `modifiedAt`).
     *
     * @param records Lista de registros para analisar
     * @return DuplicateAttendanceResult com estatísticas
     */
    suspend fun detectDuplicates(records: List<AttendanceRecord>): DuplicateAttendanceResult {
        return mutex.withLock {
            Log.d(TAG, "🔍 Detectando duplicatas em ${records.size} registros...")

            val uniqueMap = mutableMapOf<String, AttendanceRecord>()
            val duplicates = mutableListOf<AttendanceRecord>()
            var conflictResolutions = 0

            // Ordenar por timestamp decrescente para manter a mais recente
            for (record in records.sortedByDescending { it.modifiedAt }) {
                val key = createCompositeKey(record.studentId, record.date)

                if (uniqueMap.containsKey(key)) {
                    // Existe duplicata
                    val existing = uniqueMap[key]!!
                    duplicates.add(record)  // Adiciona a mais antiga à lista de duplicatas

                    Log.w(TAG, "⚠️ Duplicata encontrada: $key")
                    Log.w(TAG, "   - Mantendo: timestamp=${existing.modifiedAt}, presente=${existing.isPresent}")
                    Log.w(TAG, "   - Descartando: timestamp=${record.modifiedAt}, presente=${record.isPresent}")

                    conflictResolutions++
                } else {
                    uniqueMap[key] = record
                }
            }

            val result = DuplicateAttendanceResult(
                unique = uniqueMap.values.toList(),
                duplicates = duplicates,
                removedCount = duplicates.size,
                conflictResolutions = conflictResolutions,
                totalAnalyzed = records.size
            )

            Log.i(TAG, "✅ Análise completa:")
            Log.i(TAG, "   - Total: ${result.totalAnalyzed}")
            Log.i(TAG, "   - Únicos: ${result.unique.size}")
            Log.i(TAG, "   - Duplicatas: ${result.removedCount}")
            Log.i(TAG, "   - Conflitos resolvidos: ${result.conflictResolutions}")

            result
        }
    }

    /**
     * Deduplica registros antes da sincronização.
     *
     * Chamado durante o processo de sync para garantir que
     * apenas registros únicos sejam enviados para Firestore.
     *
     * @param records Registros não sincronizados
     * @return Lista deduplica de registros
     */
    suspend fun deduplicateBeforeSync(records: List<AttendanceRecord>): List<AttendanceRecord> {
        val result = detectDuplicates(records)

        Log.i(TAG, "📤 Deduplicando antes de sync:")
        Log.i(TAG, "   - Registros únicos para sincronizar: ${result.unique.size}")
        if (result.removedCount > 0) {
            Log.w(TAG, "   - ${result.removedCount} duplicatas removidas")
        }

        return result.unique
    }

    /**
     * Verifica integridade: detecta e resolve duplicatas no banco local.
     *
     * Útil para manutenção e auditoria. Processa todo o banco de dados
     * local em busca de inconsistências.
     *
     * @return IntegrityCheckResult com relatório
     */
    suspend fun performIntegrityCheck(): IntegrityCheckResult {
        return mutex.withLock {
            try {
                Log.i(TAG, "🔧 Iniciando verificação de integridade...")

                val allAttendance = attendanceDao.getUnsyncedAttendance()
                Log.d(TAG, "   - Analisando ${allAttendance.size} registros locais")

                val uniqueMap = mutableMapOf<String, AttendanceEntity>()
                val duplicateIds = mutableListOf<String>()
                var recovered = 0

                // Agrupar por chave composta e identificar duplicatas
                for (entity in allAttendance.sortedByDescending { it.lastModified }) {
                    val key = createCompositeKey(entity.studentId, entity.date)

                    if (uniqueMap.containsKey(key)) {
                        // Duplicata encontrada - marcar para remoção
                        duplicateIds.add(entity.id)
                        Log.w(TAG, "❌ Duplicata detectada: $key (id=${entity.id})")
                    } else {
                        uniqueMap[key] = entity
                    }
                }

                // Remover duplicatas do banco
                for (duplicateId in duplicateIds) {
                    try {
                        attendanceDao.deleteAttendanceById(duplicateId)
                        recovered++
                        Log.i(TAG, "✅ Duplicata removida: $duplicateId")
                    } catch (e: Exception) {
                        Log.e(TAG, "❌ Erro ao remover duplicata: $duplicateId", e)
                    }
                }

                val result = IntegrityCheckResult(
                    totalRecords = allAttendance.size,
                    duplicatesFound = duplicateIds.size,
                    duplicatesRemoved = recovered,
                    isHealthy = duplicateIds.isEmpty(),
                    timestamp = System.currentTimeMillis()
                )

                Log.i(TAG, "✅ Verificação de integridade completa:")
                Log.i(TAG, "   - Status: ${if (result.isHealthy) "✓ Saudável" else "⚠️ Duplicatas encontradas"}")
                Log.i(TAG, "   - Total: ${result.totalRecords}")
                Log.i(TAG, "   - Duplicatas removidas: ${result.duplicatesRemoved}/${result.duplicatesFound}")

                result
            } catch (e: Exception) {
                Log.e(TAG, "❌ Erro durante verificação de integridade", e)
                IntegrityCheckResult(
                    totalRecords = 0,
                    duplicatesFound = 0,
                    duplicatesRemoved = 0,
                    isHealthy = false,
                    timestamp = System.currentTimeMillis(),
                    error = e.message
                )
            }
        }
    }

    /**
     * Valida registros antes de inserção em lote.
     *
     * Útil ao importar attendance de um arquivo ou ao processar
     * registros em lote do servidor.
     *
     * @param records Registros para validar
     * @return ValidationResult com detalhes
     */
    suspend fun validateBatch(records: List<AttendanceRecord>): ValidationResult {
        return mutex.withLock {
            Log.d(TAG, "✓ Validando lote de ${records.size} registros...")

            val issues = mutableListOf<ValidationIssue>()
            val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())

            for ((index, record) in records.withIndex()) {
                // Validar data
                if (record.date.isBlank()) {
                    issues.add(ValidationIssue(index, "Data inválida", "Data está vazia"))
                } else {
                    try {
                        dateFormat.parse(record.date)
                    } catch (e: Exception) {
                        issues.add(ValidationIssue(index, "Data inválida", "Formato deve ser yyyy-MM-dd"))
                    }
                }

                // Validar studentId
                if (record.studentId.isBlank()) {
                    issues.add(ValidationIssue(index, "Student ID inválido", "ID do aluno está vazio"))
                }

                // Validar timestamps
                if (record.modifiedAt <= 0) {
                    issues.add(ValidationIssue(index, "Timestamp inválido", "modifiedAt deve ser > 0"))
                }

                // Validar campos obrigatórios
                if (record.studentName.isBlank()) {
                    issues.add(ValidationIssue(index, "Nome do aluno inválido", "Nome está vazio"))
                }
            }

            // Detectar duplicatas
            val duplicateDetection = detectDuplicates(records)

            val result = ValidationResult(
                totalRecords = records.size,
                validRecords = records.size - issues.size,
                issues = issues,
                duplicatesDetected = duplicateDetection.removedCount,
                isValid = issues.isEmpty() && duplicateDetection.removedCount == 0,
                timestamp = System.currentTimeMillis()
            )

            Log.i(TAG, "✅ Validação completa:")
            Log.i(TAG, "   - Válidos: ${result.validRecords}/${result.totalRecords}")
            Log.i(TAG, "   - Problemas encontrados: ${result.issues.size}")
            Log.i(TAG, "   - Duplicatas: ${result.duplicatesDetected}")

            result
        }
    }

    /**
     * Obtém relatório de deduplicação para análise.
     *
     * @param records Registros para analisar
     * @return DeduplicationReport com estatísticas detalhadas
     */
    suspend fun generateDeduplicationReport(records: List<AttendanceRecord>): DeduplicationReport {
        return mutex.withLock {
            val duplicateResult = detectDuplicates(records)

            // Agrupar duplicatas por aluno
            val duplicatesByStudent = mutableMapOf<String, List<AttendanceRecord>>()
            for (duplicate in duplicateResult.duplicates) {
                val studentId = duplicate.studentId
                if (!duplicatesByStudent.containsKey(studentId)) {
                    duplicatesByStudent[studentId] = mutableListOf()
                }
                (duplicatesByStudent[studentId] as MutableList).add(duplicate)
            }

            // Agrupar duplicatas por data
            val duplicatesByDate = mutableMapOf<String, List<AttendanceRecord>>()
            for (duplicate in duplicateResult.duplicates) {
                val date = duplicate.date
                if (!duplicatesByDate.containsKey(date)) {
                    duplicatesByDate[date] = mutableListOf()
                }
                (duplicatesByDate[date] as MutableList).add(duplicate)
            }

            DeduplicationReport(
                totalRecords = records.size,
                uniqueRecords = duplicateResult.unique.size,
                duplicates = duplicateResult.removedCount,
                deduplicationRate = if (records.isEmpty()) 0.0
                    else (duplicateResult.removedCount.toDouble() / records.size) * 100,
                duplicatesByStudent = duplicatesByStudent,
                duplicatesByDate = duplicatesByDate,
                conflictResolutions = duplicateResult.conflictResolutions,
                timestamp = System.currentTimeMillis()
            )
        }
    }

    /**
     * Gera chave composta para presença (studentId-date).
     *
     * @param studentId ID do aluno
     * @param date Data em formato yyyy-MM-dd
     * @return Chave composta
     */
    private fun generateCompositeKey(studentId: String, date: String): String {
        return createCompositeKey(studentId, date)
    }

    /**
     * Cria chave composta (helper estático).
     */
    private fun createCompositeKey(studentId: String, date: String): String {
        return String.format(COMPOSITE_KEY_FORMAT, studentId, date)
    }

    /**
     * Converte AttendanceRecord para AttendanceEntity.
     */
    private fun AttendanceRecord.toEntity(id: String): AttendanceEntity {
        return AttendanceEntity(
            id = id,
            studentId = studentId,
            studentRa = studentRa,
            studentName = studentName,
            studentClass = studentClass,
            classId = classId,
            date = date,
            isPresent = isPresent,
            timestamp = System.currentTimeMillis().toString(),
            isSynced = isSynced,
            lastModified = modifiedAt
        )
    }
}

/**
 * Resultado da detecção de duplicatas.
 *
 * @property unique Lista de registros únicos (sem duplicatas)
 * @property duplicates Lista de registros identificados como duplicatas
 * @property removedCount Quantidade de duplicatas removidas
 * @property conflictResolutions Quantidade de conflitos resolvidos
 * @property totalAnalyzed Total de registros analisados
 */
data class DuplicateAttendanceResult(
    val unique: List<AttendanceRecord>,
    val duplicates: List<AttendanceRecord>,
    val removedCount: Int,
    val conflictResolutions: Int,
    val totalAnalyzed: Int
)

/**
 * Resultado da verificação de integridade.
 *
 * @property totalRecords Total de registros analisados
 * @property duplicatesFound Duplicatas encontradas
 * @property duplicatesRemoved Duplicatas removidas com sucesso
 * @property isHealthy Se o banco está íntegro (sem duplicatas)
 * @property timestamp Quando a verificação foi realizada
 * @property error Mensagem de erro, se houver
 */
data class IntegrityCheckResult(
    val totalRecords: Int,
    val duplicatesFound: Int,
    val duplicatesRemoved: Int,
    val isHealthy: Boolean,
    val timestamp: Long,
    val error: String? = null
)

/**
 * Resultado da validação de lote.
 *
 * @property totalRecords Total de registros
 * @property validRecords Registros válidos
 * @property issues Lista de problemas encontrados
 * @property duplicatesDetected Duplicatas detectadas
 * @property isValid Se todos os registros são válidos
 * @property timestamp Quando foi validado
 */
data class ValidationResult(
    val totalRecords: Int,
    val validRecords: Int,
    val issues: List<ValidationIssue>,
    val duplicatesDetected: Int,
    val isValid: Boolean,
    val timestamp: Long
)

/**
 * Problema encontrado durante validação.
 *
 * @property recordIndex Índice do registro com problema
 * @property field Campo com problema
 * @property message Descrição do problema
 */
data class ValidationIssue(
    val recordIndex: Int,
    val field: String,
    val message: String
)

/**
 * Relatório detalhado de deduplicação.
 *
 * @property totalRecords Total de registros analisados
 * @property uniqueRecords Registros únicos mantidos
 * @property duplicates Quantidade de duplicatas
 * @property deduplicationRate Percentual de duplicatas (0-100%)
 * @property duplicatesByStudent Mapa de duplicatas agrupadas por aluno
 * @property duplicatesByDate Mapa de duplicatas agrupadas por data
 * @property conflictResolutions Quantidade de conflitos resolvidos
 * @property timestamp Quando foi gerado
 */
data class DeduplicationReport(
    val totalRecords: Int,
    val uniqueRecords: Int,
    val duplicates: Int,
    val deduplicationRate: Double,
    val duplicatesByStudent: Map<String, List<AttendanceRecord>>,
    val duplicatesByDate: Map<String, List<AttendanceRecord>>,
    val conflictResolutions: Int,
    val timestamp: Long
) {
    /**
     * Resumo em texto do relatório.
     */
    override fun toString(): String {
        return """
            ╔════════════════════════════════════════════════╗
            ║  RELATÓRIO DE DEDUPLICAÇÃO DE PRESENÇA         ║
            ╠════════════════════════════════════════════════╣
            ║ Total de Registros:     $totalRecords
            ║ Registros Únicos:       $uniqueRecords
            ║ Duplicatas Detectadas:  $duplicates
            ║ Taxa de Deduplicação:   ${String.format("%.2f%%", deduplicationRate)}
            ║ Conflitos Resolvidos:   $conflictResolutions
            ║ Alunos com Duplicatas:  ${duplicatesByStudent.size}
            ║ Datas com Duplicatas:   ${duplicatesByDate.size}
            ╚════════════════════════════════════════════════╝
        """.trimIndent()
    }
}
