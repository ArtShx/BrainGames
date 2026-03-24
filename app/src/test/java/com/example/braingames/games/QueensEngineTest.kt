package com.example.braingames.games

import com.example.braingames.core.BoardState
import com.example.braingames.core.CellState
import com.example.braingames.games.queens.QueensEngine
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class QueensEngineTest {
    private val engine = QueensEngine()

    @Test
    fun rejects_same_row_queen_conflict() {
        val board = BoardState(
            rows = 4,
            cols = 4,
            cells = listOf(
                listOf(CellState("Q"), CellState(), CellState(), CellState()),
                listOf(CellState(), CellState(), CellState(), CellState()),
                listOf(CellState(), CellState(), CellState(), CellState()),
                listOf(CellState(), CellState(), CellState(), CellState())
            )
        )
        assertFalse(engine.isMoveValid(board, 0, 2, "Q"))
    }

    @Test
    fun accepts_non_conflicting_queen() {
        val board = BoardState.empty(4, 4)
        assertTrue(engine.isMoveValid(board, 1, 3, "Q"))
    }
}
