package com.example.braingames.games.queens

import com.example.braingames.core.CellState

object QueensBoardMapper {
    fun mapCellDisplay(cell: CellState): String = cell.value ?: "."
}
