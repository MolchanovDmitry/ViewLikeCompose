package dmitry.molchanov.viewvscompose

import android.view.View
import android.view.ViewGroup
import dmitry.molchanov.common.MainViewState


class ListeningView(
    val view: View,
    val stateChangeListener: StateChangeListener
)

fun ViewGroup.addView(listeningView: ListeningView) {
    addView(listeningView.view)
}

fun interface StateChangeListener {
    fun onStateChanged(state: MainViewState)
}