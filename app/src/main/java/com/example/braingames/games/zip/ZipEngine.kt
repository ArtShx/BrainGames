package com.example.braingames.games.zip

import com.example.braingames.core.BoardState
import com.example.braingames.core.GameMeta
import com.example.braingames.core.GameRuleEngine
import com.example.braingames.core.GameType
import com.example.braingames.core.Hint

class ZipEngine : GameRuleEngine {
    override val gameType: GameType = GameType.Zip
    override val meta: GameMeta = GameMeta(title = "Zip", boardRows = 5, boardCols = 5)

    override fun isMoveValid(boardState: BoardState, row: Int, col: Int, newValue: String?): Boolean {
        return newValue == null || newValue == "•"
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
        return if (currentValue == "•") null else "•"
    }

    override fun isSolved(boardState: BoardState): Boolean {
        return boardState.cells.flatten().all { it.value == "•" }
    }

    override fun getHint(boardState: BoardState): Hint? {
        val index = boardState.cells.flatten().indexOfFirst { it.value != "•" }
        if (index < 0) return null
        return Hint(
            row = index / boardState.cols,
            col = index % boardState.cols,
            message = "Fill all cells for a complete zip path."
        )
    }
}
