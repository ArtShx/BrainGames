package com.example.braingames.games

import com.example.braingames.core.GameRuleEngine
import com.example.braingames.core.GameType
import com.example.braingames.games.memory.MemoryEngine
import com.example.braingames.games.queens.QueensEngine
import com.example.braingames.games.simon.SimonSaysEngine
import com.example.braingames.games.tango.TangoEngine
import com.example.braingames.games.zip.ZipEngine

object GameFactory {
    fun engineFor(type: GameType): GameRuleEngine {
        return when (type) {
            GameType.Queens -> QueensEngine()
            GameType.Zip -> ZipEngine()
            GameType.Tango -> TangoEngine()
            GameType.Memory -> MemoryEngine()
            GameType.SimonSays -> SimonSaysEngine()
        }
    }
}
