package com.example.braingames.games.factory

import com.example.braingames.core.GameRuleEngine
import com.example.braingames.core.GameType
import com.example.braingames.games.interfaces.GameController
import com.example.braingames.games.memory.MemoryEngine
import com.example.braingames.games.memory.MemoryGameController
import com.example.braingames.games.queens.QueensEngine
import com.example.braingames.games.simon.SimonSaysEngine
import com.example.braingames.games.simon.SimonSaysGameController
import com.example.braingames.games.tango.TangoEngine
import com.example.braingames.games.zip.ZipEngine
import kotlin.reflect.KClass

object ControllerFactory {
    fun make(type: GameType, engine: GameRuleEngine): GameController {
        return when (type) {
            GameType.Memory -> MemoryGameController(engine as MemoryEngine)
            GameType.SimonSays -> SimonSaysGameController(engine as SimonSaysEngine)
            else -> TODO("Implement other games")
        }
    }
}