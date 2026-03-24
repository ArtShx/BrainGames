package com.example.braingames.games.memory

import com.example.braingames.core.CellState

object MemoryBoardMapper {
    fun mapCellDisplay(cell: CellState): String = if (cell.value == "●") "●" else "○"
}
