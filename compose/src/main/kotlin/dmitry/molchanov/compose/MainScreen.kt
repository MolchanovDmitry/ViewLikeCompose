package dmitry.molchanov.compose

import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import dmitry.molchanov.common.MainViewModel

@Composable
fun MainScreen(viewModel: MainViewModel) {
    val state = viewModel.stateFlow.collectAsState()
    Box{

    }
}