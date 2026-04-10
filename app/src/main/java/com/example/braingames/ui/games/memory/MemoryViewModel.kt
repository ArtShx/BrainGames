package com.example.braingames.ui.games.memory

import android.util.Log
import androidx.lifecycle.ViewModel
import com.example.braingames.core.BoardState
import com.example.braingames.core.GameSnapshot
import com.example.braingames.core.GameType
import com.example.braingames.games.factory.GameFactory
import com.example.braingames.games.memory.MemoryEngine
import com.example.braingames.games.memory.MemoryGameController
import com.example.braingames.ui.BaseGameViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class MemoryViewModel: BaseGameViewModel() {
    override val engine = GameFactory.engineFor(GameType.Memory)
    override val controller = MemoryGameController(engine as MemoryEngine)
    override val _snapshot = MutableStateFlow(GameSnapshot(boardState = controller.initialBoard()))
    private val moveHistory = ArrayDeque<BoardState>()

    override val snapshot: StateFlow<GameSnapshot> = _snapshot.asStateFlow()

    init {
        onReset()
    }

    //override val snapshot: StateFlow<GameSnapshot> = controller.reset()

//    fun onCellTap(row: Int, col: Int): MutableStateFlow<GameSnapshot> {
//        //val current = _snapshot.value
//        //moveHistory.addLast(current.boardState)
//        _snapshot.value = controller.onCellTap(_snapshot.value, row, col)
//        return _snapshot
//    }

    override fun onReset() {
        moveHistory.clear()
        _snapshot.value = controller.reset()
         return
    }

    fun onMemoryPreviewFinished() {
        Log.d("GameViewModel", "onMemoryPreviewFinished")
        _snapshot.value = controller.onPreviewFinished(_snapshot.value)
    }

    fun getMemoryRound(): Int = controller.getRound()
    fun getMemoryHearts(): Int = controller.getHearts()
    fun isMemoryPreviewActive(): Boolean = controller.isPreviewActive()
    fun getMemoryStatusText(): String = controller.getStatusText()
    fun getMemoryPreviewMillis(): Long = controller.getPreviewMillis()
}