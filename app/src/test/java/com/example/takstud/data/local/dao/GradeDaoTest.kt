package com.example.takstud.data.local.dao

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.takstud.data.local.AppDatabase
import com.example.takstud.data.local.entity.GradeEntity
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
 * Testes abrangentes para [GradeDao] usando Room in-memory database.
 *
 * Cobre:
 * - CRUD operations
 * - Queries com filtros (por tarefa, estudante)
 * - Ordenação por timestamp
 * - Batch operations
 * - Sync status management
 *
 * @see GradeDao
 * @see GradeEntity
 */
@RunWith(AndroidJUnit4::class)
class GradeDaoTest {

    private lateinit var database: AppDatabase
    private lateinit var gradeDao: GradeDao

    @Before
    fun setUp() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        database = Room.inMemoryDatabaseBuilder(
            context,
            AppDatabase::class.java
        ).allowMainThreadQueries().build()

        gradeDao = database.gradeDao()
    }

    @After
    fun tearDown() {
        database.close()
    }

    // ==================== TESTES DE CRIAÇÃO ====================

    /**
     * Teste: Inserir uma nova nota
     * Esperado: Nota é salva com sucesso
     */
    @Test
    fun insertGrade_savesSuccessfully() = runBlocking {
        // Arrange
        val grade = GradeEntity(
            id = "task_001-student_001",
            taskId = "task_001",
            studentId = "student_001",
            score = 85,
            isSynced = false,
            timestamp = System.currentTimeMillis()
        )

        // Act
        gradeDao.insertGrade(grade)

        // Assert
        val retrieved = gradeDao.getGradeById("task_001-student_001")
        assertNotNull(retrieved)
        assertEquals(85, retrieved.score)
    }

    /**
     * Teste: Inserir múltiplas notas em batch
     * Esperado: Todas são salvas
     */
    @Test
    fun insertGrades_batch_savesAll() = runBlocking {
        // Arrange
        val grades = listOf(
            GradeEntity("task_001-student_001", "task_001", "student_001", 85, false, System.currentTimeMillis()),
            GradeEntity("task_001-student_002", "task_001", "student_002", 90, false, System.currentTimeMillis()),
            GradeEntity("task_002-student_001", "task_002", "student_001", 78, false, System.currentTimeMillis())
        )

        // Act
        gradeDao.insertGrades(grades)

        // Assert
        val allGrades = gradeDao.getAllGrades().first()
        assertEquals(3, allGrades.size)
    }

    /**
     * Teste: Inserir nota com ID duplicado
     * Esperado: OnConflictStrategy.REPLACE substitui a existente
     */
    @Test
    fun insertGrade_withDuplicateId_replaces() = runBlocking {
        // Arrange
        val grade1 = GradeEntity(
            "task_001-student_001",
            "task_001",
            "student_001",
            85,
            false,
            System.currentTimeMillis()
        )
        val grade2 = GradeEntity(
            "task_001-student_001",
            "task_001",
            "student_001",
            95,
            true,
            System.currentTimeMillis()
        )

        // Act
        gradeDao.insertGrade(grade1)
        gradeDao.insertGrade(grade2)

        // Assert
        val retrieved = gradeDao.getGradeById("task_001-student_001")
        assertNotNull(retrieved)
        assertEquals(95, retrieved.score)
        assertTrue(retrieved.isSynced)
    }

    // ==================== TESTES DE LEITURA ====================

    /**
     * Teste: Recuperar todas as notas
     * Esperado: Flow emite lista completa, ordenada por timestamp DESC
     */
    @Test
    fun getAllGrades_returnsAllOrderedByTimestamp() = runBlocking {
        // Arrange
        val now = System.currentTimeMillis()
        val grades = listOf(
            GradeEntity("grade_001", "task_001", "student_001", 85, false, now - 2000),
            GradeEntity("grade_002", "task_002", "student_001", 90, false, now - 1000),
            GradeEntity("grade_003", "task_003", "student_001", 78, false, now)
        )
        gradeDao.insertGrades(grades)

        // Act
        val result = gradeDao.getAllGrades().first()

        // Assert
        assertEquals(3, result.size)
        assertEquals(78, result[0].score) // Mais recente primeiro
        assertEquals(90, result[1].score)
        assertEquals(85, result[2].score)
    }

    /**
     * Teste: Recuperar notas de uma tarefa específica
     * Esperado: Apenas notas dessa tarefa são retornadas
     */
    @Test
    fun getGradesByTask_filtersCorrectly() = runBlocking {
        // Arrange
        val grades = listOf(
            GradeEntity("task_001-student_001", "task_001", "student_001", 85, false, System.currentTimeMillis()),
            GradeEntity("task_001-student_002", "task_001", "student_002", 90, false, System.currentTimeMillis()),
            GradeEntity("task_002-student_001", "task_002", "student_001", 78, false, System.currentTimeMillis())
        )
        gradeDao.insertGrades(grades)

        // Act
        val task1Grades = gradeDao.getGradesByTask("task_001").first()
        val task2Grades = gradeDao.getGradesByTask("task_002").first()

        // Assert
        assertEquals(2, task1Grades.size)
        assertEquals(1, task2Grades.size)
        assertTrue(task1Grades.all { it.taskId == "task_001" })
        assertTrue(task2Grades.all { it.taskId == "task_002" })
    }

    /**
     * Teste: Recuperar notas de um estudante específico
     * Esperado: Apenas notas desse estudante são retornadas
     */
    @Test
    fun getGradesByStudent_filtersCorrectly() = runBlocking {
        // Arrange
        val grades = listOf(
            GradeEntity("task_001-student_001", "task_001", "student_001", 85, false, System.currentTimeMillis()),
            GradeEntity("task_002-student_001", "task_002", "student_001", 90, false, System.currentTimeMillis()),
            GradeEntity("task_001-student_002", "task_001", "student_002", 78, false, System.currentTimeMillis())
        )
        gradeDao.insertGrades(grades)

        // Act
        val student1Grades = gradeDao.getGradesByStudent("student_001").first()
        val student2Grades = gradeDao.getGradesByStudent("student_002").first()

        // Assert
        assertEquals(2, student1Grades.size)
        assertEquals(1, student2Grades.size)
        assertTrue(student1Grades.all { it.studentId == "student_001" })
        assertTrue(student2Grades.all { it.studentId == "student_002" })
    }

    /**
     * Teste: Recuperar nota por ID
     * Esperado: Nota correta é retornada
     */
    @Test
    fun getGradeById_returnsCorrectGrade() = runBlocking {
        // Arrange
        val grade = GradeEntity(
            "task_001-student_001",
            "task_001",
            "student_001",
            85,
            false,
            System.currentTimeMillis()
        )
        gradeDao.insertGrade(grade)

        // Act
        val retrieved = gradeDao.getGradeById("task_001-student_001")

        // Assert
        assertNotNull(retrieved)
        assertEquals(85, retrieved.score)
        assertEquals("task_001", retrieved.taskId)
    }

    /**
     * Teste: Recuperar nota por ID que não existe
     * Esperado: Retorna null
     */
    @Test
    fun getGradeById_nonExistent_returnsNull() = runBlocking {
        // Act
        val grade = gradeDao.getGradeById("non_existent_id")

        // Assert
        assertNull(grade)
    }

    // ==================== TESTES DE ATUALIZAÇÃO ====================

    /**
     * Teste: Atualizar nota existente
     * Esperado: Dados são atualizados
     */
    @Test
    fun updateGrade_updatesSuccessfully() = runBlocking {
        // Arrange
        val original = GradeEntity(
            "task_001-student_001",
            "task_001",
            "student_001",
            85,
            false,
            System.currentTimeMillis()
        )
        gradeDao.insertGrade(original)

        // Act
        val updated = original.copy(score = 95, isSynced = true)
        gradeDao.updateGrade(updated)

        // Assert
        val retrieved = gradeDao.getGradeById("task_001-student_001")
        assertNotNull(retrieved)
        assertEquals(95, retrieved.score)
        assertTrue(retrieved.isSynced)
    }

    /**
     * Teste: Marcar notas como sincronizadas
     * Esperado: Flag isSynced é atualizada
     */
    @Test
    fun markAsSynced_updatesFlag() = runBlocking {
        // Arrange
        val grades = listOf(
            GradeEntity("grade_001", "task_001", "student_001", 85, false, System.currentTimeMillis()),
            GradeEntity("grade_002", "task_002", "student_001", 90, false, System.currentTimeMillis())
        )
        gradeDao.insertGrades(grades)

        // Act
        gradeDao.markAsSynced(listOf("grade_001", "grade_002"))

        // Assert
        val grade1 = gradeDao.getGradeById("grade_001")
        val grade2 = gradeDao.getGradeById("grade_002")
        assertTrue(grade1?.isSynced ?: false)
        assertTrue(grade2?.isSynced ?: false)
    }

    // ==================== TESTES DE DELEÇÃO ====================

    /**
     * Teste: Deletar nota específica
     * Esperado: Nota é removida
     */
    @Test
    fun deleteGrade_removesSuccessfully() = runBlocking {
        // Arrange
        val grade = GradeEntity(
            "task_001-student_001",
            "task_001",
            "student_001",
            85,
            false,
            System.currentTimeMillis()
        )
        gradeDao.insertGrade(grade)

        // Act
        gradeDao.deleteGrade(grade)

        // Assert
        val retrieved = gradeDao.getGradeById("task_001-student_001")
        assertNull(retrieved)
    }

    /**
     * Teste: Deletar nota por ID
     * Esperado: Nota é removida
     */
    @Test
    fun deleteGradeById_removesSuccessfully() = runBlocking {
        // Arrange
        val grade = GradeEntity(
            "task_001-student_001",
            "task_001",
            "student_001",
            85,
            false,
            System.currentTimeMillis()
        )
        gradeDao.insertGrade(grade)

        // Act
        gradeDao.deleteGradeById("task_001-student_001")

        // Assert
        val retrieved = gradeDao.getGradeById("task_001-student_001")
        assertNull(retrieved)
    }

    /**
     * Teste: Deletar todas as notas
     * Esperado: Tabela fica vazia
     */
    @Test
    fun deleteAll_removesAll() = runBlocking {
        // Arrange
        val grades = listOf(
            GradeEntity("grade_001", "task_001", "student_001", 85, false, System.currentTimeMillis()),
            GradeEntity("grade_002", "task_002", "student_001", 90, false, System.currentTimeMillis())
        )
        gradeDao.insertGrades(grades)

        // Act
        gradeDao.deleteAll()

        // Assert
        val allGrades = gradeDao.getAllGrades().first()
        assertEquals(0, allGrades.size)
    }

    // ==================== TESTES DE SYNC ====================

    /**
     * Teste: Recuperar notas não sincronizadas
     * Esperado: Apenas notas com isSynced=false são retornadas
     */
    @Test
    fun getUnsyncedGrades_returnsOnlyUnsynced() = runBlocking {
        // Arrange
        val grades = listOf(
            GradeEntity("grade_001", "task_001", "student_001", 85, false, System.currentTimeMillis()),
            GradeEntity("grade_002", "task_002", "student_001", 90, true, System.currentTimeMillis()),
            GradeEntity("grade_003", "task_003", "student_001", 78, false, System.currentTimeMillis())
        )
        gradeDao.insertGrades(grades)

        // Act
        val unsynced = gradeDao.getUnsyncedGrades()

        // Assert
        assertEquals(2, unsynced.size)
        assertTrue(unsynced.all { !it.isSynced })
        assertTrue(unsynced.any { it.id == "grade_001" })
        assertTrue(unsynced.any { it.id == "grade_003" })
        assertFalse(unsynced.any { it.id == "grade_002" })
    }

    /**
     * Teste: Sincronizar notas parcialmente
     * Esperado: Apenas as selecionadas são marcadas como sincronizadas
     */
    @Test
    fun markAsSynced_partial_onlySelectedMarked() = runBlocking {
        // Arrange
        val grades = listOf(
            GradeEntity("grade_001", "task_001", "student_001", 85, false, System.currentTimeMillis()),
            GradeEntity("grade_002", "task_002", "student_001", 90, false, System.currentTimeMillis()),
            GradeEntity("grade_003", "task_003", "student_001", 78, false, System.currentTimeMillis())
        )
        gradeDao.insertGrades(grades)

        // Act
        gradeDao.markAsSynced(listOf("grade_001", "grade_003"))

        // Assert
        val unsynced = gradeDao.getUnsyncedGrades()
        assertEquals(1, unsynced.size)
        assertEquals("grade_002", unsynced[0].id)
    }

    // ==================== TESTES DE EDGE CASES ====================

    /**
     * Teste: Notas com scores em limites (0-100)
     * Esperado: Todos são salvos corretamente
     */
    @Test
    fun insertGrade_minMaxScores_bothSaveCorrectly() = runBlocking {
        // Arrange
        val gradeMin = GradeEntity("grade_001", "task_001", "student_001", 0, false, System.currentTimeMillis())
        val gradeMax = GradeEntity("grade_002", "task_001", "student_002", 100, false, System.currentTimeMillis())

        // Act
        gradeDao.insertGrade(gradeMin)
        gradeDao.insertGrade(gradeMax)

        // Assert
        val min = gradeDao.getGradeById("grade_001")
        val max = gradeDao.getGradeById("grade_002")
        assertEquals(0, min?.score)
        assertEquals(100, max?.score)
    }

    /**
     * Teste: Filtro por tarefa que não tem notas
     * Esperado: Retorna lista vazia
     */
    @Test
    fun getGradesByTask_emptyTask_returnsEmpty() = runBlocking {
        // Arrange - Nenhuma nota inserida

        // Act
        val grades = gradeDao.getGradesByTask("task_nonexistent").first()

        // Assert
        assertEquals(0, grades.size)
    }

    /**
     * Teste: Filtro por estudante que não tem notas
     * Esperado: Retorna lista vazia
     */
    @Test
    fun getGradesByStudent_emptyStudent_returnsEmpty() = runBlocking {
        // Arrange

        // Act
        val grades = gradeDao.getGradesByStudent("student_nonexistent").first()

        // Assert
        assertEquals(0, grades.size)
    }

    /**
     * Teste: Atualizar nota que não existe
     * Esperado: Não causa erro
     */
    @Test
    fun updateGrade_nonExistent_noError() = runBlocking {
        // Arrange
        val grade = GradeEntity("non_existent", "task_001", "student_001", 95, false, System.currentTimeMillis())

        // Act
        gradeDao.updateGrade(grade) // Should not throw

        // Assert
        val retrieved = gradeDao.getGradeById("non_existent")
        assertNull(retrieved)
    }

    /**
     * Teste: Múltiplas notas de mesma tarefa de estudantes diferentes
     * Esperado: Todas são recuperadas corretamente
     */
    @Test
    fun multipleStudentsSameTask_allRetrievedCorrectly() = runBlocking {
        // Arrange
        val grades = listOf(
            GradeEntity("task_001-student_001", "task_001", "student_001", 85, false, System.currentTimeMillis()),
            GradeEntity("task_001-student_002", "task_001", "student_002", 90, false, System.currentTimeMillis()),
            GradeEntity("task_001-student_003", "task_001", "student_003", 78, false, System.currentTimeMillis())
        )
        gradeDao.insertGrades(grades)

        // Act
        val taskGrades = gradeDao.getGradesByTask("task_001").first()

        // Assert
        assertEquals(3, taskGrades.size)
        assertEquals(3, taskGrades.filter { it.taskId == "task_001" }.size)
    }

    /**
     * Teste: Múltiplas operações em sequência
     * Esperado: Todas são executadas corretamente
     */
    @Test
    fun sequentialOperations_allSucceed() = runBlocking {
        // Arrange
        val grade = GradeEntity(
            "task_001-student_001",
            "task_001",
            "student_001",
            85,
            false,
            System.currentTimeMillis()
        )

        // Act
        gradeDao.insertGrade(grade) // Create
        var retrieved = gradeDao.getGradeById("task_001-student_001") // Read
        assertNotNull(retrieved)

        gradeDao.updateGrade(grade.copy(score = 95)) // Update
        retrieved = gradeDao.getGradeById("task_001-student_001")
        assertEquals(95, retrieved?.score)

        gradeDao.deleteGrade(grade) // Delete
        retrieved = gradeDao.getGradeById("task_001-student_001")
        assertNull(retrieved)

        // Assert
        assertTrue(true)
    }
}
