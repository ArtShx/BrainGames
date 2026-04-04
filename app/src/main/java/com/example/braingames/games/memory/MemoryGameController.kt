package com.example.braingames.games.memory

import com.example.braingames.core.BoardCoordinate
import com.example.braingames.core.BoardState
import com.example.braingames.core.GameResult
import com.example.braingames.core.GameSnapshot
import com.example.braingames.games.interfaces.GameController

/**
 * Keeps all state and transitions for the Memory game.
 * The UI/view model can delegate to this controller to stay generic.
 */
class MemoryGameController (override val engine: MemoryEngine): GameController {
    private var round: Int = 1
    private var hearts: Int = engine.initialHearts
    private var targets: Set<BoardCoordinate> = emptySet()
    private val tapped: MutableSet<BoardCoordinate> = mutableSetOf()

    private var previewActive: Boolean = false
    private var statusText: String = ""
    private var gameOver: Boolean = false

    fun initialBoard(): BoardState = BoardState.empty(rows = 4, cols = 4)

    fun reset(): GameSnapshot {
        round = 1
        hearts = engine.initialHearts
        targets = emptySet()
        tapped.clear()
        previewActive = false
        gameOver = false
        statusText = "Round $round"
        return startMemoryRound(
            current = GameSnapshot(boardState = initialBoard()),
            reuseRound = false
        )
    }

    fun onPreviewFinished(current: GameSnapshot): GameSnapshot {
        if (!previewActive) return current
        previewActive = false

        return current.copy(
            boardState = engine.createRoundBoard(
                round = round,
                targets = targets,
                revealed = false,
                tapped = tapped
            ),
            gameResult = GameResult.InProgress
        )
    }

    fun onCellTap(current: GameSnapshot, row: Int, col: Int): GameSnapshot {
        if (previewActive || current.gameResult is GameResult.Solved) {
            return current.copy(gameResult = GameResult.InvalidMove("Wait for preview to end"))
        }
        if (gameOver) return current

        if (row !in 0 until current.boardState.rows || col !in 0 until current.boardState.cols) {
            return current.copy(gameResult = GameResult.InvalidMove("Invalid cell"))
        }

        val coord = BoardCoordinate(row, col)
        val isTarget = coord in targets

        if (!isTarget) {
            hearts -= 1
            if (hearts <= 0) {
                gameOver = true
                previewActive = false
                statusText = "Game over. You ran out of hearts."
                return current.copy(
                    gameResult = GameResult.InvalidMove("Game over"),
                    moveCount = current.moveCount + 1
                )
            }

            tapped.clear()
            statusText = "Missed. Hearts left: $hearts. Retry round $round."
            return startMemoryRound(
                current = current,
                reuseRound = true
            )
        }

        tapped.add(coord)
        val updatedBoard = engine.createRoundBoard(
            round = round,
            targets = targets,
            revealed = false,
            tapped = tapped
        )

        val nextMoveCount = current.moveCount + 1

        if (tapped.size == targets.size) {
            if (round >= engine.maxRound) {
                gameOver = false
                statusText = "You won all ${engine.maxRound} rounds."
                return current.copy(
                    boardState = updatedBoard,
                    moveCount = nextMoveCount,
                    gameResult = GameResult.Solved(moveCount = nextMoveCount)
                )
            }

            round += 1
            tapped.clear()
            statusText = "Round cleared. Starting round $round."
            // Keep moveCount consistent with the original behavior:
            // snapshot is updated for the last tap, then preview board starts.
            return startMemoryRound(
                current = current.copy(boardState = updatedBoard, moveCount = nextMoveCount),
                reuseRound = false
            )
        }

        return current.copy(
            boardState = updatedBoard,
            moveCount = nextMoveCount,
            gameResult = GameResult.InProgress
        )
    }

    private fun startMemoryRound(current: GameSnapshot, reuseRound: Boolean): GameSnapshot {
        if (!reuseRound) {
            targets = engine.generateTargets(round)
        }

        previewActive = true
        val board = engine.createRoundBoard(
            round = round,
            targets = targets,
            revealed = true,
            tapped = emptySet()
        )
        return current.copy(boardState = board, gameResult = GameResult.InProgress)
    }

    fun getRound(): Int = round
    fun getHearts(): Int = hearts
    fun isPreviewActive(): Boolean = previewActive
    fun getStatusText(): String = statusText
    fun getPreviewMillis(): Long = engine.previewMillis
    fun isGameOver(): Boolean = gameOver

    fun getHintMessage(): String {
        val previewLabel = if (previewActive) "Preview" else "Answer"
        return "Round $round/${engine.maxRound} | Hearts: $hearts | Phase: $previewLabel"
    }
}

