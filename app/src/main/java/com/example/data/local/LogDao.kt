package com.example.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface LogDao {
    @Query("SELECT * FROM dispatch_logs ORDER BY timestampEpochMillis DESC LIMIT 100")
    fun getAllLogs(): Flow<List<DispatchLogEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(log: DispatchLogEntity): Long

    @Query("SELECT COUNT(*) FROM dispatch_logs WHERE status != 'FAILED'")
    fun getSuccessfulCount(): Flow<Int>

    @Query("SELECT SUM(estimatedReach) FROM dispatch_logs WHERE status != 'FAILED'")
    fun getTotalEstimatedReach(): Flow<Int?>

    @Query("DELETE FROM dispatch_logs")
    suspend fun clearAll()
}
