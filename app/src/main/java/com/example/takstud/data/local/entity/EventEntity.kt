package com.example.takstud.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverters
import com.example.takstud.data.local.converters.StringListConverter
import com.example.takstud.model.EventCalendar
import com.example.takstud.model.EventType
import com.example.takstud.model.ReminderTime

/**
 * EventEntity - Entidade Room para cache local de eventos da agenda
 */
@Entity(tableName = "events")
@TypeConverters(StringListConverter::class)
data class EventEntity(
    @PrimaryKey
    val id: String,
    val title: String,
    val description: String,
    val eventType: String, // EventType.name
    val date: String, // formato dd/MM/yyyy
    val startTime: String, // formato HH:mm
    val endTime: String, // formato HH:mm
    val studentClass: String,
    val createdBy: String,
    val participants: List<String>,
    val location: String,
    val color: String,
    val isAllDay: Boolean,
    val reminder: String, // ReminderTime.name
    val createdAt: Long,
    val modifiedAt: Long,
    val isSynced: Boolean,
    val isActive: Boolean = true,
    val lastSyncAt: Long = 0L
)

/**
 * Conversão de EventEntity para EventCalendar (domain model)
 */
fun EventEntity.toDomainModel(): EventCalendar {
    return EventCalendar(
        id = this.id,
        title = this.title,
        description = this.description,
        eventType = try { EventType.valueOf(this.eventType) } catch (e: Exception) { EventType.OTHER },
        date = this.date,
        startTime = this.startTime,
        endTime = this.endTime,
        studentClass = this.studentClass,
        createdBy = this.createdBy,
        participants = this.participants,
        location = this.location,
        color = this.color,
        isAllDay = this.isAllDay,
        reminder = try { ReminderTime.valueOf(this.reminder) } catch (e: Exception) { ReminderTime.NONE },
        createdAt = this.createdAt,
        modifiedAt = this.modifiedAt,
        isSynced = this.isSynced
    )
}

/**
 * Conversão de EventCalendar para EventEntity
 */
fun EventCalendar.toEntity(): EventEntity {
    return EventEntity(
        id = this.id,
        title = this.title,
        description = this.description,
        eventType = this.eventType.name,
        date = this.date,
        startTime = this.startTime,
        endTime = this.endTime,
        studentClass = this.studentClass,
        createdBy = this.createdBy,
        participants = this.participants,
        location = this.location,
        color = this.color,
        isAllDay = this.isAllDay,
        reminder = this.reminder.name,
        createdAt = this.createdAt,
        modifiedAt = this.modifiedAt,
        isSynced = this.isSynced,
        isActive = true,
        lastSyncAt = System.currentTimeMillis()
    )
}
