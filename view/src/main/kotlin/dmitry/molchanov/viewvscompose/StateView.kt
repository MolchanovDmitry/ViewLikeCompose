package dmitry.molchanov.viewvscompose

import android.view.View
import dmitry.molchanov.common.MainViewState


class StateView<T : View>(
    val view: T,
    private val listener: T.(MainViewState) -> Unit
) {
    fun processState(state: MainViewState) = view.listener(state)
}