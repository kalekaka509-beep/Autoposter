package com.example.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface QueueDao {
    @Query("SELECT * FROM queue_posts WHERE status = 'QUEUED' ORDER BY scheduledTimeEpochMillis ASC")
    fun getQueuedPosts(): Flow<List<QueuePostEntity>>

    @Query("SELECT * FROM queue_posts ORDER BY createdEpochMillis DESC")
    fun getAllPosts(): Flow<List<QueuePostEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(post: QueuePostEntity): Long

    @Update
    suspend fun update(post: QueuePostEntity)

    @Query("UPDATE queue_posts SET status = :newStatus WHERE id = :id")
    suspend fun updateStatus(id: Long, newStatus: String)

    @Query("DELETE FROM queue_posts WHERE id = :id")
    suspend fun deleteById(id: Long)

    @Query("SELECT * FROM queue_posts WHERE status = 'QUEUED' AND scheduledTimeEpochMillis <= :nowMillis")
    suspend fun getPendingExecution(nowMillis: Long): List<QueuePostEntity>
}
