package com.muan.habitrpg.data.dao

import androidx.room.*
import com.muan.habitrpg.data.entities.ActivityLog
import kotlinx.coroutines.flow.Flow

@Dao
interface ActivityLogDao {
    @Query("SELECT * FROM activity_logs ORDER BY completedAt DESC LIMIT 50")
    fun getRecentLogs(): Flow<List<ActivityLog>>

    @Insert
    suspend fun insert(log: ActivityLog)

    @Query("SELECT COUNT(*) FROM activity_logs WHERE activityId = :activityId AND wasCompleted = 1")
    suspend fun getCompletionCount(activityId: Int): Int
}