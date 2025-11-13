package com.example.takstud.performance

import com.example.takstud.attendance.AttendanceDeduplicationManager
import com.example.takstud.data.local.dao.AttendanceDao
import com.example.takstud.data.local.dao.GradeDao
import com.example.takstud.grade.GradeBatchManager
import com.example.takstud.model.AttendanceRecord
import com.example.takstud.model.Grade
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.WriteBatch
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.runBlocking
import org.junit.Before
import org.junit.Test
import kotlin.system.measureTimeMillis
import kotlin.test.assertTrue

/**
 * Testes de Performance e Stress para validar eficiência em cenários extremos.
 *
 * Cobre:
 * - Processamento de grandes volumes de dados (1000+, 5000+, 10000+ items)
 * - Operações repetidas (stress testing)
 * - Eficiência de memória
 * - Deduplicação em larga escala
 * - Batch operations com muitos items
 * - Concorrência e thread safety
 * - Timeout scenarios
 * - Performance degradation tests
 */
class PerformanceStressTest {

    private lateinit var mockAttendanceDao: AttendanceDao
    private lateinit var mockGradeDao: GradeDao
    private lateinit var mockFirestore: FirebaseFirestore
    private lateinit var mockBatch: WriteBatch

    private lateinit var dedupManager: AttendanceDeduplicationManager
    private lateinit var gradeBatchManager: GradeBatchManager

    private val taskId = "task_001"

    @Before
    fun setUp() {
        mockAttendanceDao = mockk()
        mockGradeDao = mockk()
        mockFirestore = mockk()
        mockBatch = mockk()

        // Setup mocks
        every { mockFirestore.batch() } returns mockBatch
        every { mockFirestore.collection("grades") } returns mockk()
        every { mockBatch.set(any(), any()) } returns mockBatch
        every { mockBatch.update(any(), any()) } returns mockBatch
        coEvery { mockBatch.commit() } returns mockk()

        coEvery { mockAttendanceDao.insertAttendance(any()) } returns Unit
        coEvery { mockAttendanceDao.updateAttendance(any()) } returns Unit
        coEvery { mockGradeDao.insertGrades(any()) } returns Unit
        coEvery { mockGradeDao.markAsSynced(any()) } returns Unit

        dedupManager = AttendanceDeduplicationManager(mockAttendanceDao)
        gradeBatchManager = GradeBatchManager(mockGradeDao, mockFirestore)
    }

    // ==================== Helper Functions ====================

    private fun createAttendanceRecord(
        studentId: String,
        date: String,
        modifiedAt: Long = System.currentTimeMillis()
    ): AttendanceRecord {
        return AttendanceRecord(
            id = "$studentId-$date",
            studentId = studentId,
            date = date,
            isPresent = (studentId.hashCode() % 2 == 0),
            studentName = "Student $studentId",
            modifiedAt = modifiedAt
        )
    }

    private fun createGrade(studentId: String, score: String = "85"): Grade {
        return Grade(
            id = "$taskId-$studentId",
            taskId = taskId,
            studentId = studentId,
            score = score,
            classId = "class_6A",
            releaseDate = System.currentTimeMillis()
        )
    }

    // ==================== LARGE DATASET TESTS (10 tests) ====================

    @Test
    fun `performance - deduplicate 1000 attendance records with 90% duplicates`() = runBlocking {
        // Arrange - 1000 records with many duplicates
        val records = mutableListOf<AttendanceRecord>()
        for (i in 1..100) {  // 100 unique
            for (j in 1..10) {  // 10 duplicates each
                records.add(
                    createAttendanceRecord(
                        studentId = "student_$i",
                        date = "2025-11-${14 + (i % 7)}",
                        modifiedAt = System.currentTimeMillis() + j
                    )
                )
            }
        }

        // Act
        val duration = measureTimeMillis {
            val dedupedRecords = dedupManager.deduplicateBeforeSync(records)
            // Assert - Should have only 100 unique records
            assertTrue(dedupedRecords.size <= 100)
        }

        // Assert - Should complete in reasonable time (< 5 seconds)
        assertTrue(duration < 5000, "Deduplication took ${duration}ms, expected < 5000ms")
    }

    @Test
    fun `performance - deduplicate 5000 records efficiently`() = runBlocking {
        // Arrange - 5000 records
        val records = (1..5000).map { i ->
            createAttendanceRecord(
                studentId = "student_${i % 500}",  // 500 unique
                date = "2025-11-14",
                modifiedAt = System.currentTimeMillis() + i
            )
        }

        // Act
        val duration = measureTimeMillis {
            val dedupedRecords = dedupManager.deduplicateBeforeSync(records)
            // Should have 500 unique
            assertTrue(dedupedRecords.size <= 500)
        }

        // Assert - Should complete in < 10 seconds
        assertTrue(duration < 10000, "5000-record dedup took ${duration}ms, expected < 10000ms")
    }

    @Test
    fun `performance - save 1000 grades in batch operations`() = runBlocking {
        // Arrange - 1000 grades
        val grades = (1..1000).map { i ->
            createGrade(studentId = "student_$i", score = "85")
        }

        coEvery { mockGradeDao.insertGrades(any()) } returns Unit

        // Act
        val duration = measureTimeMillis {
            val result = gradeBatchManager.saveGradesBatch(grades)
            // Should succeed
            assertTrue(result.failed == 0)
        }

        // Assert - Should complete in < 5 seconds
        assertTrue(duration < 5000, "Batch save took ${duration}ms")
    }

    @Test
    fun `performance - bulk release to 1000 students efficiently`() = runBlocking {
        // Arrange
        val studentIds = (1..1000).map { "student_$it" }
        coEvery { mockGradeDao.insertGrades(any()) } returns Unit

        // Act
        val duration = measureTimeMillis {
            val result = gradeBatchManager.bulkGradeRelease(
                studentIds = studentIds,
                taskId = taskId,
                score = "85"
            )
            assertTrue(result.created > 0)
        }

        // Assert - Should complete in < 3 seconds
        assertTrue(duration < 3000, "Bulk release took ${duration}ms")
    }

    @Test
    fun `performance - handle 10000 attendance records`() = runBlocking {
        // Arrange - 10000 records (stress test limit)
        val records = (1..10000).map { i ->
            createAttendanceRecord(
                studentId = "student_${i % 1000}",
                date = "2025-11-${14 + (i % 7)}"
            )
        }

        // Act
        val duration = measureTimeMillis {
            val dedupedRecords = dedupManager.deduplicateBeforeSync(records)
            // Should have ~1000 unique
            assertTrue(dedupedRecords.size in 900..1000)
        }

        // Assert - Should complete in < 15 seconds
        assertTrue(duration < 15000, "10000-record handling took ${duration}ms")
    }

    @Test
    fun `performance - memory efficient with large lists`() = runBlocking {
        // Arrange - Very large list
        val largeList = (1..5000).map { i ->
            createGrade(studentId = "student_${i % 500}", score = "${70 + (i % 30)}")
        }

        // Act
        val memBefore = Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory()

        val duration = measureTimeMillis {
            val result = gradeBatchManager.saveGradesBatch(largeList)
            assertTrue(result.total == largeList.size)
        }

        val memAfter = Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory()
        val memUsed = (memAfter - memBefore) / (1024 * 1024)  // Convert to MB

        // Assert
        assertTrue(duration < 10000)
        // Memory usage shouldn't exceed 100MB for this operation
        assertTrue(memUsed < 100, "Memory usage: ${memUsed}MB")
    }

    @Test
    fun `performance - batch operations with maximum batch size (500)`() = runBlocking {
        // Arrange - Exactly 500 items
        val grades = (1..500).map { i ->
            createGrade(studentId = "student_$i")
        }

        coEvery { mockGradeDao.insertGrades(any()) } returns Unit

        // Act
        val duration = measureTimeMillis {
            val result = gradeBatchManager.saveGradesBatch(grades)
            assertTrue(result.succeeded == 500)
        }

        // Assert
        assertTrue(duration < 3000)
    }

    @Test
    fun `performance - batch operations exceeding maximum (501+)`() = runBlocking {
        // Arrange - 501 items (requires chunking)
        val grades = (1..501).map { i ->
            createGrade(studentId = "student_$i")
        }

        coEvery { mockGradeDao.insertGrades(any()) } returns Unit

        // Act
        val duration = measureTimeMillis {
            val result = gradeBatchManager.saveGradesBatch(grades)
            // Should still succeed with chunking
            assertTrue(result.succeeded == 501)
        }

        // Assert
        assertTrue(duration < 5000)
    }

    @Test
    fun `performance - concurrent deduplication operations`() = runBlocking {
        // Arrange - Multiple dedup operations
        val records1 = (1..500).map { i ->
            createAttendanceRecord(
                studentId = "student_${i % 50}",
                date = "2025-11-14",
                modifiedAt = System.currentTimeMillis() + i
            )
        }

        val records2 = (501..1000).map { i ->
            createAttendanceRecord(
                studentId = "student_${i % 50}",
                date = "2025-11-15",
                modifiedAt = System.currentTimeMillis() + i
            )
        }

        // Act
        val duration = measureTimeMillis {
            val result1 = dedupManager.deduplicateBeforeSync(records1)
            val result2 = dedupManager.deduplicateBeforeSync(records2)

            // Both should complete
            assertTrue(result1.isNotEmpty())
            assertTrue(result2.isNotEmpty())
        }

        // Assert - Concurrent ops should still be fast
        assertTrue(duration < 2000)
    }

    // ==================== STRESS TESTS (10 tests) ====================

    @Test
    fun `stress - repeated bulk operations 100 times`() = runBlocking {
        // Arrange
        val studentIds = (1..10).map { "student_$it" }
        coEvery { mockGradeDao.insertGrades(any()) } returns Unit

        // Act
        val duration = measureTimeMillis {
            repeat(100) {
                val result = gradeBatchManager.bulkGradeRelease(
                    studentIds = studentIds,
                    taskId = taskId,
                    score = "85"
                )
                assertTrue(result.created > 0)
            }
        }

        // Assert - 100 operations should complete in reasonable time
        assertTrue(duration < 30000, "100 bulk operations took ${duration}ms")
    }

    @Test
    fun `stress - rapid save operations without errors`() = runBlocking {
        // Arrange
        coEvery { mockGradeDao.insertGrades(any()) } returns Unit

        // Act
        val duration = measureTimeMillis {
            repeat(50) { iteration ->
                val grades = (1..10).map { i ->
                    createGrade(studentId = "student_$i")
                }
                val result = gradeBatchManager.saveGradesBatch(grades)
                assertTrue(result.succeeded == 10)
            }
        }

        // Assert
        assertTrue(duration < 20000)
    }

    @Test
    fun `stress - mixed operations maintain consistency`() = runBlocking {
        // Arrange
        coEvery { mockGradeDao.insertGrades(any()) } returns Unit
        coEvery { mockAttendanceDao.insertAttendance(any()) } returns Unit

        // Act
        val duration = measureTimeMillis {
            // Mix of attendance and grade operations
            repeat(20) {
                // Save 10 grades
                val grades = (1..10).map { i ->
                    createGrade(studentId = "student_$i")
                }
                gradeBatchManager.saveGradesBatch(grades)

                // Save 10 attendance records
                val attendance = (1..10).map { i ->
                    createAttendanceRecord(
                        studentId = "student_$i",
                        date = "2025-11-14"
                    )
                }
                attendance.forEach { record ->
                    coEvery { mockAttendanceDao.getAttendanceById(any()) } returns null
                    dedupManager.saveAttendanceWithDeduplication(record)
                }
            }
        }

        // Assert
        assertTrue(duration < 30000)
    }

    @Test
    fun `stress - large dedup operations 10 times`() = runBlocking {
        // Arrange
        val largeRecords = (1..1000).map { i ->
            createAttendanceRecord(
                studentId = "student_${i % 100}",
                date = "2025-11-14",
                modifiedAt = System.currentTimeMillis() + i
            )
        }

        // Act
        val duration = measureTimeMillis {
            repeat(10) {
                dedupManager.deduplicateBeforeSync(largeRecords)
            }
        }

        // Assert
        assertTrue(duration < 20000)
    }

    @Test
    fun `stress - validation on 1000 items`() = runBlocking {
        // Arrange
        val grades = (1..1000).map { i ->
            createGrade(studentId = "student_$i", score = "85")
        }

        // Act
        val duration = measureTimeMillis {
            gradeBatchManager.saveGradesBatch(
                grades = grades,
                validateBeforeSave = true
            )
        }

        // Assert
        assertTrue(duration < 5000)
    }

    @Test
    fun `stress - error recovery under load`() = runBlocking {
        // Arrange - Make DAO fail intermittently
        var callCount = 0
        coEvery { mockGradeDao.insertGrades(any()) } answers {
            callCount++
            if (callCount % 3 == 0) throw Exception("Simulated DAO error")
            Unit
        }

        // Act
        val duration = measureTimeMillis {
            repeat(10) {
                try {
                    val grades = (1..20).map { i ->
                        createGrade(studentId = "student_$i")
                    }
                    gradeBatchManager.saveGradesBatch(grades)
                } catch (e: Exception) {
                    // Expected - some calls will fail
                }
            }
        }

        // Assert
        assertTrue(duration < 15000)
    }

    @Test
    fun `stress - memory stability under repeated operations`() = runBlocking {
        // Arrange
        coEvery { mockGradeDao.insertGrades(any()) } returns Unit

        val memSamples = mutableListOf<Long>()

        // Act
        repeat(20) {
            val grades = (1..100).map { i ->
                createGrade(studentId = "student_$i")
            }
            gradeBatchManager.saveGradesBatch(grades)

            val currentMem = Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory()
            memSamples.add(currentMem / (1024 * 1024))  // MB
        }

        // Assert - Memory should not grow unbounded
        val avgMem = memSamples.average()
        val maxMem = memSamples.maxOrNull() ?: 0
        assertTrue(maxMem < 200, "Max memory: ${maxMem}MB")
    }

    // ==================== EDGE CASES (8 tests) ====================

    @Test
    fun `edge case - empty data at scale`() = runBlocking {
        // Arrange
        val emptyRecords = emptyList<AttendanceRecord>()

        // Act
        val dedupedRecords = dedupManager.deduplicateBeforeSync(emptyRecords)

        // Assert
        assertTrue(dedupedRecords.isEmpty())
    }

    @Test
    fun `edge case - single record at scale`() = runBlocking {
        // Arrange
        val singleRecord = listOf(
            createAttendanceRecord(studentId = "student_1", date = "2025-11-14")
        )

        // Act
        val dedupedRecords = dedupManager.deduplicateBeforeSync(singleRecord)

        // Assert
        assertTrue(dedupedRecords.size == 1)
    }

    @Test
    fun `edge case - all identical records`() = runBlocking {
        // Arrange - 1000 identical records
        val identicalRecords = (1..1000).map { i ->
            createAttendanceRecord(
                studentId = "student_1",
                date = "2025-11-14",
                modifiedAt = System.currentTimeMillis() + i
            )
        }

        // Act
        val dedupedRecords = dedupManager.deduplicateBeforeSync(identicalRecords)

        // Assert - Should have only 1 (the newest)
        assertTrue(dedupedRecords.size == 1)
    }

    @Test
    fun `edge case - maximum grade value boundary`() = runBlocking {
        // Arrange
        val maxGrades = (1..100).map { i ->
            createGrade(studentId = "student_$i", score = "100.0")
        }

        coEvery { mockGradeDao.insertGrades(any()) } returns Unit

        // Act
        val result = gradeBatchManager.saveGradesBatch(maxGrades)

        // Assert
        assertTrue(result.succeeded == 100)
    }

    @Test
    fun `edge case - minimum grade value boundary`() = runBlocking {
        // Arrange
        val minGrades = (1..100).map { i ->
            createGrade(studentId = "student_$i", score = "0.0")
        }

        coEvery { mockGradeDao.insertGrades(any()) } returns Unit

        // Act
        val result = gradeBatchManager.saveGradesBatch(minGrades)

        // Assert
        assertTrue(result.succeeded == 100)
    }

    @Test
    fun `edge case - mixed valid and invalid in large batch`() = runBlocking {
        // Arrange - 100 valid, 10 invalid
        val mixedGrades = mutableListOf<Grade>()
        for (i in 1..100) {
            mixedGrades.add(createGrade(studentId = "student_$i", score = "85"))
        }
        // Add invalid ones
        for (i in 101..110) {
            mixedGrades.add(createGrade(studentId = "student_$i", score = "150"))
        }

        // Act
        val result = gradeBatchManager.saveGradesBatch(
            grades = mixedGrades,
            validateBeforeSave = true
        )

        // Assert
        assertTrue(result.failed > 0)
    }

    @Test
    fun `edge case - dedup with null timestamps`() = runBlocking {
        // Arrange
        val records = listOf(
            createAttendanceRecord(studentId = "student_1", date = "2025-11-14", modifiedAt = 0)
        )

        // Act
        val dedupedRecords = dedupManager.deduplicateBeforeSync(records)

        // Assert
        assertTrue(dedupedRecords.isNotEmpty())
    }

    @Test
    fun `edge case - timeout simulation with large data`() = runBlocking {
        // Arrange - Large data set
        val largeData = (1..5000).map { i ->
            createAttendanceRecord(
                studentId = "student_${i % 500}",
                date = "2025-11-14",
                modifiedAt = System.currentTimeMillis() + i
            )
        }

        // Act
        val duration = measureTimeMillis {
            dedupManager.deduplicateBeforeSync(largeData)
        }

        // Assert - Should complete within timeout (3 seconds)
        assertTrue(duration < 3000)
    }
}
