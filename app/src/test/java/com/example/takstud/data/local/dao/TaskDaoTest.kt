package com.example.takstud.data.local.dao

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.takstud.data.local.AppDatabase
import com.example.takstud.data.local.entity.TaskEntity
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

/**
 * Testes abrangentes para [TaskDao] usando Room in-memory database.
 *
 * Cobre:
 * - CRUD operations
 * - Queries com filtros (por turma)
 * - Ordenação por data de vencimento
 * - Batch operations
 * - Sync status management
 *
 * @see TaskDao
 * @see TaskEntity
 */
@RunWith(AndroidJUnit4::class)
class TaskDaoTest {

    private lateinit var database: AppDatabase
    private lateinit var taskDao: TaskDao

    @Before
    fun setUp() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        database = Room.inMemoryDatabaseBuilder(
            context,
            AppDatabase::class.java
        ).allowMainThreadQueries().build()

        taskDao = database.taskDao()
    }

    @After
    fun tearDown() {
        database.close()
    }

    // ==================== TESTES DE CRIAÇÃO ====================

    /**
     * Teste: Inserir nova tarefa
     * Esperado: Tarefa é salva com sucesso
     */
    @Test
    fun insertTask_savesSuccessfully() = runBlocking {
        // Arrange
        val task = TaskEntity(
            id = "task_001",
            title = "Math Homework",
            description = "Chapter 5 exercises",
            studentClass = "6A",
            dueDate = System.currentTimeMillis(),
            isSynced = false,
            timestamp = System.currentTimeMillis()
        )

        // Act
        taskDao.insertTask(task)

        // Assert
        val retrieved = taskDao.getTaskById("task_001")
        assertNotNull(retrieved)
        assertEquals("Math Homework", retrieved.title)
    }

    /**
     * Teste: Inserir múltiplas tarefas em batch
     * Esperado: Todas são salvas
     */
    @Test
    fun insertTasks_batch_savesAll() = runBlocking {
        // Arrange
        val tasks = listOf(
            TaskEntity("task_001", "Math", "Chapter 5", "6A", System.currentTimeMillis(), false, System.currentTimeMillis()),
            TaskEntity("task_002", "English", "Essay", "6A", System.currentTimeMillis(), false, System.currentTimeMillis()),
            TaskEntity("task_003", "Science", "Project", "6B", System.currentTimeMillis(), false, System.currentTimeMillis())
        )

        // Act
        taskDao.insertTasks(tasks)

        // Assert
        val allTasks = taskDao.getAllTasks().first()
        assertEquals(3, allTasks.size)
    }

    /**
     * Teste: Inserir com ID duplicado
     * Esperado: OnConflictStrategy.REPLACE substitui a existente
     */
    @Test
    fun insertTask_withDuplicateId_replaces() = runBlocking {
        // Arrange
        val task1 = TaskEntity(
            "task_001",
            "Original Title",
            "Original Description",
            "6A",
            System.currentTimeMillis(),
            false,
            System.currentTimeMillis()
        )
        val task2 = TaskEntity(
            "task_001",
            "Updated Title",
            "Updated Description",
            "6B",
            System.currentTimeMillis(),
            true,
            System.currentTimeMillis()
        )

        // Act
        taskDao.insertTask(task1)
        taskDao.insertTask(task2)

        // Assert
        val retrieved = taskDao.getTaskById("task_001")
        assertNotNull(retrieved)
        assertEquals("Updated Title", retrieved.title)
        assertEquals("6B", retrieved.studentClass)
        assertTrue(retrieved.isSynced)
    }

    // ==================== TESTES DE LEITURA ====================

    /**
     * Teste: Recuperar todas as tarefas
     * Esperado: Flow emite lista completa, ordenada por data DESC
     */
    @Test
    fun getAllTasks_returnsAllOrderedByDueDate() = runBlocking {
        // Arrange
        val now = System.currentTimeMillis()
        val tasks = listOf(
            TaskEntity("task_001", "Task 1", "Desc 1", "6A", now - 2000, false, now - 2000),
            TaskEntity("task_002", "Task 2", "Desc 2", "6A", now - 1000, false, now - 1000),
            TaskEntity("task_003", "Task 3", "Desc 3", "6A", now, false, now)
        )
        taskDao.insertTasks(tasks)

        // Act
        val result = taskDao.getAllTasks().first()

        // Assert
        assertEquals(3, result.size)
        assertEquals("Task 3", result[0].title) // Mais recente primeiro
        assertEquals("Task 2", result[1].title)
        assertEquals("Task 1", result[2].title)
    }

    /**
     * Teste: Recuperar tarefas por turma
     * Esperado: Apenas tarefas dessa turma são retornadas
     */
    @Test
    fun getTasksByClass_filtersCorrectly() = runBlocking {
        // Arrange
        val tasks = listOf(
            TaskEntity("task_001", "Task 1", "Desc 1", "6A", System.currentTimeMillis(), false, System.currentTimeMillis()),
            TaskEntity("task_002", "Task 2", "Desc 2", "6A", System.currentTimeMillis(), false, System.currentTimeMillis()),
            TaskEntity("task_003", "Task 3", "Desc 3", "6B", System.currentTimeMillis(), false, System.currentTimeMillis())
        )
        taskDao.insertTasks(tasks)

        // Act
        val class6ATasks = taskDao.getTasksByClass("6A").first()
        val class6BTasks = taskDao.getTasksByClass("6B").first()

        // Assert
        assertEquals(2, class6ATasks.size)
        assertEquals(1, class6BTasks.size)
        assertTrue(class6ATasks.all { it.studentClass == "6A" })
        assertTrue(class6BTasks.all { it.studentClass == "6B" })
    }

    /**
     * Teste: Recuperar tarefa por ID
     * Esperado: Tarefa correta é retornada
     */
    @Test
    fun getTaskById_returnsCorrectTask() = runBlocking {
        // Arrange
        val task = TaskEntity(
            "task_001",
            "Math Homework",
            "Chapter 5",
            "6A",
            System.currentTimeMillis(),
            false,
            System.currentTimeMillis()
        )
        taskDao.insertTask(task)

        // Act
        val retrieved = taskDao.getTaskById("task_001")

        // Assert
        assertNotNull(retrieved)
        assertEquals("Math Homework", retrieved.title)
        assertEquals("Chapter 5", retrieved.description)
    }

    /**
     * Teste: Recuperar tarefa por ID que não existe
     * Esperado: Retorna null
     */
    @Test
    fun getTaskById_nonExistent_returnsNull() = runBlocking {
        // Act
        val task = taskDao.getTaskById("non_existent_id")

        // Assert
        assertNull(task)
    }

    // ==================== TESTES DE ATUALIZAÇÃO ====================

    /**
     * Teste: Atualizar tarefa existente
     * Esperado: Dados são atualizados
     */
    @Test
    fun updateTask_updatesSuccessfully() = runBlocking {
        // Arrange
        val original = TaskEntity(
            "task_001",
            "Original Title",
            "Original Description",
            "6A",
            System.currentTimeMillis(),
            false,
            System.currentTimeMillis()
        )
        taskDao.insertTask(original)

        // Act
        val updated = original.copy(
            title = "Updated Title",
            description = "Updated Description",
            isSynced = true
        )
        taskDao.updateTask(updated)

        // Assert
        val retrieved = taskDao.getTaskById("task_001")
        assertNotNull(retrieved)
        assertEquals("Updated Title", retrieved.title)
        assertEquals("Updated Description", retrieved.description)
        assertTrue(retrieved.isSynced)
    }

    /**
     * Teste: Marcar tarefas como sincronizadas
     * Esperado: Flag isSynced é atualizada
     */
    @Test
    fun markAsSynced_updatesFlag() = runBlocking {
        // Arrange
        val tasks = listOf(
            TaskEntity("task_001", "Task 1", "Desc 1", "6A", System.currentTimeMillis(), false, System.currentTimeMillis()),
            TaskEntity("task_002", "Task 2", "Desc 2", "6A", System.currentTimeMillis(), false, System.currentTimeMillis())
        )
        taskDao.insertTasks(tasks)

        // Act
        taskDao.markAsSynced(listOf("task_001", "task_002"))

        // Assert
        val task1 = taskDao.getTaskById("task_001")
        val task2 = taskDao.getTaskById("task_002")
        assertTrue(task1?.isSynced ?: false)
        assertTrue(task2?.isSynced ?: false)
    }

    // ==================== TESTES DE DELEÇÃO ====================

    /**
     * Teste: Deletar tarefa específica
     * Esperado: Tarefa é removida
     */
    @Test
    fun deleteTask_removesSuccessfully() = runBlocking {
        // Arrange
        val task = TaskEntity(
            "task_001",
            "Task to delete",
            "Will be deleted",
            "6A",
            System.currentTimeMillis(),
            false,
            System.currentTimeMillis()
        )
        taskDao.insertTask(task)

        // Act
        taskDao.deleteTask(task)

        // Assert
        val retrieved = taskDao.getTaskById("task_001")
        assertNull(retrieved)
    }

    /**
     * Teste: Deletar tarefa por ID
     * Esperado: Tarefa é removida
     */
    @Test
    fun deleteTaskById_removesSuccessfully() = runBlocking {
        // Arrange
        val task = TaskEntity(
            "task_001",
            "Task to delete",
            "Will be deleted",
            "6A",
            System.currentTimeMillis(),
            false,
            System.currentTimeMillis()
        )
        taskDao.insertTask(task)

        // Act
        taskDao.deleteTaskById("task_001")

        // Assert
        val retrieved = taskDao.getTaskById("task_001")
        assertNull(retrieved)
    }

    /**
     * Teste: Deletar todas as tarefas
     * Esperado: Tabela fica vazia
     */
    @Test
    fun deleteAll_removesAll() = runBlocking {
        // Arrange
        val tasks = listOf(
            TaskEntity("task_001", "Task 1", "Desc 1", "6A", System.currentTimeMillis(), false, System.currentTimeMillis()),
            TaskEntity("task_002", "Task 2", "Desc 2", "6A", System.currentTimeMillis(), false, System.currentTimeMillis())
        )
        taskDao.insertTasks(tasks)

        // Act
        taskDao.deleteAll()

        // Assert
        val allTasks = taskDao.getAllTasks().first()
        assertEquals(0, allTasks.size)
    }

    // ==================== TESTES DE SYNC ====================

    /**
     * Teste: Recuperar tarefas não sincronizadas
     * Esperado: Apenas tarefas com isSynced=false são retornadas
     */
    @Test
    fun getUnsyncedTasks_returnsOnlyUnsynced() = runBlocking {
        // Arrange
        val tasks = listOf(
            TaskEntity("task_001", "Task 1", "Desc 1", "6A", System.currentTimeMillis(), false, System.currentTimeMillis()),
            TaskEntity("task_002", "Task 2", "Desc 2", "6A", System.currentTimeMillis(), true, System.currentTimeMillis()),
            TaskEntity("task_003", "Task 3", "Desc 3", "6A", System.currentTimeMillis(), false, System.currentTimeMillis())
        )
        taskDao.insertTasks(tasks)

        // Act
        val unsynced = taskDao.getUnsyncedTasks()

        // Assert
        assertEquals(2, unsynced.size)
        assertTrue(unsynced.all { !it.isSynced })
        assertTrue(unsynced.any { it.id == "task_001" })
        assertTrue(unsynced.any { it.id == "task_003" })
        assertFalse(unsynced.any { it.id == "task_002" })
    }

    /**
     * Teste: Sincronizar tarefas parcialmente
     * Esperado: Apenas as selecionadas são marcadas como sincronizadas
     */
    @Test
    fun markAsSynced_partial_onlySelectedMarked() = runBlocking {
        // Arrange
        val tasks = listOf(
            TaskEntity("task_001", "Task 1", "Desc 1", "6A", System.currentTimeMillis(), false, System.currentTimeMillis()),
            TaskEntity("task_002", "Task 2", "Desc 2", "6A", System.currentTimeMillis(), false, System.currentTimeMillis()),
            TaskEntity("task_003", "Task 3", "Desc 3", "6A", System.currentTimeMillis(), false, System.currentTimeMillis())
        )
        taskDao.insertTasks(tasks)

        // Act
        taskDao.markAsSynced(listOf("task_001", "task_003"))

        // Assert
        val unsynced = taskDao.getUnsyncedTasks()
        assertEquals(1, unsynced.size)
        assertEquals("task_002", unsynced[0].id)
    }

    // ==================== TESTES DE EDGE CASES ====================

    /**
     * Teste: Inserir tarefa com título vazio
     * Esperado: Ainda assim é salva
     */
    @Test
    fun insertTask_withEmptyTitle_stillSaves() = runBlocking {
        // Arrange
        val task = TaskEntity(
            id = "task_001",
            title = "",
            description = "Description",
            studentClass = "6A",
            dueDate = System.currentTimeMillis(),
            isSynced = false,
            timestamp = System.currentTimeMillis()
        )

        // Act
        taskDao.insertTask(task)

        // Assert
        val retrieved = taskDao.getTaskById("task_001")
        assertNotNull(retrieved)
        assertEquals("", retrieved.title)
    }

    /**
     * Teste: Tarefas de múltiplas turmas, ordenadas
     * Esperado: Retorna todas ordenadas por data
     */
    @Test
    fun getAllTasks_multipleSections_orderedCorrectly() = runBlocking {
        // Arrange
        val now = System.currentTimeMillis()
        val tasks = listOf(
            TaskEntity("task_001", "Task 1", "Desc 1", "6A", now - 1000, false, now - 1000),
            TaskEntity("task_002", "Task 2", "Desc 2", "6B", now - 2000, false, now - 2000),
            TaskEntity("task_003", "Task 3", "Desc 3", "6A", now, false, now)
        )
        taskDao.insertTasks(tasks)

        // Act
        val result = taskDao.getAllTasks().first()

        // Assert
        assertEquals(3, result.size)
        assertEquals("Task 3", result[0].title)
        assertEquals("Task 1", result[1].title)
        assertEquals("Task 2", result[2].title)
    }

    /**
     * Teste: Múltiplas operações em sequência
     * Esperado: Todas são executadas corretamente
     */
    @Test
    fun sequentialOperations_allSucceed() = runBlocking {
        // Arrange
        val task = TaskEntity(
            "task_001",
            "Original",
            "Description",
            "6A",
            System.currentTimeMillis(),
            false,
            System.currentTimeMillis()
        )

        // Act
        taskDao.insertTask(task) // Create
        var retrieved = taskDao.getTaskById("task_001") // Read
        assertNotNull(retrieved)

        taskDao.updateTask(task.copy(title = "Updated")) // Update
        retrieved = taskDao.getTaskById("task_001")
        assertEquals("Updated", retrieved?.title)

        taskDao.deleteTask(task) // Delete
        retrieved = taskDao.getTaskById("task_001")
        assertNull(retrieved)

        // Assert
        assertTrue(true)
    }
}
