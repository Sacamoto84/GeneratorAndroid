package com.example.generator2

import android.app.ActivityManager
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
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withTimeoutOrNull
import timber.log.Timber
import java.util.concurrent.Executors
import javax.inject.Inject
import kotlin.system.exitProcess

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

    /** Остановка уже идёт. Защита от повторного shutdown из onDestroy. */
    private var shuttingDown = false

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
     * Идемпотентен: защита на случай, если onDestroy придёт по независимому
     * пути (системное уничтожение сервиса).
     */
    private fun shutdown() {
        if (shuttingDown) return
        shuttingDown = true

        Timber.i("SoundService shutdown: остановка движка")

        // Дожидаемся выхода из цикла пампа: он может быть внутри блокирующей
        // записи в AudioTrack длиной до ~200 мс. Освобождать AudioOut раньше
        // выхода нельзя — запись в освобождённый AudioTrack валит процесс.
        val joined = runBlocking {
            withTimeoutOrNull(1500) { serviceJob.cancelAndJoin() }
        }
        if (joined == null)
            Timber.w("Памп не завершился за 1500 мс, продолжаем принудительно")

        audioMixerPump.shutdown()
        Spectrogram.stopFFTLoop()
        audioExecutor.shutdownNow()

        ServiceCompat.stopForeground(this, ServiceCompat.STOP_FOREGROUND_REMOVE)
        engineRunning = false

        Timber.i("SoundService shutdown: движок остановлен")
    }

    /**
     * Убирает карточку приложения из недавних. При смахивании она уже удалена
     * (appTasks пуст) — метод нужен для закрытия из нотификации, иначе
     * карточка останется висеть мёртвой.
     */
    private fun removeTaskFromRecents() {
        try {
            val am = getSystemService(ACTIVITY_SERVICE) as ActivityManager
            am.appTasks.forEach { it.finishAndRemoveTask() }
        } catch (e: Exception) {
            Timber.e(e, "Не удалось убрать задачу из недавних")
        }
    }

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
