package com.muan.habitrpg.data.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "users")
data class User(
    @PrimaryKey val id: Int = 1, // Завжди 1, бо один користувач
    val username: String = "Hero",
    val overallLevel: Int = 1,
    val createdAt: Long = System.currentTimeMillis()
)