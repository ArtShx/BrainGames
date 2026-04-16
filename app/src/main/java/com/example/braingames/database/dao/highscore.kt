package com.example.braingames.database.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import com.example.braingames.core.GameType
import com.example.braingames.database.entity.HighScore

@Dao
interface HighScoreDao {

    @Insert
    suspend fun insertScore(score: HighScore)

    // Get Top 10 scores for a specific game, sorted by score (DESC) then time (ASC)
    @Query("""
        SELECT * FROM high_scores
    """)
    suspend fun getAllScores(): List<HighScore>

    // Get Top 10 scores for a specific game, sorted by score (DESC) then time (ASC)
    @Query("""
        SELECT * FROM high_scores
        WHERE gameReferenceId = :gameType
    """)
    suspend fun getAllScoresByGame(gameType: String): List<HighScore>

    // Get Top 10 scores for a specific game, sorted by score (DESC) then time (ASC)
    @Query("""
        SELECT * FROM high_scores 
        WHERE gameReferenceId = :gameId AND difficulty = :difficulty 
        ORDER BY score DESC, duration ASC 
        LIMIT 10
    """)
    suspend fun getTopScores(gameId: String, difficulty: String): List<HighScore>

    @Query("DELETE FROM high_scores WHERE id = :scoreId")
    suspend fun deleteScore(scoreId: Long)

    // Feature: Get the player's personal best for a specific game
    @Query("SELECT MAX(score) FROM high_scores WHERE gameReferenceId = :gameId")
    suspend fun getPersonalBest(gameId: String): Int?
}