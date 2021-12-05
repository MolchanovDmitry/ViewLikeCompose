package dmitry.molchanov.viewvscompose

import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.flowWithLifecycle
import androidx.lifecycle.lifecycleScope
import dmitry.molchanov.common.MainViewModel
import dmitry.molchanov.common.MainViewModelFactory
import dmitry.molchanov.common.PauseAction
import dmitry.molchanov.common.ReleaseAction
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach

class MainActivity : AppCompatActivity() {

    private val viewModel: MainViewModel by viewModels { MainViewModelFactory(this) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val viewWithParams = viewScreenRoot(viewModel)
        setContentView(viewWithParams.view)

        viewModel.stateFlow
            .flowWithLifecycle(lifecycle, Lifecycle.State.CREATED)
            .onEach(viewWithParams.stateChangeListener::onStateChanged)
            .launchIn(lifecycleScope)
    }

    override fun onPause() {
        super.onPause()
        viewModel.onAction(PauseAction)
    }

    override fun onDestroy() {
        super.onDestroy()
        viewModel.onAction(ReleaseAction)
    }
}