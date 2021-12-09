package dmitry.molchanov.viewvscompose

import android.view.View
import dmitry.molchanov.common.MainViewState

/**
 * View, с блоком обработки состояния [MainViewState].
 */
class StateView<T : View, R>(
    val view: T,
    private val stateChangeBlock: T.(R) -> Unit
) {
    /** Вызывается, когда получили новое состояние [state] */
    fun processState(state: R) = view.stateChangeBlock(state)
}