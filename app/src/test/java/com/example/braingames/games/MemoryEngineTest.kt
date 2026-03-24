package com.example.braingames.games

import com.example.braingames.core.BoardState
import com.example.braingames.games.memory.MemoryEngine
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class MemoryEngineTest {
    private val engine = MemoryEngine()

    @Test
    fun move_marks_cell() {
        val board = BoardState.empty(4, 4)
        val updated = engine.applyMove(board, 0, 0, "●")
        assertTrue(updated.cellAt(0, 0).value == "●")
    }

    @Test
    fun solved_when_all_revealed() {
        val board = BoardState(
            rows = 2,
            cols = 2,
            cells = listOf(
                listOf(
                    com.example.braingames.core.CellState(value = "●"),
                    com.example.braingames.core.CellState(value = "●")
                ),
                listOf(
                    com.example.braingames.core.CellState(value = "●"),
                    com.example.braingames.core.CellState(value = "●")
                )
            )
        )
        assertTrue(engine.isSolved(board))
        assertFalse(engine.isSolved(BoardState.empty(2, 2)))
    }
}
