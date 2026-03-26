package com.example.braingames.ui

import android.util.Log
import androidx.lifecycle.ViewModel
import com.example.braingames.core.BoardState
import com.example.braingames.core.BoardCoordinate
import com.example.braingames.core.DefaultPuzzleGenerator
import com.example.braingames.core.Difficulty
import com.example.braingames.core.GameResult
import com.example.braingames.core.GameSnapshot
import com.example.braingames.core.GameType
import com.example.braingames.games.GameFactory
import com.example.braingames.games.memory.MemoryEngine
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class GameViewModel(
    private val gameType: GameType,
    private val difficulty: Difficulty = Difficulty.Easy
) : ViewModel() {

    private val engine = GameFactory.engineFor(gameType)
    private val memoryEngine = engine as? MemoryEngine
    private val puzzleGenerator = DefaultPuzzleGenerator()
    private val moveHistory = ArrayDeque<BoardState>()
    private var memoryRound: Int = 1
    private var memoryHearts: Int = memoryEngine?.initialHearts ?: 0
    private var memoryTargets: Set<BoardCoordinate> = emptySet()
    private var memoryTapped: MutableSet<BoardCoordinate> = mutableSetOf()
    private var memoryPreviewActive: Boolean = false
    private var memoryStatusText: String = ""
    private var memoryGameOver: Boolean = false

    private val _snapshot = MutableStateFlow(GameSnapshot(boardState = initialBoard()))
    val snapshot: StateFlow<GameSnapshot> = _snapshot.asStateFlow()

    init {
        if (gameType == GameType.Memory) {
            startMemoryRound()
        }
    }

    fun onCellTap(row: Int, col: Int) {
        Log.d("GameViewModel", "onCellTap: $row, $col, gametype: $gameType")
        if (gameType == GameType.Memory) {
            onMemoryTap(row, col)
            return
        }
        val current = _snapshot.value
        val existing = current.boardState.cellAt(row, col).value
        val nextValue = engine.nextValue(current.boardState, row, col, existing)
        if (!engine.isMoveValid(current.boardState, row, col, nextValue)) {
            _snapshot.value = current.copy(gameResult = GameResult.InvalidMove("Invalid move"))
            return
        }

        moveHistory.addLast(current.boardState)
        val updatedBoard = engine.applyMove(current.boardState, row, col, nextValue)
        val nextMoveCount = current.moveCount + 1
        val nextResult = if (engine.isSolved(updatedBoard)) {
            GameResult.Solved(moveCount = nextMoveCount)
        } else {
            GameResult.InProgress
        }
        _snapshot.value = GameSnapshot(
            boardState = updatedBoard,
            moveCount = nextMoveCount,
            gameResult = nextResult
        )
    }

    private fun onMemoryTap(row: Int, col: Int) {
        val mem = memoryEngine ?: return
        val current = _snapshot.value
        if (memoryPreviewActive || current.gameResult is GameResult.Solved) {
            _snapshot.value = current.copy(gameResult = GameResult.InvalidMove("Wait for preview to end"))
            return
        }
        if (memoryGameOver) return
        if (row !in 0 until current.boardState.rows || col !in 0 until current.boardState.cols) {
            _snapshot.value = current.copy(gameResult = GameResult.InvalidMove("Invalid cell"))
            return
        }

        val coord = BoardCoordinate(row, col)
        val isTarget = coord in memoryTargets
        if (!isTarget) {
            memoryHearts -= 1
            if (memoryHearts <= 0) {
                memoryGameOver = true
                memoryPreviewActive = false
                memoryStatusText = "Game over. You ran out of hearts."
                _snapshot.value = current.copy(
                    gameResult = GameResult.InvalidMove("Game over"),
                    moveCount = current.moveCount + 1
                )
                return
            }
            memoryTapped.clear()
            memoryStatusText = "Missed. Hearts left: $memoryHearts. Retry round $memoryRound."
            startMemoryRound(reuseRound = true)
            return
        }

        memoryTapped.add(coord)
        val updatedBoard = mem.createRoundBoard(
            round = memoryRound,
            targets = memoryTargets,
            revealed = false,
            tapped = memoryTapped
        )
        val nextMoveCount = current.moveCount + 1

        if (memoryTapped.size == memoryTargets.size) {
            if (memoryRound >= mem.maxRound) {
                memoryGameOver = false
                memoryStatusText = "You won all ${mem.maxRound} rounds."
                _snapshot.value = current.copy(
                    boardState = updatedBoard,
                    moveCount = nextMoveCount,
                    gameResult = GameResult.Solved(moveCount = nextMoveCount)
                )
                return
            }
            memoryRound += 1
            memoryTapped.clear()
            memoryStatusText = "Round cleared. Starting round $memoryRound."
            _snapshot.value = current.copy(boardState = updatedBoard, moveCount = nextMoveCount)
            startMemoryRound()
            return
        }

        _snapshot.value = current.copy(
            boardState = updatedBoard,
            moveCount = nextMoveCount,
            gameResult = GameResult.InProgress
        )
    }

    fun onMemoryPreviewFinished() {
        if (gameType != GameType.Memory || !memoryPreviewActive) return
        val mem = memoryEngine ?: return
        memoryPreviewActive = false
        val current = _snapshot.value
        _snapshot.value = current.copy(
            boardState = mem.createRoundBoard(
                round = memoryRound,
                targets = memoryTargets,
                revealed = false,
                tapped = memoryTapped
            ),
            gameResult = GameResult.InProgress
        )
    }

    private fun startMemoryRound(reuseRound: Boolean = false) {
        val mem = memoryEngine ?: return
        if (!reuseRound) {
            memoryTargets = mem.generateTargets(memoryRound)
        }
        memoryPreviewActive = true
        val board = mem.createRoundBoard(
            round = memoryRound,
            targets = memoryTargets,
            revealed = true,
            tapped = emptySet()
        )
        val current = _snapshot.value
        _snapshot.value = current.copy(boardState = board, gameResult = GameResult.InProgress)
    }

    fun onReset() {
        moveHistory.clear()
        if (gameType == GameType.Memory) {
            memoryRound = 1
            memoryHearts = memoryEngine?.initialHearts ?: 3
            memoryTapped.clear()
            memoryGameOver = false
            memoryStatusText = "Round 1"
            _snapshot.value = GameSnapshot(boardState = initialBoard())
            startMemoryRound()
            return
        }
        _snapshot.value = GameSnapshot(boardState = initialBoard())
    }

    fun onUndo() {
        val previous = moveHistory.removeLastOrNull() ?: return
        val current = _snapshot.value
        _snapshot.value = current.copy(
            boardState = previous,
            moveCount = (current.moveCount - 1).coerceAtLeast(0),
            gameResult = GameResult.InProgress
        )
    }

    fun getHintMessage(): String {
        if (gameType == GameType.Memory) {
            val previewLabel = if (memoryPreviewActive) "Preview" else "Answer"
            return "Round $memoryRound/${memoryEngine?.maxRound ?: 10} | Hearts: $memoryHearts | Phase: $previewLabel"
        }
        return engine.getHint(_snapshot.value.boardState)?.message ?: "No hint available"
    }

    fun getMemoryRound(): Int = memoryRound
    fun getMemoryHearts(): Int = memoryHearts
    fun isMemoryPreviewActive(): Boolean = memoryPreviewActive
    fun getMemoryStatusText(): String = memoryStatusText
    fun getMemoryPreviewMillis(): Long = memoryEngine?.previewMillis ?: 2_000L
    fun isGameFinished(): Boolean {
        return when {
            _snapshot.value.gameResult is GameResult.Solved -> true
            gameType == GameType.Memory && memoryGameOver -> true
            else -> false
        }
    }
    fun onPlayAgain() = onReset()

    private fun initialBoard(): BoardState {
        if (gameType == GameType.Memory) {
            return BoardState.empty(rows = 4, cols = 4)
        }
        return puzzleGenerator.createInitialState(gameType = gameType, difficulty = difficulty)
    }
}
