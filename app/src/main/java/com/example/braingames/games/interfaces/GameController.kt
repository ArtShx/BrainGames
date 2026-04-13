package com.example.braingames.games.interfaces

import com.example.braingames.core.GameRuleEngine
import com.example.braingames.core.GameSnapshot
import com.example.braingames.database.entity.HighScore

interface GameController {
    val engine: GameRuleEngine
    var gameOver: Boolean

    fun onCellTap(current: GameSnapshot, row: Int, col: Int): GameSnapshot
    fun reset(): GameSnapshot
    fun isGameOver(): Boolean = gameOver
    fun getElapsedTime(): Long
    fun getHighScore(): HighScore
}
