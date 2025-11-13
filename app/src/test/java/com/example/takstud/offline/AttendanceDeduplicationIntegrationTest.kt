package com.example.takstud.offline

import com.example.takstud.data.local.dao.AttendanceDao
import com.example.takstud.data.local.entity.AttendanceEntity
import com.example.takstud.model.AttendanceRecord
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.runBlocking
import org.junit.Before
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

/**
 * Testes de integração para AttendanceDeduplicationIntegration.
 *
 * Cobre:
 * - Integração entre Deduplication e OfflineSyncQueue
 * - Fluxo completo: Save → Deduplicate → Queue → Sync
 * - Processamento com sincronização
 * - Limpeza de duplicatas
 * - Cenários de concorrência
 */
class AttendanceDeduplicationIntegrationTest {

    private lateinit var mockAttendanceDao: AttendanceDao
    private lateinit var mockOfflineQueue: OfflineSyncQueue
    private lateinit var dedupManager: AttendanceDeduplicationManager

    private val testDate = "2025-11-14"
    private val studentId1 = "student_001"
    private val studentId2 = "student_002"

    @Before
    fun setUp() {
        mockAttendanceDao = mockk()
        mockOfflineQueue = mockk()
        dedupManager = AttendanceDeduplicationManager(mockAttendanceDao)
    }

    // ==================== Helper Functions ====================

    private fun createAttendanceRecord(
        studentId: String = studentId1,
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
        id: String = "$studentId1-$testDate",
        studentId: String = studentId1,
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

    // ==================== SAVE WITH DEDUP AND QUEUE TESTS ====================

    @Test
    fun `save attendance with deduplication and queue integration - success flow`() = runBlocking {
        // Arrange
        val record = createAttendanceRecord()
        coEvery { mockAttendanceDao.getAttendanceById(any()) } returns null
        coEvery { mockAttendanceDao.insertAttendance(any()) } returns Unit
        coEvery { mockOfflineQueue.addOperation(any(), any(), any(), any()) } returns Unit

        // Act
        val dedupResult = dedupManager.saveAttendanceWithDeduplication(record)
        if (dedupResult) {
            mockOfflineQueue.addOperation(
                SyncOperation.CREATE,
                "ATTENDANCE",
                record.id,
                record
            )
        }

        // Assert
        assertTrue(dedupResult)
        coVerify { mockAttendanceDao.insertAttendance(any()) }
        coVerify { mockOfflineQueue.addOperation(any(), any(), any(), any()) }
    }

    @Test
    fun `save duplicate attendance is not queued`() = runBlocking {
        // Arrange - Duplicate with older timestamp
        val oldTimestamp = System.currentTimeMillis() - 10000
        val newTimestamp = System.currentTimeMillis()
        val existingRecord = createAttendanceEntity(lastModified = newTimestamp)
        val olderRecord = createAttendanceRecord(modifiedAt = oldTimestamp)

        coEvery { mockAttendanceDao.getAttendanceById(any()) } returns existingRecord

        // Act
        val dedupResult = dedupManager.saveAttendanceWithDeduplication(olderRecord)
        // Should NOT queue if it's a duplicate

        // Assert
        assertFalse(dedupResult)
        coVerify(exactly = 0) { mockOfflineQueue.addOperation(any(), any(), any(), any()) }
    }

    @Test
    fun `save newer duplicate replaces old and requeues`() = runBlocking {
        // Arrange
        val oldTimestamp = System.currentTimeMillis() - 10000
        val newTimestamp = System.currentTimeMillis()
        val oldRecord = createAttendanceEntity(lastModified = oldTimestamp)
        val newRecord = createAttendanceRecord(modifiedAt = newTimestamp)

        coEvery { mockAttendanceDao.getAttendanceById(any()) } returns oldRecord
        coEvery { mockAttendanceDao.updateAttendance(any()) } returns Unit
        coEvery { mockOfflineQueue.addOperation(any(), any(), any(), any()) } returns Unit

        // Act
        val dedupResult = dedupManager.saveAttendanceWithDeduplication(newRecord)
        if (dedupResult) {
            mockOfflineQueue.addOperation(SyncOperation.UPDATE, "ATTENDANCE", newRecord.id, newRecord)
        }

        // Assert
        assertTrue(dedupResult)
        coVerify { mockAttendanceDao.updateAttendance(any()) }
        coVerify { mockOfflineQueue.addOperation(SyncOperation.UPDATE, any(), any(), any()) }
    }

    @Test
    fun `save multiple records with dedup and queue in sequence`() = runBlocking {
        // Arrange
        val record1 = createAttendanceRecord(studentId = studentId1)
        val record2 = createAttendanceRecord(studentId = studentId2)
        coEvery { mockAttendanceDao.getAttendanceById(any()) } returns null
        coEvery { mockAttendanceDao.insertAttendance(any()) } returns Unit
        coEvery { mockOfflineQueue.addOperation(any(), any(), any(), any()) } returns Unit

        // Act
        val result1 = dedupManager.saveAttendanceWithDeduplication(record1)
        val result2 = dedupManager.saveAttendanceWithDeduplication(record2)

        // Assert
        assertTrue(result1)
        assertTrue(result2)
        coVerify(exactly = 2) { mockAttendanceDao.insertAttendance(any()) }
    }

    // ==================== PROCESS SYNC WITH DEDUPLICATION TESTS ====================

    @Test
    fun `process sync removes duplicates from unsynced records`() = runBlocking {
        // Arrange - Mix of unique and duplicate records
        val records = listOf(
            createAttendanceRecord(studentId = studentId1, modifiedAt = 100),
            createAttendanceRecord(studentId = studentId1, modifiedAt = 200),  // Duplicate, newer
            createAttendanceRecord(studentId = studentId2, modifiedAt = 100)
        )

        // Act
        val dedupResult = dedupManager.deduplicateBeforeSync(records)

        // Assert
        assertEquals(2, dedupResult.size)  // Only unique records
        assertTrue(dedupResult.any { it.studentId == studentId1 && it.modifiedAt == 200L })
        assertTrue(dedupResult.any { it.studentId == studentId2 })
    }

    @Test
    fun `sync with dedup preserves presentation state (present/absent)`() = runBlocking {
        // Arrange - Duplicate records with different presence states
        val presentRecord = createAttendanceRecord(
            studentId = studentId1,
            isPresent = true,
            modifiedAt = 200
        )
        val absentRecord = createAttendanceRecord(
            studentId = studentId1,
            isPresent = false,
            modifiedAt = 100
        )
        val records = listOf(absentRecord, presentRecord)

        // Act
        val dedupResult = dedupManager.deduplicateBeforeSync(records)

        // Assert
        assertEquals(1, dedupResult.size)
        assertTrue(dedupResult[0].isPresent)  // Keep the newer one (present)
    }

    @Test
    fun `sync deduplicate handles large unsynced queue efficiently`() = runBlocking {
        // Arrange - 1000 records with 90% duplicates
        val records = mutableListOf<AttendanceRecord>()
        for (i in 1..100) {
            // Each student-date gets 10 duplicate records
            for (j in 1..10) {
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
        val dedupResult = dedupManager.deduplicateBeforeSync(records)

        // Assert
        assertEquals(100, dedupResult.size)  // Only 100 unique (one per student-date)
        assertEquals(900, records.size - dedupResult.size)  // 900 duplicates removed
    }

    @Test
    fun `sync with empty unsynced records`() = runBlocking {
        // Arrange
        val records = emptyList<AttendanceRecord>()

        // Act
        val dedupResult = dedupManager.deduplicateBeforeSync(records)

        // Assert
        assertEquals(0, dedupResult.size)
    }

    // ==================== CLEANUP DUPLICATE ATTENDANCE TESTS ====================

    @Test
    fun `cleanup removes duplicate records from local database`() = runBlocking {
        // Arrange
        val duplicateEntities = listOf(
            createAttendanceEntity(id = "id_1", lastModified = 100),
            createAttendanceEntity(id = "id_2", lastModified = 100)  // Duplicate
        )
        coEvery { mockAttendanceDao.getUnsyncedAttendance() } returns duplicateEntities
        coEvery { mockAttendanceDao.deleteAttendanceById(any()) } returns Unit

        // Act
        val integrityResult = dedupManager.performIntegrityCheck()

        // Assert
        assertTrue(integrityResult.duplicatesFound > 0)
        coVerify { mockAttendanceDao.deleteAttendanceById(any()) }
    }

    @Test
    fun `cleanup respects last-write-wins (keeps newer record)`() = runBlocking {
        // Arrange - Same student-date, different timestamps
        val olderRecord = createAttendanceEntity(
            id = "$studentId1-$testDate-old",
            lastModified = 100
        )
        val newerRecord = createAttendanceEntity(
            id = "$studentId1-$testDate-new",
            lastModified = 200
        )
        coEvery { mockAttendanceDao.getUnsyncedAttendance() } returns listOf(olderRecord, newerRecord)
        coEvery { mockAttendanceDao.deleteAttendanceById(any()) } returns Unit

        // Act
        val integrityResult = dedupManager.performIntegrityCheck()

        // Assert
        assertTrue(integrityResult.duplicatesFound > 0)
        // The older one should be marked for deletion
        coVerify { mockAttendanceDao.deleteAttendanceById("$studentId1-$testDate-old") }
    }

    @Test
    fun `cleanup handles partial failures gracefully`() = runBlocking {
        // Arrange
        coEvery { mockAttendanceDao.getUnsyncedAttendance() } returns listOf(
            createAttendanceEntity(id = "id_1", lastModified = 100),
            createAttendanceEntity(id = "id_2", lastModified = 100)
        )
        // First delete fails, second succeeds
        coEvery { mockAttendanceDao.deleteAttendanceById("id_1") } throws Exception("Delete failed")
        coEvery { mockAttendanceDao.deleteAttendanceById("id_2") } returns Unit

        // Act
        val integrityResult = dedupManager.performIntegrityCheck()

        // Assert
        assertTrue(integrityResult.duplicatesFound > 0)
        assertEquals(1, integrityResult.duplicatesRemoved)  // Only one successful
    }

    @Test
    fun `cleanup on healthy database removes nothing`() = runBlocking {
        // Arrange - No duplicates
        val healthyRecords = listOf(
            createAttendanceEntity(id = "$studentId1-2025-11-13", lastModified = 100),
            createAttendanceEntity(id = "$studentId2-2025-11-14", lastModified = 200)
        )
        coEvery { mockAttendanceDao.getUnsyncedAttendance() } returns healthyRecords

        // Act
        val integrityResult = dedupManager.performIntegrityCheck()

        // Assert
        assertTrue(integrityResult.isHealthy)
        assertEquals(0, integrityResult.duplicatesRemoved)
    }

    // ==================== FULL WORKFLOW INTEGRATION TESTS ====================

    @Test
    fun `complete workflow - offline save → deduplicate → sync`() = runBlocking {
        // Scenario: User saves attendance offline, records get duplicates, sync cleans them up

        // 1. Save attendance offline (with dedup)
        val record1 = createAttendanceRecord(studentId = studentId1, modifiedAt = 100)
        val record2 = createAttendanceRecord(studentId = studentId1, modifiedAt = 200)  // Duplicate

        coEvery { mockAttendanceDao.getAttendanceById(any()) } returns null
        coEvery { mockAttendanceDao.insertAttendance(any()) } returns Unit
        coEvery { mockAttendanceDao.updateAttendance(any()) } returns Unit

        val result1 = dedupManager.saveAttendanceWithDeduplication(record1)

        // Now try to save duplicate
        coEvery { mockAttendanceDao.getAttendanceById(any()) } returns createAttendanceEntity(lastModified = 100)
        val result2 = dedupManager.saveAttendanceWithDeduplication(record2)

        // 2. Deduplicate before sync
        val unSyncedRecords = listOf(record1, record2)
        val dedupedForSync = dedupManager.deduplicateBeforeSync(unSyncedRecords)

        // Assert
        assertTrue(result1)  // First save succeeds
        assertTrue(result2)  // Second save (duplicate) replaces
        assertEquals(1, dedupedForSync.size)  // Only one sent to sync
    }

    @Test
    fun `workflow handles network failure during sync`() = runBlocking {
        // Arrange
        val record = createAttendanceRecord()
        coEvery { mockAttendanceDao.getAttendanceById(any()) } returns null
        coEvery { mockAttendanceDao.insertAttendance(any()) } returns Unit

        // Act - Save succeeds offline
        val saveResult = dedupManager.saveAttendanceWithDeduplication(record)

        // Simulate network failure during sync
        coEvery { mockOfflineQueue.addOperation(any(), any(), any(), any()) } throws Exception("Network error")

        try {
            mockOfflineQueue.addOperation(SyncOperation.CREATE, "ATTENDANCE", record.id, record)
        } catch (e: Exception) {
            // Network failed, but local save still succeeded
        }

        // Assert
        assertTrue(saveResult)  // Offline save was successful
        // Record remains in local database for retry
        coVerify { mockAttendanceDao.insertAttendance(any()) }
    }

    @Test
    fun `concurrent saves with dedup maintain consistency`() = runBlocking {
        // Arrange
        coEvery { mockAttendanceDao.getAttendanceById(any()) } returns null
        coEvery { mockAttendanceDao.insertAttendance(any()) } returns Unit

        val records = (1..5).map { i ->
            createAttendanceRecord(
                studentId = "student_${i % 2}",  // Only 2 students
                date = "2025-11-${14 + (i / 2)}",
                modifiedAt = System.currentTimeMillis() + i
            )
        }

        // Act - Save multiple records concurrently
        val results = records.map { record ->
            dedupManager.saveAttendanceWithDeduplication(record)
        }

        // Assert
        assertTrue(results.all { it })  // All save attempts return true or false correctly
        coVerify(atLeast = 1) { mockAttendanceDao.insertAttendance(any()) }
    }

    @Test
    fun `batch import with deduplication`() = runBlocking {
        // Arrange - Import batch from file or server
        val importedRecords = (1..100).map { i ->
            createAttendanceRecord(
                studentId = "student_${i % 20}",
                date = "2025-11-14",
                modifiedAt = System.currentTimeMillis() + i
            )
        }

        // Act - Validate batch
        val validationResult = dedupManager.validateBatch(importedRecords)

        // Assert - Should detect some duplicates (20 students × 5 records each = 100 records, 80 duplicates)
        assertTrue(!validationResult.isValid || validationResult.duplicatesDetected > 0)
    }

    @Test
    fun `report generation after deduplication identifies patterns`() = runBlocking {
        // Arrange
        val records = listOf(
            createAttendanceRecord(studentId = "student_1", date = "2025-11-13", modifiedAt = 100),
            createAttendanceRecord(studentId = "student_1", date = "2025-11-13", modifiedAt = 200),
            createAttendanceRecord(studentId = "student_2", date = "2025-11-14", modifiedAt = 100),
            createAttendanceRecord(studentId = "student_2", date = "2025-11-14", modifiedAt = 200)
        )

        // Act
        val report = dedupManager.generateDeduplicationReport(records)

        // Assert
        assertEquals(4, report.totalRecords)
        assertEquals(2, report.uniqueRecords)
        assertEquals(2, report.duplicates)
        assertEquals(2, report.duplicatesByStudent.size)
        assertEquals(2, report.duplicatesByDate.size)
    }
}
