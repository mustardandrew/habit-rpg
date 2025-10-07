package com.muan.habitrpg.data.repository

import com.muan.habitrpg.data.database.AppDatabase
import com.muan.habitrpg.data.entities.*
import kotlinx.coroutines.flow.Flow

class HabitRepository(private val database: AppDatabase) {

    // User
    fun getUser(): Flow<User?> = database.userDao().getUser()

    // Stats
    fun getAllStats(): Flow<List<Stat>> = database.statDao().getAllStats()

    fun getStatByType(type: String): Flow<Stat?> =
        database.statDao().getStatByType(type)

    // Activities
    fun getActiveActivities(): Flow<List<Activity>> =
        database.activityDao().getActiveActivities()

    suspend fun getActivityById(id: Int): Activity? =
        database.activityDao().getActivityById(id)

    // Activity Logs
    fun getRecentLogs(): Flow<List<ActivityLog>> =
        database.activityLogDao().getRecentLogs()

    suspend fun logActivity(activityId: Int, xpEarned: Int, wasCompleted: Boolean) {
        val log = ActivityLog(
            activityId = activityId,
            xpEarned = xpEarned,
            wasCompleted = wasCompleted
        )
        database.activityLogDao().insert(log)
    }

    // // XP accrual
    suspend fun addXpToStat(statType: String, xp: Int) {
        val stat = database.statDao().getStatByType(statType)
        // TODO: Here will be the calculation logic (will be added later)
    }
}