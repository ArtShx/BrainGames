package com.example.braingames.ui

import androidx.lifecycle.ViewModel
import com.example.braingames.core.Difficulty
import com.example.braingames.core.GameType
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

data class HomeState(
    val selectedDifficulty: Difficulty = Difficulty.Easy,
    val lastPlayedGame: GameType? = null
)

class HomeViewModel : ViewModel() {
    private val _state = MutableStateFlow(HomeState())
    val state: StateFlow<HomeState> = _state.asStateFlow()

    fun setDifficulty(difficulty: Difficulty) {
        _state.value = _state.value.copy(selectedDifficulty = difficulty)
    }

    fun markPlayed(gameType: GameType) {
        _state.value = _state.value.copy(lastPlayedGame = gameType)
    }
}
