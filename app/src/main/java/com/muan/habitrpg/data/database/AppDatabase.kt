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
            Log.d("AppDatabase", "getDatabase викликано")
            return INSTANCE ?: synchronized(this) {
                Log.d("AppDatabase", "Створення нового екземпляру бази даних")
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "habit_rpg_database"
                )
                    .addCallback(DatabaseCallback())
                    .build()
                INSTANCE = instance
                Log.d("AppDatabase", "База даних створена")
                instance
            }
        }
    }

    private class DatabaseCallback : RoomDatabase.Callback() {
        override fun onCreate(db: SupportSQLiteDatabase) {
            super.onCreate(db)
            Log.d("AppDatabase", "onCreate callback - заповнюємо базу даних")
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
    Log.d("AppDatabase", "populateDatabase - початок")
    val userDao = db.userDao()
    val statDao = db.statDao()
    val activityDao = db.activityDao()

    // Створити користувача
    userDao.insertUser(User(id = 1, username = "Mustard Andrew"))
    Log.d("AppDatabase", "User створено")

    // Створити стати
    val stats = listOf(
        Stat(userId = 1, statType = "STR", currentXp = 0, currentLevel = 1), // Сила
        Stat(userId = 1, statType = "INT", currentXp = 0, currentLevel = 1), // Розум
        Stat(userId = 1, statType = "DEX", currentXp = 0, currentLevel = 1), // Спритність
    )
    statDao.insertAll(stats)
    Log.d("AppDatabase", "Stats створено: ${stats.size} записів")

    // Створити 4 базові активності (твої!)
    val activities = listOf(
        Activity(name = "Віджимання 20 разів", statType = "STR", baseXpReward = 10),
        Activity(name = "Читання 30 хв", statType = "INT", baseXpReward = 15),
        Activity(name = "Програмування 1 год", statType = "DEX", baseXpReward = 20),
        Activity(name = "Малювання 15 хв", statType = "DEX", baseXpReward = 12)
    )
    activityDao.insertAll(activities)

    Log.d("AppDatabase", "Activities створено: ${activities.size} записів")

    Log.d("AppDatabase", "populateDatabase - завершено")
}