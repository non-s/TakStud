package com.example.takstud.data.remote

import com.example.takstud.model.schedule.*
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

/**
 * 🔥 Firebase Service para Horários
 * - Sincronização em tempo real
 * - Backup na nuvem
 * - Compartilhamento entre dispositivos
 */
@Singleton
class FirebaseScheduleService @Inject constructor(
    private val firestore: FirebaseFirestore
) {

    companion object {
        private const val SUBJECTS_COLLECTION = "subjects"
        private const val SCHEDULES_COLLECTION = "class_schedules"
        private const val TIME_SLOTS_COLLECTION = "time_slots"
        private const val TEMPLATES_COLLECTION = "schedule_templates"
    }

    // ==================== SUBJECTS ====================

    /**
     * Obter todas as disciplinas (real-time)
     */
    fun getSubjects(schoolId: String): Flow<List<Subject>> = callbackFlow {
        val listener = firestore.collection(SUBJECTS_COLLECTION)
            .whereEqualTo("schoolId", schoolId)
            .whereEqualTo("isActive", true)
            .orderBy("name", Query.Direction.ASCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }

                val subjects = snapshot?.documents?.mapNotNull { doc ->
                    doc.toObject(SubjectFirestore::class.java)?.toDomain()
                } ?: emptyList()

                trySend(subjects)
            }

        awaitClose { listener.remove() }
    }

    /**
     * Salvar disciplina
     */
    suspend fun saveSubject(schoolId: String, subject: Subject) {
        val firestoreSubject = SubjectFirestore.fromDomain(subject, schoolId)

        firestore.collection(SUBJECTS_COLLECTION)
            .document(subject.id)
            .set(firestoreSubject)
            .await()
    }

    /**
     * Deletar disciplina
     */
    suspend fun deleteSubject(subjectId: String) {
        firestore.collection(SUBJECTS_COLLECTION)
            .document(subjectId)
            .delete()
            .await()
    }

    // ==================== SCHEDULES ====================

    /**
     * Obter todas as grades (real-time)
     */
    fun getSchedules(schoolId: String): Flow<List<ClassSchedule>> = callbackFlow {
        val listener = firestore.collection(SCHEDULES_COLLECTION)
            .whereEqualTo("schoolId", schoolId)
            .whereEqualTo("isActive", true)
            .whereEqualTo("isTemplate", false)
            .orderBy("className", Query.Direction.ASCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }

                val schedules = snapshot?.documents?.mapNotNull { doc ->
                    doc.toObject(ClassScheduleFirestore::class.java)?.toDomain()
                } ?: emptyList()

                trySend(schedules)
            }

        awaitClose { listener.remove() }
    }

    /**
     * Obter grade por ID (real-time)
     */
    fun getScheduleById(scheduleId: String): Flow<ClassSchedule?> = callbackFlow {
        val scheduleListener = firestore.collection(SCHEDULES_COLLECTION)
            .document(scheduleId)
            .addSnapshotListener { scheduleDoc, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }

                val schedule = scheduleDoc?.toObject(ClassScheduleFirestore::class.java)?.toDomain()

                // Se temos a grade, buscar os time slots
                if (schedule != null) {
                    // Buscar time slots separadamente
                    // (Outra opção seria usar subcoleção)
                    trySend(schedule)
                } else {
                    trySend(null)
                }
            }

        awaitClose { scheduleListener.remove() }
    }

    /**
     * Salvar grade completa
     */
    suspend fun saveSchedule(schoolId: String, schedule: ClassSchedule) {
        val batch = firestore.batch()

        // Salvar a grade
        val scheduleRef = firestore.collection(SCHEDULES_COLLECTION).document(schedule.id)
        val firestoreSchedule = ClassScheduleFirestore.fromDomain(schedule, schoolId)
        batch.set(scheduleRef, firestoreSchedule)

        // Deletar time slots antigos
        val oldSlots = firestore.collection(TIME_SLOTS_COLLECTION)
            .whereEqualTo("scheduleId", schedule.id)
            .get()
            .await()

        oldSlots.documents.forEach { doc ->
            batch.delete(doc.reference)
        }

        // Adicionar novos time slots
        (schedule.timeSlots + schedule.breaks).forEach { slot ->
            val slotRef = firestore.collection(TIME_SLOTS_COLLECTION).document(slot.id)
            val firestoreSlot = TimeSlotFirestore.fromDomain(slot, schoolId)
            batch.set(slotRef, firestoreSlot)
        }

        // Executar batch
        batch.commit().await()
    }

    /**
     * Deletar grade
     */
    suspend fun deleteSchedule(scheduleId: String) {
        val batch = firestore.batch()

        // Deletar grade
        val scheduleRef = firestore.collection(SCHEDULES_COLLECTION).document(scheduleId)
        batch.delete(scheduleRef)

        // Deletar time slots
        val slots = firestore.collection(TIME_SLOTS_COLLECTION)
            .whereEqualTo("scheduleId", scheduleId)
            .get()
            .await()

        slots.documents.forEach { doc ->
            batch.delete(doc.reference)
        }

        batch.commit().await()
    }

    // ==================== TIME SLOTS ====================

    /**
     * Obter time slots de uma grade (real-time)
     */
    fun getTimeSlots(scheduleId: String): Flow<List<TimeSlot>> = callbackFlow {
        val listener = firestore.collection(TIME_SLOTS_COLLECTION)
            .whereEqualTo("scheduleId", scheduleId)
            .orderBy("dayOfWeek", Query.Direction.ASCENDING)
            .orderBy("startTime", Query.Direction.ASCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }

                val slots = snapshot?.documents?.mapNotNull { doc ->
                    doc.toObject(TimeSlotFirestore::class.java)?.toDomain()
                } ?: emptyList()

                trySend(slots)
            }

        awaitClose { listener.remove() }
    }

    /**
     * Salvar time slot
     */
    suspend fun saveTimeSlot(schoolId: String, timeSlot: TimeSlot) {
        val firestoreSlot = TimeSlotFirestore.fromDomain(timeSlot, schoolId)

        firestore.collection(TIME_SLOTS_COLLECTION)
            .document(timeSlot.id)
            .set(firestoreSlot)
            .await()
    }

    /**
     * Deletar time slot
     */
    suspend fun deleteTimeSlot(timeSlotId: String) {
        firestore.collection(TIME_SLOTS_COLLECTION)
            .document(timeSlotId)
            .delete()
            .await()
    }

    // ==================== TEMPLATES ====================

    /**
     * Obter templates
     */
    fun getTemplates(schoolId: String): Flow<List<ClassSchedule>> = callbackFlow {
        val listener = firestore.collection(SCHEDULES_COLLECTION)
            .whereEqualTo("schoolId", schoolId)
            .whereEqualTo("isTemplate", true)
            .orderBy("templateName", Query.Direction.ASCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }

                val templates = snapshot?.documents?.mapNotNull { doc ->
                    doc.toObject(ClassScheduleFirestore::class.java)?.toDomain()
                } ?: emptyList()

                trySend(templates)
            }

        awaitClose { listener.remove() }
    }
}

// ==================== FIRESTORE MODELS ====================

/**
 * Modelo Firestore para Subject
 */
data class SubjectFirestore(
    val id: String = "",
    val schoolId: String = "",
    val name: String = "",
    val shortName: String = "",
    val teacherName: String = "",
    val teacherId: String = "",
    val classroom: String = "",
    val color: Long = 0L,
    val weeklyHours: Int = 0,
    val description: String = "",
    val requiredMaterials: List<String> = emptyList(),
    val isActive: Boolean = true,
    val createdAt: Long = 0L,
    val updatedAt: Long = 0L
) {
    fun toDomain(): Subject = Subject(
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
        fun fromDomain(subject: Subject, schoolId: String): SubjectFirestore = SubjectFirestore(
            id = subject.id,
            schoolId = schoolId,
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
 * Modelo Firestore para TimeSlot
 */
data class TimeSlotFirestore(
    val id: String = "",
    val schoolId: String = "",
    val scheduleId: String = "",
    val dayOfWeek: String = "",
    val startTime: String = "",
    val endTime: String = "",
    val subjectId: String = "",
    val subjectName: String = "",
    val subjectShortName: String = "",
    val teacherName: String = "",
    val classroom: String = "",
    val subjectColor: Long = 0L,
    val isBreak: Boolean = false,
    val isSpecialEvent: Boolean = false,
    val eventTitle: String = "",
    val notes: String = "",
    val isSubstitute: Boolean = false,
    val substituteTeacher: String = "",
    val createdAt: Long = 0L,
    val updatedAt: Long = 0L
) {
    fun toDomain(): TimeSlot = TimeSlot(
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
        fun fromDomain(timeSlot: TimeSlot, schoolId: String): TimeSlotFirestore = TimeSlotFirestore(
            id = timeSlot.id,
            schoolId = schoolId,
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
 * Modelo Firestore para ClassSchedule
 */
data class ClassScheduleFirestore(
    val id: String = "",
    val schoolId: String = "",
    val className: String = "",
    val period: String = "",
    val year: Int = 0,
    val semester: Int = 0,
    val schoolStartTime: String = "",
    val schoolEndTime: String = "",
    val slotDuration: Int = 0,
    val isTemplate: Boolean = false,
    val templateName: String = "",
    val isActive: Boolean = true,
    val notes: String = "",
    val createdAt: Long = 0L,
    val updatedAt: Long = 0L
) {
    fun toDomain(): ClassSchedule = ClassSchedule(
        id = id,
        className = className,
        period = com.example.takstud.model.Period.valueOf(period),
        year = year,
        semester = semester,
        timeSlots = emptyList(), // Carregado separadamente
        breaks = emptyList(), // Carregado separadamente
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
        fun fromDomain(schedule: ClassSchedule, schoolId: String): ClassScheduleFirestore = ClassScheduleFirestore(
            id = schedule.id,
            schoolId = schoolId,
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
