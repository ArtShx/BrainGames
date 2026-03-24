package com.example.braingames.ui

import androidx.lifecycle.ViewModel
import com.example.braingames.core.BoardState
import com.example.braingames.core.DefaultPuzzleGenerator
import com.example.braingames.core.Difficulty
import com.example.braingames.core.GameResult
import com.example.braingames.core.GameSnapshot
import com.example.braingames.core.GameType
import com.example.braingames.games.GameFactory
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class GameViewModel(
    private val gameType: GameType,
    private val difficulty: Difficulty = Difficulty.Easy
) : ViewModel() {

    private val engine = GameFactory.engineFor(gameType)
    private val puzzleGenerator = DefaultPuzzleGenerator()
    private val moveHistory = ArrayDeque<BoardState>()

    private val _snapshot = MutableStateFlow(GameSnapshot(boardState = initialBoard()))
    val snapshot: StateFlow<GameSnapshot> = _snapshot.asStateFlow()

    fun onCellTap(row: Int, col: Int) {
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

    fun onReset() {
        moveHistory.clear()
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
        return engine.getHint(_snapshot.value.boardState)?.message ?: "No hint available"
    }

    private fun initialBoard(): BoardState {
        return puzzleGenerator.createInitialState(gameType = gameType, difficulty = difficulty)
    }
}
