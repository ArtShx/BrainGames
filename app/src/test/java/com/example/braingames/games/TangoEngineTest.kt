package com.example.braingames.games

import com.example.braingames.core.BoardState
import com.example.braingames.games.tango.TangoEngine
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class TangoEngineTest {
    private val engine = TangoEngine()

    @Test
    fun rejects_third_same_symbol_in_row() {
        val board = BoardState.empty(3, 3)
            .let { engine.applyMove(it, 0, 0, "X") }
            .let { engine.applyMove(it, 0, 1, "X") }
        assertFalse(engine.isMoveValid(board, 0, 2, "X"))
    }

    @Test
    fun allows_alternating_symbol() {
        val board = BoardState.empty(3, 3)
            .let { engine.applyMove(it, 0, 0, "X") }
            .let { engine.applyMove(it, 0, 1, "X") }
        assertTrue(engine.isMoveValid(board, 0, 2, "O"))
    }
}
