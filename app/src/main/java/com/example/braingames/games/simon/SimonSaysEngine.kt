package com.example.braingames.games.simon

import com.example.braingames.core.BoardCoordinate
import com.example.braingames.core.BoardState
import com.example.braingames.core.CellState
import com.example.braingames.core.Difficulty
import com.example.braingames.core.GameMeta
import com.example.braingames.core.GameRuleEngine
import com.example.braingames.core.GameType
import com.example.braingames.core.Hint

class SimonSaysEngine : GameRuleEngine {
    override val gameType: GameType = GameType.SimonSays
    override val meta: GameMeta = GameMeta(title = "Simon Says", boardRows = 5, boardCols = 5)

    val maxRound: Int = 10
    val initialHearts: Int = 3
    val stepHighlightMillis: Long = 600L
    val stepGapMillis: Long = 200L

    fun gridSide(difficulty: Difficulty): Int = when (difficulty) {
        Difficulty.Easy -> 3
        Difficulty.Medium -> 4
        Difficulty.Hard -> 5
    }

    fun createBoard(
        difficulty: Difficulty,
        sequence: List<BoardCoordinate>,
        playbackHighlight: BoardCoordinate?,
        inputProgress: Int
    ): BoardState {
        val side = gridSide(difficulty)
        val cells = List(side) { row ->
            List(side) { col ->
                val coord = BoardCoordinate(row, col)
                val inPrefix = (0 until inputProgress.coerceAtMost(sequence.size)).any { sequence[it] == coord }
                CellState(
                    value = if (inPrefix) "●" else "",
                    isHighlighted = coord == playbackHighlight
                )
            }
        }
        return BoardState(rows = side, cols = side, cells = cells)
    }

    override fun isMoveValid(boardState: BoardState, row: Int, col: Int, newValue: String?): Boolean {
        return row in 0 until boardState.rows && col in 0 until boardState.cols
    }

    override fun applyMove(boardState: BoardState, row: Int, col: Int, newValue: String?): BoardState {
        return boardState
    }

    override fun nextValue(boardState: BoardState, row: Int, col: Int, currentValue: String?): String? {
        return null
    }

    override fun isSolved(boardState: BoardState): Boolean {
        return false
    }

    override fun getHint(boardState: BoardState): Hint? {
        return Hint(row = 0, col = 0, message = "Watch the pattern, then tap the cells in order.")
    }
}
