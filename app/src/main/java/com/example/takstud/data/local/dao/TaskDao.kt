package com.example.takstud.data.local.dao

import androidx.room.*
import com.example.takstud.data.local.entity.TaskEntity
import kotlinx.coroutines.flow.Flow

/**
 * TaskDao - Data Access Object para operações de tarefas no Room
 */
@Dao
interface TaskDao {

    /**
     * Obter todas as tarefas como Flow para observar mudanças
     */
    @Query("SELECT * FROM tasks ORDER BY dueDate DESC")
    fun getAllTasks(): Flow<List<TaskEntity>>

    /**
     * Obter tarefas por turma
     */
    @Query("SELECT * FROM tasks WHERE studentClass = :studentClass ORDER BY dueDate DESC")
    fun getTasksByClass(studentClass: String): Flow<List<TaskEntity>>

    /**
     * Obter uma tarefa específica
     */
    @Query("SELECT * FROM tasks WHERE id = :taskId")
    suspend fun getTaskById(taskId: String): TaskEntity?

    /**
     * Inserir tarefa
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTask(task: TaskEntity)

    /**
     * Inserir múltiplas tarefas
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTasks(tasks: List<TaskEntity>)

    /**
     * Atualizar tarefa
     */
    @Update
    suspend fun updateTask(task: TaskEntity)

    /**
     * Deletar tarefa
     */
    @Delete
    suspend fun deleteTask(task: TaskEntity)

    /**
     * Deletar tarefa por ID
     */
    @Query("DELETE FROM tasks WHERE id = :taskId")
    suspend fun deleteTaskById(taskId: String)

    /**
     * Marcar tarefas como sincronizadas
     */
    @Query("UPDATE tasks SET isSynced = 1 WHERE id IN (:taskIds)")
    suspend fun markAsSynced(taskIds: List<String>)

    /**
     * Obter tarefas não sincronizadas
     */
    @Query("SELECT * FROM tasks WHERE isSynced = 0")
    suspend fun getUnsyncedTasks(): List<TaskEntity>

    /**
     * Limpar todas as tarefas
     */
    @Query("DELETE FROM tasks")
    suspend fun deleteAll()
}
