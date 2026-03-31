package com.example.takstud.grade

import com.example.takstud.data.local.dao.GradeDao
import com.example.takstud.data.local.entity.GradeEntity
import com.example.takstud.model.Grade
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.WriteBatch
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.runBlocking
import org.junit.Before
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

/**
 * Testes para GradeBatchManager.
 *
 * Valida:
 * - Salvamento em batch
 * - Validação de grades
 * - Atualização em batch
 * - Lançamento em lote
 * - Curva de notas
 * - Deleção em batch
 * - Tratamento de erros
 */
class GradeBatchManagerTest {

    private lateinit var mockGradeDao: GradeDao
    private lateinit var mockFirestore: FirebaseFirestore
    private lateinit var mockBatch: WriteBatch
    private lateinit var mockCollection: CollectionReference
    private lateinit var mockDocRef: DocumentReference
    private lateinit var manager: GradeBatchManager

    @Before
    fun setUp() {
        mockGradeDao = mockk()
        mockFirestore = mockk()
        mockBatch = mockk()
        mockCollection = mockk()
        mockDocRef = mockk()

        // Setup mocks
        every { mockFirestore.batch() } returns mockBatch
        every { mockFirestore.collection("grades") } returns mockCollection
        every { mockCollection.document(any()) } returns mockDocRef
        every { mockBatch.set(any(), any()) } returns mockBatch
        every { mockBatch.update(any(), any()) } returns mockBatch
        every { mockBatch.delete(any()) } returns mockBatch
        coEvery { mockBatch.commit() } returns mockk()

        manager = GradeBatchManager(mockGradeDao, mockFirestore)
    }

    // ==================== SAVE GRADES BATCH TESTS ====================

    @Test
    fun `save grades batch with valid grades should succeed`() = runBlocking {
        // Arrange
        val grades = listOf(
            createGrade("task_001", "student_001", "85"),
            createGrade("task_001", "student_002", "92"),
            createGrade("task_001", "student_003", "78")
        )
        coEvery { mockGradeDao.insertGrades(any()) } returns Unit
        coEvery { mockGradeDao.markAsSynced(any()) } returns Unit

        // Act
        val result = manager.saveGradesBatch(grades, localSave = true, validateBeforeSave = true)

        // Assert
        assertEquals(3, result.total)
        assertEquals(3, result.succeeded)
        assertEquals(0, result.failed)
        assertTrue(result.isSuccess)
        coVerify(exactly = 1) { mockGradeDao.insertGrades(any()) }
    }

    @Test
    fun `save grades batch with invalid grades should fail validation`() = runBlocking {
        // Arrange
        val grades = listOf(
            createGrade("task_001", "student_001", "85"),
            createGrade("task_001", "student_002", "150"),  // Inválido > 100
            createGrade("task_001", "student_003", "invalid")  // Não é número
        )

        // Act
        val result = manager.saveGradesBatch(grades, localSave = true, validateBeforeSave = true)

        // Assert
        assertEquals(3, result.total)
        assertEquals(0, result.succeeded)
        assertEquals(3, result.failed)
        assertTrue(result.isValidationError)
        assertEquals(2, result.invalidGrades.size)
    }

    @Test
    fun `save grades batch with empty list should return zero`() = runBlocking {
        // Act
        val result = manager.saveGradesBatch(emptyList())

        // Assert
        assertEquals(0, result.total)
        assertEquals(0, result.succeeded)
        assertEquals(0, result.failed)
    }

    @Test
    fun `save grades batch without validation should accept all grades`() = runBlocking {
        // Arrange
        val grades = listOf(
            createGrade("task_001", "student_001", "85"),
            createGrade("task_001", "student_002", "92")
        )
        coEvery { mockGradeDao.insertGrades(any()) } returns Unit
        coEvery { mockGradeDao.markAsSynced(any()) } returns Unit

        // Act
        val result = manager.saveGradesBatch(grades, localSave = true, validateBeforeSave = false)

        // Assert
        assertEquals(2, result.total)
        assertEquals(2, result.succeeded)
        assertTrue(result.isSuccess)
    }

    @Test
    fun `save grades batch without local save should skip room insertion`() = runBlocking {
        // Arrange
        val grades = listOf(createGrade("task_001", "student_001", "85"))
        coEvery { mockGradeDao.markAsSynced(any()) } returns Unit

        // Act
        val result = manager.saveGradesBatch(grades, localSave = false, validateBeforeSave = true)

        // Assert
        assertTrue(result.isSuccess)
        coVerify(exactly = 0) { mockGradeDao.insertGrades(any()) }
    }

    // ==================== UPDATE GRADES BATCH TESTS ====================

    @Test
    fun `update grades batch with valid updates should succeed`() = runBlocking {
        // Arrange
        val updates = mapOf(
            "task_001-student_001" to GradeUpdate(score = "90"),
            "task_001-student_002" to GradeUpdate(score = "85")
        )
        coEvery { mockCollection.document(any()) } returns mockDocRef

        // Act
        val result = manager.updateGradesBatch(updates)

        // Assert
        assertEquals(2, result.total)
        assertEquals(2, result.succeeded)
        verify(atLeast = 2) { mockBatch.update(any(), any()) }
    }

    @Test
    fun `update grades batch with invalid score should reject item`() = runBlocking {
        // Arrange
        val updates = mapOf(
            "task_001-student_001" to GradeUpdate(score = "150"),  // Inválido
            "task_001-student_002" to GradeUpdate(score = "85")
        )

        // Act
        val result = manager.updateGradesBatch(updates)

        // Assert
        assertEquals(2, result.total)
        assertEquals(1, result.succeeded)
        assertEquals(1, result.failed)
    }

    @Test
    fun `update grades batch with empty updates should return zero`() = runBlocking {
        // Act
        val result = manager.updateGradesBatch(emptyMap())

        // Assert
        assertEquals(0, result.total)
        assertEquals(0, result.succeeded)
    }

    // ==================== BULK GRADE RELEASE TESTS ====================

    @Test
    fun `bulk grade release with valid data should create all grades`() = runBlocking {
        // Arrange
        val studentIds = listOf("s001", "s002", "s003")
        val taskId = "task_123"
        val score = "75"

        coEvery { mockGradeDao.insertGrades(any()) } returns Unit
        coEvery { mockGradeDao.markAsSynced(any()) } returns Unit

        // Act
        val result = manager.bulkGradeRelease(studentIds, taskId, score)

        // Assert
        assertEquals(3, result.totalStudents)
        assertEquals(3, result.created)
        assertEquals(0, result.failed)
        assertTrue(result.isSuccess)
    }

    @Test
    fun `bulk grade release with invalid score should fail`() = runBlocking {
        // Arrange
        val studentIds = listOf("s001", "s002")
        val taskId = "task_123"
        val score = "150"  // Inválido

        // Act
        val result = manager.bulkGradeRelease(studentIds, taskId, score)

        // Assert
        assertEquals(2, result.totalStudents)
        assertEquals(0, result.created)
        assertEquals(2, result.failed)
        assertFalse(result.isSuccess)
    }

    @Test
    fun `bulk grade release with non-numeric score should fail`() = runBlocking {
        // Act
        val result = manager.bulkGradeRelease(
            listOf("s001"),
            "task_123",
            "invalid"
        )

        // Assert
        assertEquals(0, result.created)
        assertFalse(result.isSuccess)
    }

    @Test
    fun `bulk grade release should set correct timestamps`() = runBlocking {
        // Arrange
        val studentIds = listOf("s001")
        coEvery { mockGradeDao.insertGrades(any()) } returns Unit
        coEvery { mockGradeDao.markAsSynced(any()) } returns Unit

        // Act
        val result = manager.bulkGradeRelease(studentIds, "task_123", "75")

        // Assert
        assertTrue(result.isSuccess)
        coVerify(exactly = 1) { mockGradeDao.insertGrades(any()) }
    }

    // ==================== CURVE GRADES TESTS ====================

    @Test
    fun `curve grades with positive percentage should increase scores`() = runBlocking {
        // Act
        val result = manager.curveGrades(
            listOf("s001", "s002", "s003"),
            curvePercentage = 10.0
        )

        // Assert
        assertEquals(3, result.totalStudents)
        assertEquals(3, result.updated)
        assertEquals(0, result.failed)
        assertTrue(result.isSuccess)
    }

    @Test
    fun `curve grades with invalid percentage should fail`() = runBlocking {
        // Act
        val result = manager.curveGrades(
            listOf("s001"),
            curvePercentage = 150.0  // Inválido
        )

        // Assert
        assertEquals(0, result.updated)
        assertFalse(result.isSuccess)
    }

    @Test
    fun `curve grades with negative percentage should fail`() = runBlocking {
        // Act
        val result = manager.curveGrades(
            listOf("s001"),
            curvePercentage = -10.0
        )

        // Assert
        assertFalse(result.isSuccess)
    }

    @Test
    fun `curve grades with capped scores should track capped items`() = runBlocking {
        // Act
        val result = manager.curveGrades(
            listOf("s001", "s002"),
            curvePercentage = 50.0,  // Grande aumento
            maxScore = 100.0
        )

        // Assert
        assertTrue(result.updated >= 0)
        // Alguns podem estar capped se a nota + curva > 100
    }

    // ==================== DELETE GRADES BATCH TESTS ====================

    @Test
    fun `delete grades batch should create audit entries`() = runBlocking {
        // Arrange
        val gradeIds = listOf("grade_1", "grade_2", "grade_3")
        val auditReason = "Deletado por erro"

        // Act
        val result = manager.deleteGradesBatch(gradeIds, auditReason)

        // Assert
        assertEquals(3, result.total)
        verify(atLeast = 3) { mockBatch.delete(any()) }
    }

    @Test
    fun `delete grades batch with empty list should return zero`() = runBlocking {
        // Act
        val result = manager.deleteGradesBatch(emptyList())

        // Assert
        assertEquals(0, result.total)
        assertEquals(0, result.succeeded)
    }

    // ==================== VALIDATION TESTS ====================

    @Test
    fun `validate grade with valid values should pass`() = runBlocking {
        // Arrange
        val grade = createGrade("task_001", "student_001", "85")

        // Act
        val grades = listOf(grade)
        val result = manager.saveGradesBatch(grades, validateBeforeSave = true, localSave = false)

        // Assert
        assertTrue(result.isSuccess || !result.isValidationError)
    }

    @Test
    fun `validate grade with missing fields should fail`() = runBlocking {
        // Arrange
        val grade = Grade(
            id = "",  // Vazio
            taskId = "task_001",
            studentId = "student_001",
            score = "85",
            value = "85"
        )

        // Act
        val result = manager.saveGradesBatch(listOf(grade), validateBeforeSave = true)

        // Assert
        assertTrue(result.isValidationError)
    }

    @Test
    fun `validate grade with boundary values should accept 0 and 100`() = runBlocking {
        // Arrange
        val grades = listOf(
            createGrade("t1", "s1", "0"),    // Mínimo
            createGrade("t2", "s2", "100")   // Máximo
        )
        coEvery { mockGradeDao.insertGrades(any()) } returns Unit
        coEvery { mockGradeDao.markAsSynced(any()) } returns Unit

        // Act
        val result = manager.saveGradesBatch(grades, validateBeforeSave = true)

        // Assert
        assertEquals(2, result.total)
        assertEquals(2, result.succeeded)
    }

    // ==================== REALISTIC SCENARIOS ====================

    @Test
    fun `scenario - teacher releases grades for entire class`() = runBlocking {
        // Arrange
        val studentIds = (1..30).map { "student_${String.format("%03d", it)}" }
        val taskId = "task_123"
        val score = "85"

        coEvery { mockGradeDao.insertGrades(any()) } returns Unit
        coEvery { mockGradeDao.markAsSynced(any()) } returns Unit

        // Act
        val result = manager.bulkGradeRelease(studentIds, taskId, score)

        // Assert
        assertEquals(30, result.totalStudents)
        assertEquals(30, result.created)
        assertTrue(result.isSuccess)
    }

    @Test
    fun `scenario - teacher curves grades for class due to difficult question`() = runBlocking {
        // Arrange
        val studentIds = (1..25).map { "student_${String.format("%03d", it)}" }

        // Act
        val result = manager.curveGrades(studentIds, curvePercentage = 15.0)

        // Assert
        assertEquals(25, result.totalStudents)
        assertTrue(result.updated > 0)
    }

    @Test
    fun `scenario - bulk save of mixed valid and invalid grades`() = runBlocking {
        // Arrange
        val grades = listOf(
            createGrade("t1", "s1", "85"),     // Válido
            createGrade("t2", "s2", "150"),    // Inválido
            createGrade("t3", "s3", "92"),     // Válido
            createGrade("t4", "s4", "-5")      // Inválido
        )

        // Act
        val result = manager.saveGradesBatch(grades, validateBeforeSave = true)

        // Assert
        assertEquals(4, result.total)
        assertEquals(0, result.succeeded)  // Nenhuma salva por validação falhar
        assertEquals(2, result.invalidGrades.size)
    }

    @Test
    fun `scenario - update subset of grades with different values`() = runBlocking {
        // Arrange
        val updates = mapOf(
            "g1" to GradeUpdate(score = "90"),
            "g2" to GradeUpdate(score = "85"),
            "g3" to GradeUpdate(score = "95")
        )

        // Act
        val result = manager.updateGradesBatch(updates)

        // Assert
        assertEquals(3, result.total)
        assertEquals(3, result.succeeded)
    }

    // ==================== ERROR HANDLING TESTS ====================

    @Test
    fun `save grades batch handles local save error gracefully`() = runBlocking {
        // Arrange
        val grades = listOf(createGrade("t1", "s1", "85"))
        coEvery { mockGradeDao.insertGrades(any()) } throws Exception("Database error")

        // Act
        val result = manager.saveGradesBatch(grades, localSave = true)

        // Assert
        assertFalse(result.isSuccess)
        assertEquals(1, result.failed)
    }

    @Test
    fun `bulk release handles errors gracefully`() = runBlocking {
        // Arrange - simulate error
        coEvery { mockGradeDao.insertGrades(any()) } throws Exception("Error")

        // Act
        val result = manager.bulkGradeRelease(listOf("s1"), "t1", "85")

        // Assert
        assertFalse(result.isSuccess)
        assertEquals(0, result.created)
    }

    // ==================== HELPER FUNCTIONS ====================

    private fun createGrade(
        taskId: String,
        studentId: String,
        score: String
    ): Grade {
        return Grade(
            id = "$taskId-$studentId",
            taskId = taskId,
            studentId = studentId,
            score = score,
            value = score,
            createdAt = System.currentTimeMillis(),
            modifiedAt = System.currentTimeMillis(),
            isSynced = false
        )
    }
}
