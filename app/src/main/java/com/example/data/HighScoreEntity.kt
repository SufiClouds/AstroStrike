package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "high_scores")
data class HighScoreEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val score: Long,
    val wave: Int,
    val enemiesKilled: Int,
    val dateTimestamp: Long = System.currentTimeMillis()
)
