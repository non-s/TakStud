package com.example.takstud.data.local.dao

import androidx.room.*
import com.example.takstud.data.local.entity.SyncQueueEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface SyncQueueDao {
    @Query("SELECT * FROM sync_queue ORDER BY timestamp ASC")
    fun getSyncQueue(): Flow<List<SyncQueueEntity>>

    @Query("SELECT * FROM sync_queue WHERE synced = 0 ORDER BY timestamp ASC")
    fun getPendingSyncOperations(): Flow<List<SyncQueueEntity>>

    @Query("SELECT * FROM sync_queue WHERE id = :queueId")
    suspend fun getSyncQueueItemById(queueId: Int): SyncQueueEntity?

    @Insert
    suspend fun insertSyncQueueItem(item: SyncQueueEntity): Long

    @Insert
    suspend fun insertSyncQueueItems(items: List<SyncQueueEntity>)

    @Update
    suspend fun updateSyncQueueItem(item: SyncQueueEntity)

    @Delete
    suspend fun deleteSyncQueueItem(item: SyncQueueEntity)

    @Query("DELETE FROM sync_queue WHERE id = :queueId")
    suspend fun deleteSyncQueueItemById(queueId: Int)

    @Query("UPDATE sync_queue SET synced = 1 WHERE id IN (:queueIds)")
    suspend fun markAsSynced(queueIds: List<Int>)

    @Query("DELETE FROM sync_queue WHERE synced = 1")
    suspend fun clearSyncedItems()

    @Query("DELETE FROM sync_queue")
    suspend fun clearAll()

    @Query("SELECT COUNT(*) FROM sync_queue WHERE synced = 0")
    suspend fun getPendingOperationCount(): Int
}
