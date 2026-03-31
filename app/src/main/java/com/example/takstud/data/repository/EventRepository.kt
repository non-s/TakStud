package com.example.takstud.data.repository

import com.example.takstud.model.EventCalendar
import com.example.takstud.model.EventType
import com.example.takstud.util.firestoreCollectionFlow
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.tasks.await
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class EventRepository @Inject constructor(
    private val db: FirebaseFirestore
) {
    private val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())

    /**
     * Obtém todos os eventos
     */
    fun getEvents(): Flow<List<EventCalendar>> = firestoreCollectionFlow(
        db.collection("events"),
        EventCalendar::class.java,
        "TakStud"
    )

    /**
     * Obtém eventos de um mês específico
     */
    fun getEventsByMonth(year: Int, month: Int): Flow<List<EventCalendar>> =
        getEvents().map { events ->
            events.filter { event ->
                try {
                    val eventDate = dateFormat.parse(event.date)
                    val cal = Calendar.getInstance().apply { time = eventDate }
                    cal.get(Calendar.YEAR) == year && cal.get(Calendar.MONTH) == month
                } catch (e: Exception) {
                    false
                }
            }.sortedBy { dateFormat.parse(it.date)?.time ?: 0 }
        }

    /**
     * Obtém eventos de uma data específica
     */
    fun getEventsByDate(date: String): Flow<List<EventCalendar>> =
        getEvents().map { events ->
            events.filter { it.date == date }
                .sortedBy { it.startTime }
        }

    /**
     * Obtém eventos de uma turma
     */
    fun getEventsByClass(classId: String): Flow<List<EventCalendar>> =
        getEvents().map { events ->
            events.filter { it.studentClass == classId || it.studentClass.isEmpty() }
                .sortedByDescending { it.createdAt }
        }

    /**
     * Obtém eventos por tipo
     */
    fun getEventsByType(type: EventType): Flow<List<EventCalendar>> =
        getEvents().map { events ->
            events.filter { it.eventType == type }
                .sortedBy { dateFormat.parse(it.date)?.time ?: 0 }
        }

    /**
     * Obtém eventos futuros
     */
    fun getUpcomingEvents(limit: Int = 10): Flow<List<EventCalendar>> =
        getEvents().map { events ->
            val now = System.currentTimeMillis()
            events.filter { event ->
                try {
                    val eventDate = dateFormat.parse(event.date)
                    eventDate?.time ?: 0 >= now
                } catch (e: Exception) {
                    false
                }
            }.sortedBy { dateFormat.parse(it.date)?.time ?: 0 }
                .take(limit)
        }

    /**
     * Obtém eventos do usuário (participante ou criador)
     */
    fun getEventsByUser(userId: String): Flow<List<EventCalendar>> =
        getEvents().map { events ->
            events.filter {
                it.createdBy == userId || it.participants.contains(userId)
            }.sortedByDescending { it.createdAt }
        }

    /**
     * Salva ou atualiza um evento
     */
    suspend fun saveEvent(event: EventCalendar): Result<EventCalendar> {
        return try {
            val eventRef = if (event.id.isBlank()) {
                db.collection("events").document()
            } else {
                db.collection("events").document(event.id)
            }

            val updatedEvent = event.copy(
                id = eventRef.id,
                modifiedAt = System.currentTimeMillis(),
                isSynced = true
            )

            eventRef.set(updatedEvent).await()
            Result.success(updatedEvent)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Deleta um evento
     */
    suspend fun deleteEvent(eventId: String): Result<Unit> {
        return try {
            db.collection("events").document(eventId).delete().await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Adiciona participante a um evento
     */
    suspend fun addParticipant(eventId: String, userId: String): Result<Unit> {
        return try {
            val eventDoc = db.collection("events").document(eventId)
            val event = eventDoc.get().await().toObject(EventCalendar::class.java)

            if (event != null && !event.participants.contains(userId)) {
                val updatedParticipants = event.participants + userId
                eventDoc.update("participants", updatedParticipants).await()
            }

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Remove participante de um evento
     */
    suspend fun removeParticipant(eventId: String, userId: String): Result<Unit> {
        return try {
            val eventDoc = db.collection("events").document(eventId)
            val event = eventDoc.get().await().toObject(EventCalendar::class.java)

            if (event != null && event.participants.contains(userId)) {
                val updatedParticipants = event.participants.filter { it != userId }
                eventDoc.update("participants", updatedParticipants).await()
            }

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
