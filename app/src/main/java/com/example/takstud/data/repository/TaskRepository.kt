package com.example.takstud.data.repository

import com.example.takstud.model.task.TaskExtended
import com.example.takstud.util.firestoreCollectionFlow
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.tasks.await
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

    suspend fun saveTask(task: TaskExtended) {
        val taskRef = if (task.id.isBlank()) db.collection("tasks").document() else db.collection("tasks").document(task.id)
        val taskToSave = task.copy(id = taskRef.id)
        taskRef.set(taskToSave).await()
    }

    suspend fun deleteTask(task: TaskExtended) {
        db.collection("tasks").document(task.id).delete().await()
    }
    
    fun getTaskById(taskId: String): Flow<TaskExtended?> {
        return getTasks().map { tasks -> tasks.find { it.id == taskId } }
    }
}
