package com.example.braingames.database.converter

import androidx.room.TypeConverter
import com.example.braingames.core.GameType

class Converters {
    @TypeConverter
    fun fromGameType(value: GameType): String {
        return value.name
    }

    @TypeConverter
    fun toGameType(value: String): GameType {
        return GameType.valueOf(value)
    }
}