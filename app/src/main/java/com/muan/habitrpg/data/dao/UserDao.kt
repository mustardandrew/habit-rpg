package com.muan.habitrpg.data.dao

import androidx.room.*
import com.muan.habitrpg.data.entities.User
import kotlinx.coroutines.flow.Flow

@Dao
interface UserDao {
    @Query("SELECT * FROM users WHERE id = 1")
    fun getUser(): Flow<User?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUser(user: User)

    @Query("UPDATE users SET overallLevel = :level WHERE id = 1")
    suspend fun updateLevel(level: Int)
}