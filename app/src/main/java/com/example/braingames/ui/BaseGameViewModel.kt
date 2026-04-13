package com.example.braingames.ui

import android.content.Context
import androidx.compose.foundation.text2.input.insert
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.braingames.core.GameResult
import com.example.braingames.core.GameRuleEngine
import com.example.braingames.core.GameSnapshot
import com.example.braingames.database.AppDatabase
import com.example.braingames.database.entity.HighScore
import com.example.braingames.games.interfaces.GameController
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

abstract class BaseGameViewModel : ViewModel() {
    // Shared state for all games
    protected abstract val _snapshot: MutableStateFlow<GameSnapshot>

    // Expose as read-only StateFlow for the UI
    abstract val snapshot: StateFlow<GameSnapshot>

    protected abstract val engine: GameRuleEngine
    protected abstract  val controller: GameController
    protected abstract val context: Context

    // Timer
    private  val _elapsedTime = MutableStateFlow(0L)
    val elapsedTime: StateFlow<Long> = _elapsedTime.asStateFlow()
    private var timerJob: Job? = null
    private val _isRunning = MutableStateFlow(false)
    val isRunning: StateFlow<Boolean> = _isRunning.asStateFlow()

    open fun onCellTap(row: Int, col: Int): MutableStateFlow<GameSnapshot> {
        _snapshot.value = controller.onCellTap(_snapshot.value, row, col)
        return _snapshot
    }
    open fun onReset() {
        _snapshot.value = controller.reset()
        resetTimer()
        startTimer()
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

    fun onGameFinished() {
        viewModelScope.launch(Dispatchers.IO) {
            var db = AppDatabase.getDatabase(context)
            db.highScoreDao().insertScore(controller.getHighScore())
        }
    }

    fun startTimer() {
        if (_isRunning.value) return

        _isRunning.value = true
        timerJob = viewModelScope.launch {
            while (isActive) {
                delay(1000)
                _elapsedTime.value += 1
            }
        }
    }

    fun pauseTimer() {
        _isRunning.value = false
        timerJob?.cancel()
    }

    fun resetTimer() {
        pauseTimer()
        _elapsedTime.value = 0
    }

    // Helper to format seconds into 00:00:00
    fun formatTime(seconds: Long): String {
        val h = seconds / 3600
        val m = (seconds % 3600) / 60
        val s = seconds % 60
        return "%02d:%02d:%02d".format(h, m, s)
    }

    fun getElapsedTime(): String {
        return formatTime(elapsedTime.value)
    }

}