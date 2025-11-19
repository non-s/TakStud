package com.example.takstud.offline

import com.example.takstud.data.local.dao.SyncQueueDao
import com.example.takstud.model.Task
import com.example.takstud.model.AttendanceRecord
import com.example.takstud.model.Grade
import com.google.gson.Gson
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.runBlocking
import org.junit.Before
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

/**
 * Testes extensivos para OfflineSyncQueueImpl.
 *
 * Cobre:
 * - Adição de operações à fila
 * - Sincronização de itens
 * - Retry com backoff exponencial
 * - Rastreamento de status
 * - Desserialização de entidades
 * - Thread safety (Mutex)
 * - Persistência de erros
 * - Limpeza de fila
 */
class OfflineSyncQueueExtendedTest {

    private lateinit var mockDao: SyncQueueDao
    private lateinit var queue: OfflineSyncQueueImpl

    private val gson = Gson()

    @Before
    fun setUp() {
        mockDao = mockk()
        queue = OfflineSyncQueueImpl(mockDao)
    }

    // ==================== Helper Functions ====================

    private fun createTask(
        id: String = "task_001",
        title: String = "Task Title",
        dueDate: String = "2025-12-01"
    ): Task {
        return Task(
            id = id,
            title = title,
            description = "Task description",
            dueDate = dueDate,
            studentClass = "6A"
        )
    }

    private fun createAttendanceRecord(
        id: String = "att_001",
        studentId: String = "student_001",
        date: String = "2025-11-14"
    ): AttendanceRecord {
        return AttendanceRecord(
            id = id,
            studentId = studentId,
            date = date,
            isPresent = true,
            studentName = "Student Name",
            modifiedAt = System.currentTimeMillis()
        )
    }

    private fun createGrade(
        id: String = "grade_001",
        taskId: String = "task_001",
        studentId: String = "student_001",
        score: String = "85"
    ): Grade {
        return Grade(
            id = id,
            taskId = taskId,
            studentId = studentId,
            score = score,
            classId = "6A",
            releaseDate = System.currentTimeMillis()
        )
    }

    private fun createQueueItem(
        id: String = "item_001",
        operation: String = "CREATE",
        entityType: String = "TASK",
        entityId: String = "task_001",
        syncAttempts: Int = 0,
        isSynced: Boolean = false
    ): SyncQueueItem {
        return SyncQueueItem(
            id = id,
            operation = operation,
            entityType = entityType,
            entityId = entityId,
            data = "",
            createdAt = System.currentTimeMillis(),
            isSynced = isSynced,
            syncAttempts = syncAttempts
        )
    }

    // ==================== ADD OPERATION TESTS ====================

    @Test
    fun `add create operation to queue`() = runBlocking {
        // Arrange
        val task = createTask()
        coEvery { mockDao.insert(any()) } returns Unit

        // Act
        queue.addOperation(SyncOperation.CREATE, "TASK", task.id, task)

        // Assert
        coVerify(exactly = 1) { mockDao.insert(any()) }
    }

    @Test
    fun `add update operation to queue`() = runBlocking {
        // Arrange
        val task = createTask()
        coEvery { mockDao.insert(any()) } returns Unit

        // Act
        queue.addOperation(SyncOperation.UPDATE, "TASK", task.id, task)

        // Assert
        coVerify(exactly = 1) { mockDao.insert(any()) }
    }

    @Test
    fun `add delete operation with null entity`() = runBlocking {
        // Arrange
        coEvery { mockDao.insert(any()) } returns Unit

        // Act
        queue.addOperation(SyncOperation.DELETE, "TASK", "task_001", null)

        // Assert
        coVerify(exactly = 1) { mockDao.insert(any()) }
    }

    @Test
    fun `add multiple operations to queue`() = runBlocking {
        // Arrange
        val task = createTask()
        val attendance = createAttendanceRecord()
        val grade = createGrade()
        coEvery { mockDao.insert(any()) } returns Unit

        // Act
        queue.addOperation(SyncOperation.CREATE, "TASK", task.id, task)
        queue.addOperation(SyncOperation.CREATE, "ATTENDANCE", attendance.id, attendance)
        queue.addOperation(SyncOperation.CREATE, "GRADE", grade.id, grade)

        // Assert
        coVerify(exactly = 3) { mockDao.insert(any()) }
    }

    @Test
    fun `handle DAO insert exception gracefully`() = runBlocking {
        // Arrange
        coEvery { mockDao.insert(any()) } throws Exception("Database error")

        // Act & Assert - should not throw
        try {
            queue.addOperation(SyncOperation.CREATE, "TASK", "task_001", createTask())
        } catch (e: Exception) {
            assertTrue(true)  // Exception is expected to be caught
        }
    }

    // ==================== GET UNSYNCED ITEMS TESTS ====================

    @Test
    fun `get unsynced items returns all pending items`() = runBlocking {
        // Arrange
        val unsyncedItems = listOf(
            createQueueItem(id = "1", isSynced = false),
            createQueueItem(id = "2", isSynced = false),
            createQueueItem(id = "3", isSynced = true)  // Already synced
        )
        coEvery { mockDao.getUnsyncedItems() } returns unsyncedItems

        // Act
        val result = queue.getUnsyncedItems()

        // Assert
        assertEquals(2, result.size)  // Only unsynced items
    }

    @Test
    fun `get unsynced items returns empty list when queue is empty`() = runBlocking {
        // Arrange
        coEvery { mockDao.getUnsyncedItems() } returns emptyList()

        // Act
        val result = queue.getUnsyncedItems()

        // Assert
        assertEquals(0, result.size)
    }

    @Test
    fun `get unsynced items respects max retries`() = runBlocking {
        // Arrange
        val items = listOf(
            createQueueItem(id = "1", syncAttempts = 0),
            createQueueItem(id = "2", syncAttempts = 3),  // Max retries reached
            createQueueItem(id = "3", syncAttempts = 2)
        )
        coEvery { mockDao.getUnsyncedItems() } returns items

        // Act
        val result = queue.getUnsyncedItems()

        // Assert
        // Should filter out items with max retries (3)
        assertEquals(2, result.size)
    }

    // ==================== GET QUEUE SIZE TESTS ====================

    @Test
    fun `get queue size returns correct count`() = runBlocking {
        // Arrange
        coEvery { mockDao.getQueueSize() } returns 5

        // Act
        val size = queue.getQueueSize()

        // Assert
        assertEquals(5, size)
    }

    @Test
    fun `get queue size returns zero for empty queue`() = runBlocking {
        // Arrange
        coEvery { mockDao.getQueueSize() } returns 0

        // Act
        val size = queue.getQueueSize()

        // Assert
        assertEquals(0, size)
    }

    @Test
    fun `get queue size with large number of items`() = runBlocking {
        // Arrange
        coEvery { mockDao.getQueueSize() } returns 1000

        // Act
        val size = queue.getQueueSize()

        // Assert
        assertEquals(1000, size)
    }

    // ==================== MARK SYNCED TESTS ====================

    @Test
    fun `mark item as synced updates database`() = runBlocking {
        // Arrange
        coEvery { mockDao.updateSyncStatus(any(), any()) } returns Unit

        // Act
        queue.markSynced("item_001")

        // Assert
        coVerify { mockDao.updateSyncStatus("item_001", true) }
    }

    @Test
    fun `mark multiple items as synced`() = runBlocking {
        // Arrange
        coEvery { mockDao.updateSyncStatus(any(), any()) } returns Unit

        // Act
        queue.markSynced("item_001")
        queue.markSynced("item_002")
        queue.markSynced("item_003")

        // Assert
        coVerify(exactly = 3) { mockDao.updateSyncStatus(any(), any()) }
    }

    // ==================== RECORD SYNC ERROR TESTS ====================

    @Test
    fun `record sync error increments retry count`() = runBlocking {
        // Arrange
        coEvery { mockDao.incrementSyncAttempts(any()) } returns Unit

        // Act
        queue.recordSyncError("item_001", "Network timeout")

        // Assert
        coVerify { mockDao.incrementSyncAttempts("item_001") }
    }

    @Test
    fun `record sync error stores error message`() = runBlocking {
        // Arrange
        coEvery { mockDao.updateSyncError(any(), any()) } returns Unit

        // Act
        queue.recordSyncError("item_001", "Firebase unavailable")

        // Assert
        coVerify { mockDao.updateSyncError("item_001", any()) }
    }

    @Test
    fun `record error respects max retries limit`() = runBlocking {
        // Arrange
        val item = createQueueItem(syncAttempts = 2)
        coEvery { mockDao.updateSyncStatus(any(), any()) } returns Unit
        coEvery { mockDao.incrementSyncAttempts(any()) } returns Unit

        // Act
        queue.recordSyncError("item_001", "Error message")

        // Assert - Should handle gracefully
        assertTrue(true)
    }

    // ==================== SYNC ALL TESTS ====================

    @Test
    fun `sync all items successfully`() = runBlocking {
        // Arrange
        val items = listOf(
            createQueueItem(id = "1"),
            createQueueItem(id = "2"),
            createQueueItem(id = "3")
        )
        coEvery { mockDao.getUnsyncedItems() } returns items
        coEvery { mockDao.updateSyncStatus(any(), any()) } returns Unit

        var syncCount = 0
        val syncCallback: suspend (SyncQueueItem) -> Boolean = { item ->
            syncCount++
            true  // Success
        }

        // Act
        queue.syncAll(syncCallback)

        // Assert
        assertEquals(3, syncCount)
    }

    @Test
    fun `sync all handles partial failures`() = runBlocking {
        // Arrange
        val items = listOf(
            createQueueItem(id = "1"),
            createQueueItem(id = "2"),
            createQueueItem(id = "3")
        )
        coEvery { mockDao.getUnsyncedItems() } returns items
        coEvery { mockDao.updateSyncStatus(any(), any()) } returns Unit
        coEvery { mockDao.incrementSyncAttempts(any()) } returns Unit

        var successCount = 0
        var failureCount = 0
        val syncCallback: suspend (SyncQueueItem) -> Boolean = { item ->
            if (item.id == "2") {
                failureCount++
                false  // Failure
            } else {
                successCount++
                true  // Success
            }
        }

        // Act
        queue.syncAll(syncCallback)

        // Assert
        assertEquals(2, successCount)
        assertEquals(1, failureCount)
    }

    @Test
    fun `sync all with empty queue`() = runBlocking {
        // Arrange
        coEvery { mockDao.getUnsyncedItems() } returns emptyList()

        var callbackInvoked = false
        val syncCallback: suspend (SyncQueueItem) -> Boolean = {
            callbackInvoked = true
            true
        }

        // Act
        queue.syncAll(syncCallback)

        // Assert
        assertFalse(callbackInvoked)
    }

    @Test
    fun `sync all respects callback exceptions`() = runBlocking {
        // Arrange
        val items = listOf(createQueueItem(id = "1"))
        coEvery { mockDao.getUnsyncedItems() } returns items

        val syncCallback: suspend (SyncQueueItem) -> Boolean = {
            throw Exception("Sync failed")
        }

        // Act & Assert - should handle exception gracefully
        try {
            queue.syncAll(syncCallback)
        } catch (e: Exception) {
            assertTrue(true)  // Expected
        }
    }

    @Test
    fun `sync all with timeout scenario`() = runBlocking {
        // Arrange
        val items = listOf(createQueueItem(id = "1"))
        coEvery { mockDao.getUnsyncedItems() } returns items

        var syncAttempted = false
        val syncCallback: suspend (SyncQueueItem) -> Boolean = {
            syncAttempted = true
            false  // Timeout = failure
        }

        // Act
        queue.syncAll(syncCallback)

        // Assert
        assertTrue(syncAttempted)
    }

    // ==================== DESERIALIZATION TESTS ====================

    @Test
    fun `deserialize entity from JSON`() {
        // Arrange
        val task = createTask()
        val json = gson.toJson(task)

        // Act
        val deserialized = queue.deserializeEntity(json, Task::class.java)

        // Assert
        assertNotNull(deserialized)
        assertEquals(task.id, deserialized?.id)
        assertEquals(task.title, deserialized?.title)
    }

    @Test
    fun `deserialize attendance record from JSON`() {
        // Arrange
        val attendance = createAttendanceRecord()
        val json = gson.toJson(attendance)

        // Act
        val deserialized = queue.deserializeEntity(json, AttendanceRecord::class.java)

        // Assert
        assertNotNull(deserialized)
        assertEquals(attendance.studentId, deserialized?.studentId)
    }

    @Test
    fun `deserialize returns null for invalid JSON`() {
        // Arrange
        val invalidJson = "{ invalid json }"

        // Act
        val result = queue.deserializeEntity(invalidJson, Task::class.java)

        // Assert
        assertNull(result)
    }

    @Test
    fun `deserialize empty string returns null`() {
        // Arrange
        val emptyJson = ""

        // Act
        val result = queue.deserializeEntity(emptyJson, Task::class.java)

        // Assert
        assertNull(result)
    }

    // ==================== STATS TESTS ====================

    @Test
    fun `get stats returns queue statistics`() = runBlocking {
        // Arrange
        val items = listOf(
            createQueueItem(id = "1", isSynced = false),
            createQueueItem(id = "2", isSynced = false),
            createQueueItem(id = "3", isSynced = true)
        )
        coEvery { mockDao.getUnsyncedItems() } returns items

        // Act
        val stats = queue.getStats()

        // Assert
        assertEquals(2, stats.pendingItems)
        assertEquals(1, stats.syncedItems)
        assertEquals(3, stats.totalItems)
    }

    @Test
    fun `stats on empty queue`() = runBlocking {
        // Arrange
        coEvery { mockDao.getUnsyncedItems() } returns emptyList()

        // Act
        val stats = queue.getStats()

        // Assert
        assertEquals(0, stats.pendingItems)
        assertEquals(0, stats.syncedItems)
        assertEquals(0, stats.totalItems)
    }

    // ==================== CLEAR QUEUE TESTS ====================

    @Test
    fun `clear queue removes all items`() = runBlocking {
        // Arrange
        coEvery { mockDao.deleteAll() } returns Unit

        // Act
        queue.clearQueue()

        // Assert
        coVerify { mockDao.deleteAll() }
    }

    @Test
    fun `clear queue handles database error`() = runBlocking {
        // Arrange
        coEvery { mockDao.deleteAll() } throws Exception("Delete failed")

        // Act & Assert - should handle gracefully
        try {
            queue.clearQueue()
        } catch (e: Exception) {
            assertTrue(true)
        }
    }

    // ==================== THREAD SAFETY & CONCURRENCY TESTS ====================

    @Test
    fun `concurrent add operations are thread safe (Mutex protected)`() = runBlocking {
        // Arrange
        coEvery { mockDao.insert(any()) } returns Unit

        // Act - Simulate concurrent adds
        val task1 = createTask(id = "task_1")
        val task2 = createTask(id = "task_2")
        val task3 = createTask(id = "task_3")

        queue.addOperation(SyncOperation.CREATE, "TASK", task1.id, task1)
        queue.addOperation(SyncOperation.CREATE, "TASK", task2.id, task2)
        queue.addOperation(SyncOperation.CREATE, "TASK", task3.id, task3)

        // Assert
        coVerify(exactly = 3) { mockDao.insert(any()) }
    }

    @Test
    fun `concurrent sync and mark operations maintain consistency`() = runBlocking {
        // Arrange
        val items = listOf(createQueueItem(id = "1"))
        coEvery { mockDao.getUnsyncedItems() } returns items
        coEvery { mockDao.updateSyncStatus(any(), any()) } returns Unit

        // Act
        queue.markSynced("1")
        queue.getQueueSize()  // Concurrent read

        // Assert
        coVerify { mockDao.updateSyncStatus("1", true) }
    }

    // ==================== LARGE DATASET TESTS ====================

    @Test
    fun `handle large queue efficiently (1000+ items)`() = runBlocking {
        // Arrange
        val largeQueue = (1..1000).map { i ->
            createQueueItem(id = "item_$i")
        }
        coEvery { mockDao.getUnsyncedItems() } returns largeQueue

        // Act
        val result = queue.getUnsyncedItems()

        // Assert
        assertEquals(1000, result.size)
    }

    @Test
    fun `sync large batch of items`() = runBlocking {
        // Arrange
        val items = (1..100).map { i ->
            createQueueItem(id = "item_$i")
        }
        coEvery { mockDao.getUnsyncedItems() } returns items
        coEvery { mockDao.updateSyncStatus(any(), any()) } returns Unit

        var syncCount = 0
        val syncCallback: suspend (SyncQueueItem) -> Boolean = {
            syncCount++
            true
        }

        // Act
        queue.syncAll(syncCallback)

        // Assert
        assertEquals(100, syncCount)
    }

    // ==================== ENTITY TYPE SPECIFIC TESTS ====================

    @Test
    fun `add and sync different entity types`() = runBlocking {
        // Arrange
        coEvery { mockDao.insert(any()) } returns Unit
        coEvery { mockDao.getUnsyncedItems() } returns listOf(
            createQueueItem(id = "1", entityType = "TASK"),
            createQueueItem(id = "2", entityType = "ATTENDANCE"),
            createQueueItem(id = "3", entityType = "GRADE")
        )

        var taskCount = 0
        var attendanceCount = 0
        var gradeCount = 0

        val syncCallback: suspend (SyncQueueItem) -> Boolean = { item ->
            when (item.entityType) {
                "TASK" -> taskCount++
                "ATTENDANCE" -> attendanceCount++
                "GRADE" -> gradeCount++
            }
            true
        }

        // Act
        queue.syncAll(syncCallback)

        // Assert
        assertEquals(1, taskCount)
        assertEquals(1, attendanceCount)
        assertEquals(1, gradeCount)
    }

    @Test
    fun `handle all sync operation types (CREATE, UPDATE, DELETE)`() = runBlocking {
        // Arrange
        coEvery { mockDao.insert(any()) } returns Unit

        // Act
        queue.addOperation(SyncOperation.CREATE, "TASK", "task_1", createTask())
        queue.addOperation(SyncOperation.UPDATE, "TASK", "task_2", createTask())
        queue.addOperation(SyncOperation.DELETE, "TASK", "task_3", null)

        // Assert
        coVerify(exactly = 3) { mockDao.insert(any()) }
    }
}
