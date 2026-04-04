package com.example.braingames.database

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.example.braingames.database.converter.Converters
import com.example.braingames.database.dao.HighScoreDao
import com.example.braingames.database.entity.GameMetadata
import com.example.braingames.database.entity.HighScore

@Database(entities = [HighScore::class, GameMetadata::class], version = 1)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun highScoreDao(): HighScoreDao
}

