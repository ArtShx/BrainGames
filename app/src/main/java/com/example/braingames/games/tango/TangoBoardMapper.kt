package com.example.braingames.games.tango

import com.example.braingames.core.CellState

object TangoBoardMapper {
    fun mapCellDisplay(cell: CellState): String = cell.value ?: "-"
}
