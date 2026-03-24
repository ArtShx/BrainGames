package com.example.braingames.games

import com.example.braingames.core.BoardState
import com.example.braingames.games.zip.ZipEngine
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ZipEngineTest {
    private val engine = ZipEngine()

    @Test
    fun accepts_only_dot_or_empty() {
        val board = BoardState.empty(2, 2)
        assertTrue(engine.isMoveValid(board, 0, 0, "•"))
        assertTrue(engine.isMoveValid(board, 0, 0, null))
        assertFalse(engine.isMoveValid(board, 0, 0, "X"))
    }
}
