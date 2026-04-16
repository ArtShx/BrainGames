package com.example.braingames.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.example.braingames.core.GameType
import com.example.braingames.database.AppDatabase
import com.example.braingames.database.entity.HighScore
import com.example.braingames.utils.formatTimestamp

@Composable
fun HighScoreScreen(game_type: GameType) {
    var context = LocalContext.current
    var db = AppDatabase.getDatabase(context)
    var highScores by remember { mutableStateOf(listOf<HighScore>()) }

    LaunchedEffect(Unit) {
        highScores = db.highScoreDao().getAllScoresByGame(game_type.toString())
    }

    Column(modifier = Modifier.padding(16.dp)) {
        Text(
            text = "Game: ${game_type.name}",
            style = MaterialTheme.typography.headlineSmall,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        LazyColumn(modifier = Modifier.fillMaxWidth()) {
            if (highScores.isEmpty()) {
                item {
                    Text(
                        text = "No high scores found",
                        modifier = Modifier.padding(top = 28.dp)
                    )
                }
            } else {
                items(highScores) { score ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant
                        )
                    ) {
                        Row(
                            modifier = Modifier.padding(16.dp),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Score: ${score.score}")
                            Text("Date: ${formatTimestamp(score.timestamp)}")
                        }
                    }
                }
            }
        }
    }
}