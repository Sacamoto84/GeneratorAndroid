package com.example.generator2.features.generator

class RenderChannel {

    companion object {
        init {
            System.loadLibrary("plasma")
        }
    }

    external fun jniRenderChannel(
        CH: StructureCh,
        numFrames: Int,
        sampleRate: Int,
        //mBuffer: FloatArray,

        rC: Int,
        rAM: Int,
        rFM: Int,

        enCH: Boolean,
        enAM: Boolean,
        enFM: Boolean,

        volume: Float,
        amDepth: Float,

        channel: Int,// 0 1 номер канала

        mBuffer: FloatArray

    )

    external fun sendBuffer(ch: Int, modulation: Int, data: FloatArray)

    fun renderChanel(
        liveData: DataLiveData,
        ch: StructureCh,
        numFrames: Int,
        sampleRate: Int,
    ): FloatArray {

        val rC: UInt
        val rAM: UInt
        val rFM: UInt

        val enCH: Boolean
        val enAM: Boolean
        val enFM: Boolean

        val volume: Float
        val amDepth: Float

//        val startTime1 = System.nanoTime()
//        enCH = liveData.ch2_EN.value
//        enAM = liveData.ch2_AM_EN.value
//        enFM = liveData.ch2_FM_EN.value
//        volume = liveData.volume1.value
//        amDepth = liveData.ch2AmDepth.value
//        val endTime1 = System.nanoTime()
//        val duration1 = endTime1 - startTime1
//        println("Time 1 >>>: ${duration1 / 1000} us")

        if (ch.ch == 0) {
            rC = convertHzToR(liveData.ch1_Carrier_Fr.value, sampleRate).toUInt()
            rAM = convertHzToR(liveData.ch1_AM_Fr.value, sampleRate).toUInt()
            rFM = convertHzToR(liveData.ch1_FM_Fr.value, sampleRate).toUInt()
            enCH = liveData.ch1_EN.value
            enAM = liveData.ch1_AM_EN.value
            enFM = liveData.ch1_FM_EN.value
            volume = liveData.volume0.value
            amDepth = liveData.ch1AmDepth.value
        } else {
            rC = convertHzToR(liveData.ch2_Carrier_Fr.value, sampleRate).toUInt()
            rAM = convertHzToR(liveData.ch2_AM_Fr.value, sampleRate).toUInt()
            rFM = convertHzToR(liveData.ch2_FM_Fr.value, sampleRate).toUInt()
            enCH = liveData.ch2_EN.value
            enAM = liveData.ch2_AM_EN.value
            enFM = liveData.ch2_FM_EN.value
            volume = liveData.volume1.value
            amDepth = liveData.ch2AmDepth.value
        }

        val mBuffer = FloatArray(numFrames)

        //val endTime1 = System.nanoTime()
        //val duration1 = endTime1 - startTime1
        //println("Time 1 >>>: ${duration1 / 1000 - 3} us")

        //val startTime = System.nanoTime()

        jniRenderChannel(
            ch,
            numFrames,
            sampleRate,

            rC.toInt(),
            rAM.toInt(),
            rFM.toInt(),

            enCH,
            enAM,
            enFM,

            volume,
            amDepth,
            ch.ch, //номер канала
            mBuffer
        )

        //val endTime = System.nanoTime()
        //val duration = endTime - startTime
        //println("Time JNI>>>: ${duration / 1000 - 3} us")

        return mBuffer

    }


//    fun renderChanel(CH: StructureCh, numFrames: Int, sampleRate: Int): FloatArray {
//
//        this.sampleRate = sampleRate
//
//        val rC: UInt
//        val rAM: UInt
//        val rFM: UInt
//
//        val enCH: Boolean
//        val enAM: Boolean
//        val enFM: Boolean
//
//        val volume: Float
//        val amDepth: Float
//
//        if (CH.ch == 0) {
//            rC = convertHzToR(liveData.ch1_Carrier_Fr.value).toUInt()
//            rAM = convertHzToR(liveData.ch1_AM_Fr.value).toUInt()
//            rFM = convertHzToR(liveData.ch1_FM_Fr.value).toUInt()
//            enCH = liveData.ch1_EN.value
//            enAM = liveData.ch1_AM_EN.value
//            enFM = liveData.ch1_FM_EN.value
//            volume = liveData.volume0.value
//            amDepth = liveData.ch1AmDepth.value
//        } else {
//            rC = convertHzToR(liveData.ch2_Carrier_Fr.value).toUInt()
//            rAM = convertHzToR(liveData.ch2_AM_Fr.value).toUInt()
//            rFM = convertHzToR(liveData.ch1_FM_Fr.value).toUInt()
//            enCH = liveData.ch2_EN.value
//            enAM = liveData.ch2_AM_EN.value
//            enFM = liveData.ch2_FM_EN.value
//            volume = liveData.volume1.value
//            amDepth = liveData.ch2AmDepth.value
//        }
//
//        if (mBuffer.size != numFrames)
//            mBuffer = FloatArray(numFrames)
//
//        for (i in 0 until numFrames) {
//
//            if (enCH) {
//
//                if (enFM) {
//                    CH.phase_accumulator_fm += rFM
//                    CH.phase_accumulator_carrier += convertHzToR(
//                        CH.buffer_fm[CH.phase_accumulator_fm.shr(
//                            22
//                        ).toInt()].toFloat()
//                    ).toUInt()
//                } else
//                    CH.phase_accumulator_carrier += rC
//
//                if (enAM) {
//                    CH.phase_accumulator_am += rAM
//                    //-1..1
//                    o = volume * (CH.buffer_carrier[CH.phase_accumulator_carrier.shr(22)
//                        .toInt()].toFloat() - 2048.0F) / 2048.0F *
//                            map(
//                                (CH.buffer_am[CH.phase_accumulator_am.shr(22)
//                                    .toInt()].toFloat() / 4095.0F),
//                                0.0F,
//                                1.0F,
//                                1.0F - amDepth,
//                                1.0F
//                            )
//                } else
//                    o = volume * (CH.buffer_carrier[CH.phase_accumulator_carrier.shr(22)
//                        .toInt()].toFloat() - 2048.0F) / 2048.0F
//
//            } else
//                o = 0F
//
//            mBuffer[i] = o
//        }
//
//        return mBuffer
//    }

    private fun convertHzToR(hz: Float, sampleRate: Int): Float {
        //return (48000.0F / sampleRate) * (hz * 16384.0f / 3.798f * 2.0f * 1000.0 / 48.8 / 2.0 * 1000.0 / 988.0).toFloat()
        return (4294967296L / sampleRate) * hz
    }

//    private fun map(x: Float, in_min: Float, in_max: Float, out_min: Float, out_max: Float): Float {
//        return (x - in_min) * (out_max - out_min) / (in_max - in_min) + out_min
//    }

}