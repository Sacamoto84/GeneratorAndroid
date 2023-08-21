package com.example.generator2.generator

import com.example.generator2.model.itemList
import com.example.generator2.util.bufMerge
import kotlinx.coroutines.flow.MutableStateFlow

val gen = Generator()

class Generator {

    val liveData: DataLiveData = DataLiveData()

    var itemlistCarrier: ArrayList<itemList> = ArrayList() //Создать список
    var itemlistAM: ArrayList<itemList> = ArrayList()      //Создать список
    var itemlistFM: ArrayList<itemList> = ArrayList()      //Создать список

    val ch1: StructureCh = StructureCh(ch = 0)
    val ch2: StructureCh = StructureCh(ch = 1)

    fun renderAudio(numFrames: Int = 1024): ShortArray {

        val enL = gen.liveData.enL.value
        val enR = gen.liveData.enR.value

        val buf: FloatArray
        val out = ShortArray(numFrames)

        if (!gen.liveData.mono.value) {

            //stereo
            val l = renderChanel(ch1, numFrames / 2)
            val r = renderChanel(ch2, numFrames / 2)

            //Нормальный режим
            buf = if (!gen.liveData.shuffle.value)
                bufMerge(r, l, enL, enR)
            else
                bufMerge(l, r, enL, enR)

        } else {
            //Mono
            val m = renderChanel(ch1, numFrames / 2)

            buf = if (!gen.liveData.invert.value)
                bufMerge(m, m, enL, enR)
            else
                bufMerge(m, m, enL, enR, true)

        }

        val max = Short.MAX_VALUE - 1

        buf.forEachIndexed { i, v ->
            out[i] = (v * max).toInt().toShort()
        }

        return out

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

    private fun renderChanel(CH: StructureCh, numFrames: Int): FloatArray {

        var o: Float

        val rC: UInt
        val rAM: UInt
        val rFM: UInt

        val enCH: Boolean
        val enAM: Boolean
        val enFM: Boolean

        val volume: Float
        val amDepth: Float

        if (CH.ch == 0) {
            rC = convertHzToR(liveData.ch1_Carrier_Fr.value).toUInt()
            rAM = convertHzToR(liveData.ch1_AM_Fr.value).toUInt()
            rFM = convertHzToR(liveData.ch1_FM_Fr.value).toUInt()
            enCH = liveData.ch1_EN.value
            enAM = liveData.ch1_AM_EN.value
            enFM = liveData.ch1_FM_EN.value
            volume = liveData.volume0.value
            amDepth = liveData.ch1AmDepth.value
        } else {
            rC = convertHzToR(liveData.ch2_Carrier_Fr.value).toUInt()
            rAM = convertHzToR(liveData.ch2_AM_Fr.value).toUInt()
            rFM = convertHzToR(liveData.ch1_FM_Fr.value).toUInt()
            enCH = liveData.ch2_EN.value
            enAM = liveData.ch2_AM_EN.value
            enFM = liveData.ch2_FM_EN.value
            volume = liveData.volume1.value
            amDepth = liveData.ch2AmDepth.value
        }

        //std::fill_n(CH->mBuffer, numFrames, 0);

        val mBuffer: FloatArray = FloatArray(numFrames)

        for (i in 0 until numFrames) {

            if (enCH) {

                if (enFM) {
                    CH.phase_accumulator_fm += rFM
                    CH.phase_accumulator_carrier += convertHzToR(
                        CH.buffer_fm[CH.phase_accumulator_fm.shr(
                            22
                        ).toInt()].toFloat()
                    ).toUInt()
                } else
                    CH.phase_accumulator_carrier += rC

                if (enAM) {
                    CH.phase_accumulator_am += rAM
                    //-1..1
                    o = volume * (CH.buffer_carrier[CH.phase_accumulator_carrier.shr(22)
                        .toInt()].toFloat() - 2048.0F) / 2048.0F *
                            map(
                                (CH.buffer_am[CH.phase_accumulator_am.shr(22)
                                    .toInt()].toFloat() / 4095.0F),
                                0.0F,
                                1.0F,
                                1.0F - amDepth,
                                1.0F
                            )
                } else
                    o = volume * (CH.buffer_carrier[CH.phase_accumulator_carrier.shr(22)
                        .toInt()].toFloat() - 2048.0F) / 2048.0F

            } else
                o = 0F
            mBuffer[i] = o
        }
        return mBuffer
    }

    private fun convertHzToR(hz: Float): Float {
        return (hz * 16384.0f / 3.798f * 2.0f * 1000.0 / 48.8 / 2.0 * 1000.0 / 988.0).toFloat()
    }

    private fun map(x: Float, in_min: Float, in_max: Float, out_min: Float, out_max: Float): Float {
        return (x - in_min) * (out_max - out_min) / (in_max - in_min) + out_min
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

    val parameterFloat0: MutableStateFlow<Float> = MutableStateFlow(1000.0f), //PR PS PC CH1 FM min
    val parameterFloat1: MutableStateFlow<Float> = MutableStateFlow(2000.0f), //PR PS PC CH1 FM max
    val parameterFloat2: MutableStateFlow<Float> = MutableStateFlow(1500.0f), //PR PS PC CH2 FM min
    val parameterFloat3: MutableStateFlow<Float> = MutableStateFlow(2500.0f), //PR PS PC CH2 FM max
    val parameterFloat4: MutableStateFlow<Float> = MutableStateFlow(0.0f),    //PR PS PC
    val parameterFloat5: MutableStateFlow<Float> = MutableStateFlow(0.0f),    //PR PS PC
    val parameterFloat6: MutableStateFlow<Float> = MutableStateFlow(0.0f),    //PR PS PC
    val parameterFloat7: MutableStateFlow<Float> = MutableStateFlow(0.0f),    //PR PS PC

    val parameterInt0: MutableStateFlow<Int> = MutableStateFlow(0), //PR PS PC CH1 режим выбора частот FM модуляции 0-обычный 1-минимум макс
    val parameterInt1: MutableStateFlow<Int> = MutableStateFlow(0), //PR PS PC CH2 режим выбора частот FM модуляции 0-обычный 1-минимум макс
    val parameterInt2: MutableStateFlow<Int> = MutableStateFlow(0), //PR PS PC
    val parameterInt3: MutableStateFlow<Int> = MutableStateFlow(0), //PR PS PC
    val parameterInt4: MutableStateFlow<Int> = MutableStateFlow(0), //PR PS PC
    val parameterInt5: MutableStateFlow<Int> = MutableStateFlow(0), //PR PS PC
    val parameterInt6: MutableStateFlow<Int> = MutableStateFlow(0), //PR PS PC
    val parameterInt7: MutableStateFlow<Int> = MutableStateFlow(0), //PR PS PC

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

