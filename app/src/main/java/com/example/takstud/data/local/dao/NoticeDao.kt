package com.example.takstud.data.local.dao

import androidx.room.*
import com.example.takstud.data.local.entity.NoticeEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface NoticeDao {
    @Query("SELECT * FROM notices ORDER BY createdAt DESC")
    fun getAllNotices(): Flow<List<NoticeEntity>>

    @Query("SELECT * FROM notices WHERE studentClass = :studentClass ORDER BY createdAt DESC")
    fun getNoticesByClass(studentClass: String): Flow<List<NoticeEntity>>

    @Query("SELECT * FROM notices WHERE id = :noticeId")
    suspend fun getNoticeById(noticeId: String): NoticeEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertNotice(notice: NoticeEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertNotices(notices: List<NoticeEntity>)

    @Update
    suspend fun updateNotice(notice: NoticeEntity)

    @Delete
    suspend fun deleteNotice(notice: NoticeEntity)

    @Query("DELETE FROM notices WHERE id = :noticeId")
    suspend fun deleteNoticeById(noticeId: String)

    @Query("UPDATE notices SET isSynced = 1 WHERE id IN (:noticeIds)")
    suspend fun markAsSynced(noticeIds: List<String>)

    @Query("SELECT * FROM notices WHERE isSynced = 0")
    suspend fun getUnsyncedNotices(): List<NoticeEntity>

    @Query("DELETE FROM notices")
    suspend fun deleteAll()
}
