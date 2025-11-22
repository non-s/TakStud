package com.example.takstud.data.repository

import com.example.takstud.model.Grade
import com.example.takstud.util.firestoreCollectionFlow
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class GradeRepository @Inject constructor(
    private val db: FirebaseFirestore
) {

    fun getGrades(): Flow<List<Grade>> = firestoreCollectionFlow(
        db.collection("grades"),
        Grade::class.java,
        "TakStud"
    )

    fun saveGrade(grade: Grade) {
        val docId = if(grade.id.isNotBlank()) grade.id else "${grade.taskId}-${grade.studentId}"
        db.collection("grades").document(docId).set(grade.copy(id = docId))
    }
}
