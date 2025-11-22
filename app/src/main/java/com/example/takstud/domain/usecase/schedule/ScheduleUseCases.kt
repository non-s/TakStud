package com.example.takstud.domain.usecase.schedule

import com.example.takstud.data.repository.ScheduleRepository
import com.example.takstud.model.schedule.*
import javax.inject.Inject

/**
 * 🎯 Use Cases para Horários
 * Encapsula lógica de negócios complexa
 */

// ==================== DETECT CONFLICTS ====================

/**
 * Detecta conflitos em uma grade horária
 */
class DetectScheduleConflictsUseCase @Inject constructor(
    private val repository: ScheduleRepository
) {
    suspend operator fun invoke(scheduleId: String): Result<List<ScheduleConflict>> {
        return try {
            val conflicts = repository.detectConflicts(scheduleId)
            Result.success(conflicts)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Detecta conflitos em uma grade sem salvá-la
     */
    operator fun invoke(schedule: ClassSchedule): List<ScheduleConflict> {
        return ConflictDetector.detectConflicts(schedule)
    }
}

// ==================== VALIDATE TIME SLOT ====================

/**
 * Valida um time slot antes de salvar
 */
class ValidateTimeSlotUseCase @Inject constructor(
    private val repository: ScheduleRepository
) {
    suspend operator fun invoke(scheduleId: String, timeSlot: TimeSlot): ValidationResult {
        val errors = mutableListOf<String>()

        // Validação básica
        if (!timeSlot.isValid()) {
            errors.add("Dados do horário inválidos")
        }

        // Validação de horários
        if (timeSlot.startTime >= timeSlot.endTime) {
            errors.add("Horário de término deve ser maior que o de início")
        }

        // Verificar conflitos
        try {
            val hasConflict = repository.checkSlotConflict(scheduleId, timeSlot)
            if (hasConflict) {
                errors.add("Este horário conflita com outro existente")
            }
        } catch (e: Exception) {
            errors.add("Erro ao verificar conflitos: ${e.message}")
        }

        // Validação específica por tipo
        when {
            timeSlot.isBreak -> {
                if (timeSlot.getDurationMinutes() < 10) {
                    errors.add("Intervalo deve ter pelo menos 10 minutos")
                }
            }
            timeSlot.isSpecialEvent -> {
                if (timeSlot.eventTitle.isBlank()) {
                    errors.add("Evento deve ter um título")
                }
            }
            else -> {
                if (timeSlot.subjectId.isBlank()) {
                    errors.add("Selecione uma disciplina")
                }
                if (timeSlot.getDurationMinutes() < 30) {
                    errors.add("Aula deve ter pelo menos 30 minutos")
                }
            }
        }

        return if (errors.isEmpty()) {
            ValidationResult.Success
        } else {
            ValidationResult.Error(errors)
        }
    }
}

sealed class ValidationResult {
    object Success : ValidationResult()
    data class Error(val errors: List<String>) : ValidationResult()
}

// ==================== COPY SCHEDULE ====================

/**
 * Copia uma grade para outra turma
 */
class CopyScheduleUseCase @Inject constructor(
    private val repository: ScheduleRepository
) {
    suspend operator fun invoke(
        sourceScheduleId: String,
        targetClassName: String,
        copyBreaks: Boolean = true,
        adjustTimes: Boolean = false
    ): Result<ClassSchedule> {
        return try {
            // Copiar grade
            var copiedSchedule = repository.copyScheduleToClass(sourceScheduleId, targetClassName)

            // Remover intervalos se solicitado
            if (!copyBreaks) {
                copiedSchedule = copiedSchedule.copy(
                    breaks = emptyList()
                )
            }

            // Ajustar horários se solicitado (ex: tarde vs manhã)
            if (adjustTimes) {
                // TODO: Implementar ajuste de horários
                // Por exemplo, se copiar de manhã para tarde, adicionar 6 horas
            }

            // Salvar grade ajustada
            repository.saveSchedule(copiedSchedule)

            Result.success(copiedSchedule)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}

// ==================== CREATE FROM TEMPLATE ====================

/**
 * Cria uma nova grade a partir de um template
 */
class CreateScheduleFromTemplateUseCase @Inject constructor(
    private val repository: ScheduleRepository
) {
    suspend operator fun invoke(
        templateId: String,
        className: String,
        period: com.example.takstud.model.Period,
        year: Int
    ): Result<ClassSchedule> {
        return try {
            // Copiar template
            val newSchedule = repository.copyScheduleToClass(templateId, className)

            // Atualizar com dados específicos
            val customizedSchedule = newSchedule.copy(
                period = period,
                year = year,
                isTemplate = false
            )

            // Salvar
            repository.saveSchedule(customizedSchedule)

            Result.success(customizedSchedule)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}

// ==================== OPTIMIZE SCHEDULE ====================

/**
 * Otimiza uma grade horária
 * - Remove janelas de horário desnecessárias
 * - Distribui aulas uniformemente
 * - Sugere melhorias
 */
class OptimizeScheduleUseCase @Inject constructor() {
    operator fun invoke(schedule: ClassSchedule): ScheduleOptimizationResult {
        val suggestions = mutableListOf<String>()
        val warnings = mutableListOf<String>()

        // Analisar cada dia
        DayOfWeek.weekDays().forEach { day ->
            val daySlots = schedule.getAllSlotsByDay(day)

            // Verificar janelas (gaps)
            val gaps = detectGaps(daySlots)
            if (gaps.isNotEmpty()) {
                warnings.add("$day: ${gaps.size} janela(s) de horário detectada(s)")
                suggestions.add("Reorganize as aulas em $day para remover janelas")
            }

            // Verificar carga horária
            val totalMinutes = daySlots.filter { !it.isBreak }.sumOf { it.getDurationMinutes() }
            val hours = totalMinutes / 60.0

            if (hours > 6) {
                warnings.add("$day: Carga horária muito alta (${hours}h)")
                suggestions.add("Considere redistribuir algumas aulas de $day para outros dias")
            } else if (hours > 0 && hours < 3) {
                warnings.add("$day: Carga horária muito baixa (${hours}h)")
                suggestions.add("Aproveite melhor o dia $day com mais aulas")
            }

            // Verificar intervalos
            val hasBreak = daySlots.any { it.isBreak }
            if (daySlots.size >= 5 && !hasBreak) {
                warnings.add("$day: Sem intervalo definido")
                suggestions.add("Adicione um intervalo em $day (${daySlots.size} aulas seguidas)")
            }
        }

        // Analisar distribuição semanal
        val totalSlots = schedule.timeSlots.size
        val daysWithClasses = DayOfWeek.weekDays().count { day ->
            schedule.getSlotsByDay(day).isNotEmpty()
        }

        if (daysWithClasses < 5 && totalSlots >= 15) {
            suggestions.add("Distribua as aulas em mais dias da semana")
        }

        // Verificar duplicação de disciplinas
        val subjectCounts = schedule.timeSlots
            .groupingBy { it.subjectId }
            .eachCount()

        subjectCounts.forEach { (subjectId, count) ->
            if (count > 8) { // Mais de 8 aulas/semana da mesma disciplina
                val subject = schedule.timeSlots.find { it.subjectId == subjectId }
                warnings.add("${subject?.subjectName}: Muitas aulas por semana ($count)")
            }
        }

        return ScheduleOptimizationResult(
            suggestions = suggestions,
            warnings = warnings,
            score = calculateOptimizationScore(warnings.size, suggestions.size)
        )
    }

    private fun detectGaps(slots: List<TimeSlot>): List<Gap> {
        if (slots.size < 2) return emptyList()

        val gaps = mutableListOf<Gap>()
        val sortedSlots = slots.sortedBy { it.startTime }

        for (i in 0 until sortedSlots.size - 1) {
            val current = sortedSlots[i]
            val next = sortedSlots[i + 1]

            if (current.endTime != next.startTime) {
                gaps.add(Gap(current.endTime, next.startTime))
            }
        }

        return gaps
    }

    private fun calculateOptimizationScore(warningsCount: Int, suggestionsCount: Int): Int {
        // Score de 0 a 100
        val baseScore = 100
        val warningPenalty = warningsCount * 10
        val suggestionPenalty = suggestionsCount * 5

        return (baseScore - warningPenalty - suggestionPenalty).coerceIn(0, 100)
    }
}

data class Gap(val start: String, val end: String)

data class ScheduleOptimizationResult(
    val suggestions: List<String>,
    val warnings: List<String>,
    val score: Int // 0-100
)

// ==================== GENERATE REPORT ====================

/**
 * Gera relatório detalhado de uma grade
 */
class GenerateScheduleReportUseCase @Inject constructor(
    private val repository: ScheduleRepository
) {
    suspend operator fun invoke(scheduleId: String): Result<ScheduleReport> {
        return try {
            val stats = repository.getScheduleStats(scheduleId)
            val conflicts = repository.detectConflicts(scheduleId)

            // TODO: Coletar mais dados para o relatório

            val report = ScheduleReport(
                scheduleId = scheduleId,
                stats = stats,
                conflicts = conflicts,
                generatedAt = System.currentTimeMillis()
            )

            Result.success(report)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}

data class ScheduleReport(
    val scheduleId: String,
    val stats: ScheduleStats,
    val conflicts: List<ScheduleConflict>,
    val generatedAt: Long
)

// ==================== BULK OPERATIONS ====================

/**
 * Operações em lote para time slots
 */
class BulkTimeSlotOperationsUseCase @Inject constructor(
    private val repository: ScheduleRepository
) {
    /**
     * Adiciona múltiplos slots de uma vez
     */
    suspend fun addMultipleSlots(
        scheduleId: String,
        slots: List<TimeSlot>
    ): Result<BulkOperationResult> {
        val results = mutableListOf<SlotOperationResult>()

        slots.forEach { slot ->
            try {
                // Validar
                if (!slot.isValid()) {
                    results.add(SlotOperationResult.Error(slot.id, "Dados inválidos"))
                    return@forEach
                }

                // Verificar conflito
                val hasConflict = repository.checkSlotConflict(scheduleId, slot)
                if (hasConflict) {
                    results.add(SlotOperationResult.Error(slot.id, "Conflito detectado"))
                    return@forEach
                }

                // Salvar
                repository.saveTimeSlot(slot)
                results.add(SlotOperationResult.Success(slot.id))

            } catch (e: Exception) {
                results.add(SlotOperationResult.Error(slot.id, e.message ?: "Erro desconhecido"))
            }
        }

        val successCount = results.count { it is SlotOperationResult.Success }
        val errorCount = results.count { it is SlotOperationResult.Error }

        return Result.success(
            BulkOperationResult(
                total = slots.size,
                successful = successCount,
                failed = errorCount,
                results = results
            )
        )
    }

    /**
     * Remove múltiplos slots
     */
    suspend fun deleteMultipleSlots(slotIds: List<String>): Result<Int> {
        return try {
            var deletedCount = 0
            slotIds.forEach { id ->
                try {
                    repository.deleteTimeSlot(id)
                    deletedCount++
                } catch (e: Exception) {
                    // Log error but continue
                }
            }
            Result.success(deletedCount)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}

sealed class SlotOperationResult {
    data class Success(val slotId: String) : SlotOperationResult()
    data class Error(val slotId: String, val message: String) : SlotOperationResult()
}

data class BulkOperationResult(
    val total: Int,
    val successful: Int,
    val failed: Int,
    val results: List<SlotOperationResult>
)
