package com.example.braingames.games.memory

import androidx.activity.result.launch
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.room.Room
import com.example.braingames.core.BoardState
import com.example.braingames.database.AppDatabase
import com.example.braingames.database.entity.HighScore
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun MemoryRoundBoard(
    boardState: BoardState,
    round: Int,
    hearts: Int,
    isPreviewPhase: Boolean,
    previewMillis: Long,
    statusText: String,
    onCellTap: (Int, Int) -> Unit,
    onPreviewFinished: () -> Unit,
    modifier: Modifier = Modifier
) {
    LaunchedEffect(round, isPreviewPhase) {
        if (isPreviewPhase) {
            delay(previewMillis)
            onPreviewFinished()
        }
    }

    var context = LocalContext.current
    var db = AppDatabase.getDatabase(context) //Room.databaseBuilder(context, AppDatabase::class.java, "my-db").build()
    val scope = rememberCoroutineScope()

    var highScores by remember { mutableStateOf(listOf<HighScore>()) }

    LaunchedEffect(Unit) {
        highScores = db.highScoreDao().getTopScores("Memory", "Easy") // Make sure this exists in your DAO
    }

    Button(
        onClick = {
            scope.launch {
                val newScore = HighScore(
                    score = (10..100).random(), // Random score for testing
                    timestamp = System.currentTimeMillis(),
                    gameReferenceId = "Memory",
                    duration = 5000,
                    difficulty = "Easy"
                )
                db.highScoreDao().insertScore(newScore)
                // Refresh the list after adding
                highScores = db.highScoreDao().getAllScores()
            }
        },
        modifier = Modifier.fillMaxWidth()
    ) {
        Text("Add Random High Score")
    }

    Text(text = "Recent Scores:", style = MaterialTheme.typography.headlineSmall)

    // 4. List to display scores
    LazyColumn(
        //modifier = Modifier.weight(1f).fillMaxWidth()
    ) {
        items(highScores) { score ->
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
            ) {
                Row(
                    modifier = Modifier.padding(8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Score: ${score.score}")
                    Text("Date: ${score.timestamp}")
                }
            }
        }
    }


    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(
            text = "Round $round/10",
            style = MaterialTheme.typography.titleMedium
        )
        Text(
            text = "Hearts: $hearts",
            style = MaterialTheme.typography.bodyMedium
        )
        Text(
            text = if (isPreviewPhase) "Preview..." else "Your turn",
            style = MaterialTheme.typography.bodyMedium
        )
        if (statusText.isNotBlank()) {
            Text(
                text = statusText,
                style = MaterialTheme.typography.bodySmall
            )
        }
        boardState.cells.forEachIndexed { rowIndex, row ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                row.forEachIndexed { colIndex, cell ->
                    val isActive = cell.isHighlighted || cell.value == "●"
                    Card(
                        modifier = Modifier.size(48.dp),
                        onClick = {
                            if (!isPreviewPhase && hearts > 0) {
                                onCellTap(rowIndex, colIndex)
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
                        Column(modifier = Modifier.fillMaxSize()) {}
                    }
                }
            }
        }
    }
}
