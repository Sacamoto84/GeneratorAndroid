package com.example.generator2.generator

import com.example.generator2.model.itemList
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.flow.MutableStateFlow
import timber.log.Timber

class Generator {

    val liveData: DataLiveData = DataLiveData()

    var itemlistCarrier: ArrayList<itemList> = ArrayList() //Создать список
    var itemlistAM: ArrayList<itemList> = ArrayList()      //Создать список
    var itemlistFM: ArrayList<itemList> = ArrayList()      //Создать список

    val ch1: StructureCh = StructureCh(ch = 0)
    val ch2: StructureCh = StructureCh(ch = 1)

    var sampleRate: Int = 48000

    private val renderChanelL = RenderChannel(liveData)
    private val renderChanelR = RenderChannel(liveData)


    suspend fun renderAudio(numFrames: Int = 1024): Pair<FloatArray, FloatArray> {

        if (numFrames == 0) Timber.e("numFrames == 0")

        val l: FloatArray
        val r: FloatArray

        if (!liveData.mono.value) {

            val job1 = CoroutineScope(Dispatchers.IO).async {
                renderChanelL.renderChanel(ch1, numFrames / 2, sampleRate)
            }

            val job2 = CoroutineScope(Dispatchers.IO).async {
                renderChanelR.renderChanel(ch2, numFrames / 2, sampleRate)
            }

            val results = awaitAll(job1, job2)

            l = results[0]
            r = results[1]

//           l = renderChanelL.renderChanel(ch1, numFrames / 2, sampleRate)
//           r = renderChanelR.renderChanel(ch2, numFrames / 2, sampleRate)

        } else {
            //Mono
            val m = renderChanelL.renderChanel(ch1, numFrames / 2, sampleRate)
            l = m
            r = m
        }

        return Pair(l, r)

    }

    fun createFm(ch: String) {

        //void CreateFM_CH1(void) {
        //    int x, y;
        //    int i = 0;
        //    x = CH1.Carrier_fr - CH1.FM_Dev;
        //    y = CH1.FM_Dev * 2;

        //    for (i = 0; i < 1024; i++)
        //    CH1.buffer_fm[i] = x + (y * CH1.source_buffer_fm[i] / 4095.0F);
        //}

        val carrierFr =
            if (ch == "CH0") liveData.ch1_Carrier_Fr.value else liveData.ch2_Carrier_Fr.value
        val fmDevFr = if (ch == "CH0") liveData.ch1_FM_Dev.value else liveData.ch2_FM_Dev.value
        val x: Int = (carrierFr - fmDevFr).toInt()
        val y: Int = (fmDevFr * 2.0F).toInt()
        val buf = if (ch == "CH0") ch1.buffer_fm else ch2.buffer_fm
        val source = if (ch == "CH0") ch1.source_buffer_fm else ch2.source_buffer_fm
        for (i in 0..1023) {
            buf[i] = (x + (y * source[i] / 4095.0F)).toInt().toShort()
        }
    }


}

data class DataLiveData(

    var ch1_EN: MutableStateFlow<Boolean> = MutableStateFlow(false),              //PR PS PC
    var ch1_Carrier_Filename: MutableStateFlow<String> = MutableStateFlow("Sine"),//PR PS PC
    var ch1_Carrier_Fr: MutableStateFlow<Float> = MutableStateFlow(400.0f),       //PR PS PC //Частота несущей
    var ch1_AM_EN: MutableStateFlow<Boolean> = MutableStateFlow(false),           //PR PS PC
    var ch1_AM_Filename: MutableStateFlow<String> = MutableStateFlow("09_Ramp"),  //PR PS PC
    var ch1_AM_Fr: MutableStateFlow<Float> = MutableStateFlow(8.7f),              //PR PS PC
    var ch1_FM_EN: MutableStateFlow<Boolean> = MutableStateFlow(false),           //PR PS PC
    var ch1_FM_Filename: MutableStateFlow<String> = MutableStateFlow("06_CHIRP"), //PR PS PC
    var ch1_FM_Dev: MutableStateFlow<Float> = MutableStateFlow(1100f),            //PR PS PC //Частота базы
    var ch1_FM_Fr: MutableStateFlow<Float> = MutableStateFlow(5.1f),              //PR PS PC

    var ch2_EN: MutableStateFlow<Boolean> = MutableStateFlow(false),
    var ch2_Carrier_Filename: MutableStateFlow<String> = MutableStateFlow("Sine"), //PR PS PC
    var ch2_Carrier_Fr: MutableStateFlow<Float> = MutableStateFlow(2000.0f),     //PR PS PC Частота несущей
    var ch2_AM_EN: MutableStateFlow<Boolean> = MutableStateFlow(false),          //PR PS PC
    var ch2_AM_Filename: MutableStateFlow<String> = MutableStateFlow("09_Ramp"), //PR PS PC
    var ch2_AM_Fr: MutableStateFlow<Float> = MutableStateFlow(8.7f),             //PR PS PC
    var ch2_FM_EN: MutableStateFlow<Boolean> = MutableStateFlow(false),          //PR PS PC
    var ch2_FM_Filename: MutableStateFlow<String> = MutableStateFlow("06_CHIRP"),//PR PS PC
    var ch2_FM_Dev: MutableStateFlow<Float> = MutableStateFlow(1100f),           //PR PS PC Частота базы
    var ch2_FM_Fr: MutableStateFlow<Float> = MutableStateFlow(5.1f),             //PR PS PC

    var volume0: MutableStateFlow<Float> = MutableStateFlow(1f),              //PR PS PC Используется для AudioDevice = maxVolume0 * currentVolume0
    var volume1: MutableStateFlow<Float> = MutableStateFlow(1f),              //PR PS PC


    var mono: MutableStateFlow<Boolean> = MutableStateFlow(false),            //PR PS PC Режим повторения настроек второго канала с первым
    var invert: MutableStateFlow<Boolean> = MutableStateFlow(false),          //PR PS PC Инверсия сигнала во втором канале, только при моно

    var shuffle: MutableStateFlow<Boolean> = MutableStateFlow(false),         //PR PS PC меняем левый и правый канал в стерео режиме

    var enL: MutableStateFlow<Boolean> = MutableStateFlow(true),              //PR PS PC
    var enR: MutableStateFlow<Boolean> = MutableStateFlow(true),              //PR PS PC

    var maxVolume0: MutableStateFlow<Float> = MutableStateFlow(0.9f),         //PR PS PC JsonVolume максимальная громкость усилителя
    var maxVolume1: MutableStateFlow<Float> = MutableStateFlow(0.9f),         //PR PS PC

    var currentVolume0: MutableStateFlow<Float> = MutableStateFlow(1.0f),     //PR PS PC Громкость канала на регуляторе 0 100 JsonConfig()
    var currentVolume1: MutableStateFlow<Float> = MutableStateFlow(1.0f),     //PR PS PC

    var ch1AmDepth: MutableStateFlow<Float> = MutableStateFlow(1f),           //PR PS PC Глубина AM модуляции
    var ch2AmDepth: MutableStateFlow<Float> = MutableStateFlow(1f),           //PR PS PC Глубина AM модуляции

    //Количество звезд
    val star: MutableStateFlow<Int> = MutableStateFlow(0),                    //PR PS PC

    //Имя текущего пресета
    val presetsName: MutableStateFlow<String> = MutableStateFlow(""),         //PR PS(name) PC

    val ch1FmMin: MutableStateFlow<Float> = MutableStateFlow(1000.0f), //PR PS PC CH1 FM min
    val ch1FmMax: MutableStateFlow<Float> = MutableStateFlow(2000.0f), //PR PS PC CH1 FM max
    val ch2FmMin: MutableStateFlow<Float> = MutableStateFlow(1500.0f), //PR PS PC CH2 FM min
    val ch2FmMax: MutableStateFlow<Float> = MutableStateFlow(2500.0f), //PR PS PC CH2 FM max


    val parameterInt0: MutableStateFlow<Int> = MutableStateFlow(0), //PR PS PC CH1 режим выбора частот FM модуляции 0-обычный 1-минимум макс
    val parameterInt1: MutableStateFlow<Int> = MutableStateFlow(0), //PR PS PC CH2 режим выбора частот FM модуляции 0-обычный 1-минимум макс

)

data class StructureCh(
    var ch: Int = 0, //Номер канала 0 1
    //Буфферы
    var buffer_carrier: ShortArray = ShortArray(1024),
    var buffer_am: ShortArray = ShortArray(1024),
    var buffer_fm: ShortArray = ShortArray(1024),

    var source_buffer_fm: ShortArray = ShortArray(1024), //Используется для перерасчета модуляции

    //Аккумуляторы
    var phase_accumulator_carrier: UInt = 0u,
    var phase_accumulator_am: UInt = 0u,
    var phase_accumulator_fm: UInt = 0u,

    //var mBuffer: FloatArray = FloatArray(4096),

)

