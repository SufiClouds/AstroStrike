package com.example.data

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull

class GameRepository(private val gameDao: GameDao) {

    val topScores: Flow<List<HighScoreEntity>> = gameDao.getTopScores()
    val pilotStats: Flow<PilotStatsEntity?> = gameDao.getPilotStats()

    suspend fun saveGameResult(
        score: Long,
        wave: Int,
        enemiesKilled: Int,
        energyCoresCollected: Int
    ): Boolean {
        // Record high score
        val currentStats = pilotStats.firstOrNull() ?: PilotStatsEntity()
        val isNewRecord = score > currentStats.highScore

        gameDao.insertHighScore(
            HighScoreEntity(
                score = score,
                wave = wave,
                enemiesKilled = enemiesKilled
            )
        )

        val updatedStats = currentStats.copy(
            energyCores = currentStats.energyCores + energyCoresCollected,
            highScore = maxOf(currentStats.highScore, score),
            totalGamesPlayed = currentStats.totalGamesPlayed + 1,
            totalEnemiesDefeated = currentStats.totalEnemiesDefeated + enemiesKilled
        )
        gameDao.insertOrUpdatePilotStats(updatedStats)
        return isNewRecord
    }

    suspend fun upgradeBlaster(cost: Int): Boolean {
        val stats = pilotStats.firstOrNull() ?: PilotStatsEntity()
        if (stats.energyCores >= cost && stats.blasterLevel < 5) {
            gameDao.insertOrUpdatePilotStats(
                stats.copy(
                    energyCores = stats.energyCores - cost,
                    blasterLevel = stats.blasterLevel + 1
                )
            )
            return true
        }
        return false
    }

    suspend fun upgradeShield(cost: Int): Boolean {
        val stats = pilotStats.firstOrNull() ?: PilotStatsEntity()
        if (stats.energyCores >= cost && stats.shieldLevel < 5) {
            gameDao.insertOrUpdatePilotStats(
                stats.copy(
                    energyCores = stats.energyCores - cost,
                    shieldLevel = stats.shieldLevel + 1
                )
            )
            return true
        }
        return false
    }

    suspend fun upgradeMagnet(cost: Int): Boolean {
        val stats = pilotStats.firstOrNull() ?: PilotStatsEntity()
        if (stats.energyCores >= cost && stats.magnetLevel < 5) {
            gameDao.insertOrUpdatePilotStats(
                stats.copy(
                    energyCores = stats.energyCores - cost,
                    magnetLevel = stats.magnetLevel + 1
                )
            )
            return true
        }
        return false
    }

    suspend fun buyNovaBomb(cost: Int): Boolean {
        val stats = pilotStats.firstOrNull() ?: PilotStatsEntity()
        if (stats.energyCores >= cost && stats.bombCount < 5) {
            gameDao.insertOrUpdatePilotStats(
                stats.copy(
                    energyCores = stats.energyCores - cost,
                    bombCount = stats.bombCount + 1
                )
            )
            return true
        }
        return false
    }

    suspend fun selectShipColor(colorIndex: Int) {
        val stats = pilotStats.firstOrNull() ?: PilotStatsEntity()
        gameDao.insertOrUpdatePilotStats(stats.copy(shipColorIndex = colorIndex))
    }

    suspend fun consumeBomb(): Boolean {
        val stats = pilotStats.firstOrNull() ?: PilotStatsEntity()
        if (stats.bombCount > 0) {
            gameDao.insertOrUpdatePilotStats(stats.copy(bombCount = stats.bombCount - 1))
            return true
        }
        return false
    }

    suspend fun ensureInitialStats() {
        val stats = pilotStats.firstOrNull()
        if (stats == null) {
            gameDao.insertOrUpdatePilotStats(PilotStatsEntity())
        }
    }
}
