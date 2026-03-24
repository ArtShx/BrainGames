package com.example.braingames.games.tango

import com.example.braingames.core.BoardState
import com.example.braingames.core.GameMeta
import com.example.braingames.core.GameRuleEngine
import com.example.braingames.core.GameType
import com.example.braingames.core.Hint

class TangoEngine : GameRuleEngine {
    override val gameType: GameType = GameType.Tango
    override val meta: GameMeta = GameMeta(title = "Tango", boardRows = 6, boardCols = 6)

    override fun isMoveValid(boardState: BoardState, row: Int, col: Int, newValue: String?): Boolean {
        if (newValue == null) return true
        if (newValue != "X" && newValue != "O") return false
        return !wouldCreateThreeInRow(boardState, row, col, newValue)
    }

    override fun applyMove(boardState: BoardState, row: Int, col: Int, newValue: String?): BoardState {
        val next = boardState.cells.mapIndexed { r, rowCells ->
            rowCells.mapIndexed { c, cell ->
                if (r == row && c == col) cell.copy(value = newValue) else cell
            }
        }
        return boardState.copy(cells = next)
    }

    override fun nextValue(boardState: BoardState, row: Int, col: Int, currentValue: String?): String? {
        return when (currentValue) {
            null -> "X"
            "X" -> "O"
            else -> null
        }
    }

    override fun isSolved(boardState: BoardState): Boolean {
        return boardState.cells.flatten().none { it.value == null }
    }

    override fun getHint(boardState: BoardState): Hint? {
        val index = boardState.cells.flatten().indexOfFirst { it.value == null }
        if (index < 0) return null
        return Hint(
            row = index / boardState.cols,
            col = index % boardState.cols,
            message = "Alternate X and O; avoid three adjacent same symbols."
        )
    }

    private fun wouldCreateThreeInRow(
        boardState: BoardState,
        row: Int,
        col: Int,
        newValue: String
    ): Boolean {
        val tempBoard = applyMove(boardState, row, col, newValue)
        return hasTripleInRows(tempBoard) || hasTripleInCols(tempBoard)
    }

    private fun hasTripleInRows(boardState: BoardState): Boolean {
        for (r in 0 until boardState.rows) {
            for (c in 0 until boardState.cols - 2) {
                val a = boardState.cellAt(r, c).value
                val b = boardState.cellAt(r, c + 1).value
                val d = boardState.cellAt(r, c + 2).value
                if (a != null && a == b && b == d) return true
            }
        }
        return false
    }

    private fun hasTripleInCols(boardState: BoardState): Boolean {
        for (c in 0 until boardState.cols) {
            for (r in 0 until boardState.rows - 2) {
                val a = boardState.cellAt(r, c).value
                val b = boardState.cellAt(r + 1, c).value
                val d = boardState.cellAt(r + 2, c).value
                if (a != null && a == b && b == d) return true
            }
        }
        return false
    }
}
