package com.example.braingames.core

class DefaultPuzzleGenerator : PuzzleGenerator {
    override fun createInitialState(gameType: GameType, difficulty: Difficulty): BoardState {
        val size = when (difficulty) {
            Difficulty.Easy -> 4
            Difficulty.Medium -> 6
            Difficulty.Hard -> 8
        }
        return when (gameType) {
            GameType.Memory -> BoardState.empty(rows = 4, cols = 4)
            GameType.Queens -> BoardState.empty(rows = size, cols = size)
            GameType.Zip -> BoardState.empty(rows = size, cols = size)
            GameType.Tango -> BoardState.empty(rows = size, cols = size)
        }
    }
}
