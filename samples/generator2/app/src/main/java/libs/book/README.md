
```kotlin
 var xxx = MutableLiveData<String> ( "03_HWave2")

 ch1_EN.observeForever { value -> ... }

 val r1 by Global.ch1_EN.observeAsState()

 live.postValue(0)   │
 live.sevValue(0)    │ в основном потоке
```

```kotlin
┌───────────────────┬─────────────────────────────────────────────────────────────────────┐
│ kotlin.system     │ https://kotlinlang.org/api/latest/jvm/stdlib/kotlin.system/         │
├───────────────────┴────────────────────────┬────────────────────────────────────────────┤
│ measureNanoTime  (block: () -> Unit): Long │ Возвращает прошедшее время в наносекундах  │
│ measureTimeMillis(block: () -> Unit): Long │ Возвращает прошедшее время в милисекундах  │
├───────────────────┬────────────────────────┴────────────────────────────────────────────┤
│ Измерение времени │ val time = measureTimeMillis{...}                                   │
└───────────────────┴─────────────────────────────────────────────────────────────────────┘
```

```kotlin
┌───────────────────┬────────────────────┐
│ Создание потока   │                    │
├───────────────────┴────────────────────┤
│ val threadWithRunnable = Thread(xxx()) │
│ threadWithRunnable.start(              │
│                                        │
│ class xxx : Runnable {                 │
│   override fun run() {                 │
│        while (true) { ... } } }        │
└────────────────────────────────────────┘
```
