package com.muan.habitrpg.data.database

import android.content.Context
import android.util.Log
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.muan.habitrpg.data.dao.*
import com.muan.habitrpg.data.entities.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(
    entities = [User::class, Stat::class, Activity::class, ActivityLog::class],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun userDao(): UserDao
    abstract fun statDao(): StatDao
    abstract fun activityDao(): ActivityDao
    abstract fun activityLogDao(): ActivityLogDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                // Create new database instance
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "habit_rpg_database"
                )
                    .addCallback(DatabaseCallback())
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }

    private class DatabaseCallback : RoomDatabase.Callback() {
        override fun onCreate(db: SupportSQLiteDatabase) {
            super.onCreate(db)

            // Populate the database
            INSTANCE?.let { database ->
                CoroutineScope(Dispatchers.IO).
                launch {
                    populateDatabase(database)
                }
            }
        }
    }
}

private suspend fun populateDatabase(db: AppDatabase) {
    val userDao = db.userDao()
    val statDao = db.statDao()
    val activityDao = db.activityDao()

    // Create user
    userDao.insertUser(User(id = 1, username = "Mustard Andrew"))
    Log.d("AppDatabase", "User створено")

    // Create stats
    val stats = listOf(
        Stat(userId = 1, statType = "STR", currentXp = 0, currentLevel = 1), // Strong
        Stat(userId = 1, statType = "INT", currentXp = 0, currentLevel = 1), // Intellect
        Stat(userId = 1, statType = "DEX", currentXp = 0, currentLevel = 1), // Dexterity
    )
    statDao.insertAll(stats)

    // Create 4 base activities (my activities)
    val activities = listOf(
        Activity(name = "Віджимання 20 разів", statType = "STR", baseXpReward = 10),
        Activity(name = "Читання 30 хв", statType = "INT", baseXpReward = 15),
        Activity(name = "Програмування 1 год", statType = "DEX", baseXpReward = 20),
        Activity(name = "Малювання 15 хв", statType = "DEX", baseXpReward = 12)
    )
    activityDao.insertAll(activities)
}