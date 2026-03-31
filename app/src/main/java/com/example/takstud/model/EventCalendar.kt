package com.example.takstud.model

/**
 * Modelo de Evento para Agenda Digital.
 *
 * Representa eventos no calendário escolar como provas, atividades,
 * reuniões, feriados, e outros eventos importantes.
 */
data class EventCalendar(
    val id: String = "",
    val title: String = "",
    val description: String = "",
    val eventType: EventType = EventType.OTHER,
    val date: String = "", // formato dd/MM/yyyy
    val startTime: String = "", // formato HH:mm
    val endTime: String = "", // formato HH:mm
    val studentClass: String = "", // vazio para eventos gerais
    val createdBy: String = "", // userId do criador
    val participants: List<String> = emptyList(), // lista de userIds
    val location: String = "", // sala, auditório, etc.
    val color: String = "#2196F3", // cor no calendário
    val isAllDay: Boolean = false,
    val reminder: ReminderTime = ReminderTime.NONE,
    val createdAt: Long = System.currentTimeMillis(),
    val modifiedAt: Long = System.currentTimeMillis(),
    val isSynced: Boolean = false
)

/**
 * Tipos de eventos suportados
 */
enum class EventType(val displayName: String) {
    EXAM("Prova"),
    HOMEWORK("Tarefa"),
    MEETING("Reunião"),
    HOLIDAY("Feriado"),
    SCHOOL_EVENT("Evento Escolar"),
    CLASS("Aula"),
    PARENT_MEETING("Reunião de Pais"),
    SPORTS("Esporte"),
    FIELD_TRIP("Excursão"),
    OTHER("Outro")
}

/**
 * Tempo de lembrete antes do evento
 */
enum class ReminderTime(val displayName: String, val minutesBefore: Int) {
    NONE("Sem lembrete", 0),
    AT_TIME("Na hora", 0),
    FIVE_MINUTES("5 minutos antes", 5),
    FIFTEEN_MINUTES("15 minutos antes", 15),
    THIRTY_MINUTES("30 minutos antes", 30),
    ONE_HOUR("1 hora antes", 60),
    ONE_DAY("1 dia antes", 1440),
    TWO_DAYS("2 dias antes", 2880)
}
