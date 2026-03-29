package com.example.braingames.games.simon

import com.example.braingames.core.BoardCoordinate
import com.example.braingames.core.BoardState
import com.example.braingames.core.Difficulty
import com.example.braingames.core.GameResult
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import kotlin.random.Random

class SimonSaysGameControllerTest {

    private fun wrongCell(correct: BoardCoordinate, side: Int): BoardCoordinate {
        for (r in 0 until side) {
            for (c in 0 until side) {
                if (r != correct.row || c != correct.col) return BoardCoordinate(r, c)
            }
        }
        return BoardCoordinate(0, 0)
    }

    private fun dotCount(board: BoardState): Int =
        board.cells.flatten().count { it.value == "●" }

    @Test
    fun correct_full_sequence_advances_length_and_starts_playback() {
        val c = SimonSaysGameController(SimonSaysEngine(), Difficulty.Easy, Random(123L))
        var snap = c.reset()
        assertEquals(1, c.getRound())
        assertTrue(c.isPlaybackActive())
        snap = c.onPlaybackFinished(snap)
        assertFalse(c.isPlaybackActive())

        val step = c.getSequence()[0]
        snap = c.onCellTap(snap, step.row, step.col)
        assertEquals(2, c.getRound())
        assertTrue(c.isPlaybackActive())
        assertTrue(snap.gameResult is GameResult.InProgress)
    }

    @Test
    fun wrong_tap_costs_heart_and_restarts_playback_same_length() {
        val c = SimonSaysGameController(SimonSaysEngine(), Difficulty.Easy, Random(123L))
        var snap = c.reset()
        snap = c.onPlaybackFinished(snap)
        val lenBefore = c.getRound()
        val heartsBefore = c.getHearts()
        val bad = wrongCell(c.getSequence()[0], side = 3)
        snap = c.onCellTap(snap, bad.row, bad.col)

        assertEquals(heartsBefore - 1, c.getHearts())
        assertEquals(lenBefore, c.getRound())
        assertTrue(c.isPlaybackActive())
        assertTrue(snap.gameResult is GameResult.InProgress)
    }

    @Test
    fun out_of_hearts_sets_game_over() {
        val c = SimonSaysGameController(SimonSaysEngine(), Difficulty.Easy, Random(123L))
        var snap = c.reset()
        snap = c.onPlaybackFinished(snap)
        val side = 3
        val bad = wrongCell(c.getSequence()[0], side)
        repeat(3) {
            snap = c.onCellTap(snap, bad.row, bad.col)
            if (c.getHearts() > 0) {
                snap = c.onPlaybackFinished(snap)
            }
        }
        assertTrue(c.isGameOver())
        assertTrue(snap.gameResult is GameResult.InvalidMove)
    }

    @Test
    fun autoplay_reaches_solved_at_max_length() {
        val engine = SimonSaysEngine()
        val c = SimonSaysGameController(engine, Difficulty.Easy, Random(99L))
        var snap = c.reset()
        var steps = 0
        while (snap.gameResult !is GameResult.Solved && !c.isGameOver() && steps < 3000) {
            steps++
            if (c.isPlaybackActive()) {
                snap = c.onPlaybackFinished(snap)
                continue
            }
            val seq = c.getSequence()
            val idx = dotCount(snap.boardState)
            assertTrue(idx < seq.size)
            snap = c.onCellTap(snap, seq[idx].row, seq[idx].col)
        }
        assertTrue(snap.gameResult is GameResult.Solved)
        assertEquals(engine.maxRound, c.getRound())
    }

    @Test
    fun playback_tick_highlights_without_advancing_input() {
        val c = SimonSaysGameController(SimonSaysEngine(), Difficulty.Easy, Random(7L))
        var snap = c.reset()
        assertTrue(c.isPlaybackActive())
        snap = c.onPlaybackTick(snap, 0)
        val seq = c.getSequence()
        val highlighted = snap.boardState.cells.flatten().count { it.isHighlighted }
        assertEquals(1, highlighted)
        assertTrue(snap.boardState.cellAt(seq[0].row, seq[0].col).isHighlighted)
    }

    @Test
    fun playback_finished_allows_input() {
        val c = SimonSaysGameController(SimonSaysEngine(), Difficulty.Easy, Random(7L))
        var snap = c.reset()
        snap = c.onPlaybackFinished(snap)
        assertFalse(c.isPlaybackActive())
        val cell = c.getSequence()[0]
        snap = c.onCellTap(snap, cell.row, cell.col)
        assertTrue(snap.gameResult is GameResult.InProgress)
    }
}
