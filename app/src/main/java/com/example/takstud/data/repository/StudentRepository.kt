package com.example.takstud.data.repository

import com.example.takstud.model.Student
import com.example.takstud.util.firestoreCollectionFlow
import com.example.takstud.util.firestoreQueryFlow
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class StudentRepository @Inject constructor(
    private val db: FirebaseFirestore
) {

    fun getStudents(): Flow<List<Student>> = firestoreCollectionFlow(
        db.collection("students"),
        Student::class.java,
        "TakStud"
    )

    fun getStudentsByClass(classId: String): Flow<List<Student>> = firestoreQueryFlow(
        db.collection("students").whereEqualTo("classId", classId),
        Student::class.java,
        "TakStud"
    )

    fun saveStudent(student: Student, onComplete: () -> Unit) {
        val studentRef = if (student.id.isBlank()) db.collection("students").document() else db.collection("students").document(student.id)
        studentRef.set(student.copy(id = studentRef.id)).addOnSuccessListener {
            onComplete()
        }
    }

    fun deleteStudent(student: Student) {
        db.collection("students").document(student.id).delete()
    }

    suspend fun isParentOfStudent(parentId: String, studentId: String): Boolean = try {
        val relationshipDoc = db.collection("parent_student_relationships")
            .document("${parentId}_${studentId}")
            .get()
            .await()

        if (relationshipDoc.exists()) {
            true
        } else {
            val studentDoc = db.collection("students").document(studentId).get().await()
            val student = studentDoc.toObject(Student::class.java)
            student?.parent == parentId
        }
    } catch (e: Exception) {
        false
    }

    fun getStudentsForParent(parentId: String): Flow<List<Student>> = firestoreQueryFlow(
        db.collection("students").whereEqualTo("parent", parentId),
        Student::class.java,
        "TakStud"
    )

    fun getStudentsForTeacher(teacherId: String, classNames: List<String>): Flow<List<Student>> {
        return if (classNames.isEmpty()) {
            kotlinx.coroutines.flow.flowOf(emptyList())
        } else {
            firestoreQueryFlow(
                db.collection("students").whereIn("studentClass", classNames),
                Student::class.java,
                "TakStud"
            )
        }
    }

    suspend fun createParentStudentRelationship(parentId: String, studentId: String) {
        db.collection("parent_student_relationships")
            .document("${parentId}_${studentId}")
            .set(mapOf(
                "parentId" to parentId,
                "studentId" to studentId,
                "createdAt" to System.currentTimeMillis()
            ))
            .await()
    }

    suspend fun removeParentStudentRelationship(parentId: String, studentId: String) {
        db.collection("parent_student_relationships")
            .document("${parentId}_${studentId}")
            .delete()
            .await()
    }
}
