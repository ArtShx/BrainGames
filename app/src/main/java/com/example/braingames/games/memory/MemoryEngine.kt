package com.example.braingames.games.memory

import com.example.braingames.core.BoardState
import com.example.braingames.core.GameMeta
import com.example.braingames.core.GameRuleEngine
import com.example.braingames.core.GameType
import com.example.braingames.core.Hint

class MemoryEngine : GameRuleEngine {
    override val gameType: GameType = GameType.Memory
    override val meta: GameMeta = GameMeta(title = "Memory", boardRows = 4, boardCols = 4)

    override fun isMoveValid(boardState: BoardState, row: Int, col: Int, newValue: String?): Boolean {
        return row in 0 until boardState.rows && col in 0 until boardState.cols
    }

    override fun applyMove(boardState: BoardState, row: Int, col: Int, newValue: String?): BoardState {
        val next = boardState.cells.mapIndexed { r, rowCells ->
            rowCells.mapIndexed { c, cell ->
                if (r == row && c == col) {
                    val showValue = newValue ?: ""
                    cell.copy(value = showValue)
                } else {
                    cell
                }
            }
        }
        return boardState.copy(cells = next)
    }

    override fun nextValue(boardState: BoardState, row: Int, col: Int, currentValue: String?): String? {
        return if (currentValue == "●") "" else "●"
    }

    override fun isSolved(boardState: BoardState): Boolean {
        return boardState.cells.flatten().all { it.value == "●" }
    }

    override fun getHint(boardState: BoardState): Hint? {
        val firstHidden = boardState.cells
            .flatten()
            .indexOfFirst { it.value != "●" }
            .takeIf { it >= 0 } ?: return null
        val row = firstHidden / boardState.cols
        val col = firstHidden % boardState.cols
        return Hint(row = row, col = col, message = "Try revealing [$row,$col]")
    }
}
