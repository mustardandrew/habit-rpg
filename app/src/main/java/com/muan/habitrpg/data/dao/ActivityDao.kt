package com.muan.habitrpg.data.dao

import androidx.room.*
import com.muan.habitrpg.data.entities.Activity
import kotlinx.coroutines.flow.Flow

@Dao
interface ActivityDao {
    @Query("SELECT * FROM activities WHERE isActive = 1")
    fun getActiveActivities(): Flow<List<Activity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(activities: List<Activity>)

    @Query("SELECT * FROM activities WHERE id = :id")
    suspend fun getActivityById(id: Int): Activity?
}