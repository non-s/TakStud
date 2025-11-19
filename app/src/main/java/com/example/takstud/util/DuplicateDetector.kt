package com.example.takstud.util

import android.util.Log
import com.example.takstud.model.*

/**
 * DuplicateDetector - Detecta e remove dados duplicados em Room.
 *
 * FUNCIONALIDADES:
 * - Detecção de duplicatas por ID e conteúdo
 * - Merge automático de duplicatas
 * - Hash-based deduplication
 * - Remoção automática
 * - Auditoria de duplicatas
 *
 * PADRÃO:
 * - Executar após sincronização com Firestore
 * - Remove duplicatas mantendo a versão mais recente
 * - Registra duplicatas removidas para auditoria
 */
class DuplicateDetector {

    private val TAG = "DuplicateDetector"

    // ============== DETECTAR DUPLICATAS ==============

    /**
     * Detecta tarefas duplicadas por ID ou conteúdo.
     */
    fun detectDuplicateTasks(tasks: List<Task>): DuplicateResult<Task> {
        Log.i(TAG, "Verificando ${tasks.size} tasks por duplicatas...")

        val uniqueTasks = mutableMapOf<String, Task>()
        val duplicates = mutableListOf<Task>()

        // Por ID exato
        for (task in tasks.sortedByDescending { it.modifiedAt ?: 0 }) {
            if (uniqueTasks.containsKey(task.id)) {
                duplicates.add(task)
                Log.w(TAG, "⚠️ Duplicata encontrada: task ${task.id}")
            } else {
                uniqueTasks[task.id] = task
            }
        }

        // Por conteúdo similar (mesma descrição + data)
        val contentHashes = mutableMapOf<String, Task>()
        for ((_, task) in uniqueTasks) {
            val hash = "${task.title}|${task.dueDate}".hashCode().toString()
            if (contentHashes.containsKey(hash)) {
                duplicates.add(task)
                Log.w(TAG, "⚠️ Conteúdo duplicado: task ${task.id}")
            } else {
                contentHashes[hash] = task
            }
        }

        return DuplicateResult(
            unique = uniqueTasks.values.toList(),
            duplicates = duplicates,
            removed = duplicates.size
        )
    }

    /**
     * Detecta grades duplicadas (crítico - notas não devem duplicar).
     */
    fun detectDuplicateGrades(grades: List<Grade>): DuplicateResult<Grade> {
        Log.i(TAG, "Verificando ${grades.size} grades por duplicatas...")

        val uniqueGrades = mutableMapOf<String, Grade>()
        val duplicates = mutableListOf<Grade>()

        // Por ID composto: studentId + taskId
        for (grade in grades.sortedByDescending { it.modifiedAt ?: 0 }) {
            val compositeKey = "${grade.studentId}|${grade.taskId}"

            if (uniqueGrades.containsKey(compositeKey)) {
                duplicates.add(grade)
                Log.w(TAG, "⚠️ Grade duplicada: student ${grade.studentId}, task ${grade.taskId}")
            } else {
                uniqueGrades[compositeKey] = grade
            }
        }

        return DuplicateResult(
            unique = uniqueGrades.values.toList(),
            duplicates = duplicates,
            removed = duplicates.size
        )
    }

    /**
     * Detecta registros de attendance duplicados.
     */
    fun detectDuplicateAttendance(records: List<AttendanceRecord>): DuplicateResult<AttendanceRecord> {
        Log.i(TAG, "Verificando ${records.size} registros de attendance...")

        val uniqueRecords = mutableMapOf<String, AttendanceRecord>()
        val duplicates = mutableListOf<AttendanceRecord>()

        // Por ID composto: studentId + date
        for (record in records.sortedByDescending { it.modifiedAt ?: 0 }) {
            val compositeKey = "${record.studentId}|${record.date}"

            if (uniqueRecords.containsKey(compositeKey)) {
                duplicates.add(record)
                Log.w(TAG, "⚠️ Attendance duplicada: student ${record.studentId}, date ${record.date}")
            } else {
                uniqueRecords[compositeKey] = record
            }
        }

        return DuplicateResult(
            unique = uniqueRecords.values.toList(),
            duplicates = duplicates,
            removed = duplicates.size
        )
    }

    // ============== RESULTADO ==============

    data class DuplicateResult<T>(
        val unique: List<T>,
        val duplicates: List<T>,
        val removed: Int
    ) {
        fun hasDuplicates(): Boolean = duplicates.isNotEmpty()

        fun summary(): String = """
            Duplicatas Detectadas:
            ├─ Únicos: ${unique.size}
            ├─ Duplicatas: ${duplicates.size}
            └─ Removidas: $removed
        """.trimIndent()
    }

    // ============== MERGE ==============

    /**
     * Faz merge de duas tarefas duplicadas, mantendo a mais recente.
     */
    fun mergeTaskDuplicates(original: Task, duplicate: Task): Task {
        return if ((original.modifiedAt ?: 0) >= (duplicate.modifiedAt ?: 0)) {
            original
        } else {
            duplicate
        }
    }

    /**
     * Faz merge de duas grades duplicadas.
     * CRÍTICO: Usa regra de conflito (mais recente vence).
     */
    fun mergeGradeDuplicates(original: Grade, duplicate: Grade): Grade {
        val merged = if ((original.modifiedAt ?: 0) >= (duplicate.modifiedAt ?: 0)) {
            original
        } else {
            duplicate
        }

        // Se modificados no mesmo timestamp, usar valor maior (beneficio pro aluno)
        if ((original.modifiedAt ?: 0) == (duplicate.modifiedAt ?: 0)) {
            val origValue = original.value.toDoubleOrNull() ?: 0.0
            val dupValue = duplicate.value.toDoubleOrNull() ?: 0.0
            if (dupValue > origValue) {
                return duplicate
            }
        }

        return merged
    }

    // ============== VALIDAÇÃO ==============

    /**
     * Valida integridade dos dados após deduplicação.
     */
    fun <T> validateDeduplicationResult(
        original: List<T>,
        result: DuplicateResult<T>
    ): ValidationResult {
        val totalBefore = original.size
        val totalAfter = result.unique.size + result.duplicates.size
        val dataLoss = totalBefore - totalAfter

        return ValidationResult(
            beforeCount = totalBefore,
            afterCount = totalAfter,
            removedDuplicates = result.duplicates.size,
            dataLoss = dataLoss,
            isValid = dataLoss == 0 && result.unique.size < totalBefore
        )
    }

    data class ValidationResult(
        val beforeCount: Int,
        val afterCount: Int,
        val removedDuplicates: Int,
        val dataLoss: Int,
        val isValid: Boolean
    ) {
        override fun toString(): String = """
            Validação de Deduplicação:
            ├─ Antes: $beforeCount
            ├─ Depois: $afterCount
            ├─ Duplicatas removidas: $removedDuplicates
            ├─ Perda de dados: $dataLoss
            └─ Válido: ${if (isValid) "✓" else "✗"}
        """.trimIndent()
    }
}
