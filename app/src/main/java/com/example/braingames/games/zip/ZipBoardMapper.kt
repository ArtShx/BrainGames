package com.example.braingames.games.zip

import com.example.braingames.core.CellState

object ZipBoardMapper {
    fun mapCellDisplay(cell: CellState): String = cell.value ?: " "
}
