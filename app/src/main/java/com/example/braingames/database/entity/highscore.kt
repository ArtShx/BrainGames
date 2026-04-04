package com.example.braingames.database.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

import androidx.room.*
import com.example.braingames.core.GameType
import java.util.Date

@Entity(tableName = "game_metadata")
data class GameMetadata(
    @PrimaryKey val gameId: GameType,
    val gameName: String,
    val description: String?
)

@Entity(
    tableName = "high_scores",
    foreignKeys = [
        ForeignKey(
            entity = GameMetadata::class,
            parentColumns = ["gameId"],
            childColumns = ["gameReferenceId"],
            onDelete = ForeignKey.CASCADE // If a game is deleted, delete its scores
        )
    ],
    indices = [Index(value = ["gameReferenceId"])] // Index for faster lookups
)
data class HighScore(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,

    @ColumnInfo(name = "gameReferenceId")
    val gameReferenceId: String,

    @ColumnInfo(name = "score")
    val score: Int,                // Points/Sequence length

    @ColumnInfo(name = "duration")
    val duration: Long,   // Duration in milliseconds

    @ColumnInfo(name = "difficulty")
    val difficulty: String,        // "Easy", "Medium", "Hard"

    @ColumnInfo(name = "timestamp")
    val timestamp: Long = System.currentTimeMillis() // Date of game
)