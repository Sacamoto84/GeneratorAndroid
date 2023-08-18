package com.example.generator2.generator

fun createFm(ch: String) {


    val carrierFr = if (ch == "CH0") gen.liveData.ch1_Carrier_Fr.value else gen.liveData.ch2_Carrier_Fr.value
    val fmDevFr = if (ch == "CH0") gen.liveData.ch1_FM_Dev.value else gen.liveData.ch2_FM_Dev.value

    val x: Int = (carrierFr - fmDevFr).toInt()
    val y: Int = (fmDevFr * 2.0F).toInt()

    val buf = if (ch == "CH0") ch1.buffer_fm else ch2.buffer_fm

    val source = if (ch == "CH0") ch1.source_buffer_fm else ch2.source_buffer_fm

    for (i in 0..1023) {
        buf[i] = (x + (y * source[i] / 4095.0F)).toInt().toShort()
    }

}

//void CreateFM_CH1(void) {
//    int x, y;
//    int i = 0;
//    x = CH1.Carrier_fr - CH1.FM_Dev;
//    y = CH1.FM_Dev * 2;
//
//    for (i = 0; i < 1024; i++)
//    CH1.buffer_fm[i] = x + (y * CH1.source_buffer_fm[i] / 4095.0F);
//}