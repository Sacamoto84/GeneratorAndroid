```kotlin

var ch1_FM_Base = MutableStateFlow<Float>(2500f)

val fmBase: State<Float?> = 
    ch1_FM_Base.collectAsState()

LiveData.ch1_FM_Base.update { value }

mutable.value = 1
mutable.emit(2)
mutable.update {3}

LiveData.ch1_FM_Base.value = dataJsonVolume.ch1_FM_Base

GlobalScope.launch(dispatchers) {
        LiveData.volume0.collect {
            hub.playbackEngine.setVolume(
                0,
                it
            )
        }
    }
```