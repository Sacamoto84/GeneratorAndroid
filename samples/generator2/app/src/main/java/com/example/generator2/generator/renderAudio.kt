package com.example.generator2.generator

import com.example.generator2.model.LiveData


@OptIn(ExperimentalUnsignedTypes::class)
fun renderChanel(CH: StructureCh, numFrames: Int): FloatArray {

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
        rC = convertHzToR(LiveData.ch1_Carrier_Fr.value).toUInt()
        rAM = convertHzToR(LiveData.ch1_AM_Fr.value).toUInt()
        rFM = convertHzToR(LiveData.ch1_FM_Fr.value).toUInt()
        enCH = LiveData.ch1_EN.value
        enAM = LiveData.ch1_AM_EN.value
        enFM = LiveData.ch1_FM_EN.value
        volume = LiveData.volume0.value
        amDepth = LiveData.ch1AmDepth.value
    } else {
        rC = convertHzToR(LiveData.ch2_Carrier_Fr.value).toUInt()
        rAM = convertHzToR(LiveData.ch2_AM_Fr.value).toUInt()
        rFM = convertHzToR(LiveData.ch1_FM_Fr.value).toUInt()
        enCH = LiveData.ch2_EN.value
        enAM = LiveData.ch2_AM_EN.value
        enFM = LiveData.ch2_FM_EN.value
        volume = LiveData.volume1.value
        amDepth = LiveData.ch2AmDepth.value
    }


    //std::fill_n(CH->mBuffer, numFrames, 0);

    val mBuffer : FloatArray = FloatArray(numFrames)

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