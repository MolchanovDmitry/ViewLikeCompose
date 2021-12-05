package dmitry.molchanov.compose

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import dmitry.molchanov.common.MainViewModel
import dmitry.molchanov.common.MainViewModelFactory
import dmitry.molchanov.common.PauseAction
import dmitry.molchanov.common.ReleaseAction

class MainActivity : AppCompatActivity() {

    private val viewModel: MainViewModel by viewModels { MainViewModelFactory(this) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MainScreen(viewModel)
        }
    }

    override fun onStop() {
        super.onStop()
        viewModel.onAction(PauseAction)
    }

    override fun onDestroy() {
        super.onDestroy()
        viewModel.onAction(ReleaseAction)
    }
}