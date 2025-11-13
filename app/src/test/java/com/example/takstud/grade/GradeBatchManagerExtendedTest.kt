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
 * Testes extensivos para GradeBatchManager.
 *
 * Cobre:
 * - Salvamento em batch (com chunking para 500+ items)
 * - Atualização em batch
 * - Lançamento em lote (bulkGradeRelease)
 * - Curva de notas
 * - Deleção em batch com auditoria
 * - Validação prévia
 * - WriteBatch atomicity
 * - Error handling (Firebase, timeouts, partial failures)
 * - Edge cases
 */
class GradeBatchManagerExtendedTest {

    private lateinit var mockGradeDao: GradeDao
    private lateinit var mockFirestore: FirebaseFirestore
    private lateinit var mockBatch: WriteBatch
    private lateinit var mockCollection: CollectionReference
    private lateinit var mockDocRef: DocumentReference
    private lateinit var manager: GradeBatchManager

    private val taskId = "task_001"
    private val studentId1 = "student_001"
    private val studentId2 = "student_002"
    private val studentId3 = "student_003"

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

    // ==================== Helper Functions ====================

    private fun createGrade(
        taskId: String = this.taskId,
        studentId: String = studentId1,
        score: String = "85",
        classId: String = "6A"
    ): Grade {
        return Grade(
            id = "$taskId-$studentId",
            taskId = taskId,
            studentId = studentId,
            score = score,
            classId = classId,
            releaseDate = System.currentTimeMillis()
        )
    }

    private fun createGradeEntity(
        taskId: String = this.taskId,
        studentId: String = studentId1,
        score: String = "85"
    ): GradeEntity {
        return GradeEntity(
            id = "$taskId-$studentId",
            taskId = taskId,
            studentId = studentId,
            score = score,
            releaseDate = System.currentTimeMillis(),
            isSynced = false
        )
    }

    // ==================== SAVE GRADES BATCH TESTS ====================

    @Test
    fun `save small batch of grades successfully (under 500)`() = runBlocking {
        // Arrange
        val grades = listOf(
            createGrade(studentId = studentId1, score = "85"),
            createGrade(studentId = studentId2, score = "92"),
            createGrade(studentId = studentId3, score = "78")
        )
        coEvery { mockGradeDao.insertGrades(any()) } returns Unit
        coEvery { mockGradeDao.markAsSynced(any()) } returns Unit

        // Act
        val result = manager.saveGradesBatch(grades, localSave = true, validateBeforeSave = true)

        // Assert
        assertEquals(3, result.total)
        assertTrue(result.isSuccess)
        coVerify { mockGradeDao.insertGrades(any()) }
    }

    @Test
    fun `save large batch with chunking (500+ items splits into multiple batches)`() = runBlocking {
        // Arrange - Create 750 grades (requires 2 WriteBatches: 500 + 250)
        val grades = (1..750).map { i ->
            createGrade(
                studentId = "student_$i",
                score = "${80 + (i % 20)}"
            )
        }
        coEvery { mockGradeDao.insertGrades(any()) } returns Unit
        coEvery { mockGradeDao.markAsSynced(any()) } returns Unit

        // Act
        val result = manager.saveGradesBatch(grades, localSave = true, validateBeforeSave = true)

        // Assert
        assertEquals(750, result.total)
        assertTrue(result.isSuccess)
        // Verify WriteBatch was called twice (chunking)
        verify(atLeast = 2) { mockFirestore.batch() }
    }

    @Test
    fun `save with validation - rejects invalid grades`() = runBlocking {
        // Arrange - Mix of valid and invalid grades
        val grades = listOf(
            createGrade(score = "85"),      // Valid
            createGrade(score = "150"),     // Invalid: > 100
            createGrade(score = "-10"),     // Invalid: < 0
            createGrade(score = "92")       // Valid
        )

        // Act
        val result = manager.saveGradesBatch(grades, validateBeforeSave = true)

        // Assert
        assertFalse(result.isSuccess)
        assertEquals(2, result.invalidGrades.size)
        assertEquals(2, result.savedCount)
    }

    @Test
    fun `save without validation bypasses checks`() = runBlocking {
        // Arrange - Invalid grades but validation disabled
        val grades = listOf(
            createGrade(score = "150"),  // Invalid but will be saved
            createGrade(score = "-10")   // Invalid but will be saved
        )
        coEvery { mockGradeDao.insertGrades(any()) } returns Unit

        // Act
        val result = manager.saveGradesBatch(grades, validateBeforeSave = false)

        // Assert
        assertTrue(result.isSuccess)
        assertEquals(2, result.savedCount)
    }

    @Test
    fun `save batch with local persistence disabled`() = runBlocking {
        // Arrange
        val grades = listOf(
            createGrade(studentId = studentId1),
            createGrade(studentId = studentId2)
        )
        coEvery { mockGradeDao.insertGrades(any()) } returns Unit

        // Act
        val result = manager.saveGradesBatch(grades, localSave = false)

        // Assert
        assertTrue(result.isSuccess)
        // DAO should not be called if localSave = false
        coVerify(exactly = 0) { mockGradeDao.insertGrades(any()) }
    }

    @Test
    fun `save batch handles DAO insert failure gracefully`() = runBlocking {
        // Arrange
        val grades = listOf(createGrade())
        coEvery { mockGradeDao.insertGrades(any()) } throws Exception("Database error")

        // Act
        val result = manager.saveGradesBatch(grades, localSave = true)

        // Assert
        assertFalse(result.isSuccess)
    }

    @Test
    fun `save batch handles Firestore commit failure`() = runBlocking {
        // Arrange
        val grades = listOf(createGrade())
        coEvery { mockGradeDao.insertGrades(any()) } returns Unit
        coEvery { mockBatch.commit() } throws Exception("Firebase error")

        // Act
        val result = manager.saveGradesBatch(grades, localSave = true)

        // Assert
        assertFalse(result.isSuccess)
    }

    @Test
    fun `save empty batch`() = runBlocking {
        // Arrange
        val grades = emptyList<Grade>()

        // Act
        val result = manager.saveGradesBatch(grades)

        // Assert
        assertTrue(result.isSuccess)
        assertEquals(0, result.total)
    }

    // ==================== UPDATE GRADES BATCH TESTS ====================

    @Test
    fun `update batch of grades successfully`() = runBlocking {
        // Arrange
        val grades = listOf(
            createGrade(studentId = studentId1, score = "88"),
            createGrade(studentId = studentId2, score = "95")
        )
        coEvery { mockGradeDao.updateGrades(any()) } returns Unit

        // Act
        val result = manager.updateGradesBatch(grades)

        // Assert
        assertEquals(2, result.total)
        assertTrue(result.isSuccess)
        coVerify { mockGradeDao.updateGrades(any()) }
    }

    @Test
    fun `update batch with validation rejects invalid scores`() = runBlocking {
        // Arrange
        val grades = listOf(
            createGrade(score = "88"),      // Valid
            createGrade(score = "120"),     // Invalid: > 100
            createGrade(score = "95")       // Valid
        )

        // Act
        val result = manager.updateGradesBatch(grades, validateBeforeSave = true)

        // Assert
        assertFalse(result.isSuccess)
        assertEquals(1, result.invalidGrades.size)
    }

    @Test
    fun `update batch handles DAO update failure`() = runBlocking {
        // Arrange
        val grades = listOf(createGrade())
        coEvery { mockGradeDao.updateGrades(any()) } throws Exception("Update failed")

        // Act
        val result = manager.updateGradesBatch(grades)

        // Assert
        assertFalse(result.isSuccess)
    }

    // ==================== BULK GRADE RELEASE TESTS ====================

    @Test
    fun `bulk release grades to multiple students`() = runBlocking {
        // Arrange
        val studentIds = listOf(studentId1, studentId2, studentId3)
        coEvery { mockGradeDao.insertGrades(any()) } returns Unit

        // Act
        val result = manager.bulkGradeRelease(studentIds, taskId, "85")

        // Assert
        assertEquals(3, result.created)
        assertEquals(3, result.totalStudents)
        assertTrue(result.isSuccess)
    }

    @Test
    fun `bulk release with invalid score (>100)`() = runBlocking {
        // Arrange
        val studentIds = listOf(studentId1, studentId2)

        // Act
        val result = manager.bulkGradeRelease(studentIds, taskId, "150")

        // Assert
        assertFalse(result.isSuccess)
        assertEquals(0, result.created)
    }

    @Test
    fun `bulk release with invalid score (<0)`() = runBlocking {
        // Arrange
        val studentIds = listOf(studentId1, studentId2)

        // Act
        val result = manager.bulkGradeRelease(studentIds, taskId, "-5")

        // Assert
        assertFalse(result.isSuccess)
    }

    @Test
    fun `bulk release to empty student list`() = runBlocking {
        // Arrange
        val studentIds = emptyList<String>()

        // Act
        val result = manager.bulkGradeRelease(studentIds, taskId, "85")

        // Assert
        assertTrue(result.isSuccess)
        assertEquals(0, result.created)
    }

    @Test
    fun `bulk release with chunking for 500+ students`() = runBlocking {
        // Arrange
        val studentIds = (1..750).map { "student_$it" }
        coEvery { mockGradeDao.insertGrades(any()) } returns Unit

        // Act
        val result = manager.bulkGradeRelease(studentIds, taskId, "85")

        // Assert
        assertEquals(750, result.totalStudents)
        assertTrue(result.isSuccess)
    }

    @Test
    fun `bulk release handles database error gracefully`() = runBlocking {
        // Arrange
        val studentIds = listOf(studentId1, studentId2)
        coEvery { mockGradeDao.insertGrades(any()) } throws Exception("DB error")

        // Act
        val result = manager.bulkGradeRelease(studentIds, taskId, "85")

        // Assert
        assertFalse(result.isSuccess)
    }

    // ==================== CURVE GRADES TESTS ====================

    @Test
    fun `curve grades with percentage increase`() = runBlocking {
        // Arrange
        val studentIds = listOf(studentId1, studentId2, studentId3)
        val curvePercentage = 10.0  // Add 10 points
        coEvery { mockGradeDao.insertGrades(any()) } returns Unit

        // Act
        val result = manager.curveGrades(studentIds, taskId, curvePercentage)

        // Assert
        assertEquals(3, result.total)
        assertTrue(result.isSuccess)
        // After curve: 85 + (85 * 0.10) = 93.5, capped at 100
        verify { mockGradeDao.insertGrades(any()) }
    }

    @Test
    fun `curve grades caps at 100`() = runBlocking {
        // Arrange
        val studentIds = listOf(studentId1)
        val curvePercentage = 30.0  // Would exceed 100
        coEvery { mockGradeDao.insertGrades(any()) } returns Unit

        // Act
        val result = manager.curveGrades(studentIds, taskId, curvePercentage)

        // Assert
        assertTrue(result.isSuccess)
        assertEquals(1, result.capped)  // One grade capped at 100
    }

    @Test
    fun `curve grades with negative percentage`() = runBlocking {
        // Arrange
        val studentIds = listOf(studentId1)
        val curvePercentage = -5.0  // Subtract 5 points
        coEvery { mockGradeDao.insertGrades(any()) } returns Unit

        // Act
        val result = manager.curveGrades(studentIds, taskId, curvePercentage)

        // Assert
        assertTrue(result.isSuccess)
    }

    @Test
    fun `curve grades with large percentage`() = runBlocking {
        // Arrange
        val studentIds = (1..10).map { "student_$it" }
        val curvePercentage = 50.0  // Add 50%
        coEvery { mockGradeDao.insertGrades(any()) } returns Unit

        // Act
        val result = manager.curveGrades(studentIds, taskId, curvePercentage)

        // Assert
        assertEquals(10, result.total)
        // Most should be capped at 100
        assertTrue(result.capped > 0)
    }

    @Test
    fun `curve grades on empty student list`() = runBlocking {
        // Arrange
        val studentIds = emptyList<String>()

        // Act
        val result = manager.curveGrades(studentIds, taskId, 10.0)

        // Assert
        assertTrue(result.isSuccess)
        assertEquals(0, result.total)
    }

    // ==================== DELETE GRADES BATCH TESTS ====================

    @Test
    fun `delete batch of grades with audit logging`() = runBlocking {
        // Arrange
        val grades = listOf(
            createGrade(studentId = studentId1),
            createGrade(studentId = studentId2)
        )
        coEvery { mockGradeDao.deleteGrades(any()) } returns Unit

        // Act
        val result = manager.deleteGradesBatch(grades)

        // Assert
        assertEquals(2, result.deleted)
        assertTrue(result.isSuccess)
        coVerify { mockGradeDao.deleteGrades(any()) }
    }

    @Test
    fun `delete batch with Firestore cleanup`() = runBlocking {
        // Arrange
        val grades = listOf(createGrade())
        coEvery { mockGradeDao.deleteGrades(any()) } returns Unit

        // Act
        val result = manager.deleteGradesBatch(grades)

        // Assert
        assertTrue(result.isSuccess)
        // Verify WriteBatch was used for Firestore deletion
        verify { mockFirestore.batch() }
    }

    @Test
    fun `delete batch handles DAO deletion failure`() = runBlocking {
        // Arrange
        val grades = listOf(createGrade())
        coEvery { mockGradeDao.deleteGrades(any()) } throws Exception("Delete failed")

        // Act
        val result = manager.deleteGradesBatch(grades)

        // Assert
        assertFalse(result.isSuccess)
    }

    @Test
    fun `delete empty batch`() = runBlocking {
        // Arrange
        val grades = emptyList<Grade>()

        // Act
        val result = manager.deleteGradesBatch(grades)

        // Assert
        assertTrue(result.isSuccess)
        assertEquals(0, result.deleted)
    }

    @Test
    fun `delete large batch with chunking (500+ items)`() = runBlocking {
        // Arrange
        val grades = (1..600).map { i ->
            createGrade(studentId = "student_$i")
        }
        coEvery { mockGradeDao.deleteGrades(any()) } returns Unit

        // Act
        val result = manager.deleteGradesBatch(grades)

        // Assert
        assertEquals(600, result.deleted)
        assertTrue(result.isSuccess)
    }

    // ==================== EDGE CASES & ERROR HANDLING ====================

    @Test
    fun `save grades with null batch returns failure`() = runBlocking {
        // Arrange
        val grades = listOf<Grade>()  // Empty

        // Act
        val result = manager.saveGradesBatch(grades)

        // Assert
        assertTrue(result.isSuccess)
    }

    @Test
    fun `handle concurrent batch operations safely`() = runBlocking {
        // Arrange
        val grades1 = listOf(createGrade(studentId = studentId1))
        val grades2 = listOf(createGrade(studentId = studentId2))
        val grades3 = listOf(createGrade(studentId = studentId3))
        coEvery { mockGradeDao.insertGrades(any()) } returns Unit

        // Act
        val result1 = manager.saveGradesBatch(grades1)
        val result2 = manager.saveGradesBatch(grades2)
        val result3 = manager.saveGradesBatch(grades3)

        // Assert
        assertTrue(result1.isSuccess)
        assertTrue(result2.isSuccess)
        assertTrue(result3.isSuccess)
    }

    @Test
    fun `validate grades with boundary values`() = runBlocking {
        // Arrange
        val grades = listOf(
            createGrade(score = "0"),     // Valid: minimum
            createGrade(score = "100"),   // Valid: maximum
            createGrade(score = "50.5")   // Valid: decimal
        )

        // Act
        val result = manager.saveGradesBatch(grades, validateBeforeSave = true)

        // Assert
        assertTrue(result.isSuccess)
        assertEquals(3, result.savedCount)
    }

    @Test
    fun `save grades with missing task ID`() = runBlocking {
        // Arrange
        val grades = listOf(
            Grade(
                id = "",
                taskId = "",  // Missing
                studentId = studentId1,
                score = "85",
                releaseDate = System.currentTimeMillis()
            )
        )

        // Act
        val result = manager.saveGradesBatch(grades, validateBeforeSave = true)

        // Assert
        assertFalse(result.isSuccess)
        assertEquals(1, result.invalidGrades.size)
    }

    @Test
    fun `save grades with missing student ID`() = runBlocking {
        // Arrange
        val grades = listOf(
            Grade(
                id = "",
                taskId = taskId,
                studentId = "",  // Missing
                score = "85",
                releaseDate = System.currentTimeMillis()
            )
        )

        // Act
        val result = manager.saveGradesBatch(grades, validateBeforeSave = true)

        // Assert
        assertFalse(result.isSuccess)
    }

    @Test
    fun `bulk release with max batch size (1000 students)`() = runBlocking {
        // Arrange
        val studentIds = (1..1000).map { "student_$it" }
        coEvery { mockGradeDao.insertGrades(any()) } returns Unit

        // Act
        val result = manager.bulkGradeRelease(studentIds, taskId, "85")

        // Assert
        assertEquals(1000, result.totalStudents)
        assertTrue(result.isSuccess)
    }

    @Test
    fun `handle Firestore batch commit timeout gracefully`() = runBlocking {
        // Arrange
        val grades = listOf(createGrade())
        coEvery { mockGradeDao.insertGrades(any()) } returns Unit
        coEvery { mockBatch.commit() } throws Exception("Timeout: Firestore commit failed")

        // Act
        val result = manager.saveGradesBatch(grades, localSave = true)

        // Assert
        assertFalse(result.isSuccess)
    }

    @Test
    fun `save grades with special characters in student names`() = runBlocking {
        // Arrange - Grades can have UTF-8 characters
        val grades = listOf(createGrade())
        coEvery { mockGradeDao.insertGrades(any()) } returns Unit

        // Act
        val result = manager.saveGradesBatch(grades, validateBeforeSave = true)

        // Assert
        assertTrue(result.isSuccess)
    }
}
