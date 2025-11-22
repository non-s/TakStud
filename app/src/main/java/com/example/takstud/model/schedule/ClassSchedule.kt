package com.example.takstud.model.schedule

import com.example.takstud.model.Period
import java.util.UUID

/**
 * 📅 Grade Horária Completa
 * Representa a grade horária de uma turma
 */
data class ClassSchedule(
    val id: String = UUID.randomUUID().toString(),
    val className: String = "",               // Nome da turma (ex: "1º A")
    val period: Period = Period.MANHA,       // Período (Manhã, Tarde, EJA)
    val year: Int = 0,                       // Ano letivo
    val semester: Int = 1,                   // Semestre (1 ou 2)
    val timeSlots: List<TimeSlot> = emptyList(), // Slots de horário
    val breaks: List<TimeSlot> = emptyList(),    // Intervalos/recreios
    val schoolStartTime: String = "07:00",   // Horário de início das aulas
    val schoolEndTime: String = "12:00",     // Horário de término das aulas
    val slotDuration: Int = 50,              // Duração padrão de cada aula (minutos)
    val isTemplate: Boolean = false,         // É um template?
    val templateName: String = "",           // Nome do template
    val isActive: Boolean = true,            // Grade ativa?
    val notes: String = "",                  // Observações gerais
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
) {
    /**
     * Retorna slots organizados por dia da semana
     */
    fun getSlotsByDay(day: DayOfWeek): List<TimeSlot> {
        return timeSlots
            .filter { it.dayOfWeek == day }
            .sortedBy { it.startTime }
    }

    /**
     * Retorna todos os slots de um dia (incluindo intervalos)
     */
    fun getAllSlotsByDay(day: DayOfWeek): List<TimeSlot> {
        val daySlots = getSlotsByDay(day)
        val dayBreaks = breaks.filter { it.dayOfWeek == day }
        return (daySlots + dayBreaks).sortedBy { it.startTime }
    }

    /**
     * Verifica se há conflito de horário
     */
    fun hasConflict(newSlot: TimeSlot): Boolean {
        val sameDaySlots = getSlotsByDay(newSlot.dayOfWeek)
        return sameDaySlots.any { existing ->
            existing.id != newSlot.id && timesOverlap(existing, newSlot)
        }
    }

    /**
     * Verifica se dois slots se sobrepõem
     */
    private fun timesOverlap(slot1: TimeSlot, slot2: TimeSlot): Boolean {
        val start1 = timeToMinutes(slot1.startTime)
        val end1 = timeToMinutes(slot1.endTime)
        val start2 = timeToMinutes(slot2.startTime)
        val end2 = timeToMinutes(slot2.endTime)

        return start1 < end2 && start2 < end1
    }

    /**
     * Converte horário HH:mm para minutos
     */
    private fun timeToMinutes(time: String): Int {
        return try {
            val parts = time.split(":")
            parts[0].toInt() * 60 + parts[1].toInt()
        } catch (e: Exception) {
            0
        }
    }

    /**
     * Retorna estatísticas da grade
     */
    fun getStats(): ScheduleStats {
        val totalSlots = timeSlots.size
        val uniqueSubjects = timeSlots.map { it.subjectId }.distinct().size
        val totalWeeklyHours = timeSlots.sumOf { it.getDurationMinutes() } / 60.0
        val daysWithClasses = timeSlots.map { it.dayOfWeek }.distinct().size

        return ScheduleStats(
            totalSlots = totalSlots,
            uniqueSubjects = uniqueSubjects,
            totalWeeklyHours = totalWeeklyHours,
            daysWithClasses = daysWithClasses
        )
    }

    /**
     * Cria cópia desta grade para outra turma
     */
    fun copyToClass(newClassName: String): ClassSchedule {
        return this.copy(
            id = UUID.randomUUID().toString(),
            className = newClassName,
            timeSlots = timeSlots.map { it.copy(id = UUID.randomUUID().toString()) },
            breaks = breaks.map { it.copy(id = UUID.randomUUID().toString()) },
            isTemplate = false,
            createdAt = System.currentTimeMillis(),
            updatedAt = System.currentTimeMillis()
        )
    }

    /**
     * Converte para template
     */
    fun toTemplate(name: String): ClassSchedule {
        return this.copy(
            id = UUID.randomUUID().toString(),
            isTemplate = true,
            templateName = name,
            createdAt = System.currentTimeMillis(),
            updatedAt = System.currentTimeMillis()
        )
    }

    /**
     * Validação
     */
    fun isValid(): Boolean {
        return className.isNotBlank() &&
               year > 0 &&
               schoolStartTime.isNotBlank() &&
               schoolEndTime.isNotBlank()
    }
}

/**
 * Estatísticas da grade horária
 */
data class ScheduleStats(
    val totalSlots: Int = 0,
    val uniqueSubjects: Int = 0,
    val totalWeeklyHours: Double = 0.0,
    val daysWithClasses: Int = 0
)
