package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "pilot_stats")
data class PilotStatsEntity(
    @PrimaryKey
    val id: Int = 1,
    val energyCores: Int = 50,
    val blasterLevel: Int = 1,
    val shieldLevel: Int = 1,
    val magnetLevel: Int = 1,
    val bombCount: Int = 2,
    val shipColorIndex: Int = 0,
    val highScore: Long = 0,
    val totalGamesPlayed: Int = 0,
    val totalEnemiesDefeated: Int = 0
)
