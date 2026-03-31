package com.example.takstud.data.repository

import com.example.takstud.model.Schedule
import com.example.takstud.util.firestoreCollectionFlow
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ScheduleRepository @Inject constructor(
    private val db: FirebaseFirestore
) {

    fun getSchedules(): Flow<List<Schedule>> = firestoreCollectionFlow(
        db.collection("schedules"),
        Schedule::class.java,
        "TakStud"
    )

    fun getClassesByPeriod(): Flow<Map<String, List<String>>> =
        getSchedules().map { schedules ->
            schedules
                .groupBy { it.periodo.name }
                .mapValues { (_, scheduleList) ->
                    scheduleList
                        .map { it.studentClass }
                        .distinct()
                        .sorted()
                }
        }

    fun saveSchedule(schedule: Schedule, onComplete: () -> Unit) {
        val docId = if (schedule.id.isNotBlank()) schedule.id else "${schedule.studentClass.replace(" ", "")}-${schedule.periodo}"
        db.collection("schedules").document(docId).set(schedule.copy(id = docId)).addOnSuccessListener {
            onComplete()
        }
    }

    fun deleteSchedule(schedule: Schedule) {
        db.collection("schedules").document(schedule.id).delete()
    }
}
