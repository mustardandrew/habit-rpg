package com.muan.habitrpg.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.muan.habitrpg.MyApplication
import com.muan.habitrpg.data.entities.*
import com.muan.habitrpg.data.repository.HabitRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class MainViewModel(application: Application) : AndroidViewModel(application) {

    private val database = (application as MyApplication).database
    private val repository = HabitRepository(database)

    // States
    val user = repository.getUser()
        .stateIn(viewModelScope, SharingStarted.Lazily, null)

    val stats = repository.getAllStats()
        .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    val activities = repository.getActiveActivities()
        .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    fun completeActivity(activityId: Int) {
        viewModelScope.launch {
            // TODO: Add logic
        }
    }

    fun skipActivity(activityId: Int) {
        viewModelScope.launch {
            // TODO: Add logic
        }
    }
}