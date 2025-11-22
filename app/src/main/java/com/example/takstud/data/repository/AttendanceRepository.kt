package com.example.takstud.data.repository

import com.example.takstud.model.AttendanceRecord
import com.example.takstud.util.firestoreCollectionFlow
import com.example.takstud.util.firestoreQueryFlow
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AttendanceRepository @Inject constructor(
    private val db: FirebaseFirestore
) {

    fun getAttendanceRecords(): Flow<List<AttendanceRecord>> = firestoreCollectionFlow(
        db.collection("attendance"),
        AttendanceRecord::class.java,
        "TakStud"
    )

    fun getAttendanceRecordsByClassAndDate(classId: String, date: String): Flow<List<AttendanceRecord>> = firestoreQueryFlow(
        db.collection("attendance")
            .whereEqualTo("classId", classId)
            .whereEqualTo("date", date),
        AttendanceRecord::class.java,
        "TakStud"
    )

    fun saveAttendanceRecord(record: AttendanceRecord) {
        val docId = if(record.id.isNotBlank()) record.id else "${record.studentId}-${record.date}"
        db.collection("attendance").document(docId).set(record.copy(id = docId))
    }
}
