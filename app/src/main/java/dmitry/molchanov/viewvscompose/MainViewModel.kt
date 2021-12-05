package dmitry.molchanov.viewvscompose

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.google.android.exoplayer2.ExoPlayer
import com.google.android.exoplayer2.MediaItem
import com.google.android.exoplayer2.PlaybackException
import com.google.android.exoplayer2.Player.*
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class MainViewModel(private val player: ExoPlayer) : ViewModel(), Listener {

    private val _stateFlow = MutableStateFlow<MainViewState>(Undefined)
    val stateFlow = _stateFlow.asStateFlow()

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
            is TimeLineProgressChanged -> onTimeLineProgressChanged(action.timeLineProgress)
        }
    }

    override fun onPlayerStateChanged(playWhenReady: Boolean, playbackState: Int) {
        when (playbackState) {
            STATE_BUFFERING -> {
                LoadingState
            }
            STATE_READY -> {
                if (playWhenReady) {
                    runProgressChangeJob()
                } else {
                    stopProgressChangeJob()
                }
                PlayingState(player)
            }
            else -> {
                stopProgressChangeJob()
                Undefined
            }
        }.let { state -> _stateFlow.value = state }
    }

    override fun onPlayerError(error: PlaybackException) {
        super.onPlayerError(error)
        _stateFlow.value = ErrorState(error.message ?: "Undefined error")
    }

    private var progressJob: Job? = null

    private fun runProgressChangeJob() {
        progressJob?.cancel()
        progressJob = viewModelScope.launch {
            delay(1000)
            _stateFlow.value = PlayingState(player)
            runProgressChangeJob()
        }
    }

    private fun stopProgressChangeJob() {
        progressJob?.cancel()
        progressJob = null
    }

    private fun onTimeLineProgressChanged(timeLineProgress: Int) {
        val newPosition = timeLineProgress * player.duration / 100
        player.seekTo(newPosition)
    }
}

class MainViewModelFactory(
    private val context: Context
) : ViewModelProvider.Factory {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return MainViewModel(ExoPlayer.Builder(context.applicationContext).build()) as T
    }
}

sealed class MainViewState
object Undefined : MainViewState()
object LoadingState : MainViewState()
object PauseState : MainViewState()
class ErrorState(val message: String) : MainViewState()
class PlayingState(val player: ExoPlayer) : MainViewState()


sealed class ViewModelAction
object PlayAction : ViewModelAction()
object PauseAction : ViewModelAction()
object ReleaseAction : ViewModelAction()
class TimeLineProgressChanged(val timeLineProgress: Int) : ViewModelAction()