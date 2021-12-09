package dmitry.molchanov.viewlikecompose

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

fun Context.viewScreenRoot(viewModel: MainViewModel): StateView<FrameLayout, MainViewState> {
    val viewWrappers = arrayOf(
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
    return StateView(
        view = FrameLayout(this).apply {
            viewWrappers
                .map { it.view }
                .forEach(::addView)
        },
        stateChangeBlock = { state ->
            viewWrappers.forEach { viewWrapper -> viewWrapper.processState(state) }
        }
    )
}


private fun Context.progressBar() =
    StateView<ProgressBar, MainViewState>(
        view = ProgressBar(this).apply {
            layoutParams = FrameLayout.LayoutParams(WRAP_CONTENT, WRAP_CONTENT).apply {
                gravity = Gravity.CENTER
            }
        },
        stateChangeBlock = { state ->
            visibility = if (state is LoadingState) VISIBLE else GONE
        }
    )

private fun Context.playerView() =
    StateView<PlayerView, MainViewState>(
        view = PlayerView(this).apply {
            useController = false
            layoutParams = FrameLayout.LayoutParams(MATCH_PARENT, MATCH_PARENT)
        },
        stateChangeBlock = { state ->
            player = (state as? PlayerHolder)?.player
        })

private fun Context.pauseButton(onClick: () -> Unit) =
    StateView<ImageView, MainViewState>(
        view = ImageView(this).apply {
            setImageResource(R.drawable.ic_pause)
            layoutParams = FrameLayout.LayoutParams(WRAP_CONTENT, WRAP_CONTENT).apply {
                gravity = Gravity.CENTER
            }
            setOnClickListener { onClick() }
        },
        stateChangeBlock = { state ->
            visibility = if (state is PlayingState) VISIBLE else GONE
        }
    )

private fun Context.playButton(onClick: () -> Unit) =
    StateView<ImageView, MainViewState>(
        view = ImageView(this).apply {
            setImageResource(R.drawable.ic_play)
            layoutParams = FrameLayout.LayoutParams(WRAP_CONTENT, WRAP_CONTENT).apply {
                gravity = Gravity.CENTER
            }
            setOnClickListener { onClick() }
        },
        stateChangeBlock = { state ->
            visibility = if (state is PauseState) VISIBLE else GONE
        }
    )

private fun Context.timeLine(onTimeLineProgressChanged: (Long) -> Unit): StateView<SeekBar, MainViewState> {
    var isBarEdit = false
    var duration = 0L
    return StateView(
        view = SeekBar(this).apply {

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
                    val newPosition = seekBar.progress * duration / 100
                    onTimeLineProgressChanged(newPosition)
                }
            })
        },
        stateChangeBlock = { state ->
            if (state is PlayerHolder && !isBarEdit && state.player.duration != 0L) {
                duration = state.player.duration
                val progress = state.player.currentPosition * 100 / duration
                this.progress = progress.toInt()
            }
        })
}

private fun Context.errorView() =
    StateView<TextView, MainViewState>(
        view = TextView(this).apply {
            setTextColor(Color.WHITE)
            visibility = GONE
            layoutParams = FrameLayout.LayoutParams(WRAP_CONTENT, WRAP_CONTENT).apply {
                gravity = Gravity.CENTER
            }
        },
        stateChangeBlock = { state ->
            if (state is ErrorState) {
                text = state.message
                visibility = VISIBLE
            }
        })