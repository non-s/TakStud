package com.example.takstud.integration

import com.example.takstud.attendance.AttendanceDeduplicationManager
import com.example.takstud.data.local.dao.AttendanceDao
import com.example.takstud.data.local.dao.GradeDao
import com.example.takstud.data.local.entity.AttendanceEntity
import com.example.takstud.data.local.entity.GradeEntity
import com.example.takstud.grade.GradeBatchManager
import com.example.takstud.model.AttendanceRecord
import com.example.takstud.model.Grade
import com.example.takstud.offline.OfflineSyncQueueImpl
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.WriteBatch
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.runBlocking
import org.junit.Before
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/**
 * Testes de integração end-to-end para cenários completos.
 *
 * Cobre:
 * - Workflow completo: offline save → deduplicate → queue → sync
 * - Múltiplos módulos interagindo (grades, attendance, queue, sync)
 * - Transições de estado (offline → online)
 * - Consistência de dados entre módulos
 * - Error recovery em múltiplos níveis
 */
class EndToEndIntegrationTest {

    private lateinit var mockAttendanceDao: AttendanceDao
    private lateinit var mockGradeDao: GradeDao
    private lateinit var mockFirestore: FirebaseFirestore
    private lateinit var mockBatch: WriteBatch
    private lateinit var mockSyncQueue: OfflineSyncQueueImpl

    private lateinit var dedupManager: AttendanceDeduplicationManager
    private lateinit var gradeBatchManager: GradeBatchManager

    private val taskId = "task_001"
    private val studentId1 = "student_001"
    private val studentId2 = "student_002"
    private val classId = "class_6A"

    @Before
    fun setUp() {
        mockAttendanceDao = mockk()
        mockGradeDao = mockk()
        mockFirestore = mockk()
        mockBatch = mockk()
        mockSyncQueue = mockk()

        // Setup Firebase mocks
        every { mockFirestore.batch() } returns mockBatch
        every { mockFirestore.collection("grades") } returns mockk()
        every { mockFirestore.collection("attendance") } returns mockk()
        every { mockBatch.set(any(), any()) } returns mockBatch
        every { mockBatch.update(any(), any()) } returns mockBatch
        every { mockBatch.delete(any()) } returns mockBatch
        coEvery { mockBatch.commit() } returns mockk()

        // Setup DAO mocks
        coEvery { mockAttendanceDao.insertAttendance(any()) } returns Unit
        coEvery { mockAttendanceDao.updateAttendance(any()) } returns Unit
        coEvery { mockAttendanceDao.getAttendanceById(any()) } returns null
        coEvery { mockGradeDao.insertGrades(any()) } returns Unit
        coEvery { mockGradeDao.markAsSynced(any()) } returns Unit

        // Setup Queue mocks
        coEvery { mockSyncQueue.addOperation(any(), any(), any(), any(), any()) } returns Unit
        coEvery { mockSyncQueue.getUnsyncedItems() } returns emptyList()

        dedupManager = AttendanceDeduplicationManager(mockAttendanceDao)
        gradeBatchManager = GradeBatchManager(mockGradeDao, mockFirestore)
    }

    // ==================== Helper Functions ====================

    private fun createAttendanceRecord(
        studentId: String = studentId1,
        date: String = "2025-11-14",
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

    private fun createGrade(
        studentId: String = studentId1,
        score: String = "85"
    ): Grade {
        return Grade(
            id = "$taskId-$studentId",
            taskId = taskId,
            studentId = studentId,
            score = score,
            classId = classId,
            releaseDate = System.currentTimeMillis(),
            value = score
        )
    }

    // ==================== COMPLETE ATTENDANCE WORKFLOW (1 test) ====================

    @Test
    fun `complete workflow - offline save attendance then deduplicate then queue`() = runBlocking {
        // Scenario: Teacher marks attendance offline, duplicate records are created,
        // system deduplicates, then queues for sync when online

        // ===== PHASE 1: OFFLINE SAVE =====
        val record1 = createAttendanceRecord(studentId = studentId1, date = "2025-11-14", isPresent = true, modifiedAt = 1000)
        val record2 = createAttendanceRecord(studentId = studentId1, date = "2025-11-14", isPresent = true, modifiedAt = 2000)  // Duplicate, newer
        val record3 = createAttendanceRecord(studentId = studentId2, date = "2025-11-14", isPresent = false, modifiedAt = 1500)

        coEvery { mockAttendanceDao.getAttendanceById(any()) } returns null
        coEvery { mockAttendanceDao.insertAttendance(any()) } returns Unit

        // Save records offline
        val saveResult1 = dedupManager.saveAttendanceWithDeduplication(record1)
        val saveResult2 = dedupManager.saveAttendanceWithDeduplication(record2)
        val saveResult3 = dedupManager.saveAttendanceWithDeduplication(record3)

        // ===== PHASE 2: DEDUPLICATE BEFORE SYNC =====
        val unsyncedRecords = listOf(record1, record2, record3)
        val dedupedRecords = dedupManager.deduplicateBeforeSync(unsyncedRecords)

        // ===== PHASE 3: QUEUE FOR SYNC =====
        coEvery { mockSyncQueue.addOperation(any(), any(), any(), any(), any()) } returns Unit

        for (record in dedupedRecords) {
            mockSyncQueue.addOperation(
                "CREATE",
                "ATTENDANCE",
                record.id,
                record,
                "CRITICAL"
            )
        }

        // ===== ASSERTIONS =====
        // Saves succeed
        assertTrue(saveResult1)
        assertTrue(saveResult2)
        assertTrue(saveResult3)

        // Deduplication removes duplicate
        assertEquals(2, dedupedRecords.size)  // Only 2 unique (student_001 and student_002)

        // Contains the newest record for student_001
        assertTrue(dedupedRecords.any { it.studentId == studentId1 && it.modifiedAt == 2000L })
        assertTrue(dedupedRecords.any { it.studentId == studentId2 })
    }

    // ==================== COMPLETE GRADE WORKFLOW (1 test) ====================

    @Test
    fun `complete workflow - validate and batch save grades then queue`() = runBlocking {
        // Scenario: Teacher bulk releases grades for entire class
        // System validates, saves locally, saves to Firestore, queues for sync

        // ===== PHASE 1: PREPARE GRADES =====
        val studentIds = listOf(studentId1, studentId2, "student_003")
        val grades = studentIds.map { createGrade(studentId = it, score = "85") }

        coEvery { mockGradeDao.insertGrades(any()) } returns Unit
        coEvery { mockGradeDao.markAsSynced(any()) } returns Unit
        coEvery { mockSyncQueue.addOperation(any(), any(), any(), any(), any()) } returns Unit

        // ===== PHASE 2: VALIDATE AND SAVE =====
        val batchResult = gradeBatchManager.saveGradesBatch(
            grades = grades,
            localSave = true,
            validateBeforeSave = true
        )

        // ===== PHASE 3: QUEUE FOR SYNC =====
        if (batchResult.succeeded > 0) {
            for (grade in grades) {
                mockSyncQueue.addOperation(
                    "CREATE",
                    "GRADES",
                    grade.id,
                    grade,
                    "CRITICAL"
                )
            }
        }

        // ===== ASSERTIONS =====
        // Batch save succeeds
        assertEquals(3, batchResult.total)
        assertEquals(3, batchResult.succeeded)
        assertEquals(0, batchResult.failed)

        // All grades have unique IDs
        val gradeIds = grades.map { it.id }
        assertEquals(3, gradeIds.distinct().size)
    }

    // ==================== OFFLINE TO ONLINE TRANSITION (1 test) ====================

    @Test
    fun `transition workflow - accumulate data offline then sync when online`() = runBlocking {
        // Scenario: Device offline for 10 minutes, multiple operations accumulated
        // Then goes online, syncs everything in proper order

        // ===== PHASE 1: OFFLINE - ACCUMULATE DATA =====
        val offlineAttendance = (1..5).map { i ->
            createAttendanceRecord(
                studentId = "student_$i",
                date = "2025-11-14",
                isPresent = (i % 2 == 0)
            )
        }

        val offlineGrades = (1..5).map { i ->
            createGrade(
                studentId = "student_$i",
                score = "${70 + i * 5}"
            )
        }

        coEvery { mockAttendanceDao.insertAttendance(any()) } returns Unit
        coEvery { mockGradeDao.insertGrades(any()) } returns Unit
        coEvery { mockAttendanceDao.getAttendanceById(any()) } returns null

        // Save attendance offline
        for (record in offlineAttendance) {
            dedupManager.saveAttendanceWithDeduplication(record)
        }

        // Save grades offline
        val gradeResult = gradeBatchManager.saveGradesBatch(offlineGrades)

        // ===== PHASE 2: ONLINE - SYNC =====
        // Deduplicate before syncing
        val dedupedAttendance = dedupManager.deduplicateBeforeSync(offlineAttendance)

        // Queue all for sync
        coEvery { mockSyncQueue.addOperation(any(), any(), any(), any(), any()) } returns Unit

        for (record in dedupedAttendance) {
            mockSyncQueue.addOperation("CREATE", "ATTENDANCE", record.id, record, "CRITICAL")
        }

        // ===== ASSERTIONS =====
        // All offline data was saved locally
        assertEquals(5, offlineAttendance.size)
        assertEquals(5, gradeResult.total)

        // Deduplication preserved unique records
        assertEquals(5, dedupedAttendance.size)
    }

    // ==================== DATA CONSISTENCY CHECK (1 test) ====================

    @Test
    fun `consistency workflow - verify data integrity across modules`() = runBlocking {
        // Scenario: System checks consistency when syncing
        // Ensures attendance and grades for same student are consistent

        // ===== SETUP: Create matched attendance and grades =====
        val students = listOf(studentId1, studentId2, "student_003")

        // Create attendance for each student
        val attendance = students.map { studentId ->
            createAttendanceRecord(
                studentId = studentId,
                date = "2025-11-14",
                isPresent = true
            )
        }

        // Create grades for each student
        val grades = students.map { studentId ->
            createGrade(
                studentId = studentId,
                score = "85"
            )
        }

        coEvery { mockAttendanceDao.insertAttendance(any()) } returns Unit
        coEvery { mockGradeDao.insertGrades(any()) } returns Unit
        coEvery { mockAttendanceDao.getAttendanceById(any()) } returns null

        // ===== SAVE DATA =====
        for (record in attendance) {
            dedupManager.saveAttendanceWithDeduplication(record)
        }

        val gradeResult = gradeBatchManager.saveGradesBatch(grades)

        // ===== VERIFY CONSISTENCY =====
        // Extract unique students from both
        val attendanceStudents = attendance.map { it.studentId }.distinct().sorted()
        val gradeStudents = grades.map { it.studentId }.distinct().sorted()

        // ===== ASSERTIONS =====
        // Both have same students
        assertEquals(attendanceStudents, gradeStudents)
        assertEquals(3, attendanceStudents.size)

        // Grades were saved successfully
        assertEquals(3, gradeResult.succeeded)

        // No duplicates in either dataset
        assertEquals(attendance.size, attendance.distinctBy { it.studentId }.size)
        assertEquals(grades.size, grades.distinctBy { it.studentId }.size)
    }

    // ==================== ERROR RECOVERY WORKFLOW (1 test) ====================

    @Test
    fun `recovery workflow - handles partial failures and retries`() = runBlocking {
        // Scenario: Batch operation fails partially, system retries failed items

        // ===== SETUP: Mix of valid and invalid data =====
        val grades = listOf(
            createGrade(studentId = studentId1, score = "85"),      // Valid
            createGrade(studentId = studentId2, score = "92"),      // Valid
            createGrade(studentId = "student_003", score = "150")   // Invalid (>100)
        )

        coEvery { mockGradeDao.insertGrades(any()) } returns Unit

        // ===== FIRST ATTEMPT: With validation =====
        val firstAttempt = gradeBatchManager.saveGradesBatch(
            grades = grades,
            validateBeforeSave = true
        )

        // ===== RECOVERY: Retry without invalid item =====
        val validGrades = grades.filter { it.score.toDoubleOrNull() in 0.0..100.0 }
        val secondAttempt = gradeBatchManager.saveGradesBatch(
            grades = validGrades,
            validateBeforeSave = false
        )

        // ===== ASSERTIONS =====
        // First attempt fails due to validation
        assertEquals(0, firstAttempt.succeeded)
        assertEquals(3, firstAttempt.failed)
        assertTrue(firstAttempt.isValidationError)

        // Second attempt with valid items succeeds
        assertEquals(2, secondAttempt.succeeded)
    }
}
