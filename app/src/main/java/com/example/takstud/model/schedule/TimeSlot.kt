package com.example.takstud.model.schedule

import java.util.UUID

/**
 * ⏰ Slot de Horário
 * Representa um horário específico na grade (ex: Segunda 07:00-07:50)
 */
data class TimeSlot(
    val id: String = UUID.randomUUID().toString(),
    val scheduleId: String = "",              // ID da grade que pertence
    val dayOfWeek: DayOfWeek = DayOfWeek.MONDAY, // Dia da semana
    val startTime: String = "",               // Hora de início (formato HH:mm)
    val endTime: String = "",                 // Hora de fim (formato HH:mm)
    val subjectId: String = "",               // ID da disciplina
    val subjectName: String = "",             // Nome da disciplina (cache)
    val subjectShortName: String = "",        // Nome curto (cache)
    val teacherName: String = "",             // Nome do professor (cache)
    val classroom: String = "",               // Sala/local (cache)
    val subjectColor: Long = 0L,              // Cor da disciplina (cache)
    val isBreak: Boolean = false,            // É intervalo/recreio?
    val isSpecialEvent: Boolean = false,     // Evento especial?
    val eventTitle: String = "",              // Título do evento (se especial)
    val notes: String = "",                   // Observações
    val isSubstitute: Boolean = false,       // Professor substituto?
    val substituteTeacher: String = "",       // Nome do substituto
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
) {
    /**
     * Retorna descrição do horário
     */
    fun getTimeRange(): String = "$startTime - $endTime"

    /**
     * Retorna duração em minutos
     */
    fun getDurationMinutes(): Int {
        return try {
            val start = startTime.split(":").map { it.toInt() }
            val end = endTime.split(":").map { it.toInt() }
            val startMinutes = start[0] * 60 + start[1]
            val endMinutes = end[0] * 60 + end[1]
            endMinutes - startMinutes
        } catch (e: Exception) {
            50 // padrão 50 minutos
        }
    }

    /**
     * Validação
     */
    fun isValid(): Boolean {
        if (isBreak) return startTime.isNotBlank() && endTime.isNotBlank()
        return startTime.isNotBlank() &&
               endTime.isNotBlank() &&
               (subjectId.isNotBlank() || isSpecialEvent)
    }
}

/**
 * Enum para dias da semana
 */
enum class DayOfWeek(val displayName: String, val shortName: String) {
    MONDAY("Segunda-feira", "SEG"),
    TUESDAY("Terça-feira", "TER"),
    WEDNESDAY("Quarta-feira", "QUA"),
    THURSDAY("Quinta-feira", "QUI"),
    FRIDAY("Sexta-feira", "SEX"),
    SATURDAY("Sábado", "SÁB"),
    SUNDAY("Domingo", "DOM");

    companion object {
        /**
         * Dias úteis (segunda a sexta)
         */
        fun weekDays(): List<DayOfWeek> = listOf(MONDAY, TUESDAY, WEDNESDAY, THURSDAY, FRIDAY)

        /**
         * Todos os dias
         */
        fun allDays(): List<DayOfWeek> = values().toList()
    }
}
