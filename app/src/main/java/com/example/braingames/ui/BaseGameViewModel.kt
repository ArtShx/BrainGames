package com.example.braingames.ui

import androidx.lifecycle.ViewModel
import com.example.braingames.core.GameResult
import com.example.braingames.core.GameRuleEngine
import com.example.braingames.core.GameSnapshot
import com.example.braingames.games.interfaces.GameController
import com.example.braingames.games.memory.MemoryEngine
import com.example.braingames.games.memory.MemoryGameController
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

abstract class BaseGameViewModel : ViewModel() {
    // Shared state for all games
    //protected val _snapshot = MutableStateFlow(GameSnapshot())
    protected abstract val _snapshot: MutableStateFlow<GameSnapshot>

    // Expose as read-only StateFlow for the UI
    //val snapshot: StateFlow<GameSnapshot> by lazy { _snapshot.asStateFlow() }
    abstract val snapshot: StateFlow<GameSnapshot>

    protected abstract val engine: GameRuleEngine
    protected abstract  val controller: GameController

    open fun onCellTap(row: Int, col: Int): MutableStateFlow<GameSnapshot> {
        _snapshot.value = controller.onCellTap(_snapshot.value, row, col)
        return _snapshot
    }
    open fun onReset() {
        _snapshot.value = controller.reset()
        return
    }
    fun onPlayAgain() { onReset() }
    fun isGameFinished(): Boolean {
        return when {
            _snapshot.value.gameResult is GameResult.Solved -> true
            controller.isGameOver() == true -> true
            else -> false
        }
    }

}