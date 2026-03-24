package com.example.braingames.core

data class CellState(
    val value: String? = null,
    val isSelected: Boolean = false,
    val isEditable: Boolean = true,
    val isHighlighted: Boolean = false
)

data class BoardState(
    val rows: Int,
    val cols: Int,
    val cells: List<List<CellState>>
) {
    init {
        require(rows > 0) { "rows must be greater than 0" }
        require(cols > 0) { "cols must be greater than 0" }
        require(cells.size == rows) { "cells row count must equal rows" }
        require(cells.all { it.size == cols }) { "all cell rows must equal cols" }
    }

    fun cellAt(row: Int, col: Int): CellState = cells[row][col]

    companion object {
        fun empty(rows: Int, cols: Int): BoardState {
            val grid = List(rows) { List(cols) { CellState() } }
            return BoardState(rows = rows, cols = cols, cells = grid)
        }
    }
}

data class BoardCoordinate(
    val row: Int,
    val col: Int
)

data class GameMeta(
    val title: String,
    val boardRows: Int,
    val boardCols: Int
)

sealed interface GameResult {
    data object InProgress : GameResult
    data class Solved(val moveCount: Int, val elapsedTimeMillis: Long? = null) : GameResult
    data class InvalidMove(val reason: String) : GameResult
}

data class Hint(
    val row: Int,
    val col: Int,
    val message: String
)

interface GameRuleEngine {
    val gameType: GameType
    val meta: GameMeta

    fun isMoveValid(
        boardState: BoardState,
        row: Int,
        col: Int,
        newValue: String?
    ): Boolean

    fun applyMove(
        boardState: BoardState,
        row: Int,
        col: Int,
        newValue: String?
    ): BoardState

    fun nextValue(
        boardState: BoardState,
        row: Int,
        col: Int,
        currentValue: String?
    ): String?

    fun isSolved(boardState: BoardState): Boolean

    fun getHint(boardState: BoardState): Hint?
}

interface PuzzleGenerator {
    fun createInitialState(gameType: GameType, difficulty: Difficulty): BoardState
}

data class GameSnapshot(
    val boardState: BoardState,
    val moveCount: Int = 0,
    val gameResult: GameResult = GameResult.InProgress
)
