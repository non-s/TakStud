package com.example.takstud.offline

import com.example.takstud.model.Task
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.runBlocking
import org.junit.Before
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

/**
 * Testes para OfflineSyncQueue.
 *
 * Valida:
 * - Adicionar operações à fila
 * - Sincronizar itens
 * - Retry em caso de erro
 * - Persistência de dados
 * - Cleanup de fila
 */
class OfflineSyncQueueTest {

    private lateinit var mockDao: SyncQueueDao
    private lateinit var queue: OfflineSyncQueue

    private val task = Task(
        id = "task_001",
        title = "Prova de Matemática",
        description = "Capítulo 1-3",
        dueDate = "2025-11-20",
        studentClass = "6A"
    )

    @Before
    fun setUp() {
        mockDao = mockk()
        queue = OfflineSyncQueueImpl(mockDao)
    }

    // ==================== ADD OPERATION TESTS ====================

    @Test
    fun `add operation to queue successfully`() = runBlocking {
        // Arrange
        coEvery { mockDao.insert(any()) } returns Unit

        // Act
        queue.addOperation(
            operation = SyncOperation.CREATE,
            entityType = "TASK",
            entityId = "task_001",
            entity = task
        )

        // Assert
        coVerify(exactly = 1) { mockDao.insert(any()) }
    }

    @Test
    fun `add multiple operations to queue`() = runBlocking {
        // Arrange
        coEvery { mockDao.insert(any()) } returns Unit

        // Act
        queue.addOperation(SyncOperation.CREATE, "TASK", "task_001", task)
        queue.addOperation(SyncOperation.UPDATE, "TASK", "task_002", task)
        queue.addOperation(SyncOperation.DELETE, "TASK", "task_003", null)

        // Assert
        coVerify(exactly = 3) { mockDao.insert(any()) }
    }

    @Test
    fun `add delete operation with null entity`() = runBlocking {
        // Arrange
        coEvery { mockDao.insert(any()) } returns Unit

        // Act
        queue.addOperation(
            operation = SyncOperation.DELETE,
            entityType = "TASK",
            entityId = "task_001",
            entity = null
        )

        // Assert
        coVerify(exactly = 1) { mockDao.insert(any()) }
    }

    // ==================== GET OPERATIONS TESTS ====================

    @Test
    fun `get unsynced items returns all pending items`() = runBlocking {
        // Arrange
        val items = listOf(
            SyncQueueItem("1", "CREATE", "TASK", "task_001", "", 1000, isSynced = false),
            SyncQueueItem("2", "UPDATE", "TASK", "task_002", "", 2000, isSynced = false)
        )
        coEvery { mockDao.getUnsyncedItems() } returns items

        // Act
        val result = queue.getUnsyncedItems()

        // Assert
        assertEquals(2, result.size)
        assertTrue(result.all { !it.isSynced })
    }

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
    fun `get empty queue returns empty list`() = runBlocking {
        // Arrange
        coEvery { mockDao.getUnsyncedItems() } returns emptyList()

        // Act
        val items = queue.getUnsyncedItems()

        // Assert
        assertTrue(items.isEmpty())
    }

    // ==================== MARK SYNCED TESTS ====================

    @Test
    fun `mark synced removes item from queue`() = runBlocking {
        // Arrange
        coEvery { mockDao.delete("item_1") } returns Unit

        // Act
        queue.markSynced("item_1")

        // Assert
        coVerify(exactly = 1) { mockDao.delete("item_1") }
    }

    // ==================== SYNC ERROR TESTS ====================

    @Test
    fun `record sync error increments retry count`() = runBlocking {
        // Arrange
        val item = SyncQueueItem(
            "1", "CREATE", "TASK", "task_001", "", 1000,
            isSynced = false, syncAttempts = 1
        )
        coEvery { mockDao.getById("1") } returns item
        coEvery { mockDao.incrementRetries("1", any()) } returns Unit

        // Act
        queue.recordSyncError("1", "Timeout")

        // Assert
        coVerify { mockDao.incrementRetries("1", any()) }
    }

    @Test
    fun `record sync error marks as permanent failure after max retries`() = runBlocking {
        // Arrange
        val item = SyncQueueItem(
            "1", "CREATE", "TASK", "task_001", "", 1000,
            isSynced = false, syncAttempts = 3  // Já atingiu MAX_RETRIES
        )
        coEvery { mockDao.getById("1") } returns item
        coEvery { mockDao.updateError("1", any(), true) } returns Unit

        // Act
        queue.recordSyncError("1", "Timeout")

        // Assert
        coVerify { mockDao.updateError("1", any(), true) }
    }

    // ==================== SYNC ALL TESTS ====================

    @Test
    fun `sync all items successfully`() = runBlocking {
        // Arrange
        val items = listOf(
            SyncQueueItem("1", "CREATE", "TASK", "task_001", "", 1000, isSynced = false),
            SyncQueueItem("2", "UPDATE", "TASK", "task_002", "", 2000, isSynced = false)
        )
        coEvery { mockDao.getUnsyncedItems() } returns items
        coEvery { mockDao.delete(any()) } returns Unit

        var syncCount = 0
        val syncCallback = suspend { _: SyncQueueItem ->
            syncCount++
            true  // Sempre sucesso
        }

        // Act
        queue.syncAll(syncCallback)

        // Assert
        assertEquals(2, syncCount)
        coVerify(exactly = 2) { mockDao.delete(any()) }
    }

    @Test
    fun `sync with some failures retries unsynced items`() = runBlocking {
        // Arrange
        val items = listOf(
            SyncQueueItem("1", "CREATE", "TASK", "task_001", "", 1000, isSynced = false),
            SyncQueueItem("2", "UPDATE", "TASK", "task_002", "", 2000, isSynced = false),
            SyncQueueItem("3", "DELETE", "TASK", "task_003", "", 3000, isSynced = false)
        )
        coEvery { mockDao.getUnsyncedItems() } returns items
        coEvery { mockDao.delete(any()) } returns Unit
        coEvery { mockDao.getById(any()) } returns items[0]
        coEvery { mockDao.incrementRetries(any(), any()) } returns Unit

        var syncCount = 0
        val syncCallback = suspend { item: SyncQueueItem ->
            syncCount++
            item.id == "1"  // Apenas primeiro sucede
        }

        // Act
        queue.syncAll(syncCallback)

        // Assert
        assertEquals(3, syncCount)  // Todos tentam
        coVerify(exactly = 1) { mockDao.delete(any()) }  // Apenas 1 bem-sucedido
    }

    @Test
    fun `sync with empty queue does nothing`() = runBlocking {
        // Arrange
        coEvery { mockDao.getUnsyncedItems() } returns emptyList()

        var syncCount = 0
        val syncCallback = suspend { _: SyncQueueItem ->
            syncCount++
            true
        }

        // Act
        queue.syncAll(syncCallback)

        // Assert
        assertEquals(0, syncCount)
    }

    // ==================== CLEAR QUEUE TESTS ====================

    @Test
    fun `clear queue removes all items`() = runBlocking {
        // Arrange
        coEvery { mockDao.deleteAll() } returns Unit

        // Act
        queue.clearQueue()

        // Assert
        coVerify(exactly = 1) { mockDao.deleteAll() }
    }

    // ==================== STATISTICS TESTS ====================

    @Test
    fun `get stats returns queue statistics`() = runBlocking {
        // Arrange
        val items = listOf(
            SyncQueueItem("1", "CREATE", "TASK", "task_001", "", 1000, isSynced = false, syncAttempts = 0),
            SyncQueueItem("2", "UPDATE", "TASK", "task_002", "", 2000, isSynced = true, syncAttempts = 1),
            SyncQueueItem("3", "DELETE", "TASK", "task_003", "", 3000, isSynced = false, syncAttempts = 3, isPermanentFailure = true)
        )
        coEvery { mockDao.getAll() } returns items

        // Act
        val stats = queue.getStats()

        // Assert
        assertEquals(3, stats.totalItems)
        assertEquals(1, stats.pendingItems)
        assertEquals(1, stats.failedItems)
        assertEquals(1, stats.syncedItems)
    }

    @Test
    fun `get stats with empty queue returns zeros`() = runBlocking {
        // Arrange
        coEvery { mockDao.getAll() } returns emptyList()

        // Act
        val stats = queue.getStats()

        // Assert
        assertEquals(0, stats.totalItems)
        assertEquals(0, stats.pendingItems)
        assertEquals(0, stats.failedItems)
        assertEquals(0, stats.syncedItems)
    }

    // ==================== SYNC QUEUE ITEM TESTS ====================

    @Test
    fun `sync queue item calculates minutes since creation`() {
        // Arrange
        val createdAt = System.currentTimeMillis() - 120000  // 2 minutos atrás
        val item = SyncQueueItem(
            "1", "CREATE", "TASK", "task_001", "", createdAt,
            isSynced = false
        )

        // Act
        val minutes = item.minutesSinceCreation()

        // Assert
        assertTrue(minutes >= 1)
        assertTrue(minutes < 3)
    }

    @Test
    fun `sync queue item can retry if not synced and under max retries`() {
        // Arrange
        val item = SyncQueueItem(
            "1", "CREATE", "TASK", "task_001", "", 1000,
            isSynced = false, syncAttempts = 1, isPermanentFailure = false
        )

        // Act
        val canRetry = item.canRetry(maxRetries = 3)

        // Assert
        assertTrue(canRetry)
    }

    @Test
    fun `sync queue item cannot retry if exceeds max retries`() {
        // Arrange
        val item = SyncQueueItem(
            "1", "CREATE", "TASK", "task_001", "", 1000,
            isSynced = false, syncAttempts = 3, isPermanentFailure = true
        )

        // Act
        val canRetry = item.canRetry(maxRetries = 3)

        // Assert
        assertFalse(canRetry)
    }

    @Test
    fun `sync queue item cannot retry if already synced`() {
        // Arrange
        val item = SyncQueueItem(
            "1", "CREATE", "TASK", "task_001", "", 1000,
            isSynced = true, syncAttempts = 0
        )

        // Act
        val canRetry = item.canRetry()

        // Assert
        assertFalse(canRetry)
    }

    // ==================== SYNC OPERATION ENUM TESTS ====================

    @Test
    fun `sync operations enum values exist`() {
        // Assert
        assertEquals("CREATE", SyncOperation.CREATE.name)
        assertEquals("UPDATE", SyncOperation.UPDATE.name)
        assertEquals("DELETE", SyncOperation.DELETE.name)
    }

    // ==================== DESERIALIZATION TESTS ====================

    @Test
    fun `deserialize entity from json`() {
        // Arrange
        val json = """{"id":"task_001","title":"Test","description":"Desc","dueDate":"2025-11-20","studentClass":"6A"}"""

        // Act
        val result = queue.deserializeEntity(json, Task::class.java)

        // Assert
        assertNotNull(result)
        assertEquals("task_001", result.id)
        assertEquals("Test", result.title)
    }

    @Test
    fun `deserialize invalid json returns null`() {
        // Arrange
        val json = "invalid json"

        // Act
        val result = queue.deserializeEntity(json, Task::class.java)

        // Assert
        assertTrue(result == null)
    }

    // ==================== REALISTIC SCENARIOS ====================

    @Test
    fun `scenario - offline user creates task then syncs`() = runBlocking {
        // Arrange
        coEvery { mockDao.insert(any()) } returns Unit
        coEvery { mockDao.getUnsyncedItems() } returns listOf(
            SyncQueueItem("1", "CREATE", "TASK", "task_001", "", 1000, isSynced = false)
        )
        coEvery { mockDao.delete("1") } returns Unit

        // Act
        queue.addOperation(SyncOperation.CREATE, "TASK", "task_001", task)
        val itemsBeforeSync = queue.getUnsyncedItems()

        var syncedCount = 0
        queue.syncAll { syncedCount++; true }

        // Assert
        assertEquals(1, itemsBeforeSync.size)
        assertEquals(1, syncedCount)
    }

    @Test
    fun `scenario - multiple operations with partial failures`() = runBlocking {
        // Arrange
        val items = listOf(
            SyncQueueItem("1", "CREATE", "TASK", "task_001", "", 1000, isSynced = false),
            SyncQueueItem("2", "UPDATE", "STUDENT", "student_001", "", 2000, isSynced = false),
            SyncQueueItem("3", "DELETE", "GRADE", "grade_001", "", 3000, isSynced = false)
        )
        coEvery { mockDao.getUnsyncedItems() } returns items
        coEvery { mockDao.delete(any()) } returns Unit
        coEvery { mockDao.getById(any()) } returns items[0]
        coEvery { mockDao.incrementRetries(any(), any()) } returns Unit

        // Act
        var successCount = 0
        queue.syncAll { item ->
            if (item.id == "1") {
                successCount++
                true
            } else {
                false
            }
        }

        // Assert
        assertEquals(1, successCount)
        coVerify(exactly = 1) { mockDao.delete(any()) }
    }
}
