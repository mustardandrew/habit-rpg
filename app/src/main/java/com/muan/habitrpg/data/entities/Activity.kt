package com.muan.habitrpg.data.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "activities")
data class Activity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String,
    val statType: String, // "STR", "INT", "DEX", "END"
    val baseXpReward: Int,
    val isActive: Boolean = true
)