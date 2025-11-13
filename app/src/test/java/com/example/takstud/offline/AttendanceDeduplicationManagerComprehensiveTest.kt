package com.example.takstud.offline

import com.example.takstud.data.local.dao.AttendanceDao
import com.example.takstud.data.local.entity.AttendanceEntity
import com.example.takstud.model.AttendanceRecord
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.runBlocking
import org.junit.Before
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

/**
 * Testes abrangentes para AttendanceDeduplicationManager.
 *
 * Cobre:
 * - Deduplicação com Last-Write-Wins
 * - Validação de lotes
 * - Verificação de integridade
 * - Geração de relatórios
 * - Tratamento de erros
 * - Thread safety (Mutex)
 * - Edge cases
 */
class AttendanceDeduplicationManagerComprehensiveTest {

    private lateinit var mockDao: AttendanceDao
    private lateinit var manager: AttendanceDeduplicationManager

    private val testDate = "2025-11-14"
    private val testStudent1 = "student_001"
    private val testStudent2 = "student_002"

    @Before
    fun setUp() {
        mockDao = mockk()
        manager = AttendanceDeduplicationManager(mockDao)
    }

    // ==================== Helper Functions ====================

    private fun createAttendanceRecord(
        studentId: String = testStudent1,
        date: String = testDate,
        isPresent: Boolean = true,
        modifiedAt: Long = System.currentTimeMillis()
    ): AttendanceRecord {
        return AttendanceRecord(
            id = "$studentId-$date",
            studentId = studentId,
            date = date,
            isPresent = isPresent,
            studentName = "Student Name",
            modifiedAt = modifiedAt
        )
    }

    private fun createAttendanceEntity(
        id: String = "$testStudent1-$testDate",
        studentId: String = testStudent1,
        date: String = testDate,
        lastModified: Long = System.currentTimeMillis()
    ): AttendanceEntity {
        return AttendanceEntity(
            id = id,
            studentId = studentId,
            date = date,
            isPresent = true,
            studentName = "Student Name",
            lastModified = lastModified
        )
    }

    // ==================== SAVE WITH DEDUPLICATION TESTS ====================

    @Test
    fun `save new attendance record successfully`() = runBlocking {
        // Arrange
        val record = createAttendanceRecord()
        coEvery { mockDao.getAttendanceById(any()) } returns null
        coEvery { mockDao.insertAttendance(any()) } returns Unit

        // Act
        val result = manager.saveAttendanceWithDeduplication(record)

        // Assert
        assertTrue(result)
        coVerify { mockDao.insertAttendance(any()) }
    }

    @Test
    fun `save newer record replaces older duplicate (LWW - Last Write Wins)`() = runBlocking {
        // Arrange
        val oldTimestamp = System.currentTimeMillis() - 10000
        val newTimestamp = System.currentTimeMillis()
        val oldRecord = createAttendanceEntity(lastModified = oldTimestamp)
        val newRecord = createAttendanceRecord(modifiedAt = newTimestamp)

        coEvery { mockDao.getAttendanceById(any()) } returns oldRecord
        coEvery { mockDao.updateAttendance(any()) } returns Unit

        // Act
        val result = manager.saveAttendanceWithDeduplication(newRecord)

        // Assert
        assertTrue(result)
        coVerify { mockDao.updateAttendance(any()) }
    }

    @Test
    fun `discard older record when duplicate with newer timestamp exists (LWW)`() = runBlocking {
        // Arrange
        val oldTimestamp = System.currentTimeMillis()
        val newTimestamp = System.currentTimeMillis() + 10000
        val existingRecord = createAttendanceEntity(lastModified = newTimestamp)
        val olderRecord = createAttendanceRecord(modifiedAt = oldTimestamp)

        coEvery { mockDao.getAttendanceById(any()) } returns existingRecord
        coEvery { mockDao.updateAttendance(any()) } returns Unit

        // Act
        val result = manager.saveAttendanceWithDeduplication(olderRecord)

        // Assert
        assertFalse(result)
        coVerify(exactly = 0) { mockDao.updateAttendance(any()) }
    }

    @Test
    fun `handle DAO exception gracefully during save`() = runBlocking {
        // Arrange
        val record = createAttendanceRecord()
        coEvery { mockDao.getAttendanceById(any()) } throws Exception("Database error")

        // Act
        val result = manager.saveAttendanceWithDeduplication(record)

        // Assert
        assertFalse(result)
    }

    @Test
    fun `save attendance for different students independently`() = runBlocking {
        // Arrange
        val record1 = createAttendanceRecord(studentId = testStudent1)
        val record2 = createAttendanceRecord(studentId = testStudent2)
        coEvery { mockDao.getAttendanceById(any()) } returns null
        coEvery { mockDao.insertAttendance(any()) } returns Unit

        // Act
        val result1 = manager.saveAttendanceWithDeduplication(record1)
        val result2 = manager.saveAttendanceWithDeduplication(record2)

        // Assert
        assertTrue(result1)
        assertTrue(result2)
        coVerify(exactly = 2) { mockDao.insertAttendance(any()) }
    }

    @Test
    fun `same student on different dates are not duplicates`() = runBlocking {
        // Arrange
        val record1 = createAttendanceRecord(studentId = testStudent1, date = "2025-11-13")
        val record2 = createAttendanceRecord(studentId = testStudent1, date = "2025-11-14")
        coEvery { mockDao.getAttendanceById(any()) } returns null
        coEvery { mockDao.insertAttendance(any()) } returns Unit

        // Act
        val result1 = manager.saveAttendanceWithDeduplication(record1)
        val result2 = manager.saveAttendanceWithDeduplication(record2)

        // Assert
        assertTrue(result1)
        assertTrue(result2)
        coVerify(exactly = 2) { mockDao.insertAttendance(any()) }
    }

    // ==================== DETECT DUPLICATES TESTS ====================

    @Test
    fun `detect duplicates in list with identical student-date pairs`() = runBlocking {
        // Arrange
        val timestamp1 = System.currentTimeMillis()
        val timestamp2 = System.currentTimeMillis() + 1000
        val records = listOf(
            createAttendanceRecord(modifiedAt = timestamp1),
            createAttendanceRecord(modifiedAt = timestamp2)  // Newer
        )

        // Act
        val result = manager.detectDuplicates(records)

        // Assert
        assertEquals(1, result.unique.size)
        assertEquals(1, result.removedCount)
        assertEquals(2, result.totalAnalyzed)
        assertTrue(result.unique[0].modifiedAt == timestamp2)  // Keep newer
    }

    @Test
    fun `no duplicates in list with different student-date combinations`() = runBlocking {
        // Arrange
        val records = listOf(
            createAttendanceRecord(studentId = testStudent1, date = "2025-11-13"),
            createAttendanceRecord(studentId = testStudent1, date = "2025-11-14"),
            createAttendanceRecord(studentId = testStudent2, date = "2025-11-13")
        )

        // Act
        val result = manager.detectDuplicates(records)

        // Assert
        assertEquals(3, result.unique.size)
        assertEquals(0, result.removedCount)
    }

    @Test
    fun `multiple duplicates of same student-date`() = runBlocking {
        // Arrange
        val timestamps = listOf(100L, 200L, 300L, 400L)  // Highest = 400L
        val records = timestamps.map { createAttendanceRecord(modifiedAt = it) }

        // Act
        val result = manager.detectDuplicates(records)

        // Assert
        assertEquals(1, result.unique.size)
        assertEquals(3, result.removedCount)
        assertEquals(400L, result.unique[0].modifiedAt)  // Keep the one with highest timestamp
    }

    @Test
    fun `empty list returns empty result`() = runBlocking {
        // Arrange
        val records = emptyList<AttendanceRecord>()

        // Act
        val result = manager.detectDuplicates(records)

        // Assert
        assertEquals(0, result.unique.size)
        assertEquals(0, result.removedCount)
    }

    @Test
    fun `single record returns as unique`() = runBlocking {
        // Arrange
        val records = listOf(createAttendanceRecord())

        // Act
        val result = manager.detectDuplicates(records)

        // Assert
        assertEquals(1, result.unique.size)
        assertEquals(0, result.removedCount)
    }

    @Test
    fun `handles null timestamps correctly`() = runBlocking {
        // Arrange - timestamps of 0 should not cause issues
        val records = listOf(
            createAttendanceRecord(modifiedAt = 0),
            createAttendanceRecord(modifiedAt = 1)
        )

        // Act
        val result = manager.detectDuplicates(records)

        // Assert
        assertEquals(1, result.unique.size)
        assertEquals(1000L, result.unique[0].modifiedAt)  // Should keep non-zero timestamp
    }

    // ==================== VALIDATE BATCH TESTS ====================

    @Test
    fun `validate batch with all valid records`() = runBlocking {
        // Arrange
        val records = listOf(
            createAttendanceRecord(studentId = testStudent1, date = "2025-11-14"),
            createAttendanceRecord(studentId = testStudent2, date = "2025-11-14")
        )
        coEvery { mockDao.getAttendanceById(any()) } returns null

        // Act
        val result = manager.validateBatch(records)

        // Assert
        assertTrue(result.isValid)
        assertEquals(2, result.validRecords)
        assertEquals(0, result.issues.size)
    }

    @Test
    fun `validate batch detects invalid dates`() = runBlocking {
        // Arrange
        val records = listOf(
            createAttendanceRecord(date = ""),  // Invalid empty date
            createAttendanceRecord(date = "not-a-date"),  // Invalid format
            createAttendanceRecord(date = "2025-11-14")   // Valid
        )

        // Act
        val result = manager.validateBatch(records)

        // Assert
        assertFalse(result.isValid)
        assertEquals(2, result.issues.size)  // 2 date validation issues
    }

    @Test
    fun `validate batch detects missing student IDs`() = runBlocking {
        // Arrange
        val records = listOf(
            createAttendanceRecord(studentId = ""),  // Invalid
            createAttendanceRecord(studentId = testStudent1)  // Valid
        )

        // Act
        val result = manager.validateBatch(records)

        // Assert
        assertFalse(result.isValid)
        assertEquals(1, result.issues.size)
    }

    @Test
    fun `validate batch detects invalid timestamps`() = runBlocking {
        // Arrange
        val records = listOf(
            createAttendanceRecord(modifiedAt = 0),  // Invalid
            createAttendanceRecord(modifiedAt = 1)   // Valid (> 0)
        )

        // Act
        val result = manager.validateBatch(records)

        // Assert
        assertFalse(result.isValid)
        assertEquals(1, result.issues.size)
    }

    @Test
    fun `validate batch detects duplicate student-date pairs`() = runBlocking {
        // Arrange
        val records = listOf(
            createAttendanceRecord(studentId = testStudent1, date = "2025-11-14", modifiedAt = 100),
            createAttendanceRecord(studentId = testStudent1, date = "2025-11-14", modifiedAt = 200)
        )

        // Act
        val result = manager.validateBatch(records)

        // Assert
        assertFalse(result.isValid)
        assertEquals(1, result.duplicatesDetected)
    }

    @Test
    fun `validate empty batch is valid`() = runBlocking {
        // Arrange
        val records = emptyList<AttendanceRecord>()

        // Act
        val result = manager.validateBatch(records)

        // Assert
        assertTrue(result.isValid)
        assertEquals(0, result.validRecords)
    }

    // ==================== INTEGRITY CHECK TESTS ====================

    @Test
    fun `integrity check on healthy database`() = runBlocking {
        // Arrange
        coEvery { mockDao.getUnsyncedAttendance() } returns listOf(
            createAttendanceEntity(id = "$testStudent1-2025-11-13"),
            createAttendanceEntity(id = "$testStudent2-2025-11-14")
        )
        coEvery { mockDao.deleteAttendanceById(any()) } returns Unit

        // Act
        val result = manager.performIntegrityCheck()

        // Assert
        assertTrue(result.isHealthy)
        assertEquals(2, result.totalRecords)
        assertEquals(0, result.duplicatesFound)
    }

    @Test
    fun `integrity check detects and removes duplicates`() = runBlocking {
        // Arrange
        val duplicateId = "$testStudent1-2025-11-14-dup"
        coEvery { mockDao.getUnsyncedAttendance() } returns listOf(
            createAttendanceEntity(id = "$testStudent1-2025-11-14", lastModified = 100),
            createAttendanceEntity(id = duplicateId, lastModified = 50)  // Older
        )
        coEvery { mockDao.deleteAttendanceById(any()) } returns Unit

        // Act
        val result = manager.performIntegrityCheck()

        // Assert
        assertFalse(result.isHealthy)
        assertEquals(2, result.totalRecords)
        assertEquals(1, result.duplicatesFound)
        assertEquals(1, result.duplicatesRemoved)
        verify { mockDao.deleteAttendanceById(duplicateId) }
    }

    @Test
    fun `integrity check handles deletion errors gracefully`() = runBlocking {
        // Arrange
        coEvery { mockDao.getUnsyncedAttendance() } returns listOf(
            createAttendanceEntity(id = "$testStudent1-2025-11-14", lastModified = 100),
            createAttendanceEntity(id = "$testStudent1-2025-11-14-dup", lastModified = 50)
        )
        coEvery { mockDao.deleteAttendanceById(any()) } throws Exception("Delete failed")

        // Act
        val result = manager.performIntegrityCheck()

        // Assert
        assertFalse(result.isHealthy)
        assertEquals(1, result.duplicatesFound)
        assertEquals(0, result.duplicatesRemoved)  // Couldn't delete
    }

    @Test
    fun `integrity check on empty database`() = runBlocking {
        // Arrange
        coEvery { mockDao.getUnsyncedAttendance() } returns emptyList()

        // Act
        val result = manager.performIntegrityCheck()

        // Assert
        assertTrue(result.isHealthy)
        assertEquals(0, result.totalRecords)
        assertEquals(0, result.duplicatesFound)
    }

    @Test
    fun `integrity check handles DAO exception`() = runBlocking {
        // Arrange
        coEvery { mockDao.getUnsyncedAttendance() } throws Exception("Database error")

        // Act
        val result = manager.performIntegrityCheck()

        // Assert
        assertFalse(result.isHealthy)
        assertEquals(0, result.totalRecords)
    }

    // ==================== REPORT GENERATION TESTS ====================

    @Test
    fun `generate deduplication report for batch with no duplicates`() = runBlocking {
        // Arrange
        val records = listOf(
            createAttendanceRecord(studentId = testStudent1, date = "2025-11-13"),
            createAttendanceRecord(studentId = testStudent2, date = "2025-11-14")
        )

        // Act
        val report = manager.generateDeduplicationReport(records)

        // Assert
        assertEquals(2, report.totalRecords)
        assertEquals(2, report.uniqueRecords)
        assertEquals(0, report.duplicates)
        assertEquals(0.0, report.deduplicationRate)
    }

    @Test
    fun `generate deduplication report with duplicates`() = runBlocking {
        // Arrange
        val records = listOf(
            createAttendanceRecord(studentId = testStudent1, date = "2025-11-14", modifiedAt = 100),
            createAttendanceRecord(studentId = testStudent1, date = "2025-11-14", modifiedAt = 200),  // Duplicate
            createAttendanceRecord(studentId = testStudent2, date = "2025-11-14")
        )

        // Act
        val report = manager.generateDeduplicationReport(records)

        // Assert
        assertEquals(3, report.totalRecords)
        assertEquals(2, report.uniqueRecords)
        assertEquals(1, report.duplicates)
        assertEquals(33.33, report.deduplicationRate, 0.1)
    }

    @Test
    fun `report includes duplicates grouped by student`() = runBlocking {
        // Arrange
        val records = listOf(
            createAttendanceRecord(studentId = testStudent1, date = "2025-11-14", modifiedAt = 100),
            createAttendanceRecord(studentId = testStudent1, date = "2025-11-14", modifiedAt = 200),
            createAttendanceRecord(studentId = testStudent2, date = "2025-11-14", modifiedAt = 100),
            createAttendanceRecord(studentId = testStudent2, date = "2025-11-14", modifiedAt = 200)
        )

        // Act
        val report = manager.generateDeduplicationReport(records)

        // Assert
        assertEquals(2, report.duplicatesByStudent.size)
        assertTrue(report.duplicatesByStudent.containsKey(testStudent1))
        assertTrue(report.duplicatesByStudent.containsKey(testStudent2))
    }

    @Test
    fun `report includes duplicates grouped by date`() = runBlocking {
        // Arrange
        val date1 = "2025-11-13"
        val date2 = "2025-11-14"
        val records = listOf(
            createAttendanceRecord(studentId = testStudent1, date = date1, modifiedAt = 100),
            createAttendanceRecord(studentId = testStudent1, date = date1, modifiedAt = 200),
            createAttendanceRecord(studentId = testStudent2, date = date2, modifiedAt = 100),
            createAttendanceRecord(studentId = testStudent2, date = date2, modifiedAt = 200)
        )

        // Act
        val report = manager.generateDeduplicationReport(records)

        // Assert
        assertEquals(2, report.duplicatesByDate.size)
        assertTrue(report.duplicatesByDate.containsKey(date1))
        assertTrue(report.duplicatesByDate.containsKey(date2))
    }

    @Test
    fun `report on empty batch`() = runBlocking {
        // Arrange
        val records = emptyList<AttendanceRecord>()

        // Act
        val report = manager.generateDeduplicationReport(records)

        // Assert
        assertEquals(0, report.totalRecords)
        assertEquals(0, report.uniqueRecords)
        assertEquals(0, report.duplicates)
        assertEquals(0.0, report.deduplicationRate)
    }

    // ==================== DEDUPLICATE BEFORE SYNC TESTS ====================

    @Test
    fun `deduplicate before sync removes duplicates from unsynced items`() = runBlocking {
        // Arrange
        val records = listOf(
            createAttendanceRecord(studentId = testStudent1, date = "2025-11-14", modifiedAt = 100),
            createAttendanceRecord(studentId = testStudent1, date = "2025-11-14", modifiedAt = 200)  // Duplicate
        )

        // Act
        val result = manager.deduplicateBeforeSync(records)

        // Assert
        assertEquals(1, result.size)  // Only unique record
        assertEquals(200L, result[0].modifiedAt)  // The newer one
    }

    @Test
    fun `deduplicate before sync preserves all unique records`() = runBlocking {
        // Arrange
        val records = listOf(
            createAttendanceRecord(studentId = testStudent1, date = "2025-11-13"),
            createAttendanceRecord(studentId = testStudent2, date = "2025-11-14"),
            createAttendanceRecord(studentId = testStudent1, date = "2025-11-15")
        )

        // Act
        val result = manager.deduplicateBeforeSync(records)

        // Assert
        assertEquals(3, result.size)
    }

    // ==================== EDGE CASES & CONCURRENCY ====================

    @Test
    fun `handle concurrent save operations sequentially (Mutex protected)`() = runBlocking {
        // Arrange
        val records = (1..5).map {
            createAttendanceRecord(studentId = "student_$it", date = "2025-11-14")
        }
        coEvery { mockDao.getAttendanceById(any()) } returns null
        coEvery { mockDao.insertAttendance(any()) } returns Unit

        // Act
        val results = records.map { record ->
            manager.saveAttendanceWithDeduplication(record)
        }

        // Assert
        assertTrue(results.all { it })
        coVerify(exactly = 5) { mockDao.insertAttendance(any()) }
    }

    @Test
    fun `handles very long lists efficiently`() = runBlocking {
        // Arrange
        val records = (1..1000).map {
            createAttendanceRecord(
                studentId = "student_${it % 100}",
                date = "2025-11-${14 + (it / 100)}",
                modifiedAt = (System.currentTimeMillis() + it)
            )
        }

        // Act
        val result = manager.detectDuplicates(records)

        // Assert
        assertEquals(1000, result.totalAnalyzed)
        assertTrue(result.unique.size <= records.size)
    }
}
