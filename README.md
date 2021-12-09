## Оглавление
1. [О проекте](#о-проекте)
2. [Модуль приложения compose](#модуль-приложения-compose)
3. [Модуль приложения view](#модуль-приложения-view)\
3.1. [Обвязка View](#обвязка-view)\
3.2. [Простейший пример StateView](#простейший-пример-stateView)\
3.3. [Добавление StateView на экран](#добавление-stateView-на-экран)\
3.3. [Добавление корневого StateView на экран](#добавление-корневого-stateView-на-экран)\
4. [Сравнение](#сравнение)\
4.1. [Добавление на экран](#добавление-на-экран)\
4.1. [Добавление дочерних элементов](#добавление-дочерних-элементов)\
4.1. [Кнопка старта](#кнопка-старта)\
4.1. [Линия прогресса воспроизведения](#линия-прогресса-воспроизведения)\
5. [Итог](#итог)


## О проекте.
Одними из главных недостатках андроид View в сравнении с Jetpack Compose:
* Разметка и обработка находятся в двух разных местах.
* Работа с View не заточена под состояния.

Приложение состоит из двух моделей приложения:
* compose;
* view.
В данном примере решил реализовать compose-like программную реализацию View и сравнить с compose реализацией.

## Модуль приложения compose
Панель управления плеера, реализованная с использованием [Jetpack Compose](https://developer.android.com/jetpack/compose).
Кто хочет сразу перейти к исходникам и сравнить:
[Compose панель управления.]()\
[View панель управления.]()

## Модуль приложения view

### Обвязка View
Создаем сущность StateView:
```kotlin
/** View, с блоком обработки состояния [MainViewState]. */
class StateView<T : View, R>(
    val view: T,
    private val stateChangeBlock: T.(R) -> Unit
) {
    /** Вызывается, когда получили новое состояние [state] */
    fun processState(state: R) = view.stateChangeBlock(state)
}
```

### Простейший пример StateView
В качестве примера реализации `StateView` приведу `ProgressBar`, который отображается при получении состояния `LoadingState`.
Обернем инициализацию `StateView` в функцию, чтобы было похоже на compose.
```kotlin
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
```


### Добавление StateView на экран
Чтобы добавить `ProgressBar` из пункта выше в разметку, необходимо создать еще одну функцию,
которая будет возвращать корневой `StateView`.
Корневой `StateView` представляет из себя `FrameLayout`, который при изменении состояния будет
проходиться по всем дочерним `StateView` и уведомлять о получении нового состояния.
```kotlin
fun Context.viewScreenRoot(viewModel: MainViewModel): StateView<FrameLayout, MainViewState> {
    val viewWrappers = arrayOf(
        progressBar(),
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
```

### Добавление корневого StateView на экран
Чтобы добавить корневой `StateView` на экран, необходимо его создать и подписать через [StateFlow](https://developer.android.com/kotlin/flow/stateflow-and-sharedflow)
```kotlin
override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)

    val stateView = viewScreenRoot(viewModel)
    setContentView(stateView.view)

    viewModel.stateFlow
        .flowWithLifecycle(lifecycle, Lifecycle.State.CREATED)
        .onEach(stateView::processState)
        .launchIn(lifecycleScope)
}
```
Готово, теперь мы имеем корневой и дочерние `StateView`, которые подписаны на состояния из вью модели.

## Сравнение

### Добавление на экран

Compose:
```kotlin
override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContent {
        MainScreen(viewModel)
    }
}
```

View:
```kotlin
override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)

    val stateView = viewScreenRoot(viewModel)
    setContentView(stateView.view)

    viewModel.stateFlow
        .flowWithLifecycle(lifecycle, Lifecycle.State.CREATED)
        .onEach(stateView::processState)
        .launchIn(lifecycleScope)
}
```

### Добавление дочерних элементов
Compose:
```kotlin
@Composable
fun MainScreen(viewModel: MainViewModel) {
    val state = viewModel.stateFlow.collectAsState()
    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier.fillMaxSize()
    ) {
        ExoPlayerView(state)
        ProgressBar(state)
        PlayButton(state) {
            viewModel.onAction(PlayAction)
        }
        PauseButton(state) {
            viewModel.onAction(PauseAction)
        }
        TimeLine(state) { newPosition ->
            viewModel.onAction(SetPlayerPosition(newPosition))
        }
        ErrorView(state)
    }
}
```
View:
```kotlin
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
```

### Кнопка старта
Compose:
```kotlin
@Composable
private fun PlayButton(state: State<MainViewState>, onClick: () -> Unit) {
    if (state.value is PauseState) {
        IconButton(
            onClick = { onClick() }
        ) {
            Icon(
                imageVector = Icons.Filled.PlayCircleOutline,
                tint = MaterialTheme.colors.primary,
                contentDescription = "Play",
                modifier = Modifier.size(60.dp)
            )
        }
    }
}
```
View:
```kotlin
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
```
### Линия прогресса воспроизведения
Compose:
```kotlin
@Composable
private fun BoxScope.TimeLine(
    state: State<MainViewState>,
    changePosition: (Long) -> Unit
) {
    val player = (state.value as? PlayerHolder)?.player ?: return
    val duration = if (player.duration >= 0) player.duration else 0
    var sliderPosition by remember { mutableStateOf<Float?>(null) }
    var seekPosition by remember { mutableStateOf<Float?>(null) }
    Slider(
        value = sliderPosition ?: player.currentPosition.toFloat(),
        onValueChange = { value ->
            sliderPosition = value
            seekPosition = value
        },
        onValueChangeFinished = {
            seekPosition?.toLong()?.let { newPosition -> changePosition(newPosition) }
            sliderPosition = null
            seekPosition = null
        },
        valueRange = 0f..duration.toFloat(),
        colors = SliderDefaults.colors(
            thumbColor = MaterialTheme.colors.primary,
            activeTrackColor = MaterialTheme.colors.primary
        ),
        modifier = Modifier
            .fillMaxWidth()
            .align(Alignment.BottomCenter)
            .padding(all = 8.dp)
            .padding(bottom = 16.dp)
    )
}
```
View:
```kotlin
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
```

## Итог

Если очень хочется, то можно писать в compose стиле и на android View.
Но я все же всем советую переходить на compose.

[Compose панель управления.]()\
[View панель управления.]()
