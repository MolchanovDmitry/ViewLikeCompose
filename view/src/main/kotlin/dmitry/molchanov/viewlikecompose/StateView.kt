package dmitry.molchanov.viewlikecompose

import android.view.View

/**
 * View, с блоком обработки состояния R.
 */
class StateView<T : View, R>(
    val view: T,
    private val stateChangeBlock: T.(R) -> Unit
) {
    /** Вызывается, когда получили новое состояние [state] */
    fun processState(state: R) = view.stateChangeBlock(state)
}