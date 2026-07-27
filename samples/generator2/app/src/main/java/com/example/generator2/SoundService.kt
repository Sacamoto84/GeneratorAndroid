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
