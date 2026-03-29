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
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.unit.dp
import com.example.braingames.R
import com.example.braingames.core.BoardCoordinate
import com.example.braingames.core.BoardState
import kotlinx.coroutines.delay

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
    onCellTap: (Int, Int) -> Unit,
    onPlaybackTick: (Int?) -> Unit,
    onPlaybackFinished: () -> Unit,
    modifier: Modifier = Modifier
) {
    val phaseState = rememberUpdatedState(isPlaybackPhase)

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
    val soundId = remember { soundPool.load(context, R.raw.example, 1) }

    LaunchedEffect(playbackEpoch) {
        Log.d("SimonSaysRoundBoard", "LaunchedEffect ${phaseState.value}")
        if (!phaseState.value) return@LaunchedEffect
        if (sequence.isEmpty()) {
            onPlaybackFinished()
            return@LaunchedEffect
        }
        Log.d("SimonSaysRoundBoard", "LaunchedEffect2 ckp1")
        delay(600)
        Log.d("SimonSaysRoundBoard", "LaunchedEffect2 ckp2")
        for (i in sequence.indices) {
            onPlaybackTick(i)
            Log.d("SimonSaysRoundBoard", "LaunchedEffect2 ckp3")
            delay(highlightStepMillis)
            onPlaybackTick(null)
            Log.d("SimonSaysRoundBoard", "LaunchedEffect2 ckp4")
            delay(gapMillis)
        }
        Log.d("SimonSaysRoundBoard", "LaunchedEffect2 ckp5")
        onPlaybackFinished()
        Log.d("SimonSaysRoundBoard", "LaunchedEffect2 ckp6")
        delay(300)
    }

    DisposableEffect(Unit) {
        onDispose {
            soundPool.release()
        }
    }


    @Composable
    fun AudibleButton() {
//        val context = LocalContext.current
//
//        // 1. Initialize and remember the SoundPool
//        val soundPool = remember {
//            SoundPool.Builder()
//                .setMaxStreams(3)
//                .setAudioAttributes(
//                    AudioAttributes.Builder()
//                        .setUsage(AudioAttributes.USAGE_ASSISTANCE_SONIFICATION)
//                        .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
//                        .build()
//                )
//                .build()
//        }
//
//        // 2. Load the sound (returns a soundId)
//        val soundId = remember { soundPool.load(context, R.raw.example, 1) }

        // 3. Clean up memory when this Composable leaves the screen
        DisposableEffect(Unit) {
            onDispose {
                soundPool.release()
            }
        }

        Button(onClick = {
            // 4. Play! (soundId, leftVol, rightVol, priority, loop, rate)
            soundPool.play(soundId, 1f, 1f, 0, 0, 1f)
        }) {
            Text("Click Me for Sound")
        }
    }

    //AudibleButton()

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
            else -> 48.dp
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
                        onClick = {
                            //if (!isPlaybackPhase && hearts > 0) {
                            onCellTap(rowIndex, colIndex)
                            soundPool.play(soundId, 1f, 1f, 0, 0, 1f)

                            //soundPool.play(soundId, 1f, 1f, 0, 0, 1f)
                            //view.playSoundEffect(android.view.SoundEffectConstants.CLICK)

                            //}
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
                        Column(modifier = Modifier.fillMaxSize()) {}
                    }
                }
            }
        }
    }
}
