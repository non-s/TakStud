package com.example.takstud.model.schedule

/**
 * ⚠️ Conflito de Horário
 * Representa um conflito detectado na grade horária
 */
data class ScheduleConflict(
    val type: ConflictType,
    val severity: ConflictSeverity,
    val message: String,
    val affectedSlots: List<TimeSlot>,
    val suggestions: List<String> = emptyList()
)

/**
 * Tipos de conflito
 */
enum class ConflictType(val displayName: String) {
    TIME_OVERLAP("Sobreposição de horários"),
    TEACHER_CONFLICT("Professor em dois lugares ao mesmo tempo"),
    CLASSROOM_CONFLICT("Sala ocupada"),
    EXCESSIVE_HOURS("Carga horária excessiva"),
    INSUFFICIENT_BREAK("Intervalo insuficiente"),
    SCHEDULING_GAP("Janela de horário"),
    INVALID_TIME("Horário inválido")
}

/**
 * Severidade do conflito
 */
enum class ConflictSeverity(val displayName: String) {
    CRITICAL("Crítico"),    // Bloqueia a criação
    WARNING("Atenção"),     // Permite mas avisa
    INFO("Informação")      // Apenas informativo
}

/**
 * Detector de conflitos
 */
object ConflictDetector {

    /**
     * Detecta todos os conflitos em uma grade
     */
    fun detectConflicts(schedule: ClassSchedule): List<ScheduleConflict> {
        val conflicts = mutableListOf<ScheduleConflict>()

        // Detectar sobreposições de horário
        conflicts.addAll(detectTimeOverlaps(schedule))

        // Detectar conflitos de professor
        conflicts.addAll(detectTeacherConflicts(schedule))

        // Detectar conflitos de sala
        conflicts.addAll(detectClassroomConflicts(schedule))

        // Detectar carga horária excessiva
        conflicts.addAll(detectExcessiveHours(schedule))

        // Detectar intervalos insuficientes
        conflicts.addAll(detectInsufficientBreaks(schedule))

        return conflicts
    }

    private fun detectTimeOverlaps(schedule: ClassSchedule): List<ScheduleConflict> {
        val conflicts = mutableListOf<ScheduleConflict>()

        DayOfWeek.weekDays().forEach { day ->
            val daySlots = schedule.getSlotsByDay(day)

            for (i in daySlots.indices) {
                for (j in i + 1 until daySlots.size) {
                    if (timesOverlap(daySlots[i], daySlots[j])) {
                        conflicts.add(
                            ScheduleConflict(
                                type = ConflictType.TIME_OVERLAP,
                                severity = ConflictSeverity.CRITICAL,
                                message = "Sobreposição de horários em ${day.shortName}: ${daySlots[i].getTimeRange()} com ${daySlots[j].getTimeRange()}",
                                affectedSlots = listOf(daySlots[i], daySlots[j]),
                                suggestions = listOf(
                                    "Ajuste o horário de uma das aulas",
                                    "Verifique se as aulas podem ser em dias diferentes"
                                )
                            )
                        )
                    }
                }
            }
        }

        return conflicts
    }

    private fun detectTeacherConflicts(schedule: ClassSchedule): List<ScheduleConflict> {
        val conflicts = mutableListOf<ScheduleConflict>()

        // Agrupar slots por professor e horário
        val slotsByTeacherAndTime = schedule.timeSlots
            .filter { it.teacherName.isNotBlank() }
            .groupBy { "${it.teacherName}_${it.dayOfWeek}_${it.startTime}" }

        slotsByTeacherAndTime.forEach { (_, slots) ->
            if (slots.size > 1) {
                conflicts.add(
                    ScheduleConflict(
                        type = ConflictType.TEACHER_CONFLICT,
                        severity = ConflictSeverity.CRITICAL,
                        message = "Professor ${slots[0].teacherName} está agendado em dois lugares ao mesmo tempo",
                        affectedSlots = slots,
                        suggestions = listOf(
                            "Escolha outro professor para uma das turmas",
                            "Ajuste o horário de uma das aulas"
                        )
                    )
                )
            }
        }

        return conflicts
    }

    private fun detectClassroomConflicts(schedule: ClassSchedule): List<ScheduleConflict> {
        val conflicts = mutableListOf<ScheduleConflict>()

        // Agrupar slots por sala e horário
        val slotsByClassroomAndTime = schedule.timeSlots
            .filter { it.classroom.isNotBlank() && !it.classroom.equals("Sala", ignoreCase = true) }
            .groupBy { "${it.classroom}_${it.dayOfWeek}_${it.startTime}" }

        slotsByClassroomAndTime.forEach { (_, slots) ->
            if (slots.size > 1) {
                conflicts.add(
                    ScheduleConflict(
                        type = ConflictType.CLASSROOM_CONFLICT,
                        severity = ConflictSeverity.WARNING,
                        message = "Sala ${slots[0].classroom} está sendo usada por duas turmas ao mesmo tempo",
                        affectedSlots = slots,
                        suggestions = listOf(
                            "Escolha outra sala para uma das turmas",
                            "Ajuste o horário de uma das aulas"
                        )
                    )
                )
            }
        }

        return conflicts
    }

    private fun detectExcessiveHours(schedule: ClassSchedule): List<ScheduleConflict> {
        val conflicts = mutableListOf<ScheduleConflict>()

        DayOfWeek.weekDays().forEach { day ->
            val daySlots = schedule.getSlotsByDay(day)
            val totalMinutes = daySlots.sumOf { it.getDurationMinutes() }
            val totalHours = totalMinutes / 60.0

            if (totalHours > 6) { // Mais de 6 horas/dia
                conflicts.add(
                    ScheduleConflict(
                        type = ConflictType.EXCESSIVE_HOURS,
                        severity = ConflictSeverity.WARNING,
                        message = "Carga horária excessiva em ${day.shortName}: ${"%.1f".format(totalHours)} horas",
                        affectedSlots = daySlots,
                        suggestions = listOf(
                            "Considere redistribuir as aulas ao longo da semana",
                            "Verifique se todos os horários são necessários"
                        )
                    )
                )
            }
        }

        return conflicts
    }

    private fun detectInsufficientBreaks(schedule: ClassSchedule): List<ScheduleConflict> {
        val conflicts = mutableListOf<ScheduleConflict>()

        DayOfWeek.weekDays().forEach { day ->
            val allSlots = schedule.getAllSlotsByDay(day)
            val breaks = allSlots.filter { it.isBreak }

            if (allSlots.size >= 5 && breaks.isEmpty()) {
                conflicts.add(
                    ScheduleConflict(
                        type = ConflictType.INSUFFICIENT_BREAK,
                        severity = ConflictSeverity.WARNING,
                        message = "Nenhum intervalo definido em ${day.shortName} (${allSlots.size} aulas)",
                        affectedSlots = emptyList(),
                        suggestions = listOf(
                            "Adicione um intervalo entre as aulas",
                            "Recomenda-se pelo menos 15-20 minutos de intervalo"
                        )
                    )
                )
            }
        }

        return conflicts
    }

    private fun timesOverlap(slot1: TimeSlot, slot2: TimeSlot): Boolean {
        if (slot1.dayOfWeek != slot2.dayOfWeek) return false

        val start1 = timeToMinutes(slot1.startTime)
        val end1 = timeToMinutes(slot1.endTime)
        val start2 = timeToMinutes(slot2.startTime)
        val end2 = timeToMinutes(slot2.endTime)

        return start1 < end2 && start2 < end1
    }

    private fun timeToMinutes(time: String): Int {
        return try {
            val parts = time.split(":")
            parts[0].toInt() * 60 + parts[1].toInt()
        } catch (e: Exception) {
            0
        }
    }
}
