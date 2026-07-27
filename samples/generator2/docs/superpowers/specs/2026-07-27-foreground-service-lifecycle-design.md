# Управляемый жизненный цикл Foreground Service для генерации звука

Дата: 2026-07-27

## Проблема

`SoundService` не владеет звуком. Он показывает нотификацию и больше не делает ничего.
Реальная генерация живёт вне сервиса:

- [MainActivity.kt:218](../../../app/src/main/java/com/example/generator2/MainActivity.kt) —
  `Thread { runBlocking { launch(Dispatchers.Default) { audioMixerPump.run() } } }`, внутри
  `while (true)` без выхода;
- [MainActivity.kt:241](../../../app/src/main/java/com/example/generator2/MainActivity.kt) —
  `Spectrogram.startFFTLoop()`, нативный `pthread`, наружу стоп не выведен;
- `AudioMixerPump` — `@Singleton` в `SingletonComponent`, живёт столько же, сколько процесс.

Следствия:

1. Пользователь смахивает приложение из недавних. Activity уничтожается, но foreground service
   держит процесс живым, поток пампа продолжает писать в `AudioTrack` — звук идёт вечно, остановить
   его нечем.
2. `SoundService` не имеет ни `onTaskRemoved`, ни `stopSelf`, ни экшнов в нотификации.
3. У `MainActivity` нет `android:configChanges`. Поворот экрана пересоздаёт Activity, `onCreate`
   запускает второй поток пампа поверх первого — два `AudioTrack` на одном устройстве.

## Требуемое поведение

| Событие | Звук | Нотификация | Процесс |
|---|---|---|---|
| Приложение свёрнуто, экран выключен, открыто другое приложение | идёт | висит | жив |
| Поворот экрана | идёт, не удваивается | висит | жив |
| Смахнули из недавних | глохнет | исчезает | уходит |
| Нажали «Закрыть» в нотификации | глохнет | исчезает | уходит, карточка из недавних убрана |

Foreground service нужен ровно для одного: не дать системе убить процесс со звуком раньше времени.

## Архитектура

Владение движком переезжает из `MainActivity` в `SoundService`. Activity остаётся только UI.

```
MainActivity.onCreate ──► startForegroundService(SoundService, ACTION_START)

SoundService.onStartCommand(ACTION_START)
   ├─ startForeground(NOTIFICATION_ID, notification)   ← первым делом, до любой работы
   └─ if (!engineRunning) {
          engineRunning = true
          Spectrogram.startFFTLoop()
          serviceScope.launch(audioDispatcher) { audioMixerPump.run() }
      }
```

`SoundService` помечается `@AndroidEntryPoint` и инжектит тот же `@Singleton AudioMixerPump`.
DI-модуль [module.kt](../../../app/src/main/java/com/example/generator2/di/module.kt) не меняется —
`Generator`, `Initialization` и все ViewModel продолжают видеть тот же экземпляр.

### Ключевые ограничения

**`startForeground` строго первым.** Android 12+ бросает
`ForegroundServiceDidNotStartInTimeException`, если между `startForegroundService` и
`startForeground` прошло больше 5 секунд. Инициализация пампа блокируется в ожидании ExoPlayer,
поэтому она обязана идти после вызова `startForeground`.

**Идемпотентность.** `onStartCommand` вызывается заново при каждом возврате в приложение. Флаг
`engineRunning` гарантирует один памп на процесс и заодно чинит существующий баг с поворотом экрана.

**Поток пампа.** Вместо `Thread { runBlocking { ... } }` — однопоточный диспетчер, созданный из
`Executors.newSingleThreadExecutor` с фабрикой потока, выставляющей
`Process.THREAD_PRIORITY_AUDIO`. Приоритет сохраняется, но корутина становится отменяемой — это
основа всей остановки.

## Остановка

Оба сценария сходятся в одну точку, отдельной логики для каждого нет:

```
смахнул из недавних ──► onTaskRemoved() ──┐
                                          ├──► stopSelf() ──► onDestroy() ──► shutdown()
нажал «Закрыть» ──► ACTION_CLOSE ─────────┘

shutdown():
   1. serviceScope.cancel()          → корутина пампа выходит
   2. audioMixerPump.shutdown()      → audioOut.destroy(), exoplayer.release()
   3. Spectrogram.stopFFTLoop()      → новый JNI: exit = true, sem_post, pthread_join
   4. stopForeground(REMOVE), engineRunning = false   → нотификация исчезает
   5. ActivityManager.appTasks.forEach { it.finishAndRemoveTask() }
   6. exitProcess(0)
```

Порядок шагов 1–3 обязателен: памп останавливается раньше FFT, иначе `sentToFloatRingBufferFFT`
может сделать `sem_post` в уже присоединённый поток.

Шаги 5 и 6 выполняются в обоих сценариях. `exitProcess(0)` гарантирует, что следующий запуск
начинается с чистого состояния: Hilt-синглтоны иначе пережили бы закрытие и `AudioMixerPump`
остался бы с уничтоженным `AudioOut` и освобождённым ExoPlayer.

### Правки в AudioMixerPump

Цикл `run()` сейчас неотменяем. Нужно:

- `coroutineContext.ensureActive()` в начале каждой итерации `while (true)`;
- тело цикла в `try / finally`, в `finally` — освобождение `AudioOut`;
- стартовый `while (!init)` (busy-wait ожидания ExoPlayer) получает `ensureActive()` и `delay(1)`,
  иначе отмена во время старта подвесит поток навсегда;
- новый метод `shutdown()`: `audioOut.destroy()` и освобождение `PlayerMP3`.

`exoplayer.streamOut.receive()` — suspend-функция, отменяется мгновенно.
`audioOut.out?.write(..., WRITE_BLOCKING)` отмену не чувствует, но возвращается за время буфера
(порядка 200 мс); `onDestroy` ждёт этот тик.

### Правки в нативном коде

В [jniFFT.cpp](../../../app/src/main/cpp/spectrogram/jniFFT.cpp) механизм выхода уже реализован:
`context1.exit` и семафор `headwriteprotect`. Не хватает экспорта наружу.

Добавляется `Java_com_example_generator2_Spectrogram_stopFFTLoop`: выставляет `context1.exit = true`,
делает `sem_post`, затем `pthread_join(context1.worker)` и сбрасывает `isInitialized = false`.
Функция идемпотентна — повторный вызов при `isInitialized == false` завершается сразу.

Соответствующий `external fun stopFFTLoop()` объявляется в
[Spectrogram.kt](../../../app/src/main/java/com/example/generator2/Spectrogram.kt).

## Нотификация

```
┌──────────────────────────────────────┐
│ ♪ Генератор                          │
│ Генерация звука активна              │
│                          [ ЗАКРЫТЬ ] │
└──────────────────────────────────────┘
   тап по телу → открыть MainActivity
```

- Канал прежний: `IMPORTANCE_LOW`, без звука и вибрации. `setOngoing(true)`, `setSilent(true)`.
- `contentIntent` → `PendingIntent.getActivity(MainActivity)` с `FLAG_IMMUTABLE`. Сейчас его нет
  вообще, тап по нотификации ничего не делает.
- Экшн «Закрыть» → `PendingIntent.getService(SoundService, ACTION_CLOSE)`, тоже `FLAG_IMMUTABLE`.
- Иконка в статусбаре остаётся `android.R.drawable.ic_media_play`: `@drawable/j7` цветная и
  превратится в белый квадрат.

Кнопка одна. Промежуточного состояния «звук заглушен, приложение живо» в дизайне нет.

## Порядок запуска и POST_NOTIFICATIONS

Сейчас [MainActivity.kt:293](../../../app/src/main/java/com/example/generator2/MainActivity.kt) не
стартует сервис, если `POST_NOTIFICATIONS` не выдано. Раньше это было безобидно — звук шёл из потока
Activity независимо от сервиса. После переноса владения тот же код означал бы: разрешение не дал →
звука нет вообще.

Новое поведение: **foreground service стартует всегда**. `POST_NOTIFICATIONS` на запуск сервиса не
влияет — при отказе сервис работает, просто нотификация не отображается. Разрешение запрашивается
отдельно и только ради видимости нотификации.

Побочный эффект отказа: нотификации нет → кнопки «Закрыть» нет → остаётся смахивание из недавних.
Это ограничение системы, обойти нечем.

## Изменения по файлам

| Файл | Что делаем |
|---|---|
| `SoundService.kt` | `@AndroidEntryPoint`, инжект `AudioMixerPump`, `ACTION_START` / `ACTION_CLOSE`, `serviceScope` + аудио-диспетчер, `onTaskRemoved`, `shutdown()`, экшн и `contentIntent` в нотификации |
| `MainActivity.kt` | Убрать `Thread { runBlocking { pump.run() } }` и `Spectrogram.startFFTLoop()`; стартовать сервис безусловно; `POST_NOTIFICATIONS` запрашивать отдельно от старта |
| `AudioMixerPump.kt` | `ensureActive()` в обоих циклах, `try/finally`, `delay(1)` в busy-wait, новый `shutdown()` |
| `Spectrogram.kt` | `external fun stopFFTLoop()` |
| `jniFFT.cpp` | Экспорт `stopFFTLoop`: `exit = true`, `sem_post`, `pthread_join`, `isInitialized = false` |
| `AndroidManifest.xml` | Без изменений: `foregroundServiceType="mediaPlayback"` и `FOREGROUND_SERVICE_MEDIA_PLAYBACK` уже объявлены |

`START_NOT_STICKY` в `onStartCommand` сохраняется: иначе система будет воскрешать сервис после
убийства, прямо против цели задачи.

## Краевые случаи

- **OEM-киллеры** (Xiaomi, Huawei, агрессивный режим Samsung) убивают процесс, не вызывая
  `onTaskRemoved`. Результат тот же — звук глохнет вместе с процессом. Приемлемо.
- **Старт FGS из фона** запрещён на Android 12+, но старт идёт из `MainActivity.onCreate`, когда
  приложение на переднем плане. Ограничение не задевает.
- **Повторный `ACTION_START`** при живом движке — no-op благодаря `engineRunning`.

## Проверка

Автотесты не пишутся: поведение целиком складывается из жизненного цикла Android и нативного
потока, юнит-тесты на моках проверили бы только сами моки. Проверка ручная.

| # | Сценарий | Ожидание |
|---|---|---|
| 1 | Запуск | Звук идёт, нотификация висит |
| 2 | Свернул / выключил экран / открыл другое приложение | Звук идёт |
| 3 | Поворот экрана | Звук не удваивается, `Запуск AudioOut` в логе один раз |
| 4 | Смахнул из недавних | Звук глохнет менее чем за секунду, нотификация исчезает, `adb shell pidof com.example.generator2` пусто |
| 5 | Кнопка «Закрыть» | То же плюс карточка ушла из недавних |
| 6 | Тап по телу нотификации | Открывается приложение, звук не прерывается |
| 7 | Отказ в `POST_NOTIFICATIONS` | Звук есть, нотификации нет, сервис жив |
| 8 | Запуск после «Закрыть» | Работает с нуля, звук есть |

Логи Timber в `onCreate`, `onDestroy`, `onTaskRemoved` сохраняются — они и есть инструмент проверки.
