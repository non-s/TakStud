package com.example.takstud.data.repository

import com.example.takstud.model.task.TaskExtended
import com.example.takstud.util.firestoreCollectionFlow
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TaskRepository @Inject constructor(
    private val db: FirebaseFirestore
) {

    fun getTasks(): Flow<List<TaskExtended>> = firestoreCollectionFlow(
        db.collection("tasks"),
        TaskExtended::class.java,
        "TakStud"
    )

    fun saveTask(task: TaskExtended, onComplete: () -> Unit) {
        val taskRef = if (task.id.isBlank()) db.collection("tasks").document() else db.collection("tasks").document(task.id)
        val taskToSave = task.copy(id = taskRef.id)
        taskRef.set(taskToSave).addOnSuccessListener {
            onComplete()
        }
    }

    fun deleteTask(task: TaskExtended) {
        db.collection("tasks").document(task.id).delete()
    }
    
    fun getTaskById(taskId: String): Flow<TaskExtended?> {
        // Implementação simples de flow para um único documento seria ideal, 
        // mas por enquanto vamos filtrar do getTasks ou implementar um utilitário novo.
        // Para simplificar e manter consistência com o padrão atual:
        return com.example.takstud.util.firestoreDocumentFlow(
            db.collection("tasks").document(taskId),
            TaskExtended::class.java
        )
    }
}
