package com.example.braingames.games.queens

import com.example.braingames.core.BoardState
import com.example.braingames.core.GameMeta
import com.example.braingames.core.GameRuleEngine
import com.example.braingames.core.GameType
import com.example.braingames.core.Hint
import kotlin.math.abs

class QueensEngine : GameRuleEngine {
    override val gameType: GameType = GameType.Queens
    override val meta: GameMeta = GameMeta(title = "Queens", boardRows = 6, boardCols = 6)

    override fun isMoveValid(boardState: BoardState, row: Int, col: Int, newValue: String?): Boolean {
        if (newValue != "Q") return true
        for (r in 0 until boardState.rows) {
            for (c in 0 until boardState.cols) {
                if (r == row && c == col) continue
                if (boardState.cellAt(r, c).value == "Q") {
                    if (r == row || c == col) return false
                    if (abs(r - row) == abs(c - col)) return false
                }
            }
        }
        return true
    }

    override fun applyMove(boardState: BoardState, row: Int, col: Int, newValue: String?): BoardState {
        val next = boardState.cells.mapIndexed { r, rowCells ->
            rowCells.mapIndexed { c, cell ->
                if (r == row && c == col) {
                    cell.copy(value = newValue)
                } else {
                    cell
                }
            }
        }
        return boardState.copy(cells = next)
    }

    override fun nextValue(boardState: BoardState, row: Int, col: Int, currentValue: String?): String? {
        return if (currentValue == "Q") null else "Q"
    }

    override fun isSolved(boardState: BoardState): Boolean {
        val queens = mutableListOf<Pair<Int, Int>>()
        for (r in 0 until boardState.rows) {
            for (c in 0 until boardState.cols) {
                if (boardState.cellAt(r, c).value == "Q") queens.add(r to c)
            }
        }
        if (queens.size != boardState.rows) return false
        return queens.allIndexed { i, (r1, c1) ->
            queens.drop(i + 1).none { (r2, c2) ->
                r1 == r2 || c1 == c2 || abs(r1 - r2) == abs(c1 - c2)
            }
        }
    }

    override fun getHint(boardState: BoardState): Hint? {
        for (r in 0 until boardState.rows) {
            if ((0 until boardState.cols).none { c -> boardState.cellAt(r, c).value == "Q" }) {
                return Hint(r, 0, "Place a queen somewhere in row $r")
            }
        }
        return null
    }
}

private inline fun <T> List<T>.allIndexed(block: (index: Int, T) -> Boolean): Boolean {
    forEachIndexed { index, value ->
        if (!block(index, value)) return false
    }
    return true
}
