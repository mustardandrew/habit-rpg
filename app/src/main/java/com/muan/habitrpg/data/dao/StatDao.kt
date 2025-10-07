package com.muan.habitrpg.data.dao

import androidx.room.*
import com.muan.habitrpg.data.entities.Stat
import kotlinx.coroutines.flow.Flow

@Dao
interface StatDao {
    @Query("SELECT * FROM stats WHERE userId = 1")
    fun getAllStats(): Flow<List<Stat>>

    @Query("SELECT * FROM stats WHERE statType = :type AND userId = 1")
    fun getStatByType(type: String): Flow<Stat?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(stats: List<Stat>)

    @Query("UPDATE stats SET currentXp = :xp, currentLevel = :level WHERE statType = :type")
    suspend fun updateStat(type: String, xp: Int, level: Int)
}