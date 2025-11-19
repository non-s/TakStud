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
 * Testes para AttendanceDeduplicationManager.
 *
 * Valida:
 * - Detecção de duplicatas
 * - Resolução de conflitos (Last-Write-Wins)
 * - Deduplicação antes de sync
 * - Validação de lote
 * - Verificação de integridade
 * - Geração de relatórios
 */
class AttendanceDeduplicationManagerTest {

    private lateinit var mockDao: AttendanceDao
    private lateinit var manager: AttendanceDeduplicationManager

    private val studentId = "student_001"
    private val date = "2025-11-13"
    private val compositeKey = "$studentId-$date"

    @Before
    fun setUp() {
        mockDao = mockk()
        manager = AttendanceDeduplicationManager(mockDao)
    }

    // ==================== SAVE WITH DEDUPLICATION TESTS ====================

    @Test
    fun `save attendance when new record should insert successfully`() = runBlocking {
        // Arrange
        val record = createAttendanceRecord(
            studentId = studentId,
            date = date,
            isPresent = true,
            modifiedAt = 1000
        )
        coEvery { mockDao.getAttendanceById(compositeKey) } returns null
        coEvery { mockDao.insertAttendance(any()) } returns Unit

        // Act
        val result = manager.saveAttendanceWithDeduplication(record)

        // Assert
        assertTrue(result)
        coVerify(exactly = 1) { mockDao.insertAttendance(any()) }
    }

    @Test
    fun `save attendance when newer than existing should update`() = runBlocking {
        // Arrange
        val existing = createAttendanceEntity(studentId, date, lastModified = 1000)
        val newRecord = createAttendanceRecord(
            studentId = studentId,
            date = date,
            isPresent = false,
            modifiedAt = 2000  // Mais recente
        )

        coEvery { mockDao.getAttendanceById(compositeKey) } returns existing
        coEvery { mockDao.updateAttendance(any()) } returns Unit

        // Act
        val result = manager.saveAttendanceWithDeduplication(newRecord)

        // Assert
        assertTrue(result)
        coVerify(exactly = 1) { mockDao.updateAttendance(any()) }
    }

    @Test
    fun `save attendance when older than existing should discard`() = runBlocking {
        // Arrange
        val existing = createAttendanceEntity(studentId, date, lastModified = 2000)
        val olderRecord = createAttendanceRecord(
            studentId = studentId,
            date = date,
            isPresent = true,
            modifiedAt = 1000  // Mais antigo
        )

        coEvery { mockDao.getAttendanceById(compositeKey) } returns existing

        // Act
        val result = manager.saveAttendanceWithDeduplication(olderRecord)

        // Assert
        assertFalse(result)
        coVerify(exactly = 0) { mockDao.updateAttendance(any()) }
        coVerify(exactly = 0) { mockDao.insertAttendance(any()) }
    }

    // ==================== DETECT DUPLICATES TESTS ====================

    @Test
    fun `detect duplicates with no duplicates should return all records as unique`() = runBlocking {
        // Arrange
        val records = listOf(
            createAttendanceRecord("student_001", "2025-11-13", modifiedAt = 1000),
            createAttendanceRecord("student_002", "2025-11-13", modifiedAt = 1000),
            createAttendanceRecord("student_001", "2025-11-14", modifiedAt = 1000)
        )

        // Act
        val result = manager.detectDuplicates(records)

        // Assert
        assertEquals(3, result.unique.size)
        assertEquals(0, result.duplicates.size)
        assertEquals(0, result.removedCount)
    }

    @Test
    fun `detect duplicates with exact duplicates should remove older ones`() = runBlocking {
        // Arrange
        val records = listOf(
            createAttendanceRecord(studentId, date, isPresent = true, modifiedAt = 1000),
            createAttendanceRecord(studentId, date, isPresent = false, modifiedAt = 2000),  // Mais recente
            createAttendanceRecord(studentId, date, isPresent = true, modifiedAt = 1500)   // Intermediário
        )

        // Act
        val result = manager.detectDuplicates(records)

        // Assert
        assertEquals(1, result.unique.size)
        assertEquals(2, result.duplicates.size)
        assertEquals(2, result.removedCount)
        // A mais recente deve ser mantida
        assertEquals(2000, result.unique[0].modifiedAt)
        assertEquals(false, result.unique[0].isPresent)
    }

    @Test
    fun `detect duplicates with multiple conflicts should resolve all`() = runBlocking {
        // Arrange
        val records = listOf(
            // Conflito 1: student_001, 2025-11-13
            createAttendanceRecord("student_001", "2025-11-13", isPresent = true, modifiedAt = 1000),
            createAttendanceRecord("student_001", "2025-11-13", isPresent = false, modifiedAt = 2000),

            // Conflito 2: student_002, 2025-11-13
            createAttendanceRecord("student_002", "2025-11-13", isPresent = true, modifiedAt = 1500),
            createAttendanceRecord("student_002", "2025-11-13", isPresent = false, modifiedAt = 1200),

            // Sem conflito
            createAttendanceRecord("student_003", "2025-11-13", isPresent = true, modifiedAt = 1000)
        )

        // Act
        val result = manager.detectDuplicates(records)

        // Assert
        assertEquals(3, result.unique.size)  // 3 registros únicos
        assertEquals(2, result.duplicates.size)  // 2 duplicatas removidas
        assertEquals(2, result.conflictResolutions)
    }

    @Test
    fun `detect duplicates should keep most recent record`() = runBlocking {
        // Arrange
        val old = createAttendanceRecord(studentId, date, isPresent = false, modifiedAt = 1000)
        val new = createAttendanceRecord(studentId, date, isPresent = true, modifiedAt = 3000)
        val mid = createAttendanceRecord(studentId, date, isPresent = true, modifiedAt = 2000)

        // Act
        val result = manager.detectDuplicates(listOf(old, new, mid))

        // Assert
        assertEquals(1, result.unique.size)
        assertEquals(new.modifiedAt, result.unique[0].modifiedAt)
        assertTrue(result.unique[0].isPresent)
    }

    // ==================== DEDUPLICATE BEFORE SYNC TESTS ====================

    @Test
    fun `deduplicate before sync should return unique records`() = runBlocking {
        // Arrange
        val records = listOf(
            createAttendanceRecord(studentId, date, modifiedAt = 1000),
            createAttendanceRecord(studentId, date, modifiedAt = 2000),  // Duplicata
            createAttendanceRecord("student_002", date, modifiedAt = 1000)
        )

        // Act
        val result = manager.deduplicateBeforeSync(records)

        // Assert
        assertEquals(2, result.size)  // 2 registros únicos
        assertTrue(result.any { it.studentId == "student_002" })
        assertTrue(result.any { it.studentId == studentId && it.modifiedAt == 2000L })
    }

    @Test
    fun `deduplicate before sync with no duplicates should return same list`() = runBlocking {
        // Arrange
        val records = listOf(
            createAttendanceRecord("student_001", "2025-11-13", modifiedAt = 1000),
            createAttendanceRecord("student_002", "2025-11-14", modifiedAt = 1000)
        )

        // Act
        val result = manager.deduplicateBeforeSync(records)

        // Assert
        assertEquals(2, result.size)
    }

    // ==================== INTEGRITY CHECK TESTS ====================

    @Test
    fun `perform integrity check with healthy database should return no duplicates`() = runBlocking {
        // Arrange
        val entities = listOf(
            createAttendanceEntity("student_001", "2025-11-13"),
            createAttendanceEntity("student_002", "2025-11-14")
        )
        coEvery { mockDao.getUnsyncedAttendance() } returns entities

        // Act
        val result = manager.performIntegrityCheck()

        // Assert
        assertTrue(result.isHealthy)
        assertEquals(2, result.totalRecords)
        assertEquals(0, result.duplicatesFound)
        assertEquals(0, result.duplicatesRemoved)
    }

    @Test
    fun `perform integrity check with duplicates should detect and remove them`() = runBlocking {
        // Arrange
        val entities = listOf(
            createAttendanceEntity("student_001", "2025-11-13", id = "id_1", lastModified = 1000),
            createAttendanceEntity("student_001", "2025-11-13", id = "id_2", lastModified = 2000),  // Duplicata
            createAttendanceEntity("student_002", "2025-11-13", id = "id_3")
        )
        coEvery { mockDao.getUnsyncedAttendance() } returns entities
        coEvery { mockDao.deleteAttendanceById(any()) } returns Unit

        // Act
        val result = manager.performIntegrityCheck()

        // Assert
        assertFalse(result.isHealthy)
        assertEquals(3, result.totalRecords)
        assertEquals(1, result.duplicatesFound)
        assertEquals(1, result.duplicatesRemoved)
        coVerify(exactly = 1) { mockDao.deleteAttendanceById("id_1") }
    }

    // ==================== VALIDATE BATCH TESTS ====================

    @Test
    fun `validate batch with valid records should return all valid`() = runBlocking {
        // Arrange
        val records = listOf(
            createAttendanceRecord("student_001", "2025-11-13"),
            createAttendanceRecord("student_002", "2025-11-14"),
            createAttendanceRecord("student_003", "2025-11-15")
        )
        coEvery { mockDao.getAttendanceById(any()) } returns null
        coEvery { mockDao.insertAttendance(any()) } returns Unit

        // Act
        val result = manager.validateBatch(records)

        // Assert
        assertTrue(result.isValid)
        assertEquals(3, result.validRecords)
        assertEquals(0, result.issues.size)
    }

    @Test
    fun `validate batch with invalid dates should detect issues`() = runBlocking {
        // Arrange
        val records = listOf(
            createAttendanceRecord("student_001", "invalid-date"),  // Data inválida
            createAttendanceRecord("student_002", "")  // Data vazia
        )

        // Act
        val result = manager.validateBatch(records)

        // Assert
        assertFalse(result.isValid)
        assertEquals(2, result.issues.size)
    }

    @Test
    fun `validate batch with missing student id should detect issue`() = runBlocking {
        // Arrange
        val records = listOf(
            createAttendanceRecord("", "2025-11-13")  // StudentId vazio
        )

        // Act
        val result = manager.validateBatch(records)

        // Assert
        assertFalse(result.isValid)
        assertTrue(result.issues.any { it.field == "Student ID inválido" })
    }

    @Test
    fun `validate batch with duplicates should detect them`() = runBlocking {
        // Arrange
        val records = listOf(
            createAttendanceRecord("student_001", "2025-11-13", modifiedAt = 1000),
            createAttendanceRecord("student_001", "2025-11-13", modifiedAt = 2000)
        )

        // Act
        val result = manager.validateBatch(records)

        // Assert
        assertEquals(1, result.duplicatesDetected)
    }

    // ==================== GENERATE REPORT TESTS ====================

    @Test
    fun `generate deduplication report with no duplicates should show zero rate`() = runBlocking {
        // Arrange
        val records = listOf(
            createAttendanceRecord("student_001", "2025-11-13"),
            createAttendanceRecord("student_002", "2025-11-13"),
            createAttendanceRecord("student_003", "2025-11-13")
        )

        // Act
        val report = manager.generateDeduplicationReport(records)

        // Assert
        assertEquals(3, report.totalRecords)
        assertEquals(3, report.uniqueRecords)
        assertEquals(0, report.duplicates)
        assertEquals(0.0, report.deduplicationRate)
    }

    @Test
    fun `generate deduplication report with duplicates should calculate rate`() = runBlocking {
        // Arrange
        val records = listOf(
            createAttendanceRecord("student_001", "2025-11-13", modifiedAt = 1000),
            createAttendanceRecord("student_001", "2025-11-13", modifiedAt = 2000),  // Duplicata
            createAttendanceRecord("student_001", "2025-11-13", modifiedAt = 1500)   // Duplicata
        )

        // Act
        val report = manager.generateDeduplicationReport(records)

        // Assert
        assertEquals(3, report.totalRecords)
        assertEquals(1, report.uniqueRecords)
        assertEquals(2, report.duplicates)
        assertEquals((2.0 / 3) * 100, report.deduplicationRate)
    }

    @Test
    fun `generate report should group duplicates by student`() = runBlocking {
        // Arrange
        val records = listOf(
            createAttendanceRecord("student_001", "2025-11-13", modifiedAt = 1000),
            createAttendanceRecord("student_001", "2025-11-13", modifiedAt = 2000),
            createAttendanceRecord("student_002", "2025-11-13", modifiedAt = 1000),
            createAttendanceRecord("student_002", "2025-11-13", modifiedAt = 1500)
        )

        // Act
        val report = manager.generateDeduplicationReport(records)

        // Assert
        assertEquals(2, report.duplicatesByStudent.size)
        assertTrue(report.duplicatesByStudent.containsKey("student_001"))
        assertTrue(report.duplicatesByStudent.containsKey("student_002"))
    }

    @Test
    fun `generate report should group duplicates by date`() = runBlocking {
        // Arrange
        val records = listOf(
            createAttendanceRecord("student_001", "2025-11-13", modifiedAt = 1000),
            createAttendanceRecord("student_001", "2025-11-13", modifiedAt = 2000),
            createAttendanceRecord("student_002", "2025-11-14", modifiedAt = 1000),
            createAttendanceRecord("student_003", "2025-11-14", modifiedAt = 1500)
        )

        // Act
        val report = manager.generateDeduplicationReport(records)

        // Assert
        assertEquals(2, report.duplicatesByDate.size)
        assertTrue(report.duplicatesByDate.containsKey("2025-11-13"))
        assertTrue(report.duplicatesByDate.containsKey("2025-11-14"))
    }

    // ==================== REALISTIC SCENARIOS ====================

    @Test
    fun `scenario - offline user marks same attendance twice`() = runBlocking {
        // Arrange
        val firstMark = createAttendanceRecord(
            studentId = studentId,
            date = date,
            isPresent = true,
            modifiedAt = 1000
        )
        val secondMark = createAttendanceRecord(
            studentId = studentId,
            date = date,
            isPresent = true,
            modifiedAt = 2000  // Marcado novamente
        )

        coEvery { mockDao.getAttendanceById(compositeKey) } returns null andThen
                createAttendanceEntity(studentId, date)
        coEvery { mockDao.insertAttendance(any()) } returns Unit
        coEvery { mockDao.updateAttendance(any()) } returns Unit

        // Act - Primeira marcação
        val result1 = manager.saveAttendanceWithDeduplication(firstMark)

        // Segunda marcação
        val result2 = manager.saveAttendanceWithDeduplication(secondMark)

        // Assert
        assertTrue(result1)  // Primeira marcação inserida
        assertTrue(result2)  // Segunda marcação atualiza (mais recente)
    }

    @Test
    fun `scenario - sync with multiple duplicate records from queue`() = runBlocking {
        // Arrange
        val queueRecords = listOf(
            // Mesmo aluno, mesma data, múltiplas vezes
            createAttendanceRecord("student_001", "2025-11-13", isPresent = true, modifiedAt = 1000),
            createAttendanceRecord("student_001", "2025-11-13", isPresent = false, modifiedAt = 2000),
            createAttendanceRecord("student_001", "2025-11-13", isPresent = true, modifiedAt = 1500),

            // Outro aluno, mesma data, duplicado
            createAttendanceRecord("student_002", "2025-11-13", isPresent = true, modifiedAt = 1000),
            createAttendanceRecord("student_002", "2025-11-13", isPresent = true, modifiedAt = 1100)
        )

        // Act
        val deduplicatedRecords = manager.deduplicateBeforeSync(queueRecords)
        val report = manager.generateDeduplicationReport(queueRecords)

        // Assert
        assertEquals(2, deduplicatedRecords.size)  // Apenas 2 registros únicos
        assertEquals(3, report.duplicates)  // 3 duplicatas removidas
        assertTrue(deduplicatedRecords.any { it.modifiedAt == 2000L })  // Mantém o mais recente
    }

    @Test
    fun `scenario - teacher marks attendance for entire class and detects duplicates`() = runBlocking {
        // Arrange - 30 alunos, 3 deles marcados 2x por erro
        val records = mutableListOf<AttendanceRecord>()
        val date = "2025-11-13"

        for (i in 1..30) {
            val studentId = "student_${String.format("%03d", i)}"
            records.add(createAttendanceRecord(studentId, date, modifiedAt = 1000))

            // Adicionar duplicatas para alunos 1, 2, 3
            if (i <= 3) {
                records.add(createAttendanceRecord(studentId, date, modifiedAt = 2000))
            }
        }

        // Act
        val validationResult = manager.validateBatch(records)
        val deduplicationReport = manager.generateDeduplicationReport(records)

        // Assert
        assertTrue(validationResult.isValid)
        assertEquals(33, validationResult.totalRecords)  // 30 + 3 duplicatas
        assertEquals(3, deduplicationReport.duplicates)
        assertEquals(30, deduplicationReport.uniqueRecords)
    }

    // ==================== ERROR HANDLING TESTS ====================

    @Test
    fun `save attendance handles exceptions gracefully`() = runBlocking {
        // Arrange
        val record = createAttendanceRecord(studentId, date)
        coEvery { mockDao.getAttendanceById(any()) } throws Exception("Database error")

        // Act
        val result = manager.saveAttendanceWithDeduplication(record)

        // Assert
        assertFalse(result)
    }

    @Test
    fun `integrity check handles database errors gracefully`() = runBlocking {
        // Arrange
        coEvery { mockDao.getUnsyncedAttendance() } throws Exception("Database error")

        // Act
        val result = manager.performIntegrityCheck()

        // Assert
        assertFalse(result.isHealthy)
        assertTrue(result.error != null)
        assertEquals(0, result.totalRecords)
    }

    // ==================== HELPER FUNCTIONS ====================

    private fun createAttendanceRecord(
        studentId: String = "student_001",
        date: String = "2025-11-13",
        isPresent: Boolean = true,
        modifiedAt: Long = System.currentTimeMillis()
    ): AttendanceRecord {
        return AttendanceRecord(
            id = "$studentId-$date",
            date = date,
            studentId = studentId,
            studentRa = "RA$studentId",
            studentName = "Aluno $studentId",
            studentClass = "6A",
            classId = "class_001",
            isPresent = isPresent,
            createdAt = System.currentTimeMillis(),
            modifiedAt = modifiedAt,
            isSynced = false
        )
    }

    private fun createAttendanceEntity(
        studentId: String = "student_001",
        date: String = "2025-11-13",
        id: String = "$studentId-$date",
        lastModified: Long = System.currentTimeMillis()
    ): AttendanceEntity {
        return AttendanceEntity(
            id = id,
            studentId = studentId,
            studentRa = "RA$studentId",
            studentName = "Aluno $studentId",
            studentClass = "6A",
            classId = "class_001",
            date = date,
            isPresent = true,
            timestamp = System.currentTimeMillis().toString(),
            isSynced = false,
            lastModified = lastModified
        )
    }
}
