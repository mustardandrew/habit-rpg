package com.muan.habitrpg.data.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "activity_logs")
data class ActivityLog(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val activityId: Int,
    val completedAt: Long = System.currentTimeMillis(),
    val xpEarned: Int,
    val wasCompleted: Boolean // true = виконано, false = пропущено
)