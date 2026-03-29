package com.example.braingames.core

class DefaultPuzzleGenerator : PuzzleGenerator {
    override fun createInitialState(gameType: GameType, difficulty: Difficulty): BoardState {
        val size = when (difficulty) {
            Difficulty.Easy -> 4
            Difficulty.Medium -> 6
            Difficulty.Hard -> 8
        }
        val simonSide = when (difficulty) {
            Difficulty.Easy -> 3
            Difficulty.Medium -> 4
            Difficulty.Hard -> 5
        }
        return when (gameType) {
            GameType.Memory -> BoardState.empty(rows = 4, cols = 4)
            GameType.SimonSays -> BoardState.empty(rows = simonSide, cols = simonSide)
            GameType.Queens -> BoardState.empty(rows = size, cols = size)
            GameType.Zip -> BoardState.empty(rows = size, cols = size)
            GameType.Tango -> BoardState.empty(rows = size, cols = size)
        }
    }
}
