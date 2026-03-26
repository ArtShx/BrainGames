package com.example.braingames.games.memory

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay

@Composable
fun MemoryRoundBoard(
    boardState: com.example.braingames.core.BoardState,
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
