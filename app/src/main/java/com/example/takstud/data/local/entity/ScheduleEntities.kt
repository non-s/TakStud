package com.example.takstud.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverter
import androidx.room.TypeConverters
import com.example.takstud.model.Period
import com.example.takstud.model.schedule.*
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

/**
 * 📚 Entidade Subject para Room
 */
@Entity(tableName = "subjects")
@TypeConverters(StringListConverter::class)
data class SubjectEntity(
    @PrimaryKey
    val id: String,
    val name: String,
    val shortName: String,
    val teacherName: String,
    val teacherId: String,
    val classroom: String,
    val color: Long,
    val weeklyHours: Int,
    val description: String,
    val requiredMaterials: List<String>,
    val isActive: Boolean,
    val createdAt: Long,
    val updatedAt: Long
) {
    fun toSubject(): Subject = Subject(
        id = id,
        name = name,
        shortName = shortName,
        teacherName = teacherName,
        teacherId = teacherId,
        classroom = classroom,
        color = color,
        weeklyHours = weeklyHours,
        description = description,
        requiredMaterials = requiredMaterials,
        isActive = isActive,
        createdAt = createdAt,
        updatedAt = updatedAt
    )

    companion object {
        fun fromSubject(subject: Subject): SubjectEntity = SubjectEntity(
            id = subject.id,
            name = subject.name,
            shortName = subject.shortName,
            teacherName = subject.teacherName,
            teacherId = subject.teacherId,
            classroom = subject.classroom,
            color = subject.color,
            weeklyHours = subject.weeklyHours,
            description = subject.description,
            requiredMaterials = subject.requiredMaterials,
            isActive = subject.isActive,
            createdAt = subject.createdAt,
            updatedAt = subject.updatedAt
        )
    }
}

/**
 * ⏰ Entidade TimeSlot para Room
 */
@Entity(tableName = "time_slots")
data class TimeSlotEntity(
    @PrimaryKey
    val id: String,
    val scheduleId: String,
    val dayOfWeek: String,  // Stored as String
    val startTime: String,
    val endTime: String,
    val subjectId: String,
    val subjectName: String,
    val subjectShortName: String,
    val teacherName: String,
    val classroom: String,
    val subjectColor: Long,
    val isBreak: Boolean,
    val isSpecialEvent: Boolean,
    val eventTitle: String,
    val notes: String,
    val isSubstitute: Boolean,
    val substituteTeacher: String,
    val createdAt: Long,
    val updatedAt: Long
) {
    fun toTimeSlot(): TimeSlot = TimeSlot(
        id = id,
        scheduleId = scheduleId,
        dayOfWeek = DayOfWeek.valueOf(dayOfWeek),
        startTime = startTime,
        endTime = endTime,
        subjectId = subjectId,
        subjectName = subjectName,
        subjectShortName = subjectShortName,
        teacherName = teacherName,
        classroom = classroom,
        subjectColor = subjectColor,
        isBreak = isBreak,
        isSpecialEvent = isSpecialEvent,
        eventTitle = eventTitle,
        notes = notes,
        isSubstitute = isSubstitute,
        substituteTeacher = substituteTeacher,
        createdAt = createdAt,
        updatedAt = updatedAt
    )

    companion object {
        fun fromTimeSlot(timeSlot: TimeSlot): TimeSlotEntity = TimeSlotEntity(
            id = timeSlot.id,
            scheduleId = timeSlot.scheduleId,
            dayOfWeek = timeSlot.dayOfWeek.name,
            startTime = timeSlot.startTime,
            endTime = timeSlot.endTime,
            subjectId = timeSlot.subjectId,
            subjectName = timeSlot.subjectName,
            subjectShortName = timeSlot.subjectShortName,
            teacherName = timeSlot.teacherName,
            classroom = timeSlot.classroom,
            subjectColor = timeSlot.subjectColor,
            isBreak = timeSlot.isBreak,
            isSpecialEvent = timeSlot.isSpecialEvent,
            eventTitle = timeSlot.eventTitle,
            notes = timeSlot.notes,
            isSubstitute = timeSlot.isSubstitute,
            substituteTeacher = timeSlot.substituteTeacher,
            createdAt = timeSlot.createdAt,
            updatedAt = timeSlot.updatedAt
        )
    }
}

/**
 * 📅 Entidade ClassSchedule para Room
 */
@Entity(tableName = "class_schedules")
data class ClassScheduleEntity(
    @PrimaryKey
    val id: String,
    val className: String,
    val period: String,  // Stored as String
    val year: Int,
    val semester: Int,
    val schoolStartTime: String,
    val schoolEndTime: String,
    val slotDuration: Int,
    val isTemplate: Boolean,
    val templateName: String,
    val isActive: Boolean,
    val notes: String,
    val createdAt: Long,
    val updatedAt: Long
) {
    fun toClassSchedule(timeSlots: List<TimeSlot>, breaks: List<TimeSlot>): ClassSchedule = ClassSchedule(
        id = id,
        className = className,
        period = Period.valueOf(period),
        year = year,
        semester = semester,
        timeSlots = timeSlots,
        breaks = breaks,
        schoolStartTime = schoolStartTime,
        schoolEndTime = schoolEndTime,
        slotDuration = slotDuration,
        isTemplate = isTemplate,
        templateName = templateName,
        isActive = isActive,
        notes = notes,
        createdAt = createdAt,
        updatedAt = updatedAt
    )

    companion object {
        fun fromClassSchedule(schedule: ClassSchedule): ClassScheduleEntity = ClassScheduleEntity(
            id = schedule.id,
            className = schedule.className,
            period = schedule.period.name,
            year = schedule.year,
            semester = schedule.semester,
            schoolStartTime = schedule.schoolStartTime,
            schoolEndTime = schedule.schoolEndTime,
            slotDuration = schedule.slotDuration,
            isTemplate = schedule.isTemplate,
            templateName = schedule.templateName,
            isActive = schedule.isActive,
            notes = schedule.notes,
            createdAt = schedule.createdAt,
            updatedAt = schedule.updatedAt
        )
    }
}

/**
 * Type Converters para Room
 */
class StringListConverter {
    private val gson = Gson()

    @TypeConverter
    fun fromStringList(value: List<String>): String {
        return gson.toJson(value)
    }

    @TypeConverter
    fun toStringList(value: String): List<String> {
        val listType = object : TypeToken<List<String>>() {}.type
        return gson.fromJson(value, listType) ?: emptyList()
    }
}
