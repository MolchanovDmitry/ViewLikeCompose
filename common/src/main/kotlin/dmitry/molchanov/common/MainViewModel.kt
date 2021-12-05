package dmitry.molchanov.common

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.google.android.exoplayer2.ExoPlayer
import com.google.android.exoplayer2.MediaItem
import com.google.android.exoplayer2.PlaybackException
import com.google.android.exoplayer2.Player
import com.google.android.exoplayer2.Player.*
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class MainViewModel(private val player: ExoPlayer) : ViewModel(), Listener {

    private val _stateFlow = MutableStateFlow<MainViewState>(UndefinedState)
    val stateFlow = _stateFlow.asStateFlow()

    private var progressJob: Job? = null

    init {
        player.apply {
            addListener(this@MainViewModel)
            addMediaItem(MediaItem.fromUri("https://clck.ru/YQb6f"))
            prepare()
            play()
        }
    }

    fun onAction(action: ViewModelAction) {
        when (action) {
            PlayAction -> player.play()
            PauseAction -> player.pause()
            ReleaseAction -> player.release()
            is SetPlayerPosition -> onTimeLineProgressChanged(action.newPosition)
        }
    }

    override fun onPlayerStateChanged(playWhenReady: Boolean, playbackState: Int) {
        when (playbackState) {
            STATE_IDLE -> onIdleState()
            STATE_BUFFERING -> onBufferingState()
            STATE_READY -> onReadyState(playWhenReady)
            STATE_ENDED -> onFinish()
            else -> error("Uncatched exception")
        }
    }

    override fun onPlayerError(error: PlaybackException) {
        super.onPlayerError(error)
        _stateFlow.value = ErrorState(error.message ?: "Undefined error")
    }

    private fun onIdleState() {
        if (stateFlow.value !is ErrorState) {
            _stateFlow.value = UndefinedState
        }
    }

    private fun onBufferingState() {
        stopProgressChangeJob()
        _stateFlow.value = LoadingState(player)
    }

    private fun onReadyState(playWhenReady: Boolean) {
        _stateFlow.value = if (playWhenReady) {
            runProgressChangeJob()
            PlayingState(player)
        } else {
            stopProgressChangeJob()
            PauseState(player)
        }
    }

    private fun onFinish() {
        stopProgressChangeJob()
        _stateFlow.value = PauseState(player)
    }

    private fun runProgressChangeJob() {
        progressJob?.cancel()
        progressJob = viewModelScope.launch {
            delay(1_000)
            _stateFlow.value = PlayingState(player)
            runProgressChangeJob()
        }
    }

    private fun stopProgressChangeJob() {
        progressJob?.cancel()
        progressJob = null
    }

    private fun onTimeLineProgressChanged(timeLineProgress: Long) {
        player.seekTo(timeLineProgress)
    }
}

class MainViewModelFactory(
    private val context: Context
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return MainViewModel(ExoPlayer.Builder(context.applicationContext).build()) as T
    }
}

interface PlayerHolder {
    val player: Player
}

sealed class MainViewState
object UndefinedState : MainViewState()
class ErrorState(val message: String) : MainViewState()
class PauseState(override val player: Player) : MainViewState(), PlayerHolder
class PlayingState(override val player: Player) : MainViewState(), PlayerHolder
class LoadingState(override val player: Player) : MainViewState(), PlayerHolder


sealed class ViewModelAction
object PlayAction : ViewModelAction()
object PauseAction : ViewModelAction()
object ReleaseAction : ViewModelAction()
class SetPlayerPosition(val newPosition: Long) : ViewModelAction()