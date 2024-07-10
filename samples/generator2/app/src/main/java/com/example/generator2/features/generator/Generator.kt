package com.example.generator2.features.generator

import com.example.generator2.model.itemList
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

    var zeroBufferSize = 1024
    var zeroBuffer = FloatArray(zeroBufferSize)

    fun renderAudio(numFrames: Int = 1024): Pair<FloatArray, FloatArray> {

        if (numFrames == 0) Timber.e("numFrames == 0")

        // val startTime = System.nanoTime()
        //  val l = FloatArray(numFrames / 2)
        //  val r = FloatArray(numFrames / 2)

        //val startTime = System.nanoTime()

        //ret
        //val endTime = System.nanoTime()
        //val duration = endTime - startTime
        //println("Time taken to allocate ret: ${duration/1000} us")
        val l: FloatArray
        val r: FloatArray

        if (!liveData.mono.value) {

            l = if (liveData.ch1_EN.value)
                RenderChannel().renderChanel(liveData, ch1, numFrames / 2, sampleRate)
            else {
                if (numFrames / 2 != zeroBufferSize)
                {
                    zeroBufferSize = numFrames / 2
                    zeroBuffer = FloatArray(zeroBufferSize)
                }
                zeroBuffer
            }

            r = if (liveData.ch2_EN.value)
                RenderChannel().renderChanel(liveData, ch2, numFrames / 2, sampleRate)

            else {

                if (numFrames / 2 != zeroBufferSize)
                {
                    zeroBufferSize = numFrames / 2
                    zeroBuffer = FloatArray(zeroBufferSize)
                }
                zeroBuffer

            }
        } else {
            //Mono
            val m = RenderChannel().renderChanel(liveData, ch1, numFrames / 2, sampleRate)
            l = m
            r = m
        }
        return Pair(l, r)
    }

    fun createFm(ch: Int) {
        val carrierFr = if (ch == 0) liveData.ch1_Carrier_Fr.value else liveData.ch2_Carrier_Fr.value
        val fmDevFr = if (ch == 0) liveData.ch1_FM_Dev.value else liveData.ch2_FM_Dev.value
        val buf = if (ch == 0) ch1.calculate_buffer_fm else ch2.calculate_buffer_fm
        val source = if (ch == 0) ch1.buffer_fm else ch2.buffer_fm

        for (i in 0..1023) {
            buf[i] = (carrierFr + (fmDevFr * source[i]))
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

    var count: Int = 0

)

data class StructureCh(
    var ch: Int = 0, //Номер канала 0 1

    //Буфферы

    var buffer_carrier: FloatArray = FloatArray(1024), //-1..1

    //0..4095 -> 0..1
    var buffer_am: FloatArray = FloatArray(1024),      //0..1

    var buffer_fm: FloatArray = FloatArray(1024),      //-1..1

    //Содержит частоты которые уже промодулированы
    var calculate_buffer_fm: FloatArray = FloatArray(1024), //100..10000//Используется для перерасчета модуляции

    //var buffer_carrier_direct: FloatBuffer = ByteBuffer.allocateDirect(4096).order(ByteOrder.nativeOrder()).asFloatBuffer(),
    //var buffer_am_direct: FloatBuffer = ByteBuffer.allocateDirect(4096).order(ByteOrder.nativeOrder()).asFloatBuffer(),
    //var buffer_fm_direct: FloatBuffer = ByteBuffer.allocateDirect(4096).order(ByteOrder.nativeOrder()).asFloatBuffer(),

    //Аккумуляторы
    var phase_accumulator_carrier: UInt = 0u,
    var phase_accumulator_am: UInt = 0u,
    var phase_accumulator_fm: UInt = 0u,

    //var mBuffer: FloatArray = FloatArray(4096),

)

