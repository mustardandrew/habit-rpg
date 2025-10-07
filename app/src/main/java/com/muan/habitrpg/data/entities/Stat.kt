package com.muan.habitrpg.data.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "stats")
data class Stat(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val userId: Int = 1,
    val statType: String, // "STR", "INT", "DEX", "END"
    val currentXp: Int = 0,
    val currentLevel: Int = 1
)