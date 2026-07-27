# Управляемый жизненный цикл Foreground Service — план реализации

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Перенести владение аудиодвижком из `MainActivity` в `SoundService`, чтобы звук
продолжался при сворачивании приложения и полностью останавливался при смахивании из недавних или
нажатии кнопки «Закрыть» в нотификации.

**Architecture:** `SoundService` становится `@AndroidEntryPoint` и запускает `AudioMixerPump` в
собственном `CoroutineScope` на отменяемом однопоточном аудио-диспетчере плюс нативный FFT-цикл.
`MainActivity` больше не создаёт потоков — только стартует сервис и рисует UI. Смахивание из
недавних (`onTaskRemoved`) и `ACTION_CLOSE` из нотификации сходятся в один метод `closeApp()`:
остановка пампа, освобождение `AudioOut` и ExoPlayer, `stopFFTLoop()`, снятие FGS, удаление задачи
из недавних, `exitProcess(0)`.

**Tech Stack:** Kotlin, Coroutines, Hilt, Media3 ExoPlayer, AudioTrack, JNI/pthread (C++), Android
Foreground Service (`mediaPlayback`), minSdk 26 / targetSdk 37.

**Спека:** [2026-07-27-foreground-service-lifecycle-design.md](../specs/2026-07-27-foreground-service-lifecycle-design.md)

---

## Про тесты

Автотестов в этом плане нет намеренно — так решено в спеке. Поведение целиком складывается из
жизненного цикла Android-сервиса и нативного pthread; юнит-тесты на моках проверили бы только сами
моки. Каталог `app/src/test/java` в проекте пуст, тестовой инфраструктуры нет.

Вместо «красный тест → зелёный тест» каждая задача проверяется двумя шагами:

1. `./gradlew :app:assembleDebug` — сборка проходит;
2. конкретная проверка на устройстве с чтением logcat.

Задача 6 — сквозной ручной чеклист по всем восьми сценариям из спеки.

Все команды выполняются из `G:/GeneratorAndroid/samples/generator2`.

---

## Структура файлов

| Файл | Ответственность после изменений |
|---|---|
| `app/src/main/cpp/spectrogram/jniFFT.cpp` | Нативный FFT-цикл: старт **и остановка** рабочего pthread |
| `app/src/main/java/com/example/generator2/Spectrogram.kt` | JNI-фасад спектрограммы, добавляется `stopFFTLoop()` |
| `app/src/main/java/com/example/generator2/features/audio/AudioMixerPump.kt` | Аудиодвижок: отменяемый цикл смешивания + `shutdown()` для освобождения железа |
| `app/src/main/java/com/example/generator2/SoundService.kt` | Владелец жизненного цикла звука: запуск движка, нотификация, единственный путь остановки |
| `app/src/main/java/com/example/generator2/MainActivity.kt` | Только UI и старт сервиса. Никаких потоков и нативных вызовов |

`AndroidManifest.xml` и `di/module.kt` не меняются.

---

## Task 1: Остановка нативного FFT-цикла

Механизм выхода в C++ уже есть (`context1.exit` + семафор `headwriteprotect`), наружу он не
экспортирован. Задача самодостаточна: добавляет неиспользуемую пока функцию, ничего не ломает.

**Files:**
- Modify: `app/src/main/cpp/spectrogram/jniFFT.cpp`
- Modify: `app/src/main/java/com/example/generator2/Spectrogram.kt`

- [ ] **Step 1: Добавить `stopFFTLoop()` в jniFFT.cpp**

В `app/src/main/cpp/spectrogram/jniFFT.cpp` найти конец функции `initFTTLoop()` — она заканчивается
строками:

```cpp
    isInitialized = true;
    LOGE("!!! initFTTLoop isInitialized = true");
}
```

Сразу после закрывающей скобки вставить:

```cpp
/**
 * Остановка рабочего потока FFT. Идемпотентна: повторный вызов при
 * isInitialized == false завершается сразу.
 *
 * Семафор намеренно НЕ уничтожается: sentToFloatRingBufferFFT может
 * сделать sem_post уже после join, и это должно остаться безопасным.
 */
void stopFFTLoop() {
    if (!isInitialized)
        return;

    LOGE("!!! stopFFTLoop");

    context1.exit = true;
    sem_post(&context1.headwriteprotect);
    pthread_join(context1.worker, nullptr);
    isInitialized = false;

    LOGE("!!! stopFFTLoop joined");
}
```

- [ ] **Step 2: Экспортировать функцию в JNI**

В том же файле найти существующий экспорт:

```cpp
extern "C"
JNIEXPORT void JNICALL
Java_com_example_generator2_Spectrogram_startFFTLoop(JNIEnv *env, jobject) {
    initFTTLoop();
}
```

Сразу после него вставить:

```cpp
/**
 * Остановка потока FFT, вызывается при закрытии приложения
 */
extern "C"
JNIEXPORT void JNICALL
Java_com_example_generator2_Spectrogram_stopFFTLoop(JNIEnv *env, jobject) {
    stopFFTLoop();
}
```

- [ ] **Step 3: Объявить `external fun` в Spectrogram.kt**

В `app/src/main/java/com/example/generator2/Spectrogram.kt` найти:

```kotlin
    /**
     * Запуск потока для работы с FFT, запускается один раз
     */
    external fun startFFTLoop()
```

Заменить на:

```kotlin
    /**
     * Запуск потока для работы с FFT, запускается один раз
     */
    external fun startFFTLoop()

    /**
     * Остановка потока FFT. Идемпотентна — повторный вызов безопасен.
     */
    external fun stopFFTLoop()
```

- [ ] **Step 4: Собрать**

Run: `./gradlew :app:assembleDebug`
Expected: `BUILD SUCCESSFUL`. Если линковка ругается `undefined reference` — сигнатура JNI-функции
не совпадает с именем пакета/класса, проверить `Java_com_example_generator2_Spectrogram_stopFFTLoop`
посимвольно.

- [ ] **Step 5: Коммит**

```bash
git add app/src/main/cpp/spectrogram/jniFFT.cpp app/src/main/java/com/example/generator2/Spectrogram.kt
git commit -m "feat(fft): экспорт остановки нативного FFT-цикла"
```

---

## Task 2: Отменяемый AudioMixerPump

Сейчас `run()` — неотменяемый `while (true)`. Без этой задачи `serviceScope.cancel()` не даст
ничего.

**Files:**
- Modify: `app/src/main/java/com/example/generator2/features/audio/AudioMixerPump.kt`

- [ ] **Step 1: Добавить импорты**

В `AudioMixerPump.kt` найти блок импортов и добавить к нему три строки:

```kotlin
import kotlinx.coroutines.delay
import kotlinx.coroutines.ensureActive
import kotlin.coroutines.coroutineContext
```

(`kotlinx.coroutines.DelicateCoroutinesApi`, `Dispatchers`, `GlobalScope`, `async` и остальные
существующие импорты остаются на месте.)

- [ ] **Step 2: Сделать отменяемым стартовый busy-wait**

Найти в `run()`:

```kotlin
        while (!init) {
            init = try {
                exoplayer.player
                true
            } catch (e: UninitializedPropertyAccessException) {
                false
            }
        }
```

Заменить на:

```kotlin
        while (!init) {
            coroutineContext.ensureActive()
            init = try {
                exoplayer.player
                true
            } catch (e: UninitializedPropertyAccessException) {
                false
            }
            if (!init) delay(1)
        }
```

Без `ensureActive()` и `delay(1)` отмена во время старта подвесила бы поток навсегда: busy-loop не
даёт корутине точки отмены.

- [ ] **Step 3: Обернуть главный цикл в try/finally и добавить точку отмены**

Найти начало главного цикла:

```kotlin
        while (true) {


            if (exoplayer.isPlayingD.value) {
```

Заменить на:

```kotlin
        try {

        while (true) {

            coroutineContext.ensureActive()

            if (exoplayer.isPlayingD.value) {
```

Затем найти конец `run()` — закрытие цикла и функции после записи в аудиоустройство:

```kotlin
            // LRLRLR
            audioOut.out?.write(v, 0, v.size, WRITE_BLOCKING)
            //───────────────────────────────────────────────┘

        }



    }
```

Заменить на:

```kotlin
            // LRLRLR
            audioOut.out?.write(v, 0, v.size, WRITE_BLOCKING)
            //───────────────────────────────────────────────┘

        }

        } finally {
            Timber.w("AudioMixerPump: цикл завершён, освобождаем AudioOut")
            audioOut.destroy()
        }

    }
```

`audioOut.out?.write(..., WRITE_BLOCKING)` отмену не чувствует, но возвращается за время буфера
(порядка 200 мс), после чего `ensureActive()` на следующей итерации бросает
`CancellationException` и управление уходит в `finally`.

- [ ] **Step 4: Добавить метод `shutdown()`**

Найти конец функции `run()` (закрывающая скобка, добавленная в предыдущем шаге) и следующее за ней:

```kotlin
    @OptIn(DelicateCoroutinesApi::class)
    suspend fun initializationGen() {
```

Между ними вставить:

```kotlin
    /**
     * Освобождение аудиожелеза. Вызывается ТОЛЬКО из главного потока и ТОЛЬКО
     * после того, как корутина run() завершилась — иначе памп продолжит писать
     * в уже освобождённый AudioTrack.
     *
     * ExoPlayer.release() обязан выполняться на том же потоке, где плеер был
     * создан (главный), поэтому SoundService зовёт shutdown() из onDestroy /
     * обработчика ACTION_CLOSE, а не из корутины пампа.
     *
     * Идемпотентен: AudioOut.destroy() обнуляет out, повторный вызов — no-op.
     */
    fun shutdown() {
        Timber.w("AudioMixerPump shutdown")

        audioOut.destroy()

        try {
            exoplayer.player.release()
        } catch (e: Exception) {
            Timber.e(e, "Ошибка освобождения ExoPlayer")
        }
    }

```

- [ ] **Step 5: Собрать**

Run: `./gradlew :app:assembleDebug`
Expected: `BUILD SUCCESSFUL`. Приложение на этом шаге ведёт себя как раньше — `shutdown()` пока
никто не зовёт.

- [ ] **Step 6: Коммит**

```bash
git add app/src/main/java/com/example/generator2/features/audio/AudioMixerPump.kt
git commit -m "feat(звук): отменяемый цикл AudioMixerPump и освобождение железа"
```

---

## Task 3: SoundService владеет движком

Ключевая задача. Сервис начинает запускать памп и FFT, `MainActivity` перестаёт это делать. Оба
файла правятся в одном коммите: если развести по разным, между ними получится состояние с двумя
пампами или без единого.

**Files:**
- Modify: `app/src/main/java/com/example/generator2/SoundService.kt` (полная замена)
- Modify: `app/src/main/java/com/example/generator2/MainActivity.kt:217-243` и `:293-324`

- [ ] **Step 1: Переписать SoundService.kt**

Полностью заменить содержимое `app/src/main/java/com/example/generator2/SoundService.kt` на:

```kotlin
package com.example.generator2

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Intent
import android.content.pm.ServiceInfo
import android.os.IBinder
import android.os.Process
import androidx.core.app.NotificationCompat
import androidx.core.app.ServiceCompat
import com.example.generator2.features.audio.AudioMixerPump
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.asCoroutineDispatcher
import kotlinx.coroutines.launch
import timber.log.Timber
import java.util.concurrent.Executors
import javax.inject.Inject

/**
 * Владелец жизненного цикла звука.
 *
 * Foreground service нужен ровно для одного: не дать системе убить процесс,
 * пока идёт генерация. Звук живёт здесь, а не в MainActivity, поэтому
 * закрытие приложения гарантированно глушит его.
 */
@AndroidEntryPoint
@androidx.media3.common.util.UnstableApi
class SoundService : Service() {

    companion object {
        const val CHANNEL_ID = "AudioOutServiceChannel"
        const val NOTIFICATION_ID = 1

        const val ACTION_START = "com.example.generator2.action.START"
    }

    @Inject
    lateinit var audioMixerPump: AudioMixerPump

    /**
     * Однопоточный аудио-диспетчер с приоритетом THREAD_PRIORITY_AUDIO.
     * В отличие от прежнего Thread { runBlocking { ... } } он отменяем.
     */
    private val audioExecutor = Executors.newSingleThreadExecutor { runnable ->
        Thread({
            Process.setThreadPriority(Process.THREAD_PRIORITY_AUDIO)
            runnable.run()
        }, "AudioPump")
    }

    private val serviceJob = SupervisorJob()
    private val serviceScope = CoroutineScope(serviceJob + audioExecutor.asCoroutineDispatcher())

    /** Движок запущен. Защита от второго пампа при повторном onStartCommand. */
    private var engineRunning = false

    override fun onCreate() {
        super.onCreate()
        Timber.i("SoundService onCreate")
        createNotificationChannel()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {

        // startForeground строго первым: Android 12+ бросает
        // ForegroundServiceDidNotStartInTimeException, если между
        // startForegroundService и startForeground прошло больше 5 секунд,
        // а инициализация пампа ждёт ExoPlayer.
        ServiceCompat.startForeground(
            this,
            NOTIFICATION_ID,
            createNotification(),
            ServiceInfo.FOREGROUND_SERVICE_TYPE_MEDIA_PLAYBACK
        )

        if (!engineRunning) {
            engineRunning = true
            Timber.i("SoundService: запуск аудиодвижка")

            Spectrogram.startFFTLoop()

            serviceScope.launch {
                audioMixerPump.run()
            }
        } else {
            Timber.i("SoundService: движок уже работает, повторный старт пропущен")
        }

        return START_NOT_STICKY
    }

    override fun onDestroy() {
        Timber.i("SoundService onDestroy")
        super.onDestroy()
    }

    override fun onBind(intent: Intent?): IBinder? = null

    private fun createNotification(): Notification {
        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("Генератор")
            .setContentText("Генерация звука активна")
            .setSmallIcon(android.R.drawable.ic_media_play)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setOngoing(true)
            .setSilent(true)
            .build()
    }

    private fun createNotificationChannel() {
        val channel = NotificationChannel(
            CHANNEL_ID,
            "AudioOut Service",
            NotificationManager.IMPORTANCE_LOW
        )
        val manager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        manager.createNotificationChannel(channel)
    }
}
```

- [ ] **Step 2: Убрать запуск потоков из MainActivity.onCreate**

В `app/src/main/java/com/example/generator2/MainActivity.kt` найти в `onCreate`:

```kotlin
        // Запускаем корутину в потоке с высоким приоритетом
        val highPriorityThread = Thread {
            runBlocking {
                val highPriorityCoroutine = launch(Dispatchers.Default) {
                    android.os.Process.setThreadPriority(android.os.Process.THREAD_PRIORITY_AUDIO)
                    audioMixerPump.run()
                }
                highPriorityCoroutine.join()
            }
        }
        highPriorityThread.start()
```

Удалить целиком.

Затем найти ниже:

```kotlin
        Spectrogram.startFFTLoop()

        startForegroundService()
```

Заменить на:

```kotlin
        startSoundService()
```

`Spectrogram.startFFTLoop()` теперь вызывает сервис — здесь он привёл бы к двойной инициализации.

- [ ] **Step 3: Переписать запуск сервиса и работу с разрешением**

В том же файле найти метод `startForegroundService()` и лаунчер разрешения целиком:

```kotlin
    private fun startForegroundService() {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(
                    this,
                    POST_NOTIFICATIONS
                ) == PackageManager.PERMISSION_GRANTED
            ) {
                val intent = Intent(this, SoundService::class.java)
                startForegroundService(intent)
            } else {
                Timber.w("Notification permission not granted: Requesting permission...")
                // Запрашиваем разрешение через launcher
                requestPermissionLauncher.launch(POST_NOTIFICATIONS)
            }
        } else {
            val intent = Intent(this, SoundService::class.java)
            startForegroundService(intent)
        }
    }

    // Создаем launcher для запроса разрешений
    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            // Разрешение предоставлено, запускаем сервис
            val intent = Intent(this, SoundService::class.java)
            startForegroundService(intent)
        } else {
            Timber.w("Notification permission denied: Foreground service cannot start")
        }
    }
```

Заменить на:

```kotlin
    /**
     * Старт сервиса со звуком.
     *
     * POST_NOTIFICATIONS на запуск foreground service не влияет: при отказе
     * сервис работает, просто нотификация не отображается. Поэтому старт
     * безусловный, а разрешение запрашивается отдельно и только ради
     * видимости нотификации с кнопкой «Закрыть».
     */
    private fun startSoundService() {
        val intent = Intent(this, SoundService::class.java).apply {
            action = SoundService.ACTION_START
        }
        startForegroundService(intent)

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU &&
            ContextCompat.checkSelfPermission(this, POST_NOTIFICATIONS)
            != PackageManager.PERMISSION_GRANTED
        ) {
            Timber.w("POST_NOTIFICATIONS не выдано: звук работает, нотификация не показывается")
            requestPermissionLauncher.launch(POST_NOTIFICATIONS)
        }
    }

    // Лаунчер запроса разрешения на нотификации
    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            // Сервис уже работает; повторный ACTION_START перевыставляет
            // нотификацию, которая до выдачи разрешения была подавлена.
            val intent = Intent(this, SoundService::class.java).apply {
                action = SoundService.ACTION_START
            }
            startForegroundService(intent)
        } else {
            Timber.w("POST_NOTIFICATIONS отклонено: кнопка «Закрыть» недоступна")
        }
    }
```

- [ ] **Step 4: Собрать**

Run: `./gradlew :app:assembleDebug`
Expected: `BUILD SUCCESSFUL`. Если компилятор ругается на неиспользуемые импорты `runBlocking`,
`Dispatchers`, `launch` в `MainActivity.kt` — это предупреждения, оставить как есть (файл содержит
много закомментированного кода, чистка импортов не входит в задачу).

- [ ] **Step 5: Проверить на устройстве**

```bash
./gradlew :app:installDebug
```

Запустить приложение, затем:

```bash
adb logcat -d | grep -E "SoundService|Запуск AudioOut|initFTTLoop"
```

Expected: `SoundService onCreate`, `SoundService: запуск аудиодвижка`, `Запуск AudioOut` **ровно
один раз**, `!!! initFTTLoop`. Звук идёт.

Повернуть экран, снова прочитать лог: `SoundService: движок уже работает, повторный старт пропущен`,
второго `Запуск AudioOut` нет. Это чинит существующий баг с удвоением звука при повороте.

- [ ] **Step 6: Коммит**

```bash
git add app/src/main/java/com/example/generator2/SoundService.kt app/src/main/java/com/example/generator2/MainActivity.kt
git commit -m "feat(сервис): SoundService владеет аудиодвижком"
```

---

## Task 4: Остановка при смахивании из недавних

**Files:**
- Modify: `app/src/main/java/com/example/generator2/SoundService.kt`

- [ ] **Step 1: Добавить импорты**

В `SoundService.kt` к существующим импортам добавить:

```kotlin
import android.app.ActivityManager
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withTimeoutOrNull
import kotlin.system.exitProcess
```

- [ ] **Step 2: Добавить флаг защиты от повторной остановки**

Найти:

```kotlin
    /** Движок запущен. Защита от второго пампа при повторном onStartCommand. */
    private var engineRunning = false
```

Заменить на:

```kotlin
    /** Движок запущен. Защита от второго пампа при повторном onStartCommand. */
    private var engineRunning = false

    /** Остановка уже идёт. Защита от повторного shutdown из onDestroy. */
    private var shuttingDown = false
```

- [ ] **Step 3: Добавить onTaskRemoved и методы остановки**

Найти:

```kotlin
    override fun onDestroy() {
        Timber.i("SoundService onDestroy")
        super.onDestroy()
    }

    override fun onBind(intent: Intent?): IBinder? = null
```

Заменить на:

```kotlin
    /**
     * Пользователь смахнул приложение из недавних. Foreground service
     * пережил бы это и продолжил играть — именно поэтому раньше звук
     * оставался вечным.
     */
    override fun onTaskRemoved(rootIntent: Intent?) {
        Timber.i("SoundService onTaskRemoved: закрываем приложение")
        super.onTaskRemoved(rootIntent)
        closeApp()
    }

    override fun onDestroy() {
        Timber.i("SoundService onDestroy")
        shutdown()
        super.onDestroy()
    }

    override fun onBind(intent: Intent?): IBinder? = null

    /**
     * Полное закрытие: глушим звук, убираем нотификацию и карточку из
     * недавних, уводим процесс.
     *
     * exitProcess нужен потому, что Hilt-синглтоны переживают уничтожение
     * сервиса: без него AudioMixerPump остался бы в процессе с уничтоженным
     * AudioOut и освобождённым ExoPlayer, и следующий запуск получил бы
     * поломанное состояние.
     */
    private fun closeApp() {
        shutdown()
        removeTaskFromRecents()
        stopSelf()
        exitProcess(0)
    }

    /**
     * Остановка движка. Порядок обязателен: сначала памп, потом FFT —
     * иначе sentToFloatRingBufferFFT сделает sem_post в уже присоединённый
     * поток.
     *
     * Идемпотентен: onDestroy вызовет его повторно после closeApp.
     */
    private fun shutdown() {
        if (shuttingDown) return
        shuttingDown = true

        Timber.i("SoundService shutdown: остановка движка")

        // Дожидаемся выхода из цикла пампа: он может быть внутри блокирующей
        // записи в AudioTrack длиной до ~200 мс. Освобождать AudioOut раньше
        // выхода нельзя — запись в освобождённый AudioTrack валит процесс.
        runBlocking {
            withTimeoutOrNull(1500) { serviceJob.cancelAndJoin() }
        }

        audioMixerPump.shutdown()
        Spectrogram.stopFFTLoop()
        audioExecutor.shutdownNow()

        ServiceCompat.stopForeground(this, ServiceCompat.STOP_FOREGROUND_REMOVE)
        engineRunning = false

        Timber.i("SoundService shutdown: движок остановлен")
    }

    /** Убирает карточку приложения из недавних, иначе она останется мёртвой. */
    private fun removeTaskFromRecents() {
        try {
            val am = getSystemService(ACTIVITY_SERVICE) as ActivityManager
            am.appTasks.forEach { it.finishAndRemoveTask() }
        } catch (e: Exception) {
            Timber.e(e, "Не удалось убрать задачу из недавних")
        }
    }
```

- [ ] **Step 4: Собрать и установить**

Run: `./gradlew :app:installDebug`
Expected: `BUILD SUCCESSFUL`

- [ ] **Step 5: Проверить остановку на устройстве**

Запустить приложение, убедиться что звук идёт. Смахнуть из недавних. Затем:

```bash
adb shell pidof com.example.generator2
```

Expected: пустой вывод. Звук пропал менее чем за секунду, нотификация исчезла.

Лог до смахивания смотреть так (logcat после смерти процесса очищается не сразу):

```bash
adb logcat -d | grep -E "onTaskRemoved|shutdown|stopFFTLoop"
```

Expected: `SoundService onTaskRemoved`, `остановка движка`, `AudioMixerPump: цикл завершён`,
`!!! stopFFTLoop joined`, `движок остановлен`.

Если процесс остался жив — значит `runBlocking` завис на `cancelAndJoin`; проверить, что Task 2
шаг 2 (`ensureActive()` в стартовом busy-wait) реально применён.

- [ ] **Step 6: Коммит**

```bash
git add app/src/main/java/com/example/generator2/SoundService.kt
git commit -m "feat(сервис): остановка звука при удалении задачи из недавних"
```

---

## Task 5: Кнопка «Закрыть» и переход в приложение по тапу

**Files:**
- Modify: `app/src/main/java/com/example/generator2/SoundService.kt`

- [ ] **Step 1: Добавить импорт PendingIntent**

В `SoundService.kt` к импортам добавить:

```kotlin
import android.app.PendingIntent
```

- [ ] **Step 2: Объявить ACTION_CLOSE**

Найти:

```kotlin
        const val ACTION_START = "com.example.generator2.action.START"
```

Заменить на:

```kotlin
        const val ACTION_START = "com.example.generator2.action.START"
        const val ACTION_CLOSE = "com.example.generator2.action.CLOSE"
```

- [ ] **Step 3: Обработать ACTION_CLOSE в onStartCommand**

Найти начало `onStartCommand`:

```kotlin
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {

        // startForeground строго первым: Android 12+ бросает
```

Заменить на:

```kotlin
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {

        if (intent?.action == ACTION_CLOSE) {
            Timber.i("SoundService: нажата кнопка «Закрыть»")
            closeApp()
            return START_NOT_STICKY
        }

        // startForeground строго первым: Android 12+ бросает
```

- [ ] **Step 4: Добавить в нотификацию тап и кнопку**

Найти метод `createNotification()`:

```kotlin
    private fun createNotification(): Notification {
        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("Генератор")
            .setContentText("Генерация звука активна")
            .setSmallIcon(android.R.drawable.ic_media_play)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setOngoing(true)
            .setSilent(true)
            .build()
    }
```

Заменить на:

```kotlin
    private fun createNotification(): Notification {

        val contentIntent = PendingIntent.getActivity(
            this,
            0,
            Intent(this, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            },
            PendingIntent.FLAG_IMMUTABLE
        )

        val closeIntent = PendingIntent.getService(
            this,
            1,
            Intent(this, SoundService::class.java).apply { action = ACTION_CLOSE },
            PendingIntent.FLAG_IMMUTABLE
        )

        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("Генератор")
            .setContentText("Генерация звука активна")
            .setSmallIcon(android.R.drawable.ic_media_play)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setOngoing(true)
            .setSilent(true)
            .setContentIntent(contentIntent)
            .addAction(0, "Закрыть", closeIntent)
            .build()
    }
```

Иконка экшна — `0`: в Android 7+ иконки в кнопках нотификации не отображаются, лишний ресурс не
нужен. `FLAG_IMMUTABLE` обязателен начиная с Android 12, иначе `PendingIntent` бросит исключение.

- [ ] **Step 5: Собрать и установить**

Run: `./gradlew :app:installDebug`
Expected: `BUILD SUCCESSFUL`

- [ ] **Step 6: Проверить кнопку на устройстве**

Запустить приложение, свернуть, раскрыть шторку. В нотификации «Генератор» видна кнопка «ЗАКРЫТЬ».

Тапнуть по телу нотификации — открывается приложение, звук не прерывается.

Снова свернуть, нажать «ЗАКРЫТЬ»:

```bash
adb shell pidof com.example.generator2
```

Expected: пустой вывод, звук пропал, нотификация исчезла, карточки в недавних нет.

- [ ] **Step 7: Коммит**

```bash
git add app/src/main/java/com/example/generator2/SoundService.kt
git commit -m "feat(сервис): кнопка «Закрыть» и переход в приложение из нотификации"
```

---

## Task 6: Сквозная ручная проверка

Восемь сценариев из спеки, прогоняются подряд на одном устройстве.

**Files:** нет изменений кода, если все сценарии проходят.

- [ ] **Step 1: Установить свежую сборку**

```bash
./gradlew :app:installDebug
```

- [ ] **Step 2: Прогнать чеклист**

| # | Сценарий | Ожидание |
|---|---|---|
| 1 | Запуск приложения | Звук идёт, нотификация висит |
| 2 | Свернуть, выключить экран, открыть другое приложение | Звук идёт |
| 3 | Поворот экрана | Звук не удваивается, `Запуск AudioOut` в логе один раз |
| 4 | Смахнуть из недавних | Звук глохнет меньше чем за секунду, нотификация исчезла, `adb shell pidof com.example.generator2` пусто |
| 5 | Кнопка «Закрыть» | То же плюс карточка ушла из недавних |
| 6 | Тап по телу нотификации | Открывается приложение, звук не прерывается |
| 7 | Отказ в `POST_NOTIFICATIONS` | Звук есть, нотификации нет, сервис жив |
| 8 | Запуск после «Закрыть» | Работает с нуля, звук есть |

Сценарий 7 проверяется так:

```bash
adb shell pm revoke com.example.generator2 android.permission.POST_NOTIFICATIONS
adb shell am force-stop com.example.generator2
```

Запустить приложение, отклонить запрос разрешения. Звук должен идти. Проверить, что сервис жив:

```bash
adb shell dumpsys activity services com.example.generator2 | grep SoundService
```

Expected: сервис в списке. Затем вернуть разрешение:

```bash
adb shell pm grant com.example.generator2 android.permission.POST_NOTIFICATIONS
```

- [ ] **Step 3: Коммит (только если что-то чинилось)**

Если все восемь сценариев прошли — коммитить нечего, задача закрыта. Если пришлось править код,
коммит с описанием конкретной правки:

```bash
git add -A
git commit -m "fix(сервис): <что именно исправлено по итогам проверки>"
```

---

## Что НЕ входит в план

- Кнопка «Стоп» без выхода из приложения и кнопка «Пуск» в UI — отвергнуты в брейншторме, одна
  кнопка «Закрыть».
- `MediaSessionService`, управление с наушников и экрана блокировки — отдельный продукт.
- Пересоздание `AudioOut` и ExoPlayer для повторного запуска в живом процессе — не нужно, процесс
  уходит через `exitProcess(0)`.
- Чистка неиспользуемых импортов и закомментированного кода в `MainActivity.kt` — не относится к
  задаче.
- `android:configChanges` для `MainActivity` — удвоение звука при повороте чинится флагом
  `engineRunning`, менять поведение Activity незачем.
