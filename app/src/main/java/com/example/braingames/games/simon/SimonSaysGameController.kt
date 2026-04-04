package com.example.braingames.games.simon

import android.util.Log
import com.example.braingames.core.BoardCoordinate
import com.example.braingames.core.BoardState
import com.example.braingames.core.Difficulty
import com.example.braingames.core.GameResult
import com.example.braingames.core.GameSnapshot
import com.example.braingames.database.AppDatabase
import com.example.braingames.games.interfaces.GameController
import com.example.braingames.games.memory.MemoryEngine
import kotlin.random.Random
//class MemoryGameController (override val memoryEngine: MemoryEngine): GameController {

class SimonSaysGameController(
    override val engine: SimonSaysEngine,

): GameController {
    private val sequence = mutableListOf<BoardCoordinate>()
    private var inputIndex: Int = 0
    private var hearts: Int = engine.initialHearts
    private var playbackActive: Boolean = false
    private var gameOver: Boolean = false
    private var playbackEpoch: Int = 0
    private var statusText: String = ""
    private var isProcessingTap = false
    private var isSolved = false
    private val difficulty: Difficulty = Difficulty.Easy
    private val random: Random = Random.Default


    fun initialBoard(): BoardState =
        BoardState.empty(rows = engine.gridSide(difficulty), cols = engine.gridSide(difficulty))

    fun reset(): GameSnapshot {
        Log.d("SimonSaysGameController", "reset")
        sequence.clear()
        hearts = engine.initialHearts
        inputIndex = 0
        gameOver = false
        appendRandomStep(allowAnyCoordinate = true)
        playbackActive = true
        playbackEpoch++
        statusText = "Watch the pattern"
        return GameSnapshot(
            boardState = engine.createBoard(
                difficulty = difficulty,
                sequence = sequence,
                playbackHighlight = null,
                inputProgress = 0
            ),
            gameResult = GameResult.InProgress
        )
    }

    fun onPlaybackTick(current: GameSnapshot, stepIndex: Int?): GameSnapshot {
        Log.d("SimonSaysGameController", "onPlaybackTick: $stepIndex")
        if (!playbackActive) return current
        val highlight = stepIndex?.let { sequence.getOrNull(it);  }

        return current.copy(
            boardState = engine.createBoard(
                difficulty = difficulty,
                sequence = sequence,
                playbackHighlight = highlight,
                inputProgress = 0
            ),
            gameResult = GameResult.InProgress
        )
    }

    fun onPlaybackFinished(current: GameSnapshot): GameSnapshot {
        Log.d("SimonSaysGameController", "onPlaybackFinished")
        if (!playbackActive) return current
        playbackActive = false
        inputIndex = 0
        statusText = "Your turn"
        isProcessingTap = false
        return current.copy(
            boardState = engine.createBoard(
                difficulty = difficulty,
                sequence = sequence,
                playbackHighlight = null,
                inputProgress = 0
            ),
            gameResult = GameResult.InProgress
        )
    }

    fun onCellTap(current: GameSnapshot, row: Int, col: Int): GameSnapshot {
        Log.d("SimonSaysGameController", "onCellTap: $row, $col .. $isProcessingTap")
        if (playbackActive)
            return current.copy(gameResult = GameResult.InvalidMove("Watch the pattern"), earlyReturn = true)


        if (gameOver) return current
        if (current.gameResult is GameResult.Solved)
            return current.copy(gameResult = GameResult.InvalidMove("Already solved"), isMistake = true, earlyReturn = true)

        val side = engine.gridSide(difficulty)
        if (row !in 0 until side || col !in 0 until side) {
            return current.copy(gameResult = GameResult.InvalidMove("Invalid cell"), earlyReturn = true)
        }
        if (sequence.isEmpty()) return current.copy(earlyReturn = true)
        if (isProcessingTap) return current.copy(earlyReturn = true)
        isProcessingTap = true

        val coord = BoardCoordinate(row, col)
        val expected = sequence[inputIndex]
        val nextMoveCount = current.moveCount + 1

        if (coord != expected) {
            hearts -= 1
            if (hearts <= 0) {
                gameOver = true
                playbackActive = false
                statusText = "Game over. You ran out of hearts."
                isProcessingTap = false
                return current.copy(
                    gameResult = GameResult.InvalidMove("Game over"),
                    moveCount = nextMoveCount,
                    isMistake = true
                )
            }
            inputIndex = 0
            playbackActive = true
            playbackEpoch++
            statusText = "Missed. Hearts left: $hearts. Watch again."
            isProcessingTap = false

            return current.copy(
                boardState = engine.createBoard(
                    difficulty = difficulty,
                    sequence = sequence,
                    playbackHighlight = null,
                    inputProgress = 0
                ),
                moveCount = nextMoveCount,
                gameResult = GameResult.InProgress,
                isMistake = true
            )
        }

        inputIndex++
        if (inputIndex == sequence.size) {
            if (sequence.size >= engine.maxRound) {
                gameOver = false
                statusText = "You reached length ${engine.maxRound}!"
                isProcessingTap = false
                isSolved = true

                return current.copy(
                    boardState = engine.createBoard(
                        difficulty = difficulty,
                        sequence = sequence,
                        playbackHighlight = null,
                        inputProgress = sequence.size
                    ),
                    moveCount = nextMoveCount,
                    gameResult = GameResult.Solved(moveCount = nextMoveCount),
                    isMistake = false
                )
            }
            appendRandomStep(allowAnyCoordinate = false)
            inputIndex = 0
            playbackActive = true
            playbackEpoch++
            statusText = "Nice! Watch the next pattern."
            isProcessingTap = false
            return current.copy(
                moveCount = nextMoveCount,
                gameResult = GameResult.InProgress,
                isMistake = false
            )
        }

        isProcessingTap = false
        return current.copy(
            moveCount = nextMoveCount,
            gameResult = GameResult.InProgress,
            isMistake = null
        )
    }

    fun onUserTapHighlight(current: GameSnapshot, row: Int, col: Int): GameSnapshot {
        // highlight only the selected cell
        return GameSnapshot(
            boardState = engine.createBoard(
                difficulty = difficulty,
                sequence = sequence,
                playbackHighlight = BoardCoordinate(row, col),
                inputProgress = 0
            ),
            gameResult = GameResult.InProgress
        )
    }

    fun getSequence(): List<BoardCoordinate> = sequence.toList()
    fun getRound(): Int = sequence.size
    fun getHearts(): Int = hearts
    fun isPlaybackActive(): Boolean = playbackActive
    fun getStatusText(): String = statusText
    fun getPlaybackEpoch(): Int = playbackEpoch
    fun isGameOver(): Boolean = gameOver
    fun getStepHighlightMillis(): Long = engine.stepHighlightMillis
    fun getStepGapMillis(): Long = engine.stepGapMillis
    fun getMaxRound(): Int = engine.maxRound
    fun isSolved(): Boolean = isSolved

    fun getHintMessage(): String {
        val phase = if (playbackActive) "Playback" else "Your turn"
        return "Length ${sequence.size}/${engine.maxRound} | Hearts: $hearts | Phase: $phase"
    }

    private fun appendRandomStep(allowAnyCoordinate: Boolean) {
        val side = engine.gridSide(difficulty)
        val last = sequence.lastOrNull()
        repeat(64) {
            val next = BoardCoordinate(random.nextInt(side), random.nextInt(side))
            if (allowAnyCoordinate || last == null || next != last) {
                sequence.add(next)
                return
            }
        }
        val next = if (side > 1) {
            BoardCoordinate(
                row = if (last?.row == 0) 1 else 0,
                col = last?.col ?: 0
            )
        } else {
            BoardCoordinate(0, 0)
        }
        sequence.add(next)
    }
}
