package com.example.braingames.core

enum class GameType {
    Memory,
    SimonSays,
    Queens,
    Zip,
    Tango;

    fun isImplemented(): Boolean {
        return when (this) {
            Memory -> true
            SimonSays -> true
            else -> false
        }
    }
}

enum class Difficulty {
    Easy,
    Medium,
    Hard
}
