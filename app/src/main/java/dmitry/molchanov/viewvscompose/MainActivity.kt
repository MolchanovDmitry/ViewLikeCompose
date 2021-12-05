package dmitry.molchanov.viewvscompose

import android.content.Context
import android.graphics.Color
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.Gravity
import android.view.View
import android.view.View.*
import android.view.ViewGroup
import android.view.ViewGroup.LayoutParams.MATCH_PARENT
import android.view.ViewGroup.LayoutParams.WRAP_CONTENT
import android.widget.*
import androidx.activity.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.flowWithLifecycle
import androidx.lifecycle.lifecycleScope
import com.google.android.exoplayer2.ui.PlayerView
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach

class MainActivity : AppCompatActivity() {

    private val viewModel: MainViewModel by viewModels { MainViewModelFactory(this) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val viewWithParams = root(viewModel)
        setContentView(viewWithParams.view)
        viewModel.stateFlow
            .flowWithLifecycle(lifecycle, Lifecycle.State.CREATED)
            .onEach(viewWithParams.stateChangeListener::onStateChanged)
            .launchIn(lifecycleScope)
    }
}

private fun Context.root(viewModel: MainViewModel): ListeningView =
    FrameLayout(this).run {
        val stateListeners = arrayOf(
            playerView(),
            progressBar(),
            errorView(),
            pauseButton {
                viewModel.onAction(PauseAction)
            },
            playButton {
                viewModel.onAction(PlayAction)
            },
            timeLine { timeLineProgress ->
                viewModel.onAction(TimeLineProgressChanged(timeLineProgress))
            },
        )
            .onEach(::addView)
            .map { view -> view.stateChangeListener }
        val listener = StateChangeListener { state ->
            stateListeners.forEach { stateListener -> stateListener.onStateChanged(state) }
        }
        return ListeningView(this, listener)
    }


private fun Context.progressBar(): ListeningView {
    val view = ProgressBar(this).apply {
        layoutParams = FrameLayout.LayoutParams(WRAP_CONTENT, WRAP_CONTENT).apply {
            gravity = Gravity.CENTER
        }
    }
    val listener = StateChangeListener { state ->
        view.visibility = if (state == LoadingState) VISIBLE else GONE
    }
    return ListeningView(view, listener)
}

private fun Context.playerView(): ListeningView {
    val view = PlayerView(this).apply {
        useController = false
        layoutParams = FrameLayout.LayoutParams(MATCH_PARENT, MATCH_PARENT)
    }
    val listener = StateChangeListener { state ->
        view.player = (state as? PlayingState)?.player
    }
    return ListeningView(view, listener)
}

private fun Context.pauseButton(onClick: () -> Unit): ListeningView {
    val view = ImageView(this).apply {
        setImageResource(R.drawable.ic_pause)
        layoutParams = FrameLayout.LayoutParams(WRAP_CONTENT, WRAP_CONTENT).apply {
            gravity = Gravity.CENTER
        }
        setOnClickListener { onClick() }
    }
    val listener = StateChangeListener { state ->
        view.visibility = if (state is PlayingState && state.player.isPlaying) VISIBLE else GONE
    }
    return ListeningView(view, listener)
}

private fun Context.playButton(onClick: () -> Unit): ListeningView {
    val view = ImageView(this).apply {
        setImageResource(R.drawable.ic_play)
        layoutParams = FrameLayout.LayoutParams(WRAP_CONTENT, WRAP_CONTENT).apply {
            gravity = Gravity.CENTER
        }
        setOnClickListener { onClick() }
    }
    val listener = StateChangeListener { state ->
        view.visibility = if (state is PlayingState && !state.player.isPlaying) VISIBLE else GONE
    }
    return ListeningView(view, listener)
}

private fun Context.timeLine(onTimeLineProgressChanged: (Int) -> Unit): ListeningView {
    var isBarEdit = false
    val view = SeekBar(this).apply {
        layoutParams = FrameLayout.LayoutParams(MATCH_PARENT, WRAP_CONTENT).apply {
            gravity = Gravity.BOTTOM
        }
        setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar, p1: Int, p2: Boolean) = Unit

            override fun onStartTrackingTouch(seekBar: SeekBar) {
                isBarEdit = true
            }

            override fun onStopTrackingTouch(seekBar: SeekBar) {
                isBarEdit = false
                onTimeLineProgressChanged(seekBar.progress)
            }
        })
    }
    val listener = StateChangeListener { state ->
        if (state is PlayingState && !isBarEdit) {
            val progress = state.player.currentPosition * 100 / state.player.duration
            view.progress = progress.toInt()
        }
    }
    return ListeningView(view, listener)
}

private fun Context.errorView(): ListeningView {
    val view = TextView(this).apply {
        setTextColor(Color.WHITE)
        visibility = GONE
        layoutParams = FrameLayout.LayoutParams(WRAP_CONTENT, WRAP_CONTENT).apply {
            gravity = Gravity.CENTER
        }
    }
    val listener = StateChangeListener { state ->
        if (state is ErrorState) {
            view.text = state.message
            view.visibility = VISIBLE
        }
    }
    return ListeningView(view, listener)
}

private class ListeningView(
    val view: View,
    val stateChangeListener: StateChangeListener
)

private fun ViewGroup.addView(listeningView: ListeningView) {
    addView(listeningView.view)
}

private fun interface StateChangeListener {
    fun onStateChanged(state: MainViewState)
}