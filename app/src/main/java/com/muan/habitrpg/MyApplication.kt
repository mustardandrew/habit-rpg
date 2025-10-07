package com.muan.habitrpg

import android.app.Application
import com.muan.habitrpg.data.database.AppDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

class MyApplication : Application() {
    // Database instance for the entire application
    val database: AppDatabase by lazy {
        AppDatabase.getDatabase(this)
    }

    private val applicationScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    override fun onCreate() {
        super.onCreate()

        // Initialize the database at app startup
        applicationScope.launch {
            // This will create the database and trigger the callback
            database.openHelper.writableDatabase
        }
    }
}