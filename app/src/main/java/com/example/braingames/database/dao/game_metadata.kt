package com.example.braingames.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.example.braingames.database.entity.GameMetadata

@Dao
interface GameMetadataDao {

    @Insert
    suspend fun insert(score: GameMetadata)


    @Query("""
        SELECT * FROM game_metadata
        LIMIT 100
    """)
    suspend fun getAll(): List<GameMetadata>
}