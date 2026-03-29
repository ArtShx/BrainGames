package com.example.braingames.ui

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.braingames.core.BoardState
import com.example.braingames.core.DefaultPuzzleGenerator
import com.example.braingames.core.Difficulty
import com.example.braingames.core.GameResult
import com.example.braingames.core.GameSnapshot
import com.example.braingames.core.GameType
import com.example.braingames.games.GameFactory
import com.example.braingames.games.memory.MemoryGameController
import com.example.braingames.games.memory.MemoryEngine
import com.example.braingames.games.simon.SimonSaysEngine
import com.example.braingames.games.simon.SimonSaysGameController
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class GameViewModel(
    private val gameType: GameType,
    private val difficulty: Difficulty = Difficulty.Easy
) : ViewModel() {

    private val engine = GameFactory.engineFor(gameType)
    private val memoryController = (engine as? MemoryEngine)?.let { MemoryGameController(it) }
    private val simonSaysController =
        (engine as? SimonSaysEngine)?.let { SimonSaysGameController(it, difficulty) }
    private val puzzleGenerator = DefaultPuzzleGenerator()
    private val moveHistory = ArrayDeque<BoardState>()

    private val _snapshot = MutableStateFlow(GameSnapshot(boardState = initialBoard()))
    val snapshot: StateFlow<GameSnapshot> = _snapshot.asStateFlow()

    init {
        memoryController?.let { _snapshot.value = it.reset() }
        simonSaysController?.let { _snapshot.value = it.reset() }
        Log.d("GameViewModel", "init $simonSaysController ... $memoryController")
    }

    fun onCellTap(row: Int, col: Int) {
        Log.d("GameViewModel", "onCellTap: $row, $col, gametype: $gameType")

        memoryController?.let { controller ->
            _snapshot.value = controller.onCellTap(_snapshot.value, row, col)
            return
        }
        simonSaysController?.let { controller ->
            val resultSnapshot = controller.onCellTap(_snapshot.value, row, col)
            // display cell tapped
            viewModelScope.launch {
                _snapshot.value = simonSaysController.onUserTapHighlight(_snapshot.value, row, col)
                delay(500)
                _snapshot.value = resultSnapshot
            }
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

    fun onMemoryPreviewFinished() {
        Log.d("GameViewModel", "onMemoryPreviewFinished")
        val controller = memoryController ?: return
        _snapshot.value = controller.onPreviewFinished(_snapshot.value)
    }

    fun onSimonPlaybackTick(stepIndex: Int?) {
        Log.d("GameViewModel", "onSimonPlaybackTick: $stepIndex")
        val controller = simonSaysController ?: return
        _snapshot.value = controller.onPlaybackTick(_snapshot.value, stepIndex)
    }

    fun onSimonPlaybackFinished() {
        Log.d("GameViewModel", "onSimonPlaybackFinished")
        val controller = simonSaysController ?: return
        Log.d("GameViewModel", "onSimonPlaybackFinished2")

        _snapshot.value = controller.onPlaybackFinished(_snapshot.value)
    }

    fun onReset() {
        moveHistory.clear()
        memoryController?.let {
            _snapshot.value = it.reset()
            return
        }
        simonSaysController?.let {
            _snapshot.value = it.reset()
            return
        }
        _snapshot.value = GameSnapshot(boardState = initialBoard())
    }

//    fun onUndo() {
//        val previous = moveHistory.removeLastOrNull() ?: return
//        val current = _snapshot.value
//        _snapshot.value = current.copy(
//            boardState = previous,
//            moveCount = (current.moveCount - 1).coerceAtLeast(0),
//            gameResult = GameResult.InProgress
//        )
//    }
//
//    fun getHintMessage(): String {
//        memoryController?.let { return it.getHintMessage() }
//        simonSaysController?.let { return it.getHintMessage() }
//        return engine.getHint(_snapshot.value.boardState)?.message ?: "No hint available"
//    }

    fun getMemoryRound(): Int = memoryController?.getRound() ?: 0
    fun getMemoryHearts(): Int = memoryController?.getHearts() ?: 0
    fun isMemoryPreviewActive(): Boolean = memoryController?.isPreviewActive() ?: false
    fun getMemoryStatusText(): String = memoryController?.getStatusText() ?: ""
    fun getMemoryPreviewMillis(): Long = memoryController?.getPreviewMillis() ?: 2_000L
    fun isGameFinished(): Boolean {
        return when {
            _snapshot.value.gameResult is GameResult.Solved -> true
            memoryController?.isGameOver() == true -> true
            simonSaysController?.isGameOver() == true -> true
            else -> false
        }
    }

    fun getSimonSequence() = simonSaysController?.getSequence().orEmpty()
    fun getSimonRound() = simonSaysController?.getRound() ?: 0
    fun getSimonMaxRound() = simonSaysController?.getMaxRound() ?: 10
    fun getSimonHearts() = simonSaysController?.getHearts() ?: 0
    fun isSimonPlaybackActive() = simonSaysController?.isPlaybackActive() ?: false
    fun getSimonStatusText() = simonSaysController?.getStatusText().orEmpty()
    fun getSimonPlaybackEpoch() = simonSaysController?.getPlaybackEpoch() ?: 0
    fun getSimonStepHighlightMillis() = simonSaysController?.getStepHighlightMillis() ?: 600L
    fun getSimonStepGapMillis() = simonSaysController?.getStepGapMillis() ?: 200L
    fun onPlayAgain() = onReset()

    private fun initialBoard(): BoardState {
        return memoryController?.initialBoard()
            ?: simonSaysController?.initialBoard()
            ?: puzzleGenerator.createInitialState(gameType = gameType, difficulty = difficulty)
    }
}
