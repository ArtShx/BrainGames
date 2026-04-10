package com.example.braingames.ui.games.simonsays


import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.braingames.core.BoardState
import com.example.braingames.core.GameResult
import com.example.braingames.core.GameSnapshot
import com.example.braingames.core.GameType
import com.example.braingames.games.factory.GameFactory
import com.example.braingames.games.simon.SimonSaysEngine
import com.example.braingames.games.simon.SimonSaysGameController
import com.example.braingames.ui.BaseGameViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class SimonSaysViewModel: BaseGameViewModel() {
    override val engine = GameFactory.engineFor(GameType.SimonSays)
    override val controller = SimonSaysGameController(engine as SimonSaysEngine)
    override val _snapshot = MutableStateFlow(GameSnapshot(boardState = controller.initialBoard()))
    private val moveHistory = ArrayDeque<BoardState>()
    override val snapshot: StateFlow<GameSnapshot> = _snapshot.asStateFlow()

    init {
        controller.reset()
    }


    //val snapshot = controller.reset()

    override fun onCellTap(row: Int, col: Int): MutableStateFlow<GameSnapshot> {
        val resultSnapshot = controller.onCellTap(_snapshot.value, row, col)
        // display cell tapped
        viewModelScope.launch {
            _snapshot.value = controller.onUserTapHighlight(_snapshot.value, row, col)
            delay(420)
            _snapshot.value = resultSnapshot
        }
        return _snapshot
    }

    override fun onReset() {
        moveHistory.clear()
        _snapshot.value = controller.reset()
        return
    }

    fun onSimonPlaybackTick(stepIndex: Int?) {
        Log.d("GameViewModel", "onSimonPlaybackTick: $stepIndex")
        _snapshot.value = controller.onPlaybackTick(_snapshot.value, stepIndex)
    }

    fun onSimonPlaybackFinished() {
        Log.d("GameViewModel", "onSimonPlaybackFinished")
        _snapshot.value = controller.onPlaybackFinished(_snapshot.value)
    }

    fun getSimonSequence() = controller.getSequence()
    fun getSimonRound() = controller.getRound()
    fun getSimonMaxRound() = controller.getMaxRound()
    fun getSimonHearts() = controller.getHearts()
    fun isSimonPlaybackActive() = controller.isPlaybackActive()
    fun getSimonStatusText() = controller.getStatusText()
    fun getSimonPlaybackEpoch() = controller.getPlaybackEpoch()
    fun getSimonStepHighlightMillis() = controller.getStepHighlightMillis()
    fun getSimonStepGapMillis() = controller.getStepGapMillis()
}