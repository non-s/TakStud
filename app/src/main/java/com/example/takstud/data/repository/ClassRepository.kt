package com.example.takstud.data.repository

import com.example.takstud.model.Class
import com.example.takstud.util.firestoreCollectionFlow
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ClassRepository @Inject constructor(
    private val db: FirebaseFirestore
) {

    fun getClasses(): Flow<List<Class>> = firestoreCollectionFlow(
        db.collection("classes"),
        Class::class.java,
        "TakStud"
    )

    fun saveClass(schoolClass: Class, onComplete: () -> Unit) {
        val classRef = if (schoolClass.id.isBlank()) db.collection("classes").document() else db.collection("classes").document(schoolClass.id)
        classRef.set(schoolClass.copy(id = classRef.id, createdAt = System.currentTimeMillis())).addOnSuccessListener {
            onComplete()
        }
    }

    fun deleteClass(schoolClass: Class) {
        db.collection("classes").document(schoolClass.id).delete()
    }
}
