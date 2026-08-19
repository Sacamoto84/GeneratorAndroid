package com.example.generator2.features.scope

import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import com.example.generator2.Spectrogram
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import timber.log.Timber

enum class OSCILLSYNC {  NONE, R, L }

/**
 * Осциллограф: состояние, каналы данных и маршрутизация аудиопотока.
 *
 * Разметка живёт отдельно — ScopeUi.kt рисует сам экран, ScopePanel.kt
 * панель управления под ним.
 */
class Scope(
    /**
     * Оба канала несут один сигнал.
     *
     * Подставляет AudioMixerPump: осциллограф не знает ни о генераторе, ни о
     * маршрутизации, а на экран к нему приходит уже смикшированный поток.
     * Синхронизация кадра по фронту корректна только когда в обоих ушах одно
     * и то же, то есть генератор в моно И оба маршрута идут с него.
     *
     * Лямбда, а не StateFlow: значение опрашивается на каждом пакете, а
     * захваченная ссылка на конкретный флоу протухла бы при подмене liveData.
     */
    internal val isMonoOut: () -> Boolean = { false }
) {

    // Начальное значение: дальше AudioMixerPump подставит частоту,
    // на которой реально открылся аудиовыход.
    var audioSampleRate = 48000

    /**
     * Используем компонент или нет
     */
    val isUse = MutableStateFlow(true)

    //Режимы отображения каналов на осцилографе
    val isVisibleL = MutableStateFlow(true)  //Отобразить Левый канал
    val isVisibleR = MutableStateFlow(true)  //Отобразить Правый канал
    val isOneTwo = MutableStateFlow(false)   //Комбинация двух каналов или раздельно

    val isPause = MutableStateFlow(false)

    //============== Lissagu ===================
    val isUseLissagu = MutableStateFlow(true)

    /*
       1   |   26 ms  | 38.28 Hz |   1152 |   2304
       2   |   52 ms  | 19.14 Hz |   2304 |   4608
       4   |  104 ms  |  9.57 Hz |   4608 |   9216
       8   |  208 ms  |  4.78 Hz |   9216 |  18432
       16  |  418 ms  |  2.4  Hz |  18432 |  36864
       32  |  836 ms  |  1.2  Hz |  36864 |  73728
       64  |  1.64 s  |  0.6  Hz |  73728 | 147456
       128 |  3.34 s  |  0.3  Hz | 147456 | 294912
       256 |  6.68 s  |  0.15 Hz | 294912 | 589824

     */
    /** Количество пакетов в которое будет упакован выходной канал */
    val compressorCount = mutableFloatStateOf(256f)

    /** Подпись развёртки: целое от единицы и выше, ниже — доля пакета. */
    internal fun sweepLabel(value: Float): String =
        if (value >= 1f) value.toInt().toString()
        else "1/${(1f / value).toInt()}"

    /** Растянуть развёртку, но не длиннее буфера истории. */
    internal fun compressorCountUp() {
        compressorCount.floatValue = (compressorCount.floatValue * 2).coerceAtMost(256f)
    }

    /** Сжать развёртку до одной восьмой пакета — короче сетка не строится. */
    internal fun compressorCountDown() {
        compressorCount.floatValue = (compressorCount.floatValue / 2.0f).coerceAtLeast(0.125f)
    }

    /** ## Выход аудиоданных -> dataRouter */
    val channelAudioOut = Channel<FloatArray>(capacity = 16, BufferOverflow.DROP_OLDEST)

    /** Сжатые данные после компрессора */
    val channelDataStreamOutCompressor = Channel<FloatArray>(capacity = Channel.RENDEZVOUS)

    /** Разрешение на обновление нового кадра осцилографа, признак того что нужно перерисовать */
    val enableOscill = MutableStateFlow(true)

    val enableLissagu = MutableStateFlow(true)

    val deferredOscill = Channel<Int>(capacity = 1, BufferOverflow.DROP_OLDEST) //CompletableDeferred<Long>()
    val deferredLissagu = Channel<Int>(capacity = 1, BufferOverflow.DROP_OLDEST)

    val oscillSync = mutableStateOf(OSCILLSYNC.L)

    //Маршрутизатор аудиоданных живёт здесь, а не в анонимном scope: без ссылки
    //на scope петля не останавливалась никогда, даже после остановки движка
    private val routerScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    private var routerJob: Job? = null

    init {
        Timber.i("!!! init Scope")
        dataRouter()
    }

    /**
     * Остановить маршрутизацию аудиоданных.
     *
     * Пока не вызывается: подача в FFT-петлю привязана к жизни сервиса, и её
     * отключение — отдельная задача про потребителя спектрограммы.
     */
    fun stopDataRouter() {
        routerJob?.cancel()
        routerJob = null
    }

    private fun dataRouter() {

        routerJob?.cancel()

        routerJob = routerScope.launch {

            while (isActive) {
                val buf = channelAudioOut.receive()

                //Передаем FFT порцию данных
                Spectrogram.sentToFloatRingBufferFFT(buf, buf.size, audioSampleRate)

                // Буфер истории считает пакетами и меньше одного не умеет.
                // Развёртки ниже единицы показывают часть пакета, долю
                // отсчитывает уже сетка фосфора.
                NativeFloatDirectBuffer.add(
                    buf, buf.size,
                    compressorCount.floatValue.coerceAtLeast(1f).toInt()
                )

                if (enableOscill.value && !isPause.value)  deferredOscill.send(0)

                if (enableLissagu.value && !isPause.value) deferredLissagu.send(0)

            }
        }
    }

}
