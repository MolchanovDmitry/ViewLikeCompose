package dmitry.molchanov.viewvscompose

import android.content.Context
import android.graphics.Color
import android.view.Gravity
import android.view.View.GONE
import android.view.View.VISIBLE
import android.view.ViewGroup.LayoutParams.MATCH_PARENT
import android.view.ViewGroup.LayoutParams.WRAP_CONTENT
import android.widget.*
import com.google.android.exoplayer2.ui.PlayerView
import dmitry.molchanov.common.*

fun Context.viewScreenRoot(viewModel: MainViewModel): ListeningView =
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
                viewModel.onAction(SetPlayerPosition(timeLineProgress))
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
        view.visibility = if (state is LoadingState) VISIBLE else GONE
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
        view.visibility =
            if (state is PlayingState && state.player.isPlaying) VISIBLE else GONE
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

private fun Context.timeLine(onTimeLineProgressChanged: (Long) -> Unit): ListeningView {
    var isBarEdit = false
    var duration = 0L
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
                val newPosition =  seekBar.progress * duration/ 100
                onTimeLineProgressChanged(newPosition)
            }
        })
    }
    val listener = StateChangeListener { state ->
        if (state is PlayerHolder && !isBarEdit && state.player.duration != 0L) {
            duration = state.player.duration
            val progress = state.player.currentPosition * 100 / duration
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