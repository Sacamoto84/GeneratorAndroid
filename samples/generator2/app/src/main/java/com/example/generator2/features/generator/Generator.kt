package com.example.generator2.features.generator

import com.example.generator2.model.itemList
import kotlinx.coroutines.flow.MutableStateFlow
import timber.log.Timber

//Минимально допустимая мгновенная частота несущей при FM модуляции, Гц
const val FM_FREQ_MIN = 50f

class Generator {


    val liveData: DataLiveData = DataLiveData()

    var itemlistCarrier: ArrayList<itemList> = ArrayList() //Создать список
    var itemlistAM: ArrayList<itemList> = ArrayList()      //Создать список
    var itemlistFM: ArrayList<itemList> = ArrayList()      //Создать список

    val chL: StructureCh = StructureCh(ch = 0)
    val chR: StructureCh = StructureCh(ch = 1)

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

            l = if (liveData.chL_EN.value)
                RenderChannel().renderChanel(liveData, chL, numFrames / 2, sampleRate)
            else {
                if (numFrames / 2 != zeroBufferSize)
                {
                    zeroBufferSize = numFrames / 2
                    zeroBuffer = FloatArray(zeroBufferSize)
                }
                zeroBuffer
            }

            r = if (liveData.chR_EN.value)
                RenderChannel().renderChanel(liveData, chR, numFrames / 2, sampleRate)

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
            val m = RenderChannel().renderChanel(liveData, chL, numFrames / 2, sampleRate)
            l = m
            r = m
        }
        return Pair(l, r)
    }

    /**
     * Максимально допустимая девиация для режима 0, при которой
     * несущая не опускается ниже [FM_FREQ_MIN]
     */
    fun fmDevLimit(ch: Int): Float {
        val carrierFr = if (ch == 0) liveData.chL_Carrier_Fr.value else liveData.chR_Carrier_Fr.value
        return (carrierFr - FM_FREQ_MIN).coerceAtLeast(0f)
    }

    fun createFm(ch: Int) {
        val buf = if (ch == 0) chL.calculate_buffer_fm else chR.calculate_buffer_fm
        val source = if (ch == 0) chL.buffer_fm else chR.buffer_fm

        //Режим выбора частот: 0 - несущая ± девиация, 1 - минимум/максимум
        val mode = if (ch == 0) liveData.parameterInt0.value else liveData.parameterInt1.value

        val center: Float
        val deviation: Float

        if (mode == 0) {
            center = if (ch == 0) liveData.chL_Carrier_Fr.value else liveData.chR_Carrier_Fr.value
            val fmDevFr = if (ch == 0) liveData.chL_FM_Dev.value else liveData.chR_FM_Dev.value
            deviation = fmDevFr.coerceAtMost(fmDevLimit(ch))
        } else {
            val min = (if (ch == 0) liveData.chLFmMin.value else liveData.chRFmMin.value)
                .coerceAtLeast(FM_FREQ_MIN)
            val max = (if (ch == 0) liveData.chLFmMax.value else liveData.chRFmMax.value)
                .coerceAtLeast(min)
            center = (max + min) / 2f
            deviation = (max - min) / 2f
        }

        for (i in 0..1023) {
            buf[i] = (center + (deviation * source[i]))
        }
    }

    /**
     * Переключение режима задания частот FM с пересчётом параметров так,
     * чтобы диапазон частот (а значит и звучание) не изменился
     */
    fun switchFmMode(ch: Int) {
        val mode = if (ch == 0) liveData.parameterInt0 else liveData.parameterInt1

        if (mode.value == 0) {
            //несущая ± девиация -> минимум/максимум
            val carrier =
                if (ch == 0) liveData.chL_Carrier_Fr.value else liveData.chR_Carrier_Fr.value
            val dev = (if (ch == 0) liveData.chL_FM_Dev.value else liveData.chR_FM_Dev.value)
                .coerceAtMost(fmDevLimit(ch))

            val min = (carrier - dev).coerceAtLeast(FM_FREQ_MIN)
            val max = carrier + dev

            if (ch == 0) {
                liveData.chLFmMin.value = min
                liveData.chLFmMax.value = max
            } else {
                liveData.chRFmMin.value = min
                liveData.chRFmMax.value = max
            }
            mode.value = 1
        } else {
            //минимум/максимум -> несущая ± девиация
            val min = (if (ch == 0) liveData.chLFmMin.value else liveData.chRFmMin.value)
                .coerceAtLeast(FM_FREQ_MIN)
            val max = (if (ch == 0) liveData.chLFmMax.value else liveData.chRFmMax.value)
                .coerceAtLeast(min)

            val carrier = (max + min) / 2f
            val dev = (max - min) / 2f

            if (ch == 0) {
                liveData.chL_Carrier_Fr.value = carrier
                liveData.chL_FM_Dev.value = dev
            } else {
                liveData.chR_Carrier_Fr.value = carrier
                liveData.chR_FM_Dev.value = dev
            }
            mode.value = 0
        }
    }

    /** Пересчитать буфер FM и отдать его в нативный рендер */
    fun updateFm(ch: Int) {
        createFm(ch)
        RenderChannel().sendBuffer(
            ch, 2, if (ch == 0) chL.calculate_buffer_fm else chR.calculate_buffer_fm
        )
    }


}

data class DataLiveData(

    var chL_EN: MutableStateFlow<Boolean> = MutableStateFlow(false),              //PR PS PC
    var chL_Carrier_Filename: MutableStateFlow<String> = MutableStateFlow("Sine"),//PR PS PC
    var chL_Carrier_Fr: MutableStateFlow<Float> = MutableStateFlow(400.0f),       //PR PS PC //Частота несущей
    var chL_AM_EN: MutableStateFlow<Boolean> = MutableStateFlow(false),           //PR PS PC
    var chL_AM_Filename: MutableStateFlow<String> = MutableStateFlow("09_Ramp"),  //PR PS PC
    var chL_AM_Fr: MutableStateFlow<Float> = MutableStateFlow(8.7f),              //PR PS PC
    var chL_FM_EN: MutableStateFlow<Boolean> = MutableStateFlow(false),           //PR PS PC
    var chL_FM_Filename: MutableStateFlow<String> = MutableStateFlow("06_CHIRP"), //PR PS PC
    var chL_FM_Dev: MutableStateFlow<Float> = MutableStateFlow(1100f),            //PR PS PC //Частота базы
    var chL_FM_Fr: MutableStateFlow<Float> = MutableStateFlow(5.1f),              //PR PS PC

    var chR_EN: MutableStateFlow<Boolean> = MutableStateFlow(false),
    var chR_Carrier_Filename: MutableStateFlow<String> = MutableStateFlow("Sine"), //PR PS PC
    var chR_Carrier_Fr: MutableStateFlow<Float> = MutableStateFlow(2000.0f),     //PR PS PC Частота несущей
    var chR_AM_EN: MutableStateFlow<Boolean> = MutableStateFlow(false),          //PR PS PC
    var chR_AM_Filename: MutableStateFlow<String> = MutableStateFlow("09_Ramp"), //PR PS PC
    var chR_AM_Fr: MutableStateFlow<Float> = MutableStateFlow(8.7f),             //PR PS PC
    var chR_FM_EN: MutableStateFlow<Boolean> = MutableStateFlow(false),          //PR PS PC
    var chR_FM_Filename: MutableStateFlow<String> = MutableStateFlow("06_CHIRP"),//PR PS PC
    var chR_FM_Dev: MutableStateFlow<Float> = MutableStateFlow(1100f),           //PR PS PC Частота базы

    var chR_FM_Fr: MutableStateFlow<Float> = MutableStateFlow(5.1f),             //PR PS PC

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

    var chLAmDepth: MutableStateFlow<Float> = MutableStateFlow(1f),           //PR PS PC Глубина AM модуляции

    var chRAmDepth: MutableStateFlow<Float> = MutableStateFlow(1f),           //PR PS PC Глубина AM модуляции

    // Мастер-громкость CHL
    var chL_Master_EN: MutableStateFlow<Boolean> = MutableStateFlow(false),        //PR PS PC
    var chL_Master_Mode: MutableStateFlow<Int> = MutableStateFlow(1),              //PR PS PC 1=Плавный 2=Вкл/Выкл 3=Кнопка
    var chL_Master_Period: MutableStateFlow<Float> = MutableStateFlow(2f),         //PR PS PC сек 0.1..100
    var chL_Master_Filename: MutableStateFlow<String> = MutableStateFlow("09_Ramp"),//PR PS PC форма Плавного
    var chL_Master_TOn: MutableStateFlow<Float> = MutableStateFlow(1f),            //PR PS PC сек 0.1..100
    var chL_Master_TOff: MutableStateFlow<Float> = MutableStateFlow(1f),           //PR PS PC сек 0.1..100

    // Мастер-громкость CHR
    var chR_Master_EN: MutableStateFlow<Boolean> = MutableStateFlow(false),        //PR PS PC
    var chR_Master_Mode: MutableStateFlow<Int> = MutableStateFlow(1),              //PR PS PC
    var chR_Master_Period: MutableStateFlow<Float> = MutableStateFlow(2f),         //PR PS PC
    var chR_Master_Filename: MutableStateFlow<String> = MutableStateFlow("09_Ramp"),//PR PS PC
    var chR_Master_TOn: MutableStateFlow<Float> = MutableStateFlow(1f),            //PR PS PC
    var chR_Master_TOff: MutableStateFlow<Float> = MutableStateFlow(1f),           //PR PS PC

    // Метаморфоза несущей CHL
    var chL_Morph_EN: MutableStateFlow<Boolean> = MutableStateFlow(false),               //PR PS PC
    var chL_Morph_Mode: MutableStateFlow<Int> = MutableStateFlow(1),                     //PR PS PC 0=Ступень 1=Плавно
    var chL_Morph_Time: MutableStateFlow<Float> = MutableStateFlow(2f),                  //PR PS PC сек 0.1..100, длительность шага
    var chL_Morph_Slot0_EN: MutableStateFlow<Boolean> = MutableStateFlow(true),          //PR PS PC
    var chL_Morph_Slot1_EN: MutableStateFlow<Boolean> = MutableStateFlow(true),          //PR PS PC
    var chL_Morph_Slot2_EN: MutableStateFlow<Boolean> = MutableStateFlow(false),         //PR PS PC
    var chL_Morph_Slot0_Filename: MutableStateFlow<String> = MutableStateFlow("Sine"),   //PR PS PC
    var chL_Morph_Slot1_Filename: MutableStateFlow<String> = MutableStateFlow("Square"), //PR PS PC
    var chL_Morph_Slot2_Filename: MutableStateFlow<String> = MutableStateFlow("Ramp"),   //PR PS PC

    // Метаморфоза несущей CHR
    var chR_Morph_EN: MutableStateFlow<Boolean> = MutableStateFlow(false),               //PR PS PC
    var chR_Morph_Mode: MutableStateFlow<Int> = MutableStateFlow(1),                     //PR PS PC 0=Ступень 1=Плавно
    var chR_Morph_Time: MutableStateFlow<Float> = MutableStateFlow(2f),                  //PR PS PC сек 0.1..100, длительность шага
    var chR_Morph_Slot0_EN: MutableStateFlow<Boolean> = MutableStateFlow(true),          //PR PS PC
    var chR_Morph_Slot1_EN: MutableStateFlow<Boolean> = MutableStateFlow(true),          //PR PS PC
    var chR_Morph_Slot2_EN: MutableStateFlow<Boolean> = MutableStateFlow(false),         //PR PS PC
    var chR_Morph_Slot0_Filename: MutableStateFlow<String> = MutableStateFlow("Sine"),   //PR PS PC
    var chR_Morph_Slot1_Filename: MutableStateFlow<String> = MutableStateFlow("Square"), //PR PS PC
    var chR_Morph_Slot2_Filename: MutableStateFlow<String> = MutableStateFlow("Ramp"),   //PR PS PC

    // Общая кнопка мастер-громкости (runtime, НЕ в пресетах)
    var masterButton: MutableStateFlow<Boolean> = MutableStateFlow(false),

    //Количество звезд
    val star: MutableStateFlow<Int> = MutableStateFlow(0),                    //PR PS PC

    //Имя текущего пресета
    val presetsName: MutableStateFlow<String> = MutableStateFlow(""),         //PR PS(name) PC

    val chLFmMin: MutableStateFlow<Float> = MutableStateFlow(1000.0f), //PR PS PC CHL FM min
    val chLFmMax: MutableStateFlow<Float> = MutableStateFlow(2000.0f), //PR PS PC CHL FM max
    val chRFmMin: MutableStateFlow<Float> = MutableStateFlow(1500.0f), //PR PS PC CHR FM min
    val chRFmMax: MutableStateFlow<Float> = MutableStateFlow(2500.0f), //PR PS PC CHR FM max


    val parameterInt0: MutableStateFlow<Int> = MutableStateFlow(0), //PR PS PC CHL режим выбора частот FM модуляции 0-обычный 1-минимум макс
    val parameterInt1: MutableStateFlow<Int> = MutableStateFlow(0), //PR PS PC CHR режим выбора частот FM модуляции 0-обычный 1-минимум макс

    var count: Int = 0

)

data class StructureCh(
    var ch: Int = 0, //Номер канала 0 1

    //Буфферы

    var buffer_carrier: FloatArray = FloatArray(1024), //-1..1

    //0..4095 -> 0..1
    var buffer_am: FloatArray = FloatArray(1024),      //0..1

    //Форма Плавного режима мастер-громкости, 0..1 (дефолт 1.0 = пропуск сигнала)
    var buffer_master: FloatArray = FloatArray(1024) { 1f },

    var buffer_fm: FloatArray = FloatArray(1024),      //-1..1

    //Слоты форм несущей для метаморфозы, -1..1
    var buffer_morph: Array<FloatArray> = Array(3) { FloatArray(1024) },

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

