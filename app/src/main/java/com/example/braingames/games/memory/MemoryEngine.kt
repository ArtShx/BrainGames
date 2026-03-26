package com.example.braingames.games.memory

import com.example.braingames.core.BoardState
import com.example.braingames.core.BoardCoordinate
import com.example.braingames.core.CellState
import com.example.braingames.core.GameMeta
import com.example.braingames.core.GameRuleEngine
import com.example.braingames.core.GameType
import com.example.braingames.core.Hint

class MemoryEngine : GameRuleEngine {
    override val gameType: GameType = GameType.Memory
    override val meta: GameMeta = GameMeta(title = "Memory", boardRows = 4, boardCols = 4)
    val maxRound: Int = 10
    val initialHearts: Int = 3
    val previewMillis: Long = 2_000L

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
        return "●"
    }

    override fun isSolved(boardState: BoardState): Boolean {
        return boardState.cells.flatten().all { it.value == "●" }
    }

    override fun getHint(boardState: BoardState): Hint? {
        return Hint(row = 0, col = 0, message = "Watch the preview, then tap only the shown cells.")
    }

    fun gridSizeForRound(round: Int): Int {
        return when (round.coerceIn(1, maxRound)) {
            in 1..3 -> 4
            in 4..7 -> 5
            in 8..10 -> 6
            else -> 7
        }
    }

    fun targetCountForRound(round: Int, boardSize: Int): Int {
        return (round + 1).coerceAtMost(boardSize * boardSize)
    }

    fun generateTargets(round: Int): Set<BoardCoordinate> {
        val boardSize = gridSizeForRound(round)
        val targetCount = targetCountForRound(round, boardSize)
        val pool = buildList {
            repeat(boardSize) { r ->
                repeat(boardSize) { c ->
                    add(BoardCoordinate(r, c))
                }
            }
        }
        return pool.shuffled().take(targetCount).toSet()
    }

    fun createRoundBoard(
        round: Int,
        targets: Set<BoardCoordinate>,
        revealed: Boolean,
        tapped: Set<BoardCoordinate> = emptySet()
    ): BoardState {
        val size = gridSizeForRound(round)
        val cells = List(size) { row ->
            List(size) { col ->
                val coord = BoardCoordinate(row, col)
                val isTarget = coord in targets
                CellState(
                    value = when {
                        coord in tapped -> "●"
                        revealed && isTarget -> "●"
                        else -> ""
                    },
                    isHighlighted = revealed && isTarget
                )
            }
        }
        return BoardState(rows = size, cols = size, cells = cells)
    }
}
