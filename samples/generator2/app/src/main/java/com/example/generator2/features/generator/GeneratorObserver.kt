package com.example.generator2.features.generator

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.merge
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Пересчёт таблиц генератора в ответ на изменения [Generator.liveData].
 *
 * Подписки живут в одном job: [start] сначала снимает предыдущие, поэтому
 * повторная инициализация приложения не удваивает обработчики.
 */
@Singleton
class GeneratorObserver @Inject constructor(
    private val gen: Generator
) {

    private companion object {
        /**
         * Пересчёт FM запускается не чаще кадра: загрузка пресета меняет сразу
         * несколько влияющих параметров, а буфер достаточно пересобрать один раз.
         */
        const val FM_DEBOUNCE_MS = 16L
    }

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    private var job: Job? = null

    /**
     * Подписаться на изменения. Повторный вызов пересоздаёт подписки, а не
     * добавляет вторые.
     */
    fun start() {
        job?.cancel()

        job = scope.launch {
            val d = gen.liveData

            //Смена формы волны — перечитать таблицу и отдать её в нативный рендер
            waveform(d.chL_Carrier_Filename, GeneratorCH.CHL, GeneratorMOD.CR)
            waveform(d.chR_Carrier_Filename, GeneratorCH.CHR, GeneratorMOD.CR)
            waveform(d.chL_AM_Filename, GeneratorCH.CHL, GeneratorMOD.AM)
            waveform(d.chR_AM_Filename, GeneratorCH.CHR, GeneratorMOD.AM)
            waveform(d.chL_FM_Filename, GeneratorCH.CHL, GeneratorMOD.FM)
            waveform(d.chR_FM_Filename, GeneratorCH.CHR, GeneratorMOD.FM)
            waveform(d.chL_Master_Filename, GeneratorCH.CHL, GeneratorMOD.MASTER)
            waveform(d.chR_Master_Filename, GeneratorCH.CHR, GeneratorMOD.MASTER)

            waveform(d.chL_Morph_Slot0_Filename, GeneratorCH.CHL, GeneratorMOD.MORPH0)
            waveform(d.chL_Morph_Slot1_Filename, GeneratorCH.CHL, GeneratorMOD.MORPH1)
            waveform(d.chL_Morph_Slot2_Filename, GeneratorCH.CHL, GeneratorMOD.MORPH2)
            waveform(d.chR_Morph_Slot0_Filename, GeneratorCH.CHR, GeneratorMOD.MORPH0)
            waveform(d.chR_Morph_Slot1_Filename, GeneratorCH.CHR, GeneratorMOD.MORPH1)
            waveform(d.chR_Morph_Slot2_Filename, GeneratorCH.CHR, GeneratorMOD.MORPH2)

            //Любой из этих параметров требует пересчёта буфера FM
            fm(
                channel = 0,
                d.chL_FM_Dev, d.chL_Carrier_Fr, d.chLFmMin, d.chLFmMax, d.parameterInt0
            )
            fm(
                channel = 1,
                d.chR_FM_Dev, d.chR_Carrier_Fr, d.chRFmMin, d.chRFmMax, d.parameterInt1
            )
        }

        Timber.i("GeneratorObserver: подписки запущены")
    }

    /**
     * Снять подписки. Нужен, когда движок останавливается.
     */
    fun stop() {
        job?.cancel()
        job = null
        Timber.i("GeneratorObserver: подписки сняты")
    }

    private fun CoroutineScope.waveform(
        filename: StateFlow<String>,
        ch: GeneratorCH,
        mod: GeneratorMOD,
    ) = launch {
        filename.collect { Spinner_Send_Buffer(ch, mod, it, gen) }
    }

    @OptIn(FlowPreview::class)
    private fun CoroutineScope.fm(channel: Int, vararg inputs: Flow<*>) = launch {
        inputs.map { input -> input.map { } }
            .merge()
            .debounce(FM_DEBOUNCE_MS)
            .collect { gen.updateFm(channel) }
    }
}
