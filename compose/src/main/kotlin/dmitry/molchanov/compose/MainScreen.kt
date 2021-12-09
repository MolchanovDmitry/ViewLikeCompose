package dmitry.molchanov.compose

import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PauseCircleOutline
import androidx.compose.material.icons.filled.PlayCircleOutline
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.google.android.exoplayer2.ui.PlayerView
import dmitry.molchanov.common.*

@Composable
fun MainScreen(viewModel: MainViewModel) {
    val state = viewModel.stateFlow.collectAsState()
    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier.fillMaxSize()
    ) {
        ExoPlayerView(state)
        ProgressBar(state)
        PlayButton(state) {
            viewModel.onAction(PlayAction)
        }
        PauseButton(state) {
            viewModel.onAction(PauseAction)
        }
        TimeLine(state) { newPosition ->
            viewModel.onAction(SetPlayerPosition(newPosition))
        }
        ErrorView(state)
    }
}

@Composable
fun ExoPlayerView(state: State<MainViewState>) {
    when (val stateWitPlayer = state.value) {
        is PlayerHolder -> {
            AndroidView(
                factory = { context ->
                    PlayerView(context).apply {
                        player = stateWitPlayer.player
                        useController = false
                    }
                },
                modifier = Modifier.fillMaxSize()
            )
        }
    }
}

@Composable
private fun ProgressBar(state: State<MainViewState>) {
    when (state.value) {
        is UndefinedState, is LoadingState -> {
            CircularProgressIndicator()
        }
    }
}

@Composable
private fun PlayButton(state: State<MainViewState>, onClick: () -> Unit) {
    if (state.value is PauseState) {
        IconButton(
            onClick = { onClick() }
        ) {
            Icon(
                imageVector = Icons.Filled.PlayCircleOutline,
                tint = MaterialTheme.colors.primary,
                contentDescription = "Play",
                modifier = Modifier.size(60.dp)
            )
        }
    }
}

@Composable
private fun PauseButton(state: State<MainViewState>, onClick: () -> Unit) {
    if (state.value is PlayingState) {
        IconButton(
            onClick = { onClick() }
        ) {
            Icon(
                imageVector = Icons.Filled.PauseCircleOutline,
                tint = MaterialTheme.colors.primary,
                contentDescription = "Pause",
                modifier = Modifier.size(60.dp)
            )
        }
    }
}

@Composable
private fun BoxScope.TimeLine(
    state: State<MainViewState>,
    changePosition: (Long) -> Unit
) {
    val player = (state.value as? PlayerHolder)?.player ?: return
    val duration = if (player.duration >= 0) player.duration else 0
    var sliderPosition by remember { mutableStateOf<Float?>(null) }
    var seekPosition by remember { mutableStateOf<Float?>(null) }
    Slider(
        value = sliderPosition ?: player.currentPosition.toFloat(),
        onValueChange = { value ->
            sliderPosition = value
            seekPosition = value
        },
        onValueChangeFinished = {
            seekPosition?.toLong()?.let { newPosition -> changePosition(newPosition) }
            sliderPosition = null
            seekPosition = null
        },
        valueRange = 0f..duration.toFloat(),
        colors = SliderDefaults.colors(
            thumbColor = MaterialTheme.colors.primary,
            activeTrackColor = MaterialTheme.colors.primary
        ),
        modifier = Modifier
            .fillMaxWidth()
            .align(Alignment.BottomCenter)
            .padding(all = 8.dp)
            .padding(bottom = 16.dp)
    )
}

@Composable
fun ErrorView(state: State<MainViewState>) {
    (state.value as? ErrorState)?.message?.let { message ->
        Text(text = message)
    }
}
