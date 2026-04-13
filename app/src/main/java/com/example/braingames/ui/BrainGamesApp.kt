package com.example.braingames.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.platform.testTag
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import com.example.braingames.core.BoardState
import com.example.braingames.core.Difficulty
import com.example.braingames.core.GameResult
import com.example.braingames.core.GameType
import com.example.braingames.games.memory.MemoryBoardMapper
import com.example.braingames.games.memory.MemoryRoundBoard
import com.example.braingames.games.simon.SimonSaysRoundBoard
import com.example.braingames.games.queens.QueensBoardMapper
import com.example.braingames.games.tango.TangoBoardMapper
import com.example.braingames.games.zip.ZipBoardMapper
import com.example.braingames.ui.games.memory.MemoryViewModel
import com.example.braingames.ui.games.simonsays.SimonSaysViewModel
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

@Composable
fun BrainGamesApp() {
    var selectedGame by remember { mutableStateOf<GameType?>(null) }
    var selectedDifficulty by remember { mutableStateOf(Difficulty.Easy) }

    if (selectedGame == null) {
        HomeScreen(
            selectedDifficulty = selectedDifficulty,
            onDifficultySelected = { selectedDifficulty = it },
            onGameSelected = { selectedGame = it }
        )
    } else {
        GameScreen(
            gameType = selectedGame!!,
            difficulty = selectedDifficulty,
            onBack = { selectedGame = null }
        )
    }
}

@Composable
fun HomeScreen(
    selectedDifficulty: Difficulty,
    onDifficultySelected: (Difficulty) -> Unit,
    onGameSelected: (GameType) -> Unit
) {
    val games = remember { GameType.entries }

    Scaffold(
        topBar = {
            GameTopBar(title = "Brain Games")
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            contentPadding = PaddingValues(bottom = 24.dp)
        ) {
            item {
                Text(
                    text = "Pick a game",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.SemiBold
                )
            }
//            item {
//                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
//                    Difficulty.entries.forEach { difficulty ->
//                        AssistChip(
//                            onClick = { onDifficultySelected(difficulty) },
//                            label = { Text(difficulty.name) },
//                            colors = AssistChipDefaults.assistChipColors(
//                                containerColor = if (difficulty == selectedDifficulty) {
//                                    MaterialTheme.colorScheme.primaryContainer
//                                } else {
//                                    MaterialTheme.colorScheme.surfaceVariant
//                                }
//                            )
//                        )
//                    }
//                }
//            }
            items(games) { game ->
                Card(modifier = Modifier.fillMaxWidth()) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(text = game.name)
                        Button(
                            onClick = { onGameSelected(game) },
                            modifier = Modifier.testTag("play_${game.name}")
                        ) {
                            Text(game.name)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun GameScreen(
    gameType: GameType,
    difficulty: Difficulty,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val viewModel = remember(gameType, difficulty) {
        when (gameType) {
            GameType.Memory -> MemoryViewModel(context)
            GameType.SimonSays -> SimonSaysViewModel(context)
            else -> TODO("Not implemented yet")
        }
    }

    val snapshot by viewModel.snapshot.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(viewModel) {
//        viewModel.snapshot.collectLatest {
//            if (it.gameResult is GameResult.InvalidMove) {
//                snackbarHostState.showSnackbar("Invalid move for this game.")
//            }
//        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        topBar = {
            GameTopBar(
                title = gameType.name,
                onBack = onBack
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            if (viewModel is MemoryViewModel) {
                MemoryRoundBoard(
                    boardState = snapshot.boardState,
                    round = viewModel.getMemoryRound(),
                    hearts = viewModel.getMemoryHearts(),
                    isPreviewPhase = viewModel.isMemoryPreviewActive(),
                    previewMillis = viewModel.getMemoryPreviewMillis(),
                    statusText = viewModel.getMemoryStatusText(),
                    onCellTap = viewModel::onCellTap,
                    onPreviewFinished = viewModel::onMemoryPreviewFinished,
                    viewModel = viewModel
                )
            } else if (viewModel is SimonSaysViewModel) {
                SimonSaysRoundBoard(
                    boardState = snapshot.boardState,
                    sequence = viewModel.getSimonSequence(),
                    targetLength = viewModel.getSimonRound(),
                    maxLength = viewModel.getSimonMaxRound(),
                    hearts = viewModel.getSimonHearts(),
                    isPlaybackPhase = viewModel.isSimonPlaybackActive(),
                    playbackEpoch = viewModel.getSimonPlaybackEpoch(),
                    highlightStepMillis = viewModel.getSimonStepHighlightMillis(),
                    gapMillis = viewModel.getSimonStepGapMillis(),
                    statusText = viewModel.getSimonStatusText(),
                    onCellTap = viewModel::onCellTap,
                    onPlaybackTick = viewModel::onSimonPlaybackTick,
                    onPlaybackFinished = viewModel::onSimonPlaybackFinished

                )
            } else {
                BoardGrid(
                    boardState = snapshot.boardState,
                    gameType = gameType,
                    onCellTap = viewModel::onCellTap
                )
            }
//            GameActionBar(
//                onReset = { viewModel.onReset() },
//                onUndo = { viewModel.onUndo() },
//                onHint = {
//                    val message = viewModel.getHintMessage()
//                    scope.launch { snackbarHostState.showSnackbar(message) }
//                }
//            )
//            Text(
//                text = "Moves: ${snapshot.moveCount}",
//                style = MaterialTheme.typography.bodyMedium
//            )
            if (snapshot.gameResult is GameResult.Solved) {
                Text(
                    text = "Solved!",
                    modifier = Modifier.testTag("solved_banner"),
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.primary
                )
            }
            if (viewModel.isGameFinished()) {
                viewModel.onGameFinished()
                Button(
                    onClick = { viewModel.onPlayAgain() },
                    modifier = Modifier.fillMaxWidth().testTag("play_again_button")
                ) {
                    Text("Play Again")
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GameTopBar(title: String, onBack: (() -> Unit)? = null) {
    TopAppBar(
        title = { Text(title) },
        navigationIcon = {
            if (onBack != null) {
                TextButton(onClick = onBack) {
                    Text("Back")
                }
            }
        }
    )
}

@Composable
fun BoardGrid(
    boardState: BoardState,
    gameType: GameType,
    onCellTap: (Int, Int) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        boardState.cells.forEachIndexed { rowIndex, row ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                row.forEachIndexed { colIndex, cell ->
                    Card(
                        modifier = Modifier.size(56.dp),
                        onClick = { onCellTap(rowIndex, colIndex) },
                        shape = RoundedCornerShape(8.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = if (cell.isHighlighted) {
                                MaterialTheme.colorScheme.secondaryContainer
                            } else {
                                MaterialTheme.colorScheme.surfaceVariant
                            }
                        )
                    ) {
                        Column(
                            modifier = Modifier.fillMaxSize(),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Text(
                                when (gameType) {
                                    GameType.Memory -> MemoryBoardMapper.mapCellDisplay(cell)
                                    GameType.SimonSays -> ""
                                    GameType.Queens -> QueensBoardMapper.mapCellDisplay(cell)
                                    GameType.Zip -> ZipBoardMapper.mapCellDisplay(cell)
                                    GameType.Tango -> TangoBoardMapper.mapCellDisplay(cell)
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun GameActionBar(onReset: () -> Unit, onUndo: () -> Unit, onHint: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Button(onClick = onReset, modifier = Modifier.weight(1f)) {
            Text("Reset")
        }
        Button(onClick = onHint, modifier = Modifier.weight(1f)) {
            Text("Hint")
        }
        Button(onClick = onUndo, modifier = Modifier.weight(1f)) {
            Text("Undo")
        }
    }
}
