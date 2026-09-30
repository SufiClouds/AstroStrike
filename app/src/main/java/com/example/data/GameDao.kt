package com.example.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface GameDao {
    @Query("SELECT * FROM high_scores ORDER BY score DESC LIMIT 20")
    fun getTopScores(): Flow<List<HighScoreEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertHighScore(highScore: HighScoreEntity): Long

    @Query("SELECT * FROM pilot_stats WHERE id = 1")
    fun getPilotStats(): Flow<PilotStatsEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdatePilotStats(stats: PilotStatsEntity)

    @Query("DELETE FROM high_scores")
    suspend fun clearLeaderboard()
}
