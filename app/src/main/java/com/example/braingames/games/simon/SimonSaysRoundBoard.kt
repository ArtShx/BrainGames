package com.example.braingames.games.simon

import android.content.Context
import android.media.AudioAttributes
import android.media.AudioManager
import android.media.SoundPool
import android.util.Log
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewModelScope
import com.example.braingames.R
import com.example.braingames.core.BoardCoordinate
import com.example.braingames.core.BoardState
import com.example.braingames.core.GameSnapshot
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue

@Composable
fun SimonSaysRoundBoard(
    boardState: BoardState,
    sequence: List<BoardCoordinate>,
    targetLength: Int,
    maxLength: Int,
    hearts: Int,
    isPlaybackPhase: Boolean,
    playbackEpoch: Int,
    highlightStepMillis: Long,
    gapMillis: Long,
    statusText: String,
    onCellTap: (Int, Int) -> MutableStateFlow<GameSnapshot>,
    onPlaybackTick: (Int?) -> Unit,
    onPlaybackFinished: () -> Unit,
    modifier: Modifier = Modifier
) {
    val phaseState = rememberUpdatedState(isPlaybackPhase)
    val scope = rememberCoroutineScope() // Get a scope tied to this Composable's lifecycle
    var cellClickEnabled by remember { mutableStateOf(true) }

    // Audio
    // remember the SoundPool
    val context = LocalContext.current
    val soundPool = remember {
        SoundPool.Builder()
            .setMaxStreams(3)
            .setAudioAttributes(
                AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_ASSISTANCE_SONIFICATION)
                    .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                    .build()
            )
            .build()
    }
    val sound1 = remember { soundPool.load(context, R.raw.simon1, 1) }
    val sound2 = remember { soundPool.load(context, R.raw.simon2, 1) }
    val sound3 = remember { soundPool.load(context, R.raw.simon3, 1) }
    val sound4 = remember { soundPool.load(context, R.raw.simon4, 1) }
    val sound_pass = remember { soundPool.load(context, R.raw.up, 1) }

    LaunchedEffect(cellClickEnabled) {
        Log.d("SimonSaysRoundBoard", "LaunchedEffect cellClickEnabled $cellClickEnabled")
    }

    LaunchedEffect(phaseState) {
        Log.d("SimonSaysRoundBoard", "LaunchedEffect phaseState ${phaseState.value}")
    }

    LaunchedEffect(playbackEpoch) {
        // playback cells
        Log.d("SimonSaysRoundBoard", "LaunchedEffect playbackEpoch ${phaseState.value}")
        if (!phaseState.value) return@LaunchedEffect
        if (sequence.isEmpty()) {
            onPlaybackFinished()
            return@LaunchedEffect
        }
        cellClickEnabled = false

        delay(500)
        if (sequence.size > 1)
            soundPool.play(sound_pass, 1f, 1f, 0, 0, 1f)

        Log.d("SimonSaysRoundBoard", "Playback")
        delay(600)
        for ((i, cell) in sequence.withIndex()) {
            onPlaybackTick(i)

            val sound: Int
            if (cell.row == 0 && cell.col == 0) {
                sound = sound1
            } else if (cell.row == 0 && cell.col == 1) {
                sound = sound2
            } else if (cell.row == 1 && cell.col == 0) {
                sound = sound3
            } else {
                sound = sound4
            }
            soundPool.play(sound, 1f, 1f, 0, 0, 1f)
            delay(highlightStepMillis)
            onPlaybackTick(null)
            delay(gapMillis)
        }
        onPlaybackFinished()
        cellClickEnabled = true
    }

    DisposableEffect(Unit) {
        onDispose {
            soundPool.release()
        }
    }

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(
            text = "Length $targetLength/$maxLength",
            style = MaterialTheme.typography.titleMedium
        )
        Text(
            text = "Hearts: $hearts",
            style = MaterialTheme.typography.bodyMedium
        )
        Text(
            text = if (isPlaybackPhase) "Watch..." else "Your turn — tap in order",
            style = MaterialTheme.typography.bodyMedium
        )
        if (statusText.isNotBlank()) {
            Text(
                text = statusText,
                style = MaterialTheme.typography.bodySmall
            )
        }
        val cellDp = when {
            boardState.rows >= 5 -> 40.dp
            else -> 80.dp
        }
        boardState.cells.forEachIndexed { rowIndex, row ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                row.forEachIndexed { colIndex, cell ->
                    val isActive = cell.isHighlighted || cell.value == "●"
                    Card(
                        modifier = Modifier.size(cellDp),
                        enabled = true,
                        onClick = {
                            //if (!isPlaybackPhase && hearts > 0) {
                            scope.launch {
                                if (cellClickEnabled) {
                                    cellClickEnabled = false
                                    val snapshot = onCellTap(rowIndex, colIndex)
                                    if (!snapshot.value.earlyReturn) {
                                        val sound: Int
                                        if (rowIndex == 0 && colIndex == 0) {
                                            sound = sound1
                                        } else if (rowIndex == 0 && colIndex == 1) {
                                            sound = sound2
                                        } else if (rowIndex == 1 && colIndex == 0) {
                                            sound = sound3
                                        } else {
                                            sound = sound4
                                        }
                                        soundPool.play(sound, 1f, 1f, 0, 0, 1f)
                                        delay(420)
                                        Log.d(
                                            "SimonSaysRoundBoard",
                                            "mistake ${snapshot.value.isMistake} $sound"
                                        )
                                        if (snapshot.value.isMistake == false) {
                                            delay(100)
                                            soundPool.play(sound_pass, 1f, 1f, 0, 0, 1f)
                                            delay(500)
                                        }
                                        //cellClickEnabled = true
                                    }
                                }
                            }
                        },
                        shape = RoundedCornerShape(8.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = if (isActive) {
                                MaterialTheme.colorScheme.primary
                            } else {
                                MaterialTheme.colorScheme.surfaceVariant
                            }
                        )
                    ) {
                        //Column(modifier = Modifier.fillMaxSize()) {}
                    }
                }
            }
        }
    }
}
